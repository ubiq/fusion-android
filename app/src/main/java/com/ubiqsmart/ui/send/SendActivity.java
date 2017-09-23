package com.ubiqsmart.ui.send;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.ubiqsmart.R;
import com.ubiqsmart.ui.base.SecureAppCompatActivity;
import com.ubiqsmart.ui.scanqr.QRScanActivity;
import com.ubiqsmart.ui.widgets.NonSwipeViewPager;

public class SendActivity extends SecureAppCompatActivity {

  public static final int REQUEST_CODE = 200;

  private NonSwipeViewPager viewPager;
  private Fragment[] fragments;

  private TextView title;
  private CoordinatorLayout coord;
  FragmentAdapter adapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_choose_recipient);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    title = findViewById(R.id.toolbar_title);

    coord = findViewById(R.id.main_content);

    fragments = new Fragment[2];
    fragments[0] = new ChooseRecipientFragment();
    fragments[1] = new SendFragment();
    Bundle bundle = new Bundle();

    if (getIntent().hasExtra("TO_ADDRESS")) bundle.putString("TO_ADDRESS", getIntent().getStringExtra("TO_ADDRESS"));
    if (getIntent().hasExtra("AMOUNT")) bundle.putString("AMOUNT", getIntent().getStringExtra("AMOUNT"));
    if (getIntent().hasExtra("FROM_ADDRESS")) bundle.putString("FROM_ADDRESS", getIntent().getStringExtra("FROM_ADDRESS"));

    fragments[1].setArguments(bundle);

    adapter = new FragmentAdapter(getSupportFragmentManager());

    viewPager = findViewById(R.id.container);
    viewPager.setPagingEnabled(false);
    viewPager.setAdapter(adapter);

    if (getIntent().hasExtra("TO_ADDRESS")) viewPager.setCurrentItem(1);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == QRScanActivity.REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        if (fragments == null || fragments[0] == null) return;
        ((ChooseRecipientFragment) fragments[0]).setRecipientAddress(data.getStringExtra("ADDRESS"));
      } else {
        Snackbar mySnackbar = Snackbar.make(coord, this.getResources().getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT);
        mySnackbar.show();
      }
    }
  }

  public void nextStage(String toAddress) {
    viewPager.setCurrentItem(1);

    if (fragments == null || fragments[1] == null) return;
    ((SendFragment) fragments[1]).setToAddress(toAddress, this);
  }

  class FragmentAdapter extends FragmentPagerAdapter {

    private FragmentManager mFragmentManager;

    public FragmentAdapter(FragmentManager fm) {
      super(fm);
      mFragmentManager = fm;
    }

    @Override public Fragment getItem(int position) {
      return fragments[position];
    }

    @Override public int getCount() {
      return 2;
    }
  }

  public void setTitle(String s) {
    if (title != null) {
      title.setText(s);
      Snackbar mySnackbar = Snackbar.make(coord, SendActivity.this.getResources().getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT);
      mySnackbar.show();
    }
  }

  @Override public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  public void snackError(String s, int length) {
    if (coord == null) return;
    Snackbar mySnackbar = Snackbar.make(coord, s, length);
    mySnackbar.show();
  }

  public void snackError(String s) {
    snackError(s, Snackbar.LENGTH_SHORT);
  }

}
