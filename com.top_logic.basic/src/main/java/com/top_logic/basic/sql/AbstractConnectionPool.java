/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Base class for {@link ConnectionPool}s that can be created by the {@link ConnectionPoolRegistry}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConnectionPool implements ConnectionPool,
		ConfiguredInstance<AbstractConnectionPool.Config> {

	/**
	 * Configuration options for {@link AbstractConnectionPool}.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractConnectionPool>, ConnectionPoolRegistry.PoolConfig {

		@Override
		@ClassDefault(CommonConfiguredConnectionPool.class)
		public Class<? extends AbstractConnectionPool> getImplementationClass();

	}

	private final Config _config;

	/**
	 * Creates a {@link AbstractConnectionPool} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractConnectionPool(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public String getName() {
		return getConfig().getName();
	}

	@Override
	public final Config getConfig() {
		return _config;
	}

	/**
	 * Initializes the SQL dialect of this pool.
	 * 
	 * <p>
	 * Must only be called during the setup process of the connection pool.
	 * </p>
	 */
	protected abstract void initDBHelper() throws SQLException;

	@Override
	public boolean isDryRun() {
		return getConfig().isDryRun();
	}

}
