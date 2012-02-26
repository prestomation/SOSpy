from django.db import models

# Create your models here.


class Device(models.Model):
    Uuid = models.CharField(max_length=128, unique=True)
    class Meta:
        abstract = True
class TargetDevice(Device):
    pass

#A device to receive notifications on new info from a target device
class MonitorDevice(Device):
    C2DMID = models.CharField(max_length=300)
    sospyid = models.ForeignKey(TargetDevice)

#Info uploaded by the SOSpy mobile app
class SpyInfo(models.Model):
    device = models.ForeignKey(TargetDevice)
    title = models.CharField(max_length=50)
    text = models.CharField(max_length=256)
    datetime = models.DateTimeField(auto_now_add=True)

#Google Auth info for C2DM push notifications
class C2DMInfo(models.Model):
    user = models.CharField(max_length=100, primary_key = True)
    authtoken = models.CharField(max_length = 200)
    password = models.CharField(max_length = 64)

