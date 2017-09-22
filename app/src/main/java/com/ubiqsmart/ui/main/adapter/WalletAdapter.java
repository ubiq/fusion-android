package com.ubiqsmart.ui.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ubiqsmart.repository.data.TransactionDisplay;
import com.ubiqsmart.utils.AddressNameConverter;
import com.ubiqsmart.utils.Blockies;
import com.ubiqsmart.utils.ExchangeCalculator;
import me.grantland.widget.AutofitTextView;
import com.ubiqsmart.R;
import com.ubiqsmart.repository.data.Wallet;

import java.util.*;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.MyViewHolder> {

  private Context context;
  private List<Wallet> boxlist;
  private int lastPosition = -1;
  private View.OnClickListener listener;
  private View.OnCreateContextMenuListener contextMenuListener;
  private int position;

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView walletname, walletbalance;
    public ImageView addressimage, type;
    AutofitTextView walletaddress;
    public LinearLayout container;

    public MyViewHolder(View view) {
      super(view);

      walletaddress = view.findViewById(R.id.walletaddress);
      walletname = view.findViewById(R.id.wallet_name);
      walletbalance = view.findViewById(R.id.wallet_balance);
      addressimage = view.findViewById(R.id.addressimage);
      type = view.findViewById(R.id.type);
      container = view.findViewById(R.id.container);
    }

    public void clearAnimation() {
      container.clearAnimation();
    }
  }

  public WalletAdapter(List<Wallet> boxlist, Context context, View.OnClickListener listener, View.OnCreateContextMenuListener l) {
    this.boxlist = boxlist;
    this.context = context;
    this.listener = listener;
    this.contextMenuListener = l;
  }

  @Override public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_w_address, parent, false);

    itemView.setOnClickListener(listener);
    itemView.setOnCreateContextMenuListener(contextMenuListener);
    return new MyViewHolder(itemView);
  }

  @Override public void onBindViewHolder(MyViewHolder holder, final int position) {
    Wallet box = boxlist.get(position);

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        setPosition(position);
        return false;
      }
    });
    holder.walletaddress.setText(box.getPublicKey());
    String walletname = AddressNameConverter.getInstance(context).get(box.getPublicKey());
    holder.walletname.setText(walletname == null ? "New Wallet" : walletname);
    if (box.getType() != Wallet.CONTACT) {
      holder.walletbalance.setText(ExchangeCalculator.getInstance()
          .displayBalanceNicely(
              ExchangeCalculator.getInstance().convertRate(box.getBalance(), ExchangeCalculator.getInstance().getCurrent().getRate()))
          + " "
          + ExchangeCalculator.getInstance().getCurrencyShort());
    }
    holder.addressimage.setImageBitmap(Blockies.createIcon(box.getPublicKey()));

    holder.type.setVisibility(box.getType() == TransactionDisplay.NORMAL || box.getType() == Wallet.CONTACT ? View.INVISIBLE : View.VISIBLE);

    setAnimation(holder.container, position);
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  @Override public void onViewRecycled(WalletAdapter.MyViewHolder holder) {
    holder.itemView.setOnLongClickListener(null);
    super.onViewRecycled(holder);
  }

  private void setAnimation(View viewToAnimate, int position) {
    if (position > lastPosition) {
      Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_bottom);
      viewToAnimate.startAnimation(animation);
      lastPosition = position;
    }
  }

  @Override public void onViewDetachedFromWindow(MyViewHolder holder) {
    holder.clearAnimation();
  }

  @Override public int getItemCount() {
    return boxlist.size();
  }
}
