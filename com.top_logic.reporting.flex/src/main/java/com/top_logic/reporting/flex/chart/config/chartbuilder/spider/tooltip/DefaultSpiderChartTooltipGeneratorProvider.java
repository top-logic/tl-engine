/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.spider.tooltip;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.spider.SpiderChartBuilder.Config;

/**
 * Default-implementation of {@link SpiderToolTipGeneratorProvider} to create a
 * {@link DefaultSpiderChartTooltipGenerator} that ignores the configured special-series of the
 * spider-chart.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DefaultSpiderChartTooltipGeneratorProvider implements SpiderToolTipGeneratorProvider {

	/**
	 * Singleton <code>INSTANCE</code> for {@link DefaultSpiderChartTooltipGeneratorProvider}
	 */
	public static DefaultSpiderChartTooltipGeneratorProvider INSTANCE = new DefaultSpiderChartTooltipGeneratorProvider();

	@Override
	public CategoryToolTipGenerator getSpiderTooltipGenerator(JFreeChart model, ChartContext context, ChartData<? extends CategoryDataset> chartData, Config config) {
		int size = config.getSpecialSeries().size();
		return new DefaultSpiderChartTooltipGenerator(size);
	}

}