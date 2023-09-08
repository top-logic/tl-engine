/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart;

import java.util.List;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.base.chart.util.ChartType;

/**
 * This interface is an abstract way to handle chart components which can 
 * handle more than one chart type.
 * 
 * E.g.
 * Subclasses of the {@link JFreeChartComponent} can implement this interface
 * to indicate that they can produce more than one chart type (e.g. bar charts, 
 * pie charts or time series charts). A chart component can only shown one 
 * chart at the same time. Which chart type is produced depends on the current 
 * selection.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface ChartChoice extends ChartConstants {

    /**
	 * This method sets the given chart type as selection if the chart type is supported.
	 * 
	 * All chart types which are returnd by the {@link #getSupportedChartTypes()}-method are
	 * supported. Use the {@link #isChartTypeSupported(ChartType)}-method to check whether the chart
	 * type is supported.
	 * 
	 * @param chartType
	 *        A chart type. Must not be <code>null</code>. Please use for the chart type the
	 *        constants from {@link ChartConstants}.
	 * @return Returns <code>true</code> is the selection could be set, <code>false</code>
	 *         otherwise.
	 */
	public boolean setSelection(ChartType chartType);
    
    /**
     * This method returns the selected chart type never <code>null</code> or
     * an empty {@link String}. A selection must be set always.
     */
	public ChartType getSelection();
    
    /**
     * This method returns <code>true</code> if the chart type is supported,
     * <code>false</code> otherwise.
     * 
     * @param chartType
     *        A chart type. Must not be <code>null</code>. 
     *        Please use for the chart type the constants from {@link ChartConstants}.
     */
	public boolean isChartTypeSupported(ChartType chartType);
    
    /**
     * This method returns a list of supported chart types. A class which
     * implements this interface must support at least one chart type. It is not
     * allowed to return an empty list or <code>null</code>. 
     * 
     * The chart types are {@link String}s.
     * Please use for the chart type the constants from {@link ChartConstants}.
     */
	public List<ChartType> getSupportedChartTypes();
    
}

