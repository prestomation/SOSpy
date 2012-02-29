package com.prestomation.android.sospy.spy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

public abstract class ContactsUtility {
	
	/**
	 * Searches through the user's contacts and retrieves the name of the contact with the given phone number
	 * @param cr the ContentResolver
	 * @param phoneNumber the specified phone number
	 * @return the contact's name if the contact exists; otherwise, return the empty string
	 */
	public static final String getPhoneNumber(ContentResolver cr, String phoneNumber) {
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				//get contact id
				String id = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
				//check if the user has entered a phone number for the contact
				 if (Integer.parseInt(cursor.getString(
		                   cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					//query for phone number
		            Cursor phoneCursor = cr.query(
		 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		 		    null, 
		 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", 
		 		    new String[]{id}, null);
		 	        while (phoneCursor.moveToNext()) {
		 	        	//get phone number
		 	        	String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		 	        	//remove any hyphens
		 	        	number = number.replaceAll("-", "");
		 	        	if(number.equals(phoneNumber)) {
		 	        		//this contact has the specified number
		 	        		//return contact name
		 	        		String name =  cursor.getString(
		 	                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		 	        		//release resources
		 	        		phoneCursor.close();
		 	        		cursor.close();
		 	        		return name;
		 	        	}
		 	        }
		 	       phoneCursor.close();
		 	    }
			}
		}
		cursor.close();
		//none of the contacts have the specified phone number, so return the empty string
		return "";
	}
}