/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Registry of configured {@link AbstractConnectionPool}s of the application.
 * 
 * @see ConnectionPoolRegistry.Config
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	ThreadContextManager.Module.class,
})
public class ConnectionPoolRegistry extends ManagedClass {

	/**
	 * The name of the default pool.
	 * 
	 * @see #getDefaultConnectionPool()
	 */
	public static final String DEFAULT_POOL_NAME = "default";

	/**
	 * Configuration settings for {@link ConnectionPoolRegistry}.
	 * 
	 * @see #getPools()
	 */
	public interface Config extends ServiceConfiguration<ConnectionPoolRegistry> {

		/**
		 * Configuration for all managed pools.
		 */
		@Name("pools")
		@Key(PoolConfig.NAME_ATTRIBUTE)
		@Subtypes({
			@Subtype(tag = "pool", type = AbstractConnectionPool.Config.class),
			@Subtype(tag = "alias", type = PoolAlias.class),
		})
		Map<String, PoolConfig> getPools();

	}

	/**
	 * Base configuration for all pools.
	 */
	@Abstract
	public interface PoolConfig extends NamedConfigMandatory {

		/** Configuration name of the value of {@link #isDryRun()}. */
		String DRY_RUN_NAME = "dry-run";

		/**
		 * The name of the pool.
		 * 
		 * @see ConnectionPoolRegistry#getConnectionPool(String)
		 * @see ConnectionPoolRegistry#DEFAULT_POOL_NAME
		 */
		@Override
		public String getName();

		/**
		 * Whether this pool setting is used at all.
		 * 
		 * <p>
		 * Can be used to prevent pools from being installed. A disabled setting may be used in a
		 * {@link PoolAlias} to enable them under a different name.
		 * </p>
		 */
		@Name("enabled")
		@BooleanDefault(true)
		boolean isEnabled();

		/**
		 * @see #isEnabled()
		 */
		void setEnabled(boolean value);

		/**
		 * Whether this pool should be initialized directly during startup.
		 * 
		 * <p>
		 * If this setting is given, application startup fails, if the datasource for this pool is
		 * not available at application startup time.
		 * </p>
		 */
		@Name("init-during-startup")
		boolean getInitDuringStartup();

		/**
		 * @see #getInitDuringStartup()
		 */
		void setInitDuringStartup(boolean value);

		/**
		 * @see ConnectionPool#isDryRun()
		 */
		@Name(DRY_RUN_NAME)
		boolean isDryRun();

	}

	/**
	 * Installs a pool with settings copied from another pool.
	 */
	public interface PoolAlias extends PoolConfig {

		/**
		 * The name of the pool from where settings are copied.
		 * 
		 * @see Config#getPools()
		 */
		@Name("settings-from")
		@Mandatory
		String getSettingsFrom();

		/**
		 * @see #getSettingsFrom()
		 */
		void setSettingsFrom(String value);
	}

	private HashMap<String, ConnectionPool> instanceByName;
	private ConnectionPool defaultInstance;
	
	/**
	 * Creates a {@link ConnectionPoolRegistry} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConnectionPoolRegistry(InstantiationContext context, Config config) {
		super(context, config);
		
		HashMap<String, ConnectionPool> result = new HashMap<> ();
		for (PoolConfig poolConfig : config.getPools().values()) {
			String poolName = poolConfig.getName();
			if (!poolConfig.isEnabled()) {
				continue;
			}
			
			boolean init = poolConfig.getInitDuringStartup();

			AbstractConnectionPool.Config poolSettings;
			if (poolConfig instanceof PoolAlias) {
				String alias = ((PoolAlias) poolConfig).getSettingsFrom();
				PoolConfig aliasSettings = config.getPools().get(alias);
				if (aliasSettings == null) {
					context.error("Invalid pool alias config, no such pool: " + alias);
					continue;
				}

				if (!(aliasSettings instanceof AbstractConnectionPool.Config)) {
					context.error("Pool alias '" + poolName + "' must refer to concrete pool configuration.");
					continue;
				}

				// Note: Do not check again the enabled flag, to allow using settings from disabled
				// pools with alias name as enabled pools.
				poolSettings = (AbstractConnectionPool.Config) aliasSettings;
			} else {
				poolSettings = (AbstractConnectionPool.Config) poolConfig;
			}

			ConnectionPool configuredPool = initPool(context, poolSettings, init);
			
			// Note: Even initPool() might return null, if initialization
			// fails.
			if (configuredPool != null) {
				result.put(poolName, configuredPool);
			}
		}
		
		instanceByName = result;
		defaultInstance = result.get(DEFAULT_POOL_NAME);
		
		if (defaultInstance == null) {
			Logger.warn("No default connection pool configured.", ConnectionPoolRegistry.class);
		}
	}

	/**
	 * The default {@link ConnectionPool} of the application.
	 * 
	 * <p>
	 * This pool is used, whenever no explicit DB configuration is given.
	 * </p>
	 * 
	 * <p>
	 * Equivalent to {@link #getConnectionPool(String)} with
	 * {@link #DEFAULT_POOL_NAME} as argument.
	 * </p>
	 */
	public static ConnectionPool getDefaultConnectionPool() {
		return Module.INSTANCE.getImplementationInstance().defaultInstance;
	}

