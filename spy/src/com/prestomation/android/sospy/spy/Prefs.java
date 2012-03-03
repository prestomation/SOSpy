package com.prestomation.android.sospy.spy;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
	
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

}
