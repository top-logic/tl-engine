/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.shared.collection.CyclicDependencyException;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.tooling.ModuleLayout;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.service.db2.migration.DumpReader;
import com.top_logic.knowledge.service.migration.model.MigrationRef;
import com.top_logic.knowledge.service.migration.model.VersionID;

/**
 * Util class holding service methods to create {@link MigrationConfig}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationUtil {

	/** Suffix of migration files: .migration.xml */
	public static final String MIGRATION_FILE_SUFFIX = ".migration" + FileUtilities.XML_FILE_ENDING;

	private static final String ROOT_TAG_NAME = "migration";

	/** Path to migration base bath relative to {@link ModuleLayout#getWebappDir()}. */
	public static final String MIGRATION_BASE_PATH =
		ModuleLayoutConstants.WEB_INF_DIR + '/' + "kbase" + '/' + "migration";

	/** Path to the folder containing {@link MigrationConfig migration scripts}. */
	public static final String MIGRATION_BASE_RESOURCE = '/' + MIGRATION_BASE_PATH + '/';

	/**
	 * Namespace for the properties used in {@link DBProperties} to store version for modules.
	 * 
	 * <p>
	 * In the {@link DBProperties} each module serves as property(-suffix) to store the current
	 * version for this module.
	 * </p>
	 */
	public static final String COMMON_KEY_PREFIX = "databaseVersion.";

	/**
	 * Reads the {@link MigrationConfig} for the given version.
	 * 
	 * @param log
	 *        {@link Log} to write errors to.
	 * @param version
	 *        The version to get migration for. This version is used to identify the correct
	 *        resource to read configuration from.
	 * @return The migration for this resource. May be <code>null</code>, e.g. when there is no
	 *         migration for the version or an error occurs during creation of
	 *         {@link MigrationConfig}.
	 */
	public static MigrationConfig readMigrationConfig(Log log, Version version) {
		InstantiationContext context = InstantiationContext.toContext(log);
		try {
			ConfigurationDescriptor configDescriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
			Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName =
				Collections.<String, ConfigurationDescriptor> singletonMap(ROOT_TAG_NAME, configDescriptor);
			ConfigurationReader reader = new ConfigurationReader(context, globalDescriptorsByLocalName);
			MigrationConfig migrationConfig =
				(MigrationConfig) reader.setSources(FileManager.getInstance().getData(getResource(version))).read();
			Version storedVersion = migrationConfig.getVersion();
			if (!version.equals(storedVersion)) {
				StringBuilder differentVersion = new StringBuilder();
				differentVersion.append("Migration config for version '");
				differentVersion.append(toString(version));
				differentVersion.append("' has different stored version: ");
				differentVersion.append(toString(storedVersion));
				log.error(differentVersion.toString());
			}
			return migrationConfig;
		} catch (ConfigurationException ex) {
			log.error("Unable to create migration config.", ex);
			return null;
		}
	}

	private static String getResource(Version version) {
		return MIGRATION_BASE_RESOURCE + version.getModule() + '/' + version.getName() + MIGRATION_FILE_SUFFIX;
	}

	/**
	 * Reads the {@link MigrationConfig}s of a certain module.
	 */
	public static List<MigrationConfig> readMigrations(Protocol log, String migrationModule) {
		FileManager fileManager = FileManager.getInstance();
		String migrationPath = MigrationUtil.MIGRATION_BASE_RESOURCE + migrationModule + '/';
		Set<String> migrationResources = fileManager.getResourcePaths(migrationPath);
		if (migrationResources.isEmpty()) {
			return Collections.emptyList();
		}

		List<MigrationConfig> migrations = new ArrayList<>();
		for (String migrationResource : migrationResources) {
			if (!migrationResource.endsWith(MIGRATION_FILE_SUFFIX)) {
				continue;
			}
			String versionName = migrationResource.substring(migrationPath.length());
			MigrationConfig migrationConfig = readMigration(log, migrationModule, versionName);
			migrations.add(migrationConfig);
		}
		return migrations;
	}

	/**
	 * Reads the {@link MigrationConfig} for the given module and version name.
	 */
	public static MigrationConfig readMigration(Protocol log, String migrationModule, String versionName) {
		Version migrationVersion = getVersion(migrationModule, versionName);

		BufferingProtocol bufferingProtocol = new BufferingProtocol(log);
		MigrationConfig migrationConfig = readMigrationConfig(bufferingProtocol, migrationVersion);
		try {
			bufferingProtocol.checkErrors();
			// Do not add migration when error occurred.
		} catch (Exception ex) {
			log.error("Unable to get MigrationConfig from migration file " + versionName, ex);
		}
		return migrationConfig;
	}

	/**
	 * Dumps the given {@link MigrationConfig} to the given file.
	 * 
	 * @param out
	 *        The file to dump migration to.
	 * @param migration
	 *        The {@link MigrationConfig} to dump.
	 * 
	 * @see #dumpMigrationConfig(Writer, MigrationConfig)
	 */
	public static void dumpMigrationConfig(File out, MigrationConfig migration)
			throws XMLStreamException, IOException, FileNotFoundException, SAXException {
		try (FileOutputStream fos = new FileOutputStream(out);
				OutputStreamWriter osw = new OutputStreamWriter(fos, StringServices.CHARSET_UTF_8)) {
			dumpMigrationConfig(osw, migration);
		}
		XMLPrettyPrinter.normalizeFile(out);
	}

	/**
	 * Dumps the given {@link MigrationConfig} to the given file.
	 * 
	 * @param out
	 *        The writer to dump migration to.
	 * @param migration
	 *        The {@link MigrationConfig} to dump.
	 */
	public static void dumpMigrationConfig(Writer out, MigrationConfig migration) throws XMLStreamException {
		ConfigurationWriter configurationWriter = new ConfigurationWriter(out);

		// Note: Do not pass MigrationConfig.class as expected type during write to create a
		// reference to the configuration type in the generated file. This better links the
		// configuration to its schema.
		configurationWriter.write(ROOT_TAG_NAME, ConfigurationItem.class, migration);
	}

	/**
	 * Service method to get the {@link #version(MigrationConfig) version} of the given
	 * {@link MigrationConfig}s in the same order.
	 */
	public static List<Version> versions(Collection<? extends MigrationConfig> migrations) {
		return migrations.stream().map(MigrationUtil::version).collect(Collectors.toList());
	}

	/**
	 * Service method to get the version of the given {@link MigrationConfig}.
	 */
	public static Version version(MigrationConfig migration) {
		return migration.getVersion();
	}

	/**
	 * Service method to create new {@link Version}.
	 * 
	 * @param module
	 *        Value of {@link Version#getModule()}.
	 * @param versionName
	 *        Value of {@link Version#getName()}
	 */
	public static Version newVersion(String module, String versionName) {
		Version version = TypedConfiguration.newConfigItem(Version.class);
		version.setName(versionName);
		version.setModule(module);
		return version;
	}

	/**
	 * Service method to get better {@link Object#toString()} of {@link Version}.
	 * 
	 * @param version
	 *        May be <code>null</code>.
	 */
	public static String toString(Version version) {
		if (version == null) {
			return "No version";
		}
		return version.getName() + '(' + version.getModule() + ')';
	}

	/**
	 * Service method to get better {@link Object#toString()} of collection of {@link Version}.
	 * 
	 * @param versions
	 *        Must not <code>null</code>.
	 */
	public static String toString(Iterable<? extends Version> versions) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[");
		boolean first = true;
		for (Version v : versions) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(toString(v));
		}
		buffer.append("]");
		return buffer.toString();
	}

	/**
	 * Service method to get better {@link Object#toString()} of {@link Map} containing
	 * {@link Version}.
	 * 
	 * @param versions
	 *        Must not be <code>null</code>.
	 */
	public static String toString(Map<?, ?> versions) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("[");
		boolean first = true;
		for (Entry<?, ?> v : versions.entrySet()) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			if (v.getKey() instanceof Version) {
				buffer.append(toString((Version) v.getKey()));
			} else {
				buffer.append(v.getKey());
			}
			buffer.append('=');
			if (v.getValue() instanceof Version) {
				buffer.append(toString((Version) v.getValue()));
			} else {
				buffer.append(v.getValue());
			}
		}
		buffer.append("]");
		return buffer.toString();
	}

	/**
	 * Returns all known migration modules. The module with the highest index is the the top level
	 * migration module.
	 */
	public static List<String> getMigrationModules() throws ConfigurationException {
		MigrationService.Config migrationConf =
			(MigrationService.Config) ApplicationConfig.getInstance().getServiceConfiguration(MigrationService.class);
		return migrationConf.getModules();
	}

	/**
	 * Returns all potential migration files in the given migration folder.
	 */
	public static File[] migrationScripts(File migrationFolder) throws IOException {
		return FileUtilities.listFiles(migrationFolder, MigrationUtil::isMigrationFile);
	}

	private static boolean isMigrationFile(File file) {
		return file.getName().endsWith(MIGRATION_FILE_SUFFIX);
	}

	/**
	 * Returns the {@link Version} derived from the the name and parent of the given
	 * {@link DataAccessProxy} representing an XML file.
	 */
	public static Version getVersion(String migrationModule, String migrationFile) throws DatabaseAccessException {
		if (!migrationFile.endsWith(MIGRATION_FILE_SUFFIX)) {
			throw new IllegalArgumentException(
				migrationFile + " does not represent an an XML file, therefore not a migration script.");
		}

		String versionName = migrationFile.substring(0, migrationFile.length() - MIGRATION_FILE_SUFFIX.length());
		return newVersion(migrationModule, versionName);
	}

	/**
	 * Returns the {@link Version} derived from the the name and parent of the given XML file.
	 */
	public static Version getVersion(File migrationFile) {
		String fileName = migrationFile.getName();
		String version;
		if (fileName.endsWith(MIGRATION_FILE_SUFFIX)) {
			version = fileName.substring(0, fileName.length() - MIGRATION_FILE_SUFFIX.length());
		} else {
			throw new IllegalArgumentException(
				migrationFile + " is not an XML file, therefore not a migration script.");
		}
		String moduleName = migrationFile.getParentFile().getName();
		return newVersion(moduleName, version);
	}

	/**
	 * Returns all versions derived from the given {@link MigrationConfig}s.
	 * 
	 * <p>
	 * Creates a correct mapping and calls {@link #getAllVersions(Log, Map)}.
	 * </p>
	 * 
	 * @see #getAllVersions(Log, Map)
	 */
	public static List<Version> getAllVersions(Log log, Collection<MigrationConfig> migrations) {
		return getAllVersions(log,
			migrations.stream().collect(Collectors.toMap(MigrationConfig::getVersion, Function.identity())));
	}

	/**
	 * Returns all versions derived from the given {@link MigrationConfig}s.
	 * 
	 * <p>
	 * The returned array contains all versions and the dependent versions, ordered by version. The
	 * version with higher index is larger than the version with lower index. If the "first"
	 * migration config is present, the returned array has same size as migrations, otherwise size
	 * is one larger, i.e. <code>null</code> is not not contained in the returned array.
	 * </p>
	 * 
	 * @param migrations
	 *        Migrations for the same module. The migration must be complete such that for each
	 *        migration also the {@link MigrationConfig} for the dependent version is contained in
	 *        the {@link Map}. Only for the "first" migration no {@link MigrationConfig} for the
	 *        dependent version may be absent. (This is necessary because old migrations may be
	 *        deleted.)
	 */
	public static List<Version> getAllVersions(Log log, Map<Version, MigrationConfig> migrations) {
		switch (migrations.size()) {
			case 0:
				return Collections.emptyList();
			case 1:
				Entry<Version, MigrationConfig> next = migrations.entrySet().iterator().next();
				if (!next.getKey().equals(next.getValue().getVersion())) {
					throw new IllegalArgumentException(
						"Inconsistent migration mapping: Version '" + toString(next.getKey())
							+ "' is mapped to migration for version '" + toString(next.getValue().getVersion()) + "'.");
				}
				return Collections.singletonList(next.getValue().getVersion());
			default:
				return getAllVersions0(log, migrations);
		}

	}

	private static List<Version> getAllVersions0(Log log, Map<Version, MigrationConfig> migrations) {
		Map<MigrationConfig, MigrationConfig> followUpMigrations = new HashMap<>();

		String migrationModule = null;
		MigrationConfig migrationWithoutBaseMigration = null;
		Version baseVersionWithoutMigration = null;
		for (Entry<Version, MigrationConfig> migration : migrations.entrySet()) {
			Version version = migration.getKey();
			if (!version.equals(migration.getValue().getVersion())) {
				throw new IllegalArgumentException(
					"Inconsistent migration mapping: Version '" + toString(version)
						+ "' is mapped to migration for version '" + toString(migration.getValue().getVersion())
						+ "'.");
			}

			if (migrationModule == null) {
				migrationModule = version.getModule();
			} else if (!migrationModule.equals(version.getModule())) {
				throw new IllegalArgumentException("Not all migrations have the same module. Expected: "
					+ migrationModule + ", Version: " + toString(version));
			}

			Version baseVersion = migration.getValue().getDependencies().get(migrationModule);
			MigrationConfig baseMigration;
			if (baseVersion != null) {
				baseMigration = migrations.get(baseVersion);
				if (baseMigration == null) {
					/* No migration given for base version. This may happen when old migration files
					 * were deleted. */
					migrationWithoutBaseMigration = migration.getValue();
					baseVersionWithoutMigration = baseVersion;
				}
			} else {
				migrationWithoutBaseMigration = migration.getValue();
				baseMigration = null;
			}
			MigrationConfig clash = followUpMigrations.put(baseMigration, migration.getValue());
			if (clash != null) {
				log.error("Multiple versions with same base version: " + toString(migration.getValue().getVersion())
					+ " and " + toString(clash.getVersion()) + " bases both on version " + toString(baseVersion));
			}
		}
		if (migrationWithoutBaseMigration == null) {
			List<MigrationConfig> cycle = new ArrayList<>();
			MigrationConfig tmp = followUpMigrations.keySet().iterator().next();
			cycle.add(tmp);
			int cycleStart = -1;
			while (true) {
				tmp = followUpMigrations.get(tmp);
				cycleStart = cycle.indexOf(tmp);
				cycle.add(tmp);
				if (cycleStart > -1) {
					break;
				}
			}
			// Each migration is also a base migration.
			log.error("Cyclic dependencies in version dependencies in module "
				+ migrationModule + ": " + toString(versions(cycle.subList(cycleStart, cycle.size()))));
			return Collections.emptyList();
		}

		List<MigrationConfig> sortedVersions = new ArrayList<>();

		MigrationConfig tmp = null;
		assert followUpMigrations.containsKey(
			tmp) : "null is included as key in the set, because otherwise 'migrationWithoutBase' would be null and calculation would have returned before.";
		while (true) {
			MigrationConfig followUp = followUpMigrations.remove(tmp);
			if (followUp == null) {
				if (followUpMigrations.isEmpty()) {
					// All migrations processed.
					break;
				}
				log.error("No follow-up version for " + toString(version(tmp)) + " found in "
					+ toString(versions(followUpMigrations.keySet())));
				return Collections.emptyList();
			}
			sortedVersions.add(followUp);
			tmp = followUp;
		}

		checkNoLostDependency(log, sortedVersions);

		List<Version> result;
		if (baseVersionWithoutMigration == null) {
			result = versions(sortedVersions);
		} else {
			result = new ArrayList<>(sortedVersions.size() + 1);
			result.add(baseVersionWithoutMigration);
			for (int i = 0; i < sortedVersions.size(); i++) {
				result.add(version(sortedVersions.get(i)));
			}
		}
		return result;
	}

	/**
	 * Checks that the modules on which {@link MigrationConfig} at position <code>index + 1</code>
	 * bases, is a super set of the modules on which migration at position <code>index</code> bases.
	 */
	private static void checkNoLostDependency(Log log, List<MigrationConfig> sortedMigrations) {
		Set<String> dependentModules = Collections.emptySet();
		for (int i = 0; i < sortedMigrations.size(); i++) {
			Set<String> tmp = dependentModules(sortedMigrations.get(i));
			if (!tmp.containsAll(dependentModules)) {
				Set<String> missingDependenModules = new HashSet<>(dependentModules);
				missingDependenModules.removeAll(tmp);
				log.error("Lost module dependency: Migration for " + toString(version(sortedMigrations.get(i - 1)))
					+ " bases on modules " + dependentModules + ". The follow-up migration (migration for "
					+ toString(version(sortedMigrations.get(i))) + ")  bases on modules: " + tmp
					+ ". Missing module dependencies: " + missingDependenModules);
			}
			dependentModules = tmp;
		}
	}

	private static Set<String> dependentModules(MigrationConfig migration) {
		return migration.getDependencies().keySet();
	}

	/**
	 * Returns the {@link MigrationConfig} in correct order which must be applied to be up to date.
	 * 
	 * @param migrationScripts
	 *        All migration scripts indexed by the module. The value for the module is a mapping
	 *        from all known {@link Version}'s to the corresponding {@link MigrationConfig}.
	 * @param versionByModule
	 *        Mapping of the module name to all versions in the module in version order. Value of
	 *        {@link #getVersionsByModule(Log, Map)}.
	 * @param currentVersions
	 *        The current versions of the application, indexed by module name. If for a module no
	 *        {@link Version} is given, it is assumed that all migration for that module must be
	 *        applied.
	 * @param modules
	 *        All known database modules. The order of the module is the dependency order, i.e. when
	 *        <code>module1</code> depends on <code>module2</code>, <code>module2</code> appears
	 *        before <code>module1</code> in the array.
	 * 
	 * @deprecated Use {@link #getMigrations(Map, Map, Map, List)} This variant might produce an
	 *             invalid migration order in complex situations.
	 */
	@Deprecated
	public static List<MigrationConfig> getRelevantMigrations(
			final Map<String, Map<Version, MigrationConfig>> migrationScripts,
			final Map<String, List<Version>> versionByModule,
			final Map<String, Version> currentVersions,
			final List<String> modules) {
		Version[] sortedVersions = allVersionsUnsorted(migrationScripts);
		Arrays.sort(sortedVersions, versionCompare(migrationScripts, versionByModule, currentVersions, modules));

		Set<Version> newVersions = getNewVersions(new HashMap<>(versionByModule), currentVersions);

		int matchingItemCount = ArrayUtil.filterInline(sortedVersions, newVersions::contains);

		List<MigrationConfig> migrations = new ArrayList<>(matchingItemCount);
		for (int i = 0; i < matchingItemCount; i++) {
			Version v = sortedVersions[i];
			MigrationConfig migration = migrationScripts.get(v.getModule()).get(v);
			if (migration == null) {
				// This condition should have been checked before, when it is tested that all
				// versions found in the database are at least referenced in some migration script.
				throw new UnreachableAssertion(
					"There is no longer a migration for version '" + toString(v) + "' that has not yet been applied.");
			}
			migrations.add(migration);
		}
		return migrations;
	}

	/**
	 * The {@link MigrationConfig migrations} to perform in the order in which they must be applied
	 * to update the system.
	 * 
	 * <p>
	 * The migrations span a dependency graph, since each migration is based on the last migration
	 * in each dependent module at the time the migration was created. Unfortunately, these recorded
	 * dependencies are not sufficient to compute an acceptable migration order by simply applying
	 * topological sort to the dependency graph. The problem occurs, when a migration is created in
	 * a base module after some migration in a dependent module.
	 * </p>
	 * 
	 * <p>
	 * The situation is shown in the following figure. The blue arrows represent dependencies that
	 * are recorded in the migration files. Here, B0 was created before A0, because, otherwise, the
	 * dependency B0 -> A0 would have been recorded. B1 was created last, since, both dependencies
	 * B1 -> B0 and B1 -> A0 have been recorded.
	 * </p>
	 * 
	 * <img src="./01-simple-dependency.svg"/>
	 * 
	 * <p>
	 * When simply topologically sorting the dependency graph consisting of the blue arrows, both
	 * migration strategies could occur: A0, B0, B1 and B0, A0, B1. But only the latter is correct,
	 * because of the missing recorded dependency B0 -> A0.
	 * </p>
	 * 
	 * <p>
	 * To fix this situation, additional synthesized dependencies (red arrows) according to the
	 * following rules must be added to the graph before topologically sorting:
	 * </p>
	 *
	 * <h2>Dependency inversion</h2>
	 * 
	 * <img src="./02-rule-dependency-inversion.svg"/>
	 * 
	 * <p>
	 * For each cross-module dependency B2 -> A2, introduce a new dependency
	 * successor(dependency(predecessor(B2))) -> predecessor(B2), if dependency(predecessor(B2)) !=
	 * A2.
	 * </p>
	 * 
	 * <h2>Module inversion</h2>
	 * 
	 * <img src="./03-rule-module-dependencies.svg"/>
	 * 
	 * <p>
	 * For each of the modules M1, M2, where M2 depends on M1, add a dependency A1 -> B2 to a
	 * version A1 in M1 and B2 in M2 so that B2 is the head version in M2 and A2 is the latest
	 * version in M1 that is not referenced by any version in M2.
	 * </p>
	 * 
	 * @param migrationsByModule
	 *        All migrations indexed by their module. The value is a mapping from all known
	 *        {@link Version}'s of the module to the corresponding {@link MigrationConfig} that
	 *        needs to be executed to establish this version.
	 * @param versionsByModule
	 *        Totally ordered versions for each module. This value is computed by calling
	 *        {@link #getVersionsByModule(Log, Map)}.
	 * @param dbVersion
	 *        The current version of the application as store in the database. The version consists
	 *        of the head version for each module. If for a module no {@link Version} is given, it
	 *        is assumed that this module is a new dependency of the software product and therefore
	 *        no migrations for that module have to be applied. The only exception to this rule is
	 *        an empty database version. This is interpreted as the version from before introducing
	 *        automatic migration. This initial version causes all migrations to be applied.
	 * @param applicationModules
	 *        All known application modules in dependency order. When <code>module2</code> depends
	 *        on <code>module1</code>, <code>module1</code> appears before <code>module2</code> in
	 *        the list.
	 */
	public static List<MigrationConfig> getMigrations(
			final Map<String, Map<Version, MigrationConfig>> migrationsByModule,
			final Map<String, List<Version>> versionsByModule,
			final Map<String, Version> dbVersion,
			final List<String> applicationModules) {

		// Index all known migrations be their version.
		Map<VersionID, MigrationRef> migrations = new HashMap<>();
		for (Map<Version, MigrationConfig> moduleMigrations : migrationsByModule.values()) {
			for (MigrationConfig config : moduleMigrations.values()) {
				Version version = config.getVersion();

				MigrationRef migration = MigrationRef.forVersion(migrations, version);
				migration.initConfig(config);

				for (Version dependency : config.getDependencies().values()) {
					migration.addDependency(MigrationRef.forVersion(migrations, dependency));
				}
			}
		}
		
		for (MigrationRef migration : migrations.values()) {
			migration.initSucessor();
		}

		// For each module the latest version.
		Map<String, MigrationRef> latest = new HashMap<>();
		for (MigrationRef migration : migrations.values()) {
			String module = migration.getModule();
			if (latest.get(module) == null) {
				latest.put(module, migration.findLatest());
			}
		}

		for (MigrationRef latestRef : latest.values()) {
			latestRef.initLocalOrder();
		}

		makeTransitive(migrations);

		// Remember all migrations that need no longer be executed, because they are
		// reflexive-transitive dependencies of the version currently stored in the database
		// ("dbVersion").
		Set<MigrationRef> done = findMigrationsAlreadyExecuted(dbVersion, migrations);

		// For all migrations known:
		for (MigrationRef migration : migrations.values()) {
			// For all dependencies of a migration/version:
			for (MigrationRef dependency : migration.getDependencies()) {
				if (dependency.getModule().equals(migration.getModule())) {
					// The dependency is a inter-module dependency, but we are only looking for
					// cross-module dependencies.
					continue;
				}

				// The predecessor version of the current version.
				MigrationRef predecessor = migration.getPredecessor();
				if (predecessor == null) {
					// The current migration is the first in its module.
					continue;
				}

				// The dependency of the predecessor of this migration in the dependency module
				MigrationRef predecessorDependency = predecessor.getDependency(dependency.getModule());
				if (predecessorDependency == dependency) {
					// The predecessor has the same dependency. This version is just an update of
					// the predecessor version without new dependencies. Leaf the handling to the
					// predecessor.
					continue;
				}

				// Find the ancestor version of the dependency that is the direct successor of the
				// found predecessor dependency.
				MigrationRef dependencyAncestor = dependency;
				while (true) {
					MigrationRef ancestor = dependencyAncestor.getPredecessor();
					if (ancestor == null || ancestor == predecessorDependency) {
						break;
					}
					dependencyAncestor = ancestor;
				}
				dependencyAncestor.addSyntheticDependency(predecessor,
					"version " + migration + " depends on " + dependency);
			}
		}

		// For each module dependency: Add a migration dependency from the oldest migration that has
		// no successor referenced from a migration of the dependent module to the latest migration
		// of the dependent module.
		for (Entry<String, MigrationRef> latestEntry : latest.entrySet()) {
			String source = latestEntry.getKey();
			MigrationRef latestSource = latestEntry.getValue();

			for (MigrationRef latestDependency : latestSource.getDependencies()) {
				if (latestDependency.getModule().equals(source)) {
					// Only cross-module dependencies.
					continue;
				}

				MigrationRef dependencySuccessor = latestDependency.getSuccessor();
				if (dependencySuccessor != null) {
					dependencySuccessor.addSyntheticDependency(latestSource,
						"latest migration " + latestSource + " depends on " + latestDependency);
				}
			}
		}

		for (MigrationRef migration : migrations.values()) {
			migration.updateDependencies();
		}

		try {
			List<MigrationRef> sorted = CollectionUtil.topsort(m -> m.getDependencies(), migrations.values(), false);

			return sorted.stream()
				// Only those migrations that belong to modules that were already present in the base
				// version stored in the database.
				.filter(m -> dbVersion.containsKey(m.getModule()))

				// Only those migrations not already performed during former startups.
				.filter(m -> !done.contains(m))

				.map(m -> m.getConfig())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		} catch (CyclicDependencyException ex) {
			List<?> cycle = ex.getCycle();
			StringBuilder msg = new StringBuilder();
			msg.append("Versions form a cyclic dependency, cannot compute valid order: ");
			msg.append(cycle.get(0));
			for (int n = 1, cnt = cycle.size(); n < cnt; n++) {
				MigrationRef source = (MigrationRef) cycle.get(n - 1);
				MigrationRef target = (MigrationRef) cycle.get(n);

				String reason = source.getReason(target.getModule());

				msg.append(' ');
				if (reason != null) {
					msg.append('[');
					msg.append(reason);
					msg.append(']');
				}
				msg.append("-> ");
				msg.append(target);
			}

			throw new IllegalStateException(msg.toString(), ex);
		}
	}

	/**
	 * Ensure transitivity of dependencies.
	 */
	private static void makeTransitive(Map<VersionID, MigrationRef> migrations) {
		boolean changed;
		do {
			changed = false;

			for (MigrationRef migration : migrations.values()) {
				Map<String, MigrationRef> updates = new HashMap<>();

				check:
				for (MigrationRef dependency : migration.getDependencies()) {
					MigrationRef dependencySuccessor = dependency.getSuccessor();
					for (MigrationRef dependency2 : dependency.getDependencies()) {
						String module = dependency2.getModule();
						MigrationRef limit =
							dependencySuccessor == null ? null : dependencySuccessor.getDependency(module);
						MigrationRef transitive = or(updates.get(module), migration.getDependency(module));

						if (transitive == null) {
							Logger.warn(
								"Adding missing transitive dependency of '" + migration + "' to '" + dependency2
									+ "'. ",
								MigrationUtil.class);
							updates.put(module, dependency2);
						} else if (dependency2.isNewerThan(transitive)) {
							Logger.warn(
								"Upgrading transitive dependency of '" + migration + "' from '" + transitive + "' to '"
									+ dependency2 + "'. " +
								"Since '" + migration + "' directly depends on '" + dependency
									+ "', which itself depends on '" + dependency2
									+ "', the transitive dependency to module '" + module
									+ "' must be at least '" + dependency2 + "' and at most '" + limit
									+ "' (the dependency of the successor '" + dependencySuccessor
									+ "' of the direct dependency in the corresponding module).",
								MigrationUtil.class);

							updates.put(module, dependency2);
						} else if (limit != null && transitive.isNewerThan(limit)) {
							assert dependencySuccessor != null;

							// Find first successor of dependency that directly depends on
							// transitive.
							MigrationRef updated = dependencySuccessor;
							while (true) {
								MigrationRef next = updated.getSuccessor();
								MigrationRef newLimit = next == null ? null : next.getDependency(module);
								if (newLimit == null || !transitive.isNewerThan(newLimit)) {
									// Better dependency has been found.

									Logger.warn(
										"Upgrading dependency of '" + migration + "' from '" + dependency + "' to '"
											+ updated + "'. " +
											"Since '" + migration + "' depends on '" + transitive
											+ "', the dependency '" + dependency
											+ "' ist to old since it would limit dependency to module '" + module
											+ "' to versions from '" + dependency2 + "' to '" + limit
											+ "' (the dependency of the successor '" + dependencySuccessor
											+ "' of the dependency).",
										MigrationUtil.class);

									updates.put(updated.getModule(), updated);
									break check;
								}

								updated = next;
							}
						}
					}
				}

				if (!updates.isEmpty()) {
					changed = true;

					for (MigrationRef update : updates.values()) {
						if (migration.getDependency(update.getModule()) != null) {
							migration.updateDependency(update);
						} else {
							// Prevent trivial cycle from not referencing a base version that is
							// referenced from a dependency.
							Logger.warn("Adding missing transitive dependency to '" + migration + "': " + update,
								MigrationUtil.class);
							migration.addDependency(update);
						}
					}
				}
			}
		} while (changed);
	}

	private static MigrationRef or(MigrationRef first, MigrationRef second) {
		return first != null ? first : second;
	}

	private static Set<MigrationRef> findMigrationsAlreadyExecuted(final Map<String, Version> dbVersion,
			Map<VersionID, MigrationRef> migrations) {
		Set<MigrationRef> done = new HashSet<>();
		List<MigrationRef> work = new ArrayList<>();
		for (Version current : dbVersion.values()) {
			MigrationRef migration = MigrationRef.forVersion(migrations, current);

			done.add(migration);
			work.add(migration);
		}

		while (!work.isEmpty()) {
			MigrationRef migration = work.remove(work.size() - 1);
			for (MigrationRef dependency : migration.getDependencies()) {
				if (!done.add(dependency)) {
					continue;
				}
				work.add(dependency);
			}
		}
		return done;
	}

	/**
	 * Determines the versions that are new, i.e. the versions that are larger than the
	 * {@code currentVersions} or for which there is no current version.
	 * 
	 * @param versionByModule
	 *        Mapping of the module name to all versions in the module in version order. The map is
	 *        modified.
	 * @param currentVersions
	 *        The current versions of the application, indexed by module name. If this map is empty,
	 *        a system from before introducing the automatic data migration is updated. This means,
	 *        all migrations must be executed. If the map is non empty but for a module no
	 *        {@link Version} is given, no migrations must be applied to this module, because the
	 *        module is a new dependency introduced in a new software version.
	 * 
	 * @return Set of versions that are newer than the current versions.
	 * 
	 * @see #getVersionsByModule(Log, Map)
	 */
	private static Set<Version> getNewVersions(Map<String, List<Version>> versionByModule,
			Map<String, Version> currentVersions) {
		Set<Version> newVersions = new HashSet<>();
		if (currentVersions.isEmpty()) {
			// A system from before introducing the automatic migration is updated.
			for (List<Version> versionsForNewModules : versionByModule.values()) {
				newVersions.addAll(versionsForNewModules);
			}
		} else {
			// Only modules that are mentioned in the database version must be migrated. All
			// other modules are introduced by a new software version and have no data in the
			// current database.
			for (Entry<String, Version> entry : currentVersions.entrySet()) {
				String module = entry.getKey();
				Version currentVersionForModule = entry.getValue();

				List<Version> versions = versionByModule.get(module);
				if (versions == null) {
					// May happen, when a module does not longer exists.
					continue;
				}
				int lastIndexOf = versions.lastIndexOf(currentVersionForModule);
				if (lastIndexOf == -1) {
					// Failure;
					throw new IllegalStateException("Version " + toString(currentVersionForModule)
						+ " not found in available versions " + toString(versions));
				}
				for (int i = lastIndexOf + 1; i < versions.size(); i++) {
					newVersions.add(versions.get(i));
				}
			}
		}
		return newVersions;
	}

	/**
	 * Compares {@link Version}, such that <code>v1</code> is less than <code>v2</code> iff
	 * <code>v1</code> was created before <code>v2</code>.
	 * 
	 * @param migrationScripts
	 *        All migration scripts indexed by the module. The value for the module is a mapping
	 *        from all known {@link Version}'s to the corresponding {@link MigrationConfig}.
	 * @param versionByModule
	 *        Mapping of the module name to all versions in the module in version order. Value of
	 *        {@link #getVersionsByModule(Log, Map)}.
	 * @param currentVersions
	 *        The current versions of the application, indexed by module name. If for a module no
	 *        {@link Version} is given, it is assumed that all migration for that module must be
	 *        applied.
	 * @param modules
	 *        All known database modules. The order of the module is the dependency order, i.e. when
	 *        <code>module1</code> depends on <code>module2</code>, <code>module2</code> appears
	 *        before <code>module1</code> in the array.
	 */
	public static Comparator<Version> versionCompare(
			Map<String, Map<Version, MigrationConfig>> migrationScripts,
			Map<String, List<Version>> versionByModule,
			Map<String, Version> currentVersions,
			List<String> modules) {
		return new Comparator<>() {

			@Override
			public int compare(Version v1, Version v2) {
				if (v1 == v2) {
					return 0;
				}
				String module1 = v1.getModule();
				String module2 = v2.getModule();
				if (module1.equals(module2)) {
					return compareVersionsOfSameModule(v1, v2);
				}
				MigrationConfig v1Migration = migrationScripts.getOrDefault(module1, Collections.emptyMap()).get(v1);
				MigrationConfig v2Migration = migrationScripts.getOrDefault(module2, Collections.emptyMap()).get(v2);
				if (v1Migration == null) {
					if (v2Migration == null) {
						// The migration script for both versions no longer exit. Since these
						// migration do not need to be executed anymore, they can be assumed to be
						// equal.
						return 0;
					} else {
						return -1;
					}
				} else {
					if (v2Migration == null) {
						return 1;
					}
				}

				Map<String, Version> v1Dependencies = v1Migration.getDependencies();
				Map<String, Version> v2Dependencies = v2Migration.getDependencies();
				Version v2DependencyInModule1 = v2Dependencies.get(module1);
				if (v2DependencyInModule1 != null) {
					assert v1Dependencies.get(module2) == null : "Cyclic dependencies in migration for version "
						+ MigrationUtil.toString(v1) + " (v1) and " + MigrationUtil.toString(v2)
							+ " (v2). v1 depends on module of v2 and v2 depends on module of v1.";
					return compareWithDependency(v1, v2DependencyInModule1);
				}
				Version v1DependencyInModule2 = v1Dependencies.get(module2);
				if (v1DependencyInModule2 != null) {
					return -compareWithDependency(v2, v1DependencyInModule2);
				}

				/* Either modules are really independent or one depends on the other, but was
				 * created before the first version of the other was created. */
				int dependencyComparison = compareDepencencies(v1, v2, v1Dependencies, v2Dependencies);
				if (dependencyComparison != 0) {
					return dependencyComparison;
				}

				/* If the migration script for dependent module has no dependency to the base
				 * module, then the script must be created before the first version of the base
				 * module is created. Therefore the migration script of the dependent module must be
				 * applied *before* the migration of the base module. */
				return -compareIndexBased(modules, module1, module2);
			}

			private int compareWithDependency(Version version, Version dependencyOfOther) {
				int dependencyResult = compareVersionsOfSameModule(version, dependencyOfOther);
				if (dependencyResult == 0) {
					// other depends on given version
					return -1;
				}
				if (dependencyResult < 0) {
					// other depends on a later version of given version
					return -1;
				}
				// other depends on a previous version of given version
				return 1;
			}

			private int compareDepencencies(Version v1, Version v2, Map<String, Version> v1Dependencies,
					Map<String, Version> v2Dependencies) {
				/* Check common dependencies. If there is a common dependency with a different
				 * version the version with the larger dependency was created later and is therefore
				 * larger. */
				Set<String> commonDependencies =
					CollectionUtil.intersection(v1Dependencies.keySet(), v2Dependencies.keySet());

				String diferentiatingModule = null;
				int result = 0;
				for (String commonDependency : commonDependencies) {
					Version v1Dependency = v1Dependencies.get(commonDependency);
					Version v2Dependency = v2Dependencies.get(commonDependency);
					int commonDependencyComparison = compareVersionsOfSameModule(v1Dependency, v2Dependency);
					if (commonDependencyComparison == 0) {
						continue;
					}
					int localResult = commonDependencyComparison < 0 ? -1 : 1;
					if (result != 0 && result != localResult) {
						assert false : "Versions " + MigrationUtil.toString(v1) + " and " + MigrationUtil.toString(v2)
							+ " have inconsistent dependencies in modules " + diferentiatingModule + " and "
							+ commonDependency + ".";
					}
					result = localResult;
					diferentiatingModule = commonDependency;
				}
				return result;
			}

			private int compareVersionsOfSameModule(Version o1, Version o2) {
				return compareIndexBased(versionByModule.get(o1.getModule()), o1, o2);
			}

			private int compareIndexBased(List<?> arr, Object o1, Object o2) {
				return arr.indexOf(o1) - arr.indexOf(o2);
			}
		};
	}

	private static Version[] allVersionsUnsorted(Map<String, Map<Version, MigrationConfig>> migrationScripts) {
		Set<Version> allVersions = new HashSet<>();
		for (Map<Version, MigrationConfig> e : migrationScripts.values()) {
			for (Entry<Version, MigrationConfig> v : e.entrySet()) {
				allVersions.add(v.getKey());
				allVersions.addAll(v.getValue().getDependencies().values());
			}
		}
		return allVersions.toArray(new Version[allVersions.size()]);
	}

	/**
	 * Determines for all modules the {@link Version}s in correct order.
	 * 
	 * @param migrationScripts
	 *        Mapping of {@link Version} to its {@link MigrationConfig} scripts indexed by module
	 *        name.
	 * @return Mapping of module name to all {@link Version}s in correct order.
	 */
	public static Map<String, List<Version>> getVersionsByModule(Log log,
			Map<String, Map<Version, MigrationConfig>> migrationScripts) {
		final Map<String, List<Version>> versionByModule = new HashMap<>();
		for (Entry<String, Map<Version, MigrationConfig>> entry : migrationScripts.entrySet()) {
			Map<Version, MigrationConfig> migrations = entry.getValue();

			List<Version> versions = getAllVersions(log, migrations);
			if (versions.isEmpty()) {
				// There was an error computing the version.
				continue;
			}
			String module = entry.getKey();

			versionByModule.put(module, versions);
		}
		return versionByModule;
	}

	/**
	 * Returns the top level migration module for this project.
	 */
	public static String getLocalMigrationModule() throws ConfigurationException {
		List<String> migrationModules = getMigrationModules();
		return migrationModules.get(migrationModules.size() - 1);
	}

	private static Map<String, Map<Version, MigrationConfig>> readMigrationScripts(Log log, List<String> modules)
			throws DatabaseAccessException {
		Set<String> modulesSet = new HashSet<>(modules);
		Map<String, Map<Version, MigrationConfig>> migrationsByModule = new HashMap<>();
		Map<String, Version> initialVersions = new HashMap<>();
		Map<String, Set<String>> moduleDependencies = new HashMap<>();
		for (String migrationFolder : FileManager.getInstance().getResourcePaths(MIGRATION_BASE_RESOURCE)) {
			String moduleName =
				migrationFolder.substring(MIGRATION_BASE_RESOURCE.length(), migrationFolder.length() - 1);
			Set<String> baseModules = moduleDependencies.computeIfAbsent(moduleName, x -> new HashSet<>());
			baseModules.add(moduleName);

			if (!modulesSet.contains(moduleName)) {
				log.info(
					"Folder " + moduleName + " is not configured as migration folder. Configured migration folders: "
						+ modules,
					Protocol.WARN);
				continue;
			}
			log.info("Reading migrations from module " + moduleName, Protocol.INFO);
			Map<Version, MigrationConfig> moduleMigrationConfigs = new HashMap<>();

			Version initial = initialVersion(moduleName);
			MigrationConfig noMigration = TypedConfiguration.newConfigItem(MigrationConfig.class);
			noMigration.setVersion(initial);
			moduleMigrationConfigs.put(initial, noMigration);
			initialVersions.put(moduleName, initial);

			for (String migrationResource : FileManager.getInstance().getResourcePaths(migrationFolder)) {
				if (!migrationResource.endsWith(MIGRATION_FILE_SUFFIX)) {
					continue;
				}
				Version version = getVersion(moduleName, migrationResource.substring(migrationFolder.length()));
				MigrationConfig config = readMigrationConfig(log, version);
				if (config != null) {
					moduleMigrationConfigs.put(version, config);
					baseModules.addAll(config.getDependencies().keySet());
				}
			}
			migrationsByModule.put(moduleName, moduleMigrationConfigs);
		}

		// Complete migration scripts with synthesized initial versions.
		for (Entry<String, Map<Version, MigrationConfig>> moduleEntry : migrationsByModule.entrySet()) {
			String module = moduleEntry.getKey();
			Set<String> baseModules = moduleDependencies.getOrDefault(module, Collections.emptySet());
			
			for (Entry<Version, MigrationConfig> migrationEntry : moduleEntry.getValue().entrySet()) {
				Version version = migrationEntry.getKey();
				MigrationConfig migration = migrationEntry.getValue();

				// Add dependency to initial versions.
				Map<String, Version> dependencies = migration.getDependencies();
				for (String baseModule : baseModules) {
					if (!dependencies.containsKey(baseModule)) {
						Version initial = initialVersions.get(baseModule);

						// Do not add a cyclic dependency between initial versions.
						if (!version.equals(initial)) {
							dependencies.put(baseModule, initial);
						}
					}
				}
			}
		}

		return migrationsByModule;
	}

	/**
	 * Creates an implicit initial vesrion for the given module.
	 */
	public static Version initialVersion(String moduleName) {
		return newVersion(moduleName, "<initial>");
	}

	/**
	 * Determines the maximal versions from the available migration scripts.
	 */
	public static Collection<Version> maximalVersions(Log log) throws DatabaseAccessException {
		List<String> migrationModules;
		try {
			migrationModules = getMigrationModules();
		} catch (ConfigurationException ex) {
			log.error("Unable to get migration modules.", ex);
			return Collections.emptyList();
		}
		Map<String, Map<Version, MigrationConfig>> migrationScripts = readMigrationScripts(log, migrationModules);
		Map<String, List<Version>> versionByModule = getVersionsByModule(log, migrationScripts);
		return getMaximalVersions(versionByModule);
	}

	/**
	 * Updates the {@link DBProperties} by updating the values for the given versions.
	 * 
	 * @param connection
	 *        The {@link Connection} to use. Must be a "write connection". The connection is
	 *        <b>not</b> committed.
	 * @param versions
	 *        The versions to update.
	 */
	public static void updateStoredVersions(Log log, PooledConnection connection, Iterable<Version> versions) {
		for (Version v : versions) {
			updateStoredVersion(log, connection, v);
		}
	}

	private static void updateStoredVersion(Log log, PooledConnection connection, Version version) {
		String property = propertyForModule(version.getModule());
		String newValue = version.getName();
		boolean changed;
		try {
			changed = DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, property, newValue);
		} catch (SQLException ex) {
			log.error("Unable to update version of '" + version.getModule() + "' to '" + newValue + "'.", ex);
			return;
		}
		if (changed) {
			log.info("Updated version of '" + version.getModule() + "' to '" + newValue + "'.");
		} else {
			log.info("No version change in '" + version.getModule() + "': '" + newValue + "'.");
		}
	}

	private static String propertyForModule(String module) {
		return COMMON_KEY_PREFIX + module;
	}

	static List<Version> getMaximalVersions(Map<String, List<Version>> versionByModule) {
		List<Version> maximalVersions = new ArrayList<>(versionByModule.size());
		for (List<Version> v : versionByModule.values()) {
			if (v.isEmpty()) {
				// There was a problem computing the version list.
				continue;
			}
			maximalVersions.add(v.get(v.size() - 1));
		}
		return maximalVersions;
	}

	/**
	 * Builds a {@link MigrationInfo} that describes the migration from the given data version to
	 * the application version of the currently running application.
	 * 
	 * @param log
	 *        The error reporter. The resulting migration is only valid, if no errors have been
	 *        reported.
	 * @param dataVersion
	 *        Descriptor of the data to be migrated. See e.g. {@link #loadDataVersionDescriptor()},
	 *        or {@link DumpReader#getVersion()}.
	 * @return The migration.
	 * @throws ConfigurationException
	 *         If migration is not possible.
	 */
	public static MigrationInfo buildMigration(Log log, VersionDescriptor dataVersion) throws ConfigurationException {
		return relevantMigrations(log, getMigrationModules(), true, dataVersion.getModuleVersions());
	}

	/**
	 * Builds a {@link MigrationInfo} that describes the migration from the given data version to
	 * the application version of the currently running application.
	 * 
	 * @param log
	 *        The error reporter. The resulting migration is only valid, if no errors have been
	 *        reported.
	 * @param dataVersion
	 *        Descriptor of the data to be migrated. See e.g. {@link #loadDataVersionDescriptor()},
	 *        or {@link DumpReader#getVersion()}.
	 * @return The migration.
	 */
	public static MigrationInfo relevantMigrations(Log log, List<String> migrationModules, boolean allowDowngrade,
			Map<String, Version> dataVersion) {
		Map<String, Map<Version, MigrationConfig>> migrationScripts;
		try {
			migrationScripts = readMigrationScripts(log, migrationModules);
		} catch (DatabaseAccessException ex) {
			log.error("Unable to read migration scripts for modules: " + migrationModules, ex);
			return MigrationInfo.NO_MIGRATION;
		}
		Map<String, List<Version>> appVersion = getVersionsByModule(log, migrationScripts);
		boolean downGrade = isDowngrade(log, appVersion, dataVersion);
		List<MigrationConfig> relevantMigrations;
		if (downGrade) {
			if (!allowDowngrade) {
				log.error(
					"No downgrade of application version possible. Maybe old version of application started? To prevent this check, set property '"
						+ MigrationService.Config.ALLOW_DOWNGRADE + "' for '" + MigrationService.class.getName()
						+ "'.");
				return MigrationInfo.NO_MIGRATION;
			}
			log.info("Downgrading application version.");
			relevantMigrations = new ArrayList<>();
		} else {
			relevantMigrations =
				getMigrations(migrationScripts, appVersion, dataVersion, migrationModules);
			if (relevantMigrations.isEmpty()) {
				log.info("No migration required.");
				return MigrationInfo.NO_MIGRATION;
			}
			log.info("Pending migrations:");
			for (MigrationConfig m : relevantMigrations) {
				log.info("\t" + m.getVersion().getModule() + ": " + m.getVersion().getName());
			}
		}
		return MigrationInfo.migrations(downGrade, relevantMigrations);
	}

	/**
	 * Drops all versions from the database.
	 * 
	 * @param log
	 *        Log to write infos and errors to.
	 * 
	 * @param connection
	 *        {@link PooledConnection} to access database
	 */
	public static void dropStoredVersions(Log log, PooledConnection connection) {
		Map<String, String> properties;
		try {
			properties = DBProperties.getPropertiesForNode(connection, DBProperties.GLOBAL_PROPERTY);
		} catch (SQLException ex) {
			log.error("Unable to fetch database versions.");
			return;
		}
		for (String property : properties.keySet()) {
			if (property.startsWith(COMMON_KEY_PREFIX)) {
				try {
					DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, property, null);
					log.info("Removed stored version " + property + ".");
				} catch (SQLException ex) {
					log.error("Unable to reset property " + property + ".");
				}
			}
		}
	}

	private static boolean isDowngrade(Log log, Map<String, List<Version>> versionsByModule,
			Map<String, Version> storedVersions) {
		boolean downGrade = false;
		module:
		for (Entry<String, Version> storedVersion : storedVersions.entrySet()) {
			List<Version> versions = versionsByModule.get(storedVersion.getKey());
			if (versions == null) {
				// module is not longer a migration version.
				continue;
			}
			for (Version tmp : versions) {
				if (ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(tmp, storedVersion.getValue())) {
					continue module;
				}
			}
			log.info("No valid version found for database version '" + toString(storedVersion.getValue())
				+ "' in versions " + toString(versions) + ".");
			downGrade = true;
		}
		return downGrade;
	}

	/**
	 * Loads the application data version that is currently stored in the database.
	 */
	public static VersionDescriptor loadDataVersionDescriptor() throws SQLException, ConfigurationException {
		MigrationService.Config config =
			(MigrationService.Config) ApplicationConfig.getInstance().getServiceConfiguration(MigrationService.class);
		List<String> modules = config.getModules();
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			Map<String, Version> versions = readStoredVersions(connection, modules);

			VersionDescriptor result = TypedConfiguration.newConfigItem(VersionDescriptor.class);
			result.setModuleVersions(versions);
			return result;
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Loads the application data version that is currently stored in the database.
	 */
	public static Map<String, Version> readStoredVersions(PooledConnection connection, List<String> migrationModules)
			throws SQLException {
		if (migrationModules.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Version> storedVersions = new HashMap<>();
		for (String module : migrationModules) {
			String property = propertyForModule(module);
			String moduleVersion = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, property);
			if (moduleVersion != null) {
				storedVersions.put(module, newVersion(module, moduleVersion));
			}
		}
		return storedVersions;
	}

}
