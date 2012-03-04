package com.prestomation.android.sospy.spy;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;

public class Prefs {
	
	public static final String PREF_DEVICE_ID = "devID";
	
	//This is a static method for returning a global preferences instance
	public static SharedPreferences get(Context ctx) 
	{
		return ctx.getSharedPreferences("AFD_PREFS", 0);
	}
	
	public static void deletePrefs(Context ctx)
	{
		SharedPreferences.Editor settings = get(ctx).edit();
		settings.clear();
		settings.commit();
	}
	
	
	public static String getSOSpyID(Context ctx)
	{
		
		
		SharedPreferences prefs = get(ctx);
		if (!prefs.contains(PREF_DEVICE_ID)) {
			// If this is the first run, we must save off our unique ID
			SharedPreferences.Editor editor = prefs.edit();
			String devID = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
			editor.putString(PREF_DEVICE_ID, devID);
			editor.commit();

			// Sent a special token to the server so this device gets created
			// server side
			AppEngineClient client = new AppEngineClient(devID);
			client.sendSpyData(AppEngineClient.REGISTRATION_STRING, "", "");

		}
		
		
		return get(ctx).getString(PREF_DEVICE_ID, "error!");
		
		
	}

}
