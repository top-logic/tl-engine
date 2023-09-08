/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.tooltip;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.AbstractSeriesDataset;

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
public class SimpleXYToolTipGeneratorProvider implements XYToolTipGeneratorProvider,
		ConfiguredInstance<SimpleXYToolTipGeneratorProvider.Config> {

	/**
	 * Config-interface for {@link SimpleXYToolTipGeneratorProvider}
	 */
	public interface Config extends PolymorphicConfiguration<SimpleXYToolTipGeneratorProvider> {

		/**
		 * the configured tooltip-provider to use for {@link XYPlot}s
		 */
		@InstanceFormat
		@InstanceDefault(StandardXYToolTipGenerator.class)
		public XYToolTipGenerator getXYProvider();

	}

	private final Config _config;

	/**
	 * Config-constructor for {@link SimpleXYToolTipGeneratorProvider}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public SimpleXYToolTipGeneratorProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public XYToolTipGenerator getXYTooltipGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends AbstractSeriesDataset> chartData) {
		return _config.getXYProvider();
	}

}
