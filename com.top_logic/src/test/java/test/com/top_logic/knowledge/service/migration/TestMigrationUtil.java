/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import static com.top_logic.knowledge.service.migration.MigrationUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.Version;

/**
 * Test class for {@link MigrationUtil}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMigrationUtil extends BasicTestCase {

	/**
	 * Dependency graph
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class TestModule {
		private String _name;

		private List<TestModule> _dependencies;

		/**
		 * Creates a {@link TestMigrationUtil.TestModule}.
		 */
		public TestModule(String name, TestModule... dependencies) {
			_name = name;
			_dependencies = Arrays.asList(dependencies);
		}

		public String name() {
			return _name;
		}

		List<TestModule> getDependencies() {
			return _dependencies;
		}

		public boolean dependsOn(TestModule earlierModule) {
			List<TestModule> dependencies = _dependencies;
			boolean isDirectDependency = dependencies.contains(earlierModule);
			if (isDirectDependency) {
				return true;
			}
			for (TestModule dependency : _dependencies) {
				if (dependency.dependsOn(earlierModule)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return _name + ": " + _dependencies.stream().map(TestModule::name).collect(Collectors.joining(", "));
		}
	}

	/**
	 * The latest version for each module of the simulated software product.
	 */
	private Map<TestModule, Version> _latestVersions;

	/**
	 * The latest versions of each module that was current just before the key version was created.
	 */
	private Map<Version, Map<TestModule, Version>> _latestVersionsAtVersionTime;

	/**
	 * All versions created for a simulated software product in the order they were created.
	 */
	private List<Version> _allVersions;

	/**
	 * The migration instruction for each created version in a simulated software product.
	 */
	private Map<Version, MigrationConfig> _migrationForVersion;

	/**
	 * Counter for creating unique version names in a simulated software product.
	 */
	private int _nextVersionNumber;

	private Protocol _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		reset();
	}

	private void reset() {
		_log = new AssertProtocol(TestMigrationUtil.class.getName());
		_nextVersionNumber = 0;
		_migrationForVersion = new HashMap<>();
		_allVersions = new ArrayList<>();
		_latestVersions = new HashMap<>();
		_latestVersionsAtVersionTime = new HashMap<>();
	}

	public void testGetAllVersions() {
		TestModule tl = new TestModule("tl");
		TestModule element = new TestModule("element", tl);
		TestModule importer = new TestModule("importer", element, tl);
		TestModule reporting = new TestModule("reporting", tl);
		TestModule ewe = new TestModule("ewe", reporting, element, tl);
		TestModule demo = new TestModule("demo", ewe, reporting, importer, element, tl);

		Version tlInitial = createInitalVersion(tl);
		@SuppressWarnings("unused")
		Version elementInitial = createInitalVersion(element);
		@SuppressWarnings("unused")
		Version importerInitial = createInitalVersion(importer);
		@SuppressWarnings("unused")
		Version eweInitial = createInitalVersion(ewe);
		@SuppressWarnings("unused")
		Version reportingInitial = createInitalVersion(reporting);
		Version demoInitial = createInitalVersion(demo);

		Version tl1 = createVersion(tl);
		Version tl2 = createVersion(tl);
		Version demo1 = createVersion(demo);
		Version tl3 = createVersion(tl);
		Version ewe1 = createVersion(ewe);
		Version demo2 = createVersion(demo);
		Version tl4 = createVersion(tl);
		{
			List<MigrationConfig> migrationByVersion = new ArrayList<>();
			migrationByVersion.add(_migrationForVersion.get(demo2));
			migrationByVersion.add(_migrationForVersion.get(demo1));
			List<Version> allVersions = MigrationUtil.getAllVersions(_log, migrationByVersion);
			assertConfigEquals(allVersions, demoInitial, demo1, demo2);
		}
		{
			List<MigrationConfig> migrationByVersion = new ArrayList<>();
			migrationByVersion.add(_migrationForVersion.get(tl4));
			migrationByVersion.add(_migrationForVersion.get(tl3));
			migrationByVersion.add(_migrationForVersion.get(tl2));
			List<Version> allButFirstVersions = MigrationUtil.getAllVersions(_log, migrationByVersion);
			assertConfigEquals(allButFirstVersions, tl1, tl2, tl3, tl4);
			migrationByVersion.add(_migrationForVersion.get(tl1));
			List<Version> allVersions = MigrationUtil.getAllVersions(_log, migrationByVersion);
			assertConfigEquals(allVersions, tlInitial, tl1, tl2, tl3, tl4);
		}
		{
			List<MigrationConfig> migrationByVersion = new ArrayList<>();
			List<Version> allVersions = MigrationUtil.getAllVersions(_log, migrationByVersion);
			assertConfigEquals(allVersions, new Version[0]);
		}
		{
			List<MigrationConfig> migrationByVersion = new ArrayList<>();
			migrationByVersion.add(_migrationForVersion.get(ewe1));
			List<Version> allVersions = MigrationUtil.getAllVersions(_log, migrationByVersion);
			assertConfigEquals(allVersions, ewe1);
		}
	}

	public void testLostDependency() {
		TestModule tl = new TestModule("tl");
		TestModule element = new TestModule("element", tl);
		TestModule importer = new TestModule("importer", element, tl);
		TestModule reporting = new TestModule("reporting", tl);
		TestModule ewe = new TestModule("ewe", reporting, element, tl);
		TestModule demo = new TestModule("demo", ewe, reporting, importer, element, tl);

		createInitalVersion(tl);
		createInitalVersion(element);
		createInitalVersion(importer);
		createInitalVersion(ewe);
		createInitalVersion(reporting);
		createInitalVersion(demo);

		createVersion(tl);
		Version demo1 = createVersion(demo);
		createVersion(tl);
		createVersion(element);
		Version demo2 = createVersion(demo);

		// simulate missing module.
		_migrationForVersion.get(demo2).getDependencies().remove(tl.name());

		List<MigrationConfig> migrationByVersion = new ArrayList<>();
		migrationByVersion.add(_migrationForVersion.get(demo1));
		migrationByVersion.add(_migrationForVersion.get(demo2));
		try {
			MigrationUtil.getAllVersions(_log, migrationByVersion);
			fail(
				"Migration " + _migrationForVersion.get(demo2) + " is not complete: Base module " + tl.name()
				+ " missing.");
		} catch (AssertionFailedError expected) {
			if (!expected.getMessage().contains("Lost module dependency: Migration for")) {
				// Unexpected message
				throw expected;
			}
		}
	}

	public void testCorrectMigrationOrder() {
		TestModule tl = new TestModule("tl");
		TestModule element = new TestModule("element", tl);
		TestModule importer = new TestModule("importer", element, tl);
		TestModule reporting = new TestModule("reporting", tl);
		TestModule ewe = new TestModule("ewe", reporting, element, tl);
		TestModule demo = new TestModule("demo", ewe, reporting, importer, element, tl);

		createInitalVersion(tl);
		createInitalVersion(element);
		createInitalVersion(importer);
		createInitalVersion(ewe);
		createInitalVersion(reporting);
		createInitalVersion(demo);

		List<Version> versions = new ArrayList<>();
		versions.add(createVersion(tl));
		versions.add(createVersion(tl));
		versions.add(createVersion(element));
		versions.add(createVersion(demo));
		versions.add(createVersion(ewe));
		versions.add(createVersion(element));
		versions.add(createVersion(importer));
		checkMigrationOrder(versions, Arrays.asList(tl, element, importer, reporting, ewe, demo));
	}

	/**
	 * Test upgrading a system from a state before introducing automatic migration.
	 */
	public void testFullUpgrade() {
		TestModule tl = new TestModule("tl");
		TestModule element = new TestModule("element", tl);
		TestModule importer = new TestModule("importer", element, tl);
		TestModule reporting = new TestModule("reporting", tl);
		TestModule ewe = new TestModule("ewe", reporting, element, tl);
		TestModule demo = new TestModule("demo", ewe, reporting, importer, element, tl);

		List<Version> versions = new ArrayList<>();

		versions.add(createInitalVersion(tl));
		versions.add(createInitalVersion(element));
		versions.add(createInitalVersion(importer));
		versions.add(createInitalVersion(reporting));
		versions.add(createInitalVersion(ewe));
		versions.add(createInitalVersion(demo));

		versions.add(createVersion(tl));
		versions.add(createVersion(tl));
		versions.add(createVersion(element));
		versions.add(createVersion(demo));
		versions.add(createVersion(ewe));
		versions.add(createVersion(element));
		versions.add(createVersion(importer));

		// The version information stored in the database of the simulated software product at
		// the time the migration starts.
		Map<String, Version> currentVersions = Collections.emptyMap();

		List<TestModule> modules = Arrays.asList(tl, element, importer, reporting, ewe, demo);
		List<MigrationConfig> migrations = migrationsPerformed(currentVersions, modules);
		checkMigrationOrder(modules, versions, migrations);
	}

	public void testCorrectMigrationOrder2() {
		TestModule tl = new TestModule("tl");
		TestModule element = new TestModule("element", tl);
		TestModule importer = new TestModule("importer", element, tl);
		TestModule reporting = new TestModule("reporting", tl);
		TestModule ewe = new TestModule("ewe", reporting, element, tl);
		TestModule demo = new TestModule("demo", ewe, reporting, importer, element, tl);

		createInitalVersion(tl);
		createInitalVersion(element);
		createInitalVersion(importer);
		createInitalVersion(ewe);
		createInitalVersion(reporting);
		createInitalVersion(demo);

		List<Version> versions = new ArrayList<>();
		versions.add(createVersion(reporting));
		versions.add(createVersion(ewe));
		versions.add(createVersion(importer));
		versions.add(createVersion(demo));
		versions.add(createVersion(tl));

		// The version information stored in the database of the simulated software product at
		// the time the migration starts.
		Map<String, Version> currentVersions = currentVersions(_latestVersionsAtVersionTime.get(versions.get(0)));

		List<TestModule> modules = Arrays.asList(tl, element, importer, reporting, ewe, demo);
		List<MigrationConfig> migrations = migrationsPerformed(currentVersions, modules);
		checkMigrationOrder(modules, versions, migrations);
	}

	public void testSimple() {
		TestModule m1 = new TestModule("m1");
		TestModule m2 = new TestModule("m2", m1);

		createInitalVersion(m1);
		createInitalVersion(m2);

		Version b0 = createVersion(m2, "b");
		Version a1 = createVersion(m1, "a");
		Version b2 = createVersion(m2, "b");

		List<TestModule> modules = Arrays.asList(m1, m2);
		List<Version> versions = Arrays.asList(b0, a1, b2);

		List<MigrationConfig> migrations =
			migrationsPerformed(currentVersions(_latestVersionsAtVersionTime.get(b0)), modules);

		printMigrations(migrations);

		checkMigrationOrder(modules, versions, migrations);
	}

	public void testDependencyFirst() {
		TestModule m1 = new TestModule("m1");
		TestModule m2 = new TestModule("m2", m1);

		createInitalVersion(m1);
		createInitalVersion(m2);

		Version b0 = createVersion(m2, "b");
		Version b1 = createVersion(m2, "b");
		Version a2 = createVersion(m1, "a");
		Version a3 = createVersion(m1, "a");
		Version a4 = createVersion(m1, "a");
		Version a5 = createVersion(m1, "a");

		List<TestModule> modules = Arrays.asList(m1, m2);
		List<Version> versions = Arrays.asList(b0, b1, a2, a3, a4, a5);

		List<MigrationConfig> migrations =
			migrationsPerformed(currentVersions(_latestVersionsAtVersionTime.get(b0)), modules);

		printMigrations(migrations);

		checkMigrationOrder(modules, versions, migrations);
	}

	public void testDependencyLast() {
		TestModule m1 = new TestModule("m1");
		TestModule m2 = new TestModule("m2", m1);

		createInitalVersion(m1);
		createInitalVersion(m2);

		Version a0 = createVersion(m1, "a");
		Version a1 = createVersion(m1, "a");
		Version a2 = createVersion(m1, "a");
		Version a3 = createVersion(m1, "a");
		Version b4 = createVersion(m2, "b");
		Version b5 = createVersion(m2, "b");

		List<TestModule> modules = Arrays.asList(m1, m2);
		List<Version> versions = Arrays.asList(a0, a1, a2, a3, b4, b5);

		List<MigrationConfig> migrations =
			migrationsPerformed(currentVersions(_latestVersionsAtVersionTime.get(a0)), modules);

		printMigrations(migrations);

		checkMigrationOrder(modules, versions, migrations);
	}

	public void testTail() {
		TestModule m1 = new TestModule("m1");
		TestModule m2 = new TestModule("m2", m1);

		createInitalVersion(m1);
		createInitalVersion(m2);
		
		//
		// m1:       i1       <- a2       <- a5 <- a6 <- a7
		//           ^           ^
		// m2: i2 <- b0 <- b1 <- b3 <- b4 
		//

		Version b0 = createVersion(m2, "b");
		Version b1 = createVersion(m2, "b");
		Version a2 = createVersion(m1, "a");
		Version b3 = createVersion(m2, "b");
		Version b4 = createVersion(m2, "b");
		Version a5 = createVersion(m1, "a");
		Version a6 = createVersion(m1, "a");
		Version a7 = createVersion(m1, "a");

		List<TestModule> modules = Arrays.asList(m1, m2);
		List<Version> versions = Arrays.asList(b0, b1, a2, b3, b4, a5, a6, a7);

		List<MigrationConfig> migrations =
			migrationsPerformed(currentVersions(_latestVersionsAtVersionTime.get(b0)), modules);

		printMigrations(migrations);

		checkMigrationOrder(modules, versions, migrations);
	}

	public void testDynamic() {
		Random rnd = new Random(42);

		for (int n = 0; n < 100; n++) {
			int moduleCnt = 3 + rnd.nextInt(50);
			int versionCnt = 2 + rnd.nextInt(100);

			testDynamic(rnd, moduleCnt, versionCnt);
			reset();
		}
	}

	private void testDynamic(Random rnd, int moduleCnt, int versionCnt) {
		List<TestModule> modules = new ArrayList<>();

		// Create modules.
		for (int n = 0; n < moduleCnt; n++) {
			int cntDependencies = Math.min(n, 1 + rnd.nextInt(3));

			Set<TestModule> directDependencies = new HashSet<>();
			while (directDependencies.size() < cntDependencies) {
				directDependencies.add(modules.get(rnd.nextInt(modules.size())));
			}

			Set<TestModule> dependencies = new HashSet<>();
			dependencies.addAll(directDependencies);
			for (TestModule dependency : directDependencies) {
				dependencies.addAll(dependency.getDependencies());
			}

			TestModule module = new TestModule("m" + n, dependencies.toArray(new TestModule[0]));
			modules.add(module);

			// System.out.println(module);
		}

		// System.out.println();

		// Create initial versions.
		List<Version> versions = new ArrayList<>();
		for (TestModule m : modules) {
			createInitalVersion(m);
		}

		// Create versions.
		for (int n = 0; n < versionCnt; n++) {
			Version version = createVersion(modules.get(rnd.nextInt(modules.size())));
			versions.add(version);

			printVersion(version);
		}


		// System.out.println();

		// The version information stored in the database of the simulated software product at
		// the time the migration starts.
		Map<String, Version> dbVersion = currentVersions(_latestVersionsAtVersionTime.get(versions.get(0)));

		List<MigrationConfig> migrations = migrationsPerformed(dbVersion, modules);

		// printMigrations(migrations);

		checkMigrationOrder(modules, versions, migrations);
	}

	private void printMigrations(List<MigrationConfig> migrations) {
		for (MigrationConfig migration : migrations) {
			printVersion(migration.getVersion());
		}
	}

	private void printVersion(Version version) {
		MigrationConfig migration = _migrationForVersion.get(version);

		System.out.println(toString(version) + ": " + migration.getDependencies().values().stream()
			.map((Version v) -> toString(v)).collect(Collectors.joining(", ")));
	}

	/**
	 * The migration order is acceptable, if no migration happens before an earlier migration of a
	 * dependency and no later migration of a dependency happens before the depending migration.
	 */
	private void checkMigrationOrder(List<TestModule> modules, List<Version> versions,
			List<MigrationConfig> migrations) {
		Set<Version> migrationsDone = new HashSet<>();
		for (MigrationConfig migration : migrations) {
			Version version = migration.getVersion();

			// For all versions in version create order that were created before the current one:
			int index = 0;
			for (; true; index++) {
				Version earlier = versions.get(index);
				if (earlier.equals(version)) {
					// The current migration has been reached.
					break;
				}

				// "Earlier" is a version that was actually created before "version".
				if (migrationsDone.contains(earlier)) {
					// Migration already performed, this is OK.
					continue;
				}

				// The earlier migration was not scheduled to be executed before the current
				// "version". This is only OK, if the module of the current version does not depend
				// on the module of the earlier version.
				// Check whether the missing migration is one of a dependency.
				TestModule versionModule = module(modules, version.getModule());
				TestModule earlierModule = module(modules, earlier.getModule());

				if (versionModule.dependsOn(earlierModule)) {
					fail("Invalid migration order: Version " + toString(version) + " depends on " + toString(earlier)
						+ " but is executed before.");
				}
			}

			// Check versions created later.
			for (index++; index < versions.size(); index++) {
				Version later = versions.get(index);

				if (migrationsDone.contains(later)) {
					// The version created later was executed before this one. This is only OK, if
					// the version's module does not depend on the later versions module. Otherwise,
					// the current migration sees changes it does not expect.
					TestModule versionModule = module(modules, version.getModule());
					TestModule laterModule = module(modules, later.getModule());
					if (versionModule.dependsOn(laterModule)) {
						fail("Invalid migration order: Version " + toString(later) + " executed before "
							+ toString(version) + " but was created after it.");
					}
				}
			}

			migrationsDone.add(version);
		}
	}

	private static String toString(Version version) {
		return version.getName() + "(" + version.getModule() + ")";
	}

	private TestModule module(List<TestModule> modules, String name) {
		return modules.stream().filter(m -> m.name().equals(name)).findFirst().get();
	}

	private void checkMigrationOrder(List<Version> versions, List<TestModule> modules) {
		for (int skip = 0; skip < versions.size(); skip++) {
			// Simulate that i versions have been dropped from the software product (are no longer
			// part of the delivered artifacts).
			List<Version> suffixList = versions.subList(skip, versions.size());

			// The version information stored in the database of the simulated software product at
			// the time the migration starts.
			Map<String, Version> currentVersions = currentVersions(_latestVersionsAtVersionTime.get(suffixList.get(0)));

			assertConfigEquals("Wrong migrations when skipping " + skip + " versions.", expectedMigrations(suffixList),
				migrationsPerformed(currentVersions, modules));
		}
	}

	private List<MigrationConfig> migrationsPerformed(Map<String, Version> currentVersions, List<TestModule> modules) {
		Map<String, Map<Version, MigrationConfig>> availableMigrations = availableMigrations();
		Map<String, List<Version>> versionsByModule = getVersionsByModule(_log, availableMigrations);

		// Check total order of migration instructions for each module.
		for (Entry<String, List<Version>> entry : versionsByModule.entrySet()) {
			String module = entry.getKey();
			List<Version> moduleVersions = entry.getValue();

			for (int n = 1; n < moduleVersions.size(); n++) {
				Version version = moduleVersions.get(n);
				MigrationConfig migration = _migrationForVersion.get(version);
				Version predecessor = migration.getDependencies().get(module);
				assertNotNull("Non-initial version " + toString(version) + " has no predecessor.", predecessor);

				assertEquals("Wrong predecessor/invalid total version order in module '" + module + "'.", predecessor,
					moduleVersions.get(n - 1));
			}
		}

		return getMigrations(_log, availableMigrations, versionsByModule, currentVersions,
			moduleDependencies(modules));
	}

	/**
	 * The list of migrations that are expected to be performed for upgrading the given versions.
	 */
	List<MigrationConfig> expectedMigrations(List<Version> versionsToUpgrade) {
		List<MigrationConfig> migrations = new ArrayList<>();
		for (Version v : versionsToUpgrade) {
			migrations.add(_migrationForVersion.get(v));
		}
		return migrations;
	}


	private List<String> moduleDependencies(List<TestModule> modules) {
		List<TestModule> c = CollectionUtil.topsort(new Mapping<TestModule, Iterable<? extends TestModule>>() {
			@Override
			public Iterable<? extends TestModule> map(TestModule input) {
				return input.getDependencies();
			}
		}, modules, false);
		List<String> result = new ArrayList<>(c.size());
		for (TestModule module : c) {
			result.add(module.name());
		}
		return result;
	}

	/**
	 * All migration scripts read from the simulated software product indexed by module.
	 */
	private Map<String, Map<Version, MigrationConfig>> availableMigrations() {
		Map<String, Map<Version, MigrationConfig>> result = new HashMap<>();

		// The version information read from the modules of the simulated software product in a
		// stable but otherwise arbitrary order.
		List<Version> discoveredVersions = new ArrayList<>(_migrationForVersion.keySet());
		Collections.shuffle(discoveredVersions, new Random(47));

		for (Version version : discoveredVersions) {
			result
				.computeIfAbsent(version.getModule(), x -> new HashMap<>())
				.put(version, _migrationForVersion.get(version));
		}
		return result;
	}

	private static void assertConfigEquals(List<? extends ConfigurationItem> actual, ConfigurationItem... expected) {
		assertEquals("Unexpected number of versions.", expected.length, actual.size());
		for (int i = 0; i < actual.size(); i++) {
			AbstractTypedConfigurationTestCase.assertEquals(expected[i], actual.get(i));
		}
	}

	private static Map<String, Version> currentVersions(Map<TestModule, Version> latestVersions) {
		HashMap<String, Version> result = new HashMap<>();
		for (Entry<TestModule, Version> entry : latestVersions.entrySet()) {
			result.put(entry.getKey().name(), entry.getValue());
		}
		return result;
	}

	private static MigrationConfig newMigration(Version version, Iterable<? extends Version> dependencies) {
		MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
		migration.setVersion(version);
		for (Version dependency : dependencies) {
			migration.getDependencies().put(dependency.getModule(), dependency);
		}
		return migration;
	}

	/**
	 * Simulates creating the implicit initial version for the given module.
	 */
	private Version createInitalVersion(TestModule module) {
		return createVersion(module, "initial");
	}

	/**
	 * Simulates creating a new version in the given module.
	 */
	private Version createVersion(TestModule module) {
		return createVersion(module, "v");
	}

	private Version createVersion(TestModule module, String name) {
		Version newVersion = MigrationUtil.newVersion(module.name(), name + Integer.toString(_nextVersionNumber++));
		MigrationConfig migrationForVersion = newMigration(newVersion, dependentVersions(module));
		_latestVersionsAtVersionTime.put(newVersion, new HashMap<>(_latestVersions));
		_latestVersions.put(module, newVersion);
		_migrationForVersion.put(newVersion, migrationForVersion);
		_allVersions.add(newVersion);
		return newVersion;
	}

	/**
	 * All versions, an newly created version in the given module would depend on.
	 */
	private Collection<Version> dependentVersions(TestModule module) {
		List<Version> result = new ArrayList<>();
		addLatestVersion(result, module);
		for (TestModule dependencyModule : module.getDependencies()) {
			addLatestVersion(result, dependencyModule);
		}
		return result;
	}

	private void addLatestVersion(List<Version> versions, TestModule module) {
		Version latestVersion = _latestVersions.get(module);
		if (latestVersion != null) {
			versions.add(latestVersion);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMigrationUtil}.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(null, TestMigrationUtil.class, TypeIndex.Module.INSTANCE,
			DataAccessService.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}

