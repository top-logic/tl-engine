/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.load;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.Oracle12Helper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.migration.DumpReader;
import com.top_logic.knowledge.service.db2.migration.sql.InsertWriter;
import com.top_logic.knowledge.service.db2.migration.sql.SQLDumper;
import com.top_logic.knowledge.service.db2.migration.sql.SQLLoaderInsertWriter;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.knowledge.service.migration.DataMigration;
import com.top_logic.knowledge.service.migration.MigrationInfo;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.Version;
import com.top_logic.knowledge.service.migration.VersionDescriptor;
import com.top_logic.knowledge.service.migration.dump.KBDump;
import com.top_logic.util.Zipper;
import com.top_logic.util.error.TopLogicException;

/**
 * A dump of the data of an application (as written by {@link KBDump}) prepared for loading it into
 * the database of this application.
 *
 * <p>
 * {@link #open(Log, BinaryData)} reads the data version of the dump and builds the migration that
 * brings the data to the version of this application. The dump is then converted into a script
 * loading it into an empty database:
 * </p>
 * <ul>
 * <li>{@link #createSqlScript(Log)}: an SQL script of insert statements in the SQL dialect of the
 * database of this application,</li>
 * <li>{@link #createLoaderScript(Log)}: control files of the Oracle SQL*Loader, packed as ZIP
 * archive, e.g. for loading a large dump into an Oracle database.</li>
 * </ul>
 *
 * <p>
 * {@link #replaceDatabase(Log, BinaryData)} replaces the whole content of the database of the
 * running application by the data of such an SQL script.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBLoad {

	/**
	 * File extension of a gzip-compressed SQL script created by {@link #createSqlScript(Log)}.
	 */
	public static final String SQL_EXT = ".sql.gz";

	/**
	 * Suffix of the base name of the archive created by {@link #createLoaderScript(Log)}.
	 */
	public static final String LOADER_SUFFIX = "-loader";

	/**
	 * File extension of the archive created by {@link #createLoaderScript(Log)}.
	 */
	public static final String ZIP_EXT = ".zip";

	/**
	 * Content type of a gzip-compressed SQL script.
	 */
	public static final String GZIP_CONTENT_TYPE = "application/gzip";

	/**
	 * Content type of a ZIP archive.
	 */
	public static final String ZIP_CONTENT_TYPE = "application/zip";

	private final BinaryData _dump;

	private final VersionDescriptor _dataVersion;

	private final DataMigration _migration;

	private KBLoad(BinaryData dump, VersionDescriptor dataVersion, DataMigration migration) {
		_dump = dump;
		_dataVersion = dataVersion;
		_migration = migration;
	}

	/**
	 * Reads the data version of the given dump and builds the migration that brings its data to the
	 * version of this application.
	 *
	 * @param log
	 *        Receives the messages of building the migration.
	 * @param dump
	 *        The dump, as written by {@link KBDump}, optionally gzip-compressed.
	 * @return The dump prepared for loading.
	 *
	 * @throws TopLogicException
	 *         If the dump cannot be read, its data is newer than this application, or it requires
	 *         an SQL migration, which cannot be applied while loading a dump.
	 */
	public static KBLoad open(Log log, BinaryData dump) {
		try (InputStream in = input(dump)) {
			try (DumpReader reader = new DumpReader(null, in)) {
				VersionDescriptor dataVersion = reader.getVersion();
				MigrationInfo migrationInfo = dataVersion == null ? MigrationInfo.NO_MIGRATION
					: MigrationUtil.buildMigration(log, dataVersion);
				if (migrationInfo.isDownGrade()) {
					throw new TopLogicException(I18NConstants.ERROR_NO_DOWNGRADE_POSSIBLE);
				}

				DataMigration migration = new DataMigration(migrationInfo);
				migration.build(log);
				if (!migration.getProcessors().isEmpty()) {
					throw new TopLogicException(I18NConstants.ERROR_SQL_PREPROCESSING_REQUIRED);
				}
				return new KBLoad(dump, dataVersion, migration);
			}
		} catch (ConfigurationException | XMLStreamException | IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_EXTRACTING_DATA_VERSION__MESSAGE.fill(ex.getMessage()),
				ex);
		}
	}

	/**
	 * The dump this load reads.
	 */
	public BinaryData getDump() {
		return _dump;
	}

	/**
	 * The versions of the modules the data of the dump was written with.
	 *
	 * @return The module versions, empty if the dump does not state its data version.
	 */
	public Collection<Version> getDataVersions() {
		if (_dataVersion == null) {
			return Collections.emptyList();
		}
		return _dataVersion.getModuleVersions().values();
	}

	/**
	 * Whether the dump states the versions of the modules its data was written with.
	 */
	public boolean hasDataVersion() {
		return _dataVersion != null;
	}

	/**
	 * The versions of the modules the data is migrated to while loading.
	 *
	 * @return The module versions reached by a migration, empty if no migration is necessary.
	 */
	public Collection<Version> getMigratedVersions() {
		if (_dataVersion == null) {
			return Collections.emptyList();
		}
		return _migration.getNewVersion().values();
	}

	/**
	 * The given module versions as text, one line <code>module: version</code> per module.
	 */
	public static String toText(Collection<Version> versions) {
		StringBuilder result = new StringBuilder();
		for (Version moduleVersion : versions) {
			result.append(moduleVersion.getModule()).append(": ").append(moduleVersion.getName()).append('\n');
		}
		return result.toString();
	}

	/**
	 * The name of the dump without its extensions, the base name of the scripts created from it.
	 */
	public String baseName() {
		return removeExt(removeExt(_dump.getName(), KBDump.GZ_EXT), KBDump.DUMP_EXT);
	}

	private static String removeExt(String name, String ext) {
		return name.endsWith(ext) ? name.substring(0, name.length() - ext.length()) : name;
	}

	/**
	 * Converts the dump into a gzip-compressed SQL script of insert statements in the SQL dialect of
	 * the database of this application.
	 *
	 * @param log
	 *        Receives the progress of the conversion.
	 * @return The script, named after the dump with the extension {@link #SQL_EXT}.
	 *
	 * @see #replaceDatabase(Log, BinaryData)
	 */
	public BinaryData createSqlScript(Log log) {
		return BinaryDataFactory.createBinaryData(createSqlFile(log), GZIP_CONTENT_TYPE, baseName() + SQL_EXT);
	}

	/**
	 * Converts the dump into a gzip-compressed SQL script of insert statements in the SQL dialect of
	 * the database of this application.
	 *
	 * @param log
	 *        Receives the progress of the conversion.
	 * @return A temporary file holding the script.
	 */
	public File createSqlFile(Log log) {
		log.info("Creating import SQL.");
		try {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			File sqlFile = File.createTempFile("data", SQL_EXT, Settings.getInstance().getTempDir());
			try (InsertWriter insertWriter = SQLDumper.createInsertWriter(pool.getSQLDialect(),
				SQLDumper.DEFAULT_INSERT_CHUNK_SIZE, writer(sqlFile))) {
				processDumpData(log, pool, insertWriter);
			}
			log.info("SQL generated.");
			return sqlFile;
		} catch (AbortExecutionException | TopLogicException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED, ex);
		}
	}

	/**
	 * Converts the dump into control files of the Oracle SQL*Loader, packed as ZIP archive.
	 *
	 * <p>
	 * The control files are written for the database of this application if it is an Oracle
	 * database, and for Oracle 12 otherwise.
	 * </p>
	 *
	 * @param log
	 *        Receives the progress of the conversion.
	 * @return The archive, named after the dump with the suffix {@link #LOADER_SUFFIX} and the
	 *         extension {@link #ZIP_EXT}.
	 */
	public BinaryData createLoaderScript(Log log) {
		String baseName = baseName() + LOADER_SUFFIX;
		return BinaryDataFactory.createBinaryData(createLoaderZip(log, baseName), ZIP_CONTENT_TYPE, baseName + ZIP_EXT);
	}

	/**
	 * Converts the dump into control files of the Oracle SQL*Loader, packed as ZIP archive.
	 *
	 * @param log
	 *        Receives the progress of the conversion.
	 * @param baseName
	 *        The name of the folder in the archive holding the control files.
	 * @return A temporary file holding the archive.
	 */
	public File createLoaderZip(Log log, String baseName) {
		log.info("Creating SQL loader scripts.");
		try {
			ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
			DBHelper sqlDialect = pool.getSQLDialect();
			if (!(sqlDialect instanceof OracleHelper)) {
				// For "cross-compiling" application data.
				sqlDialect = TypedConfigUtil.createInstance(Oracle12Helper.Config.class);
			}
			File loaderDir = File.createTempFile(baseName, "", Settings.getInstance().getTempDir());

			// Allow creating directory with tmp file name.
			loaderDir.delete();
			loaderDir.mkdir();

			InsertWriter insertWriter = new SQLLoaderInsertWriter(sqlDialect, loaderDir);
			processDumpData(log, pool, insertWriter);

			log.info("Zipping loader scripts.");
			File loaderFile = File.createTempFile(baseName, ZIP_EXT, Settings.getInstance().getTempDir());
			try (Zipper zip = new Zipper(loaderFile)) {
				zip.addFolder(loaderDir, baseName);
			}
			return loaderFile;
		} catch (AbortExecutionException | TopLogicException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED, ex);
		}
	}

	/**
	 * Converts the dump with the given writer.
	 *
	 * <p>
	 * The conversion reports a defect of the dump, e.g. a change of an object the dump never
	 * created, to the log and goes on. A script converted from such a dump would load incomplete
	 * data, so an error reported while converting fails the conversion.
	 * </p>
	 */
	private void processDumpData(Log log, ConnectionPool pool, InsertWriter insertWriter) throws Exception {
		boolean hadErrors = log.hasErrors();
		InsertWriter applicationData = withoutInstallationTables(insertWriter);
		try (InputStream in = input(_dump)) {
			applicationData.beginDump();
			_migration.createInsertSQL(log, loadKbConfig(), pool, 1L, in, applicationData);
			applicationData.endDump();
		}
		if (!hadErrors && log.hasErrors()) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED, log.getFirstProblem());
		}
	}

	/**
	 * The given writer, dropping the rows of the {@link KBDump#INSTALLATION_TABLES}, which a dump
	 * may still contain: loading a dump keeps the runtime state of the installation it is loaded
	 * into.
	 */
	private static InsertWriter withoutInstallationTables(InsertWriter out) {
		return new InsertWriter() {
			@Override
			public void beginDump() throws IOException {
				out.beginDump();
			}

			@Override
			public void appendInsert(DBTable table, List<Object[]> values) throws IOException {
				if (KBDump.INSTALLATION_TABLES.contains(table.getName())) {
					return;
				}
				out.appendInsert(table, values);
			}

			@Override
			public void endDump() throws IOException {
				out.endDump();
			}

			@Override
			public void close() throws IOException {
				out.close();
			}
		};
	}

	/**
	 * Replaces the whole content of the database of the running application by the data of the
	 * given SQL script, a few seconds from now.
	 *
	 * <p>
	 * The script is executed while the persistency layer is stopped, after all tables of the
	 * application have been dropped and created again empty. The {@link KBDump#INSTALLATION_TABLES}
	 * are kept, so the installation keeps its cluster state, e.g. an active maintenance window. The persistency layer is restarted
	 * afterwards, so every session ends. All data the database held before is lost.
	 * </p>
	 *
	 * <p>
	 * The tables are rewritten with plain SQL, so the replacement runs only under the conditions of
	 * {@link PersistencyMaintenance#checkPreconditions()}: in an active maintenance window, in which
	 * no user works with the data, and, in a cluster, on the single active node, so that no other
	 * node works on the tables being dropped.
	 * </p>
	 *
	 * @param log
	 *        Receives the progress of the replacement.
	 * @param sqlScript
	 *        The gzip-compressed SQL script created by {@link #createSqlScript(Log)}.
	 *
	 * @throws TopLogicException
	 *         If a precondition of {@link PersistencyMaintenance#checkPreconditions()} is
	 *         violated.
	 */
	public static void replaceDatabase(Log log, BinaryData sqlScript) {
		PersistencyMaintenance.requirePreconditions();

		KnowledgeBaseConfiguration kbConfig = loadKbConfig();
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		PersistencyMaintenance.scheduleRestart(log, false, () -> load(log, kbConfig, pool, sqlScript));
	}

	private static void load(Log log, KnowledgeBaseConfiguration kbConfig, ConnectionPool pool,
			BinaryData sqlScript) {
		log.info("Starting import.");
		try (Reader in = reader(sqlScript)) {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				DataMigration.dropAndRecreateDatabase(connection, kbConfig, in, KBDump.INSTALLATION_TABLES);
			} finally {
				pool.releaseWriteConnection(connection);
			}
			log.info("Import finished successfully.");
		} catch (Exception ex) {
			log.error("Data import reported errors, trying to restart anyway.", ex);
		}
	}

	/**
	 * The configuration of the default knowledge base of this application.
	 */
	public static KnowledgeBaseConfiguration loadKbConfig() {
		try {
			KnowledgeBaseFactoryConfig serviceConfiguration =
				(KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
					.getServiceConfiguration(KnowledgeBaseFactory.class);
			return serviceConfiguration.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	private static Reader reader(BinaryData sqlScript) throws IOException {
		return new InputStreamReader(gunzip(sqlScript.getStream()), StandardCharsets.UTF_8);
	}

	private static Writer writer(File sqlFile) throws IOException {
		return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(sqlFile)), StandardCharsets.UTF_8);
	}

	private static InputStream input(BinaryData data) throws IOException {
		InputStream in = data.getStream();
		if (data.getName().endsWith(KBDump.GZ_EXT)) {
			return gunzip(in);
		}
		return in;
	}

	private static InputStream gunzip(InputStream in) throws IOException {
		try {
			return new GZIPInputStream(in);
		} catch (Throwable ex) {
			in.close();
			throw ex;
		}
	}

}
