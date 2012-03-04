package com.prestomation.android.sospy.spy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;



public class IncomingCall extends BroadcastReceiver 
{

    @Override
    public void onReceive(final Context context, Intent intent) 
    {
            final Bundle bundle = intent.getExtras();
            
            if(null == bundle)
            {
            	return;
            }
            
            
            String state = bundle.getString(TelephonyManager.EXTRA_STATE);

            
            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
            {
                
                    // Do the actual work in a worker thread. We don't want to tie up
            		// onStartCommand as this can tie up on UI/prompt a force close
            		new Thread(new Runnable() 
            		{
            			public void run() 
            			{
            				String phonenumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            				
                    		String contact = ContactsUtility.getPhoneNumber(context.getContentResolver(), phonenumber);
                    		String devID = Prefs.getSOSpyID(context);
                            AppEngineClient client = new AppEngineClient(devID);
                            
                            Log.i(SetupActivity.TAG,"Incoming #: " + contact);
                            
                            String title = "Incoming Call from " + contact + " (" + phonenumber + ")";
                            String body = "";
                            String date = "";
            				client.sendSpyData(title, body, date);
            			}
            		}).start();

            }
            
    }


}
