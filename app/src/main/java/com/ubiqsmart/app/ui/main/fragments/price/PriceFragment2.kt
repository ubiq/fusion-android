package com.ubiqsmart.app.ui.main.fragments.price

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseFragment
import com.ubiqsmart.extensions.obtainViewModel
import kotlinx.android.synthetic.main.fragment_price2.*

class PriceFragment2 : BaseFragment() {

  private lateinit var viewModel: PriceViewModel

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_price2, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    onSetupChartView()
    onCreateViewModel()
  }

  override fun onDestroyView() {
    viewModel.onDestroyView()
    super.onDestroyView()
  }

  private fun onSetupChartView() {
    candlestick_chart_view.description.isEnabled = false
    candlestick_chart_view.legend.isEnabled = false
    candlestick_chart_view.setTouchEnabled(true)
    candlestick_chart_view.setScaleEnabled(true)
    candlestick_chart_view.setPinchZoom(true)

    candlestick_chart_view.axisLeft.isEnabled = true
    candlestick_chart_view.axisLeft.setDrawGridLines(false)
    candlestick_chart_view.axisLeft.disableGridDashedLine()
    candlestick_chart_view.axisLeft.gridLineWidth = 1.2f
    candlestick_chart_view.axisLeft.gridColor = Color.WHITE

    candlestick_chart_view.axisRight.isEnabled = false
    candlestick_chart_view.axisRight.setDrawGridLines(false)
    candlestick_chart_view.axisRight.gridLineWidth = 1.2f
    candlestick_chart_view.axisRight.gridColor = Color.WHITE
//    candlestick_chart_view.axisLeft.setDrawGridLines(false)
//    candlestick_chart_view.axisLeft.setDrawAxisLine(false)
//    candlestick_chart_view.axisLeft.spaceTop = 10f
//    candlestick_chart_view.axisLeft.spaceBottom = 30f
//    candlestick_chart_view.axisLeft.axisLineColor = 0xFFFFFF
//    candlestick_chart_view.axisLeft.textColor = 0xFFFFFF
//    candlestick_chart_view.axisLeft.setDrawTopYLabelEntry(false)
//    candlestick_chart_view.axisLeft.labelCount = 0

//    candlestick_chart_view.xAxis.isEnabled = false
//    candlestick_chart_view.xAxis.setDrawGridLines(false)
//    candlestick_chart_view.xAxis.setDrawAxisLine(false)
//    candlestick_chart_view.xAxis.axisLineColor = 0xFFFFFF
//    candlestick_chart_view.xAxis.textColor = 0xFFFFFF

    val yVals1 = ArrayList<CandleEntry>()

    for (i in 0 until 60) {
      val mult = i + 1
      val value = (Math.random() * 40).toFloat() + mult

      val high = (Math.random() * 9).toFloat() + 8f
      val low = (Math.random() * 9).toFloat() + 8f

      val open = (Math.random() * 6).toFloat() + 1f
      val close = (Math.random() * 6).toFloat() + 1f

      val even = i % 2 == 0

      yVals1.add(CandleEntry(
          i.toFloat(), value + high,
          value - low,
          if (even) value + open else value - open,
          if (even) value - close else value + close
      ))
    }

    val set1 = CandleDataSet(yVals1, "")

    set1.setDrawIcons(false)
    set1.setDrawValues(false)
    set1.axisDependency = AxisDependency.LEFT
    set1.shadowColor = Color.WHITE
    set1.shadowWidth = 0.5f
    set1.decreasingColor = Color.parseColor("#814C3D")
    set1.decreasingPaintStyle = Paint.Style.FILL

    set1.increasingColor = Color.parseColor("#374732")
    set1.increasingPaintStyle = Paint.Style.STROKE

    val data = CandleData(set1)

    candlestick_chart_view.data = data
    candlestick_chart_view.invalidate()
  }

  private fun onCreateViewModel() {
    viewModel = activity.obtainViewModel(PriceViewModel::class.java)
    viewModel.onViewCreated()
  }
}