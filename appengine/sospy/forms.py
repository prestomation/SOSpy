from django import forms
from .models import C2DMInfo

class UpdateTokenForm(forms.ModelForm):
    class Meta:
        model = C2DMInfo
