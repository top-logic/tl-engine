/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig;

/**
 * Factory for instantiation of {@link DataSourceConfig}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DataSourceFactory extends AbstractConfiguredInstance<DataSourceConfig> {

	/**
	 * Creates a {@link DataSourceFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DataSourceFactory(InstantiationContext context, DataSourceConfig config) {
		super(context, config);
	}

	/**
	 * Creates the configured {@link DataSource}
	 */
	public abstract DataSource createDataSource(InstantiationContext context) throws ConfigurationException;

}
