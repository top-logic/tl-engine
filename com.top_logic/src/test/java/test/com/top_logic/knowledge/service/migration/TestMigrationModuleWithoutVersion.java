/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationService;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.Version;

/**
 * Test for the migrations of a module that has no version stored in the database.
 *
 * <p>
 * Such a module is considered new to the application, so its migrations are skipped. The skip is
 * logged together with the hint to {@link MigrationService.Config#getMinimumModules()}, which
 * makes the migrations run.
 * </p>
 */
public class TestMigrationModuleWithoutVersion extends BasicTestCase {

	private static final String BASE = "base";

	private static final String APP = "app";

	private static final String BASE_MIGRATION = "Ticket_1_base_step";

	private static final String APP_MIGRATION = "Ticket_2_first_app_step";

	private static final String SKIP_MESSAGE = "Skipping migrations of module '" + APP + "'";

	private Map<String, Map<Version, MigrationConfig>> _scripts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scripts = new HashMap<>();

		Version baseInitial = addInitial(BASE);
		Version baseMigration = addMigration(MigrationUtil.newVersion(BASE, BASE_MIGRATION), baseInitial);

		Version appInitial = addInitial(APP);
		addMigration(MigrationUtil.newVersion(APP, APP_MIGRATION), appInitial, baseMigration);
	}

	/**
	 * The application module has no stored version, the base module has: The application's
	 * migration is skipped, and the log says how to make it run.
	 */
	public void testUnversionedModuleSkipped() {
		Map<String, Version> dbVersion = new HashMap<>();
		dbVersion.put(BASE, MigrationUtil.newVersion(BASE, BASE_MIGRATION));

		BufferingProtocol log = new BufferingProtocol();
		List<String> pending = pending(log, dbVersion);

		assertEquals(List.of(), pending);
		List<String> hints = skipMessages(log);
		assertEquals("Expected one hint on the skipped module: " + log.getInfos(), 1, hints.size());
		String hint = hints.get(0);
		assertTrue(hint, hint.contains(APP_MIGRATION));
		assertTrue(hint, hint.contains(MigrationService.Config.MINIMUM_MODULES));
	}

	/**
	 * The application module has its initial version in the data version, as configured by
	 * {@link MigrationService.Config#getMinimumModules()}: The application's migration runs, and
	 * no skip is logged.
	 */
	public void testInitialVersionScheduled() {
		Map<String, Version> dbVersion = new HashMap<>();
		dbVersion.put(BASE, MigrationUtil.newVersion(BASE, BASE_MIGRATION));
		dbVersion.put(APP, MigrationUtil.initialVersion(APP));

		BufferingProtocol log = new BufferingProtocol();
		List<String> pending = pending(log, dbVersion);

		assertEquals(List.of(APP + ": " + APP_MIGRATION), pending);
		assertEquals(List.of(), skipMessages(log));
	}

	/**
	 * A module with only its initial version is not reported, there is no migration to skip.
	 */
	public void testModuleWithoutMigrationsNotReported() {
		_scripts.remove(APP);
		addInitial(APP);

		Map<String, Version> dbVersion = new HashMap<>();
		dbVersion.put(BASE, MigrationUtil.newVersion(BASE, BASE_MIGRATION));

		BufferingProtocol log = new BufferingProtocol();
		pending(log, dbVersion);

		assertEquals(List.of(), skipMessages(log));
	}

	/**
	 * {@link MigrationUtil#versionsOfUnstoredModules(java.util.Collection, Map)} selects the
	 * versions of modules missing in the stored versions and leaves stored modules alone.
	 */
	public void testVersionsOfUnstoredModules() {
		Version baseMax = MigrationUtil.newVersion(BASE, BASE_MIGRATION);
		Version appMax = MigrationUtil.newVersion(APP, APP_MIGRATION);

		Map<String, Version> stored = new HashMap<>();
		stored.put(BASE, MigrationUtil.initialVersion(BASE));

		List<Version> missing = MigrationUtil.versionsOfUnstoredModules(List.of(baseMax, appMax), stored);
		assertEquals(1, missing.size());
		assertSame(appMax, missing.get(0));

		stored.put(APP, MigrationUtil.initialVersion(APP));
		assertEquals(List.of(), MigrationUtil.versionsOfUnstoredModules(List.of(baseMax, appMax), stored));
	}

	private List<String> pending(BufferingProtocol log, Map<String, Version> dbVersion) {
		List<MigrationConfig> migrations = MigrationUtil.getMigrations(log, _scripts,
			MigrationUtil.getVersionsByModule(log, _scripts), dbVersion, List.of(BASE, APP));
		log.checkErrors();
		return migrations.stream()
			.map(MigrationConfig::getVersion)
			.map(v -> v.getModule() + ": " + v.getName())
			.collect(Collectors.toList());
	}

	private static List<String> skipMessages(BufferingProtocol log) {
		return log.getInfos().stream().filter(m -> m.contains(SKIP_MESSAGE)).collect(Collectors.toList());
	}

	private Version addInitial(String module) {
		Version initial = MigrationUtil.initialVersion(module);
		addMigration(initial);
		return initial;
	}

	private Version addMigration(Version version, Version... dependencies) {
		MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
		migration.setVersion(version);
		for (Version dependency : dependencies) {
			migration.getDependencies().put(dependency.getModule(), dependency);
		}
		_scripts.computeIfAbsent(version.getModule(), x -> new HashMap<>()).put(version, migration);
		return version;
	}

	/**
	 * The suite, with the services the migration configurations are created with.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(null, TestMigrationModuleWithoutVersion.class,
			TypeIndex.Module.INSTANCE, DataAccessService.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
