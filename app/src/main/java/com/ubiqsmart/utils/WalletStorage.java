package com.ubiqsmart.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.ubiqsmart.interfaces.StorableWallet;
import com.ubiqsmart.repository.data.FullWallet;
import com.ubiqsmart.repository.data.WatchWallet;
import com.ubiqsmart.ui.main.MainActivity;
import kotlin.Deprecated;
import org.json.JSONException;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

@Deprecated(message = "Replace this with a repository with Room")
public class WalletStorage {

  private static WalletStorage instance;

  private final Context context;
  private final SharedPreferences sharedPreferences;
  private final AddressNameConverter addressNameConverter;

  private List<StorableWallet> mapdb;
  private String walletToExport; // Used as temp if users wants to export but still needs to grant write permission

  public static WalletStorage getInstance(final Context context, final AddressNameConverter addressNameConverter) {
    if (instance == null) {
      final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      instance = new WalletStorage(context, sharedPreferences, addressNameConverter);
    }
    return instance;
  }

  private WalletStorage(final Context context, final SharedPreferences sharedPreferences, final AddressNameConverter addressNameConverter) {
    this.context = context.getApplicationContext();
    this.sharedPreferences = sharedPreferences;
    this.addressNameConverter = addressNameConverter;

    try {
      load();
    } catch (Exception e) {
      e.printStackTrace();
      mapdb = new ArrayList<>();
    }

    if (mapdb.size() == 0) { // Try to find local wallets
      checkForWallets();
    }
  }

  public synchronized boolean add(StorableWallet address) {
    for (int i = 0; i < mapdb.size(); i++)
      if (mapdb.get(i).getPubKey().equalsIgnoreCase(address.getPubKey())) {
        return false;
      }
    mapdb.add(address);
    save();
    return true;
  }

  public synchronized List<StorableWallet> get() {
    return mapdb;
  }

  public synchronized List<String> getFullOnly() {
    final List<String> erg = new ArrayList<>();
    if (mapdb.size() == 0) return erg;
    for (int i = 0; i < mapdb.size(); i++) {
      final StorableWallet cur = mapdb.get(i);
      if (cur instanceof FullWallet) {
        erg.add(cur.getPubKey());
      }
    }
    return erg;
  }

  public synchronized boolean isFullWallet(String addr) {
    if (mapdb.size() == 0) return false;
    for (int i = 0; i < mapdb.size(); i++) {
      StorableWallet cur = mapdb.get(i);
      if (cur instanceof FullWallet && cur.getPubKey().equalsIgnoreCase(addr)) return true;
    }
    return false;
  }

  public void removeWallet(String address, Context context) {
    int position = -1;
    for (int i = 0; i < mapdb.size(); i++) {
      if (mapdb.get(i).getPubKey().equalsIgnoreCase(address)) {
        position = i;
        break;
      }
    }
    if (position >= 0) {
      if (mapdb.get(position) instanceof FullWallet) // IF full wallet delete private key too
      {
        new File(context.getFilesDir(), address.substring(2, address.length())).delete();
      }
      mapdb.remove(position);
    }

    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove(address);
    editor.apply();

    save();
  }

  private void checkForWallets() {
    // Full wallets
    final File[] wallets = context.getFilesDir().listFiles();
    if (wallets == null) {
      return;
    }

    for (File wallet : wallets) {
      if (wallet.isFile()) {
        if (wallet.getName().length() == 40) {
          add(new FullWallet("0x" + wallet.getName(), wallet.getName()));
        }
      }
    }

    // Watch only
    final Map<String, ?> allEntries = sharedPreferences.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
      if (entry.getKey().length() == 42 && !mapdb.contains(entry.getKey())) {
        add(new WatchWallet(entry.getKey()));
      }
    }

