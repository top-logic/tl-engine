/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.url;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.general.AbstractSeriesDataset;

import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;

/**
 * Provider for a url-generator that needs context-information.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface XYURLGeneratorProvider extends JFreeChartURLGeneratorProvider {

	/**
	 * the generator to be applied to a {@link XYPlot}
	 */
	public XYURLGenerator getXYURLGenerator(JFreeChart model, ChartContext context, ChartData<? extends AbstractSeriesDataset> chartData);
	
}