package com.ubiqsmart.ui.scanqr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.ubiqsmart.R;
import com.ubiqsmart.ui.base.BaseActivity;
import com.ubiqsmart.utils.qr.AddressEncoder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import java.io.*;
import java.util.*;

public class QRScanActivity extends BaseActivity implements ZXingScannerView.ResultHandler {

  public static final int REQUEST_CODE = 100;
  public static final int REQUEST_CAMERA_PERMISSION = 106;

  public static final byte SCAN_ONLY = 0;
  public static final byte ADD_TO_WALLETS = 1;
  public static final byte REQUEST_PAYMENT = 2;
  public static final byte PRIVATE_KEY = 3;

  private byte type;

  private ZXingScannerView scannerView;
  private FrameLayout barCode;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_qrscan);

    final Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    final TextView title = findViewById(R.id.toolbar_title);
    type = getIntent().getByteExtra("TYPE", SCAN_ONLY);
    title.setText(type == SCAN_ONLY ? "Scan Address" : "ADD WALLET");

    barCode = findViewById(R.id.barcode);

    if (hasPermission(this)) {
      initQRScan(barCode);
    } else {
      askForPermissionRead(this);
    }
  }

  @Override public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  public void initQRScan(FrameLayout frame) {
    scannerView = new ZXingScannerView(this);
    frame.addView(scannerView);
    scannerView.setResultHandler(this);

    final List<BarcodeFormat> supported = new ArrayList<>();
    supported.add(BarcodeFormat.QR_CODE);
    scannerView.setFormats(supported);
    scannerView.startCamera();
  }

  @Override public void onPause() {
    super.onPause();
    if (scannerView != null) {
      scannerView.stopCamera();
    }
  }

  public boolean hasPermission(Context c) {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || c.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
  }

  public static void askForPermissionRead(Activity c) {
    if (Build.VERSION.SDK_INT < 23) {
      return;
    }
    ActivityCompat.requestPermissions(c, new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA_PERMISSION);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CAMERA_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          initQRScan(barCode);
        } else {
          Toast.makeText(this, R.string.grant_camera_permission, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  @Override public void handleResult(Result result) {
    if (result == null) {
      return;
    }

    final String address = result.getText();
    try {
      AddressEncoder scanned = AddressEncoder.decode(address);
      Intent data = new Intent();
      data.putExtra("ADDRESS", scanned.getAddress().toLowerCase());

      if (address.length() > 42 && !address.startsWith("0x") && scanned.getAmount() == null) {
        type = PRIVATE_KEY;
      }

      if (scanned.getAmount() != null) {
        data.putExtra("AMOUNT", scanned.getAmount());
        type = REQUEST_PAYMENT;
      }

      data.putExtra("TYPE", type);
      setResult(RESULT_OK, data);
      finish();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}