/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.ScratchDirectory;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.KBSetup.KBType;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;

import com.top_logic.basic.io.FileUtilities;

/**
 * {@link TestSetup} for testing app migrations starting from a database file test fixture.
 * 
 * <p>
 * The setup prevents setting up tables to use the database test fixture. Additionally, it enables
 * migration during test startup, which is normally disabled. The database file is copied from
 * <code>tmp/h2</code> to <code>target/test-run/</code> to allow starting the test multiple times
 * without reverting changes. To boot the tests from this database,
 * </p>
 *
 * <p>
 * The documents of the fixture are copied from <code>tmp/app-data</code> to
 * <code>target/test-run/app-data</code>, which the setup makes the storage path of the application
 * by setting the system property {@link ScratchDirectory#STORAGE_PATH_VARIABLE} for the duration of
 * the test. The system property takes precedence over a storage path given by the environment of
 * the test run (such as the storage directory of a scratch directory).
 * </p>
 */
public class AppMigrationTestSetup extends TestSetup {
	/**
	 * Creates a {@link AppMigrationTestSetup}.
	 */
	public AppMigrationTestSetup(Test test) {
		super(test);
	}

	/**
	 * Directory holding the copy of the test fixture.
	 */
	private static final File TEST_ROOT = new File("./target/test-run");

	/**
	 * Name of the storage directory of the application in the test fixture.
	 */
	private static final String STORAGE_DIR = "app-data";

	private boolean _before;

	private String _storagePathBefore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_before = KBSetup.shouldCreateTables();
		KBSetup.setCreateTables(false);
		
		FileUtilities.deleteR(TEST_ROOT);
		FileUtilities.copyR(new File("./tmp"), TEST_ROOT);

		_storagePathBefore = System.getProperty(ScratchDirectory.STORAGE_PATH_VARIABLE);
		System.setProperty(ScratchDirectory.STORAGE_PATH_VARIABLE, new File(TEST_ROOT, STORAGE_DIR).getPath());

		ModuleLicenceTestSetup.enableTableSetup();
	}

	@Override
	protected void tearDown() throws Exception {
		KBSetup.setCreateTables(_before);
		if (_storagePathBefore == null) {
			System.clearProperty(ScratchDirectory.STORAGE_PATH_VARIABLE);
		} else {
			System.setProperty(ScratchDirectory.STORAGE_PATH_VARIABLE, _storagePathBefore);
		}

		super.tearDown();
	}

	/**
	 * Wraps an application test using a database fixture in <code>tmp/h2</code>.
	 */
	public static Test suite(Test migrationTest) {
		return new AppMigrationTestSetup(
			ApplicationTestSetup.setupApplication(
				new CustomPropertiesSetup(migrationTest, "/WEB-INF/conf/test-migrate-app-with-h2.xml"),
				KBType.KB_H2));
	}
}