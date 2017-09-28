package com.ubiqsmart.app.ui.base

import android.content.Intent

abstract class SecureAppCompatActivity : BaseActivity() {

  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == AppLockActivity.REQUEST_CODE) {
      AppLockActivity.handleLockResponse(this, resultCode)
    }
  }

  public override fun onResume() {
    super.onResume()
    AppLockActivity.protectWithLock(this, true)
  }

  public override fun onPause() {
    super.onPause()
    AppLockActivity.protectWithLock(this, false)
  }
}
