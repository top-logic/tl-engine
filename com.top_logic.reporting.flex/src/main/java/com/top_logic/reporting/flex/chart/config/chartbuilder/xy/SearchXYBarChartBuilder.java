/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.xy;

import java.awt.Paint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.Orientation;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.IntervalXYDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.XYToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.XYURLGeneratorProvider;

/**
 * {@link AbstractJFreeChartBuilder} that builds XY-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class SearchXYBarChartBuilder extends AbstractJFreeChartBuilder<IntervalXYDataset> {

	/**
	 * Config-interface for {@link SearchXYBarChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<IntervalXYDataset> {

		@Override
		@ClassDefault(SearchXYBarChartBuilder.class)
		public Class<SearchXYBarChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(IntervalXYDatasetBuilder.class)
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

		@Override
		public XYURLGeneratorProvider getURLGeneratorProvider();

		@Override
		@FormattedDefault("horizontal")
		public Orientation getOrientation();
	}

	/**
	 * Config-Constructor for {@link SearchXYBarChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public SearchXYBarChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}
	
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<IntervalXYDataset> chartData) {
		XYIntervalSeriesCollection dataset = (XYIntervalSeriesCollection) chartData.getDataset();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			Comparable<?> key = dataset.getSeriesKey(i);
			if (key instanceof UniqueName) {
				Comparable<?> key2 = ((UniqueName) key).getKey();
				if (key2 instanceof Tuple) {
					for (int j = 0; j < ((Tuple) key2).size(); j++) {
						Object object = ((Tuple) key2).get(j);
						if (object instanceof UniqueName) {
							LabelProvider labelProvider = getLabelProvider(j);
							((UniqueName) object).setProvider(labelProvider);
						}
					}
				}
				else {
					LabelProvider labelProvider = getLabelProvider(0);
					((UniqueName) key).setProvider(labelProvider);
				}
			}
		}

		JFreeChart chart = createJFreeChart(chartData.getDataset());
		return chart;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<IntervalXYDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYURLGenerator generator = getConfig().getURLGeneratorProvider().getXYURLGenerator(model, context, (ChartData) chartData);
		plot.getRenderer().setURLGenerator(generator);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<IntervalXYDataset> chartData) {
		XYToolTipGenerator generator = getConfig().getTooltipGeneratorProvider().getXYTooltipGenerator(model, context, (ChartData) chartData);
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
		plot.setRangePannable(true);
		plot.setRangeAxis(new DateAxis());
		int seriesCount = chartData.getDataset().getSeriesCount();
		String[] seriesNames = new String[seriesCount];
		for (int i = 0; i < seriesCount; i++) {
			seriesNames[i] = chartData.getDataset().getSeriesKey(i).toString();
		}
		SymbolAxis xAxis = new SymbolAxis(null, seriesNames);
		xAxis.setGridBandsVisible(false);
		plot.setDomainAxis(xAxis);
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setUseYInterval(true);
		plot.setRenderer(renderer);

		for (int i = 0; i < chartData.getDataset().getSeriesCount(); i++) {
			Comparable<?> rowKey = chartData.getDataset().getSeriesKey(i);
			Paint color = getColor(rowKey);
			renderer.setSeriesPaint(i, color);
		}

		plot.setFixedLegendItems(new LegendItemCollection());
	}

	@Override
	public int getMaxDimensions() {
		return 5;
	}

	@Override
	public int getMinDimensions() {
		return 1;
	}

	private JFreeChart createJFreeChart(IntervalXYDataset dataSet) {
		PlotOrientation po = getOrientation();
		return ChartFactory.createXYBarChart(getTitle(), getXAxisLabel(),
			true, getYAxisLabel(), dataSet, po, getConfig().getShowLegend(),
			false, false);
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
	 * Factory method to create an initialized {@link SearchXYBarChartBuilder}.
	 * 
	 * @return a new XYBarChartBuilder.
	 */
	public static SearchXYBarChartBuilder newInstance() {
		return (SearchXYBarChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(defaultConfig());
	}

}
