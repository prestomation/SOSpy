package com.prestomation.android.sospy.monitor;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.c2dm.C2DMessaging;
import com.prestomation.android.sospy.monitor.database.SpyInfoAdapter;
import com.prestomation.android.sospy.monitor.database.SpyInfoSource;
import com.prestomation.android.sospy.monitor.database.SpySqlLiteHelper;

public class SetupActivity extends Activity {
	public static final String UPDATE_UI_ACTION = "com.prestomation.android.sospy.UPDATE_UI";
	public static final String AUTH_PERMISSION_ACTION = "com.prestomation.android.sospy.AUTH_PERMISSION";
	public static final String PREF_SCREEN_ID = "savedScreenId";
	public static final String PREF_TARGET_SOSPY_ID = "targetSOSpyID";
	public static final String PREF_ACCOUNT_NAME = "accountName";
	public static final String TAG = "SOSpyM";

	private boolean mPendingAuth = false;
	private int mScreenId = -1;
	private int mAccountSelectedPosition = 0;
	private String mTargetSOSpyID = null;
	private ProgressDialog mProgressDialog;
	private SpyInfoSource mInfoSource;
	private SimpleCursorAdapter mInfoAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int savedScreenId = Prefs.get(this).getInt(PREF_SCREEN_ID, R.layout.accountselection);

		mTargetSOSpyID = Prefs.get(this).getString(PREF_TARGET_SOSPY_ID, android.os.Build.MODEL);

		setScreenContent(savedScreenId);

