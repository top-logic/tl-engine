/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

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
import java.nio.charset.StandardCharsets;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import com.top_logic.basic.Environment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.equal.EqualityRedirect;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
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
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
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
import com.top_logic.knowledge.service.migration.processors.AddApplicationTypesProcessor;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.util.model.CompatibilityService;

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
	AttributeSettings.Module.class,
	CompatibilityService.Module.class,
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
		 * @see #getMinimumModules()
		 */
		String MINIMUM_MODULES = "minimum-modules";

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
		 * Assume at least the initial version of the given modules is present in the base version
		 * stored in the database.
		 * 
		 * <p>
		 * The setting is required for upgrading systems from a version before the introduction of
		 * initial versions.
		 * </p>
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(MINIMUM_MODULES)
		List<String> getMinimumModules();

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
		List<String> migrationModules = config.getModules();
		return relevantMigrations(context, connection, migrationModules, config.getAllowDowngrade());
	}

	/**
	 * Determines the correct {@link MigrationConfig}s for the given migration modules.
	 * 
	 * @param connection
	 *        {@link PooledConnection} to connect to database to read current version.
	 * @param migrationModules
	 *        All known database modules. The order of the module is the dependency order, i.e. when
	 *        <code>module1</code> depends on <code>module2</code>, <code>module2</code> appears
	 *        before <code>module1</code> in the array.
	 * @param allowDowngrade
	 *        Whether to ignore missing version descriptors found in the database.
	 * @return {@link MigrationConfig}s to apply in that order to update to correct database
	 *         version.
	 * 
	 * @throws SQLException
	 *         when reading versions from database failed for some reason.
	 */
	private MigrationInfo relevantMigrations(Log log, PooledConnection connection,
			List<String> migrationModules, boolean allowDowngrade) throws SQLException {
		Map<String, Version> storedVersions = MigrationUtil.readStoredVersions(connection, migrationModules);

		for (String module : getConfig().getMinimumModules()) {
			storedVersions.computeIfAbsent(module, MigrationUtil::initialVersion);
		}

		log.info("Migration modules: " + migrationModules);
		log.info("Current data version: " + storedVersions.values().stream()
			.map(v -> v.getModule() + ": " + v.getName()).collect(Collectors.joining(", ")));
		return MigrationUtil.relevantMigrations(log, migrationModules, allowDowngrade, storedVersions);
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

		recoverMetaDefinitions(log, persistentTypes, diff.entriesOnlyOnLeft().keySet());

		Map<String, EqualityRedirect<MetaObjectName>> newTypes = diff.entriesOnlyOnRight();
		if (!newTypes.isEmpty()) {
			AddApplicationTypesProcessor.Config<?> addConfig =
				TypedConfiguration.newConfigItem(AddApplicationTypesProcessor.Config.class);

			for (String newTypeName : newTypes.keySet()) {
				MetaObjectName newType = applicationTypes.get(newTypeName);
				addConfig.getSchema().getTypes().put(newTypeName, newType);
			}

			execute(context, log, connection, addConfig);
			return true;
		}
		return false;
	}

	/**
	 * Creates a <code>*.meta.xml</code> definition for "old" types found in the schema definition
	 * stored in the database but not in the current application configuration. Since those types
	 * are not automatically deleted, a configuration must be created for them to ensure that a
	 * following replay is able to re-create those tables.
	 */
	private void recoverMetaDefinitions(Protocol log, Map<String, MetaObjectName> persistentTypes,
			Set<String> oldTypeNames) {
		if (!oldTypeNames.isEmpty()) {
			log.info("Recovering configurations for lost tables: "
				+ oldTypeNames.stream().collect(Collectors.joining(", ")));
			MetaObjectsConfig inappSchema = TypedConfiguration.newConfigItem(MetaObjectsConfig.class);
			for (var typeName : oldTypeNames) {
				inappSchema.getTypes().put(typeName, persistentTypes.get(typeName));
			}
			try {
				int id = 1;
				File confFile;
				String resourceName;
				String baseName;
				do {
					baseName = "inapp-schema-" + id;
					resourceName = ModuleLayoutConstants.AUTOCONF_FOLDER_RESOURCE + baseName + ".meta.xml";
					confFile = FileManager.getInstance().getIDEFile(resourceName);
					id++;
				} while (confFile.exists());
				storeResource(inappSchema, confFile);

				// Create a configuration fragment that loads the synthesized schema definition.
				ApplicationConfig.Config configFragment =
					TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
				{
					ApplicationTypes typesConfig = TypedConfiguration
						.newConfigItem(ApplicationTypes.class);
					{
						SchemaConfiguration typeSystem = TypedConfiguration.newConfigItem(SchemaConfiguration.class);
						{
							typeSystem.setName(SchemaConfiguration.DEFAULT_NAME);
							{
								ResourceDeclaration decl = TypedConfiguration.newConfigItem(ResourceDeclaration.class);
								decl.setResource(
									"webinf://" + ModuleLayoutConstants.AUTOCONF_DIR + "/" + baseName + ".meta.xml");
								typeSystem.getDeclarations().add(decl);
							}
						}
						typesConfig.getTypeSystems().add(new SchemaSetup(null, typeSystem));
					}
					configFragment.getConfigs().put(ApplicationTypes.class, typesConfig);
				}
				storeResource(configFragment,
					FileManager.getInstance().getIDEFile(
						ModuleLayoutConstants.AUTOCONF_FOLDER_RESOURCE + baseName + ".config.xml"));

				log.info("Created schema configuration with recovered types: " + confFile.getAbsolutePath());

				// Re-load configuration to make sure, the updated config is found later on.
				TLServiceUtils.reloadConfigurations();
			} catch (IOException | XMLStreamException | SAXException ex) {
				log.error("Cannot create schema add-on configuration: " + ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Creates a new configuration file.
	 */
	private void storeResource(ConfigurationItem conf, File file)
			throws XMLStreamException, IOException, FileNotFoundException, SAXException {
		try (var out = new FileOutputStream(file)) {
			try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
				new ConfigurationWriter(writer)
					.write(MORepositoryBuilder.ROOT_TAG, MetaObjectsConfig.class, conf);
			}
		}
		XMLPrettyPrinter.normalizeFile(file);
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
			MigrationContext context = new MigrationContext(log, connection);

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
				migrate(context, log, connection);

			MigrationUtil.updateStoredVersions(log, connection, maximalVersionByModule.values());

			// Update stored schema to be compatible with the current application configuration.
			StoreTypeConfiguration store = TypedConfigUtil.createInstance(StoreTypeConfiguration.Config.class);
			store.doMigration(context, log, connection);

			commitConnection(log, connection, "Unable to commit migrated changes.");

			KnowledgeBase kb = null;
			Transaction tx = null;
			try {
				for (MigrationConfig migration : _migrationInfo.getMigrations()) {
					for (PolymorphicConfiguration<? extends MigrationPostProcessor> processorConf : migration
						.getPostProcessors()) {
						MigrationPostProcessor processor = TypedConfigUtil.createInstance(processorConf);

						if (kb == null) {
							kb = createKB(log);
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

	private Map<String, Version> migrate(MigrationContext context, Protocol log, PooledConnection connection) {
		DataMigration migration = new DataMigration(_migrationInfo);
		migration.setBufferChunkSize(getConfig().getBufferChunkSize());
		migration.build(log);

		// Note: SQL migration (migration processors) must happen before the persistency layer is
		// accessed to allow upgrading the baseline of the persistency layer configuration in those
		// processors.
		migration.executeSQLMigration(context, log, connection);

		executeAutomaticSchemaMigrations(context, log, connection);

		if (migration.isReplayRequired()) {
			commitConnection(log, connection, "Unable to commit changes before KnowledgeBase replay.");

			KnowledgeBase kb = createKB(log);
			if (kb != null) {
				replayHistory(log, connection, migration, kb);
			}
		}

		return migration.getNewVersion();
	}

	private KnowledgeBase createKB(Protocol log) {
		try {
			KnowledgeBase kb = KnowledgeBaseFactory.createKnowledgeBase(log, kbConfig());
			((DBKnowledgeBase) kb).initLocalVariablesFromDatabase(log);
			return kb;
		} catch (ConfigurationException ex) {
			log.error("Unable to get KnowledgeBaseConfiguration.", ex);
			return null;
		}
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
