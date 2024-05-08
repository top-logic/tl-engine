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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

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
	private static enum Modules {
		tl {
			@Override
			Modules[] getDependencies() {
				return new Modules[] {};
			}
		},
		element {
			@Override
			Modules[] getDependencies() {
				return new Modules[] { tl };
			}
		},
		importer {
			@Override
			Modules[] getDependencies() {
				return new Modules[] { element, tl };
			}
		},
		reporting {
			@Override
			Modules[] getDependencies() {
				return new Modules[] { element, tl };
			}
		},
		ewe {
			@Override
			Modules[] getDependencies() {
				return new Modules[] { reporting, element, tl };
			}
		},
		demo {
			@Override
			Modules[] getDependencies() {
				return new Modules[] { ewe, reporting, importer, element, tl };
			}
		},
		;

		abstract Modules[] getDependencies();

	}

	private Map<Modules, Version> _maximalVersion;

	private Map<Version, Map<Modules, Version>> _maximalVersionsAtVersionTime;

	private List<Version> _allVersions;

	private Map<Version, MigrationConfig> _migrationForVersion;

	private int _versionNumber;

	private Protocol _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		reset();
	}

	private void reset() {
		_log = new AssertProtocol(TestMigrationUtil.class.getName());
		_versionNumber = 0;
		_migrationForVersion = new HashMap<>();
		_allVersions = new ArrayList<>();
		_maximalVersion = new EnumMap<>(Modules.class);
		_maximalVersionsAtVersionTime = new HashMap<>();
	}

	public void testGetAllVersions() {
		Version tl1 = version(Modules.tl);
		Version tl2 = version(Modules.tl);
		Version demo1 = version(Modules.demo);
		Version tl3 = version(Modules.tl);
		Version ewe1 = version(Modules.ewe);
		Version demo2 = version(Modules.demo);
		Version tl4 = version(Modules.tl);
		{
			List<MigrationConfig> migrationByVersion = new ArrayList<>();
			migrationByVersion.add(_migrationForVersion.get(demo2));
			migrationByVersion.add(_migrationForVersion.get(demo1));
			List<Version> allVersions = MigrationUtil.getAllVersions(_log, migrationByVersion);
			assertConfigEquals(allVersions, demo1, demo2);
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
			assertConfigEquals(allVersions, tl1, tl2, tl3, tl4);
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
		version(Modules.tl);
		Version demo1 = version(Modules.demo);
		version(Modules.tl);
		version(Modules.element);
		Version demo2 = version(Modules.demo);

		// simulate missing module.
		_migrationForVersion.get(demo2).getDependencies().remove(Modules.tl.name());

		List<MigrationConfig> migrationByVersion = new ArrayList<>();
		migrationByVersion.add(_migrationForVersion.get(demo1));
		migrationByVersion.add(_migrationForVersion.get(demo2));
		try {
			MigrationUtil.getAllVersions(_log, migrationByVersion);
			fail("Migration " + _migrationForVersion.get(demo2) + " is not complete: Base module " + Modules.tl.name()
				+ " missing.");
		} catch (AssertionFailedError expected) {
			if (!expected.getMessage().contains("Lost module dependency: Migration for")) {
				// Unexpected message
				throw expected;
			}
		}
	}

	public void testCorrectMigrationOrder() {
		List<Version> versions = new ArrayList<>();
		versions.add(version(Modules.tl));
		versions.add(version(Modules.tl));
		versions.add(version(Modules.element));
		versions.add(version(Modules.demo));
		versions.add(version(Modules.ewe));
		versions.add(version(Modules.element));
		versions.add(version(Modules.importer));
		checkMigrationOrder(versions);
	}

	@SuppressWarnings("unused")
	public void testCorrectMigrationOrder2() {
		List<Version> versions = new ArrayList<>();
		versions.add(version(Modules.reporting));
		if (true) {
			// The order of ewe and importer is not fixed, because the modules are independent. When
			// implementation changes, it may be that the other order may be correct :-(
			versions.add(version(Modules.ewe));
			versions.add(version(Modules.importer));
		} else {
			versions.add(version(Modules.importer));
			versions.add(version(Modules.ewe));
		}
		versions.add(version(Modules.demo));
		versions.add(version(Modules.tl));
		checkMigrationOrder(versions);
	}

	private void checkMigrationOrder(List<Version> versions) {
		for (int i = 0; i < versions.size(); i++) {
			List<Version> subList = versions.subList(i, versions.size());
			Map<String, Version> currentVersions = currentVersions(_maximalVersionsAtVersionTime.get(subList.get(0)));
			assertConfigEquals(getMigrations(subList), relevantMigrations(currentVersions));
		}
	}

	private List<MigrationConfig> relevantMigrations(Map<String, Version> currentVersions) {
		Map<String, Map<Version, MigrationConfig>> migrationScripts = migrationScripts();
		Map<String, List<Version>> versionByModule = getVersionsByModule(_log, migrationScripts);
		return getRelevantMigrations(migrationScripts, versionByModule, currentVersions, moduleDependencies());
	}

	List<MigrationConfig> getMigrations(List<Version> sortedVersions) {
		MigrationConfig[] migrations = new MigrationConfig[sortedVersions.size()];
		int index = 0;
		for (Version v : sortedVersions) {
			migrations[index++] = _migrationForVersion.get(v);
		}
		return Arrays.asList(migrations);
	}


	private List<String> moduleDependencies() {
		List<Modules> c = CollectionUtil.topsort(new Mapping<Modules, Iterable<? extends Modules>>() {

			@Override
			public Iterable<? extends Modules> map(Modules input) {
				return Arrays.asList(input.getDependencies());
			}
		}, Arrays.asList(Modules.values()), false);
		List<String> result = new ArrayList<>(c.size());
		for (Modules module : c) {
			result.add(module.name());
		}
		return result;
	}

	private Map<String, Map<Version, MigrationConfig>> migrationScripts() {
		Map<String, Map<Version, MigrationConfig>> result = new HashMap<>();
		ArrayList<Version> versions = new ArrayList<>(_migrationForVersion.keySet());

		// Use a kind of randomness
		Collections.shuffle(versions, new Random(47));
		for (Version version : versions) {
			Map<Version, MigrationConfig> map = result.get(version.getModule());
			if (map == null) {
				map = new LinkedHashMap<>();
				result.put(version.getModule(), map);
			}
			map.put(version, _migrationForVersion.get(version));
		}
		return result;
	}

	private void assertConfigEquals(List<? extends ConfigurationItem> actual, ConfigurationItem... expected) {
		assertEquals("Unexpected number of versions.", expected.length, actual.size());
		for (int i = 0; i < actual.size(); i++) {
			AbstractTypedConfigurationTestCase.assertEquals(expected[i], actual.get(i));
		}
	}

	private Map<String, Version> currentVersions(Map<Modules, Version> maximalVersions) {
		HashMap<String, Version> result = new HashMap<>();
		for (Entry<Modules, Version> entry : maximalVersions.entrySet()) {
			result.put(entry.getKey().name(), entry.getValue());
		}
		return result;
	}

	private MigrationConfig newMigration(Version version, Iterable<? extends Version> dependencies) {
		MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
		migration.setVersion(version);
		for (Version dependency : dependencies) {
			migration.getDependencies().put(dependency.getModule(), dependency);
		}
		return migration;
	}

	private Version version(Modules module) {
		Version newVersion = MigrationUtil.newVersion(module.name(), Integer.toString(_versionNumber++));
		MigrationConfig migrationForVersion = newMigration(newVersion, dependentVersions(module));
		_maximalVersionsAtVersionTime.put(newVersion, new EnumMap<>(_maximalVersion));
		_maximalVersion.put(module, newVersion);
		_migrationForVersion.put(newVersion, migrationForVersion);
		_allVersions.add(newVersion);
		return newVersion;
	}

	private Collection<Version> dependentVersions(Modules module) {
		List<Version> migrationDependencies = new ArrayList<>();
		addMaximalVersion(migrationDependencies, module);
		for (Modules dependencyModule : module.getDependencies()) {
			addMaximalVersion(migrationDependencies, dependencyModule);
		}
		return migrationDependencies;
	}

	private void addMaximalVersion(List<Version> versions, Modules module) {
		Version maximalModuleVersion = _maximalVersion.get(module);
		if (maximalModuleVersion != null) {
			versions.add(maximalModuleVersion);
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

