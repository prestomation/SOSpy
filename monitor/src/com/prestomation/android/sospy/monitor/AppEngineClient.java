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

package com.prestomation.android.sospy.monitor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * AppEngine client. Handles auth.
 */
public class AppEngineClient {
	private static final String TAG = "SOSpyM";

	static final String BASE_URL = "https://sospyer.appspot.com";
	private static final String AUTH_URL = BASE_URL + "/_ah/login";
	private static final String AUTH_TOKEN_TYPE = "ah";

	private final Context mContext;
	private final String mAccountName;

	public AppEngineClient(Context context, String accountName) {
		this.mContext = context;
		this.mAccountName = accountName;

	}

	public HttpResponse makeRequest(String httpMethod, String urlPath, List<NameValuePair> params)
			throws Exception {
		HttpUriRequest request;
		URI uri = new URI(BASE_URL + urlPath);
		Log.w(TAG, uri.toString());
		
		if (httpMethod == "POST")
		{
			request = new HttpPost(uri);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			((HttpPost)request).setEntity(entity);
		}
		else if (httpMethod == "DELETE")
		{
			request = new HttpDelete(uri);
		}
		else
		{
			//This should never happen
			return null;
		}
		
		HttpResponse res = makeRequestNoRetry(request, params);
		return res;
	}

	private HttpResponse makeRequestNoRetry(HttpUriRequest request,
			List<NameValuePair> params) throws Exception {
		// Get auth token for account
		String ascidCookie = getASCIDCookie(true);

		// Make POST request
		DefaultHttpClient client = new DefaultHttpClient();

		request.setHeader("Cookie", ascidCookie);
		request.setHeader("X-Same-Domain", "1"); // XSRF
		HttpResponse res = client.execute(request);
		return res;
	}

	public String getASCIDCookie(boolean https) throws Exception {

		// Get auth token for account
		Account account = new Account(mAccountName, "com.google");
		String authToken = getAuthToken(mContext, account);
		if (authToken == null) {
			throw new PendingAuthException(mAccountName);
		}
		AccountManager accountManager = AccountManager.get(mContext);
		accountManager.invalidateAuthToken(account.type, authToken);
		authToken = getAuthToken(mContext, account);

		// Get ACSID cookie
		DefaultHttpClient client = new DefaultHttpClient();
		String continueURL = BASE_URL;
		String sURI = AUTH_URL + "?continue="
				+ URLEncoder.encode(continueURL, "UTF-8") + "&auth="
				+ authToken;
		if (https == false) {
			sURI = sURI.replace("https", "http");
		}
		URI uri = new URI(sURI);
		HttpGet method = new HttpGet(uri);
		final HttpParams getParams = new BasicHttpParams();
		HttpClientParams.setRedirecting(getParams, false); // continue is not
		// used
		method.setParams(getParams);

		HttpResponse res = client.execute(method);
		Header[] headers = res.getHeaders("Set-Cookie");
		if (res.getStatusLine().getStatusCode() != 302 || headers.length == 0) {
			return res.toString();
		}

		String ascidCookie = null;
		for (Header header : headers) {
			if (header.getValue().indexOf("ACSID=") >= 0) {
				// let's parse it
				String value = header.getValue();
				String[] pairs = value.split(";");
				ascidCookie = pairs[0];
			}
		}
		Log.i(TAG, "Received ASCIDCookie: " + ascidCookie);
		return ascidCookie;
	}

	private String getAuthToken(Context context, Account account) {
		String authToken = null;
		AccountManager accountManager = AccountManager.get(context);
		try {
			AccountManagerFuture<Bundle> future = accountManager.getAuthToken(
					account, AUTH_TOKEN_TYPE, false, null, null);
			Bundle bundle = future.getResult();
			authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
			// User will be asked for "App Engine" permission.
			if (authToken == null) {
				// No auth token - will need to ask permission from user.
				Intent intent = new Intent(SetupActivity.AUTH_PERMISSION_ACTION);
				intent.putExtra("AccountManagerBundle", bundle);
				context.sendBroadcast(intent);
			}
		} catch (OperationCanceledException e) {
			Log.w(TAG, e.getMessage());
		} catch (AuthenticatorException e) {
			Log.w(TAG, e.getMessage());
		} catch (IOException e) {
			Log.w(TAG, e.getMessage());
		}
		return authToken;
	}

	public class PendingAuthException extends Exception {
		private static final long serialVersionUID = 1L;

		public PendingAuthException(String message) {
			super(message);
		}
	}
}
