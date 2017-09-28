package com.ubiqsmart.domain.models;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Token implements Comparable {

  private String name;
  private String shorty;
  private BigDecimal balance;
  private int digits;
  private double usdprice;
  private String contractAddress;
  private String totalSupply;
  private long holderCount;
  private long createdAt;

  public Token(String name, String shorty, BigDecimal balance, int digits, double usdprice, String contractAddress, String totalSupply, long holderCount,
      long createdAt) {
    this.name = name;
    this.shorty = shorty;
    this.balance = balance;
    this.digits = digits;
    this.usdprice = usdprice;
    this.contractAddress = contractAddress;
    this.totalSupply = totalSupply;
    this.holderCount = holderCount;
    this.createdAt = createdAt;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShorty() {
    return shorty;
  }

  public void setShorty(String shorty) {
    this.shorty = shorty;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public double getBalanceDouble() {
    return balance.divide((new BigDecimal("10").pow(digits))).doubleValue();
  }

  public long getTotalSupplyLong() {
    return new BigInteger(totalSupply).divide((new BigInteger("10").pow(digits))).longValue();
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public int getDigits() {
    return digits;
  }

  public void setDigits(int digits) {
    this.digits = digits;
  }

  public double getUsdprice() {
    return usdprice;
  }

  public void setUsdprice(double usdprice) {
    this.usdprice = usdprice;
  }

  public String getContractAddress() {
    return contractAddress;
  }

  public void setContractAddress(String contractAddress) {
    this.contractAddress = contractAddress;
  }

  public String getTotalSupply() {
    return totalSupply;
  }

  public void setTotalSupply(String totalSupply) {
    this.totalSupply = totalSupply;
  }

  public long getHolderCount() {
    return holderCount;
  }

  public void setHolderCount(long holderCount) {
    this.holderCount = holderCount;
  }

  @Override public int compareTo(@NonNull Object o) {
    return ((Token) o).getShorty().compareTo(shorty);
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Token that = (Token) o;

    return digits == that.digits && name.equals(that.name) && shorty.equals(that.shorty);

  }

  @Override public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + shorty.hashCode();
    result = 31 * result + digits;
    return result;
  }
}
