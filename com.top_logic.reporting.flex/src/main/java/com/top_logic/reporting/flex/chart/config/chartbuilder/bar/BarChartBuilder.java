/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.bar;

import java.awt.Paint;
import java.text.NumberFormat;
import java.util.Locale;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.NumberUtil;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.tooltip.CategoryToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.url.CategoryURLGeneratorProvider;
import com.top_logic.util.TLContext;

/**
 * {@link AbstractJFreeChartBuilder} that builds bar-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class BarChartBuilder extends AbstractJFreeChartBuilder<CategoryDataset> {

	/**
	 * Config-interface for {@link BarChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<CategoryDataset> {

		@Override
		@ClassDefault(BarChartBuilder.class)
		public Class<? extends BarChartBuilder> getImplementationClass();

		@Override
		@InstanceFormat
		@InstanceDefault(CategoryDatasetBuilder.class)
		public DatasetBuilder<? extends CategoryDataset> getDatasetBuilder();

		/**
		 * true if values should be displayed on top of the bars
		 */
		@BooleanDefault(true)
		public boolean getShowPeaks();

		/**
		 * Factory for a {@link CategoryToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public CategoryToolTipGeneratorProvider getTooltipGeneratorProvider();

		/**
		 * see {@link #getTooltipGeneratorProvider()}
		 */
		public void setTooltipGeneratorProvider(CategoryToolTipGeneratorProvider provider);

		@Override
		public CategoryURLGeneratorProvider getURLGeneratorProvider();

		/**
		 * @see BarRenderer#getMaximumBarWidth()
		 */
		@DoubleDefault(1.0d)
		public double getMaximumBarWidth();

	}

	/**
	 * Config-Constructor for {@link BarChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public BarChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Class<CategoryDataset> datasetType() {
		return CategoryDataset.class;
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<CategoryDataset> chartData) {
		CategoryDataset dataset = chartData.getDataset();
		setLabelProvider(getLabelProvider(0), dataset.getColumnKeys());
		setLabelProvider(getLabelProvider(1), dataset.getRowKeys());
		JFreeChart result = createJFreeChart(dataset);
		return result;
	}

	/**
	 * Hook for subclasses
	 * 
	 * @param dataSet
	 *        the dataset to create the chart for
	 * @return a bar chart, see
	 *         {@link ChartFactory#createBarChart(String, String, String, CategoryDataset, PlotOrientation, boolean, boolean, boolean)}
	 */
	protected JFreeChart createJFreeChart(CategoryDataset dataSet) {
		PlotOrientation po = getOrientation();
		return ChartFactory.createBarChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), dataSet, po, getConfig().getShowLegend(),
			false, false);
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.modifyPlot(model, context, chartData);

		if (getConfig().getShowPeaks()) {
			setPeakLabelGenerator(model);
		}

		CategoryPlot plot = (CategoryPlot) model.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		if (renderer instanceof BarRenderer) {
			((BarRenderer) renderer).setMaximumBarWidth(getConfig().getMaximumBarWidth());
		}

		initColors(model, chartData);
	}

	/**
	 * Hook for subclasses to initialize the colors
	 * 
	 * @param model
	 *        the {@link JFreeChart} for plot and renderer
	 * @param chartData
	 *        the universal chart-model
	 */
	protected void initColors(JFreeChart model, ChartData<CategoryDataset> chartData) {
		CategoryPlot plot = (CategoryPlot) model.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		for (int i = 0; i < chartData.getDataset().getRowCount(); i++) {
			Comparable<?> rowKey = chartData.getDataset().getRowKey(i);
			Paint color = getColor(rowKey);
			renderer.setSeriesPaint(i, color);
		}
	}

	/**
	 * Sets special renderer to the given chart.
	 * 
	 * @see Config#getShowPeaks()
	 */
	protected void setPeakLabelGenerator(JFreeChart model) {
		CategoryPlot plot = (CategoryPlot) model.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultItemLabelGenerator(newStandardCategoryItemLabelGenerator());
		setBasePositiveItemLabelPosition(renderer);
		renderer.setDefaultItemLabelsVisible(true);
	}

	private void setBasePositiveItemLabelPosition(CategoryItemRenderer renderer) {
		ItemLabelPosition position = getItemLabelPosition();
		if (position != null) {
			renderer.setDefaultPositiveItemLabelPosition(position);
		}
	}

	/**
	 * The position the label should be placed at. May be <code>null</code> which means "use
	 *         * jFreeChart default".
	 */
	protected ItemLabelPosition getItemLabelPosition() {
		return new ItemLabelPosition();
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		CategoryPlot plot = (CategoryPlot) model.getPlot();
		CategoryURLGenerator generator =
			getConfig().getURLGeneratorProvider().getCategoryURLGenerator(model, context, chartData);
		plot.getRenderer().setDefaultItemURLGenerator(generator);
	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		CategoryPlot plot = (CategoryPlot) model.getPlot();
		CategoryToolTipGenerator generator = getConfig().getTooltipGeneratorProvider().getCategoryTooltipGenerator(model, context, chartData);
		plot.getRenderer().setDefaultToolTipGenerator(generator);
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
	 * Creates a {@link StandardCategoryItemLabelGenerator} as in
	 * {@link StandardCategoryItemLabelGenerator#StandardCategoryItemLabelGenerator()} but with a
	 * number format correct for the {@link Locale} of the current user.
	 */
	public static StandardCategoryItemLabelGenerator newStandardCategoryItemLabelGenerator() {
		Locale userLocale = TLContext.getContext().getCurrentLocale();
		NumberFormat format = NumberFormat.getInstance(userLocale);
		NumberFormat percentFormat = NumberFormat.getPercentInstance(userLocale);
		String labelFormat = StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING;

		return new StandardCategoryItemLabelGenerator(labelFormat, format, percentFormat) {
			@Override
			public String generateLabel(CategoryDataset dataset, int row, int column) {
				if (NumberUtil.getIntValue(dataset.getValue(row, column)) != 0) {
					return super.generateLabel(dataset, row, column);
				} else {
					return null;
				}
			}
		};
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
	 * Factory method to create an initialized {@link BarChartBuilder}.
	 * 
	 * @return a new BarChartBuilder.
	 */
	public static BarChartBuilder newInstance() {
		return (BarChartBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(defaultConfig());
	}

}
