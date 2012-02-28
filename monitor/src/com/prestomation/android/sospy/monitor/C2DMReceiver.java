package com.prestomation.android.sospy.monitor;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.prestomation.android.sospy.monitor.datebase.SpyInfo;
import com.prestomation.android.sospy.monitor.datebase.SpyInfoSource;

public class C2DMReceiver extends C2DMBaseReceiver {

	public static final String TAG = "SOSpyM";
	private NotificationManager mNotifyManager;

	public C2DMReceiver() {
		super(CloudRegistrar.EMAIL_ID);
	}

	@Override
	public void onRegistered(Context ctx, String registration) {
		Log.i(TAG, "registered and got key: " + registration);
		SharedPreferences settings = Prefs.get(ctx);
		String nickname = settings.getString(SetupActivity.PREF_TARGET_SOSPY_ID, "myDevice");
		CloudRegistrar.registerWithCloud(ctx, nickname, registration);
	}

	@Override
	public void onError(Context context, String errorId) {

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received a message! ");

		Bundle extras = intent.getExtras();
		String title = "", text = "";
		if (extras != null) {
			title = (String) extras.get("title");
			text = (String) extras.get("text");
			if (title == null || text == null) {
				Log.e(TAG, "Blank title or text");
				return;
			}
		}

		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Log.i(TAG, "Title: " + title);
		Log.i(TAG, "Text: " + text);
		
		SpyInfoSource infoSource = new SpyInfoSource(getApplicationContext());
		infoSource.open();
		
		SpyInfo spyinfo = infoSource.createSpyInfo(title, text, System.currentTimeMillis());
		Log.e(TAG, "spyinfo " + spyinfo);
		 
		
		

		playNotificationSound(context);
		Notification mNotification = new Notification(R.drawable.icon, title, System
				.currentTimeMillis());
		mNotification.setLatestEventInfo(getApplicationContext(), title, text, PendingIntent
				.getActivity(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));

		mNotifyManager.notify(1, mNotification);

	}

	public static void playNotificationSound(Context context) {
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (uri != null) {
			Ringtone rt = RingtoneManager.getRingtone(context, uri);
			if (rt != null) {
				rt.setStreamType(AudioManager.STREAM_NOTIFICATION);
				rt.play();
			}
		}

	}
}
