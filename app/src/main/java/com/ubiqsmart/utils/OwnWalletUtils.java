package com.ubiqsmart.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class OwnWalletUtils extends WalletUtils {
  
  public static String generateNewWalletFile(String password, File destinationDirectory, boolean useFullScrypt)
      throws CipherException, IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

    ECKeyPair ecKeyPair = Keys.createEcKeyPair();
    return generateWalletFile(password, ecKeyPair, destinationDirectory, useFullScrypt);
  }

  public static String generateWalletFile(String password, ECKeyPair ecKeyPair, File destinationDirectory, boolean useFullScrypt)
      throws CipherException, IOException {

    WalletFile walletFile;
    if (useFullScrypt) {
      walletFile = Wallet.createStandard(password, ecKeyPair);
    } else {
      walletFile = Wallet.createLight(password, ecKeyPair);
    }

    String fileName = getWalletFileName(walletFile);
    File destination = new File(destinationDirectory, fileName);

    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    objectMapper.writeValue(destination, walletFile);

    return fileName;
  }

  private static String getWalletFileName(WalletFile walletFile) {
    return walletFile.getAddress();
  }

}