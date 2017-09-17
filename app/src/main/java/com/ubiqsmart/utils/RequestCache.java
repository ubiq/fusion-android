package com.ubiqsmart.utils;

import java.util.*;

/**
 * Used for temporary caching of responses. Clears once android garbage collects
 */
public class RequestCache {

  public static final String TYPE_TOKEN = "TOKEN_";
  public static final String TYPE_TXS_NORMAL = "TXS_NORMAL_";
  public static final String TYPE_TXS_INTERNAL = "TXS_INTERNAL_";

  private static RequestCache instance;

  private Map<String, String> map = new HashMap<>();

  public static RequestCache getInstance() {
    if (instance == null) {
      instance = new RequestCache();
    }
    return instance;
  }

  public void put(String type, String address, String response) {
    map.put(type + address, response);
  }

  public String get(String type, String address) {
    return map.get(type + address);
  }

  public boolean contains(String type, String address) {
    return map.containsKey(type + address);
  }

}
