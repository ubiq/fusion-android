package com.ubiqsmart.ui.widgets.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.*;

public class HourXFormatter implements IAxisValueFormatter {

  private GregorianCalendar c = new GregorianCalendar();

  @Override public String getFormattedValue(float value, AxisBase axis) {
    c.setTimeInMillis(((long) (value)) * 1000);
    return c.get(Calendar.HOUR_OF_DAY) + ":00 ";
  }
}
