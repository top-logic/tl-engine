/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.reporting.flex.chart.config.model.Partition;

/**
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class RevisionDateFunction extends AttributeAggregationFunction {

	/**
	 * Config-interface for {@link RevisionDateFunction}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public interface Config extends AttributeAggregationFunction.Config {

		@Override
		@ClassDefault(RevisionDateFunction.class)
		public Class<? extends RevisionDateFunction> getImplementationClass();

	}

	/**
	 * Config-constructor for {@link RevisionDateFunction}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public RevisionDateFunction(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Number calculate(Partition parent, List<?> objects) {
		Object first = CollectionUtil.getFirst(objects);
		Revision revision = WrapperHistoryUtils.getRevision((Wrapper) first);
		if (revision.isCurrent()) {
			return new Date().getTime();
		}
		return revision.getDate();
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item() {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setOperation(Operation.SUM);
		return item;
	}

	/**
	 * Factory method to create an initialized {@link RevisionDateFunction}.
	 * 
	 * @return a new RevisionDateFunction.
	 */
	public static RevisionDateFunction instance() {
		return (RevisionDateFunction) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(item());
	}

}