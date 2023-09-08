/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.tooltip;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;

/**
 * Default tooltip-generator-provider that works with {@link CategoryPlot}, {@link XYPlot} and
 * {@link PiePlot}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SimpleCategoryToolTipGeneratorProvider implements CategoryToolTipGeneratorProvider,
		ConfiguredInstance<SimpleCategoryToolTipGeneratorProvider.Config> {

	/**
	 * Config-interface for {@link SimpleCategoryToolTipGeneratorProvider}
	 */
	public interface Config extends PolymorphicConfiguration<SimpleCategoryToolTipGeneratorProvider> {
		/**
		 * the configured tooltip-provider to use for {@link CategoryPlot}s
		 */
		@InstanceFormat
		@InstanceDefault(StandardCategoryToolTipGenerator.class)
		public CategoryToolTipGenerator getCategoryProvider();
	}

	private final Config _config;

	/**
	 * Config-constructor for {@link SimpleCategoryToolTipGeneratorProvider}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public SimpleCategoryToolTipGeneratorProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public CategoryToolTipGenerator getCategoryTooltipGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends CategoryDataset> chartData) {
		return _config.getCategoryProvider();
	}

}
