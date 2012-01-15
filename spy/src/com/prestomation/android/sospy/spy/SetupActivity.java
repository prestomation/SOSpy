package com.prestomation.android.sospy.spy;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.TextView;


public class SetupActivity extends Activity {
	public static final String PREF_DEVICE_ID = "devID";
	public static final String TAG = "SOSpy";

	private String mDevID = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = Prefs.get(this);
		if (!prefs.contains(PREF_DEVICE_ID))
		{
			//If this is the first run, we must save off our unique ID
			SharedPreferences.Editor editor =  prefs.edit();
			editor.putString(PREF_DEVICE_ID, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
			editor.commit();
		}
		
		
		mDevID = Prefs.get(this).getString(PREF_DEVICE_ID, "error");
		
		setScreenContent(R.layout.greeting);
	}

	private void setScreenContent(int screenId) {
		//We only have one view, but we've got framework here incase this app expands
		setContentView(screenId);

		switch (screenId) {
		case R.layout.greeting: {
			Log.i(TAG, "Entering greeting screen");
			setGreetingScreenContent();
			break;
		}

		}

	}

	private void setGreetingScreenContent() {
		//Format our greeting with the DevID and change the TextView
		String greeting = getString(R.string.greeting);
		String formattedGreeting = String.format(greeting, mDevID);
		TextView greetingText = (TextView) findViewById(R.id.greetingText);
		greetingText.setText(formattedGreeting);

	}


}
