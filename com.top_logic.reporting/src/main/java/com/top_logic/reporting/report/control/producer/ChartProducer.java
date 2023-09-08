/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.producer;

import org.jfree.chart.JFreeChart;

import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.view.component.DefaultProducerChartComponent;

/**
 * The ChartProducer creates {@link JFreeChart} for a {@link ChartContext}. 
 * The producer works together with the {@link DefaultProducerChartComponent}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface ChartProducer extends ChartConstants {

	/** Key for the chart context if the legend is shown. */
    public static final String VALUE_SHOW_LEGEND   = "value.show.legend";
    /** Key for the chart context if the tooltips are shown. */
    public static final String VALUE_SHOW_TOOLTIPS = "value.show.tooltips";
    /** Key for the chart context if the urls are shown. */
    public static final String VALUE_SHOW_URLS     = "value.show.urls";
    
    /** Key for the chart context for the chart background. */
    public static final String VALUE_PAINT_BACKGROUND = "value.paint.background";
    /** Key for the chart context if anti aliasing is used. */
    public static final String VALUE_ANTI_ALIAS       = "value.anti.alias";

    /**
	 * This method returns for the given chart context a {@link JFreeChart}. If
	 * it is possible this method returns NOT null.
	 * 
	 * This method can throw an {@link ReportingException}.
	 * 
	 * @param aChartContext
	 *            The chart context contains all necessary information to
	 *            produce the chart. Must NOT be <code>null</code>.
	 */
    public JFreeChart produceChart(ChartContext aChartContext);
    
    /**
	 * This method can be used to check if a {@link ChartContext} is supported
	 * by the chart producer. If the chart producer is used in a
	 * {@link DefaultProducerChartComponent} the component delegates the
	 * supports requests to the producer. 
	 * 
	 * @param aChartContext
	 *            The chart context must NOT be <code>null</code>.
	 */
    public boolean supports(ChartContext aChartContext);
    
}

