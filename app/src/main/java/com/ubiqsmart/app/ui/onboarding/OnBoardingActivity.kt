package com.ubiqsmart.app.ui.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.ubiqsmart.R

class OnBoardingActivity : AppIntro2() {

  private var tosFragment: ToSFragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addSlide(AppIntroFragment.newInstance(getString(R.string.app_intro_title), getString(R.string.app_intro_text), R.drawable.ether_intro, Color.parseColor("#49627e")))
    tosFragment = ToSFragment()
    addSlide(tosFragment!!)

    showSkipButton(false)
    isProgressButtonEnabled = true
  }

  override fun onDonePressed(currentFragment: Fragment?) {
    super.onDonePressed(currentFragment)
    if (tosFragment!!.isToSChecked) {
      val data = Intent().apply {
        putExtra("TOS", true)
      }
      setResult(Activity.RESULT_OK, data)
      finish()
    } else {
      Toast.makeText(this, R.string.app_intro_please_agree, Toast.LENGTH_SHORT).show()
    }
  }

  companion object {

    val REQUEST_CODE = 602

    fun getStartIntent(context: Context) = Intent(context, this::class.java)
  }

}