/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.util.Configs;

/**
 * {@link AbstractChartComponent} where a static chart configuration can be given as external file.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class StaticChartComponent extends AbstractChartComponent {

	/**
	 * Configuration options for {@link StaticChartComponent}
	 */
	public interface Config extends AbstractChartComponent.Config {

		/**
		 * <code>CONFIG_FILE</code> Attribute name for config-file-property
		 */
		public static final String CHART_CONFIG = "chartConfig";

		/**
		 * the name of the file with the serialized {@link ChartConfig}
		 */
		@Name(CHART_CONFIG)
		public String getChartConfigFile();

	}

	private ChartConfig _chartConfig;

	/**
	 * Creates a {@link StaticChartComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StaticChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_chartConfig = Configs.readChartConfig(config.getChartConfigFile());
	}

	@Override
	protected ChartModel createChartModel(Object businessModel) {
		return new ChartModel(_chartConfig, businessModel);
	}

}
