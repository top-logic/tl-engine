/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.reporting.flex.chart.config.model.Partition;

/**
 * A {@link CountFunction} is a function that counts the objects in a given collection. Either the
 * size of the list is returned or if an attribut is configured, the number of non-null-values for
 * the attribute is counted.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class CountFunction extends AttributeAggregationFunction {

	/**
	 * Config-interface for {@link CountFunction}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	@DisplayInherited(DisplayStrategy.IGNORE)
	@TagName("count")
	public interface Config extends AttributeAggregationFunction.Config {

		@Override
		@ClassDefault(CountFunction.class)
		public Class<? extends CountFunction> getImplementationClass();

	}

	/**
	 * Config-constructor for {@link CountFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public CountFunction(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Number calculate(Partition parent, List<?> objects) {

		if (CollectionUtil.isEmptyOrNull(objects)) {
			return 0d;
		}

		if (StringServices.isEmpty(getAttributeName())) {
			return objects.size();
		}
		else {
			return super.calculate(parent, objects);
		}
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