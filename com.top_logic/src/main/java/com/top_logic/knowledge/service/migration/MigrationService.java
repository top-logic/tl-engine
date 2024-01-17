/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import static com.top_logic.knowledge.service.migration.MigrationUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import com.top_logic.basic.Environment;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.equal.EqualityRedirect;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.encryption.SymmetricEncryption;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.InitialTableSetup;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.PersistentIdFactory;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.knowledge.service.db2.migration.KnowledgeBaseDumper;
import com.top_logic.knowledge.service.db2.migration.db.transformers.StoreTypeConfiguration;
import com.top_logic.knowledge.service.db2.migration.sql.InsertWriter;
import com.top_logic.knowledge.service.db2.migration.sql.SQLDumper;

/**
 * {@link ManagedClass} that automatically migrates the application to the newest version.
 * 
 * <p>
 * Migrations to perform are described in XML configurations corresponding to the
 * {@link MigrationConfig} schema located in the directory
 * {@link MigrationUtil#MIGRATION_BASE_RESOURCE} in an application. The service automatically keeps
 * track of the migrations already applied so that no {@link MigrationConfig} is applied twice. In
 * other words, the set of {@link MigrationConfig}s applied corresponds to the database version of
 * the application.
 * </p>
 * 
 * @see MigrationConfig
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	ConnectionPoolRegistry.Module.class,
	/* Creation of temp file to dump KB */
	Settings.Module.class,
})
@ServiceExtensionPoint(InitialTableSetup.Module.class)
public class MigrationService extends ConfiguredManagedClass<MigrationService.Config> {

	/**
	 * Configuration for the {@link MigrationService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<MigrationService> {

		/** Configuration name of the value of {@link #getModules()}. */
		String MODULES_NAME = "modules";

		/**
		 * Option to configure the encryption algorithm to use.
		 */
		String ENCRYPTION_ALGORITHM_PROPERTY = "encryptionAlgorithm";

		/**
		 * Option to configure the maximum numbers of rows to insert within one statement.
		 */
		String INSERT_CHUNK_SIZE = "insertChunkSize";

		/**
		 * Option to configure the maximal number of items to hold in cache before the insert SQL is
		 * created.
		 */
		String BUFFER_SIZE = "bufferSize";

		/**
		 * @see #getAllowDowngrade()
		 */
		String ALLOW_DOWNGRADE = "allow-downgrade";

		/**
		 * The database modules.
		 */
		@Name(MODULES_NAME)
		@ListBinding(attribute = "name")
		List<String> getModules();

		/**
		 * The algorithm that is used to encrypt temporary files.
		 * 
		 * @return empty for "no encryption".
		 */
		@Name(ENCRYPTION_ALGORITHM_PROPERTY)
		String getEncryptionAlgorithm();

		/**
		 * Maximal number of rows to insert in one statement.
		 */
		@Name(INSERT_CHUNK_SIZE)
		@IntDefault(SQLDumper.DEFAULT_INSERT_CHUNK_SIZE)
		int getInsertChunkSize();

		/**
		 * Maximal number of items to hold in cache before the insert SQL is created.
		 */
		@Name(BUFFER_SIZE)
		@IntDefault(SQLDumper.DEFAULT_BUFFER_SIZE)
		int getBufferChunkSize();

