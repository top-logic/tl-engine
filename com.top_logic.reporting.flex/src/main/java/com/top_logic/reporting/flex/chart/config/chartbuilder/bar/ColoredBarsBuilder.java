/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.bar;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.base.chart.configurator.BarChartConfigurator;
import com.top_logic.base.chart.renderer.DifferentSeriesColorsBarRenderer;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * {@link BarChartBuilder} where series and rows can have different colors.
 * 
 * <p>
 * Only {@link CategoryDataset}s with {@link CategoryDataset#getRowCount() one row} are supported.
 * </p>
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ColoredBarsBuilder extends BarChartBuilder {

	/**
	 * Config-interface for {@link ColoredBarsBuilder}.
	 */
	public interface Config extends BarChartBuilder.Config {

		@Override
		@ClassDefault(ColoredBarsBuilder.class)
		public Class<? extends BarChartBuilder> getImplementationClass();

		@Override
		@BooleanDefault(false)
		public boolean getShowLegend();

		@Override
		@BooleanDefault(true)
		public boolean getShowPeaks();

		/**
		 * the {@link SeriesColorProvider} that decides per series what color to use
		 */
		@InstanceFormat
		@InstanceDefault(DefaultSeriesColorProvider.class)
		public SeriesColorProvider getSeriesColorProvider();

	}

	/**
	 * A {@link ColoredBarsBuilder} can use diferent colors for the same series. The
	 * {@link SeriesColorProvider} determines which color to use.
	 */
	public interface SeriesColorProvider {

		/**
		 * @param row
		 *        the row-key in the dataset
		 * @param col
		 *        the colum-key in the dataset
		 * @param chartData
		 *        the model containing jfree-chart-dataset and universal {@link ChartTree}
		 * @return the color to use for this series and row
		 */
		public Color getColor(Comparable<?> row, Comparable<?> col, ChartData<CategoryDataset> chartData);

	}

	/**
	 * The {@link DefaultSeriesColorProvider} has two configured colors, one for positive (in terms
	 * of good) and one for negative (in terms of bad) values. This provider declares numbers > 0 as
	 * positive and numbers <= 0 as negative.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class DefaultSeriesColorProvider implements SeriesColorProvider {

		/**
		 * Config-interface for {@link DefaultSeriesColorProvider}.
		 */
		public interface Config extends PolymorphicConfiguration<DefaultSeriesColorProvider> {

			/**
			 * the color to use for positive values
			 */
			@Format(ColorConfigFormat.class)
			@FormattedDefault("#00FF00")
			public Color getColorPositiv();

			/**
			 * the color to use for negative values
			 */
			@Format(ColorConfigFormat.class)
			@FormattedDefault("#FF0000")
			public Color getColorNegativ();

		}

		private final Config _config;

		/**
		 * Config-constructor for {@link DefaultSeriesColorProvider}
		 * 
		 * @param context
		 *        - default config-constructor
		 * @param config
		 *        - default config-constructor
		 */
		public DefaultSeriesColorProvider(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Color getColor(Comparable<?> row, Comparable<?> col, ChartData<CategoryDataset> chartData) {
			Number value = chartData.getDataset().getValue(row, col);
			if (isPositiv(value)) {
				return getColorPositiv();
			}
			else {
				return getColorNegativ();
			}
		}

		/**
		 * Shortcut for {@link Config#getColorNegativ()}
		 */
		protected Color getColorNegativ() {
			return _config.getColorNegativ();
		}

		/**
		 * Shortcut for {@link Config#getColorPositiv()}
		 */
		protected Color getColorPositiv() {
			return _config.getColorPositiv();
		}

		private boolean isPositiv(Number value) {
			if (value.doubleValue() < 0) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Config-constructor for {@link ColoredBarsBuilder}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ColoredBarsBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected void adaptChart(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.adaptChart(model, context, chartData);
		BarChartConfigurator configurator = new BarChartConfigurator(model);
		CategoryItemRenderer renderer = getRenderer(chartData);

		if (renderer != null) {
			configurator.setRenderer(renderer);
		}

		configurator.setDefaultValues();
		configurator.useGradientPaint();
		configurator.adjustRangeAxisBounds(1.0);
		configurator.setRangeAxisLabel(getYAxisLabel());
		configurator.setRangeAxisTickMarksVisible(false);
		configurator.setRangeAxisTickLabelsVisible(false);
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.modifyPlot(model, context, chartData);

	}

	@SuppressWarnings("unchecked")
	private CategoryItemRenderer getRenderer(ChartData<CategoryDataset> chartData) {
		CategoryDataset dataset = chartData.getDataset();
		List<Comparable<?>> rowKeys = dataset.getRowKeys();
		if (rowKeys.isEmpty()) {
			return null;
		}
		assert rowKeys.size() == 1;
		Comparable<?> row = CollectionUtil.getFirst(rowKeys);
		List<Comparable<?>> columnKeys = dataset.getColumnKeys();
		Color[] colors = new Color[columnKeys.size()];
		int i = 0;
		SeriesColorProvider provider = getConfig().getSeriesColorProvider();
		for (Comparable<?> col : columnKeys) {
			Color color = provider.getColor(row, col, chartData);
			colors[i++] = color;
		}
		return new DifferentSeriesColorsBarRenderer(colors);
	}
}
