/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.post;

import org.jfree.chart.JFreeChart;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * The NoOpPostCreationHandler does nothing.
 * 
 * @author    <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public class NoOpPostCreationHandler implements PostChartCreationHandler {

    public static final NoOpPostCreationHandler INSTANCE = new NoOpPostCreationHandler();
    
    @Override
	public void handle(LayoutComponent aLayoutComponent, ChartContext aChartContext, JFreeChart aChart, boolean sendEvent) {
        // Do nothing
    }

}

