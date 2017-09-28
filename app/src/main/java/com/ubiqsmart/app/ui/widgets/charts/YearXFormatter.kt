package com.ubiqsmart.app.ui.widgets.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

import java.util.*

class YearXFormatter : IAxisValueFormatter {

  internal var c = GregorianCalendar()
  private var months = arrayOf("Jan", "Feb", "Mar", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")

  override fun getFormattedValue(value: Float, axis: AxisBase): String {
    c.timeInMillis = value.toLong() * 1000
    return months[c.get(Calendar.MONTH) + 1]
  }
}
