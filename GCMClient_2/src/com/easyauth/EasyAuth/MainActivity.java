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

package com.easyauth.EasyAuth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easyauth.EasyAuth.BuildConfig;
import com.easyauth.EasyAuth.R;
import com.easyauth.EasyAuth.googleAuthenticator.OtpProvider;
import com.easyauth.EasyAuth.googleAuthenticator.OtpSourceException;
import com.easyauth.EasyAuth.googleAuthenticator.TotpClock;
import com.easyauth.EasyAuth.googleAuthenticator.TotpCounter;
import com.easyauth.EasyAuth.googleAuthenticator.Utilities;
import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
  static final String SENDER_ID = "490936602980";
  EditText usernameEditText;
  TextView totpTokenTextView, totpTokenTimerView;
  Button registerButton;

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
    totpTokenTextView = (TextView) findViewById(R.id.totpTokenTextView);
    totpTokenTimerView = (TextView) findViewById(R.id.totpTokenTimerView);
    registerButton = (Button) findViewById(R.id.registerButton);
    
    SharedPreferences prefs =
        getSharedPreferences(Constants.EASYAUTH_PREFERENCES_KEY, Context.MODE_PRIVATE);
    String username = prefs.getString(Constants.EASYAUTH_USERNAME_KEY, "");
    final String secret = prefs.getString(Constants.EASYAUTH_TOTP_SECRET_KEY, "");

    // Show the token and the register new button if a username is already registered in the device.
    // Else show the username and register button.
    if (!username.isEmpty() && !secret.isEmpty()) {
      usernameEditText.setVisibility(View.GONE);
      registerButton.setVisibility(View.GONE);
      totpTokenTextView.setVisibility(View.VISIBLE);
      totpTokenTimerView.setVisibility(View.VISIBLE);
      
      Thread t = new Thread() {

        @Override
        public void run() {
          try {
            while (!isInterrupted()) {
              Thread.sleep(1000);
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  String totpToken = getTotpToken(secret);
                  totpTokenTextView.setText(totpToken);
                  
                  long timeRemaining = getTotpTimeRemaining();
                  totpTokenTimerView.setText(String.valueOf(timeRemaining));
                }
              });
            }
          } catch (InterruptedException e) {
          }
        }
      };

      t.start();
    } else {
      usernameEditText.setVisibility(View.VISIBLE);
      registerButton.setVisibility(View.VISIBLE);
      totpTokenTextView.setVisibility(View.GONE);
      totpTokenTimerView.setVisibility(View.GONE);
    }
  }
  
  private String getTotpToken(String secret) {
    TotpClock totpClock = new TotpClock(this.getApplicationContext());
    OtpProvider otpProvider = new OtpProvider(null, totpClock);
    TotpCounter totpCounter = otpProvider.getTotpCounter();
    long otp_state = totpCounter.getValueAtTime(Utilities.millisToSeconds(totpClock.currentTimeMillis()));
    String totpToken = "";
    try {
      totpToken = otpProvider.computePin(secret, otp_state);
    } catch (OtpSourceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return totpToken;
  }
  
  // Returns time remaining in seconds
  private long getTotpTimeRemaining() {
    TotpClock totpClock = new TotpClock(this.getApplicationContext());
    OtpProvider otpProvider = new OtpProvider(null, totpClock);
    TotpCounter totpCounter = otpProvider.getTotpCounter();
    long otp_state = totpCounter.getValueAtTime(Utilities.millisToSeconds(totpClock.currentTimeMillis()));
    return totpCounter.getTimeStep() - Utilities.millisToSeconds(System.currentTimeMillis()) % totpCounter.getTimeStep();
  }

  public void onRegisterClick(View v) throws ClientProtocolException, IOException {
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
