package com.ubiqsmart.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ubiqsmart.repository.data.Token;
import com.ubiqsmart.R;

import java.util.*;

public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.MyViewHolder> {

  private Context context;
  private List<Token> boxlist;
  private int lastPosition = -1;
  private View.OnClickListener listener;
  private View.OnCreateContextMenuListener contextMenuListener;
  private int position;

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView nativebalance;
    public TextView etherbalance;
    public TextView image;
    public LinearLayout container;

    public MyViewHolder(View view) {
      super(view);

      nativebalance = view.findViewById(R.id.nativebalance);
      name = view.findViewById(R.id.tokenname);
      image = view.findViewById(R.id.addressimage);
      etherbalance = view.findViewById(R.id.etherbalance);
      container = view.findViewById(R.id.container);
    }

    public void clearAnimation() {
      container.clearAnimation();
    }
  }

  public TokenAdapter(List<Token> boxlist, Context context, View.OnClickListener listener, View.OnCreateContextMenuListener l) {
    this.boxlist = boxlist;
    this.context = context;
    this.listener = listener;
    this.contextMenuListener = l;
  }

  @Override public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_w_token, parent, false);

    itemView.setOnClickListener(listener);
    itemView.setOnCreateContextMenuListener(contextMenuListener);
    return new MyViewHolder(itemView);
  }

  @Override public void onBindViewHolder(MyViewHolder holder, final int position) {
    Token box = boxlist.get(position);

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        setPosition(position);
        return false;
      }
    });

    holder.name.setText(box.getName());
    double tbalance = box.getBalanceDouble();
    holder.nativebalance.setText(ExchangeCalculator.getInstance().displayEthNicely(tbalance) + " " + box.getShorty());
    holder.etherbalance.setText(ExchangeCalculator.getInstance()
        .displayEthNicely(ExchangeCalculator.getInstance()
            .convertRate(ExchangeCalculator.getInstance().convertTokenToEther(tbalance, box.getUsdprice()),
                ExchangeCalculator.getInstance().getCurrent().getRate())) + " " + ExchangeCalculator.getInstance().getCurrent().getShorty());
    if (box.getContractAddress() != null && box.getContractAddress().length() > 3) {
      holder.image.setText("");
      String iconName = box.getName();
      if (iconName.indexOf(" ") > 0) iconName = iconName.substring(0, iconName.indexOf(" "));
      holder.image.setBackground(new BitmapDrawable(context.getResources(), TokenIconCache.getInstance(context).get(iconName)));
    } else {
      holder.image.setText("Ξ");
      holder.image.setBackgroundResource(0);
      holder.etherbalance.setText(ExchangeCalculator.getInstance()
          .displayEthNicely(ExchangeCalculator.getInstance().convertRate(tbalance, ExchangeCalculator.getInstance().getCurrent().getRate()))
          + " "
          + ExchangeCalculator.getInstance().getCurrent().getShorty());
    }

    setAnimation(holder.container, position);
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  @Override public void onViewRecycled(TokenAdapter.MyViewHolder holder) {
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
