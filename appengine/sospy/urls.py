from django.conf.urls.defaults import *

urlpatterns = patterns('sospy.views',
    ('api/spy$', 'spy'),
    ('api/spy/(?P<devid>.+)$', 'spy'),
    ('api/register$', 'register'))