		registerReceiver(UpdateUIReceiver, new IntentFilter(UPDATE_UI_ACTION));
		registerReceiver(AuthPermissionReceiver, new IntentFilter(AUTH_PERMISSION_ACTION));

	}

	@Override
	public void onDestroy() {

		unregisterReceiver(UpdateUIReceiver);
		unregisterReceiver(AuthPermissionReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPendingAuth) {
			Log.i(TAG, "mPendingAuth is TRUE!");
			mPendingAuth = false;
			String regID = C2DMessaging.getRegistrationId(this);
			if (regID != null && !regID.equals("")) {
				CloudRegistrar.registerWithCloud(this, mTargetSOSpyID, regID);
			} else {
				C2DMessaging.register(this, CloudRegistrar.EMAIL_ID);
			}
		}
	}

	private void setScreenContent(int screenId) {
		mScreenId = screenId;

		setContentView(screenId);

		switch (screenId) {
		case R.layout.accountselection: {
			Log.i(TAG, "Entering account selection screen");
			setSelectAccountScreenContent();
			break;

		}

		case R.layout.select_options: {
			Log.i(TAG, "Entering options screen");
			setOptionsScreenContent();
			break;
		}

		}

		SharedPreferences.Editor editor = Prefs.get(this).edit();
		editor.putInt(PREF_SCREEN_ID, screenId);
		editor.commit();

	}

	private void setOptionsScreenContent() {
		// TODO Auto-generated method stub
		// Populate our options
		// Possible options:
		// 1. Automatically download file, or prompt
		// 2. Target directory for downloads

		// Set up options button
		String[] from = { SpySqlLiteHelper.COLUMN_TITLE, SpySqlLiteHelper.COLUMN_TEXT, SpySqlLiteHelper.COLUMN_DATE };
		
		int[] to = { R.id.infoTitle, R.id.infoText , R.id.infoDate};

		Button clearPrefsButton = (Button) findViewById(R.id.clearSettings);
		clearPrefsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				resetPrefs();
				setScreenContent(R.layout.accountselection);
			}
		});
		// Set up Nickname
		TextView deviceNicknameButton = (TextView) findViewById(R.id.nicknameDisplay);

		deviceNicknameButton.setText("Target Device: " + mTargetSOSpyID);
		deviceNicknameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				SharedPreferences prefs = Prefs.get(getBaseContext());
				promptForDeviceName(prefs.getString(PREF_ACCOUNT_NAME, ""));
			}
		});
		deviceNicknameButton.setEnabled(true);
		// clearPrefsButton.setEnabled(true);

		ListView infoListView = (ListView) findViewById(R.id.spyinfolist);

		mInfoSource = new SpyInfoSource(this);
		mInfoSource.open();
		Cursor infoCursor = mInfoSource.getSpyInfoCursor();
		infoCursor.moveToLast();
		startManagingCursor(infoCursor);
		mInfoAdapter = new SpyInfoAdapter(this, R.layout.spyinfo_row, infoCursor, from, to);
		infoListView.setAdapter(mInfoAdapter);
		infoListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				final TextView data = (TextView) view.findViewById(R.id.infoText);
				for (int child = 0; child < parent.getChildCount(); child++) {
					parent.getChildAt(child).findViewById(R.id.infoText).setVisibility(View.GONE);
				}

				data.setVisibility(View.VISIBLE);

			}
		});

	}

	private void setSelectAccountScreenContent() {
		Button backButton = (Button) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		final Button nextButton = (Button) findViewById(R.id.next);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ListView listview = (ListView) findViewById(R.id.AccountSelectlistView);
				mAccountSelectedPosition = listview.getCheckedItemPosition();
				TextView account = (TextView) listview.getChildAt(mAccountSelectedPosition);
				promptForDeviceName(account.getText().toString());

			}
		});
		String accounts[] = getAccounts();
		ListView accountLV = (ListView) findViewById(R.id.AccountSelectlistView);
		accountLV.setAdapter(new ArrayAdapter<String>(this, R.layout.account, accounts));
		accountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		accountLV.setItemChecked(0, true);

	}

	private String[] getAccounts() {

		ArrayList<String> accounts = new ArrayList<String>();
		for (Account ac : AccountManager.get(this).getAccounts()) {
			if (ac.type.equals("com.google")) {
				accounts.add(ac.name);
			}
		}
		String[] accountArray = new String[accounts.size()];
		accounts.toArray(accountArray);
		return accountArray;
	}

	private void registerAccount(String theAccount) {
		SharedPreferences prefs = Prefs.get(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_ACCOUNT_NAME, theAccount);
		editor.commit();
		C2DMessaging.register(this, CloudRegistrar.EMAIL_ID);
	}

	private final BroadcastReceiver UpdateUIReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mScreenId == R.layout.accountselection) {
				// We must be in the middle of selecting account/registering
				handleConnectingUpdate(intent.getIntExtra(CloudRegistrar.STATUS_EXTRA,
						CloudRegistrar.ERROR_STATUS));

			}
			// TODO: disconnecting case
			else
			// else if (mScreenId == R.layout.connected)
			{

			}
		}
	};

	private void handleConnectingUpdate(int status) {
		mProgressDialog.dismiss();
		if (status == CloudRegistrar.REGISTERED_STATUS) {
			setScreenContent(R.layout.select_options);
		} else if (status == CloudRegistrar.INVALID_ID_STATUS) {
			SharedPreferences prefs = Prefs.get(getBaseContext());

			Toast toast = Toast.makeText(this,
					"The server reports \"Invalid SOSpyID\", please enter a valid SOSpyID", 2000);
			toast.setGravity(Gravity.BOTTOM, -30, 50);
			toast.show();
			promptForDeviceName(prefs.getString(PREF_ACCOUNT_NAME, ""));
		} else {
			// There was an error
			Button nextButton = (Button) findViewById(R.id.next);
			nextButton.setEnabled(true);
			Toast toast = Toast.makeText(this, "There was an error. Please try again later", 2000);
			toast.setGravity(Gravity.BOTTOM, -30, 50);
			toast.show();
		}

	}

	private final BroadcastReceiver AuthPermissionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getBundleExtra("AccountManagerBundle");
			if (extras != null) {
				Intent authIntent = (Intent) extras.get(AccountManager.KEY_INTENT);
				if (authIntent != null) {
					mPendingAuth = true;
					startActivity(authIntent);
				}
			}
		}
	};

	private void resetPrefs() {
		String nickname = Prefs.get(this).getString(PREF_TARGET_SOSPY_ID, android.os.Build.MODEL);
		SharedPreferences settings = Prefs.get(this);
		String googAccountName = settings.getString(SetupActivity.PREF_ACCOUNT_NAME, null);
		Prefs.deletePrefs(this);

		// TODO: Unregister must be implemented server side
		// CloudRegistrar.unregisterWithCloud(this, googAccountName, nickname);
		C2DMessaging.unregister(this);
	}

	private void promptForDeviceName(final String accountName) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Target Device to monitor:");
		alert.setMessage("Please enter the SOSpyID provided by the Spy app");

		final EditText input = new EditText(this);
		input.setText(mTargetSOSpyID);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mTargetSOSpyID = input.getText().toString().trim().toLowerCase();
				SharedPreferences.Editor editor = Prefs.get(getBaseContext()).edit();
				editor.putString(PREF_TARGET_SOSPY_ID, mTargetSOSpyID);
				editor.commit();

				// Register the account
				registerAccount(accountName);
				mProgressDialog = ProgressDialog.show(SetupActivity.this, "",
						"Contacting Server. Please wait...", true);

			}
		});

		alert.show();
	}

}
