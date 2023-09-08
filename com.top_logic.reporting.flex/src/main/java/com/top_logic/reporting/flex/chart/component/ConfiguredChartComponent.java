/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link AbstractChartComponent} that has a {@link ChartModel} as {@link #getModel() business
 * model}.
 * 
 * <p>
 * This component is intended to work hand-in-hand with a {@link ChartConfigComponent} that does the
 * upgrade of a business model to a {@link ChartModel}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredChartComponent extends AbstractChartComponent {

	/**
	 * Creates a {@link ConfiguredChartComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected ChartModel createChartModel(Object businessModel) {
		return (ChartModel) businessModel;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || anObject instanceof ChartModel;
	}

}
