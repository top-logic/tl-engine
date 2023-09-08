/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.urls.CategoryURLGenerator;

import com.top_logic.base.chart.ChartChoice;
import com.top_logic.base.chart.util.ChartType;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.exception.ReportingException;
import com.top_logic.reporting.report.view.component.ChartData;
import com.top_logic.reporting.report.view.component.DefaultProducerChartComponent;
import com.top_logic.reporting.report.view.component.ExtendedProducerChartComponent;

/**
 * Producer for chart information handled by the {@link ExtendedProducerChartComponent}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ExtendedChartProducer {
    
    /**
     * This method returns for the given chart context a {@link JFreeChart}. If
     * it is possible this method returns NOT null.
     * 
     * This method can throw an {@link ReportingException}.
     * 
     * @param    aContext          The chart context contains all necessary information to produce the chart, must NOT be <code>null</code>.
     * @param    anURLGenerator    The generator for URLs, may be <code>null</code>.
     * @return   The chart data object, never <code>null</code>.
     */
    public ChartData produceChartData(ChartContext aContext, CategoryURLGenerator anURLGenerator);

    /** 
     * Return the names of the supported chart types (to be used with a {@link ChartChoice} component)
     * 
     * @return    The requested list of supported chart types, never <code>null</code> or empty.
     */
	public List<ChartType> getSupportedChartTypes();

    /**
     * This method can be used to check if a {@link ChartContext} is supported
     * by the chart producer. If the chart producer is used in a
     * {@link DefaultProducerChartComponent} the component delegates the
     * supports requests to the producer. 
     * 
     * @param    aContext    The chart context must NOT be <code>null</code>.
     */
    public boolean supports(ChartContext aContext);

    /** 
     * Check, if the given object is supported by the component (and the producer).
     * 
     * This method will check the business model which is no {@link ChartContext} like the
     * method {@link #supports(ChartContext)}. The {@link ExtendedProducerChartComponent} will
     * do the handling of {@link ChartContext} and business model.
     * 
     * @param    anObject    The object to be checked.
     * @return   <code>true</code> if this model is supported.
     */
    public boolean supportsObject(Object anObject);
}

