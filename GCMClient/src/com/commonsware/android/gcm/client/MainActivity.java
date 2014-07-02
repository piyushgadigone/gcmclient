/***
 * Copyright (c) 2012 CommonsWare, LLC Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 * 
 * From _The Busy Coder's Guide to Android Development_ http://commonsware.com/Android
 */

package com.commonsware.android.gcm.client;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
  static final String SENDER_ID = "611653623637";
  EditText usernameEditText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    GCMRegistrar.checkDevice(this);

    if (BuildConfig.DEBUG) {
      GCMRegistrar.checkManifest(this);
    }

    final String regId = GCMRegistrar.getRegistrationId(this);

    if (regId.length() == 0) {
      GCMRegistrar.register(this, SENDER_ID);
    } else {
      Log.d(getClass().getSimpleName(), "Existing registration: " + regId);
      Toast.makeText(this, regId, Toast.LENGTH_LONG).show();
    }

    usernameEditText = (EditText) findViewById(R.id.usernameTextView);
  }

  public void onClick(View v) throws ClientProtocolException, IOException {
    final String regId = GCMRegistrar.getRegistrationId(this);
    if (regId.length() == 0) {
      Toast.makeText(this, "Unable to register device", Toast.LENGTH_SHORT).show();
    }
    JSONObject json = new JSONObject();
    try {
      json.put("username", usernameEditText.getText().toString());
      json.put("registrationId", regId);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    new AsyncHttpTask(this.getApplicationContext()).execute("registrationId", json.toString());
  }
}
