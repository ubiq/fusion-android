package com.ubiqsmart.app.ui.transactions.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.domain.models.TransactionDisplay
import com.ubiqsmart.app.utils.AddressNameConverter
import com.ubiqsmart.app.utils.Blockies
import com.ubiqsmart.app.utils.ExchangeCalculator
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val context: Context, private val transactions: List<TransactionDisplay>, private val clickListener: View.OnClickListener,
                         private val contextMenuListener: View.OnCreateContextMenuListener) : RecyclerView.Adapter<TransactionAdapter.MyViewHolder>() {

  private val dateFormat = SimpleDateFormat("dd. MMMM yyyy, HH:mm", Locale.getDefault())
  private var lastPosition = -1
  var position: Int = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_w_transaction, parent, false)
    itemView.setOnCreateContextMenuListener(contextMenuListener)
    itemView.setOnClickListener(clickListener)
    return MyViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val box = transactions[position]

    val exchangeCalculator = ExchangeCalculator.getInstance()
    val walletName = AddressNameConverter.getInstance(context).get(box.fromAddress)
    val toName = AddressNameConverter.getInstance(context).get(box.toAddress)

    holder.apply {
      walletBalance.text = String.format("%s %s",
          exchangeCalculator.displayBalanceNicely(exchangeCalculator.convertRate(Math.abs(box.amount), exchangeCalculator.current.rate)),
          exchangeCalculator.currencyShort)
      this.walletName.text = walletName ?: box.walletName
      otherAddress.text = if (toName == null) box.toAddress else toName + " (" + box.toAddress.substring(0, 10) + ")"
      plusMinus.text = if (box.amount > 0) "+" else "-"
      plusMinus.setTextColor(ContextCompat.getColor(context, (if (box.amount > 0) R.color.ether_received else R.color.ether_spent)))
      walletBalance.setTextColor(ContextCompat.getColor(context, if (box.amount > 0) R.color.ether_received else R.color.ether_spent))
      container.alpha = 1f
      type.visibility = if (box.type == TransactionDisplay.NORMAL) View.INVISIBLE else View.VISIBLE
      error.visibility = if (box.isError) View.VISIBLE else View.GONE
      myAddressIcon.setImageBitmap(Blockies.createIcon(box.fromAddress.toLowerCase()))
      otherAddressIcon.setImageBitmap(Blockies.createIcon(box.toAddress.toLowerCase()))

      when {
        box.confirmationStatus == 0 -> {
          month.setText(R.string.unconfirmed)
          month.setTextColor(ContextCompat.getColor(context, R.color.unconfirmedNew))
          container.alpha = 0.75f
        }
        box.confirmationStatus > 12 -> {
          month.text = dateFormat.format(Date(box.date))
          month.setTextColor(ContextCompat.getColor(context, R.color.normal_black))
        }
        else -> {
          month.text = String.format(Locale.getDefault(), context.getString(R.string.minimum_confirmations), box.confirmationStatus)
          month.setTextColor(ContextCompat.getColor(context, R.color.unconfirmed))
        }
      }
    }

    holder.itemView.setOnLongClickListener {
      this.position = position
      false
    }

    setAnimation(holder.container, position)
  }

  override fun onViewRecycled(holder: MyViewHolder?) {
    holder?.itemView?.setOnLongClickListener(null)
    super.onViewRecycled(holder)
  }

  private fun setAnimation(viewToAnimate: View, position: Int) {
    if (position > lastPosition) {
      val animation = AnimationUtils.loadAnimation(context, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_bottom)
      viewToAnimate.startAnimation(animation)
      lastPosition = position
    }
  }

  override fun onViewDetachedFromWindow(holder: MyViewHolder?) {
    holder?.clearAnimation()
  }

  override fun getItemCount(): Int {
    return transactions.size
  }

  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val month: TextView = view.findViewById(R.id.month_view)
    val walletBalance: TextView = view.findViewById(R.id.wallet_balance)
    val walletName: TextView = view.findViewById(R.id.wallet_name)
    val otherAddress: TextView = view.findViewById(R.id.other_address)
    val plusMinus: TextView = view.findViewById(R.id.plus_minus)
    val myAddressIcon: ImageView = view.findViewById(R.id.my_address_icon)
    val otherAddressIcon: ImageView = view.findViewById(R.id.other_address_icon)
    val type: ImageView = view.findViewById(R.id.type)
    val error: ImageView = view.findViewById(R.id.error)
    val container: LinearLayout = view.findViewById(R.id.container)

    fun clearAnimation() {
      container.clearAnimation()
    }
  }
}
