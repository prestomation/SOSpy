/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prestomation.android.sospy.spy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

/**
 * AppEngine client. Handles auth.
 */
public class AppEngineClient {

	static final String BASE_URL = "https://sospyer.appspot.com/api/spy/";
	private final String mDevID;

	public AppEngineClient(String devID) {
		this.mDevID = devID;
	}

	public void sendSpyData(final String title, final String text) {
		// Spawn a new thread because we can't do network IO in the main thread
		// in ICS
		new Thread(new Runnable() {
			public void run() {

				URI uri;
				try {
					uri = new URI(BASE_URL + mDevID);
					HttpUriRequest request = new HttpPost(uri);

					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("title", title));
					params.add(new BasicNameValuePair("text", text));

					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
					((HttpPost) request).setEntity(entity);
					DefaultHttpClient client = new DefaultHttpClient();
					HttpResponse res = client.execute(request);

				} catch (Exception e) {
					Log.e(SetupActivity.TAG, e.toString());
				}
				Log.d(SetupActivity.TAG, "Sent spy data");
				Log.d(SetupActivity.TAG, title + " " + text);

			}
		}).start();
	}
}
