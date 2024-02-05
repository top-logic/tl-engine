/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.sql.DB2Helper;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.MSSQLHelper;
import com.top_logic.basic.sql.MySQL55Helper;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.PostgreSQLHelper;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;

/**
 * {@link MigrationProcessor} executing simple SQL files.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLMigrationProcessor implements MigrationProcessor, ConfiguredInstance<SQLMigrationProcessor.Config> {

	private enum DatabaseType implements ExternallyNamed {
		DB2("db2"), H2("h2"), MSSQL("mssql"), MYSQL("mysql"), MYSQL55("mysql55") {
			@Override
			DatabaseType getFallback() {
				return MYSQL;
			}
		},
		ORACLE("oracle"), ORACLE10("oracle10"), ORACLE12("oracle12"), ORACLE19("oracle19") {

			@Override
			DatabaseType getFallback() {
				return DatabaseType.ORACLE;
			}
		},
		POSTGRESQL("postgresql") {
			@Override
			DatabaseType getFallback() {
				return H2;
			}
		}

		;

		private final String _externalName;

		private DatabaseType(String externalName) {
			_externalName = externalName;
		}

		DatabaseType getFallback() {
			return null;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	/**
	 * Typed configuration interface definition for {@link SQLMigrationProcessor}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("sql")
	public interface Config extends PolymorphicConfiguration<SQLMigrationProcessor> {

		/**
		 * File name prefix of the SQL file. The actual file name has the form <i>
		 * 'fileNamePrefix'.'sqlDialect'.sql</i>, where 'fileNamePrefix' is value of
		 * {@link #getFileNamePrefix()}, and 'sqlDialect' something like 'mysql', 'oracle', or
		 * 'mssql'.
		 */
		@Mandatory
		String getFileNamePrefix();

		/**
		 * Setter for {@link #getFileNamePrefix()}.
		 */
		void setFileNamePrefix(String value);

		/**
		 * Encoding of the SQL files.
		 */
		@StringDefault(StringServices.UTF8)
		String getFileEncoding();

		/**
		 * Setter for {@link #getFileEncoding()}.
		 */
		void setFileEncoding(String value);

	}

	private Config _config;

	/**
	 * Create a {@link SQLMigrationProcessor}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SQLMigrationProcessor(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@SuppressWarnings("resource")
	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		DBHelper sqlDialect;
		try {
			sqlDialect = connection.getSQLDialect();
		} catch (SQLException ex) {
			log.error("Unabel to get SQL dialect from connection.", ex);
			return;
		}
		DatabaseType type = getDatabaseType(sqlDialect);
		if (type == null) {
			log.error("No database type found for SQLDialect " + sqlDialect);
			return;
		}
		InputStream migrationScript = null;
		String resourceName = null;
		List<String> missingResources = new ArrayList<>();
		while (type != null) {
			resourceName =
				getConfig().getFileNamePrefix() + "." + type.getExternalName() + ".sql";
			try {
				migrationScript = new DataAccessProxy(resourceName).getEntry();
				break;
			} catch (DatabaseAccessException ex) {
				// script does not exist or can not be accessed;
				type = type.getFallback();
				missingResources.add(resourceName);
			}
		}
		if (migrationScript == null) {
			log.error("No SQL scripts found at one of position: " + missingResources);
			return;
		}
		try {
			doMigration(log, connection, migrationScript);
		} finally {
			try {
				migrationScript.close();
			} catch (IOException ex) {
				log.info("Unable to close resource " + resourceName);
			}
		}

	}

	private void doMigration(Log log, PooledConnection connection, InputStream migrationScript) {
		try (Statement stmt = connection.createStatement()) {
			doMigration(log, stmt, migrationScript);
		} catch (SQLException ex) {
			log.error("Unable to create statement.", ex);
		}

	}

	private void doMigration(Log log, Statement stmt, InputStream migrationScript) {
		StringBuilder buffer = new StringBuilder();
		boolean comment = false;
		boolean multiLineComment = false;
		try (InputStreamReader in = new InputStreamReader(migrationScript, getConfig().getFileEncoding())) {
			int currentChar;
			while (true) {
				int currentLength = buffer.length();
				currentChar = in.read();
				if (currentChar == -1) {
					break;
				}
				if (currentChar == '\r') {
					// ignore \r
					continue;
				}
				if (comment) {
					if (currentChar == '\n') {
						// comment ends at end of line
						comment = false;
					}
					continue;
				}
				if (currentChar == '\n') {
					// ignore line breaks. Use blank instead.
					buffer.append(StringServices.BLANK_CHAR);
					continue;
				}
				if (multiLineComment) {
					if (currentChar == '/' && buffer.charAt(currentLength - 1) == '*') {
						buffer.setLength(currentLength - 1);
						multiLineComment = false;
					} else {
						buffer.setCharAt(currentLength - 1, (char) currentChar);
					}
					continue;
				}
				if (currentChar == '-' && currentLength > 0 && buffer.charAt(currentLength - 1) == '-') {
					// Line comment;
					buffer.setLength(currentLength - 1);
					comment = true;
					continue;
				}
				if (currentChar == '*' && currentLength > 0 && buffer.charAt(currentLength - 1) == '/') {
					// multiline comment.
					multiLineComment = true;
					continue;
				}
				if (currentChar == ';') {
					// Do not add ',' to statement because this is invalid, e.g. in Oracle.
					String sql = buffer.toString();
					try {
						int cnt = stmt.executeUpdate(sql);
						log.info("Updated " + cnt + " rows: " + sql);
					} catch (SQLException ex) {
						log.error("Unable to execute statement '" + sql + "'.", ex);
						return;
					}
					buffer.setLength(0);
				} else {
					buffer.append((char) currentChar);
				}
			}
		} catch (UnsupportedEncodingException ex) {
			log.error("Invalid configured encoding: " + getConfig().getFileEncoding(), ex);
		} catch (IOException ex) {
			log.error("Error reading : " + getConfig().getFileEncoding(), ex);
		}
	}

	private DatabaseType getDatabaseType(DBHelper sqlDialect) {
		DatabaseType type;
		if (sqlDialect instanceof DB2Helper) {
			type = DatabaseType.DB2;
		} else if (sqlDialect instanceof H2Helper) {
			type = DatabaseType.H2;
		} else if (sqlDialect instanceof MSSQLHelper) {
			type = DatabaseType.MSSQL;
		} else if (sqlDialect instanceof MySQLHelper) {
			type = DatabaseType.MYSQL;
			if (sqlDialect instanceof MySQL55Helper) {
				type = DatabaseType.MYSQL55;
			}
		} else if (sqlDialect instanceof OracleHelper) {
			type = DatabaseType.ORACLE;
//			if (sqlDialect instanceof Oracle10Helper) {
//				type = DatabaseType.ORACLE10;
//			} else if (sqlDialect instanceof Oracle12Helper) {
//				type = DatabaseType.ORACLE12;
//			}
		} else if (sqlDialect instanceof PostgreSQLHelper) {
			type = DatabaseType.POSTGRESQL;
		} else {
			type = null;
		}
		return type;
	}

}
