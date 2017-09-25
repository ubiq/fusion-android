package com.ubiqsmart.ui.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.Menu
import android.view.MenuItem
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.R
import com.ubiqsmart.interfaces.NetworkUpdateListener
import com.ubiqsmart.repository.data.WatchWallet
import com.ubiqsmart.services.NotificationLauncher
import com.ubiqsmart.services.WalletGenService
import com.ubiqsmart.ui.base.BaseFragment
import com.ubiqsmart.ui.base.SecureAppCompatActivity
import com.ubiqsmart.ui.detail.AddressDetailActivity
import com.ubiqsmart.ui.onboarding.AppIntroActivity
import com.ubiqsmart.ui.scanqr.QRScanActivity
import com.ubiqsmart.ui.send.SendActivity
import com.ubiqsmart.ui.settings.SettingsActivity
import com.ubiqsmart.ui.wallet.WalletGenActivity
import com.ubiqsmart.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Response
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.IOException
import java.math.BigDecimal
import java.security.Security

class MainActivity : SecureAppCompatActivity(), NetworkUpdateListener {

  lateinit var fragments: List<BaseFragment>

  private val preferences: SharedPreferences by instance()
  private val exchangeCalculator: ExchangeCalculator by instance()
  private val walletStorage: WalletStorage by instance()
  private val notificationLauncher: NotificationLauncher by instance()
  private val addressNameConverter: AddressNameConverter by instance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)

    displayAppIntroIfNecessary()

    setupActionBar()
    setupFragments()
    setupViewPager()
    setupBottomNavigationView()

    fetchCurrentExchangeRate()

    Settings.initiate(this)
    notificationLauncher.start()
  }

  private fun fetchCurrentExchangeRate() {
    try {
      val currency = preferences.getString("maincurrency", "USD")
      exchangeCalculator.updateExchangeRates(currency, this)
    } catch (e: IOException) {
      e.printStackTrace()
    }
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

  private fun setupFragments() {
    fragments = arrayListOf(PriceFragment(), WalletsFragment(), TransactionsAllFragment())
  }

  private fun displayAppIntroIfNecessary() {
    if (preferences.getLong("APP_INSTALLED", 0) == 0L) {
      val intro = Intent(this, AppIntroActivity::class.java)
      startActivityForResult(intro, AppIntroActivity.REQUEST_CODE)
    }
  }

  private fun setupViewPager() {
    view_pager.adapter = SectionsPagerAdapter(supportFragmentManager)
    view_pager.offscreenPageLimit = 3
  }

  private fun setupActionBar() {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
      title = ""
      setDisplayHomeAsUpEnabled(false)
      setDisplayHomeAsUpEnabled(false)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_activity, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

    R.id.action_import_wallet -> try {
      walletStorage.importingWalletsDetector(this)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }

    R.id.action_settings -> {
      val settings = Intent(this, SettingsActivity::class.java)
      startActivityForResult(settings, SettingsActivity.REQUEST_CODE)
      true
    }

    else -> super.onOptionsItemSelected(item)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {

      ExternalStorageHandler.REQUEST_WRITE_STORAGE -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          (fragments[1] as WalletsFragment).export()
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

  override fun onResume() {
    super.onResume()
    broadCastDataSetChanged()

    // Update wallets if activity resumed and a new wallet was found (finished generation or added as watch only address)
    if (walletStorage.get().size != (fragments[1] as WalletsFragment).displayedWalletCount) {
      try {
        (fragments[1] as WalletsFragment).update()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      QRScanActivity.REQUEST_CODE -> handleOnQrScanResult(resultCode, data)
      WalletGenActivity.REQUEST_CODE -> handleWalletGenerationResult(resultCode, data)
      SendActivity.REQUEST_CODE -> handleSendResult(resultCode, data)
      AppIntroActivity.REQUEST_CODE -> handleAppIntroResult(resultCode)
      SettingsActivity.REQUEST_CODE -> handleSettingsUpdateResult()
    }
  }

  private fun handleSettingsUpdateResult() {
    val currency = preferences.getString("maincurrency", "USD")
    if (currency != exchangeCalculator.mainCurreny.name) {
      try {
        exchangeCalculator.updateExchangeRates(currency, this)
      } catch (e: IOException) {
        e.printStackTrace()
      }

      Handler().postDelayed({
        (fragments[0] as PriceFragment).update()
        (fragments[1] as WalletsFragment).updateBalanceText()
        (fragments[1] as WalletsFragment).notifyDataSetChanged()
        (fragments[2] as TransactionsAllFragment).notifyDataSetChanged()
      }, 950)
    }
  }

  private fun handleAppIntroResult(resultCode: Int) {
    if (resultCode != Activity.RESULT_OK) {
      finish()
    } else {
      preferences.edit().apply {
        putLong("APP_INSTALLED", System.currentTimeMillis())
        commit()
      }
    }
  }

  private fun handleSendResult(resultCode: Int, data: Intent?) {
    when (resultCode) {
      Activity.RESULT_OK -> {
        val from = data!!.getStringExtra("FROM_ADDRESS")
        val to = data.getStringExtra("TO_ADDRESS")
        val rawAmount = data.getStringExtra("AMOUNT")
        val amount = BigDecimal("-" + rawAmount).multiply(BigDecimal("1000000000000000000")).toBigInteger()

        (fragments[2] as TransactionsAllFragment).addUnconfirmedTransaction(from, to, amount)
        //if (tabLayout != null) {
        //  tabLayout.getTabAt(2).select();
        //}
      }
    }
  }

  private fun handleWalletGenerationResult(resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
      val generatingService = Intent(this, WalletGenService::class.java).apply {
        putExtra("PASSWORD", data?.getStringExtra("PASSWORD"))
        val hasPrivateKey = data?.hasExtra("PRIVATE_KEY") ?: false
        if (hasPrivateKey) putExtra("PRIVATE_KEY", data?.getStringExtra("PRIVATE_KEY"))
      }
      startService(generatingService)
    }
  }

  private fun handleOnQrScanResult(resultCode: Int, data: Intent?) {
    if (resultCode == Activity.RESULT_OK) {
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
            (fragments[1] as WalletsFragment).update()
          } catch (e: IOException) {
            e.printStackTrace()
          }
          //if (tabLayout != null) {
          //  tabLayout.getTabAt(1).select();
          //}
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
          importPrivateKey(data.getStringExtra("ADDRESS"))
        } else {
          this.snackError(getString(com.ubiqsmart.R.string.invalid_private_key))
        }
      }
    } else {
      val mySnackbar = Snackbar.make(main_content!!, this@MainActivity.resources.getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT)
      mySnackbar.show()
    }
  }

  fun importPrivateKey(privatekey: String) {
    val genI = Intent(this, WalletGenActivity::class.java).apply {
      putExtra("PRIVATE_KEY", privatekey)
    }
    startActivityForResult(genI, WalletGenActivity.REQUEST_CODE)
  }

  @JvmOverloads
  fun snackError(s: String, length: Int = Snackbar.LENGTH_SHORT) {
    if (main_content == null) {
      return
    }
    val mySnackbar = Snackbar.make(main_content!!, s, length)
    mySnackbar.show()
  }

  fun broadCastDataSetChanged() {
    (fragments[1] as WalletsFragment).notifyDataSetChanged()
    (fragments[2] as TransactionsAllFragment).notifyDataSetChanged()
  }

  override fun onUpdate(s: Response) {
    runOnUiThread {
      broadCastDataSetChanged()
      (fragments[0] as PriceFragment).update()
    }
  }

  private inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
      return fragments[position]
    }

    override fun getCount(): Int {
      return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence {
      return ""
    }
  }

  companion object {

    // Spongy Castle Provider
    init {
      Security.insertProviderAt(BouncyCastleProvider(), 1)
    }
  }
}
