/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.producer;


import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.basic.col.filter.DateFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class TimeSeriesChartProducer extends AbstractChartProducer {

	/** DateFilter that defines the range of the x-axis */
    public static final String VALUE_DATE_FILTER        = "dateFilter";
    /** PlotOrientation of the labels of the x-axis */
    public static final String X_AXIS_LABEL_ORIENTATION = "xAxisLabelOrientation";
    /** DateFormat of the labels of the x-axis */
    public static final String X_AXIS_LABEL_FORMAT      = "xAxisLabelFormat";
    
    /** Default format of the labels of the X_AXIS */
    private static final String DEFAULT_FORMAT = "MMM yy";
    
    /**
	 * Creates a new {@link TimeSeriesChartProducer}.
	 */
	public TimeSeriesChartProducer(InstantiationContext context, Config someAttrs) {
		super(context, someAttrs);
    }

    @Override
	protected final JFreeChart createChart(ChartContext aChartContext) {
        XYDataset  theData  = this.createDataSet(aChartContext);

        String  theTitle     = getString(aChartContext,  LABEL_TITLE,  "");
        String  xAxisLabel   = getString(aChartContext,  LABEL_X_AXIS, "");
        String  yAxisLabel   = getString(aChartContext,  LABEL_Y_AXIS, "");
        boolean showLegend   = showLegend(aChartContext);
        boolean showTooltips = showTooltips(aChartContext);
        boolean showUrls     = showUrls(aChartContext);
        
        JFreeChart theChart = ChartFactory.createTimeSeriesChart(theTitle, xAxisLabel, yAxisLabel, theData, showLegend, showTooltips, showUrls);
        
        this.postProcessChart(aChartContext, theData, theChart);
        
        return theChart;
    }
    
    protected abstract XYDataset createDataSet(ChartContext chartContext);
    
    protected abstract void postProcessChart(ChartContext aChartContext, XYDataset aDataSet, JFreeChart aChart);
    
    @Override
	public final JFreeChart produceChart(ChartContext chartContext) {
        JFreeChart theChart = super.produceChart(chartContext);
        XYPlot     thePlot  = (XYPlot) theChart.getPlot();


		thePlot.setDomainAxis(this.configureXAxis(chartContext, thePlot, createXAxis(thePlot)));
		thePlot.setRangeAxis(this.configureYAxis(chartContext, thePlot, createYAxis(thePlot)));

        return theChart;
    }

	/**
	 * X - Axis is a date axis in time series chart...
	 */
	protected DateAxis createXAxis(XYPlot aPlot) {
		return new DateAxis();
	}

	/**
	 * By default the Y-Axis is the value axis of the plot
	 */
	protected ValueAxis createYAxis(XYPlot aPlot) {
		return aPlot.getRangeAxis();
	}

	/**
	 * Set up the parameter of the x-axis as default axis configuration
	 * 
	 * @return the configured axis
	 */
	protected ValueAxis configureXAxis(ChartContext aChartContext, XYPlot aPlot, DateAxis anXAxis) {
        TickUnits theTickUnits = getDefaultTickUnits();

        PlotOrientation labelOrientation = (PlotOrientation) aChartContext.getValue(X_AXIS_LABEL_ORIENTATION);
        DateFilter dateFilter = (DateFilter) aChartContext.getValue(VALUE_DATE_FILTER);
		Date start=null, end=null;
		if(dateFilter!=null){
			start = dateFilter.getStartDate();
			end = dateFilter.getEndDate();
		}
		DateFormat labelFormat =
			this.getDefaultDateFormat(aChartContext, X_AXIS_LABEL_FORMAT);
		AbstractChartProducer.applyToValueAxis(anXAxis);
		return configureDateAxis(anXAxis, start, end, labelOrientation, labelFormat, theTickUnits,
			getXAxisLabel(aChartContext));
	}

	/**
	 * Set up the parameter of the y-axis as default axis configuration
	 * 
	 * @return the configured axis
	 */
	protected ValueAxis configureYAxis(ChartContext chartContext, XYPlot aPlot, ValueAxis anAxis) {
		anAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		AbstractChartProducer.applyToValueAxis(anAxis);
		String theLabel = getYAxisLabel(chartContext);
		if (theLabel != null) {
			anAxis.setLabel(theLabel);
		}
		return anAxis;
	}

	/**
	 * return the dateformat from the given context property or the default format, if the property
	 * is not set
	 */
	protected DateFormat getDefaultDateFormat(ChartContext Ctx, String property) {
		return this.getDateFormat(Ctx, property, CalendarUtil.newSimpleDateFormat(DEFAULT_FORMAT));
	}

	/**
	 * the defaut Tickunits to be used in x-axis default configuration
	 */
	protected TickUnits getDefaultTickUnits() {
		TickUnits theTickUnits = new TickUnits();

        theTickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 1));
        theTickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 3));
        theTickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 6));
        theTickUnits.add(new DateTickUnit(DateTickUnitType.YEAR,  1));
		return theTickUnits;
	}

	/**
	 * the label to be used for the x-axis
	 */
	protected String getXAxisLabel(ChartContext chartContext) {
		return null;
	}

	/**
	 * the label to be used in the y-axis
	 */
	protected String getYAxisLabel(ChartContext chartContext) {
		return null;
	}
	
	/**
	 * Configure a dateaxis with the given parameters
	 * 
	 * @return the configured axis
	 */
	protected ValueAxis configureDateAxis(DateAxis theAxis, Date rangeStart, Date rangeEnd,
			PlotOrientation labelOrientation, DateFormat labelFormat, TickUnitSource theTickUnits, String axisLabel) {
		if (labelOrientation == ChartConstants.PLOT_HORIZONTAL) {
            theAxis.setVerticalTickLabels(false);
        }
		else if (labelOrientation == ChartConstants.PLOT_VERTICAL) {
            theAxis.setLabelAngle(90);
            theAxis.setVerticalTickLabels(true);
        }
        
		if (rangeStart != null && rangeEnd != null) {
            theAxis.setAutoRange(false);
			theAxis.setRange(rangeStart.getTime(), rangeEnd.getTime());
		} else {
			theAxis.setAutoRange(true);
		}

        theAxis.setStandardTickUnits(theTickUnits);
        
		theAxis.setDateFormatOverride(labelFormat);

		if (axisLabel != null) {
			theAxis.setLabel(axisLabel);
		}
        
        return theAxis;
	}

}

