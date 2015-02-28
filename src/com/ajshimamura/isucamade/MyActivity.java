package com.ajshimamura.isucamade;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyActivity extends Activity {
  private TextView message;
  private int counter = 0;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);

    setTime();
  }

  private void setTime() {
    TextView time = (TextView) findViewById(R.id.time);

    Time nextIsuca = null;
    try {
      nextIsuca = getNextIsuca();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Time current = new Time();
    current.setToNow();

    long min = (nextIsuca.toMillis(false) - current.toMillis(false)) / 1000 / 60;
    time.setText(String.format("%d分", min));
  }

  private Time getNextIsuca() throws IOException {
    URL url = new URL("http://106.185.39.185/isuca.json");
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    try {
      InputStream in = new BufferedInputStream(urlConnection.getInputStream());
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      int i = in.read();
      while(i != -1) {
        bo.write(i);
        i = in.read();
      }
      Log.d("isuca",bo.toString());
      try {
        JSONObject json = new JSONObject(bo.toString());
        JSONArray mx = json.getJSONArray("mx");

        Time nextIsuca = new Time();
        nextIsuca.set(mx.getInt(0), mx.getInt(1), mx.getInt(2), mx.getInt(3), mx.getInt(4)-1, mx.getInt(5));
        return nextIsuca;

      } catch (JSONException e) {
        e.printStackTrace();
      }
    } finally {
      urlConnection.disconnect();
    }

    return null;
  }
}
