/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.matrix;

import java.awt.Paint;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.bar.BarChartBuilder;
import com.top_logic.reporting.flex.chart.config.color.HexEncodedPaint;

/**
 * A matrix-chart has named elements on X- and Y-Axis. Values are visualized at the intersections.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class MatrixChartBuilder extends BarChartBuilder {

	/**
	 * Config-interface for {@link MatrixChartBuilder}.
	 */
	public interface Config extends BarChartBuilder.Config {

		/**
		 * the {@link TemplateRenderer} for the matrix-chart
		 */
		@ImplementationClassDefault(RedYellowGreenTemplateRenderer.class)
		@ItemDefault
		PolymorphicConfiguration<? extends TemplateRenderer> getTemplateRenderer();

		@Override
		@BooleanDefault(false)
		public boolean getShowPeaks();

		/**
		 * Gets the {@link Paint} for the domain grid line.
		 */
		@Format(HexEncodedPaint.class)
		Paint getDomainGridlinePaint();

		/**
		 * Gets the {@link Paint} for the range grid line.
		 */
		@Format(HexEncodedPaint.class)
		Paint getRangeGridlinePaint();
	}

	/**
	 * Creates a {@link MatrixChartBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MatrixChartBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public Class<CategoryDataset> datasetType() {
		return CategoryDataset.class;
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	@Override
	public int getMinDimensions() {
		return 1;
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<CategoryDataset> chartData) {
		CategoryDataset dataset = chartData.getDataset();

		setLabelProvider(getLabelProvider(0), dataset.getColumnKeys());
		setLabelProvider(getLabelProvider(1), dataset.getRowKeys());

		String[] rowLabels = getLabels(dataset.getRowKeys());

		SymbolAxis rangeAxis = new SymbolAxis(getYAxisLabel(), rowLabels);
		CategoryAxis categoryAxis = new CategoryAxis(getXAxisLabel());

		rangeAxis.setGridBandsVisible(true);

		TemplateRenderer renderer = createInstance(config().getTemplateRenderer());
		renderer.setDefaultItemLabelsVisible(false);

		CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, rangeAxis, renderer);
		JFreeChart chart = newChart(plot);

		/* Set values to plot *after* creation of chart, because default theme is applied which may
		 * override values in plot. */
		initPlot(plot);

		return chart;
	}

	private <T> T createInstance(PolymorphicConfiguration<T> config) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private void initPlot(CategoryPlot plot) {
		plot.setRangeGridlinesVisible(true);
		plot.setDomainGridlinesVisible(true);
		Paint domainGridlinePaint = config().getDomainGridlinePaint();
		if (domainGridlinePaint != null) {
			plot.setDomainGridlinePaint(domainGridlinePaint);
		}
		Paint rangeGridlinePaint = config().getRangeGridlinePaint();
		if (rangeGridlinePaint != null) {
			plot.setRangeGridlinePaint(rangeGridlinePaint);
		}
	}

	private JFreeChart newChart(CategoryPlot plot) {
		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		ChartFactory.getChartTheme().apply(chart);

		return chart;
	}

	private String[] getLabels(List<?> keys) {
		String[] result = new String[keys.size()];
		int i = 0;
		for (Object obj : keys) {
			result[i++] = String.valueOf(obj);
		}
		return result;
	}

	/**
	 * Default-{@link TemplateRenderer} where left is red and right is green.
	 */
	public static class RedYellowGreenTemplateRenderer extends TemplateRenderer {

		/**
		 * Creates a new {@link RedYellowGreenTemplateRenderer}.
		 */
		public RedYellowGreenTemplateRenderer() {
			super(MatrixChartInfo.RED_YELLOW_GREEN);

			this.setShapeGradientValue(40);
		}
	}

	/**
	 * Default-{@link TemplateRenderer} where left is green and right is red.
	 */
	public static class GreenYellowRedTemplateRenderer extends TemplateRenderer {

		/**
		 * Creates a new {@link GreenYellowRedTemplateRenderer}.
		 */
		public GreenYellowRedTemplateRenderer() {
			super(MatrixChartInfo.GREEN_YELLOW_RED);

			this.setShapeGradientValue(40);
		}
	}

}