		/**
		 * Whether to allow booting even if the version in the database is "higher" than the version
		 * of the deployed application.
		 * 
		 * <p>
		 * Normally (if this switch is <code>false</code>), booting an old version of an application
		 * on a new data version read from the database is prevented. Under rare circumstances, it
		 * can be necessary to disable this check to allow booting the main branch of an application
		 * on a database that contains (temporary) migrations done in deployments from temporary
		 * branches of the application.
		 * </p>
		 */
		@Name(ALLOW_DOWNGRADE)
		boolean getAllowDowngrade();

	}

	private final ConnectionPool _connectionPool;

	private MigrationInfo _migrationInfo;

	private File _kbDump;

	private File _sqlFile;

	private SymmetricEncryption _encryption;

	private boolean _automaticMigration = true;

	/**
	 * Creates a new {@link MigrationService} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MigrationService}.
	 */
	public MigrationService(InstantiationContext context, Config config) {
		super(context, config);
		_connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		_migrationInfo = fetchMigrations(context);
	}

	private MigrationInfo fetchMigrations(InstantiationContext context) {
		if (_connectionPool.isDryRun()) {
			context.info("No automatical data migration, because system is in \"dry run\" mode.");
			_automaticMigration = false;
			return MigrationInfo.NO_MIGRATION;
		}
		PooledConnection connection = _connectionPool.borrowReadConnection();
		try {
			return createMigrations(context, connection);
		} catch (SQLException ex) {
			context.error("Unable to fetch migration scripts.", ex);
			_automaticMigration = false;
			return MigrationInfo.NO_MIGRATION;
		} finally {
			_connectionPool.releaseReadConnection(connection);
		}
	}

	private MigrationInfo createMigrations(InstantiationContext context, PooledConnection connection)
			throws SQLException {

		if (Environment.getSystemPropertyOrEnvironmentVariable(InitialTableSetup.NO_DATABASE_TABLE_SETUP, false)) {
			StringBuilder info = new StringBuilder();
			info.append("No automatical migration, because system property '");
			info.append(InitialTableSetup.NO_DATABASE_TABLE_SETUP);
			info.append("' is set.");
			context.info(info.toString());
			_automaticMigration = false;
			return MigrationInfo.NO_MIGRATION;
		} else if (!DBProperties.tableExists(connection)) {
			StringBuilder info = new StringBuilder();
			info.append("No database table ");
			info.append(DBProperties.TABLE_NAME);
			info.append(" to read versions from.");
			context.info(info.toString(), Protocol.WARN);
			_automaticMigration = false;
			return MigrationInfo.NO_MIGRATION;
		}

		Config config = getConfig();
		String[] migrationModules = getMigrationModules(config);
		return MigrationUtil.relevantMigrations(context, connection, migrationModules, config.getAllowDowngrade());
	}

	/**
	 * All migration relevant modules listed in the given configuration.
	 */
	public static String[] getMigrationModules(Config config) {
		return toModuleNames(config.getModules());
	}

	/**
	 * Checks for schema changes between the stores database schema and the configured one and
	 * adjusts the current schema.
	 * 
	 * @param log
	 *        {@link ProcessBuilder} to write errors (and any messages) to.
	 * @param connection
	 *        {@link PooledConnection} to write changes to.
	 * @return Whether any changes have been written to the given connection.
	 */
	private boolean executeAutomaticSchemaMigrations(MigrationContext context, Protocol log,
			PooledConnection connection) {
		Map<String, MetaObjectName> applicationTypes = context.getApplicationSchema().getMetaObjects().getTypes();
		Map<String, MetaObjectName> persistentTypes = context.getPersistentSchema().getMetaObjects().getTypes();

		MapDifference<String, EqualityRedirect<MetaObjectName>> diff = Maps.difference(
			wrap(persistentTypes), wrap(applicationTypes));

		Map<String, EqualityRedirect<MetaObjectName>> newTypes = diff.entriesOnlyOnRight();
		if (!newTypes.isEmpty()) {
			MORepository allTypes = context.getApplicationSetup().createMORepository(DefaultMOFactory.INSTANCE);

			CreateTablesProcessor.Config createProcessor =
					TypedConfiguration.newConfigItem(CreateTablesProcessor.Config.class);
			SchemaSetup.addTables(createProcessor, allTypes, newTypes.keySet());

			if (!createProcessor.getTables().isEmpty()) {
				execute(context, log, connection, createProcessor);

				InsertBranchSwitchProcessor.Config insertProcessor =
					TypedConfiguration.newConfigItem(InsertBranchSwitchProcessor.Config.class);
				for (String newTypeName : newTypes.keySet()) {
					MetaObject newType = allTypes.getMetaObject(newTypeName);
					if (createProcessor.getTable(newType.getName()) != null) {
						insertProcessor.getTableTypes().add(newType.getName());
					}
				}
				assert insertProcessor.getTableTypes().size() == createProcessor.getTables().size();
				execute(context, log, connection, insertProcessor);

				// Create a StoreTypeConfiguration processor.
				StoreTypeConfiguration.Config updateProcessor =
					TypedConfiguration.newConfigItem(StoreTypeConfiguration.Config.class);
				execute(context, log, connection, updateProcessor);
				return true;
			}
		}
		return false;
	}

	private void execute(MigrationContext context, Protocol log, PooledConnection connection,
			PolymorphicConfiguration<? extends MigrationProcessor> processor) {
		TypedConfigUtil.createInstance(processor).doMigration(context, log, connection);
	}

	private static <K, V extends ConfigurationItem> Map<K, EqualityRedirect<V>> wrap(Map<K, V> map) {
		HashMap<K, EqualityRedirect<V>> result = new HashMap<>();
		for (java.util.Map.Entry<K, V> entry : map.entrySet()) {
			result.put(entry.getKey(),
				new EqualityRedirect<>(entry.getValue(), ConfigEquality.INSTANCE_ALL_BUT_DERIVED));
		}
		return result;
	}

	@Override
	protected void startUp() {
		super.startUp();
		LogProtocol log = new LogProtocol(MigrationService.class);
		migrate(log);
	}

	private void migrate(Protocol log) {
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(connection);

			if (_migrationInfo.nothingToDo()) {
				log.info("No configured migrations.");

				if (_automaticMigration) {
					boolean changes = executeAutomaticSchemaMigrations(context, log, connection);

					// Check that everything is ok.
					log.checkErrors();
					if (changes) {
						commitConnection(log, connection, "Unable to commit automatic schema changes.");
						return;
					}

				}
				return;
			}

			if (_migrationInfo.isDownGrade()) {
				MigrationUtil.dropStoredVersions(log, connection);
				Collection<Version> maxVersions = MigrationUtil.maximalVersions(log);
				MigrationUtil.updateStoredVersions(log, connection, maxVersions);
				if (_migrationInfo.getMigrations().isEmpty()) {
					/* Ensure update of version are made persistent. */
					commitConnection(log, connection, "Unable to commit version changes during downgrade.");
					return;
				}
			}
			createTempFiles();

			Map<String, Version> maximalVersionByModule =
				migrate(context, log, connection, _migrationInfo.isSchemaUpdateRequired());

			MigrationUtil.updateStoredVersions(log, connection, maximalVersionByModule.values());

			commitConnection(log, connection, "Unable to commit migrated changes.");

			KnowledgeBase kb = null;
			Transaction tx = null;
			try {
				for (MigrationConfig migration : _migrationInfo.getMigrations()) {
					for (PolymorphicConfiguration<? extends MigrationPostProcessor> processorConf : migration
						.getPostProcessors()) {
						MigrationPostProcessor processor = TypedConfigUtil.createInstance(processorConf);

						if (kb == null) {
							kb = createDumpingKB(log);
							tx = kb.beginTransaction();
						}

						processor.afterMigration(log, kb);
					}
				}

				if (tx != null && !log.hasErrors()) {
					tx.commit();
				}
			} finally {
				if (tx != null) {
					tx.rollback();
				}
			}

			// Check that everything is ok.
			log.checkErrors();

			// Remove temporary files potentially very large and containing classified information.
			removeTempFiles(log);
		} finally {
			/* Invalidate the connection: This is (currently) necessary, because the migration may
			 * change the database definition (e.g. change column from nvarchar to nclob), which may
			 * corrupt the connection, such that following actions may cause errors (as occurred in
			 * Oracle). */
			_connectionPool.invalidateWriteConnection(connection);
		}
	}

	private void commitConnection(Protocol log, PooledConnection connection, String failureMessage) {
		// Do not commit changes when an error occurred.
		log.checkErrors();

		try {
			connection.commit();
		} catch (SQLException ex) {
			throw log.fatal(failureMessage, ex);
		}
	}

	private void createTempFiles() {
		File tempDir = Settings.getInstance().getTempDir();
		_kbDump = newTempFile("kbMigration", ".xml.gz", tempDir);
		_sqlFile = newTempFile("kbMigration", ".sql.gz", tempDir);
		createSymmetricEncryption();
	}

	private void createSymmetricEncryption() throws UnreachableAssertion {
		String encryptionAlgorithm = getConfig().getEncryptionAlgorithm();
		if (encryptionAlgorithm.isEmpty()) {
			return;
		}
		SecureRandom rand = new SecureRandom();
		SecretKey secret;
		try {
			secret = KeyGenerator.getInstance(encryptionAlgorithm).generateKey();
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
		_encryption = new SymmetricEncryption(rand, secret);
	}

	private File newTempFile(String prefix, String suffix, File tempDir) {
		String date = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		File f = new File(tempDir, prefix + date + suffix);
		if (f.exists()) {
			ensureNewTimestamp();
			return newTempFile(prefix, suffix, tempDir);
		}
		boolean creationSuccessful;
		try {
			creationSuccessful = f.createNewFile();
		} catch (IOException ex) {
			creationSuccessful = false;
		}
		if (!creationSuccessful) {
			ensureNewTimestamp();
			return newTempFile(prefix, suffix, tempDir);
		}
		return f;
	}

	private void ensureNewTimestamp() {
		// Ensure new timestamp.
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			// Ignore;
		}
	}

	private void removeTempFiles(Log log) {
		removeTmpFile(log, _kbDump);
		removeTmpFile(log, _sqlFile);
	}

	void removeTmpFile(Log log, File tmpFile) {
		if (tmpFile.exists()) {
			boolean success = tmpFile.delete();
			if (!success) {
				log.info("Unable to delete temporary file '" + tmpFile.getAbsolutePath() + "'.", Log.WARN);
			}
		}
	}

	private Map<String, Version> migrate(MigrationContext context, Protocol log, PooledConnection connection,
			boolean schemaUpdateRequired) {
		DataMigration migration = new DataMigration(_migrationInfo);
		migration.setBufferChunkSize(getConfig().getBufferChunkSize());
		migration.build(log);

		// Note: SQL migration (migration processors) must happen before the persistency layer is
		// accessed to allow upgrading the baseline of the persistency layer configuration in those
		// processors.
		migration.executeSQLMigration(context, log, connection);

		executeAutomaticSchemaMigrations(context, log, connection);

		// Update stored schema before the replay to ensure the dumping KB uses the correct
		// configuration.
		if (schemaUpdateRequired) {
			StoreTypeConfiguration store = TypedConfigUtil.createInstance(StoreTypeConfiguration.Config.class);
			store.doMigration(context, log, connection);
		}

		if (migration.isReplayRequired()) {
			commitConnection(log, connection, "Unable to commit changes before KnowledgeBase replay.");

			KnowledgeBase kb = createDumpingKB(log);
			if (kb != null) {
				replayHistory(log, connection, migration, kb);
			}
		}

		return migration.getNewVersion();
	}

	private KnowledgeBase createDumpingKB(Protocol log) {
		KnowledgeBase kb;
		try {
			kb = dumpingKB(log, kbConfig());
		} catch (ConfigurationException ex) {
			log.error("Unable to get KnowledgeBaseConfiguration.", ex);
			kb = null;
		}
		return kb;
	}

	private void replayHistory(Protocol log, PooledConnection connection, DataMigration migration,
			KnowledgeBase dumpingKB) {
		try {
			// Commit all changes so far. The database is now dumped and recreated.
			connection.commit();
		} catch (Exception ex) {
			log.error("Problem committing pre-replay changes.", ex);
		}

		KnowledgeBaseConfiguration kbConfig = dumpingKB.getConfiguration();
		try {
			StopWatch sw = StopWatch.createStartedWatch();
			dumpKB(log, dumpingKB);
			log.info("Dump created in " + sw);
		} catch (Exception ex) {
			log.error("Unable to dump KnowledgeBase.", ex);
		}
		log.checkErrors();

		try (InputStream in = kbDumpIn()) {
			try (Writer out = sqlOut()) {
				StopWatch sw = StopWatch.createStartedWatch();
				DBHelper sqlDialect = connection.getSQLDialect();
				InsertWriter insertWriter =
					SQLDumper.createInsertWriter(sqlDialect, getConfig().getInsertChunkSize(), out);

				insertWriter.beginDump();
				migration.createInsertSQL(log, kbConfig, connection.getPool(), getNextObjectId(dumpingKB), in,
					insertWriter);
				insertWriter.endDump();
				log.info("Migration finished in " + sw);
			}
		} catch (Exception ex) {
			log.error("Unable to apply data transformation.", ex);
		}
		log.checkErrors();

		try (Reader in = sqlIn()) {
			log.info("Adapting database");
			StopWatch sw = StopWatch.createStartedWatch();
			migration.dropAndRecreateDatabase(connection, kbConfig, in);
			log.info("Database adapted in " + sw);
		} catch (Exception ex) {
			log.error("Unable to install SQL.", ex);
		}
		log.checkErrors();
	}

	private KnowledgeBaseConfiguration kbConfig() throws ConfigurationException {
		KnowledgeBaseFactoryConfig kbFactoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
			.getServiceConfiguration(KnowledgeBaseFactory.class);
		return kbFactoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
	}

	private void dumpKB(Protocol log, KnowledgeBase kb) throws IOException, FileNotFoundException {
		KnowledgeBaseDumper kbDumper = new KnowledgeBaseDumper(kb);
		/* Do not dump security storage. This table is typically large. The security storage is
		 * rebuild automatically when the table is empty. */
		kbDumper.setIgnoreTypes(Collections.singleton(SecurityStorage.SECURITY_STORAGE_OBJECT_NAME));
		kbDumper.setLog(log);
		try (OutputStream out = kbDumpOut()) {
			kbDumper.dump(out);
		}
	}

	private long getNextObjectId(KnowledgeBase knowledgeBase) throws SQLException {
		long lastId = getLastObjectId(knowledgeBase);
		return (lastId + 1) * PersistentIdFactory.CHUNK_SIZE;
	}

	private long getLastObjectId(KnowledgeBase knowledgeBase) throws SQLException {
		ConnectionPool connectionPool = KBUtils.getConnectionPool(knowledgeBase);
		PooledConnection connection = connectionPool.borrowReadConnection();
		try {
			return getLastSequenceNumber(connection);
		} finally {
			connectionPool.releaseReadConnection(connection);
		}
	}

	private long getLastSequenceNumber(PooledConnection connection) throws SQLException {
		SequenceManager sequenceManager = new RowLevelLockingSequenceManager();
		String sequenceId = DBKnowledgeBase.ID_SEQ;
		return sequenceManager.getLastSequenceNumber(connection, connection.getSQLDialect().retryCount(), sequenceId);
	}

	KnowledgeBase dumpingKB(Protocol log, KnowledgeBaseConfiguration kbConfig) {
		KnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(log, kbConfig);
		((DBKnowledgeBase) kb).initLocalVariablesFromDatabase(log);
		return kb;
	}

	private InputStream kbDumpIn() throws IOException, FileNotFoundException {
		FileInputStream fis = new FileInputStream(_kbDump);
		return decrypt(new GZIPInputStream(fis));
	}

	private OutputStream kbDumpOut() throws IOException, FileNotFoundException {
		FileOutputStream fos = new FileOutputStream(_kbDump);
		return encrypt(new GZIPOutputStream(fos));
	}

	private Reader sqlIn() throws IOException {
		InputStream in = new FileInputStream(_sqlFile);
		InputStream gzIn = new GZIPInputStream(in);
		InputStream decrIn = decrypt(gzIn);
		return new InputStreamReader(decrIn, StringServices.CHARSET_UTF_8);
	}

	private Writer sqlOut() throws IOException {
		OutputStream out = new FileOutputStream(_sqlFile);
		OutputStream gzOut = new GZIPOutputStream(out);
		OutputStream encrOut = encrypt(gzOut);
		return new OutputStreamWriter(encrOut, StringServices.CHARSET_UTF_8);
	}

	private InputStream decrypt(InputStream in) throws IOException {
		if (_encryption != null) {
			return _encryption.decrypt(in);
		} else {
			return in;
		}
	}

	private OutputStream encrypt(OutputStream out) throws IOException {
		if (_encryption != null) {
			return _encryption.encrypt(out);
		} else {
			return out;
		}
	}

	/**
	 * Singleton reference for {@link MigrationService}.
	 */
	public static final class Module extends TypedRuntimeModule<MigrationService> {

		/**
		 * Singleton {@link MigrationService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<MigrationService> getImplementation() {
			return MigrationService.class;
		}

	}

}
