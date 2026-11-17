/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.manticore.h2.H2MigrationTool;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * {@link DataSourceProxy} for H2 database.
 * 
 * <p>
 * The statement cache of the H2 session is switched off (see {@link #QUERY_CACHE_SIZE_SETTING}),
 * unless the database URL sets its size explicitly. A statement reused from that cache may answer a
 * query with a result that does not reflect a change another connection has committed in between.
 * </p>
 * 
 * <p>
 * This works around the H2 bug h2database/h2database#4580
 * (https://github.com/h2database/h2database/issues/4580): a re-executed query with an IN list and
 * a row limit still returns rows another connection has deleted. It is fixed by
 * h2database/h2database#4581 (https://github.com/h2database/h2database/pull/4581). The canary test
 * {@code test.com.top_logic.basic.sql.TestH2ResultReuse#testH2StaleInListResultStillPresent()}
 * fails as soon as the used H2 version contains the fix; then the statement cache switch is
 * removed together with that test.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class H2DataSourceProxy extends ReadOnlySupportDataSource {

	/**
	 * Name of the H2 database setting for the number of statements a session keeps in its
	 * statement cache.
	 * 
	 * <p>
	 * H2 reads the setting only when the database is opened, so it is given in the database URL.
	 * </p>
	 */
	public static final String QUERY_CACHE_SIZE_SETTING = "QUERY_CACHE_SIZE";

	/**
	 * Name of the configuration option holding the database URL.
	 */
	public static final String URL_OPTION = "url";

	private static final String USER_OPTION = "user";

	private static final String PASSWORD_OPTION = "password";

	private static final String URL_PREFIX = "jdbc:h2:";

	private static final String FILE_PREFIX = "file:";

	private static final char SETTING_SEPARATOR = ';';

	/**
	 * Workaround for h2database/h2database#4580, to be removed when the canary test
	 * {@code TestH2ResultReuse#testH2StaleInListResultStillPresent()} reports the fix
	 * h2database/h2database#4581 in the used H2 version.
	 */
	private static final String NO_QUERY_CACHE = QUERY_CACHE_SIZE_SETTING + "=0";

	private static final String DEFAULT_DRIVER_NAME = "org.h2.jdbcx.JdbcDataSource";

	private static final String DATABASE_FILENAME_EXTENSION = ".mv.db";

	private static final String MIGRATION_NAME_INFO = "The name of the migrated database should extend the name of the original database.";

	private final Properties _config;

	/**
	 * Creates a new {@link H2DataSourceProxy}.
	 */
	public H2DataSourceProxy(Properties config) throws SQLException, ConfigurationException {
		super(DEFAULT_DRIVER_NAME, withoutQueryCache(config));

		_config = config;
	}

	/**
	 * A copy of the given configuration whose database URL switches off the statement cache, if
	 * the URL does not set {@link #QUERY_CACHE_SIZE_SETTING} itself.
	 */
	private static Properties withoutQueryCache(Properties config) {
		String url = config.getProperty(URL_OPTION);
		if (url == null || setsQueryCacheSize(url)) {
			return config;
		}
		Properties result = new Properties();
		result.putAll(config);
		result.setProperty(URL_OPTION, url + SETTING_SEPARATOR + NO_QUERY_CACHE);
		return result;
	}

	private static boolean setsQueryCacheSize(String url) {
		return url.toUpperCase(Locale.ROOT).contains(QUERY_CACHE_SIZE_SETTING);
	}

	@Override
	public Connection getConnection() throws SQLException {
		try {
			return super.getConnection();
		} catch (SQLException exception) {
			if (exception.getMessage().contains("Unsupported database file version")) {
				migrateDatabaseVersion();

				return super.getConnection();
			} else {
				throw exception;
			}
		}
	}

	private void migrateDatabaseVersion() {
		String versionTo = getNewDatabaseVersion();
		Path database = getDatabasePath();

		Logger.info("Try to migrate database to " + versionTo, H2DataSourceProxy.class);

		try {
			Path databaseFolder = database.getParent();

			Set<Path> databasesBeforeMigration = getAllDatabases(databaseFolder);
			migrateDatabase(database, versionTo);
			Set<Path> databasesAfterMigration = getAllDatabases(databaseFolder);

			Set<Path> databaseMigrations = CollectionUtil.difference(databasesAfterMigration, databasesBeforeMigration);

			replaceDatabaseWithMigration(database, databaseMigrations);
		} catch (Exception exception) {
			Logger.error("Database migration failed.", exception, H2DataSourceProxy.class);
		}
	}

	private void replaceDatabaseWithMigration(Path database, Set<Path> databaseMigrations) throws IOException {
		int size = databaseMigrations.size();

		if (size == 0) {
			noDatabaseMigrationFoundError(database);
		} else if (size == 1) {
			replaceDatabase(database, CollectionUtilShared.getSingleValueFromCollection(databaseMigrations));
		} else {
			multipleDatabaseMigrationsFoundError(database);
		}
	}

	private Set<Path> getAllDatabases(Path folder) throws IOException {
		try (Stream<Path> stream = Files.list(folder)) {
			return stream.filter(H2DataSourceProxy::isDatabase).collect(Collectors.toSet());
		}
	}

	/**
	 * The file of the database the configured URL opens.
	 */
	public Path getDatabasePath() {
		return Paths.get(getDatabaseName() + DATABASE_FILENAME_EXTENSION).toAbsolutePath().normalize();
	}

	private String getDatabaseName() {
		String url = _config.getProperty(URL_OPTION);
		int separatorIndex = url.indexOf(SETTING_SEPARATOR);
		String name = separatorIndex > -1 ? url.substring(URL_PREFIX.length(), separatorIndex)
			: url.substring(URL_PREFIX.length());
		if (name.startsWith(FILE_PREFIX)) {
			return name.substring(FILE_PREFIX.length());
		}
		return name;
	}

	private String getNewDatabaseVersion() {
		return "H2-2.2.224";
	}

	private void migrateDatabase(Path databasePath, String versionTo) throws Exception {
		H2MigrationTool.readDriverRecords();

		String user = getUser();
		String password = getPassword();

		new H2MigrationTool().migrateAuto(versionTo, databasePath.toString(), user, password, null,
			"COMPRESSION ZIP",
			"VARIABLE_BINARY",
			true, true);
	}

	private String getUser() {
		return _config.getProperty(USER_OPTION);
	}

	private String getPassword() {
		return _config.getProperty(PASSWORD_OPTION);
	}

	private static boolean isDatabase(Path database) {
		return database.toString().endsWith(DATABASE_FILENAME_EXTENSION);
	}

	private void noDatabaseMigrationFoundError(Path database) {
		String noDatabaseMigrationFound = "Could not locate migrated database of " + database + ". ";

		Logger.error(noDatabaseMigrationFound + MIGRATION_NAME_INFO, H2DataSourceProxy.class);
	}

	private void replaceDatabase(Path databaseTo, Path databaseFrom) throws IOException {
		Logger.info("Replace content of " + databaseFrom + " with " + databaseTo, H2DataSourceProxy.class);
		Files.copy(databaseFrom, databaseTo, StandardCopyOption.REPLACE_EXISTING);
		Files.deleteIfExists(databaseFrom);
	}

	private void multipleDatabaseMigrationsFoundError(Path database) {
		String multipleDatabaseMigrationFound = "Found multiple migrated databases of " + database + ". ";

		Logger.error(multipleDatabaseMigrationFound + MIGRATION_NAME_INFO, H2DataSourceProxy.class);
	}

}

