package com.prestomation.android.sospy.monitor;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

public class CloudRegistrar {

	public static final String MONITOR_REGISTRATION_PATH = "/api/register";
	public static final String STATUS_EXTRA = "Status";
	public static final int REGISTERED_STATUS = 1;
	public static final int AUTH_ERROR_STATUS = 2;
	public static final int UNREGISTERED_STATUS = 3;
	public static final int ERROR_STATUS = 4;
	public static final int INVALID_ID_STATUS = 5;
	public static final String TAG = "SOSpyM";

	static final String EMAIL_ID = "sospyc2dm@gmail.com";

	public static void registerWithCloud(final Context ctx, final String sospyid,
			final String deviceRegID) {
		new Thread(new Runnable() {
			public void run() {
				Intent updateUI = new Intent(SetupActivity.UPDATE_UI_ACTION);
				try {
					String uuid = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);

					HttpResponse res = makeRequest(ctx, deviceRegID, sospyid, uuid, MONITOR_REGISTRATION_PATH);

					if (res.getStatusLine().getStatusCode() == 200) {
						SharedPreferences.Editor prefseditor = Prefs.get(ctx).edit();
						prefseditor.putString("deviceRegID", deviceRegID);
						prefseditor.commit();
						updateUI.putExtra(STATUS_EXTRA, REGISTERED_STATUS);

					} else if (res.getStatusLine().getStatusCode() == 404)
					{
						//The SOSpyID does not exist on the server
						Log.w(TAG, "Invalid SOSpyID");
						updateUI.putExtra(STATUS_EXTRA, INVALID_ID_STATUS);
						
					}
					 else // Else there was a registration error
					{
						Log.w(TAG, "Registration Error");
						updateUI.putExtra(STATUS_EXTRA, ERROR_STATUS);

					}
					//updateUI.putExtra(STATUS_EXTRA, REGISTERED_STATUS);

					// This gets caught by SetupActivity to resume activation
					ctx.sendBroadcast(updateUI);

				} catch (AppEngineClient.PendingAuthException pae) {
					// ignore, this will just register at a later time
				} catch (Exception e) {
					Log.w(TAG, "Registration error " + e.getMessage());
					updateUI.putExtra(STATUS_EXTRA, ERROR_STATUS);
					ctx.sendBroadcast(updateUI);
				}
			}
		}).start();
	}

	public static void unregisterWithCloud(final Context ctx, final String accountName,
			final String nickname) {
		new Thread(new Runnable() {
			public void run() {
				try {

					HttpResponse res = makeDeleteRequest(ctx, accountName, nickname, MONITOR_REGISTRATION_PATH);

					if (res.getStatusLine().getStatusCode() != 200) {
						Log.w(TAG, "Unregistration Error");
 					}
				} catch (Exception e) {
					Log.w(TAG, "Unregistration error " + e.getMessage());
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static HttpResponse makeRequest(Context ctx, String deviceRegID, String sospyid,
			String uuid, String urlPath) throws Exception {
		SharedPreferences settings = Prefs.get(ctx);
		String googAccountName = settings.getString(SetupActivity.PREF_ACCOUNT_NAME, null);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("deviceRegID", deviceRegID));
		params.add(new BasicNameValuePair("sospyid", sospyid));
		Log.w(TAG, "uuid: " + uuid);
		params.add(new BasicNameValuePair("uuid", uuid));

		AppEngineClient client = new AppEngineClient(ctx, googAccountName);
		return client.makeRequest("POST", urlPath, params);

	}

	private static HttpResponse makeDeleteRequest(Context ctx, String googAccountName,
			String devNickname, String urlPath) throws Exception {

		AppEngineClient client = new AppEngineClient(ctx, googAccountName);
		devNickname = URLEncoder.encode(devNickname, "utf-8");

		return client.makeRequest("DELETE", urlPath, null);
	}
}
