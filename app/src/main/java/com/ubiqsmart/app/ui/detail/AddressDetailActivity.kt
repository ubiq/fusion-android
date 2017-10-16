package com.ubiqsmart.app.ui.detail

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.SecureActivity
import com.ubiqsmart.app.ui.transactions.TransactionsFragment
import com.ubiqsmart.app.utils.AddressNameConverter

class AddressDetailActivity : SecureActivity() {

  private lateinit var fragments: List<Fragment>

  private var sectionsPagerAdapter: SectionsPagerAdapter? = null
  private var viewPager: ViewPager? = null
  private var address: String? = null
  private var type: Byte = 0
  private var title: TextView? = null
  private var coord: CoordinatorLayout? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_address_detail)

    address = intent.getStringExtra("ADDRESS")
    type = intent.getByteExtra("TYPE", SCANNED_WALLET)

    val toolbar = findViewById<Toolbar>(R.id.toolbar_view)
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayShowTitleEnabled(false)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    title = findViewById(R.id.toolbar_title)
    val walletName = AddressNameConverter.getInstance(this).get(address!!)
    title!!.text = if (type == OWN_WALLET) walletName ?: getString(R.string.unnamed_wallet) else getString(R.string.address)

    coord = findViewById(R.id.main_content)

    fragments = arrayListOf(DetailShareFragment(), DetailOverviewFragment(), TransactionsFragment())

    val bundle = Bundle().apply {
      putString("ADDRESS", address)
      putDouble("BALANCE", intent.getDoubleExtra("BALANCE", 0.0))
      putByte("TYPE", type)
    }

    fragments[0].arguments = bundle
    fragments[1].arguments = bundle
    fragments[2].arguments = bundle

    sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

    viewPager = findViewById(R.id.container)
    viewPager?.adapter = sectionsPagerAdapter

    val tabLayout: TabLayout? = null//(TabLayout) findViewById(R.id.tabs);
    tabLayout!!.setupWithViewPager(viewPager)
    tabLayout.setupWithViewPager(viewPager)

    tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_action_share)
    tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_wallet)
    tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_transactions)

    viewPager?.currentItem = 1
    viewPager?.offscreenPageLimit = 3
  }

  fun setTitle(s: String) {
    if (title != null) {
      title!!.text = s
      val mySnackbar = Snackbar.make(coord!!, this@AddressDetailActivity.resources.getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT)
      mySnackbar.show()
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  fun snackError(s: String) {
    if (coord == null) {
      return
    }

    val mySnackbar = Snackbar.make(coord!!, s, Snackbar.LENGTH_SHORT)
    mySnackbar.show()
  }

  inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

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

    val OWN_WALLET: Byte = 0
    val SCANNED_WALLET: Byte = 1
  }

}
