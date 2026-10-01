/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.migration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.Version;

/**
 * Asks the engine which {@code tl-ai-core} migrations it schedules on the databases that exist in
 * the field, rather than trusting the dependency list of a migration file.
 *
 * <p>
 * The order the engine computes is not a plain topological sort of the dependencies written in the
 * files: {@code MigrationUtil.getMigrations} moves a dependency up when it is too old for the other
 * dependencies of the same migration, and then counts every migration reachable from the stored
 * versions as already applied. A {@code tl-ai-core} migration that names an older engine version
 * than the applications that build on it is therefore skipped, without an error, on exactly the
 * databases it was written for (#594). The consistency check the applications run does not see
 * this: it looks at a database with no stored version.
 * </p>
 *
 * <p>
 * Each case here is a database state some installation has: the stored versions are what tl-ai
 * 2.0.4 on 8.0.0-alpha8 wrote, and the application's own migration is modelled after the ones
 * Consulting wrote on 2.0.4 and after its move to {@code Issue_487} on 2.0.5.
 * </p>
 */
public class TestMigrationScheduling extends BasicTestCase {

	private static final String MODULE = "tl-ai-core";

	/** The migration module of the application that builds on {@code tl-ai-core}. */
	private static final String APP = "test-app";

	private static final String ISSUE_230 = "Issue_230_remove_module_tl_ai_chat";

	private static final String ISSUE_487 = "Issue_487_remove_agent_session_memory";

	private static final String ISSUE_594 = "Issue_594_complete_agent_session_memory_removal";

	/**
	 * The engine versions a database created or last started by tl-ai 2.0.4 stores - the heads of
	 * 8.0.0-alpha8, read from such a database.
	 */
	private static final Map<String, String> ALPHA8 = alpha8();

	private static Map<String, String> alpha8() {
		Map<String, String> result = new LinkedHashMap<>();
		result.put("tl", "Ticket_29492_enlarge_layout_key");
		result.put("tl-element", "Ticket_29088_Add_PersistentView");
		result.put("tl-model-search", "Ticket_28710_Added_TLSearch");
		result.put("tl-contact", "Ticket_28557_enlarge_contact_attributes");
		result.put("tl-layout-formeditor", "Ticket_29092_multi_factor_authentication");
		result.put("tl-model-wysiwyg", "Ticket_29410_filename_binary");
		result.put("tl-doc", "Ticket_28604_changelog");
		result.put("tl-bpe", "Ticket_29037_Update_diagram_attribute");
		result.put("tl-monitoring", "Ticket_27736_Upgrade_layout");
		result.put("tl-search-lucene", "Ticket_27442_Update_lucene_version_to_8_11_2");
		return result;
	}

	/**
	 * The case of #594: an application wrote a migration of its own on tl-ai 2.0.4, and the database
	 * has not started on anything newer yet. Before the fix the engine took {@code Issue_487} for
	 * applied because of that application migration and ran nothing.
	 */
	public void testApplicationMigrationOnIssue230() throws Exception {
		List<String> pending = pending(ISSUE_230, ISSUE_230);

		assertEquals(List.of(ISSUE_487, ISSUE_594), pending);
	}

	/**
	 * A database that did start on tl-ai 2.0.5 with the old {@code Issue_487} and skipped it, and
	 * whose application then moved its migrations to depend on {@code Issue_487} - Consulting. The
	 * stored {@code tl-ai-core} version is still {@code Issue_230}, but the application's own
	 * version now reaches {@code Issue_487}, so only a new migration runs there.
	 */
	public void testApplicationMovedToIssue487() throws Exception {
		List<String> pending = pending(ISSUE_230, ISSUE_487);

		assertEquals(List.of(ISSUE_594), pending);
	}

	/**
	 * An application without migrations of its own on tl-ai 2.0.4, such as tl-ai-demo: nothing
	 * moves a dependency up, and both migrations run.
	 */
	public void testNoApplicationMigration() throws Exception {
		List<String> pending = pending(ISSUE_230, null);

		assertEquals(List.of(ISSUE_487, ISSUE_594), pending);
	}

	/**
	 * A database on which {@code Issue_487} did run: only the follow-up is left, whose processors
	 * then find nothing to remove.
	 */
	public void testIssue487Applied() throws Exception {
		List<String> pending = pending(ISSUE_487, ISSUE_487);

		assertEquals(List.of(ISSUE_594), pending);
	}

	/**
	 * A database tl-ai 2.0.5 or later left on 8.0.0-alpha8, started on 8.0.0-alpha9: the engine's own
	 * migrations of alpha9 run, and nothing of {@code tl-ai-core} is left or moved - with or without
	 * an application migration written on alpha8.
	 */
	public void testAlpha8DatabaseOnAlpha9() throws Exception {
		assertEquals(List.of(), pending(ISSUE_594, null));
		assertEquals(List.of(), pending(ISSUE_594, ISSUE_594));
	}

