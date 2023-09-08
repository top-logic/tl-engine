/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.xy;

import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.IntervalXYDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.reporting.flex.chart.config.axis.AbstractAxisBuilder;
import com.top_logic.reporting.flex.chart.config.axis.AbstractAxisBuilder.ChartAxis;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.XYToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.XYURLGeneratorProvider;

/**
 * {@link AbstractJFreeChartBuilder} that builds XY-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class XYBarChartBuilder extends AbstractJFreeChartBuilder<IntervalXYDataset> {

	/**
	 * Config-interface for {@link XYBarChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<IntervalXYDataset> {

		@Override
		@ClassDefault(XYBarChartBuilder.class)
		public Class<XYBarChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(TimeseriesDatasetBuilder.class)
		public DatasetBuilder<? extends IntervalXYDataset> getDatasetBuilder();

		/**
		 * Factory for a {@link XYToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public XYToolTipGeneratorProvider getTooltipGeneratorProvider();

		/**
		 * see {@link #getTooltipGeneratorProvider()}
		 */
		public void setTooltipGeneratorProvider(XYToolTipGeneratorProvider provider);

		/**
		 * the builder for the domain-axis (X-axis)
		 */
		@InstanceFormat
		public AbstractAxisBuilder getDomainAxis();

		/**
		 * the builder for the range-axis (Y-axis)
		 */
		@InstanceFormat
		public AbstractAxisBuilder getRangeAxis();

		@Override
		public XYURLGeneratorProvider getURLGeneratorProvider();
	}

	/**
	 * Config-Constructor for {@link XYBarChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public XYBarChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}
	
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<IntervalXYDataset> chartData) {
		JFreeChart chart = createJFreeChart(chartData.getDataset());
		return chart;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<IntervalXYDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYURLGenerator generator =
			getConfig().getURLGeneratorProvider().getXYURLGenerator(model, context,
				(ChartData) chartData);
		plot.getRenderer().setURLGenerator(generator);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<IntervalXYDataset> chartData) {
		XYToolTipGenerator generator =
			getConfig().getTooltipGeneratorProvider().getXYTooltipGenerator(model, context,
				(ChartData) chartData);
		XYPlot plot = (XYPlot) model.getPlot();
		plot.getRenderer().setDefaultToolTipGenerator(generator);
	}

	@Override
	public Class<IntervalXYDataset> datasetType() {
		return IntervalXYDataset.class;
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<IntervalXYDataset> chartData) {
		super.modifyPlot(model, context, chartData);

		XYPlot plot = (XYPlot) model.getPlot();

		AbstractAxisBuilder rangeAxis = getConfig().getRangeAxis();
		if (rangeAxis != null) {
			rangeAxis.applyTo(plot, ChartAxis.RANGE, chartData, getConfig());
		}
		AbstractAxisBuilder domainAxis = getConfig().getDomainAxis();
		if (domainAxis != null) {
			domainAxis.applyTo(plot, ChartAxis.DOMAIN, chartData, getConfig());
		}

		XYItemRenderer renderer = plot.getRenderer();
		for (int i = 0; i < chartData.getDataset().getSeriesCount(); i++) {
			Comparable<?> rowKey = chartData.getDataset().getSeriesKey(i);
			Paint color = getColor(rowKey);
			renderer.setSeriesPaint(i, color);
		}
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	@Override
	public int getMinDimensions() {
		return 2;
	}

	private JFreeChart createJFreeChart(IntervalXYDataset dataSet) {
		PlotOrientation po = getOrientation();
		return ChartFactory.createXYBarChart(getTitle(), getXAxisLabel(),
			true, getYAxisLabel(), dataSet, po, getConfig().getShowLegend(),
			false, false);
	}


}
