package com.ubiqsmart.repository.data;

import com.ubiqsmart.interfaces.StorableWallet;

import java.io.*;

public class WatchWallet implements StorableWallet, Serializable {

  private static final long serialVersionUID = -146500951598835658L;

  private String pubKey;
  private long dateAdded;

  public WatchWallet(String pubKey) {
    this.pubKey = pubKey.toLowerCase();
    this.dateAdded = System.currentTimeMillis();
  }

  public String getPubKey() {
    return pubKey;
  }

}