	/**
	 * Lookup a named {@link ConnectionPool}.
	 * 
	 * @param poolName
	 *        The name of the pool
	 * @return The configured {@link ConnectionPool} with the given name.
	 * 
	 * @throws IllegalArgumentException
	 *         If a pool with the given name is not configured.
	 */
	public static ConnectionPool getConnectionPool(String poolName) throws IllegalArgumentException {
		ConnectionPool result = Module.INSTANCE.getImplementationInstance().instanceByName.get(poolName);
		
		if (result == null) {			
			throw new IllegalArgumentException("Connection pool '" + poolName + "' not configured.");
		}
		return result;
	}

	private static ConnectionPool initPool(InstantiationContext context, AbstractConnectionPool.Config poolConfig,
			boolean init) {
		AbstractConnectionPool newPool = null;
		try {
			
			// Attempt to standard initialization
			try {
				newPool = context.getInstance(poolConfig);
				if (newPool == null) {
					// Error during instantiation.
					return null;
				}
				
				if (init) {
					newPool.initDBHelper();

					PooledConnection readConnection = newPool.borrowReadConnection();
					try {
						logVersions(poolConfig, readConnection.getMetaData());
					} finally {
						newPool.releaseReadConnection(readConnection);
					}
				}
			} catch (SQLException ex) {
				context.error("Setup database connection failed for pool configuration '" + poolConfig + "'.", ex);
			}
		} catch (Throwable throwable) {
			context.error("Unable to create connection pool for configuration '" + poolConfig + "'", throwable);
		}
		return newPool;
	}

	/**
	 * {@link Logger#info(String, Object)} the Version of the driver and the database (usually on startup).
	 * 
	 * @poolSection the name onf the configuration the ConnectionPool wwas configured for.
	 */
	static void logVersions(PoolConfig poolConfig, DatabaseMetaData meta)
			throws SQLException {
		Logger.info("Setup Connection pool: '" + poolConfig.getName() + "'", ConnectionPoolRegistry.class);
        String driver  = meta.getDriverName();
        String dVers   = meta.getDriverVersion();
        Logger.info("Driver: " + driver + ' ' + dVers, ConnectionPoolRegistry.class);

		String url = meta.getURL();
		if (url == null) {
			url = "No URL available";
		}
		Logger.info("Connection: " + url, ConnectionPoolRegistry.class);

        String dbName  = meta.getDatabaseProductName();
        String version = meta.getDatabaseProductVersion();
        Logger.info("Database: " + dbName + ' ' +  version, ConnectionPoolRegistry.class);
    }

	@Override
	protected void shutDown() {
		for (Iterator<ConnectionPool> it = instanceByName.values().iterator(); it.hasNext(); ) {
			ConnectionPool pool = it.next();
			pool.close();
		}
		defaultInstance = null;
		instanceByName = null;
		super.shutDown();
	}
	
	/**
	 * {@link ConnectionPoolRegistry} module.
	 */
	public static final class Module extends TypedRuntimeModule<ConnectionPoolRegistry> {
		
		/**
		 * Singleton {@link Module} reference.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ConnectionPoolRegistry> getImplementation() {
			return ConnectionPoolRegistry.class;
		}
	}

}
