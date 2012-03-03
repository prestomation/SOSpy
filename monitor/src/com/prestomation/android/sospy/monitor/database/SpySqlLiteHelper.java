package com.prestomation.android.sospy.monitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpySqlLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_SPY_INFO = "spyinfo";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TEXT = "data";

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "sospy";

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + TABLE_SPY_INFO + "( "
			+ COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TITLE
			+ " text not null, " + COLUMN_TEXT + " text not null, " + COLUMN_DATE
			+ " integer not null);";

	public SpySqlLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS" + TABLE_SPY_INFO);
		onCreate(db);

	}

}
