package com.ajshimamura.isucamade;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyActivity extends Activity {
  private ShareActionProvider mShareActionProvider;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    setContentView(R.layout.main);
  }

  @Override
  protected void onRestart() {
    super.onRestart();

    setTime();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    getMenuInflater().inflate(R.menu.main_activity_actions, menu);

    MenuItem item = menu.findItem(R.id.menu_item_share);
    mShareActionProvider = (ShareActionProvider) item.getActionProvider();

    setTime();

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
      case R.id.action_refresh:
        refresh();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setShareIntent(Intent shareIntent) {
    if (mShareActionProvider != null) {
      mShareActionProvider.setShareIntent(shareIntent);
    }
  }

  private void refresh() {
    setTime();
  }

  private void setTime() {
    try {
      Time nextIsuca = null;
      nextIsuca = getNextIsuca();

      Time current = new Time();
      current.setToNow();

      long min = (nextIsuca.toMillis(false) - current.toMillis(false)) / 1000 / 60;

      TextView time = (TextView) findViewById(R.id.output_time);
      time.setText(String.format("%d分", min));

      textFill();

      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_text), min));
      sendIntent.setType("text/plain");
      setShareIntent(sendIntent);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void textFill() {
    TextView message = (TextView) findViewById(R.id.textView);
    if ( message.getLineCount() != 1 ) {
      message.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, message.getTextSize());
      message.post(new Runnable() {
        @Override
        public void run() {
          TextView message = (TextView) findViewById(R.id.textView);
          if (message.getLineCount() > 1) {
            message.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, message.getTextSize() - 5);
            textFill();
          }
        }
      });
    }
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
