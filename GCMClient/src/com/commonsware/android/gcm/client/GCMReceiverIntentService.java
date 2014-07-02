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

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMReceiverIntentService extends IntentService {
  private static String TAG = GCMReceiverIntentService.class.getSimpleName();
  public static final int NOTIFICATION_ID = 1;
  private NotificationManager mNotificationManager;
  NotificationCompat.Builder builder;

  public GCMReceiverIntentService() {
    super("GcmIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Bundle extras = intent.getExtras();
    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
    // The getMessageType() intent parameter must be the intent you received
    // in your BroadcastReceiver.
    String messageType = gcm.getMessageType(intent);

    if (!extras.isEmpty()) { // has effect of unparcelling Bundle
      /*
       * Filter messages based on message type. Since it is likely that GCM will be extended in the
       * future with new message types, just ignore any message types you're not interested in, or
       * that you don't recognize.
       */
      if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
        sendNotification(extras);
      } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
        sendNotification(extras);
        // If it's a regular GCM message, do some work.
      } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
        Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
        // Post notification of received message.
        sendNotification(extras);
        Log.i(TAG, "Received: " + extras.toString());
      }
    }
    // Release the wake lock provided by the WakefulBroadcastReceiver.
    GCMBroadcastReceiver.completeWakefulIntent(intent);
  }

  // Put the message into a notification and post it.
  // This is just one simple example of what you might choose to do with
  // a GCM message.
  private void sendNotification(Bundle extras) {

    String token = extras.getString("token");
    String username = extras.getString("username");

    if (token.isEmpty()) {
      Log.e(TAG, "Empty token received");
    }
    if (username.isEmpty()) {
      Log.e(TAG, "Empty username received");
    }

    mNotificationManager =
        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

    PendingIntent contentIntent =
        PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("EasyAuth Notification")
            .setStyle(new NotificationCompat.BigTextStyle().bigText("EasyAuth Login Performed"))
            .setContentText("EasyAuth Login Performed by " + username);

    mBuilder.setContentIntent(contentIntent);
    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    JSONObject json = new JSONObject();
    try {
      json.put("token", token);
      json.put("username", username);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    new AsyncHttpTask(this.getApplicationContext()).execute("ip", json.toString());
  }
}
