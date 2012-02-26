from django.core.urlresolvers import reverse

from django.http import HttpResponse, HttpResponseNotFound, HttpResponseRedirect
from .models import C2DMInfo
from .forms import UpdateTokenForm 
from django.views.generic.simple import direct_to_template

import logging
logging.getLogger().setLevel(logging.DEBUG)

def updatetoken(request):
    if request.method == "POST":
        logging.error("updating token!")
        form = UpdateTokenForm(request.POST)
        if form.is_valid():
            logging.error("form is valid, updating")
            form.save()
    else:
        form = UpdateTokenForm()
    return direct_to_template(request, 'admin/updatetoken.html',
            {'form' : form, 'uploads' : C2DMInfo.objects.all()})
