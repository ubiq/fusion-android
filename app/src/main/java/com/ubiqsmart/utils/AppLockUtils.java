package com.ubiqsmart.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

public class AppLockUtils {

  public static boolean hasDeviceFingerprintSupport(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      final FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
      return ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED
          && fingerprintManager.isHardwareDetected()
          && fingerprintManager.hasEnrolledFingerprints();
    } else {
      return false;
    }
  }

}
