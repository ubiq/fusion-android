package com.ubiqsmart.app.interfaces;

import android.hardware.fingerprint.FingerprintManager;

@Deprecated
public interface FingerprintListener {

  void authenticationFailed(String error);

  void authenticationSucceeded(FingerprintManager.AuthenticationResult result);
}