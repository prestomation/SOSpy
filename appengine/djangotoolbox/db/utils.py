from copy import deepcopy
from django.db import models

def duplicate(entity):
    # TODO: find a better way to do this
    return deepcopy(entity)

class ReferencePrefetcher(object):
    def __init__(self, cache=None):
        self._cache = {}
        if cache:
            for entity in cache:
                self._add_to_cache(entity)
        self._model_fk_fields = {}

    def prefetch(self, entities, fields=None, max_depth=0):
        if not entities:
            return entities
        if fields is not None and not isinstance(fields, (list, tuple)):
            fields = (fields,)
        if fields is not None:
            opts = entities[0].__class__._meta
            fields = [opts.get_field(name) for name in fields]
        refs = {}
        for entity in entities:
            self._add_to_cache(entity)
            self._collect_refs(entity, fields, refs)
        self._fill_refs(refs)
        return entities

    def _collect_refs(self, entity, fields, refs):
        self._add_to_cache(entity)
        model = entity.__class__
        if fields is None:
            fields = self._model_fk_fields.get(model)
        if fields is None:
            fields = [field for field in model._meta.local_fields
                      if isinstance(field, models.ForeignKey)]
            self._model_fk_fields[model] = fields
        for field in fields:
            pk = getattr(entity, field.attname)
            if pk is None:
                continue
            refs.setdefault(field.rel.to, {}).setdefault(pk, []).append(
                (field, entity))

    def _fill_refs(self, refs):
        for model, fill in refs.items():
            to_fetch = set()
            for pk, entities in fill.items():
                for (field, entity) in entities:
                    if model in self._cache and pk in self._cache[model]:
                        if not hasattr(entity, field.get_cache_name()):
                            cached = duplicate(self._cache[model][pk])
                            setattr(entity, field.get_cache_name(), cached)
                    else:
                        to_fetch.add(pk)
            if not to_fetch:
                continue
            result = model.objects.in_bulk(to_fetch)
            for pk, entity in result.items():
                self._add_to_cache(entity)
                for field, target in fill[pk]:
                    if not hasattr(target, field.get_cache_name()):
                        setattr(target, field.get_cache_name(), duplicate(entity))

    def _add_to_cache(self, entity):
        model = entity.__class__
        pk = getattr(entity, model._meta.pk.attname)
        if model in self._cache and pk in self._cache[model]:
            return

        self._cache.setdefault(model, {})[pk] = entity

        for field in model._meta.local_fields:
            if (not isinstance(field, models.ForeignKey) or
                    not hasattr(entity, field.get_cache_name())):
                continue
            target = getattr(entity, field.get_cache_name())
            self._add_to_cache(target)

def prefetch_references(entities, fields=None, cache=None, max_depth=0):
    return ReferencePrefetcher(cache).prefetch(entities, fields, max_depth)
