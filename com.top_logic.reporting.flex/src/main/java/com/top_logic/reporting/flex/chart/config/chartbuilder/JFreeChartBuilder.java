/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder;

import java.awt.GradientPaint;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.reporting.flex.chart.config.color.ColorProvider;
import com.top_logic.reporting.flex.chart.config.color.ConfiguredColorProvider;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.url.DefaultURLGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.JFreeChartURLGeneratorProvider;

/**
 * Interface for classes that creates a {@link JFreeChart} for a given {@link ChartTree}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface JFreeChartBuilder<D extends Dataset> extends ConfiguredInstance<JFreeChartBuilder.Config<D>> {

	/**
	 * Configuration options for {@link JFreeChartBuilder}.
	 */
	public interface Config<D extends Dataset> extends PolymorphicConfiguration<JFreeChartBuilder<D>> {

		/**
		 * <code>COLOR_PROVIDER</code> Attribute name for color-provider-property
		 */
		public String COLOR_PROVIDER = "color-provider";

		/**
		 * Title resource of the chart.
		 */
		public ResKey getTitleKey();

		/**
		 * see {@link #getTitleKey()}
		 */
		public void setTitleKey(ResKey label);

		/**
		 * Label resource for the x-axis of the chart.
		 */
		@InstanceFormat
		public ResKey getXAxisLabelKey();

		/**
		 * Label resource for the y-axis of the chart.
		 */
		@InstanceFormat
		public ResKey getYAxisLabelKey();

		/**
		 * Whether a legend should be added to the chart
		 */
		@BooleanDefault(true)
		public boolean getShowLegend();

		/**
		 * Whether tooltips should be rendered
		 */
		@BooleanDefault(true)
		public boolean getShowTooltips();

		/**
		 * Whether urls should be rendered to show details.
		 */
		@BooleanDefault(true)
		public boolean getShowUrls();

		/**
		 * the label-provider to use when displaying an internal column/row-key at the GUI
		 */
		@InstanceFormat
		public List<LabelProvider> getLabelProviders();

		/**
		 * The {@link Orientation} of the chart, default is {@link Orientation#VERTICAL}.
		 */
		public Orientation getOrientation();

		/**
		 * The {@link ColorProvider} that defines the colors to use in the chart.
		 */
		@Name(COLOR_PROVIDER)
		@InstanceFormat
		@InstanceDefault(ConfiguredColorProvider.class)
		public ColorProvider getColorProvider();

		/**
		 * @see #getColorProvider()
		 */
		public void setColorProvider(ColorProvider provider);

		/**
		 * The {@link DatasetBuilder} that translates the universal {@link ChartTree} model to a
		 * {@link Dataset} used for {@link JFreeChart}s.
		 */
		@InstanceFormat
		public DatasetBuilder<? extends D> getDatasetBuilder();

		/**
		 * Whether the color should be translated to a {@link GradientPaint}.
		 */
		@BooleanDefault(true)
		public boolean getUseGradientPaint();

		/**
		 * the provider that applies the url-generator to the plot
		 */
		@InstanceFormat
		@InstanceDefault(DefaultURLGeneratorProvider.class)
		public JFreeChartURLGeneratorProvider getURLGeneratorProvider();

	}

	/**
	 * Shortcut for {@link Config#getDatasetBuilder()}
	 */
	public DatasetBuilder<? extends D> getDatasetBuilder();

	/**
	 * Can be used to check if configured dataset and chartbuilder match
	 */
	public Class<? extends D> datasetType();

	/**
	 * Creates a {@link JFreeChart} for the given {@link ChartContext} and {@link ChartData}
	 */
	public JFreeChart createChart(ChartContext context, ChartData<D> chartData);
}
