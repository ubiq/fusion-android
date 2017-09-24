package com.ubiqsmart.ui.widgets.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

import java.util.*

class WeekXFormatter : IAxisValueFormatter {

  private val c = GregorianCalendar()

  override fun getFormattedValue(value: Float, axis: AxisBase): String {
    c.timeInMillis = value.toLong() * 1000
    return c.get(Calendar.DAY_OF_MONTH).toString() + ". "
  }
}