	/**
	 * The {@code tl-ai-core} migrations the engine schedules on a database with the given stored
	 * versions.
	 *
	 * @param storedVersion
	 *        The {@code tl-ai-core} version stored in the database.
	 * @param appDependency
	 *        The {@code tl-ai-core} version the application's one migration depends on, or
	 *        {@code null} for an application without migrations. The database is at that
	 *        migration.
	 */
	private List<String> pending(String storedVersion, String appDependency) throws Exception {
		List<String> modules = new ArrayList<>(MigrationUtil.getMigrationModules());
		assertTrue("Not a migration module in this setup: " + modules, modules.contains(MODULE));

		BufferingProtocol readLog = new BufferingProtocol();
		Map<String, Map<Version, MigrationConfig>> scripts = readMigrationScripts(readLog, modules);
		readLog.checkErrors();

		Map<String, Version> dbVersion = new HashMap<>();
		for (String module : scripts.keySet()) {
			String name = ALPHA8.get(module);
			if (name != null) {
				dbVersion.put(module, MigrationUtil.newVersion(module, name));
			}
		}
		assertTrue("The engine module is missing in this setup: " + scripts.keySet(), dbVersion.containsKey("tl"));
		dbVersion.put(MODULE, MigrationUtil.newVersion(MODULE, storedVersion));

		if (appDependency != null) {
			Version appVersion = addApplicationMigration(scripts, appDependency);
			modules.add(APP);
			dbVersion.put(APP, appVersion);
		}

		// Moving a dependency up is logged as a warning; count it as the failure it is here.
		BufferingProtocol log = new BufferingProtocol() {
			@Override
			public void localInfo(String message, int verbosityLevel) {
				if (verbosityLevel < Protocol.INFO) {
					super.localError(message);
				} else {
					super.localInfo(message, verbosityLevel);
				}
			}
		};
		List<MigrationConfig> migrations = MigrationUtil.getMigrations(log, scripts,
			MigrationUtil.getVersionsByModule(log, scripts), dbVersion, modules);
		log.checkErrors();

		return migrations.stream()
			.map(MigrationConfig::getVersion)
			.filter(v -> MODULE.equals(v.getModule()))
			.map(Version::getName)
			.collect(Collectors.toList());
	}

	/**
	 * Adds the application's one migration, as the migration template of the engine writes it: a
	 * dependency on the given {@code tl-ai-core} version and on the head of every engine module.
	 */
	private static Version addApplicationMigration(Map<String, Map<Version, MigrationConfig>> scripts,
			String tlAiDependency) {
		Version initial = MigrationUtil.initialVersion(APP);
		MigrationConfig none = TypedConfiguration.newConfigItem(MigrationConfig.class);
		none.setVersion(initial);

		Version version = MigrationUtil.newVersion(APP, "Migration_0001_app_step");
		MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
		migration.setVersion(version);
		Map<String, Version> dependencies = migration.getDependencies();
		dependencies.put(APP, initial);
		for (Map.Entry<String, String> engineVersion : ALPHA8.entrySet()) {
			// Also the engine modules this setup reads no migrations of: tl-ai-core depends on them.
			dependencies.put(engineVersion.getKey(),
				MigrationUtil.newVersion(engineVersion.getKey(), engineVersion.getValue()));
		}
		dependencies.put(MODULE, MigrationUtil.newVersion(MODULE, tlAiDependency));

		Map<Version, MigrationConfig> appScripts = new HashMap<>();
		appScripts.put(initial, none);
		appScripts.put(version, migration);
		scripts.put(APP, appScripts);
		return version;
	}

	/**
	 * The migration scripts exactly as the migration service reads them, synthesized initial
	 * versions included.
	 *
	 * <p>
	 * {@code MigrationUtil.readMigrationScripts} is private; reading the files again here would
	 * test a copy of it instead.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Map<Version, MigrationConfig>> readMigrationScripts(Log log, List<String> modules)
			throws Exception {
		Method read = MigrationUtil.class.getDeclaredMethod("readMigrationScripts", Log.class, List.class);
		read.setAccessible(true);
		return (Map<String, Map<Version, MigrationConfig>>) read.invoke(null, log, modules);
	}

	/**
	 * The suite, with the services the migration scripts are read with.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(null, TestMigrationScheduling.class,
			DataAccessService.Module.INSTANCE, TypeIndex.Module.INSTANCE);
		return ModuleLicenceTestSetup.setupModule(test);
	}

}
