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
import test.com.top_logic.knowledge.KBSetup;
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
 */
public class AppMigrationTestSetup extends TestSetup {
	/**
	 * Creates a {@link AppMigrationTestSetup}.
	 */
	public AppMigrationTestSetup(Test test) {
		super(test);
	}

	private boolean _before;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_before = KBSetup.shouldCreateTables();
		KBSetup.setCreateTables(false);
		
		File testRoot = new File("./target/test-run");
		FileUtilities.deleteR(testRoot);
		FileUtilities.copyR(new File("./tmp"), testRoot);

		ModuleLicenceTestSetup.enableTableSetup();
	}

	@Override
	protected void tearDown() throws Exception {
		KBSetup.setCreateTables(_before);

		super.tearDown();
	}

	/**
	 * Wraps an application test using a database fixture in <code>tmp/h2</code>.
	 */
	public static Test suite(Test migrationTest) {
		return new AppMigrationTestSetup(
			ApplicationTestSetup.setupApplication(
				new CustomPropertiesSetup(migrationTest, "/WEB-INF/conf/test-migrate-app-with-h2.xml")));
	}
}