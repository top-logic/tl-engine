/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.spider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.top_logic.base.chart.plot.ExtendedSpiderWebPlot;
import com.top_logic.base.chart.plot.ExtendedSpiderWebPlot.SpiderChartLabelGenerator;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.spider.tooltip.DefaultSpiderChartTooltipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.chartbuilder.spider.tooltip.SpiderToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.color.HexEncodedPaint;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.url.JFreeChartURLGenerator;

/**
 * {@link AbstractJFreeChartBuilder} that builds spider-charts.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class SpiderChartBuilder extends AbstractJFreeChartBuilder<CategoryDataset> {

	/**
	 * Config-interface for {@link SpiderChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<CategoryDataset> {

		/**
		 * A spider-chart in the TL-flavor has some series used as background-colors without further
		 * information. No URLs or tooltips should be rendered for these special-series. The
		 * special-series with their values and colors can be configured.
		 * 
		 * @return the series used as background-color
		 */
		@EntryTag("serie")
		public List<SpecialSeriesConfig> getSpecialSeries();

		/**
		 * A default-series can be rendered as area or as outline. The colors and type (outline or
		 * area) can be configured.
		 * 
		 * @return the series used to display values
		 */
		@EntryTag("serie")
		public List<DefaultSeriesConfig> getDefaultSeries();

		@Override
		@InstanceFormat
		@InstanceDefault(CategoryDatasetBuilder.class)
		public DatasetBuilder<? extends CategoryDataset> getDatasetBuilder();

		/**
		 * Factory for a {@link CategoryToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultSpiderChartTooltipGeneratorProvider.class)
		public SpiderToolTipGeneratorProvider getTooltipGeneratorProvider();
	}

	/**
	 * A special-serie is used as background and has no further information like URLs or tooltips.
	 */
	public interface SpecialSeriesConfig extends ConfigurationItem {

		/**
		 * the color for the special-series
		 */
		@Format(HexEncodedPaint.class)
		public Paint getColor();

		/**
		 * the alpha-level of the color for the special-serie
		 */
		@FloatDefault(1f)
		public float getAlpha();

		/**
		 * a unique name for the special-series, only for intern use
		 */
		public String getName();

		/**
		 * the value to define the area used as background-color for this special-serie
		 */
		public double getValue();
	}

	/**
	 * A default-serie is a normal serie used to visualize values. The color and appearance can be
	 * configured.
	 */
	public interface DefaultSeriesConfig extends ConfigurationItem {

		/**
		 * the color for the default-serie
		 */
		@Format(HexEncodedPaint.class)
		public Paint getColor();

		/**
		 * the alpha-level of the color for the default-serie
		 */
		@FloatDefault(1f)
		public float getAlpha();

		/**
		 * true, to dras an outline only, false to draw an area
		 */
		@BooleanDefault(false)
		public boolean getIsOutline();
	}

	/**
	 * Creates a {@link SpiderChartBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SpiderChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		SpiderWebPlot plot = (SpiderWebPlot) model.getPlot();
		plot.setURLGenerator(new SpiderURLGenerator(context, this, chartData.getModel()));
	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		SpiderWebPlot plot = (SpiderWebPlot) model.getPlot();
		CategoryToolTipGenerator generator = getConfig().getTooltipGeneratorProvider().getSpiderTooltipGenerator(model, context, chartData, getConfig());
		plot.setToolTipGenerator(generator);
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<CategoryDataset> chartData) {
		CategoryDataset dataset = chartData.getDataset();
		setLabelProvider(getLabelProvider(0), dataset.getColumnKeys());
		setLabelProvider(getLabelProvider(1), dataset.getRowKeys());
		JFreeChart result = createSpiderChart(chartData.getDataset());
		return result;
	}

	private JFreeChart createSpiderChart(CategoryDataset dataSet) {
		CategoryDataset newDataSet = adaptSpecialSeries(dataSet);
		return createSpiderChart(getTitle(), newDataSet, getConfig().getShowLegend(), getConfig().getSpecialSeries().size());
	}

	private JFreeChart createSpiderChart(String title, CategoryDataset dataset, boolean showLegend, int ignoreSeries) {

		Set<Integer> outlines = getOutlineSet();
		SpiderWebPlot plot = new ExtendedSpiderWebPlot((DefaultCategoryDataset) dataset, ignoreSeries, outlines);
		plot.setLabelGenerator(new SpiderChartLabelGenerator());

		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
		ChartFactory.getChartTheme().apply(chart);
		return chart;
	}

	private Set<Integer> getOutlineSet() {
		Set<Integer> result = new HashSet<>();
		int i = getSpecialSeries().size();
		for (DefaultSeriesConfig dsc : getDefaultSeries()) {
			if (dsc.getIsOutline()) {
				result.add(i);
			}
			i++;
		}
		return result;
	}

	@Override
	public Class<CategoryDataset> datasetType() {
		return CategoryDataset.class;
	}

	@Override
	protected void modifyPlot(JFreeChart model, ChartContext context, ChartData<CategoryDataset> chartData) {
		super.modifyPlot(model, context, chartData);
		SpiderWebPlot plot = (SpiderWebPlot) model.getPlot();
		int size = getSpecialSeries().size();
		for (int i = 0; i < size; i++) {
			SpecialSeriesConfig ssc = getSpecialSeries().get(i);
			Paint col = getPaint(ssc.getColor(), ssc.getAlpha());
			plot.setSeriesPaint(i, col);
		}
		int i = size;
		for (DefaultSeriesConfig dsc : getDefaultSeries()) {
			Paint color = getPaint(dsc.getColor(), dsc.getAlpha());
			plot.setSeriesPaint(i, color);
			if (dsc.getIsOutline()) {
				plot.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
				plot.setSeriesOutlinePaint(i, color);
			}
			i++;
		}
	}

	private Paint getPaint(Paint paint, float alphaValue) {
		if (paint instanceof Color) {
			return getColor((Color) paint, alphaValue);
		} else if (paint instanceof GradientPaint) {
			GradientPaint gradientPaint = (GradientPaint) paint;
			Color color1 = getColor(gradientPaint.getColor1(), alphaValue);
			Color color2 = getColor(gradientPaint.getColor2(), alphaValue);
			return new GradientPaint(gradientPaint.getPoint1(), color1, gradientPaint.getPoint2(), color2);
		}
		return paint;
	}

	private Color getColor(Color color, float alphaValue) {
		int alpha = (int) (alphaValue * 255 + 0.5);
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	List<SpecialSeriesConfig> getSpecialSeries() {
		return getConfig().getSpecialSeries();
	}

	List<DefaultSeriesConfig> getDefaultSeries() {
		return getConfig().getDefaultSeries();
	}

	private CategoryDataset adaptSpecialSeries(CategoryDataset dataSet) {
		DefaultCategoryDataset result = new DefaultCategoryDataset();
		for (Object c : dataSet.getColumnKeys()) {
			Comparable<?> col = (Comparable<?>) c;
			for (SpecialSeriesConfig ssc : getSpecialSeries()) {
				UniqueName row = new UniqueName(ssc.getName());
				result.addValue(ssc.getValue(), row, col);
			}
			for (Object r : dataSet.getRowKeys()) {
				Comparable<?> row = (Comparable<?>) r;
				result.addValue(dataSet.getValue(row, col), row, col);
			}
		}
		return result;
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
	 * URL-generator for a spider-chart that ignores the special-series
	 */
	public static class SpiderURLGenerator extends JFreeChartURLGenerator {

		private int _size;

		/**
		 * Creates a new {@link SpiderURLGenerator}
		 */
		public SpiderURLGenerator(ChartContext context, SpiderChartBuilder builder, ChartTree tree) {
			super(context, tree);
			_size = builder.getSpecialSeries().size();
		}

		@Override
		public String generateURL(CategoryDataset dataset, int series, int category) {
			int ser = series - _size;
			if (ser < 0) {
				return null;
			}
			return super.generateURL(dataset, series, category);
		}

	}

}
