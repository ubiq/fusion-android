package com.ubiqsmart.app.ui.widgets.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

import java.util.*

class HourXFormatter : IAxisValueFormatter {

  private val c = GregorianCalendar()

  override fun getFormattedValue(value: Float, axis: AxisBase): String {
    c.timeInMillis = value.toLong() * 1000
    return c.get(Calendar.HOUR_OF_DAY).toString() + ":00 "
  }
}
