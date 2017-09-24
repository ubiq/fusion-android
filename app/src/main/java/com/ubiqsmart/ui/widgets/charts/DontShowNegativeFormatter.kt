package com.ubiqsmart.ui.widgets.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class DontShowNegativeFormatter(private val dispalyInUsd: Boolean) : IAxisValueFormatter {

  override fun getFormattedValue(value: Float, axis: AxisBase): String {
    return if (dispalyInUsd) {
      if (value >= 0) value.toInt().toString() + "" else ""
    } else {
      if (value >= 0) (Math.floor((value * 1000).toDouble()) / 1000).toString() + "" else ""
    }
  }
}
