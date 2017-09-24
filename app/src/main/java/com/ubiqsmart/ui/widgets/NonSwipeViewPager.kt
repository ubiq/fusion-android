package com.ubiqsmart.ui.widgets

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class NonSwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

  var isPagingEnabled: Boolean = false

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return this.isPagingEnabled && super.onTouchEvent(event)
  }

  override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
    return this.isPagingEnabled && super.onInterceptTouchEvent(event)
  }
}