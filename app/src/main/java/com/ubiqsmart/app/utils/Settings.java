package com.ubiqsmart.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

@Deprecated
public class Settings {

  public static boolean showTransactionsWithZero = false;
  public static boolean walletBeingGenerated = false;

  public static void initiate(Context c) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    showTransactionsWithZero = prefs.getBoolean("zeroAmountSwitch", false);
  }

}
