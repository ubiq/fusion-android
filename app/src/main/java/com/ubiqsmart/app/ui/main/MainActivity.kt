package com.ubiqsmart.app.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.R
import com.ubiqsmart.app.services.WalletGenService
import com.ubiqsmart.app.ui.base.SecureActivity
import com.ubiqsmart.app.ui.detail.AddressDetailActivity
import com.ubiqsmart.app.ui.main.adapter.MainActivityPagerAdapter
import com.ubiqsmart.app.ui.main.fragments.price.PriceFragment
import com.ubiqsmart.app.ui.main.fragments.transactions.TransactionsAllFragment
import com.ubiqsmart.app.ui.main.fragments.wallets.WalletsFragment
import com.ubiqsmart.app.ui.scanqr.QRScanActivity
import com.ubiqsmart.app.ui.send.SendActivity
import com.ubiqsmart.app.ui.settings.SettingsActivity
import com.ubiqsmart.app.ui.wallet.WalletGenActivity
import com.ubiqsmart.app.utils.*
import com.ubiqsmart.domain.models.WatchWallet
import com.ubiqsmart.extensions.obtainViewModel
import com.ubiqsmart.extensions.setupActionBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.math.BigDecimal

@Deprecated("Moving logic to MainActivity2 for easy refactoring")
class MainActivity : SecureActivity() {

  private val walletStorage: WalletStorage by instance()
  private val addressNameConverter: AddressNameConverter by instance()

  private lateinit var viewPagerAdapter: MainActivityPagerAdapter

  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)

    setupActionBar()
    setupViewPager()
    setupBottomNavigationView()
    onCreateViewModel()
  }

  override fun onResume() {
    super.onResume()
    broadCastDataSetChanged()
  }

  override fun onDestroy() {
    viewModel.onDestroyView()
    super.onDestroy()
  }

  private fun setupActionBar() {
    setupActionBar(R.id.toolbar_view) {
      title = ""
      setDisplayHomeAsUpEnabled(false)
      setDisplayHomeAsUpEnabled(false)
    }
  }

  private fun setupViewPager() {
    val fragments = arrayListOf(PriceFragment(), WalletsFragment(), TransactionsAllFragment())
    viewPagerAdapter = MainActivityPagerAdapter(supportFragmentManager, fragments)
    view_pager.adapter = viewPagerAdapter
    view_pager.offscreenPageLimit = 3
  }

  private fun setupBottomNavigationView() {
    bottom_navigation_view.setOnNavigationItemSelectedListener { item ->
      val itemId = item.itemId
      val currentItem = when (itemId) {
        R.id.action_price -> 0
        R.id.action_wallet -> 1
        else -> 2
      }
      view_pager.currentItem = currentItem
      true
    }
    bottom_navigation_view.selectedItemId = R.id.action_wallet
  }

  private fun onCreateViewModel() {
    viewModel = obtainViewModel(MainViewModel::class.java)
    viewModel.onViewCreated()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_activity, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

    R.id.action_settings -> {
      startActivityForResult(SettingsActivity.getStartIntent(this), SettingsActivity.REQUEST_CODE)
      true
    }

    else -> super.onOptionsItemSelected(item)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {

      ExternalStorageHandler.REQUEST_WRITE_STORAGE -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          (viewPagerAdapter.fragments[1] as WalletsFragment).export()
        } else {
          snackError(getString(R.string.main_grant_permission_export))
        }
      }

      ExternalStorageHandler.REQUEST_READ_STORAGE -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          try {
            walletStorage.importingWalletsDetector(this)
          } catch (e: Exception) {
            e.printStackTrace()
          }

        } else {
          snackError(getString(R.string.main_grant_permission_import))
        }
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      QRScanActivity.REQUEST_CODE -> handleOnQrScanResult(resultCode, data)
      WalletGenActivity.REQUEST_CODE -> handleWalletGenerationResult(resultCode, data)
      SendActivity.REQUEST_CODE -> handleSendResult(resultCode, data)
//      SettingsActivity.REQUEST_CODE -> handleSettingsUpdateResult()
    }
  }

