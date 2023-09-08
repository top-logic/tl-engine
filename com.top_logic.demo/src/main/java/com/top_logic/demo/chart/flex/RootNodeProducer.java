/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.demo.model.types.Root;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.DataContext;
import com.top_logic.reporting.flex.chart.config.datasource.MasterModelProducer;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class RootNodeProducer implements ChartDataSource<DataContext>,
		ConfiguredInstance<RootNodeProducer.Config> {

	/**
	 * Config-interface for {@link MasterModelProducer}.
	 */
	public interface Config extends PolymorphicConfiguration<RootNodeProducer> {

		@Override
		@ClassDefault(RootNodeProducer.class)
		public Class<RootNodeProducer> getImplementationClass();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link MasterModelProducer}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public RootNodeProducer(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<? extends Object> getRawData(DataContext context) {
		Root root = DemoTypesFactory.getInstance().getRootSingleton();
		return Collections.singletonList(root);

	}

}
