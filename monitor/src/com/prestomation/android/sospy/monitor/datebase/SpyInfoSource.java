package com.prestomation.android.sospy.monitor.datebase;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SpyInfoSource {
	// Database fields
	private SQLiteDatabase mDatabase;
	private SpySqlLiteHelper mDBHelper;
	private String[] allColumns = { SpySqlLiteHelper.COLUMN_ID, SpySqlLiteHelper.COLUMN_TEXT,
			SpySqlLiteHelper.COLUMN_TITLE, SpySqlLiteHelper.COLUMN_DATE };

	public SpyInfoSource(Context context) {
		mDBHelper = new SpySqlLiteHelper(context);
	}

	public void open() throws SQLException {
		mDatabase = mDBHelper.getWritableDatabase();
	}

	public void close() {
		mDBHelper.close();
	}

	public SpyInfo createSpyInfo(String title, String text, long epochDate) {

		ContentValues values = new ContentValues();
		values.put(SpySqlLiteHelper.COLUMN_TITLE, title);
		values.put(SpySqlLiteHelper.COLUMN_TEXT, text);
		values.put(SpySqlLiteHelper.COLUMN_DATE, epochDate);

		long insertId = mDatabase.insert(SpySqlLiteHelper.TABLE_SPY_INFO, null, values);

		Cursor cursor = mDatabase.query(SpySqlLiteHelper.TABLE_SPY_INFO, allColumns,
				SpySqlLiteHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		return cursorToSpyInfo(cursor);

	}

	public Cursor getSpyInfoCursor() {
		Cursor cursor = mDatabase.query(SpySqlLiteHelper.TABLE_SPY_INFO, allColumns, null, null,
				null, null, SpySqlLiteHelper.COLUMN_DATE + " desc");
		return cursor;

	}

	public SpyInfo cursorToSpyInfo(Cursor cursor) {
		SpyInfo info = new SpyInfo(cursor.getString(1), cursor.getString(2), cursor.getLong(3));
		return info;
	}
}
