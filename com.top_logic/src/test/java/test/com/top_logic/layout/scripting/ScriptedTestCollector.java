/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;

import static java.util.Arrays.*;
import static java.util.Objects.*;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.SimpleTestFactory;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.util.AbstractBasicTestAll;
import test.com.top_logic.basic.util.ScriptedTestUnit;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.io.DirectoriesOnlyFilter;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.file.FileNameComparator;
import com.top_logic.layout.scripting.util.ScriptFileFilter;

/**
 * A {@link Provider} that creates {@link ScriptedTest}s for the given {@link File} or directory,
 * wrapped into an {@link ApplicationTestSetup}.
 * 
 * <p>
 * The scripts directly contained in one directory form a {@link ScriptedTestUnit} keyed by the
 * directory path relative to the module directory.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptedTestCollector implements Provider<Test> {

	private static final Comparator<File> SCRIPT_COMPARATOR = new FileNameComparator(createScriptCollator());

	private final File _target;

	private final boolean _recursive;

	/**
	 * Creates a {@link ScriptedTestCollector} for the given target {@link File}.
	 * 
	 * @param target
	 *        Is not allowed to be null.
	 */
	public ScriptedTestCollector(File target) {
		this(target, true);
	}

	/**
	 * Creates a {@link ScriptedTestCollector} for the given target {@link File}.
	 * 
	 * @param target
	 *        Is not allowed to be null.
	 * @param recursive
	 *        Whether the tests in sub-directories should be included.
	 */
	public ScriptedTestCollector(File target, boolean recursive) {
		_target = requireNonNull(target);
		_recursive = recursive;
	}

	@Override
	public Test get() {
		try {
			return getScriptedTestsUnsafe();
		} catch (RuntimeException | Error ex) {
			String message = "Failed to collect the scripted tests. Cause: " + ex.getMessage();
			return SimpleTestFactory.newBrokenTest("Collection of ScriptedTests", new RuntimeException(message, ex));
		}
	}

	private Test getScriptedTestsUnsafe() {
		TestSuite suite = new TestSuite("ScriptedTests");
		if (_recursive) {
			suite.addTest(getScriptedTestsRecursively(_target));
		} else {
			addScriptedTestsInDir(_target, suite);
		}
		return ApplicationTestSetup.setupApplication(TestUtils.createIsolatingSuite(suite));
	}

	private Test getScriptedTestsRecursively(File targetFile) {
		TestSuite suite = new TestSuite(targetFile.getName());
		addScriptedTestsInSubdirs(targetFile, suite);
		addScriptedTestsInDir(targetFile, suite);
		return suite;
	}

	private void addScriptedTestsInSubdirs(File targetFile, TestSuite suite) {
		File[] subDirectories = targetFile.listFiles(DirectoriesOnlyFilter.INSTANCE);
		if (subDirectories == null) {
			return;
		}
		sort(subDirectories, SCRIPT_COMPARATOR);
		for (File subDirectory : subDirectories) {
			Test nestedSuite = getScriptedTestsRecursively(subDirectory);
			if (nestedSuite.countTestCases() == 0) {
				// Avoid empty TestSuites, as JUnit complains about them.
				continue;
			}
			suite.addTest(nestedSuite);
		}
	}

	private void addScriptedTestsInDir(File targetFile, TestSuite suite) {
		File[] scripts = targetFile.listFiles(ScriptFileFilter.INSTANCE);
		if (scripts == null || scripts.length == 0) {
			return;
		}
		sort(scripts, SCRIPT_COMPARATOR);
		ScriptedTestUnit unit = new ScriptedTestUnit(unitKey(targetFile));
		for (File normalFile : scripts) {
			unit.addTest(XmlScriptedTestUtil.createTest(normalFile));
		}
		suite.addTest(unit);
	}

	private static String unitKey(File directory) {
		String path = FileUtilities.canonicalize(directory).getPath();
		String modulePath = AbstractBasicTestAll.MODULE_LAYOUT.getModuleDir().getPath();
		if (path.startsWith(modulePath + File.separator)) {
			path = path.substring(modulePath.length() + 1);
		}
		return path.replace(File.separatorChar, '/');
	}

	private static Collator createScriptCollator() {
		Collator result = Collator.getInstance(Locale.ENGLISH);
		/* Make the collator case-insensitive */
		result.setStrength(Collator.SECONDARY);
		return result;
	}

}
