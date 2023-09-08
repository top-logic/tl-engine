/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import static com.top_logic.knowledge.service.migration.MigrationUtil.*;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.XMain;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.dsa.DataAccessService;

/**
 * Main class to create a new file that can be used as template for a migration file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateMigrationScriptTemplate extends XMain {

	private static final String NAME_OPTION = "name";

	/**
	 * Name of the migration.
	 */
	private String _name;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (NAME_OPTION.equals(option)) {
			_name = args[i++];
		} else {
			return super.longOption(option, args, i);
		}
		return i;
	}

	@Override
	protected boolean argumentsRequired() {
		return true;
	}

	@Override
	protected void showHelpOptions() {
		super.showHelpOptions();
		info("\t--" + NAME_OPTION
				+ "         Name of the migration (prefix for the migration script name which must not yet exist).");
	}

	@Override
	protected void doActualPerformance() throws Exception {
		super.doActualPerformance();

		String[] migrationModules = getMigrationModules();
		if (migrationModules.length == 0) {
			getProtocol().error(
				"No migration modules configured, cannot create migration file (see com.top_logic.knowledge.service.migration.MigrationService.Config#getModules()).");
			return;
		}

		String module = getLocalMigrationModule();
		File migrationFolder = new File(Workspace.topLevelWebapp(), MigrationUtil.MIGRATION_BASE_PATH);
		File moduleFolder = new File(migrationFolder, module);
		FileUtilities.enforceDirectory(moduleFolder);

		File file =
			new File(moduleFolder,
				_name.replaceAll("[^A-Za-z0-9]+", " ").trim().replace(' ', '_') + MigrationUtil.MIGRATION_FILE_SUFFIX);
		if (file.exists()) {
			getProtocol().error("Migration script already exists: " + file.getAbsolutePath());
			return;
		}

		getProtocol().info("Creating migration script: " + file.getAbsolutePath());
		MigrationConfig migration = newMigrationConfig(migrationModules, file);
		addReplayMigration(migration);
		migration.setProcessors(Collections.emptyList());
		migration.setPostProcessors(Collections.emptyList());
		MigrationUtil.dumpMigrationConfig(file, migration);
	}

	private void addReplayMigration(MigrationConfig migration) {
		com.top_logic.knowledge.service.db2.migration.config.ReplayConfig replayMigration = TypedConfiguration
			.newConfigItem(com.top_logic.knowledge.service.db2.migration.config.ReplayConfig.class);
		if (replayMigration.getTypeMapping() != null) {
			/* This is made to have more simple migration file. When the migration is dumped, the
			 * default value is also dumped whereas it is not necessary. */
			replayMigration.setTypeMapping(null);
		}
		migration.setMigration(replayMigration);
	}

	private MigrationConfig newMigrationConfig(String[] migrationModules, File migrationFile) {
		MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
		for (String migrationModule : migrationModules) {
			Version moduleVersion = getLargestVersion(migrationModule);
			if (moduleVersion != null) {
				migration.getDependencies().put(moduleVersion.getModule(), moduleVersion);
			}
		}
		migration.setVersion(getVersion(migrationFile));
		return migration;
	}

	private Version getLargestVersion(String migrationModule) {
		List<MigrationConfig> migrations = MigrationUtil.readMigrations(getProtocol(), migrationModule);
		if (migrations.isEmpty()) {
			return null;
		}

		Version[] allVersions = getAllVersions(getProtocol(), migrations);
		return allVersions[allVersions.length - 1];
	}

	@Override
	protected void setUp(String[] args) throws Exception {
		setupModuleContext(TypeIndex.Module.INSTANCE, DataAccessService.Module.INSTANCE);
		super.setUp(args);
	}

	/**
	 * Main function as entry point.
	 */
	public static void main(String args[]) throws Exception {
		Logger.configureStdout();
		new CreateMigrationScriptTemplate().runMainCommandLine(args);
	}

}
