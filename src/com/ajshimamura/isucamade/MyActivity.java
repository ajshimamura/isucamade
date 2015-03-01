package com.ajshimamura.isucamade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
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
  final int MX = 0;
  final int BS11 = 1;
  final String[] KEYS = {"MX", "BS11", "D"};
  final String[] LABELS = {"TOKYO MX", "BS11", "dアニメストア"};
  final String[] HASHTAGS = {"tokyomx", "bs11", "dアニメストア"};
  private int channel;
  private ShareActionProvider mShareActionProvider;
  private Menu menu;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);

    saveChannel(readChannel());
    setContentView(R.layout.main);
  }

  private int readChannel() {
    SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
    channel = sharedPref.getInt(getString(R.string.preference_channel), MX);
    return channel;
  }

  private void saveChannel(int channel) {
    SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putInt(getString(R.string.preference_channel), channel);
    editor.apply();
  }

  @Override
  protected void onRestart() {
    super.onRestart();

    setTime();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_activity_actions, menu);

    MenuItem item = menu.findItem(R.id.menu_item_share);
    mShareActionProvider = (ShareActionProvider) item.getActionProvider();

    setTime();

    this.menu = menu;
    setIcon();

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_refresh:
        refresh();
        return true;
      case R.id.change_channel:
        changeChannel();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void changeChannel() {
    new AlertDialog.Builder(this)
      .setTitle("局かえる？")
      .setItems(LABELS, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            if (channel != which) {
              channel = which;
              saveChannel(channel);
              setIcon();
              setTime();
            }
          }
        }
      )
      .show();
  }

  private void setIcon() {
    MenuItem item = menu.findItem(R.id.change_channel);
    int icon = getResources().getIdentifier(KEYS[channel].toLowerCase(), "drawable", getPackageName());
    Log.d("isucamade", String.format("%d", icon));
    item.setIcon(icon);
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
      sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_text), min, HASHTAGS[channel]));
      sendIntent.setType("text/plain");
      setShareIntent(sendIntent);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void textFill() {
    TextView message = (TextView) findViewById(R.id.textView);
    if (message.getLineCount() != 1) {
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
      while (i != -1) {
        bo.write(i);
        i = in.read();
      }
      Log.d("isuca", bo.toString());
      try {
        JSONObject json = new JSONObject(bo.toString());
        JSONArray data = json.getJSONArray(KEYS[channel]);

        Time nextIsuca = new Time();
        nextIsuca.set(data.getInt(0), data.getInt(1), data.getInt(2), data.getInt(3), data.getInt(4) - 1, data.getInt(5));
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