//  private fun handleSettingsUpdateResult() {
//    val currency = preferences.getString("maincurrency", "USD")
//    if (currency != exchangeCalculator.mainCurreny.name) {
//      try {
//        exchangeCalculator.updateExchangeRates(currency, this)
//      } catch (e: IOException) {
//        e.printStackTrace()
//      }
//
//      Handler().postDelayed({
//        (fragments[0] as PriceFragment).update()
//        (fragments[1] as WalletsFragment).updateBalanceText()
//        (fragments[1] as WalletsFragment).notifyDataSetChanged()
//        (fragments[2] as TransactionsAllFragment).notifyDataSetChanged()
//      }, 950)
//    }
//  }

  private fun handleSendResult(resultCode: Int, data: Intent?) {
    when (resultCode) {
      Activity.RESULT_OK -> {
        val from = data!!.getStringExtra("FROM_ADDRESS")
        val to = data.getStringExtra("TO_ADDRESS")
        val rawAmount = data.getStringExtra("AMOUNT")
        val amount = BigDecimal("-" + rawAmount).multiply(BigDecimal("1000000000000000000")).toBigInteger()

        (viewPagerAdapter.fragments[2] as TransactionsAllFragment).addUnconfirmedTransaction(from, to, amount)
        view_pager.currentItem = 2
      }
    }
  }

  private fun handleWalletGenerationResult(resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
      val generatingService = WalletGenService.getStartIntent(this).apply {
        putExtra("PASSWORD", data?.getStringExtra("PASSWORD"))
        val hasPrivateKey = data?.hasExtra("PRIVATE_KEY") ?: false
        if (hasPrivateKey) {
          putExtra("PRIVATE_KEY", data?.getStringExtra("PRIVATE_KEY"))
        }
      }
      startService(generatingService)
    }
  }

  private fun handleOnQrScanResult(resultCode: Int, data: Intent?) {
    when (resultCode) {
      Activity.RESULT_OK -> {
        val type = data?.getByteExtra("TYPE", QRScanActivity.SCAN_ONLY)
        if (type == QRScanActivity.SCAN_ONLY) {
          if (data.getStringExtra("ADDRESS").length != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
            snackError(getString(R.string.invalid_wallet))
            return
          }
          val watch = Intent(this, AddressDetailActivity::class.java)
          watch.putExtra("ADDRESS", data.getStringExtra("ADDRESS"))
          startActivity(watch)
        } else if (type == QRScanActivity.ADD_TO_WALLETS) {
          if (data.getStringExtra("ADDRESS").length != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
            snackError(getString(R.string.invalid_wallet))
            return
          }
          val suc = walletStorage.add(WatchWallet(data.getStringExtra("ADDRESS")))
          Handler().postDelayed({
            try {
              (viewPagerAdapter.fragments[1] as WalletsFragment).update()
            } catch (e: IOException) {
              e.printStackTrace()
            }
            view_pager.currentItem = 1
            val mySnackbar = Snackbar.make(main_content,
                this@MainActivity.resources.getString(if (suc) R.string.main_ac_wallet_added_suc else R.string.main_ac_wallet_added_er),
                Snackbar.LENGTH_SHORT)
            if (suc) {
              addressNameConverter.put(data.getStringExtra("ADDRESS"), "Watch " + data.getStringExtra("ADDRESS").substring(0, 6))
            }

            mySnackbar.show()
          }, 100)
        } else if (type == QRScanActivity.REQUEST_PAYMENT) {
          if (walletStorage.fullOnly.size == 0) {
            DialogFactory.noFullWallet(this)
          } else {
            val watch = Intent(this, SendActivity::class.java).apply {
              putExtra("TO_ADDRESS", data.getStringExtra("ADDRESS"))
              putExtra("AMOUNT", data.getStringExtra("AMOUNT"))
            }
            startActivity(watch)
          }
        } else if (type == QRScanActivity.PRIVATE_KEY) {
          if (OwnWalletUtils.isValidPrivateKey(data.getStringExtra("ADDRESS"))) {
            val genI = Intent(this, WalletGenActivity::class.java).apply {
              putExtra("PRIVATE_KEY", data.getStringExtra("ADDRESS"))
            }
            startActivityForResult(genI, WalletGenActivity.REQUEST_CODE)
          } else {
            this.snackError(getString(com.ubiqsmart.R.string.invalid_private_key))
          }
        }
      }
      else -> Snackbar.make(main_content!!, this@MainActivity.resources.getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT).show()
    }
  }

  @JvmOverloads
  fun snackError(s: String, length: Int = Snackbar.LENGTH_SHORT) {
    if (main_content == null) {
      return
    }
    Snackbar.make(main_content!!, s, length).show()
  }

  fun broadCastDataSetChanged() {
    (viewPagerAdapter.fragments[1] as WalletsFragment).notifyDataSetChanged()
    (viewPagerAdapter.fragments[2] as TransactionsAllFragment).notifyDataSetChanged()
  }

  companion object {

    fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)

  }
}
