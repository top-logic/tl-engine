/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.configurator;

import java.text.DateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.TickUnits;
import org.jfree.data.time.TimeSeriesCollection;

import com.top_logic.mig.html.HTMLFormatter;

/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TimeSeriesChartConfigurator extends XYChartConfigurator {

    /**
     * Creates a {@link TimeSeriesChartConfigurator} with the given chart.
     * 
     * @param aChart
     *        A {@link JFreeChart} to configure.
     */
    public TimeSeriesChartConfigurator(JFreeChart aChart) {
        super(aChart);
    }
    
    /**
     * Creates a {@link TimeSeriesChartConfigurator}. The constructor
     * creates intern a new bar chart with the given dataset.
     * 
     * @param legend
     *        The flag indicates whether a legend is shown.
     * @param aDataset
     *        A {@link TimeSeriesCollection}. Must not be <code>null</code>.
     */
    public TimeSeriesChartConfigurator(boolean legend, TimeSeriesCollection aDataset) {
        this("", "", "", legend, aDataset);
    }
    
    /**
     * Creates a {@link TimeSeriesChartConfigurator}. The constructor
     * creates intern a new bar chart with the given values.
     * 
     * @param aTitle
     *        A chart title. Must not be <code>null</code>.
     * @param aXAxisLabel
     *        A x-axis label. Must not be <code>null</code>.
     * @param aYAxisLabel
     *        A y-axis label. Must not be <code>null</code>.
     * @param legend
     *        The flag indicates whether a legend is shown.
     * @param aDataset
     *        A {@link TimeSeriesCollection}. Must not be <code>null</code>.
     */
    public TimeSeriesChartConfigurator(String aTitle, String aXAxisLabel, String aYAxisLabel, boolean legend, TimeSeriesCollection aDataset) {
        super(ChartFactory.createTimeSeriesChart(aTitle, 
                                                 aXAxisLabel, 
                                                 aYAxisLabel, 
                                                 aDataset,
                                                 legend, 
                                                 !TOOLTIPS, 
                                                 !URLS));
    }

    /**
     * This method sets the standart date tick units. The tick units uses the
     * given {@link DateFormat} to format the dates.
     * 
     * @param aFormat
     *        A {@link DateFormat}. Must not be <code>null</code>.
     */
    public void useStandardDateTickUnits(DateFormat aFormat) {
        if (!(getDomainAxis() instanceof DateAxis)) {
            throw new UnsupportedOperationException("The date tick units can only be set to an date axis.");
        }
        
        TickUnits theTickUnits = new TickUnits();
		theTickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 1, aFormat));
		theTickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 7, aFormat));
		theTickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 14, aFormat));
		theTickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 1, aFormat));
		theTickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 3, aFormat));
		theTickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 6, aFormat));
		theTickUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 1, aFormat));
        
        getDomainAxis().setStandardTickUnits(theTickUnits);
    }
    
    
    /**
     * This method sets the standard date tick units. The tick units uses the
     * {@link HTMLFormatter}.
     */
    public void useStandardDateTickUnitsTL() {
        useStandardDateTickUnits(HTMLFormatter.getInstance().getDateFormat());
    }
    
}

