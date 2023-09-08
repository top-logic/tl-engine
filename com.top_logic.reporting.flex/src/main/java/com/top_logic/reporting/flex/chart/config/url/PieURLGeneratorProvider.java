/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.url;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.PieDataset;

import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;

/**
 * Provider for a url-generator that needs context-information.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface PieURLGeneratorProvider extends JFreeChartURLGeneratorProvider {

	/**
	 * the generator to be applied to a {@link PiePlot}
	 */
	public PieURLGenerator getPieTooltipGenerator(JFreeChart model, ChartContext context, ChartData<? extends PieDataset> chartData);

}