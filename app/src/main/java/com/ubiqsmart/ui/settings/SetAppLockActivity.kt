package com.ubiqsmart.ui.settings

import android.os.Bundle
import android.preference.PreferenceManager
import me.zhanghai.android.patternlock.PatternUtils
import me.zhanghai.android.patternlock.PatternView
import me.zhanghai.android.patternlock.SetPatternActivity

class SetAppLockActivity : SetPatternActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onSetPattern(pattern: List<PatternView.Cell>?) {
    val patternSha1 = PatternUtils.patternToSha1String(pattern!!)
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = sharedPreferences.edit()
    editor.putString("APP_LOCK_PATTERN", patternSha1)
    editor.apply()
  }
}