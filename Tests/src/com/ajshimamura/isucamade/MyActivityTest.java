package com.ajshimamura.isucamade;

import android.test.ActivityInstrumentationTestCase2;
import junit.framework.Assert;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.ajshimamura.isucamade.MyActivityTest \
 * com.ajshimamura.isucamade.tests/android.test.InstrumentationTestRunner
 */
public class MyActivityTest extends ActivityInstrumentationTestCase2<MyActivity> {

  public MyActivityTest() {
    super("com.ajshimamura.isucamade", MyActivity.class);
  }
}
