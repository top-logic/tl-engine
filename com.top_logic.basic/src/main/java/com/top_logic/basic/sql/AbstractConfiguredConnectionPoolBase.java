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
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

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
		PolymorphicConfiguration<? extends DBHelper> getSQLDialect();

		/**
		 * @see #getSQLDialect()
		 */
		void setSQLDialect(PolymorphicConfiguration<? extends DBHelper> value);
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
		 * <p>
		 * An option holding a secret, e.g. the database password, is given as
		 * {@link EncryptedDriverOption}. Its value is read from its {@link SecretSource} each time
		 * a new connection is opened:
		 * </p>
		 * 
		 * <pre>
		 * &lt;options&gt;
		 *   &lt;option name="url" value="jdbc:postgresql://db/app" /&gt;
		 *   &lt;option name="user" value="app" /&gt;
		 *   &lt;encrypted-option name="password"&gt;
		 *     &lt;value name="DB_PASSWORD" /&gt;
		 *   &lt;/encrypted-option&gt;
		 * &lt;/options&gt;
		 * </pre>
		 * 
		 * @see #getDriverClassName()
		 */
		@Name("options")
		@Key(AbstractDriverOption.NAME_ATTRIBUTE)
		@Subtypes({
			@Subtype(tag = "option", type = DriverOption.class),
			@Subtype(tag = "encrypted-option", type = EncryptedDriverOption.class),
		})
		Map<String, AbstractDriverOption> getDriverOptions();

		/**
		 * Single database driver option.
		 * 
		 * @see DataSourceConfig#getDriverOptions()
		 */
		@Abstract
		interface AbstractDriverOption extends NamedConfigMandatory {

			/**
			 * Name of the driver option.
			 */
			@Override
			public String getName();

		}

		/**
		 * {@link AbstractDriverOption} with a plain value.
		 */
		interface DriverOption extends AbstractDriverOption {

			/**
			 * Generic value of the driver option.
			 */
			@Name("value")
			String getValue();
		}

		/**
		 * {@link AbstractDriverOption} for passing a secret, e.g. a password.
		 */
		interface EncryptedDriverOption extends AbstractDriverOption {

			/**
			 * @see #getValue()
			 */
			String VALUE = "value";

			/**
			 * The source of the secret value of the driver option.
			 * 
			 * <p>
			 * Recommended is a reference to the secret by its name, so that the secret is not part
			 * of the configuration, e.g. <code>&lt;value name="DB_PASSWORD"/&gt;</code>, which the
			 * default secret store reads from the environment variable of that name holding the
			 * plain password. An attribute value is an encrypted secret, or a plain secret with
			 * the prefix <code>unencrypted:</code>.
			 * </p>
			 * 
			 * <p>
			 * The secret is read each time a new connection to the database is opened. A secret its
			 * source does not hold - e.g. a named secret that is not set - leaves the option out of
			 * the driver options with a warning in the log naming the option and the secret, so
			 * that the driver behaves as without the option; this way an
			 * optional secret, such as the password of a trust store, needs no value. A secret
			 * that is present but cannot be read, e.g. an empty secret file, fails the connection.
			 * Without a value, the option is passed with an empty value.
			 * </p>
			 */
			@Name(VALUE)
			@Format(SecretSourceFormat.class)
			PolymorphicConfiguration<? extends SecretSource> getValue();

			/**
			 * @see #getValue()
			 */
			void setValue(PolymorphicConfiguration<? extends SecretSource> value);
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
		PolymorphicConfiguration<? extends DBHelper> sqlDialectConfig = config().getSQLDialect();
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

		// After the dialect is known and checked, prepare the database for the generated SQL, e.g.
		// create the case-insensitive collation that non-binary string columns rely on
		// (PostgreSQL). This runs once per connection pool on a write connection (DDL), so every
		// table-creation path - including tests and low-level DDL that bypass the schema setup -
		// finds the required database objects. It is a no-op for dialects that need no preparation.
		PooledConnection writeConnection = this.borrowWriteConnection();
		try {
			result.prepareDatabase(writeConnection);
			writeConnection.commit();
		} finally {
			this.releaseWriteConnection(writeConnection);
		}
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
