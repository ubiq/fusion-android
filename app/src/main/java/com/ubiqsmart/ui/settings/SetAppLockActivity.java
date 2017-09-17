package com.ubiqsmart.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
import me.zhanghai.android.patternlock.SetPatternActivity;

import java.util.*;

public class SetAppLockActivity extends SetPatternActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void onSetPattern(List<PatternView.Cell> pattern) {
    String patternSha1 = PatternUtils.patternToSha1String(pattern);
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("APP_LOCK_PATTERN", patternSha1);
    editor.apply();
  }
}