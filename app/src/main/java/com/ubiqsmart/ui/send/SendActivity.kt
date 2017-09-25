package com.ubiqsmart.ui.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.ui.base.SecureAppCompatActivity
import com.ubiqsmart.ui.scanqr.QRScanActivity
import com.ubiqsmart.ui.widgets.NonSwipeViewPager

class SendActivity : SecureAppCompatActivity() {

  private lateinit var fragments: List<Fragment>
  private lateinit var adapter: FragmentAdapter

  private var viewPager: NonSwipeViewPager? = null

  private var title: TextView? = null
  private var coord: CoordinatorLayout? = null

  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_choose_recipient)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayShowTitleEnabled(false)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    title = findViewById(R.id.toolbar_title)
    coord = findViewById(R.id.main_content)

    fragments = arrayListOf(
        ChooseRecipientFragment(),
        SendFragment()
    )

    val bundle = Bundle()

    if (intent.hasExtra("TO_ADDRESS")) {
      bundle.putString("TO_ADDRESS", intent.getStringExtra("TO_ADDRESS"))
    }
    if (intent.hasExtra("AMOUNT")) {
      bundle.putString("AMOUNT", intent.getStringExtra("AMOUNT"))
    }
    if (intent.hasExtra("FROM_ADDRESS")) {
      bundle.putString("FROM_ADDRESS", intent.getStringExtra("FROM_ADDRESS"))
    }

    fragments[1].arguments = bundle

    adapter = FragmentAdapter(supportFragmentManager)

    viewPager = findViewById(R.id.container)
    viewPager?.isPagingEnabled = false
    viewPager?.adapter = adapter

    if (intent.hasExtra("TO_ADDRESS")) {
      viewPager!!.currentItem = 1
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == QRScanActivity.REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        (fragments[0] as ChooseRecipientFragment).setRecipientAddress(data!!.getStringExtra("ADDRESS"))
      } else {
        val mySnackbar = Snackbar.make(coord!!, this.resources.getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT)
        mySnackbar.show()
      }
    }
  }

  fun nextStage(toAddress: String) {
    viewPager!!.currentItem = 1

    (fragments[1] as SendFragment).setToAddress(toAddress)
  }

  internal inner class FragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
      return fragments[position]
    }

    override fun getCount(): Int {
      return 2
    }
  }

  fun setTitle(s: String) {
    if (title != null) {
      title!!.text = s
      val mySnackbar = Snackbar.make(coord!!, this@SendActivity.resources.getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT)
      mySnackbar.show()
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  @JvmOverloads
  fun snackError(s: String, length: Int = Snackbar.LENGTH_SHORT) {
    if (coord == null) {
      return
    }
    val mySnackbar = Snackbar.make(coord!!, s, length)
    mySnackbar.show()
  }

  companion object {

    val REQUEST_CODE = 200
  }

}
