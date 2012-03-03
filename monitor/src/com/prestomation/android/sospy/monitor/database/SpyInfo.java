package com.prestomation.android.sospy.monitor.database;

import java.util.Date;

public class SpyInfo {

	private String mTitle;
	private String mText;
	private Date mDateTime;

	public SpyInfo(String title, String text, long msDatetime) {
		mTitle = title;
		mText = text;
		mDateTime = new Date(msDatetime);
	}

	@Override
	public String toString() {
		return mTitle + " " + mText;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getText() {
		return mText;
	}

	public Date getDate() {
		return mDateTime;
	}
}
