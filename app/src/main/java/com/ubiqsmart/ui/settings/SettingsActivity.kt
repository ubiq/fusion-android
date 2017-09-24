package com.ubiqsmart.ui.settings

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import com.ubiqsmart.R
import com.ubiqsmart.ui.base.BaseActivity
import com.ubiqsmart.utils.Settings

class SettingsActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupActionBar()

    fragmentManager.beginTransaction().replace(android.R.id.content, PrefsFragment()).commit()
  }

  private fun setupActionBar() {
    val rootView = findViewById<ViewGroup>(R.id.action_bar_root)

    if (rootView != null) {
      val view = layoutInflater.inflate(R.layout.activity_settings, rootView, false)
      rootView.addView(view, 0)

      val toolbar = findViewById<Toolbar>(R.id.toolbar)
      setSupportActionBar(toolbar)
    }

    val actionBar = supportActionBar
    actionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  class PrefsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.pref_general)

      val zeroAmountTx = findPreference("zeroAmountSwitch") as SwitchPreference
      zeroAmountTx.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
        Settings.showTransactionsWithZero = !zeroAmountTx.isChecked
        true
      }
    }
  }

  companion object {

    val REQUEST_CODE = 800
  }

}