package com.prestomation.android.sospy.monitor.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SpyInfoAdapter extends SimpleCursorAdapter {

	private Context mContext;
	private int mLayout;
	String[] mFrom;
	int[] mTo;

	public SpyInfoAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);

		this.mContext = context;
		this.mLayout = layout;
		mTo = to;
		mFrom = from;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		// title
		TextView titleView = (TextView) view.findViewById(mTo[0]);
		titleView.setText(cursor.getString(cursor.getColumnIndex(mFrom[0])));

		// text
		TextView textView = (TextView) view.findViewById(mTo[1]);
		textView.setText(cursor.getString(cursor.getColumnIndex(mFrom[1])));

		// date
		TextView dateView = (TextView) view.findViewById(mTo[2]);
		// the date is stored in milliseconds
		Date theDate = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(mFrom[2]))));

		
		//Format the date how we want
		SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yy hh:mm a");
		sf.setTimeZone(TimeZone.getDefault());
		dateView.setText(sf.format(theDate));

	}
}
