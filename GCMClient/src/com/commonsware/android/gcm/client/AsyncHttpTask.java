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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncHttpTask extends AsyncTask<String, Void, String> {
  private Context context;

  public AsyncHttpTask(Context context) {
    super();
    this.context = context;
  }

  @Override
  protected String doInBackground(String... params) {
    // TODO Auto-generated method stub
    String result = null;
    try {
      result = postData(params[0], params[1]);
    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return result;
  }

  protected void onPostExecute(String result) {
    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
  }

  public String postData(String path, String data) throws ClientProtocolException, IOException {
    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, Constants.TIMEOUT_MILLISEC);
    HttpConnectionParams.setSoTimeout(httpParams, Constants.TIMEOUT_MILLISEC);
    HttpClient client = new DefaultHttpClient(httpParams);


    HttpPost request = new HttpPost(Constants.serverUrl + path);
    request.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));
    request.setHeader("Content-type", "application/json");
    Log.i("MainActivity", data.toString());

    HttpResponse response = client.execute(request);
    return EntityUtils.toString(response.getEntity());
  }

}