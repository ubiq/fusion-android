package com.ubiqsmart.app.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.SecureActivity
import com.ubiqsmart.app.ui.main.adapter.MainActivityPagerAdapter
import com.ubiqsmart.app.ui.main.fragments.price.PriceFragment2
import com.ubiqsmart.app.ui.main.fragments.transactions.TransactionsFragment
import com.ubiqsmart.app.ui.main.fragments.wallets.WalletsFragment2
import com.ubiqsmart.app.ui.settings.SettingsActivity
import com.ubiqsmart.extensions.obtainViewModel
import com.ubiqsmart.extensions.setupActionBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity2 : SecureActivity(), MainNavigator {

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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_activity, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.action_settings -> {
      openSettingsScreen()
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  private fun setupActionBar() {
    setupActionBar(R.id.toolbar_view) {
      title = ""
      setDisplayHomeAsUpEnabled(false)
      setDisplayHomeAsUpEnabled(false)
    }
  }

  private fun setupViewPager() {
    val fragments = arrayListOf(PriceFragment2(), WalletsFragment2(), TransactionsFragment())
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

  override fun openSettingsScreen() {
    startActivityForResult(SettingsActivity.getStartIntent(this), SettingsActivity.REQUEST_CODE)
    overridePendingTransition(R.anim.fade_in_slide_in_right, R.anim.fade_out)
  }

  companion object {

    fun getStartIntent(context: Context) = Intent(context, MainActivity2::class.java)

  }

}