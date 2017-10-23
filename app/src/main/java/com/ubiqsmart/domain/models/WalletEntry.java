package com.ubiqsmart.domain.models;

import com.ubiqsmart.app.utils.ExchangeCalculator;

import java.math.BigDecimal;
import java.math.BigInteger;

public class WalletEntry {

  public static final byte NORMAL = 0;
  public static final byte WATCH_ONLY = 1;
  public static final byte CONTACT = 2;

  private final String name;
  private final String publicKey;
  private final BigInteger balance;
  private final byte type;

  public WalletEntry(final String name, final String publicKey) {
    this.name = name;
    this.publicKey = publicKey;
    this.balance = null;
    this.type = CONTACT;
  }

  public WalletEntry(final String name, final String publicKey, final BigInteger balance, final byte type) {
    this.name = name;
    this.publicKey = publicKey;
    this.balance = balance;
    this.type = type;
  }

  public byte getType() {
    return type;
  }

  public double getBalance() {
    final BigDecimal value = new BigDecimal(balance);
    return value.divide(ExchangeCalculator.ONE_ETHER, 8, BigDecimal.ROUND_UP).doubleValue();
  }

  public String getName() {
    return name;
  }

  public String getPublicKey() {
    return publicKey.toLowerCase();
  }

}
