/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;

/**
 * {@link AbstractConnectionPool} with underlying {@link DataSource}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConfiguredConnectionPoolBase extends AbstractConnectionPool {

	/**
	 * Configuration options for an {@link AbstractConfiguredConnectionPool}.
	 */
	public interface Config extends AbstractConnectionPool.Config {

		/**
		 * Settings for the underlying {@link DataSource}.
		 */
		@Name("data-source")
		@ItemDefault
		@ImplementationClassDefault(DefaultDataSourceFactory.class)
		DataSourceConfig getDataSource();

		/**
		 * The SQL dialect to use.
		 * 
		 * <p>
		 * If not given, it is guessed from {@link DatabaseMetaData}.
		 * </p>
		 */
		@Name("sql-dialect")
		PolymorphicConfiguration<DBHelper> getSQLDialect();

		/**
		 * @see #getSQLDialect()
		 */
		void setSQLDialect(PolymorphicConfiguration<DBHelper> value);
	}

	/**
	 * Settings for the pool {@link DataSource}.
	 */
	public interface DataSourceConfig extends PolymorphicConfiguration<DataSourceFactory> {

		/**
		 * The name under which the datasource is looked up from JNDI service.
		 */
		@Name("jndi-name")
		String getJndiName();

		/**
		 * @see #getJndiName()
		 */
		void setJndiName(String value);

		/**
		 * The driver class to create for accessing the database.
		 * 
		 * <p>
		 * Only relevant for non-JNDI datasources.
		 * </p>
		 */
		@Name("driver-class")
		String getDriverClassName();

		/**
		 * @see #getDriverClassName()
		 */
		void setDriverClassName(String value);

		/**
		 * Generic options passed to the database driver.
		 * 
		 * <p>
		 * Only relevant for non-JNDI datasources.
		 * </p>
		 * 
		 * @see #getDriverClassName()
		 */
		@Name("options")
		@Key(DriverOption.NAME_ATTRIBUTE)
		@Subtypes({
			@Subtype(tag = "option", type = DriverOption.class),
			@Subtype(tag = "encrypted-option", type = EncryptedDriverOption.class),
		})
		Map<String, DriverOption> getDriverOptions();

		/**
		 * Single database driver option.
		 * 
		 * @see DataSourceConfig#getDriverOptions()
		 */
		interface DriverOption extends NamedConfigMandatory {

			/**
			 * Name of the driver option.
			 */
			@Override
			public String getName();

			/**
			 * Generic value of the driver option.
			 */
			@Name("value")
			String getValue();
		}

		/**
		 * {@link DriverOption} for passing protected password values.
		 */
		interface EncryptedDriverOption extends DriverOption {
			@Encrypted
			@Override
			public String getValue();
		}
	}

	/**
	 * The {@link DataSource} to take {@link Connection} implementations from.
	 */
	private DataSource ds;

	/**
	 * @see ConnectionPool#getSQLDialect()
	 */
	private DBHelper dbHelper;

	/**
	 * Creates a {@link AbstractConfiguredConnectionPoolBase} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractConfiguredConnectionPoolBase(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);

		this.ds = context.getInstance(config.getDataSource()).createDataSource(context);
	}

	AbstractConfiguredConnectionPoolBase(DataSource aDs, DBHelper sqlDialect, Config config) {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		if (aDs == null) {
			throw new IllegalArgumentException("Data source must be given.");
		}

		this.ds = aDs;
		this.dbHelper = sqlDialect;
	}

	@Override
	public DataSource getDataSource() {
		return ds;
	}

	@Override
	public DBHelper getSQLDialect() throws SQLException {
		if (dbHelper == null) {
			initDBHelper();
		}
		return dbHelper;
	}

	@Override
	protected void initDBHelper() throws SQLException {
		dbHelper = null;

		DBHelper result;
		PolymorphicConfiguration<DBHelper> sqlDialectConfig = config().getSQLDialect();
		if (sqlDialectConfig != null) {
			result =
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(sqlDialectConfig);
		} else {
			result = null;
		}

		if (result == null || result.getConfig().getAdjustFromConnection()) {
			PooledConnection pc = this.borrowReadConnection();
			try {
				if (result == null) {
					result = DBHelper.getDBHelper(pc);
				}
				if (result.getConfig().getAdjustFromConnection()) {
					result = result.init(pc);
				}
				result.check(pc);
			} finally {
				this.releaseReadConnection(pc);
			}
		}

		dbHelper = result;
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public void close() {
		ds = null;
		dbHelper = null;
	}

}
