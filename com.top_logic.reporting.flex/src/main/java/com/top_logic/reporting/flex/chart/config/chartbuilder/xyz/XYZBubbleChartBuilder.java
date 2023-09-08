/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.xyz;

import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.urls.XYZURLGenerator;
import org.jfree.data.xy.XYZDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.pie.PieChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.xy.XYLineChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.XYZDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.XYZToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.XYZURLGeneratorProvider;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class XYZBubbleChartBuilder extends AbstractJFreeChartBuilder<XYZDataset> {

	/**
	 * Config-interface for {@link XYLineChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<XYZDataset> {

		@Override
		@ClassDefault(XYZBubbleChartBuilder.class)
		public Class<XYZBubbleChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(XYZDatasetBuilder.class)
		public DatasetBuilder<? extends XYZDataset> getDatasetBuilder();

		/**
		 * Factory for a {@link XYZToolTipGeneratorProvider}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public XYZToolTipGeneratorProvider getTooltipGeneratorProvider();

		@Override
		public XYZURLGeneratorProvider getURLGeneratorProvider();
	}

	/**
	 * Config-Constructor for {@link XYZBubbleChartBuilder}.
	 * 
	 * @param context
	 *        - {@link InstantiationContext} for creating the XYZBubbleChartBuilder
	 * @param config
	 *        - Configuration of the {@link XYZBubbleChartBuilder}
	 */
	public XYZBubbleChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Class<? extends XYZDataset> datasetType() {
		return XYZDataset.class;
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<XYZDataset> chartData) {
		PlotOrientation po = getOrientation();
		return ChartFactory.createBubbleChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), chartData.getDataset(), po, getConfig().getShowLegend(),
			false, false);
	}


	@Override
	public int getMaxDimensions() {
		return 3;
	}

	@Override
	public int getMinDimensions() {
		return 3;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<XYZDataset> chartData) {
		XYZToolTipGenerator generator =
			getConfig().getTooltipGeneratorProvider().getXYZTooltipGenerator(model, context, (ChartData) chartData);
		XYPlot plot = (XYPlot) model.getPlot();
		plot.getRenderer().setDefaultToolTipGenerator(generator);
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<XYZDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYZURLGenerator generator = getConfig().getURLGeneratorProvider().getXYZURLGenerator(model, context, chartData);
		plot.getRenderer().setURLGenerator(generator);
	}

	@Override
	protected void modifyPlot(JFreeChart model, ChartContext context, ChartData<XYZDataset> chartData) {
		super.modifyPlot(model, context, chartData);

		initColors(model, chartData);
	}

	/**
	 * Hook for subclasses to initialise the colors
	 * 
	 * @param model
	 *        the {@link JFreeChart} for plot and renderer
	 * @param chartData
	 *        the universal chart-model
	 */
	protected void initColors(JFreeChart model, ChartData<XYZDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYItemRenderer renderer = plot.getRenderer();
		if (renderer == null) {
			return;
		}

		XYZDataset dataset = chartData.getDataset();
		int numberSeries = dataset.getSeriesCount();
		for (int i = 0; i < numberSeries; i++) {
			Paint seriesColor = getColor(dataset.getSeriesKey(i));
			renderer.setSeriesPaint(i, seriesColor);
		}
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config defaultConfig() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	/**
	 * Factory method to create an initialized {@link PieChartBuilder}.
	 * 
	 * @return a new XYZBubbleChartBuilder.
	 */
	public static XYZBubbleChartBuilder newInstance() {
		return (XYZBubbleChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(defaultConfig());
	}

}
