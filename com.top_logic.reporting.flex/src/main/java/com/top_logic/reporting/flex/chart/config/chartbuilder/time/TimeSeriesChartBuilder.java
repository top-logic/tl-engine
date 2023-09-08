/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.time;

import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.AbstractXYDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.axis.AbstractAxisBuilder;
import com.top_logic.reporting.flex.chart.config.axis.AbstractAxisBuilder.ChartAxis;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AdditionalPlotRenderer;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AdditionalPlotRenderer.NoAdditionalPlotRenderer;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.XYToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.XYURLGeneratorProvider;

/**
 * {@link AbstractJFreeChartBuilder} that builds timeseries-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TimeSeriesChartBuilder extends AbstractJFreeChartBuilder<AbstractXYDataset> {

	/**
	 * Config-interface for {@link TimeSeriesChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<AbstractXYDataset> {

		@Override
		@ClassDefault(TimeSeriesChartBuilder.class)
		public Class<? extends TimeSeriesChartBuilder> getImplementationClass();

		/**
		 * the from-date where the timeline should start, may be null for auto-range
		 */
		public Date getLowerBound();

		/**
		 * See {@link #getLowerBound()}
		 */
		public void setLowerBound(Date startDate);

		/**
		 * the to-date where the timeline should end, may be null for auto-range
		 */
		public Date getUpperBound();

		/**
		 * See {@link #getUpperBound()}
		 */
		public void setUpperBound(Date endDate);

		@Override
		@InstanceFormat
		@InstanceDefault(TimeseriesDatasetBuilder.class)
		public DatasetBuilder<? extends AbstractXYDataset> getDatasetBuilder();

		/**
		 * Factory for a {@link XYToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public XYToolTipGeneratorProvider getTooltipGeneratorProvider();

		/**
		 * See {@link #getTooltipGeneratorProvider()}
		 */
		public void setTooltipGeneratorProvider(XYToolTipGeneratorProvider provider);

		/**
		 * the factor for the upper range of the Y-axis to add some space at the max-value
		 */
		@DoubleDefault(1)
		@Name("y-min-factor")
		public double getYAxisMinFactor();

		/**
		 * the factor for the lower range of the Y-axis to add some space at the min-value
		 */
		@DoubleDefault(1)
		@Name("y-max-factor")
		public double getYAxisMaxFactor();

		/**
		 * the builder for the domain-axis (X-axis)
		 */
		@InstanceFormat
		@InstanceDefault(DefaultDomainAxis.class)
		public AbstractAxisBuilder getDomainAxis();

		/**
		 * the builder for the range-axis (Y-axis)
		 */
		@InstanceFormat
		@InstanceDefault(DefaultRangeAxis.class)
		public AbstractAxisBuilder getRangeAxis();

		@Override
		public XYURLGeneratorProvider getURLGeneratorProvider();

		/**
		 * See {@link AdditionalPlotRenderer}
		 * 
		 * @return a configured class to modify the chart, must not be null
		 */
		@InstanceFormat
		@InstanceDefault(NoAdditionalPlotRenderer.class)
		public AdditionalPlotRenderer getAdditionalPlotRenderer();
	}

	/**
	 * Config-Constructor for {@link TimeSeriesChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public TimeSeriesChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	public Class<AbstractXYDataset> datasetType() {
		return AbstractXYDataset.class;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<AbstractXYDataset> chartData) {
		LabelProvider labelProvider = getLabelProvider(0);
		TimeSeriesCollection dataset = (TimeSeriesCollection) chartData.getDataset();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			Comparable<?> key = dataset.getSeriesKey(i);
			if (key instanceof UniqueName) {
				((UniqueName) key).setProvider(labelProvider);
			}
		}
		JFreeChart result = ChartFactory.createTimeSeriesChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), dataset, getConfig().getShowLegend(),
			false, false);
		return result;
	}

	@Override
	protected void adaptChart(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);

		XYPlot plot = (XYPlot) model.getPlot();
		plot.setRenderer(renderer);

	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYToolTipGenerator generator = getConfig().getTooltipGeneratorProvider().getXYTooltipGenerator(model, context, chartData);
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultToolTipGenerator(generator);
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {
		super.modifyPlot(model, context, chartData);

		XYPlot plot = (XYPlot) model.getPlot();
		for (int i = 0; i < chartData.getDataset().getSeriesCount(); i++) {
			plot.getRenderer().setSeriesPaint(i, getColor(chartData.getDataset().getSeriesKey(i)));
		}

		plot.setRangeZeroBaselineVisible(true);

		getConfig().getAdditionalPlotRenderer().adaptPlot(model, chartData);

		AbstractAxisBuilder rangeAxis = getConfig().getRangeAxis();
		if (rangeAxis != null) {
			rangeAxis.applyTo(plot, ChartAxis.RANGE, chartData, getConfig());
		}
		AbstractAxisBuilder domainAxis = getConfig().getDomainAxis();
		if (domainAxis != null) {
			domainAxis.applyTo(plot, ChartAxis.DOMAIN, chartData, getConfig());
		}

	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYURLGenerator generator = getConfig().getURLGeneratorProvider().getXYURLGenerator(model, context, chartData);
		plot.getRenderer().setURLGenerator(generator);
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	@Override
	public int getMinDimensions() {
		return 1;
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
	 * Factory method to create an initialized {@link TimeSeriesChartBuilder}.
	 * 
	 * @return a new TimeSeriesChartBuilder.
	 */
	public static TimeSeriesChartBuilder instance() {
		return (TimeSeriesChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item());
	}

}
