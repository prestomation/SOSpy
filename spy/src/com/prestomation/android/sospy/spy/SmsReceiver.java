package com.prestomation.android.sospy.spy;

import java.util.Set;

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

		//Tell our service to check for new messages
		//We simply use the receiving of a new message as a trigger
		context.startService(new Intent(context, SmsService.class));
	}

}
