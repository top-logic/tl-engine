/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationUtil;

/**
 * Test that check that the migration files of the tested module are consistent.
 * 
 * @see MigrationUtil
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestConsistentMigrationScripts extends BasicTestCase {

	private FileManager _fileManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_fileManager = FileManager.getInstance();
	}

	public void testConsistentMigrationScripts() throws IOException, ConfigurationException {
		List<String> migrationModules = MigrationUtil.getMigrationModules();
		if (migrationModules.isEmpty()) {
			// No configured modules
			return;
		}

		for (String migrationModule : migrationModules) {
			List<BinaryData> filesForModule =
				_fileManager.getDataOverlays(MigrationUtil.MIGRATION_BASE_RESOURCE + migrationModule);
			if (filesForModule.size() > 1) {
				fail("Multiple migration folder for module '" + migrationModule + "': " + filesForModule);
			}
		}

		/* Check only top level migration module, because only this is the module in this eclipse
		 * module. */
		String module = MigrationUtil.getLocalMigrationModule();
		Protocol log = new AssertProtocol();
		List<MigrationConfig> migrations = MigrationUtil.readMigrations(log, module);
		Collections.shuffle(Arrays.asList(migrations));
		MigrationUtil.getAllVersions(log, migrations);
		log.checkErrors();
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		Test test =
			ServiceTestSetup.createSetup(null, TestConsistentMigrationScripts.class, DataAccessService.Module.INSTANCE,
				TypeIndex.Module.INSTANCE);
		return ModuleLicenceTestSetup.setupModule(test);
	}

}
