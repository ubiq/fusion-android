package com.ubiqsmart.utils;

import android.content.Context;
import com.ubiqsmart.interfaces.LastIconLoaded;
import com.ubiqsmart.interfaces.StorableWallet;
import com.ubiqsmart.repository.api.EtherscanAPI;
import com.ubiqsmart.repository.data.Token;
import com.ubiqsmart.repository.data.TransactionDisplay;
import com.ubiqsmart.repository.data.Wallet;
import com.ubiqsmart.repository.data.WatchWallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Deprecated
public class ResponseParser {

  public static List<TransactionDisplay> parseTransactions(String response, String walletname, String address, byte type) {
    try {
      final List<TransactionDisplay> erg = new ArrayList<TransactionDisplay>();

      JSONArray data = new JSONObject(response).getJSONArray("result");
      for (int i = 0; i < data.length(); i++) {
        String from = data.getJSONObject(i).getString("from");
        String to = data.getJSONObject(i).getString("to");
        String vorzeichen = "+";
        if (address.equalsIgnoreCase(data.getJSONObject(i).getString("from"))) {
          vorzeichen = "-";
        } else {
          String temp = from;
          from = to;
          to = temp;
        }
        if (data.getJSONObject(i).getString("value").equals("0") && !Settings.showTransactionsWithZero) {
          continue; // Skip contract calls or empty transactions
        }
        erg.add(new TransactionDisplay(from, to, new BigInteger(vorzeichen + data.getJSONObject(i).getString("value")),
            data.getJSONObject(i).has("confirmations") ? data.getJSONObject(i).getInt("confirmations") : 13, data.getJSONObject(i).getLong("timeStamp") * 1000,
            walletname, type, data.getJSONObject(i).getString("hash"), data.getJSONObject(i).has("nonce") ? data.getJSONObject(i).getString("nonce") : "0",
            data.getJSONObject(i).getLong("blockNumber"), data.getJSONObject(i).getInt("gasUsed"),
            (data.getJSONObject(i).has("gasPrice") ? data.getJSONObject(i).getLong("gasPrice") : 0),
            (data.getJSONObject(i).has("isError") && data.getJSONObject(i).getInt("isError") == 1)));
      }

      return erg;
    } catch (JSONException e) {
      return new ArrayList<>();
    }
  }

  public static List<Wallet> parseWallets(String response, List<StorableWallet> storedwallets, Context context) throws Exception {
    ArrayList<Wallet> display = new ArrayList<Wallet>();
    JSONArray data = new JSONObject(response).getJSONArray("result");
    for (int i = 0; i < storedwallets.size(); i++) {
      BigInteger balance = new BigInteger("0");
      for (int j = 0; j < data.length(); j++) {
        if (data.getJSONObject(j).getString("account").equalsIgnoreCase(storedwallets.get(i).getPubKey())) {
          balance = new BigInteger(data.getJSONObject(i).getString("balance"));
          break;
        }
      }
      String walletname = AddressNameConverter.getInstance(context).get(storedwallets.get(i).getPubKey());
      display.add(new Wallet(walletname == null ? "New Wallet" : walletname, storedwallets.get(i).getPubKey(), balance,
          storedwallets.get(i) instanceof WatchWallet ? Wallet.WATCH_ONLY : Wallet.NORMAL));
    }
    return display;
  }

  public static ArrayList<Token> parseTokens(Context c, String response, LastIconLoaded callback) throws Exception {
    ArrayList<Token> display = new ArrayList<Token>();
    JSONArray data = new JSONObject(response).getJSONArray("tokens");
    for (int i = 0; i < data.length(); i++) {
      JSONObject currentToken = data.getJSONObject(i);
      try {
        display.add(new Token(currentToken.getJSONObject("tokenInfo").getString("name"), currentToken.getJSONObject("tokenInfo").getString("symbol"),
            new BigDecimal(currentToken.getString("balance")), currentToken.getJSONObject("tokenInfo").getInt("decimals"),
            currentToken.getJSONObject("tokenInfo").getJSONObject("price").getDouble("rate"), currentToken.getJSONObject("tokenInfo").getString("address"),
            currentToken.getJSONObject("tokenInfo").getString("totalSupply"), currentToken.getJSONObject("tokenInfo").getLong("holdersCount"),
            currentToken.getJSONObject("tokenInfo").getLong("createdAt")));
      } catch (JSONException e) {
      }

      // Download icon and cache it
      EtherscanAPI.getInstance().loadTokenIcon(c, currentToken.getJSONObject("tokenInfo").getString("name"), i == data.length() - 1, callback);

    }
    return display;
  }

  public static String parseBalance(String response) throws JSONException {
    return parseBalance(response, 7);
  }

  public static String parseBalance(String response, int comma) throws JSONException {
    String balance = new JSONObject(response).getString("result");
    if (balance.equals("0")) {
      return "0";
    }
    return new BigDecimal(balance).divide(new BigDecimal(1000000000000000000d), comma, BigDecimal.ROUND_UP).toPlainString();
  }

  public static BigInteger parseGasPrice(String response) throws Exception {
    final String gasprice = new JSONObject(response).getString("result");
    return new BigInteger(gasprice.substring(2), 16);
  }

  static double parsePriceConversionRate(String response) {
    try {
      return Double.parseDouble(response.split(",")[2]);
    } catch (Exception e) {
      return 1;
    }
  }

}
