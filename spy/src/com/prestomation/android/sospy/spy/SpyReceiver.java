package com.prestomation.android.sospy.spy;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SpyReceiver extends BroadcastReceiver {

	public static final String SMS_TITLE = "SMS From ";
	public static final int FIFTEEN_MINUTES_IN_MS = 900000;


	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(SetupActivity.TAG, "onReceive..");
		// Tell our service to check for new messages
		// We simply use the receiving of a new message as a trigger
		context.startService(new Intent(context, SpyService.class));

		
		
		//Set an alarm to check for new SMS every fifteen minutes
		Intent smsChecker = new Intent(context, SpyReceiver.class);
		PendingIntent recurringCheck = PendingIntent.getBroadcast(context, 0, smsChecker,
				PendingIntent.FLAG_CANCEL_CURRENT);
		
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarms.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis()+FIFTEEN_MINUTES_IN_MS,
				AlarmManager.INTERVAL_FIFTEEN_MINUTES, recurringCheck);
	}

}
