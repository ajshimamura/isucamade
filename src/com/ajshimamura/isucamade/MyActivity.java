package com.ajshimamura.isucamade;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;

public class MyActivity extends Activity {
  private TextView message;
  private int counter = 0;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);

    setTime();
  }

  private void setTime() {
    TextView time = (TextView) findViewById(R.id.time);

    Time nextIsuca = new Time();
    nextIsuca.set(0, 35, 1, 7, 3, 2015);

    Time current = new Time();
    current.setToNow();

    long min = (nextIsuca.toMillis(false) - current.toMillis(false)) / 1000 / 60;
    time.setText(String.format("%d分", min));
  }
}
