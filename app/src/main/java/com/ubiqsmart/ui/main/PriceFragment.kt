package com.ubiqsmart.ui.main

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.ubiqsmart.R
import com.ubiqsmart.repository.api.EtherscanAPI
import com.ubiqsmart.ui.base.BaseFragment
import com.ubiqsmart.ui.widgets.charts.DontShowNegativeFormatter
import com.ubiqsmart.ui.widgets.charts.HourXFormatter
import com.ubiqsmart.ui.widgets.charts.WeekXFormatter
import com.ubiqsmart.ui.widgets.charts.YearXFormatter
import com.ubiqsmart.utils.ExchangeCalculator
import kotlinx.android.synthetic.main.fragment_price.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.*

class PriceFragment : BaseFragment() {

  private val preferences: SharedPreferences by withContext(this).instance()
  private val exchangeCalculator: ExchangeCalculator by instance()
  private val etherscanApi: EtherscanAPI by instance()

  private var ac: MainActivity? = null

  private var displayType = 1
  private var displayInUsd = true // True = USD, False = BTC

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater!!.inflate(R.layout.fragment_price, container, false)

    ac = activity as MainActivity

    TITLE_TEXTS = arrayOf(getString(R.string.last_24_hours), getString(R.string.last_7_days), getString(R.string.last_30_days), getString(R.string.last_year))

    price_switch.setOnClickListener {
      displayInUsd = !displayInUsd

      update()
      general()

      preferences.edit().putBoolean("price_displayInUsd", displayInUsd).apply()
    }

    displayInUsd = preferences.getBoolean("price_displayInUsd", true)
    displayType = preferences.getInt("displaytype_chart", 1)

    left_arrow.setOnClickListener { previous() }
    right_arrow.setOnClickListener { next() }

    swipe_refresh_layout2.setColorSchemeColors(ContextCompat.getColor(context, R.color.primary))
    swipe_refresh_layout2.setOnRefreshListener {
      updateExchangeRates()
      general()
    }

    general()
    update()

    price_chart.visibility = View.INVISIBLE
    swipe_refresh_layout2.isRefreshing = true

