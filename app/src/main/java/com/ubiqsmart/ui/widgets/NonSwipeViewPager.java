package com.ubiqsmart.ui.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipeViewPager extends ViewPager {

  private boolean enabled;

  public NonSwipeViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    return this.enabled && super.onTouchEvent(event);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    return this.enabled && super.onInterceptTouchEvent(event);
  }

  public void setPagingEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}