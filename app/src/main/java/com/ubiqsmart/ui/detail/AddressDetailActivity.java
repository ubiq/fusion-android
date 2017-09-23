package com.ubiqsmart.ui.detail;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.ubiqsmart.R;
import com.ubiqsmart.ui.base.SecureAppCompatActivity;
import com.ubiqsmart.ui.transactions.TransactionsFragment;
import com.ubiqsmart.utils.AddressNameConverter;

public class AddressDetailActivity extends SecureAppCompatActivity {

  public static final byte OWN_WALLET = 0;
  public static final byte SCANNED_WALLET = 1;

  private SectionsPagerAdapter sectionsPagerAdapter;
  private ViewPager viewPager;
  private Fragment[] fragments;
  private String address;
  private byte type;
  private TextView title;
  private CoordinatorLayout coord;
  private AppBarLayout appbar;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    address = getIntent().getStringExtra("ADDRESS");
    type = getIntent().getByteExtra("TYPE", SCANNED_WALLET);

    final Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    title = findViewById(R.id.toolbar_title);
    final String walletname = AddressNameConverter.getInstance(this).get(address);
    title.setText(type == OWN_WALLET ? (walletname == null ? "Unnamed Wallet" : walletname) : "Address");

    coord = findViewById(R.id.main_content);
    appbar = findViewById(R.id.appbar);

    fragments = new Fragment[3];
    fragments[0] = new DetailShareFragment();
    fragments[1] = new DetailOverviewFragment();
    fragments[2] = new TransactionsFragment();

    final Bundle bundle = new Bundle();
    bundle.putString("ADDRESS", address);
    bundle.putDouble("BALANCE", getIntent().getDoubleExtra("BALANCE", 0));
    bundle.putByte("TYPE", type);

    fragments[0].setArguments(bundle);
    fragments[1].setArguments(bundle);
    fragments[2].setArguments(bundle);

    sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    viewPager = findViewById(R.id.container);
    viewPager.setAdapter(sectionsPagerAdapter);

    TabLayout tabLayout = null;//(TabLayout) findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setupWithViewPager(viewPager);

    tabLayout.getTabAt(0).setIcon(R.drawable.ic_action_share);
    tabLayout.getTabAt(1).setIcon(R.drawable.ic_wallet);
    tabLayout.getTabAt(2).setIcon(R.drawable.ic_transactions);
    viewPager.setCurrentItem(1);

    viewPager.setOffscreenPageLimit(3);
  }

  public void setTitle(String s) {
    if (title != null) {
      title.setText(s);
      Snackbar mySnackbar =
          Snackbar.make(coord, AddressDetailActivity.this.getResources().getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT);
      mySnackbar.show();
    }
  }

  @Override public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  public void snackError(String s) {
    if (coord == null) {
      return;
    }

    final Snackbar mySnackbar = Snackbar.make(coord, s, Snackbar.LENGTH_SHORT);
    mySnackbar.show();
  }

  public void broadCastDataSetChanged() {
    if (fragments != null && fragments[2] != null) {
      ((TransactionsFragment) fragments[2]).notifyDataSetChanged();
    }
  }

  public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int position) {
      return fragments[position];
    }

    @Override public int getCount() {
      return fragments.length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return "";
    }
  }

  public AppBarLayout getAppBar() {
    return appbar;
  }

}
