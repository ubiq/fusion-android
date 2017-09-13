package com.ubiqsmart.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.kobakei.ratethisapp.RateThisApp;
import okhttp3.Response;
import rehanced.com.ubiqsmart.R;
import rehanced.com.ubiqsmart.activities.App;
import com.ubiqsmart.data.WatchWallet;
import com.ubiqsmart.fragments.FragmentPrice;
import com.ubiqsmart.fragments.FragmentTransactionsAll;
import com.ubiqsmart.fragments.FragmentWallets;
import com.ubiqsmart.interfaces.NetworkUpdateListener;
import com.ubiqsmart.services.NotificationLauncher;
import com.ubiqsmart.services.WalletGenService;
import com.ubiqsmart.utils.AddressNameConverter;
import com.ubiqsmart.utils.Dialogs;
import com.ubiqsmart.utils.ExchangeCalculator;
import com.ubiqsmart.utils.ExternalStorageHandler;
import com.ubiqsmart.utils.OwnWalletUtils;
import com.ubiqsmart.utils.Settings;
import com.ubiqsmart.utils.WalletStorage;

import java.io.*;
import java.math.BigDecimal;
import java.security.Security;
import java.util.*;

public class MainActivity extends SecureAppCompatActivity implements NetworkUpdateListener {

  @BindView(R.id.main_content) CoordinatorLayout coord;
  @BindView(R.id.appbar) AppBarLayout appbar;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.container) ViewPager viewPager;
  @Nullable @BindView(R.id.navigation_view) BottomNavigationView bottomNavigationView;

  public List<Fragment> fragments;

  private SharedPreferences preferences;

  // Spongy Castle Provider
  static {
    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    // ButterKnife
    ButterKnife.bind(this);

    // App Intro
    this.preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    if (preferences.getLong("APP_INSTALLED", 0) == 0) {
      final Intent intro = new Intent(this, AppIntroActivity.class);
      startActivityForResult(intro, AppIntroActivity.REQUEST_CODE);
    }

    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
      @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        final int currentItem = itemId == R.id.action_price ? 0 : itemId == R.id.action_wallet ? 1 : 2;
        viewPager.setCurrentItem(currentItem);
        return true;
      }
    });

    // ------------------------------------------------------------------------

    fragments = new ArrayList<>(3);
    fragments.add(new FragmentPrice());
    fragments.add(new FragmentWallets());
    fragments.add(new FragmentTransactionsAll());

    final FragmentManager fm = getSupportFragmentManager();
    final SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(fm);
    viewPager.setAdapter(sectionsPagerAdapter);
    viewPager.setOffscreenPageLimit(3);

    try {
      ExchangeCalculator.getInstance().updateExchangeRates(preferences.getString("maincurrency", "USD"), this);
    } catch (IOException e) {
      e.printStackTrace();
    }

    Settings.initiate(this);
    NotificationLauncher.getInstance().start(this);

    if (getIntent().hasExtra("STARTAT")) { //  Click on Notification, show Transactions
      final int startat = getIntent().getIntExtra("STARTAT", 2);
      viewPager.setCurrentItem(startat);
      broadCastDataSetChanged();
    } else if (Settings.startWithWalletTab) { // if enabled in setting select wallet tab instead of price tab
      viewPager.setCurrentItem(1);
    }

    // Rate Dialog (only show on google play builds)
    if (((App) this.getApplication()).isGooglePlayBuild()) {
      try {
        RateThisApp.onCreate(this);
        RateThisApp.Config config = new RateThisApp.Config(3, 5);
        RateThisApp.init(config);
        RateThisApp.showRateDialogIfNeeded(this, R.style.AlertDialogTheme);
      } catch (Exception e) {
      }
    }
    //Security.removeProvider("BC");
    Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_activity, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    final int itemId = item.getItemId();
    switch (itemId) {
      case R.id.action_import_wallet:
        try {
          WalletStorage.getInstance(this).importingWalletsDetector(this);
        } catch (Exception e) {
          e.printStackTrace();
        }
        break;
      case R.id.action_settings:
        final Intent settings = new Intent(this, SettingsActivity.class);
        startActivityForResult(settings, SettingsActivity.REQUEST_CODE);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  public SharedPreferences getPreferences() {
    return preferences;
  }

  @Override public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case ExternalStorageHandler.REQUEST_WRITE_STORAGE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          if (fragments != null && fragments.get(1) != null) ((FragmentWallets) fragments.get(1)).export();
        } else {
          snackError(getString(R.string.main_grant_permission_export));
        }
        return;
      }
      case ExternalStorageHandler.REQUEST_READ_STORAGE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          try {
            WalletStorage.getInstance(this).importingWalletsDetector(this);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          snackError(getString(R.string.main_grant_permission_import));
        }
        return;
      }
    }
  }

  @Override public void onResume() {
    super.onResume();
    broadCastDataSetChanged();

    // Update wallets if activity resumed and a new wallet was found (finished generation or added as watch only address)
    if (fragments != null
        && fragments.get(1) != null
        && WalletStorage.getInstance(this).get().size() != ((FragmentWallets) fragments.get(1)).getDisplayedWalletCount()) {
      try {
        ((FragmentWallets) fragments.get(1)).update();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == QRScanActivity.REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        byte type = data.getByteExtra("TYPE", QRScanActivity.SCAN_ONLY);
        if (type == QRScanActivity.SCAN_ONLY) {
          if (data.getStringExtra("ADDRESS").length() != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
            snackError("Invalid Ethereum address!");
            return;
          }
          Intent watch = new Intent(this, AddressDetailActivity.class);
          watch.putExtra("ADDRESS", data.getStringExtra("ADDRESS"));
          startActivity(watch);
        } else if (type == QRScanActivity.ADD_TO_WALLETS) {
          if (data.getStringExtra("ADDRESS").length() != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
            snackError("Invalid Ethereum address!");
            return;
          }
          final boolean suc = WalletStorage.getInstance(this).add(new WatchWallet(data.getStringExtra("ADDRESS")), this);
          new Handler().postDelayed(new Runnable() {
            @Override public void run() {
              if (fragments != null && fragments.get(1) != null) {
                try {
                  ((FragmentWallets) fragments.get(1)).update();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
              //if (tabLayout != null) {
              //  tabLayout.getTabAt(1).select();
              //}
              Snackbar mySnackbar = Snackbar.make(coord,
                  MainActivity.this.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er),
                  Snackbar.LENGTH_SHORT);
              if (suc) {
                AddressNameConverter.getInstance(MainActivity.this)
                    .put(data.getStringExtra("ADDRESS"), "Watch " + data.getStringExtra("ADDRESS").substring(0, 6), MainActivity.this);
              }

              mySnackbar.show();

            }
          }, 100);
        } else if (type == QRScanActivity.REQUEST_PAYMENT) {
          if (WalletStorage.getInstance(this).getFullOnly().size() == 0) {
            Dialogs.noFullWallet(this);
          } else {
            Intent watch = new Intent(this, SendActivity.class);
            watch.putExtra("TO_ADDRESS", data.getStringExtra("ADDRESS"));
            watch.putExtra("AMOUNT", data.getStringExtra("AMOUNT"));
            startActivity(watch);
          }
        } else if (type == QRScanActivity.PRIVATE_KEY) {
          if (OwnWalletUtils.isValidPrivateKey(data.getStringExtra("ADDRESS"))) {
            importPrivateKey(data.getStringExtra("ADDRESS"));
          } else {
            this.snackError("Invalid private key!");
          }
        }
      } else {
        Snackbar mySnackbar =
            Snackbar.make(coord, MainActivity.this.getResources().getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT);
        mySnackbar.show();
      }
    } else if (requestCode == WalletGenActivity.REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        Intent generatingService = new Intent(this, WalletGenService.class);
        generatingService.putExtra("PASSWORD", data.getStringExtra("PASSWORD"));
        if (data.hasExtra("PRIVATE_KEY")) generatingService.putExtra("PRIVATE_KEY", data.getStringExtra("PRIVATE_KEY"));
        startService(generatingService);
      }
    } else if (requestCode == SendActivity.REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        if (fragments == null || fragments.get(2) == null) return;
        ((FragmentTransactionsAll) fragments.get(2)).addUnconfirmedTransaction(data.getStringExtra("FROM_ADDRESS"), data.getStringExtra
                ("TO_ADDRESS"),
            new BigDecimal("-" + data.getStringExtra("AMOUNT")).multiply(new BigDecimal("1000000000000000000")).toBigInteger());
        //if (tabLayout != null) {
        //  tabLayout.getTabAt(2).select();
        //}
      }
    } else if (requestCode == AppIntroActivity.REQUEST_CODE) {
      if (resultCode != RESULT_OK) {
        finish();
      } else {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("APP_INSTALLED", System.currentTimeMillis());
        editor.commit();
      }
    } else if (requestCode == SettingsActivity.REQUEST_CODE) {
      if (!preferences.getString("maincurrency", "USD").equals(ExchangeCalculator.getInstance().getMainCurreny().getName())) {
        try {
          ExchangeCalculator.getInstance().updateExchangeRates(preferences.getString("maincurrency", "USD"), this);
        } catch (IOException e) {
          e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
          @Override public void run() {
            if (fragments != null) {
              if (fragments.get(0) != null) ((FragmentPrice) fragments.get(0)).update();
              if (fragments.get(1) != null) {
                ((FragmentWallets) fragments.get(1)).updateBalanceText();
                ((FragmentWallets) fragments.get(1)).notifyDataSetChanged();
              }
              if (fragments.get(2) != null) ((FragmentTransactionsAll) fragments.get(2)).notifyDataSetChanged();
            }
          }
        }, 950);
      }

    }
  }

  public void importPrivateKey(String privatekey) {
    Intent genI = new Intent(this, WalletGenActivity.class);
    genI.putExtra("PRIVATE_KEY", privatekey);
    startActivityForResult(genI, WalletGenActivity.REQUEST_CODE);
  }

  public void snackError(String s, int length) {
    if (coord == null) {
      return;
    }
    Snackbar mySnackbar = Snackbar.make(coord, s, length);
    mySnackbar.show();
  }

  public void snackError(String s) {
    snackError(s, Snackbar.LENGTH_SHORT);
  }

  public void broadCastDataSetChanged() {
    if (fragments != null && fragments.get(1) != null && fragments.get(2) != null) {
      ((FragmentWallets) fragments.get(1)).notifyDataSetChanged();
      ((FragmentTransactionsAll) fragments.get(2)).notifyDataSetChanged();
    }
  }

  @Override public void onUpdate(Response s) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        broadCastDataSetChanged();
        if (fragments != null && fragments.get(0) != null) {
          ((FragmentPrice) fragments.get(0)).update();
        }
      }
    });
  }

  public AppBarLayout getAppBar() {
    return appbar;
  }

  private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int position) {
      return fragments.get(position);
    }

    @Override public int getCount() {
      return fragments.size();
    }

    @Override public CharSequence getPageTitle(int position) {
      return "";
    }
  }
}
