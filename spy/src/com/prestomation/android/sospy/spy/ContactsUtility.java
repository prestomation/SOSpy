package com.prestomation.android.sospy.spy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public abstract class ContactsUtility {

	/**
	 * Searches through the user's contacts and retrieves the name of the
	 * contact with the given phone number
	 * 
	 * @param cr
	 *            the ContentResolver
	 * @param phoneNumber
	 *            the specified phone number
	 * @return the contact's name if the contact exists; otherwise, return the
	 *         phone number given
	 */

	public static final String getPhoneNumber(ContentResolver cr, String phoneNumber) {

		Cursor contactLookupCursor = cr.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber)),
				new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID }, null, null, null);
		try {
			while (contactLookupCursor.moveToNext()) {
				String contactName = contactLookupCursor.getString(contactLookupCursor
						.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
				Log.d(SetupActivity.TAG, "contactMatch name: " + contactName);
				return contactName;

			}
		} finally {
			contactLookupCursor.close();
		}
		return phoneNumber;
	}
}
