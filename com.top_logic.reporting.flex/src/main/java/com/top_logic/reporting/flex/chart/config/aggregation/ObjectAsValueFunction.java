/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * {@link AbstractAggregationFunction} expecting all given objects being {@link Number}s.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ObjectAsValueFunction extends AbstractAggregationFunction {

	/**
	 * Config-interface for {@link ObjectAsValueFunction}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public interface Config extends AbstractAggregationFunction.Config {

		@Override
		@ClassDefault(ObjectAsValueFunction.class)
		public Class<? extends ObjectAsValueFunction> getImplementationClass();

		@Override
		@FormattedDefault("SUM")
		public Operation getOperation();

	}

	/**
	 * Config-constructor for {@link ObjectAsValueFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ObjectAsValueFunction(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getLabel() {
		return Resources.getInstance().getString(
			ResPrefix.legacyString(getClass().getSimpleName()).key(getConfig().getOperation().name()));
	}

	@Override
	protected Object getObjectValue(Object obj) {
		return obj;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item() {
		return TypedConfiguration.newConfigItem(Config.class);
	}
}