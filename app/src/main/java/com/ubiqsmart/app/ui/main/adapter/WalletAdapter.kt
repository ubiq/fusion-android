package com.ubiqsmart.app.ui.main.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.app.utils.AddressNameConverter
import com.ubiqsmart.app.utils.Blockies
import com.ubiqsmart.app.utils.ExchangeCalculator
import com.ubiqsmart.domain.models.TransactionDisplay
import com.ubiqsmart.domain.models.WalletEntry
import me.grantland.widget.AutofitTextView

class WalletAdapter(
    private val context: Context,
    private val wallets: List<WalletEntry>,
    private val listener: View.OnClickListener,
    private val contextMenuListener: View.OnCreateContextMenuListener
) : RecyclerView.Adapter<com.ubiqsmart.app.ui.main.adapter.WalletAdapter.WalletViewHolder>() {

  private var lastPosition = -1
  var position = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.wallet_item_adapter, parent, false)
    itemView.setOnClickListener(listener)
    itemView.setOnCreateContextMenuListener(contextMenuListener)
    return WalletViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
    val box = wallets[position]

    val exchangeCalculator = ExchangeCalculator.getInstance()
    val walletName = AddressNameConverter.getInstance(context).get(box.publicKey)

    holder.apply {
      walletaddress.text = box.publicKey
      walletname.text = walletName ?: context.getString(R.string.new_wallet)
      if (box.type != WalletEntry.CONTACT) {
        holder.walletbalance.text = String.format("%s %s",
            exchangeCalculator.displayBalanceNicely(exchangeCalculator.convertRate(box.balance, exchangeCalculator.current.rate)),
            exchangeCalculator.currencyShort)
      }
      addressimage.setImageBitmap(Blockies.createIcon(box.publicKey))
      type.visibility =
          if (box.type == TransactionDisplay.NORMAL || box.type == WalletEntry.CONTACT) {
            View.INVISIBLE
          } else {
            View.VISIBLE
          }
    }

    holder.itemView.setOnLongClickListener {
      this.position = position
      false
    }

    setAnimation(holder.container, position)
  }

  override fun onViewRecycled(holder: WalletViewHolder?) {
    holder!!.itemView.setOnLongClickListener(null)
    super.onViewRecycled(holder)
  }

  override fun onViewDetachedFromWindow(holder: WalletViewHolder?) {
    holder!!.clearAnimation()
  }

  override fun getItemCount() = wallets.size

  private fun setAnimation(viewToAnimate: View, position: Int) {
    if (position > lastPosition) {
      val animation = AnimationUtils.loadAnimation(context, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_bottom)
      viewToAnimate.startAnimation(animation)
      lastPosition = position
    }
  }

  class WalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var walletname: TextView = view.findViewById(R.id.wallet_name)
    var walletbalance: TextView = view.findViewById(R.id.wallet_balance)
    var addressimage: ImageView = view.findViewById(R.id.address_image_view)
    var type: ImageView = view.findViewById(R.id.type)
    var walletaddress: AutofitTextView = view.findViewById(R.id.walletaddress)
    var container: LinearLayout = view.findViewById(R.id.container)

    fun clearAnimation() {
      container.clearAnimation()
    }
  }
}
