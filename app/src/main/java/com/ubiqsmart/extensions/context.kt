package com.ubiqsmart.extensions

import android.content.Context
import android.os.Build
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat

fun Context.hasDeviceFingerprintSupport():Boolean {
  val isAtLeastMarshmallow  = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
  val fingerprintManagerCompat = FingerprintManagerCompat.from(this)
  return isAtLeastMarshmallow && fingerprintManagerCompat.isHardwareDetected && fingerprintManagerCompat.hasEnrolledFingerprints()
}