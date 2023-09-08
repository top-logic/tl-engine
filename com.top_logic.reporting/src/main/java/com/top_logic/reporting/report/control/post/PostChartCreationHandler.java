/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.post;

import org.jfree.chart.JFreeChart;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.view.component.AbstractFilterComponent;

/**
 * The PostChartCreationHandler handles additional operations
 * after a chart is created by the {@link AbstractFilterComponent}.
 * 
 * All instances that implement this interface must have a default 
 * constructor without parameters.
 *
 * E.g. send a event with legend items to an other component that act as legend.
 * 
 * @author    <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public interface PostChartCreationHandler {

    public void handle(LayoutComponent aLayoutComponent, ChartContext aChartContext, JFreeChart aChart, boolean sendEvent);
   
}

