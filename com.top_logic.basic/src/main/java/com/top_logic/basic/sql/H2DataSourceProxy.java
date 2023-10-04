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
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.manticore.h2.H2MigrationTool;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * {@link DataSourceProxy} for H2 database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class H2DataSourceProxy extends ReadOnlySupportDataSource {

	private static final String DEFAULT_DRIVER_NAME = "org.h2.jdbcx.JdbcDataSource";

	private static final String DATABASE_FILENAME_EXTENSION = ".mv.db";

	private static final String MIGRATION_NAME_INFO =
		"The name of the migrated database should extend the name of the original database. The migrated database should be the only database that extends the name of the original database.";

	private final Properties _config;

	/**
	 * Creates a new {@link H2DataSourceProxy}.
	 */
	public H2DataSourceProxy(Properties config) throws SQLException, ConfigurationException {
		super(DEFAULT_DRIVER_NAME, config);

		_config = config;
	}

	@Override
	public Connection getConnection() throws SQLException {
		try (Connection connection = super.getConnection()) {
			// Check connection.
		} catch (SQLException sqx) {
			if (sqx.getMessage().contains("Unsupported database file version")) {
				migrateDatabaseVersion();
			}
		}

		return super.getConnection();
	}

	private void migrateDatabaseVersion() {
		String versionFrom = getCurrentDatabaseVersion();
		String versionTo = getNewDatabaseVersion();
		Path databasePath = getDatabasePath();

		Logger.info("Try to migrate database from " + versionFrom + " to " + versionTo, H2DataSourceProxy.class);

		try {
			migrateDatabase(databasePath, versionFrom, versionTo);

			replaceDatabaseWithMigration(databasePath);
		} catch (Exception exception) {
			Logger.error("Database migration failed.", exception, this);
		}
	}

	private Path getDatabasePath() {
		return Paths.get(getDatabaseUrl() + DATABASE_FILENAME_EXTENSION).toAbsolutePath().normalize();
	}

	private String getDatabaseUrl() {
		String url = _config.getProperty("url");
		int separatorIndex = url.lastIndexOf(";");

		if (separatorIndex > -1) {
			return url.substring("jdbc:h2:".length(), separatorIndex);
		} else {
			return url.substring("jdbc:h2:".length());
		}
	}

	/**
	 * Since no connection to the database can be established, the current version of the database
	 * is hardcoded here.
	 */
	private String getCurrentDatabaseVersion() {
		return "H2-2.1.214";
	}

	private String getNewDatabaseVersion() {
		return "H2-2.2.224";
	}

	private void migrateDatabase(Path databasePath, String versionFrom, String versionTo) throws Exception {
		H2MigrationTool.readDriverRecords();

		String user = getUser();
		String password = getPassword();

		new H2MigrationTool().migrate(versionFrom, versionTo, databasePath.toString(), user, password, null,
			"COMPRESSION ZIP",
			"VARIABLE_BINARY",
			true, true, null);
	}

	private String getUser() {
		return _config.getProperty("user");
	}

	private String getPassword() {
		return _config.getProperty("password");
	}

	private void replaceDatabaseWithMigration(Path database) throws IOException {
		Path databaseFolder = database.getParent();

		try (Stream<Path> stream = Files.list(databaseFolder)) {
			Set<Path> databaseExtensions = getDatabaseExtensions(database, stream);

			int size = databaseExtensions.size();

			if (size == 0) {
				noDatabaseMigrationFoundError(database);
			} else if (size == 1) {
				replaceDatabase(CollectionUtilShared.getSingleValueFromCollection(databaseExtensions), database);
			} else {
				multipleDatabaseMigrationsFoundError(database);
			}
		}
	}

	private Set<Path> getDatabaseExtensions(Path database, Stream<Path> stream) {
		return stream.filter(path -> isDatabase(path) && extendsDatabase(path, database)).collect(Collectors.toSet());
	}

	private boolean isDatabase(Path database) {
		return database.toString().endsWith(DATABASE_FILENAME_EXTENSION);
	}

	private boolean extendsDatabase(Path path, Path database) {
		return path.compareTo(database) != 0 && path.toString().startsWith(database.toString());
	}

	private void noDatabaseMigrationFoundError(Path database) {
		String noDatabaseMigrationFound = "Could not locate migrated database of " + database + ". ";

		Logger.error(noDatabaseMigrationFound + MIGRATION_NAME_INFO, H2DataSourceProxy.class);
	}

	private void replaceDatabase(Path databaseFrom, Path databaseTo) throws IOException {
		Logger.info("Replace content of " + databaseFrom + " with " + databaseTo, H2DataSourceProxy.class);
		Files.copy(databaseFrom, databaseTo, StandardCopyOption.REPLACE_EXISTING);
		Files.deleteIfExists(databaseFrom);
	}

	private void multipleDatabaseMigrationsFoundError(Path database) {
		String multipleDatabaseMigrationFound = "Found multiple migrated databases of " + database + ". ";

		Logger.error(multipleDatabaseMigrationFound + MIGRATION_NAME_INFO, H2DataSourceProxy.class);
	}

}

