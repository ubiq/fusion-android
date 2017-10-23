package com.ubiqsmart.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ubiqsmart.R;
import com.ubiqsmart.app.ui.detail.AddressDetailActivity;
import com.ubiqsmart.domain.models.Token;
import com.ubiqsmart.domain.models.TransactionDisplay;
import me.grantland.widget.AutofitTextView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DialogFactory {

  public static void showTokenDetails(final Activity c, final Token tok) {
    final MaterialDialog dialog = new MaterialDialog.Builder(c).customView(R.layout.dialog_token_detail, true).show();

    View view = dialog.getCustomView();
    ImageView contractIcon = view.findViewById(R.id.my_address_icon);
    TextView tokenname = view.findViewById(R.id.wallet_name);
    AutofitTextView contractAddr = view.findViewById(R.id.walletaddr);

    TextView supply = view.findViewById(R.id.supply);
    TextView priceUSD = view.findViewById(R.id.price);
    TextView priceETH = view.findViewById(R.id.price2);
    TextView capUSD = view.findViewById(R.id.cap);
    TextView capETH = view.findViewById(R.id.cap2);
    TextView holders = view.findViewById(R.id.holders);
    TextView digits = view.findViewById(R.id.digits);
    TextView created = view.findViewById(R.id.timestamp);

    LinearLayout from = view.findViewById(R.id.from);

    from.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Intent i = new Intent(c, AddressDetailActivity.class);
        i.putExtra("ADDRESS", tok.getContractAddress());
        c.startActivity(i);
      }
    });

    ExchangeCalculator ex = ExchangeCalculator.getInstance();
    contractIcon.setImageBitmap(Blockies.createIcon(tok.getContractAddress().toLowerCase()));
    tokenname.setText(tok.getName());
    contractAddr.setText(tok.getContractAddress().toLowerCase());
    supply.setText(String.format("%s %s", ex.displayUsdNicely(tok.getTotalSupplyLong()), tok.getShorty()));
    priceUSD.setText(String.format("%s $", tok.getUsdprice()));

    priceETH.setText(String.format("%s %s", ex.displayEthNicelyExact(ex.convertTokenToEther(1, tok.getUsdprice())), ex.getEtherCurrency().getShorty()));
    capETH.setText(
        String.format("%s %s", ex.displayUsdNicely(ex.convertTokenToEther(tok.getTotalSupplyLong(), tok.getUsdprice())), ex.getEtherCurrency().getShorty()));
    capUSD.setText(String.format("%s $", ex.displayUsdNicely(tok.getUsdprice() * tok.getTotalSupplyLong())));
    holders.setText(String.format("%s", ex.displayUsdNicely(tok.getHolderCount())));
    digits.setText(String.format(Locale.getDefault(), "%d", tok.getDigits()));
    SimpleDateFormat dateformat = new SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault());
    created.setText(String.format("%s", dateformat.format(tok.getCreatedAt() * 1000)));
  }

  public static void showTXDetails(final Activity c, final TransactionDisplay tx) {
    final MaterialDialog dialog = new MaterialDialog.Builder(c).customView(R.layout.dialog_tx_detail, true).show();

    final View view = dialog.getCustomView();
    final ImageView myicon = view.findViewById(R.id.my_address_icon);
    final ImageView othericon = view.findViewById(R.id.other_address_icon);
    final TextView myAddressname = view.findViewById(R.id.wallet_name);
    final TextView otherAddressname = view.findViewById(R.id.other_address);
    final AutofitTextView myAddressaddr = view.findViewById(R.id.walletaddr);
    final AutofitTextView otherAddressaddr = view.findViewById(R.id.other_addressaddr);
    final TextView amount = view.findViewById(R.id.amount);

    final TextView month = view.findViewById(R.id.month_view);
    final TextView gasUsed = view.findViewById(R.id.gasused);
    final TextView blocknr = view.findViewById(R.id.blocknr);
    final TextView gasPrice = view.findViewById(R.id.gasPrice);
    final TextView nonce = view.findViewById(R.id.nonce);
    final TextView txcost = view.findViewById(R.id.txcost);
    final TextView txcost2 = view.findViewById(R.id.txcost2);
    final Button openInBrowser = view.findViewById(R.id.openinbrowser);
    final LinearLayout from = view.findViewById(R.id.from);
    final LinearLayout to = view.findViewById(R.id.to);
    final TextView amountfiat = view.findViewById(R.id.amountfiat);
    final TextView errormsg = view.findViewById(R.id.errormsg);

    from.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        final Intent i = new Intent(c, AddressDetailActivity.class);
        i.putExtra("ADDRESS", tx.getFromAddress());
        c.startActivity(i);
      }
    });

    to.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        final Intent i = new Intent(c, AddressDetailActivity.class);
        i.putExtra("ADDRESS", tx.getToAddress());
        c.startActivity(i);
      }
    });

    openInBrowser.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        final String url = "https://etherscan.io/tx/" + tx.getTxHash();
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        c.startActivity(i);
      }
    });

    myicon.setImageBitmap(Blockies.createIcon(tx.getFromAddress().toLowerCase()));
    othericon.setImageBitmap(Blockies.createIcon(tx.getToAddress().toLowerCase()));

    String myName = AddressNameConverter.Companion.getInstance(c).get(tx.getFromAddress().toLowerCase());
    if (myName == null) {
      myName = shortName(tx.getFromAddress().toLowerCase());
    }
    String otherName = AddressNameConverter.Companion.getInstance(c).get(tx.getToAddress().toLowerCase());
    if (otherName == null) {
      otherName = shortName(tx.getToAddress().toLowerCase());
    }
    myAddressname.setText(myName);
    otherAddressname.setText(otherName);

    errormsg.setVisibility(tx.isError() ? View.VISIBLE : View.GONE);
    myAddressaddr.setText(tx.getFromAddress());
    otherAddressaddr.setText(tx.getToAddress());
    SimpleDateFormat dateformat = new SimpleDateFormat("dd. MMMM yyyy, HH:mm:ss", Locale.getDefault());
    month.setText(String.format("%s", dateformat.format(tx.getDate())));
    blocknr.setText(String.format(Locale.getDefault(), "%d", tx.getBlock()));
    gasUsed.setText(String.format(Locale.getDefault(), "%d", tx.getGasUsed()));
    gasPrice.setText(String.format(Locale.getDefault(), "%d Gwei", tx.getGasprice() / 1000000000));
    nonce.setText(String.format("%s", tx.getNounce()));
    txcost.setText(String.format("%s Ξ",
        ExchangeCalculator.getInstance().displayEthNicelyExact(ExchangeCalculator.getInstance().weiToEther(tx.getGasUsed() * tx.getGasprice()))));
    txcost2.setText(
        String.format("%s %s", ExchangeCalculator.getInstance().convertToUsd(ExchangeCalculator.getInstance().weiToEther(tx.getGasUsed() * tx.getGasprice())),
            ExchangeCalculator.getInstance().getMainCurreny().getShorty()));
    amount.setText(String.format("%s%s Ξ", tx.getAmount() > 0 ? "+ " : "- ", Math.abs(tx.getAmount())));
    amount.setTextColor(c.getResources().getColor(tx.getAmount() > 0 ? R.color.ether_received : R.color.ether_spent));
    amountfiat.setText(String.format("%s %s", ExchangeCalculator.getInstance().displayUsdNicely(ExchangeCalculator.getInstance().convertToUsd(tx.getAmount())),
        ExchangeCalculator.getInstance().getMainCurreny().getShorty()));
  }

  private static String shortName(String addr) {
    return "0x" + addr.substring(2, 8);
  }

  public static void addWatchOnly(final MainActivity c, final WalletStorage walletStorage) {
    //// Otherwise buttons on 7.0+ are nearly invisible
    //final AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.AlertDialogTheme) : new AlertDialog.Builder(c);
    //builder.setTitle(R.string.dialog_watch_only_title);
    //
    //final EditText input = new EditText(c);
    //input.setSingleLine();
    //FrameLayout container = new FrameLayout(c);
    //FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //params.leftMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
    //params.topMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
    //params.bottomMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
    //params.rightMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
    //input.setLayoutParams(params);
    //
    //input.setInputType(InputType.TYPE_CLASS_TEXT);
    //container.addView(input);
    //builder.setView(container);
    //input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
    //  public void onFocusChange(View v, boolean hasFocus) {
    //    if (hasFocus) {
    //      InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //      inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    //    }
    //  }
    //});
    //builder.setNegativeButton(R.string.add, new DialogInterface.OnClickListener() {
    //  @Override public void onClick(DialogInterface dialog, int which) {
    //
    //    InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //    inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
    //    if (input.getText().toString().length() == 42 && input.getText().toString().startsWith("0x")) {
    //      final boolean suc = walletStorage.add(new WatchWallet(input.getText().toString()));
    //      new Handler().postDelayed(new Runnable() {
    //        @Override public void run() {
    //          if (c.getFragments() != null && c.getFragments().get(1) != null) {
    //            try {
    //              ((WalletsFragment) c.getFragments().get(1)).update();
    //            } catch (IOException e) {
    //              e.printStackTrace();
    //            }
    //          }
    //          c.snackError(c.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er));
    //          if (suc) AddressNameConverter.Companion.getInstance(c).put(input.getText().toString(), "Watch " + input.getText().toString().substring(0, 6));
    //        }
    //      }, 100);
    //    } else {
    //      c.snackError("Invalid Ethereum address!");
    //    }
    //    dialog.dismiss();
    //  }
    //});
    //builder.setPositiveButton(R.string.show, new DialogInterface.OnClickListener() {
    //  @Override public void onClick(DialogInterface dialog, int which) {
    //    InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //    inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
    //    if (input.getText().toString().length() == 42 && input.getText().toString().startsWith("0x")) {
    //      Intent detail = new Intent(c, AddressDetailActivity.class);
    //      detail.putExtra("ADDRESS", input.getText().toString().toLowerCase());
    //      c.startActivity(detail);
    //    } else {
    //      c.snackError("Invalid Ethereum address!");
    //    }
    //    dialog.cancel();
    //  }
    //});
    //builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
    //  @Override public void onClick(DialogInterface dialog, int which) {
    //    InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //    inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
    //    dialog.cancel();
    //  }
    //});
    //
    //builder.show();
  }

  public static void importWallets(final MainActivity c, final WalletStorage walletStorage, final List<File> files) {
    //final StringBuilder addresses = new StringBuilder();
    //
    //for (int i = 0; i < files.size() && i < 3; i++) {
    //  addresses.append(WalletStorage.stripWalletName(files.get(i).getName())).append("\n");
    //}
    //
    //AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.AlertDialogTheme) : new AlertDialog.Builder(c);
    //builder.setTitle(R.string.dialog_importing_wallets_title);
    //builder.setMessage(String.format(c.getString(R.string.dialog_importing_wallets_text), files.size(), files.size() > 1 ? "s" : "", addresses.toString()));
    //builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
    //  @Override public void onClick(DialogInterface dialog, int which) {
    //    try {
    //      walletStorage.importWallets(files);
    //      c.snackError("WalletEntry" + (files.size() > 1 ? "s" : "") + " successfully imported!");
    //      if (c.getFragments() != null && c.getFragments().get(1) != null) {
    //        ((WalletsFragment) c.getFragments().get(1)).update();
    //      }
    //    } catch (Exception e) {
    //      c.snackError("Error while importing wallets");
    //      e.printStackTrace();
    //    }
    //    dialog.cancel();
    //  }
    //});
    //builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
    //  @Override public void onClick(DialogInterface dialog, int which) {
    //    dialog.cancel();
    //  }
    //});
    //
    //builder.show();
  }

  public static void cantExportNonWallet(Context c) {
    final AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.Ubiq_Dialog_Alert) : new AlertDialog.Builder(c);
    builder.setTitle(R.string.dialog_ex_nofull_title);
    builder.setMessage(R.string.dialog_ex_nofull_text);
    builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }

  public static void exportWallet(Context c, DialogInterface.OnClickListener yes) {
    final AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.Ubiq_Dialog_Alert) : new AlertDialog.Builder(c);
    builder.setTitle(R.string.dialog_exporting_title);
    builder.setMessage(R.string.dialog_exporting_text);
    builder.setPositiveButton(R.string.button_ok, yes);
    builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    builder.show();
  }

  public static void noFullWallet(Context c) {
    final AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.Ubiq_Dialog_Alert) : new AlertDialog.Builder(c);
    builder.setTitle(R.string.dialog_nofullwallet);
    builder.setMessage(R.string.dialog_nofullwallet_text);
    builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }

  public static void noWallet(Context c) {
    final AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.Ubiq_Dialog_Alert) : new AlertDialog.Builder(c);
    builder.setTitle(R.string.dialog_no_wallets);
    builder.setMessage(R.string.dialog_no_wallets_text);
    builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }

  public static void noImportWalletsFound(Context c) {
    final AlertDialog.Builder builder = Build.VERSION.SDK_INT >= 24 ? new AlertDialog.Builder(c, R.style.Ubiq_Dialog_Alert) : new AlertDialog.Builder(c);
    builder.setTitle(R.string.dialog_no_wallets_found);
    builder.setMessage(R.string.dialog_no_wallets_found_text);
    builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }
}
