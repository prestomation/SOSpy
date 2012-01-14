import os,sys
os.environ('DJANGO_SETTINGS_MODULE'] = 'pyappengine.settings'

from google.appengine.ext.webapp import util

from django.conf import settings
settings._target = None

import django.core.handlers.wsgi
import django.core.signals
import django.db
import django.dispatch.dispatcher

#unregister the rollback event handler.
django.dispatch.dispatcher.disconnect(
            django.db._rollback_on_exception,
            django.core.signals.got_request_exception)

def main():
        application = django.core.handlers.wsgi.WSGIHandler()

        util.run_wsgi_app(application)

if __name__ == '__main__':

    main()

