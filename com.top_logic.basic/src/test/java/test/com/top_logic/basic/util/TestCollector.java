/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.io.File;
import java.util.ServiceLoader;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Plugin for {@link AbstractBasicTestAll}.
 * 
 * @see ServiceLoader
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TestCollector {

	/**
	 * Creates a test when only the given file is tested.
	 * 
	 * @param targetFile
	 *        The file to test.
	 * @return A {@link Test} for the given file, or <code>null</code> when the file type is not
	 *         supported.
	 */
	Test getTestsForFile(File targetFile);

	/**
	 * Adds tests for files in the given directory.
	 * 
	 * @param suite
	 *        The {@link TestSuite} to add tests to.
	 * @param testDir
	 *        Directory to test.
	 * @param recursive
	 *        Whether the also tests for sub directory must be added.
	 */
	void addTestForDirectory(TestSuite suite, File testDir, boolean recursive);

	/**
	 * Adds tests that do not depend on the current module under test.
	 * 
	 * @param suite
	 *        {@link TestSuite} to add tests to.
	 */
	void addModuleIndependentTests(TestSuite suite);

}