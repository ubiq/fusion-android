package com.ubiqsmart.app.interfaces;

import okhttp3.Response;

@Deprecated
public interface NetworkUpdateListener {

  void onUpdate(Response s);
}
