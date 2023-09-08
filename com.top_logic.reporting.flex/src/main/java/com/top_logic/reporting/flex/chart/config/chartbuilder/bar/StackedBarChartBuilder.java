/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.bar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;

/**
 * {@link BarChartBuilder} that builds stacked-bar-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class StackedBarChartBuilder extends BarChartBuilder {

	/**
	 * Config-interface for {@link StackedBarChartBuilder}.
	 */
	public interface Config extends BarChartBuilder.Config {

		@Override
		@ClassDefault(StackedBarChartBuilder.class)
		public Class<? extends StackedBarChartBuilder> getImplementationClass();
	}

	/**
	 * Config-Constructor for {@link StackedBarChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public StackedBarChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	protected JFreeChart createJFreeChart(CategoryDataset dataSet) {

		PlotOrientation orientation = getOrientation();
		return ChartFactory.createStackedBarChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), dataSet, orientation, getConfig().getShowLegend(),
			getConfig().getShowTooltips(), false);
	}

	@Override
	protected void adaptChart(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.adaptChart(model, context, chartData);
		if (getConfig().getShowPeaks()) {
			CategoryPlot plot = (CategoryPlot) model.getPlot();
			plot.setRenderer(new ExtendedStackedBarRenderer());
		}
	}

	@Override
	protected ItemLabelPosition getItemLabelPosition() {
		return null;
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	/**
	 * Factory method to create an initialized {@link StackedBarChartBuilder}.
	 * 
	 * @return a new StackedBarChartBuilder.
	 */
	public static StackedBarChartBuilder instance() {
		return (StackedBarChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item());
	}

}
