/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.FileUtilities;

/**
 * Test for the selection of the classes checked by {@link TestI18NConstantsTranslations}
 * ({@link TestI18NConstantsTranslations#isModuleLocation(URL, File)}).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestI18NConstantsTranslationsModuleSelection extends BasicTestCase {

	private File _root;

	private File _moduleDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir(TestI18NConstantsTranslationsModuleSelection.class.getSimpleName());
		_moduleDir = mkdirs(new File(_root, "module"));
	}

	@Override
	protected void tearDown() throws Exception {
		FileUtilities.deleteR(_root);
		super.tearDown();
	}

	/**
	 * The class directories of the module itself are checked.
	 */
	public void testOwnClassDirectories() throws IOException {
		assertTrue(isModuleLocation(mkdirs(new File(_moduleDir, "target/classes"))));
		assertTrue(isModuleLocation(mkdirs(new File(_moduleDir, "target/test-classes"))));
	}

	/**
	 * The class directory of another module in the same reactor is not checked, also when given
	 * relative to the module directory.
	 */
	public void testOtherModuleClassDirectory() throws IOException {
		mkdirs(new File(_root, "other/target/classes"));

		assertFalse(isModuleLocation(new File(_moduleDir, "../other/target/classes")));
	}

	/**
	 * A neighbor module whose name starts with the name of the tested module is not part of it.
	 */
	public void testNeighborWithNamePrefix() throws IOException {
		assertFalse(isModuleLocation(mkdirs(new File(_root, "module.db/target/classes"))));
	}

	/**
	 * A JAR from a Maven repository is not checked.
	 */
	public void testJar() throws IOException {
		File jar = new File(mkdirs(new File(_root, "repository/com/example/lib/1.0")), "lib-1.0.jar");
		assertTrue(jar.createNewFile());

		assertFalse(isModuleLocation(jar));
	}

	/**
	 * A location that is no file (or none at all) is not checked.
	 */
	public void testNoFileLocation() throws IOException {
		assertFalse(TestI18NConstantsTranslations.isModuleLocation(null, _moduleDir));
		assertFalse(TestI18NConstantsTranslations.isModuleLocation(new URL("http://localhost/classes/"), _moduleDir));
	}

	/**
	 * In the test run of this module (working directory is the module directory), the test classes
	 * of this module belong to it, but a class from a JAR does not.
	 */
	public void testWorkingDirectory() {
		File workingDir = new File(".");
		assertTrue(TestI18NConstantsTranslations.isModuleClass(TestI18NConstantsTranslationsModuleSelection.class,
			workingDir));
		assertFalse(TestI18NConstantsTranslations.isModuleClass(Test.class, workingDir));
	}

	private boolean isModuleLocation(File codeSource) throws IOException {
		return TestI18NConstantsTranslations.isModuleLocation(codeSource.toURI().toURL(), _moduleDir);
	}

	private static File mkdirs(File dir) {
		assertTrue(dir.mkdirs());
		return dir;
	}

	/**
	 * Creates the test suite of this class.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestI18NConstantsTranslationsModuleSelection.class));
	}

}
