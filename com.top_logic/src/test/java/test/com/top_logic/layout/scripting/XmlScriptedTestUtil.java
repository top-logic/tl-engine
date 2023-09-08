/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting;

import java.io.File;
import java.util.Optional;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.SimpleTestFactory;
import test.com.top_logic.layout.scripting.ScriptedTest.ScriptedTestParameters;

import com.top_logic.basic.io.FileLocation;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.util.LazyActionProvider;

/**
 * Utility to build scripted application tests from XML scripts.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class XmlScriptedTestUtil {

	/**
	 * Creates a new {@link LazyActionProvider} that uses the file with the given name and path.
	 * <p>
	 * The {@link File} itself cannot be given to delay errors until the {@link ApplicationAction}
	 * is requested, i.e. until the test is executed. Otherwise, a mistake on {@link File}
	 * instantiation could kill the whole test tree creation.
	 * </p>
	 * 
	 * @param file
	 *        The location of the file. Must not be <code>null</code>.
	 */
	public static LazyActionProvider newActionProvider(FileLocation file) {
		return new FileLocationActionProvider(file);
	}

	/**
	 * Convenience shortcut for: {@link #createTest(ScriptedTestParameters, Class, String)} Uses the
	 * default {@link ScriptedTestParameters}.
	 * 
	 * @see #suite(ScriptedTestParameters, Class, String...)
	 */
	public static TestSuite suite(Class<?> testClass, String... testNames) {
		return suite(new ScriptedTestParameters(), testClass, testNames);
	}

	/**
	 * Creates a test suite for the the given context class consisting of the given named tests.
	 * 
	 * <p>
	 * It is expected, to find resources named "<test-class-name>-<test-name>.xml" in the directory
	 * of the given test class. The given resources are parsed and converted into a test suite that
	 * executes the action(s).
	 * </p>
	 * 
	 * @param testClass
	 *        The context test class. Must not be <code>null</code>.
	 * @param testNames
	 *        The names of the tests cases to create. Must not be <code>null</code>.
	 * @return Never <code>null</code>
	 */
	public static TestSuite suite(ScriptedTestParameters parameters, Class<?> testClass, String... testNames) {
		TestSuite suite = new TestSuite(testClass.getName());
		for (String testName : testNames) {
			suite.addTest(createTest(parameters, testClass, testName));
		}
		return suite;
	}

	/**
	 * Convenience shortcut for: {@link #createTest(ScriptedTestParameters, Class, String)}
	 * <p>
	 * Uses the default {@link ScriptedTestParameters}. <br/>
	 * Use {@link #suite(Class, String...)} instead, if possible, as it creates the complete suite
	 * with name and not just a single test.
	 * </p>
	 */
	public static Test createTest(Class<?> testClass, String testName) {
		return createTest(new ScriptedTestParameters(), testClass, testName);
	}

	/**
	 * Creates a {@link ScriptedTest} for the the given context class and test name.
	 * 
	 * <p>
	 * Use {@link #suite(ScriptedTestParameters, Class, String...)} instead, if possible, as it
	 * creates the complete suite with name and not just a single test. <br/>
	 * It is expected, to find resources named "<test-class-name>-<test-name>.xml" in the directory
	 * of the given test class. The given resource is parsed and converted into a
	 * {@link ScriptedTest} that executes the action(s).
	 * </p>
	 * 
	 * @param testClass
	 *        The context test class. Must not be <code>null</code>.
	 * @param testName
	 *        The names of the tests cases to create. Must not be <code>null</code>.
	 * @return Never <code>null</code>
	 */
	public static Test createTest(ScriptedTestParameters parameters, Class<?> testClass, String testName) {
		String fileName = createFileName(testClass, testName);
		FileLocation file = FileLocation.fileByContextClass(fileName, testClass);
		String fullTestName = testClass.getCanonicalName() + " " + testName;
		LazyActionProvider actionProvider = newActionProvider(file);
		return new ScriptedTest(actionProvider, fullTestName, parameters);
	}

	/**
	 * Variant of {@link #createTest(ScriptedTestParameters, File)} that does uses the default
	 * {@link ScriptedTestParameters}.
	 */
	public static Test createTest(File scriptXml) {
		return createTest(new ScriptedTestParameters(), scriptXml);
	}

	/**
	 * Creates a {@link ScriptedTest} from the given script-xml-file.
	 * <p>
	 * Does not create any {@link TestSetup}.
	 * </p>
	 * 
	 * @param parameters
	 *        Is not allowed to be null.
	 * @param scriptXml
	 *        Is not allowed to be null. The script xml.
	 * @return The {@link Test} that will execute the given script.
	 */
	public static Test createTest(ScriptedTestParameters parameters, File scriptXml) {
		try {
			return createTestInternal(parameters, scriptXml);
		} catch (ThreadDeath ex) {
			throw ex;
		} catch (RuntimeException | Error ex) {
			String testName = "Creating a ScriptedTest for file: '" + scriptXml.getName() + "'";
			String message = "Creating a ScriptedTest failed. Cause: " + ex.getMessage();
			return SimpleTestFactory.newBrokenTest(testName, new RuntimeException(message, ex));
		}
	}

	private static Test createTestInternal(ScriptedTestParameters parameters, File scriptXml) {
		if (!scriptXml.exists()) {
			throw errorNoSuchFile(scriptXml);
		}
		File canonicalFile = FileUtilities.canonicalize(scriptXml);
		LazyActionProvider actionProvider = createActionProvider(scriptXml);
		String testName = createTestName(canonicalFile);
		return new ScriptedTest(actionProvider, testName, parameters);
	}

	private static LazyActionProvider createActionProvider(File scriptXml) {
		return newActionProvider(FileLocation.fileFromConstructor(scriptXml.getPath()));
	}

	private static RuntimeException errorNoSuchFile(File scriptXml) {
		String message = "The file does not exist. File: '" + scriptXml.getPath() + "'";
		throw new IllegalArgumentException(message);
	}

	/**
	 * @param testPath
	 *        Has to be the canonical path.
	 */
	private static String createTestName(File testPath) {
		String fullPath = testPath.getPath();
		Optional<String> relativePath =
			mkRelative(File.separator + ModuleLayoutConstants.SRC_TEST_DIR + File.separator, fullPath);
		if (!relativePath.isPresent()) {
			relativePath = mkRelative(File.separator + ModuleLayoutConstants.WEB_INF_DIR + File.separator, fullPath);
			if (!relativePath.isPresent()) {
				relativePath = mkRelative(File.separator, fullPath);
				if (!relativePath.isPresent()) {
					relativePath = Optional.of(fullPath);
				}
			}
		}

		/* Always use "\" instead of "/" in Test names. */
		return relativePath.get().replaceAll("\\\\", "/");
	}

	private static Optional<String> mkRelative(String base, String fullPath) {
		int baseIndex = fullPath.indexOf(base);
		if (baseIndex == -1) {
			return Optional.empty();
		}
		return Optional.of(fullPath.substring(baseIndex + base.length()));
	}

	/**
	 * Generates the filename of the test script executed in <code>testClass</code> under the name
	 * <code>testName</code>.
	 */
	public static String createFileName(Class<?> testClass, String testName) {
		return testClass.getSimpleName() + "-" + testName + ".xml";
	}

}
