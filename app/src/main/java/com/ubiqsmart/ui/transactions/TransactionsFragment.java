package com.ubiqsmart.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.ubiqsmart.R;
import com.ubiqsmart.ui.detail.AddressDetailActivity;
import com.ubiqsmart.repository.data.TransactionDisplay;
import com.ubiqsmart.network.EtherscanAPI;
import com.ubiqsmart.utils.RequestCache;
import com.ubiqsmart.utils.ResponseParser;

import java.io.*;
import java.util.*;

import static android.view.View.GONE;

public class TransactionsFragment extends TransactionsAbstractFragment {

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = super.onCreateView(inflater, container, savedInstanceState);
    getSend().setVisibility(GONE);
    getRequest_transaction().setVisibility(GONE);
    getFabmenu().setVisibility(View.GONE);
    return rootView;
  }

  public void update(boolean force) {
    if (getAc() == null) return;
    resetRequestCount();
    getWallets().clear();
    if (getSwipe_refresh_layout2() != null) getSwipe_refresh_layout2().setRefreshing(true);

    try {
      EtherscanAPI.getInstance().getNormalTransactions(getAddress(), new Callback() {
        @Override public void onFailure(Call call, IOException e) {
          if (isAdded()) {
            getAc().runOnUiThread(new Runnable() {
              @Override public void run() {
                onItemsLoadComplete();
                ((AddressDetailActivity) getAc()).snackError(getString(R.string.err_no_con));
              }
            });
          }
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
          String restring = response.body().string();
          if (restring != null && restring.length() > 2) RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, getAddress(), restring);
          final List<TransactionDisplay> w =
              new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", getAddress(), TransactionDisplay.NORMAL));
          if (isAdded()) {
            getAc().runOnUiThread(new Runnable() {
              @Override public void run() {
                onComplete(w);
              }
            });
          }
        }
      }, force);
      EtherscanAPI.getInstance().getInternalTransactions(getAddress(), new Callback() {
        @Override public void onFailure(Call call, IOException e) {
          if (isAdded()) {
            getAc().runOnUiThread(new Runnable() {
              @Override public void run() {
                onItemsLoadComplete();
                ((AddressDetailActivity) getAc()).snackError(getString(R.string.err_no_con));
              }
            });
          }
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
          String restring = response.body().string();
          if (restring != null && restring.length() > 2) RequestCache.getInstance().put(RequestCache.TYPE_TXS_INTERNAL, getAddress(), restring);
          final List<TransactionDisplay> w =
              new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", getAddress(), TransactionDisplay.CONTRACT));
          if (isAdded()) {
            getAc().runOnUiThread(new Runnable() {
              @Override public void run() {
                onComplete(w);
              }
            });
          }
        }
      }, force);
    } catch (IOException e) {
      if (getAc() != null) ((AddressDetailActivity) getAc()).snackError("Can't fetch account balances. No connection?");
      onItemsLoadComplete();
      e.printStackTrace();
    }
  }

  private void onComplete(List<TransactionDisplay> w) {
    addToWallets(w);
    addRequestCount();
    if (getRequestCount() >= 2) {
      onItemsLoadComplete();
      getNothing_found().setVisibility(getWallets().size() == 0 ? View.VISIBLE : View.GONE);
      getWalletAdapter().notifyDataSetChanged();
    }
  }

}