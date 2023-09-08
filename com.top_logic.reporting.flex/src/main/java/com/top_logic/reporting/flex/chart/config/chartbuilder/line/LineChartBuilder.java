/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.line;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.bar.BarChartBuilder;

/**
 * Mostly like {@link BarChartBuilder} but creates line-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class LineChartBuilder extends BarChartBuilder {

	/**
	 * Config-interface for {@link LineChartBuilder}.
	 */
	public interface Config extends BarChartBuilder.Config {

		@Override
		@ClassDefault(LineChartBuilder.class)
		public Class<LineChartBuilder> getImplementationClass();
	}

	/**
	 * Config-Constructor for {@link LineChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public LineChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	protected JFreeChart createJFreeChart(CategoryDataset dataSet) {
		PlotOrientation po = getOrientation();
		return ChartFactory.createLineChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), dataSet, po, getConfig().getShowLegend(),
			getConfig().getShowTooltips(), false);
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.modifyPlot(model, context, chartData);
	}

}
