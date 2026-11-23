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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.top_logic.basic.Logger;
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
import com.top_logic.basic.config.annotation.Label;
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
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.PersistenceCategory;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dsa.DatabaseAccessException;
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
import com.top_logic.knowledge.service.migration.processors.AdoptTypeChangesProcessor;
import com.top_logic.knowledge.service.migration.processors.AlignTableLayoutProcessor;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.util.model.CompatibilityService;

/**
 * Automatically migrates the application data to the newest version on startup.
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
 * <p>
 * A start that changes the database holds the {@link MigrationLock} while migrating, so that nodes
 * of a cluster starting concurrently do not migrate the same database. Each step of the migration
 * is committed together with a {@link MigrationProgress}. A start after an interrupted migration
 * resumes it after its last completed step: It takes over the lock left behind by a terminated run
 * of the same node. The lock of another node is waited for; after checking that the holder is no
 * longer running, the lock property {@value MigrationLock#LOCK_PROPERTY} may be deleted manually,
 * and the next start resumes the migration as well.
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
@Label("Data migration")
@ServiceCategory.Of(PersistenceCategory.class)
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
		 * 
		 * <p>
		 * A module without a version stored in the database is considered new to the application:
		 * Its data does not exist, and all its migrations are skipped. This is wrong for the first
		 * own migration of an application whose database was created while the application had no
		 * migration folder: Its data exists, but no version of its module was stored. Such an
		 * application must list its migration module here, so that its migrations are applied. The
		 * entry must come with the first migration: Each startup stores the maximal version of each
		 * module without stored version, so that a skipped migration counts as applied afterwards.
		 * Skipped migrations of a module without stored version are logged.
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

	private MigrationInfo _migrationInfo = MigrationInfo.NO_MIGRATION;

	/**
	 * The progress of an interrupted migration that is resumed, <code>null</code> if no
	 * migration was interrupted.
	 */
	private MigrationProgress _progress;

	/**
	 * The lock held while migrating, <code>null</code> outside the migration.
	 */
	private MigrationLock _migrationLock;

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
	}

	/**
	 * Whether the database can be migrated at all.
	 */
	private boolean isMigrationEnabled(Log log) {
		if (_connectionPool.isDryRun()) {
			log.info("No automatical data migration, because system is in \"dry run\" mode.");
			return false;
		}
		if (Environment.getSystemPropertyOrEnvironmentVariable(InitialTableSetup.NO_DATABASE_TABLE_SETUP, false)) {
			StringBuilder info = new StringBuilder();
			info.append("No automatical migration, because system property '");
			info.append(InitialTableSetup.NO_DATABASE_TABLE_SETUP);
			info.append("' is set.");
			log.info(info.toString());
			return false;
		}
		PooledConnection connection = _connectionPool.borrowReadConnection();
		try {
			if (!DBProperties.tableExists(connection)) {
				StringBuilder info = new StringBuilder();
				info.append("No database table ");
				info.append(DBProperties.TABLE_NAME);
				info.append(" to read versions from.");
				log.info(info.toString(), Protocol.WARN);
				return false;
			}
		} catch (SQLException ex) {
			log.error("Unable to check for database table " + DBProperties.TABLE_NAME + ".", ex);
			return false;
		} finally {
			_connectionPool.releaseReadConnection(connection);
		}
		return true;
	}

	/**
	 * Determines the migrations to apply from the versions currently stored in the database.
	 *
	 * <p>
	 * If a {@link MigrationProgress} of an interrupted migration exists, the migrations are the
	 * ones of its {@link MigrationProgress#getPlan() plan}, and the progress is kept in
	 * {@link #_progress}.
	 * </p>
	 *
	 * <p>
	 * Before migrating, the result must be determined again while holding the
	 * {@link MigrationLock}: Another node may have migrated the database in the meantime.
	 * </p>
	 */
	private MigrationInfo fetchMigrations(Log log) {
		_progress = null;
		PooledConnection connection = _connectionPool.borrowReadConnection();
		try {
			Config config = getConfig();
			List<String> migrationModules = config.getModules();
			Map<String, Map<Version, MigrationConfig>> migrationScripts =
				readMigrationScripts(log, migrationModules);

			MigrationProgress progress = MigrationProgress.read(connection);
			if (progress != null) {
				_progress = progress;
				return MigrationUtil.resumedMigrations(log, migrationScripts, progress.getPlan());
			}
			return relevantMigrations(log, connection, migrationScripts, migrationModules,
				config.getAllowDowngrade());
		} catch (SQLException ex) {
			log.error("Unable to fetch migration scripts.", ex);
			return MigrationInfo.NO_MIGRATION;
		} catch (ConfigurationException | IllegalArgumentException ex) {
			log.error("Unable to read the progress of an interrupted migration from the database property '"
				+ MigrationProgress.PROGRESS_PROPERTY + "'.", ex);
			return MigrationInfo.NO_MIGRATION;
		} catch (DatabaseAccessException ex) {
			log.error("Unable to read migration scripts for modules: " + getConfig().getModules(), ex);
			return MigrationInfo.NO_MIGRATION;
		} finally {
			_connectionPool.releaseReadConnection(connection);
		}
	}

	/**
	 * Reads the migrations known to the application.
	 *
	 * @param log
	 *        The error reporter.
	 * @param migrationModules
	 *        The {@link Config#getModules() migration modules}.
	 * @return The migrations indexed by module and version, see
	 *         {@link MigrationUtil#readMigrationScripts(Log, List)}.
	 */
	protected Map<String, Map<Version, MigrationConfig>> readMigrationScripts(Log log, List<String> migrationModules)
			throws DatabaseAccessException {
		return MigrationUtil.readMigrationScripts(log, migrationModules);
	}

	/**
	 * Creates the {@link MigrationContext} for the migration processors.
	 *
	 * @param log
	 *        The log of the migration.
	 * @param connection
	 *        The connection to the database to migrate.
	 */
	protected MigrationContext createMigrationContext(Protocol log, PooledConnection connection) {
		return new MigrationContext(log, connection);
	}

	/**
	 * Determines the correct {@link MigrationConfig}s for the given migration modules.
	 * 
	 * @param connection
	 *        {@link PooledConnection} to connect to database to read current version.
	 * @param migrationScripts
	 *        All known migrations, see {@link #readMigrationScripts(Log, List)}.
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
			Map<String, Map<Version, MigrationConfig>> migrationScripts, List<String> migrationModules,
			boolean allowDowngrade) throws SQLException {
		Map<String, Version> storedVersions = MigrationUtil.readStoredVersions(connection, migrationModules);

		for (String module : getConfig().getMinimumModules()) {
			storedVersions.computeIfAbsent(module, MigrationUtil::initialVersion);
		}

		log.info("Migration modules: " + migrationModules);
		log.info("Current data version: " + storedVersions.values().stream()
			.map(v -> v.getModule() + ": " + v.getName()).collect(Collectors.joining(", ")));
		return MigrationUtil.relevantMigrations(log, migrationScripts, migrationModules, allowDowngrade,
			storedVersions);
	}

	/**
	 * Checks for schema changes between the stores database schema and the configured one and
	 * adjusts the current schema.
	 *
	 * <p>
	 * First, the layout of the existing tables is aligned with changed layout options of the
	 * configured schema (see {@link AlignTableLayoutProcessor}), so that tables for types added
	 * afterwards are created in the configured layout.
	 * </p>
	 *
	 * <p>
	 * With <code>adoptTypeChanges</code>, changed definitions of types that are part of both
	 * schemas are written to the stored schema, if they do not change the tables (see
	 * {@link AdoptTypeChangesProcessor}).
	 * </p>
	 *
	 * @param log
	 *        {@link ProcessBuilder} to write errors (and any messages) to.
	 * @param connection
	 *        {@link PooledConnection} to write changes to.
	 * @param adoptTypeChanges
	 *        Whether to adopt changed type definitions without table changes.
	 * @return Whether any changes have been written to the given connection.
	 */
	private boolean executeAutomaticSchemaMigrations(MigrationContext context, Protocol log,
			PooledConnection connection, boolean adoptTypeChanges) {
		boolean layoutChanged = alignTableLayout(context, log, connection);

		MapDifference<String, EqualityRedirect<MetaObjectName>> diff = typeDifference(context);
		boolean typesAdded = addApplicationTypes(context, log, connection, diff);
		boolean typesAdopted =
			adoptTypeChanges && adoptTypeChanges(context, log, connection, changedTypes(diff));
		return layoutChanged || typesAdded || typesAdopted;
	}

	/**
	 * Whether {@link #executeAutomaticSchemaMigrations(MigrationContext, Protocol, PooledConnection, boolean)}
	 * with type adoption would change anything.
	 *
	 * <p>
	 * The check reads the database only. It uses the same criteria as the methods doing the
	 * changes: {@link #hasLayoutChange(MigrationContext)} for
	 * {@link #alignTableLayout(MigrationContext, Protocol, PooledConnection)},
	 * {@link #lostTypes(MapDifference)} and {@link #newTypes(MapDifference)} for
	 * {@link #addApplicationTypes(MigrationContext, Protocol, PooledConnection, MapDifference)}, and
	 * {@link #changedTypes(MapDifference)} for
	 * {@link #adoptTypeChanges(MigrationContext, Protocol, PooledConnection, Set)}.
	 * </p>
	 */
	private static boolean hasAutomaticSchemaChanges(MigrationContext context) {
		if (context.getPersistentSchema() == null) {
			// No stored schema to compare with, let the migration under the lock handle it.
			return true;
		}
		if (hasLayoutChange(context)) {
			return true;
		}
		MapDifference<String, EqualityRedirect<MetaObjectName>> diff = typeDifference(context);
		return !lostTypes(diff).isEmpty() || !newTypes(diff).isEmpty() || !changedTypes(diff).isEmpty();
	}

	/**
	 * Types of the stored schema that are not part of the configured schema.
	 */
	private static Map<String, EqualityRedirect<MetaObjectName>> lostTypes(
			MapDifference<String, EqualityRedirect<MetaObjectName>> diff) {
		return diff.entriesOnlyOnLeft();
	}

	/**
	 * Types of the configured schema that are not part of the stored schema.
	 */
	private static Map<String, EqualityRedirect<MetaObjectName>> newTypes(
			MapDifference<String, EqualityRedirect<MetaObjectName>> diff) {
		return diff.entriesOnlyOnRight();
	}

	/**
	 * Names of the types defined differently in the stored and the configured schema.
	 */
	private static Set<String> changedTypes(MapDifference<String, EqualityRedirect<MetaObjectName>> diff) {
		return diff.entriesDiffering().keySet();
	}

	/**
	 * The difference between the types of the stored and the configured schema.
	 */
	private static MapDifference<String, EqualityRedirect<MetaObjectName>> typeDifference(
			MigrationContext context) {
		Map<String, MetaObjectName> applicationTypes = context.getApplicationSchema().getMetaObjects().getTypes();
		Map<String, MetaObjectName> persistentTypes = context.getPersistentSchema().getMetaObjects().getTypes();
		return Maps.difference(wrap(persistentTypes), wrap(applicationTypes));
	}

	/**
	 * Aligns the layout of the existing tables with the layout options of the configured schema,
	 * if they differ from the ones of the stored schema.
	 *
	 * @return Whether any changes have been written to the given connection.
	 */
	private boolean alignTableLayout(MigrationContext context, Protocol log, PooledConnection connection) {
		if (!hasLayoutChange(context)) {
			return false;
		}
		execute(context, log, connection, TypedConfiguration.newConfigItem(AlignTableLayoutProcessor.Config.class));
		return true;
	}

	/**
	 * Whether the layout options of the stored schema differ from the ones of the configured
	 * schema.
	 *
	 * @see AlignTableLayoutProcessor#hasLayoutChange(SchemaConfiguration, SchemaConfiguration)
	 */
	private static boolean hasLayoutChange(MigrationContext context) {
		SchemaConfiguration persistentSchema = context.getPersistentSchema();
		return persistentSchema != null
			&& AlignTableLayoutProcessor.hasLayoutChange(persistentSchema, context.getApplicationSchema());
	}

	/**
	 * Creates tables for the types of the configured schema that are not part of the stored
	 * schema.
	 *
	 * @param diff
	 *        The {@link #typeDifference(MigrationContext) difference} between the types of the
	 *        stored and the configured schema.
	 * @return Whether any changes have been written to the given connection.
	 */
	private boolean addApplicationTypes(MigrationContext context, Protocol log, PooledConnection connection,
			MapDifference<String, EqualityRedirect<MetaObjectName>> diff) {
		Map<String, MetaObjectName> lostTypes = unwrap(lostTypes(diff));
		recoverMetaDefinitions(log, lostTypes, lostTypes.keySet());

		Map<String, EqualityRedirect<MetaObjectName>> newTypes = newTypes(diff);
		if (!newTypes.isEmpty()) {
			AddApplicationTypesProcessor.Config<?> addConfig =
				TypedConfiguration.newConfigItem(AddApplicationTypesProcessor.Config.class);
			addConfig.getSchema().getTypes().putAll(unwrap(newTypes));

			MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
			migration.getProcessors().add(addConfig);

			Logger.info("Updating database schema according to the following migration: \n" + migration,
				MigrationService.class);

			execute(context, log, connection, addConfig);
			return true;
		}
		return false;
	}

	/**
	 * Writes the changed definitions of the given types to the stored schema, if they do not
	 * change the tables, and reports a warning for the others.
	 *
	 * @param changedTypes
	 *        Names of the types defined differently in the stored and the configured schema.
	 * @return Whether any changes have been written to the given connection.
	 *
	 * @see AdoptTypeChangesProcessor
	 */
	private boolean adoptTypeChanges(MigrationContext context, Protocol log, PooledConnection connection,
			Set<String> changedTypes) {
		if (changedTypes.isEmpty()) {
			return false;
		}
		AdoptTypeChangesProcessor.Config adoptConfig =
			TypedConfiguration.newConfigItem(AdoptTypeChangesProcessor.Config.class);
		adoptConfig.setTypes(new ArrayList<>(changedTypes));
		execute(context, log, connection, adoptConfig);
		return true;
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
			FileManager fileManager = FileManager.getInstance();
			String schemaResource = null;
			String configResource = null;
			try {
				int id = 1;
				String resourceName;
				String baseName;
				File newFile;
				do {
					baseName = "inapp-schema-" + id;
					resourceName = ModuleLayoutConstants.AUTOCONF_FOLDER_RESOURCE + baseName + ".meta.xml";
					newFile = fileManager.getIDEFile(resourceName);
					id++;
				} while (newFile.exists());
				schemaResource = resourceName;
				File schemaFile = storeResource(fileManager, inappSchema, schemaResource);

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
				configResource = ModuleLayoutConstants.AUTOCONF_FOLDER_RESOURCE + baseName + ".config.xml";
				storeResource(fileManager, configFragment, configResource);

				log.info("Created schema configuration with recovered types: " + schemaFile.getAbsolutePath());

				// Re-load configuration to make sure, the updated config is found later on.
				TLServiceUtils.reloadConfigurations();
			} catch (IOException | XMLStreamException | SAXException ex) {
				log.info("Cannot create schema add-on configuration: " + ex.getMessage(), Log.WARN);

				// Clean up to avoid an inconsistent state.
				deleteResource(fileManager, schemaResource);
				deleteResource(fileManager, configResource);
			}
		}
	}

	/**
	 * Creates a new configuration file for the given resource and makes it visible to the given
	 * {@link FileManager}.
	 *
	 * @return The created file.
	 */
	private File storeResource(FileManager fileManager, ConfigurationItem conf, String resourceName)
			throws XMLStreamException, IOException, FileNotFoundException, SAXException {
		File file = fileManager.getIDEFile(resourceName);
		file.getParentFile().mkdirs();
		try (var out = new FileOutputStream(file)) {
			try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
				try (ConfigurationWriter w = new ConfigurationWriter(writer)) {
					w.write(MORepositoryBuilder.ROOT_TAG, MetaObjectsConfig.class, conf);
				}
			}
		}
		XMLPrettyPrinter.normalizeFile(file);
		fileManager.refresh(resourceName);
		return file;
	}

	/**
	 * Deletes the file of the given resource, if any, and makes the deletion visible to the given
	 * {@link FileManager}.
	 */
	private void deleteResource(FileManager fileManager, String resourceName) {
		if (resourceName == null) {
			return;
		}
		fileManager.getIDEFile(resourceName).delete();
		fileManager.refresh(resourceName);
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

	private static <K, V> Map<K, V> unwrap(Map<K, EqualityRedirect<V>> map) {
		Map<K, V> result = new LinkedHashMap<>();
		for (java.util.Map.Entry<K, EqualityRedirect<V>> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue().getValue());
		}
		return result;
	}

	@Override
	protected void startUp() {
		super.startUp();
		LogProtocol log = new LogProtocol(MigrationService.class);
		if (!isMigrationEnabled(log)) {
			log.checkErrors();
			_automaticMigration = false;
			_migrationInfo = MigrationInfo.NO_MIGRATION;
			return;
		}

		// Check without lock whether this start changes the database at all. This is not the case
		// for an ordinary restart, which therefore does not touch the migration lock.
		_migrationInfo = fetchMigrations(log);
		log.checkErrors();
		if (!hasPendingChanges(log)) {
			log.info("Database is up to date, no migration required.");
			return;
		}

		/* The migration runs before the cluster startup token is acquired (the token requires the
		 * persistency layer, i.e. a migrated database). The migration lock prevents nodes starting
		 * concurrently from migrating the same database at the same time. */
		try (MigrationLock lock = acquireLock(log)) {
			_migrationLock = lock;

			// Determine the pending migrations again after acquiring the lock: A node that had to
			// wait finds the database migrated by the other node.
			_migrationInfo = fetchMigrations(log);
			log.checkErrors();

			migrate(log);
		} finally {
			_migrationLock = null;
		}
	}

	/**
	 * Whether {@link #migrate(Protocol)} would write to the database for the current
	 * {@link #fetchMigrations(Log) migration info}.
	 *
	 * <p>
	 * This is the case for configured migrations (including the ones of an interrupted migration,
	 * see {@link MigrationProgress}), a downgrade, and for automatic changes at a
	 * start without migrations: a layout or type difference between the stored and the configured
	 * schema (see {@link #hasAutomaticSchemaChanges(MigrationContext)}) or a module without stored
	 * version (see {@link #missingVersions(Protocol, PooledConnection)}). The check reads the
	 * database only.
	 * </p>
	 */
	private boolean hasPendingChanges(Protocol log) {
		if (!_migrationInfo.nothingToDo()) {
			return true;
		}
		if (!_automaticMigration) {
			return false;
		}
		PooledConnection connection = _connectionPool.borrowReadConnection();
		try {
			MigrationContext context = createMigrationContext(log, connection);
			if (hasAutomaticSchemaChanges(context)) {
				return true;
			}
			List<Version> missingVersions = missingVersions(log, connection);
			log.checkErrors();
			return !missingVersions.isEmpty();
		} finally {
			_connectionPool.releaseReadConnection(connection);
		}
	}

	private MigrationLock acquireLock(Protocol log) {
		try {
			return MigrationLock.acquire(log, _connectionPool);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw log.fatal("Interrupted while waiting for the migration lock.", ex);
		}
	}

	/**
	 * Callback invoked, after all modules are up and running.
	 */
	public void applicationStarted() {
		List<MigrationConfig> migrationsWithActions =
			_migrationInfo.getMigrations().stream().filter(m -> !m.getStartupActions().isEmpty()).toList();
		if (!migrationsWithActions.isEmpty()) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			for (var m : migrationsWithActions) {
				Logger.info("Performing startup actions from: " + m.getVersion().getModule() + ": "
						+ m.getVersion().getName(), MigrationService.class);
				for (var actionConfig : m.getStartupActions()) {
					StartupAction action = TypedConfigUtil.createInstance(actionConfig);
					try (Transaction tx =
						kb.beginTransaction(I18NConstants.PERFORMED_STARTUP_ACTION__NAME
							.fill(actionConfig.getImplementationClass().getSimpleName()))) {
						action.perform();
						tx.commit();
					}
				}
			}
		}
	}

	private void migrate(Protocol log) {
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			MigrationContext context = createMigrationContext(log, connection);

			if (_migrationInfo.nothingToDo()) {
				log.info("No configured migrations.");

				if (_automaticMigration) {
					boolean changes = executeAutomaticSchemaMigrations(context, log, connection, true);

					// Check that everything is ok.
					log.checkErrors();

					boolean versionsStored = storeMissingVersions(log, connection);
					if (changes || versionsStored) {
						commitConnection(log, connection, "Unable to commit automatic schema and version changes.");
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

			migrate(context, log, connection);

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

	/**
	 * Stores the maximal version of each migration module that has no version stored in the
	 * database.
	 * 
	 * <p>
	 * A module without stored version is considered new to the application, its migrations are
	 * skipped. Storing its current version makes the next migration of the module run.
	 * </p>
	 * 
	 * @return Whether some version was written. The connection is not committed.
	 */
	private boolean storeMissingVersions(Protocol log, PooledConnection connection) {
		List<Version> missingVersions = missingVersions(log, connection);
		MigrationUtil.updateStoredVersions(log, connection, missingVersions);
		log.checkErrors();
		return !missingVersions.isEmpty();
	}

	/**
	 * The maximal versions of the migration modules that have no version stored in the database.
	 *
	 * @return The versions {@link #storeMissingVersions(Protocol, PooledConnection)} stores, the
	 *         empty list after reporting an error, if the stored versions cannot be read.
	 */
	private List<Version> missingVersions(Protocol log, PooledConnection connection) {
		Map<String, Version> storedVersions;
		try {
			storedVersions = MigrationUtil.readStoredVersions(connection, getConfig().getModules());
		} catch (SQLException ex) {
			log.error("Unable to read stored versions.", ex);
			return Collections.emptyList();
		}
		return MigrationUtil.versionsOfUnstoredModules(MigrationUtil.maximalVersions(log), storedVersions);
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

	/**
	 * Executes the steps of the configured migrations that are not yet completed.
	 *
	 * <p>
	 * The steps are: each pre-processor and each processor of the migrations, the rebuild of the
	 * {@link com.top_logic.knowledge.service.db2.RevisionXref} entries together with the automatic
	 * schema adaption, the replay (if required), storing the new versions together with the schema
	 * baseline, and the post-processors of all migrations in a {@link KnowledgeBase} transaction.
	 * Each step is committed together with the {@link MigrationProgress}.
	 * </p>
	 */
	private void migrate(MigrationContext context, Protocol log, PooledConnection connection) {
		DataMigration migration = new DataMigration(_migrationInfo);
		migration.setBufferChunkSize(getConfig().getBufferChunkSize());
		migration.build(log);

		// Note: SQL migration (migration processors) must happen before the persistency layer is
		// accessed to allow upgrading the baseline of the persistency layer configuration in those
		// processors.
		List<MigrationStep> steps = new ArrayList<>(migration.createSQLSteps(context, log, connection));

		steps.add(MigrationStep.create("rebuild of xref entries and automatic schema adaption", completion -> {
			context.revalidateXRef(log, connection);

			// The migration processors work on the table layout of the stored schema, which the
			// state of the context is derived from. The layout is aligned with the configuration
			// only afterwards, including the tables the processors have created.
			executeAutomaticSchemaMigrations(context, log, connection, false);
		}));

		if (migration.isReplayRequired()) {
			steps.add(MigrationStep.create("replay of the history", completion -> {
				KnowledgeBase kb = createKB(log);
				if (kb != null) {
					replayHistory(log, connection, migration, kb, completion);
				}
			}));
		}

		steps.add(MigrationStep.create("storing versions and schema baseline", completion -> {
			MigrationUtil.updateStoredVersions(log, connection, migration.getNewVersion().values());
			storeMissingVersions(log, connection);

			// Update stored schema to be compatible with the current application configuration.
			StoreTypeConfiguration store = TypedConfigUtil.createInstance(StoreTypeConfiguration.Config.class);
			store.doMigration(context, log, connection);
		}));

		if (hasPostProcessors()) {
			steps.add(MigrationStep.createInKnowledgeBase("post-processors", completion -> {
				executePostProcessors(log, completion);
			}));
		}

		MigrationProgress progress = _progress;
		if (progress == null) {
			progress = MigrationProgress.create(MigrationUtil.versions(_migrationInfo.getMigrations()));
		}
		progress.execute(log, connection, context, steps);
		_progress = null;
	}

	private boolean hasPostProcessors() {
		return _migrationInfo.getMigrations().stream().anyMatch(m -> !m.getPostProcessors().isEmpty());
	}

	/**
	 * Executes the post-processors of all migrations in a single {@link KnowledgeBase} transaction,
	 * in which the completion of the step is recorded.
	 */
	private void executePostProcessors(Protocol log, MigrationStep.Completion completion) throws SQLException {
		KnowledgeBase kb = null;
		Transaction tx = null;
		try {
			for (MigrationConfig migration : _migrationInfo.getMigrations()) {
				for (PolymorphicConfiguration<? extends MigrationPostProcessor> processorConf : migration
					.getPostProcessors()) {
					MigrationPostProcessor processor = TypedConfigUtil.createInstance(processorConf);

					if (kb == null) {
						kb = createKB(log);
						log.checkErrors();
						tx = kb.beginTransaction(
							I18NConstants.PERFORMED_MIGRATION__NAME.fill(migration.getVersion().getModule() + ": "
								+ migration.getVersion().getName()));
					}

					processor.afterMigration(log, kb);
				}
			}

			if (tx != null && !log.hasErrors()) {
				completion.record(KBUtils.getCurrentContext(kb).getConnection());
				tx.commit();
			}
		} finally {
			if (tx != null) {
				tx.rollback();
			}
		}
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
			KnowledgeBase dumpingKB, MigrationStep.Completion completion) {
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
			DataMigration.dropAndRecreateDatabase(connection, kbConfig, in);

			// The properties table holding the migration lock and the migration progress has been
			// dropped and re-created.
			_migrationLock.restore();
			completion.record(connection);
			connection.commit();

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
