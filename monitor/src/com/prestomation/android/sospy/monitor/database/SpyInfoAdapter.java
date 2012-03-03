package com.prestomation.android.sospy.monitor.database;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

public class SpyInfoAdapter extends SimpleCursorAdapter {

		private Context context;
		private int layout;
	
	
	public SpyInfoAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		
		this.context = context;
		this.layout = layout;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {

		//TextView
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		
		Cursor c = getCursor();
		
		return null;
	}

}
