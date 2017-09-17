package com.ubiqsmart.ui.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.Menu
import android.view.MenuItem
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.R
import com.ubiqsmart.repository.data.WatchWallet
import com.ubiqsmart.interfaces.NetworkUpdateListener
import com.ubiqsmart.services.NotificationLauncher
import com.ubiqsmart.services.WalletGenService
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
import java.io.IOException
import java.math.BigDecimal
import java.security.Security

class MainActivity : SecureAppCompatActivity(), NetworkUpdateListener, LazyKodeinAware {

    override val kodein = LazyKodein(appKodein)

    var appBar: AppBarLayout? = null
        private set

    lateinit var fragments: List<Fragment>

    private val preferences: SharedPreferences by withContext(this).instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        appBar = findViewById(R.id.appbar)

        if (preferences.getLong("APP_INSTALLED", 0) == 0L) {
            val intro = Intent(this, AppIntroActivity::class.java)
            startActivityForResult(intro, AppIntroActivity.REQUEST_CODE)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        fragments = arrayListOf(PriceFragment(), WalletsFragment(), TransactionsAllFragment())

        view_pager.adapter = SectionsPagerAdapter(supportFragmentManager)
        view_pager.offscreenPageLimit = 3

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

        try {
            val currency = preferences.getString("maincurrency", "USD")
            ExchangeCalculator.getInstance().updateExchangeRates(currency, this)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Settings.initiate(this)
        NotificationLauncher.getInstance().start(this)

        //Security.removeProvider("BC");
        Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import_wallet -> try {
                WalletStorage.getInstance(this).importingWalletsDetector(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            R.id.action_settings -> {
                val settings = Intent(this, SettingsActivity::class.java)
                startActivityForResult(settings, SettingsActivity.REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ExternalStorageHandler.REQUEST_WRITE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    (fragments[1] as WalletsFragment).export()
                } else {
                    snackError(getString(R.string.main_grant_permission_export))
                }
                return
            }
            ExternalStorageHandler.REQUEST_READ_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        WalletStorage.getInstance(this).importingWalletsDetector(this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    snackError(getString(R.string.main_grant_permission_import))
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        broadCastDataSetChanged()

        // Update wallets if activity resumed and a new wallet was found (finished generation or added as watch only address)
        if (WalletStorage.getInstance(this).get().size != (fragments[1] as WalletsFragment).displayedWalletCount) {
            try {
                (fragments[1] as WalletsFragment).update()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QRScanActivity.REQUEST_CODE) {
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
                    val suc = WalletStorage.getInstance(this).add(WatchWallet(data.getStringExtra("ADDRESS")), this)
                    Handler().postDelayed({
                        if (fragments != null && fragments!![1] != null) {
                            try {
                                (fragments!![1] as WalletsFragment).update()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        //if (tabLayout != null) {
                        //  tabLayout.getTabAt(1).select();
                        //}
                        val mySnackbar = Snackbar.make(main_content,
                                this@MainActivity.resources.getString(if (suc) R.string.main_ac_wallet_added_suc else R.string.main_ac_wallet_added_er),
                                Snackbar.LENGTH_SHORT)
                        if (suc) {
                            AddressNameConverter.getInstance(this@MainActivity)
                                    .put(data.getStringExtra("ADDRESS"), "Watch " + data.getStringExtra("ADDRESS").substring(0, 6), this@MainActivity)
                        }

                        mySnackbar.show()
                    }, 100)
                } else if (type == QRScanActivity.REQUEST_PAYMENT) {
                    if (WalletStorage.getInstance(this).fullOnly.size == 0) {
                        Dialogs.noFullWallet(this)
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
        } else if (requestCode == WalletGenActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val generatingService = Intent(this, WalletGenService::class.java).apply {
                    putExtra("PASSWORD", data?.getStringExtra("PASSWORD"))
                    val hasPrivateKey = data?.hasExtra("PRIVATE_KEY") ?: false
                    if (hasPrivateKey) putExtra("PRIVATE_KEY", data?.getStringExtra("PRIVATE_KEY"))
                }
                startService(generatingService)
            }
        } else if (requestCode == SendActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (fragments == null || fragments!![2] == null) return
                (fragments!![2] as TransactionsAllFragment).addUnconfirmedTransaction(data?.getStringExtra("FROM_ADDRESS"), data?.getStringExtra
                ("TO_ADDRESS"),
                        BigDecimal("-" + data?.getStringExtra("AMOUNT")).multiply(BigDecimal("1000000000000000000")).toBigInteger())
                //if (tabLayout != null) {
                //  tabLayout.getTabAt(2).select();
                //}
            }
        } else if (requestCode == AppIntroActivity.REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            } else {
                preferences.edit().apply {
                    putLong("APP_INSTALLED", System.currentTimeMillis())
                    commit()
                }
            }
        } else if (requestCode == SettingsActivity.REQUEST_CODE) {
            val currency = preferences.getString("maincurrency", "USD")
            if (currency != ExchangeCalculator.getInstance().mainCurreny.name) {
                try {
                    ExchangeCalculator.getInstance().updateExchangeRates(currency, this)
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
    }

    fun importPrivateKey(privatekey: String) {
        val genI = Intent(this, WalletGenActivity::class.java)
        genI.putExtra("PRIVATE_KEY", privatekey)
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
            Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
        }
    }
}
