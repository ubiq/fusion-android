package com.ubiqsmart.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import com.ubiqsmart.R;
import com.ubiqsmart.ui.base.BaseActivity;
import com.ubiqsmart.utils.Settings;

public class SettingsActivity extends BaseActivity {

  public static final int REQUEST_CODE = 800;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupActionBar();

    getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
  }

  private void setupActionBar() {
    final ViewGroup rootView = findViewById(R.id.action_bar_root);

    if (rootView != null) {
      View view = getLayoutInflater().inflate(R.layout.activity_settings, rootView, false);
      rootView.addView(view, 0);

      final Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
    }

    final ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  public static class PrefsFragment extends PreferenceFragment {

    @Override public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.pref_general);

      final SwitchPreference zeroAmountTx = (SwitchPreference) findPreference("zeroAmountSwitch");
      zeroAmountTx.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object o) {
          Settings.showTransactionsWithZero = !zeroAmountTx.isChecked();
          return true;
        }
      });
    }
  }

}