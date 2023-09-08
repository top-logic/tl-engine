/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.xy;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYSeriesCollection;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.XYSeriesBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.XYToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.XYURLGeneratorProvider;

/**
 * {@link AbstractJFreeChartBuilder} that builds XY-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class XYLineChartBuilder extends AbstractJFreeChartBuilder<XYSeriesCollection> {

	/**
	 * Config-interface for {@link XYLineChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<XYSeriesCollection> {

		@Override
		@ClassDefault(XYLineChartBuilder.class)
		public Class<XYLineChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(XYSeriesBuilder.class)
		public DatasetBuilder<? extends XYSeriesCollection> getDatasetBuilder();

		/**
		 * Factory for a {@link XYToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public XYToolTipGeneratorProvider getTooltipGeneratorProvider();

		@Override
		public XYURLGeneratorProvider getURLGeneratorProvider();
	}

	/**
	 * Config-Constructor for {@link XYLineChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public XYLineChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}
	
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<XYSeriesCollection> chartData) {
		JFreeChart chart = createJFreeChart(chartData.getDataset());
		return chart;
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<XYSeriesCollection> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYURLGenerator generator = getConfig().getURLGeneratorProvider().getXYURLGenerator(model, context, chartData);
		plot.getRenderer().setURLGenerator(generator);
	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<XYSeriesCollection> chartData) {
		XYToolTipGenerator generator =
			getConfig().getTooltipGeneratorProvider().getXYTooltipGenerator(model, context,
				chartData);
		XYPlot plot = (XYPlot) model.getPlot();
		plot.getRenderer().setDefaultToolTipGenerator(generator);
	}

	@Override
	public Class<XYSeriesCollection> datasetType() {
		return XYSeriesCollection.class;
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	@Override
	public int getMinDimensions() {
		return 2;
	}

	private JFreeChart createJFreeChart(XYSeriesCollection dataSet) {
		PlotOrientation po = getOrientation();
		return ChartFactory.createXYLineChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), dataSet, po, getConfig().getShowLegend(),
			false, false);
	}


}
