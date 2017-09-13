package com.ubiqsmart.activities;

/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.zhanghai.android.patternlock.PatternView;
import rehanced.com.ubiqsmart.R;

public class BasePatternActivity extends AppCompatActivity {

  private static final int CLEAR_PATTERN_DELAY_MILLI = 2000;

  protected TextView mmessagetext;
  protected PatternView patternView;
  protected LinearLayout buttonContainer;
  protected Button leftButton;
  protected Button rightButton;

  private final Runnable clearPatternRunnable = new Runnable() {
    public void run() {
      // clearPattern() resets display mode to DisplayMode.Correct.
      patternView.clearPattern();
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.app_lock_activity);
    mmessagetext = (TextView) findViewById(R.id.pl_message_text);
    patternView = (PatternView) findViewById(R.id.pl_pattern);
    buttonContainer = (LinearLayout) findViewById(R.id.pl_button_container);
    leftButton = (Button) findViewById(R.id.pl_left_button);
    rightButton = (Button) findViewById(R.id.pl_right_button);
  }

  protected void removeClearPatternRunnable() {
    patternView.removeCallbacks(clearPatternRunnable);
  }

  protected void postClearPatternRunnable() {
    removeClearPatternRunnable();
    patternView.postDelayed(clearPatternRunnable, CLEAR_PATTERN_DELAY_MILLI);
  }
}