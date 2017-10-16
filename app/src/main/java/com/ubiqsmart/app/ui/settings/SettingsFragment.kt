package com.ubiqsmart.app.ui.settings

import android.os.Bundle
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import com.ubiqsmart.R

class SettingsFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
    addPreferencesFromResource(R.xml.application_preferences)
  }

}