    return rootView
  }

  private operator fun next() {
    displayType = (displayType + 1) % PERIOD.size
    general()
  }

  private fun previous() {
    displayType = if (displayType > 0) displayType - 1 else PERIOD.size - 1
    general()
  }

  private fun general() {
    price_chart.visibility = View.INVISIBLE
    chart_title.text = TITLE_TEXTS!![displayType]

    color_padding.setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary_little_darker))

    preferences.edit().putInt("displaytype_chart", displayType).apply()

    try {
      loadPriceData(TIMESTAMPS[displayType].toLong(), PERIOD[displayType])
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  @Throws(IOException::class)
  private fun loadPriceData(time: Long, period: Int) {
    etherscanApi.getPriceChart(System.currentTimeMillis() / 1000 - time, period, displayInUsd, object : Callback { // 1467321600,
      override fun onFailure(call: Call, e: IOException) {
        ac?.runOnUiThread {
          onItemsLoadComplete()
          ac?.snackError(getString(R.string.err_no_con), Snackbar.LENGTH_LONG)
        }
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        val yVals = ArrayList<Entry>()
        try {
          val data = JSONArray(response.body()!!.string())
          val exchangeRate = ExchangeCalculator.getInstance().rateForChartDisplay
          val commas = (if (displayInUsd) 100 else 10000).toFloat()
          (0 until data.length())
              .asSequence()
              .map { data.getJSONObject(it) }
              .mapTo(yVals) { Entry(it.getLong("date").toFloat(), Math.floor(it.getDouble("high") * exchangeRate * commas.toDouble()).toFloat() / commas) }

          ac?.runOnUiThread {
            price_chart.visibility = View.VISIBLE
            onItemsLoadComplete()
            if (isAdded) {
              setupChart(price_chart, getData(yVals), ContextCompat.getColor(context, R.color.color_primary_little_darker))
              update()
            }
          }

        } catch (e: JSONException) {
          e.printStackTrace()
        }

      }
    })
  }

  private fun setupChart(chart: LineChart, data: LineData, color: Int) {
    (data.getDataSetByIndex(0) as LineDataSet).setCircleColorHole(color)
    chart.description.isEnabled = false
    chart.setDrawGridBackground(false)
    chart.setTouchEnabled(false)
    chart.isDragEnabled = false
    chart.setScaleEnabled(true)
    chart.setPinchZoom(false)
    chart.setBackgroundColor(color)
    chart.setViewPortOffsets(0f, 23f, 0f, 0f)
    chart.data = data

    val legend = chart.legend
    legend.isEnabled = false

    chart.axisLeft.isEnabled = true
    chart.axisLeft.setDrawGridLines(false)
    chart.axisLeft.setDrawAxisLine(false)
    chart.axisLeft.spaceTop = 10f
    chart.axisLeft.spaceBottom = 30f
    chart.axisLeft.axisLineColor = 0xFFFFFF
    chart.axisLeft.textColor = 0xFFFFFF
    chart.axisLeft.setDrawTopYLabelEntry(true)
    chart.axisLeft.labelCount = 10

    chart.xAxis.isEnabled = true
    chart.xAxis.setDrawGridLines(false)
    chart.xAxis.setDrawAxisLine(false)
    chart.xAxis.axisLineColor = 0xFFFFFF
    chart.xAxis.textColor = 0xFFFFFF

    val tf = Typeface.DEFAULT

    // X Axis
    val xAxis = chart.xAxis
    xAxis.typeface = tf
    xAxis.removeAllLimitLines()
    xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
    xAxis.textColor = Color.argb(150, 255, 255, 255)

    if (displayType == 1 || displayType == 2) { // Week and Month
      xAxis.valueFormatter = WeekXFormatter()
    } else if (displayType == 0) { //  Day
      xAxis.valueFormatter = HourXFormatter()
    } else { // Year
      xAxis.valueFormatter = YearXFormatter()
    }

    // Y Axis
    val leftAxis = chart.axisLeft
    leftAxis.removeAllLimitLines()
    leftAxis.typeface = tf
    leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
    leftAxis.textColor = Color.argb(150, 255, 255, 255)
    leftAxis.valueFormatter = DontShowNegativeFormatter(displayInUsd)

    chart.axisRight.isEnabled = false // Deactivates horizontal lines
    chart.animateX(1300)
    chart.notifyDataSetChanged()
  }

  private fun getData(yVals: List<Entry>): LineData {
    val set1 = LineDataSet(yVals, "")
    set1.lineWidth = 1.45f
    set1.color = Color.argb(240, 255, 255, 255)
    set1.setCircleColor(Color.WHITE)
    set1.highLightColor = Color.WHITE
    set1.fillColor = ContextCompat.getColor(context, R.color.chart_filled)
    set1.setDrawCircles(false)
    set1.setDrawValues(false)
    set1.setDrawFilled(true)
    set1.fillFormatter = IFillFormatter { _, _ -> price_chart.axisLeft.axisMinimum }

    return LineData(set1)
  }

  private fun updateExchangeRates() {
    try {
      exchangeCalculator.updateExchangeRates(preferences.getString("maincurrency", "USD"), ac)
      update()
      onItemsLoadComplete()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  fun update() {
    price?.text = when {
      displayInUsd -> "${exchangeCalculator.displayUsdNicely(exchangeCalculator.usdPrice)} ${exchangeCalculator.mainCurreny.name}"
      else -> "${exchangeCalculator.displayEthNicely(exchangeCalculator.btcPrice)} BTC"
    }
    onItemsLoadComplete()
  }

  internal fun onItemsLoadComplete() {
    swipe_refresh_layout2?.isRefreshing = false
    color_padding?.setBackgroundColor(0xF05a7899.toInt())
  }

  companion object {

    private val TIMESTAMPS = intArrayOf(
        86400, // 24 hours
        604800, // Week
        2678400, // Month
        31536000 // Year
    )

    private var TITLE_TEXTS: Array<String>? = null

    private val PERIOD = intArrayOf(300, 1800, 7200, 86400)
  }
}