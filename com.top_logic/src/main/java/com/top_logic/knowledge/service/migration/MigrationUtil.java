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
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.tooling.ModuleLayout;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.service.db2.migration.DumpReader;

/**
 * Util class holding service methods to create {@link MigrationConfig}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationUtil {

	private static final Version[] EMPTY_VERSION_ARRAY = new Version[0];

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
	public static String[] getMigrationModules() throws ConfigurationException {
		MigrationService.Config migrationConf =
			(MigrationService.Config) ApplicationConfig.getInstance().getServiceConfiguration(MigrationService.class);
		return toModuleNames(migrationConf.getModules());
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
	public static Version[] getAllVersions(Log log, Collection<MigrationConfig> migrations) {
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
	public static Version[] getAllVersions(Log log, Map<Version, MigrationConfig> migrations) {
		switch (migrations.size()) {
			case 0:
				return EMPTY_VERSION_ARRAY;
			case 1:
				Entry<Version, MigrationConfig> next = migrations.entrySet().iterator().next();
				if (!next.getKey().equals(next.getValue().getVersion())) {
					throw new IllegalArgumentException(
						"Inconsistent migration mapping: Version '" + toString(next.getKey())
							+ "' is mapped to migration for version '" + toString(next.getValue().getVersion()) + "'.");
				}
				return new Version[] { next.getValue().getVersion() };
			default:
				return getAllVersions0(log, migrations);
		}

	}

	private static Version[] getAllVersions0(Log log, Map<Version, MigrationConfig> migrations) {
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
			return EMPTY_VERSION_ARRAY;
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
				return EMPTY_VERSION_ARRAY;
			}
			sortedVersions.add(followUp);
			tmp = followUp;
		}

		checkNoLostDependency(log, sortedVersions);

		Version[] result;
		if (baseVersionWithoutMigration == null) {
			result = versions(sortedVersions).toArray(EMPTY_VERSION_ARRAY);
		} else {
			result = new Version[sortedVersions.size() + 1];
			result[0] = baseVersionWithoutMigration;
			for (int i = 1; i < result.length; i++) {
				result[i] = version(sortedVersions.get(i - 1));
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
	 */
	public static List<MigrationConfig> getRelevantMigrations(
			final Map<String, Map<Version, MigrationConfig>> migrationScripts,
			final Map<String, Version[]> versionByModule,
			final Map<String, Version> currentVersions,
			final String[] modules) {
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
	 * Determines the versions that are new, i.e. the versions that are larger than the
	 * {@code currentVersions} or for which there is no current version.
	 * 
	 * @param versionByModule
	 *        Mapping of the module name to all versions in the module in version order. The map is
	 *        modified.
	 * @param currentVersions
	 *        The current versions of the application, indexed by module name. If for a module no
	 *        {@link Version} is given, it is assumed that all migration for that module must be
	 *        applied.
	 * 
	 * @return Set of versions that are newer than the current versions.
	 * 
	 * @see #getVersionsByModule(Log, Map)
	 */
	private static Set<Version> getNewVersions(Map<String, Version[]> versionByModule,
			Map<String, Version> currentVersions) {
		Set<Version> newVersions = new HashSet<>();
		for (Entry<String, Version> entry : currentVersions.entrySet()) {
			String module = entry.getKey();
			Version[] versions = versionByModule.remove(module);
			if (versions == null) {
				// May happen, when a module does not longer exists.
				continue;
			}
			int lastIndexOf = ArrayUtil.lastIndexOf(entry.getValue(), versions);
			if (lastIndexOf == -1) {
				// Failure;
				throw new IllegalStateException("Version " + toString(entry.getValue())
						+ " not found in available versions " + toString(Arrays.asList(versions)));
			}
			for (int i = lastIndexOf + 1; i < versions.length; i++) {
				newVersions.add(versions[i]);
			}
		}
		for (Version[] versionsForNewModules : versionByModule.values()) {
			Collections.addAll(newVersions, versionsForNewModules);
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
	private static Comparator<Version> versionCompare(
			Map<String, Map<Version, MigrationConfig>> migrationScripts,
			Map<String, Version[]> versionByModule,
			Map<String, Version> currentVersions,
			String[] modules) {
		return new Comparator<>() {

			@Override
			public int compare(Version o1, Version o2) {
				if (o1 == o2) {
					return 0;
				}
				String module1 = o1.getModule();
				String module2 = o2.getModule();
				if (module1.equals(module2)) {
					return compareVersionsOfSameModule(o1, o2);
				}
				MigrationConfig o1Migration = migrationScripts.getOrDefault(module1, Collections.emptyMap()).get(o1);
				MigrationConfig o2Migration = migrationScripts.getOrDefault(module2, Collections.emptyMap()).get(o2);
				if (o1Migration == null) {
					if (o2Migration == null) {
						// The migration script for both versions no longer exit. Since these
						// migration do not need to be executed anymore, they can be assumed to be
						// equal.
						return 0;
					} else {
						return -1;
					}
				} else {
					if (o2Migration == null) {
						return 1;
					}
				}

				Map<String, Version> o2Dependencies = o2Migration.getDependencies();
				Map<String, Version> o1Dependencies = o1Migration.getDependencies();
				Version dependencyInModule1 = o2Dependencies.get(module1);
				if (dependencyInModule1 != null) {
					assert o1Dependencies.get(module2) == null : "Cyclic dependencies in migration for version "
							+ MigrationUtil.toString(o1) + " (v1) and " + MigrationUtil.toString(o2)
							+ " (v2). v1 depends on module of v2 and v2 depends on module of v1.";
					return compareWithDependency(o1, dependencyInModule1);
				}
				Version dependencyInModule2 = o1Dependencies.get(module2);
				if (dependencyInModule2 != null) {
					return -compareWithDependency(o2, dependencyInModule2);
				}

				/* Either modules are really independent or one depends on the other, but was
				 * created before the first version of the other was created. */
				int dependencyComparison = compareDepencencies(o1Dependencies, o2Dependencies);
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

			private int compareDepencencies(Map<String, Version> o1Dependencies, Map<String, Version> o2Dependencies) {
				/* Check common dependencies. If there is a common dependency with a different
				 * version the version with the larger dependency was created later and is therefore
				 * larger. */
				Set<String> commonDependencies =
					CollectionUtil.intersection(o1Dependencies.keySet(), o2Dependencies.keySet());
				for (String commonDependency : commonDependencies) {
					Version o1Dependency = o1Dependencies.get(commonDependency);
					Version o2Dependency = o2Dependencies.get(commonDependency);
					int commonDependencyComparison = compare(o1Dependency, o2Dependency);
					if (commonDependencyComparison != 0) {
						return commonDependencyComparison;
					}
				}
				return 0;
			}

			private int compareVersionsOfSameModule(Version o1, Version o2) {
				return compareIndexBased(versionByModule.get(o1.getModule()), o1, o2);
			}

			private int compareIndexBased(Object[] arr, Object o1, Object o2) {
				return ArrayUtil.indexOf(o1, arr) - ArrayUtil.indexOf(o2, arr);
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
	public static Map<String, Version[]> getVersionsByModule(Log log,
			Map<String, Map<Version, MigrationConfig>> migrationScripts) {
		final Map<String, Version[]> versionByModule = new HashMap<>();
		for (Entry<String, Map<Version, MigrationConfig>> e : migrationScripts.entrySet()) {
			Version[] versions = getAllVersions(log, e.getValue());
			if (versions.length == 0) {
				// There was an error computing the version.
				continue;
			}
			versionByModule.put(e.getKey(), versions);
		}
		return versionByModule;
	}

	/**
	 * Returns the top level migration module for this project.
	 */
	public static String getLocalMigrationModule() throws ConfigurationException {
		String[] migrationModules = getMigrationModules();
		return migrationModules[migrationModules.length - 1];
	}

	private static Map<String, Map<Version, MigrationConfig>> readMigrationScripts(Log log, String[] modules)
			throws DatabaseAccessException {
		Set<String> modulesSet = Set.of(modules);
		Map<String, Map<Version, MigrationConfig>> migrationConfigs = new HashMap<>();
		for (String migrationFolder : FileManager.getInstance().getResourcePaths(MIGRATION_BASE_RESOURCE)) {
			String moduleName =
				migrationFolder.substring(MIGRATION_BASE_RESOURCE.length(), migrationFolder.length() - 1);

			if (!modulesSet.contains(moduleName)) {
				log.info(
					"Folder " + moduleName + " is not configured as migration folder. Configured migration folders: "
							+ Arrays.toString(modules),
					Protocol.WARN);
				continue;
			}
			log.info("Read migration scripts for module " + moduleName, Protocol.VERBOSE);
			Map<Version, MigrationConfig> moduleMigrationConfigs = new HashMap<>();
			for (String migrationResource : FileManager.getInstance().getResourcePaths(migrationFolder)) {
				if (!migrationResource.endsWith(MIGRATION_FILE_SUFFIX)) {
					continue;
				}
				Version version = getVersion(moduleName, migrationResource.substring(migrationFolder.length()));
				MigrationConfig config = readMigrationConfig(log, version);
				if (config != null) {
					moduleMigrationConfigs.put(version, config);
				}
			}
			if (moduleMigrationConfigs.isEmpty()) {
				// No configuration read. May occur when reading failed. Error is logged by reader.
				continue;
			}
			migrationConfigs.put(moduleName, moduleMigrationConfigs);
		}
		return migrationConfigs;
	}

	static String[] toModuleNames(Collection<String> namedConfigs) {
		int size = namedConfigs.size();
		if (size == 0) {
			return ArrayUtil.EMPTY_STRING_ARRAY;
		}
		String[] result = new String[size];
		int i = 0;
		for (String module : namedConfigs) {
			result[i++] = module;
		}
		return result;
	}

	/**
	 * Determines the maximal versions from the available migration scripts.
	 */
	public static Collection<Version> maximalVersions(Log log) throws DatabaseAccessException {
		String[] migrationModules;
		try {
			migrationModules = getMigrationModules();
		} catch (ConfigurationException ex) {
			log.error("Unable to get migration modules.", ex);
			return Collections.emptyList();
		}
		Map<String, Map<Version, MigrationConfig>> migrationScripts = readMigrationScripts(log, migrationModules);
		Map<String, Version[]> versionByModule = getVersionsByModule(log, migrationScripts);
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

	static List<Version> getMaximalVersions(Map<String, Version[]> versionByModule) {
		List<Version> maximalVersions = new ArrayList<>(versionByModule.size());
		for (Version[] v : versionByModule.values()) {
			if (v.length == 0) {
				// There was a problem computing the version list.
				continue;
			}
			maximalVersions.add(v[v.length - 1]);
		}
		return maximalVersions;
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
	public static MigrationInfo relevantMigrations(Log log, PooledConnection connection,
			String[] migrationModules, boolean allowDowngrade) throws SQLException {
		Map<String, Version> storedVersions = readStoredVersions(connection, migrationModules);

		log.info("Current data version: " + storedVersions.values().stream()
			.map(v -> v.getModule() + ": " + v.getName()).collect(Collectors.joining(", ")));
		return relevantMigrations(log, migrationModules, allowDowngrade, storedVersions);
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

	private static MigrationInfo relevantMigrations(Log log, String[] migrationModules, boolean allowDowngrade,
			Map<String, Version> dataVersion) {
		Map<String, Map<Version, MigrationConfig>> migrationScripts;
		try {
			migrationScripts = readMigrationScripts(log, migrationModules);
		} catch (DatabaseAccessException ex) {
			log.error("Unable to read migration scripts for modules: " + Arrays.toString(migrationModules), ex);
			return MigrationInfo.NO_MIGRATION;
		}
		Map<String, Version[]> appVersion = getVersionsByModule(log, migrationScripts);
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
				getRelevantMigrations(migrationScripts, appVersion, dataVersion, migrationModules);
			String pendingMigrations = "Pending migrations: ";
			if (relevantMigrations.isEmpty()) {
				log.info(pendingMigrations + "None");
				return MigrationInfo.NO_MIGRATION;
			}
			log.info(pendingMigrations
					+ relevantMigrations.stream().map(m -> m.getVersion().getModule() + ": " + m.getVersion().getName())
						.collect(Collectors.joining(", ")));
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

	private static boolean isDowngrade(Log log, Map<String, Version[]> versionsByModule,
			Map<String, Version> storedVersions) {
		boolean downGrade = false;
		module:
		for (Entry<String, Version> storedVersion : storedVersions.entrySet()) {
			Version[] versions = versionsByModule.get(storedVersion.getKey());
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
				+ "' in versions " + toString(Arrays.asList(versions)) + ".");
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
		String[] modules = MigrationService.getMigrationModules(config);
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

	private static Map<String, Version> readStoredVersions(PooledConnection connection, String[] modules)
			throws SQLException {
		if (modules.length == 0) {
			return Collections.emptyMap();
		}
		Map<String, Version> storedVersions = new HashMap<>();
		for (String module : modules) {
			String property = propertyForModule(module);
			String moduleVersion = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, property);
			if (moduleVersion != null) {
				storedVersions.put(module, newVersion(module, moduleVersion));
			}
		}
		return storedVersions;
	}

}
