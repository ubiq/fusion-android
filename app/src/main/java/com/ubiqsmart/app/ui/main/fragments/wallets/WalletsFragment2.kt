package com.ubiqsmart.app.ui.main.fragments.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseFragment
import com.ubiqsmart.app.ui.main.adapter.WalletAdapter
import com.ubiqsmart.app.ui.wallet.WalletGenActivity
import com.ubiqsmart.extensions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_wallets.*

class WalletsFragment2 : BaseFragment(), WalletsNavigator {

  private lateinit var viewModel: WalletsViewModel
  private lateinit var walletAdapter: WalletAdapter

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_wallets, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    onSetupViewListeners()
    onCreateViewModel()
  }

  private fun onSetupViewListeners() {
    import_wallet_fab.setOnClickListener { }
    generate_wallet_fab.setOnClickListener { openWalletGeneratorScreen() }
    watch_address_fab.setOnClickListener { }
    scan_address_fab.setOnClickListener { }
  }

  private fun onCreateViewModel() {
    viewModel = activity.obtainViewModel(WalletsViewModel::class.java)
    viewModel.onViewCreated()
  }

  override fun openWalletGeneratorScreen() {
    activity.startActivityForResult(WalletGenActivity.getStartIntent(activity), WalletGenActivity.REQUEST_CODE)
  }
}