    if (mapdb.size() > 0) {
      save();
    }
  }

  public void importingWalletsDetector(MainActivity c) {
    if (!ExternalStorageHandler.hasReadPermission(c)) {
      ExternalStorageHandler.askForPermissionRead(c);
      return;
    }
    final File[] wallets = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Lunary/").listFiles();
    if (wallets == null) {
      DialogFactory.noImportWalletsFound(c);
      return;
    }
    final List<File> foundImports = new ArrayList<>();
    for (File wallet : wallets) {
      if (wallet.isFile()) {
        if (wallet.getName().startsWith("UTC") && wallet.getName().length() >= 40) {
          foundImports.add(wallet); // Mist naming
        } else if (wallet.getName().length() >= 40) {
          int position = wallet.getName().indexOf(".json");
          if (position < 0) continue;
          final String addr = wallet.getName().substring(0, position);
          if (addr.length() == 40 && !mapdb.contains("0x" + wallet.getName())) {
            foundImports.add(wallet); // Exported with Lunary
          }
        }
      }
    }
    if (foundImports.size() == 0) {
      DialogFactory.noImportWalletsFound(c);
      return;
    }

    DialogFactory.importWallets(c, this, foundImports);
  }

  public void setWalletForExport(String wallet) {
    walletToExport = wallet;
  }

  public boolean exportWallet(Activity c) {
    return exportWallet(c, false);
  }

  public void importWallets(final List<File> toImport) throws Exception {
    for (int i = 0; i < toImport.size(); i++) {
      final String address = stripWalletName(toImport.get(i).getName());
      if (address.length() == 40) {
        copyFile(toImport.get(i), new File(context.getFilesDir(), address));
        toImport.get(i).delete();

        add(new FullWallet("0x" + address, address));
        addressNameConverter.put("0x" + address, "Wallet " + ("0x" + address).substring(0, 6));

        final Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri fileContentUri = Uri.fromFile(toImport.get(i)); // With 'permFile' being the File object
        mediaScannerIntent.setData(fileContentUri);

        context.sendBroadcast(mediaScannerIntent); // With 'this' being the context, e.g. the activity
      }
    }
  }

  public static String stripWalletName(String s) {
    if (s.lastIndexOf("--") > 0) {
      s = s.substring(s.lastIndexOf("--") + 2);
    }

    if (s.endsWith(".json")) {
      s = s.substring(0, s.indexOf(".json"));
    }

    return s;
  }

  private boolean exportWallet(final Activity activity, boolean already) {
    if (walletToExport == null) {
      return false;
    }

    if (walletToExport.startsWith("0x")) {
      walletToExport = walletToExport.substring(2);
    }

    if (ExternalStorageHandler.hasPermission(context)) {
      final File folder = new File(Environment.getExternalStorageDirectory(), "Lunary");
      if (!folder.exists()) {
        folder.mkdirs();
      }

      final File storeFile = new File(folder, walletToExport + ".json");
      try {
        copyFile(new File(context.getFilesDir(), walletToExport), storeFile);
      } catch (IOException e) {
        return false;
      }

      // fix, otherwise won't show up via USB
      final Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      final Uri fileContentUri = Uri.fromFile(storeFile); // With 'permFile' being the File object
      mediaScannerIntent.setData(fileContentUri);

      context.sendBroadcast(mediaScannerIntent); // With 'this' being the context, e.g. the activity
      return true;
    } else if (!already) {
      ExternalStorageHandler.askForPermission(activity);
      return exportWallet(activity, true);
    } else {
      return false;
    }
  }

  private void copyFile(File src, File dst) throws IOException {
    final FileChannel inChannel = new FileInputStream(src).getChannel();
    final FileChannel outChannel = new FileOutputStream(dst).getChannel();
    try {
      inChannel.transferTo(0, inChannel.size(), outChannel);
    } finally {
      if (inChannel != null) {
        inChannel.close();
      }
      outChannel.close();
    }
  }

  public Credentials getFullWallet(String password, String wallet) throws IOException, JSONException, CipherException {
    if (wallet.startsWith("0x")) {
      wallet = wallet.substring(2, wallet.length());
    }
    return WalletUtils.loadCredentials(password, new File(context.getFilesDir(), wallet));
  }

  private synchronized void save() {
    final FileOutputStream fout;
    try {
      fout = new FileOutputStream(new File(context.getFilesDir(), "wallets.dat"));
      final ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(mapdb);
      oos.close();
      fout.close();
    } catch (Exception ignored) {
    }
  }

  @SuppressWarnings("unchecked") private synchronized void load() throws IOException, ClassNotFoundException {
    final FileInputStream fout = new FileInputStream(new File(context.getFilesDir(), "wallets.dat"));
    final ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
    mapdb = (ArrayList<StorableWallet>) oos.readObject();
    oos.close();
    fout.close();
  }

}
