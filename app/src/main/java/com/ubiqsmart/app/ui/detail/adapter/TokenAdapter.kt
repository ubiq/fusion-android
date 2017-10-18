package com.ubiqsmart.app.ui.detail.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.domain.models.Token
import com.ubiqsmart.app.utils.ExchangeCalculator
import com.ubiqsmart.app.utils.TokenIconCache

class TokenAdapter(
    private val context: Context,
    private val tokens: List<Token>,
    private val listener: View.OnClickListener,
    private val contextMenuListener: View.OnCreateContextMenuListener
) : RecyclerView.Adapter<TokenAdapter.MyViewHolder>() {

  private var lastPosition = -1
  var position: Int = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.token_item_adapter, parent, false).apply {
      setOnClickListener(listener)
      setOnCreateContextMenuListener(contextMenuListener)
    }
    return MyViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
    val box = tokens[position]
    val txBalance = box.balanceDouble

    val exchangeCalculator = ExchangeCalculator.getInstance()

    holder.apply {
      name.text = box.name
      nativebalance.text = "${exchangeCalculator.displayEthNicely(txBalance)} ${box.shorty}"
      etherbalance.text = exchangeCalculator.displayEthNicely(exchangeCalculator.convertRate(exchangeCalculator.convertTokenToEther(txBalance, box.usdprice), exchangeCalculator.current.rate)) + " " + exchangeCalculator.current.shorty
      if (box.contractAddress != null && box.contractAddress.length > 3) {
        image.text = ""
        var iconName = box.name
        if (iconName.indexOf(" ") > 0) {
          iconName = iconName.substring(0, iconName.indexOf(" "))
        }
        image.background = BitmapDrawable(context.resources, TokenIconCache.getInstance(context).get(iconName))
      } else {
        image.text = "Ξ"
        image.setBackgroundResource(0)
        etherbalance.text = "${exchangeCalculator.displayEthNicely(exchangeCalculator.convertRate(txBalance, exchangeCalculator.current.rate))} ${exchangeCalculator.current.shorty}"
      }
    }

    holder.itemView.setOnLongClickListener {
      this.position = position
      false
    }

    setAnimation(holder.container, position)
  }

  override fun onViewRecycled(holder: TokenAdapter.MyViewHolder?) {
    holder!!.itemView.setOnLongClickListener(null)
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
    holder!!.clearAnimation()
  }

  override fun getItemCount(): Int {
    return tokens.size
  }

  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var name: TextView
    var nativebalance: TextView
    var etherbalance: TextView
    var image: TextView
    var container: LinearLayout

    init {
      nativebalance = view.findViewById(R.id.nativebalance)
      name = view.findViewById(R.id.tokenname)
      image = view.findViewById(R.id.address_image_view)
      etherbalance = view.findViewById(R.id.etherbalance)
      container = view.findViewById(R.id.container)
    }

    fun clearAnimation() {
      container.clearAnimation()
    }
  }
}
