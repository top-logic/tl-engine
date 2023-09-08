/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.File;
import java.util.ServiceLoader;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ConfigLoaderTestUtil;
import test.com.top_logic.basic.LoggingTestSetup;
import test.com.top_logic.basic.SimpleTestFactory;
import test.com.top_logic.basic.TestComment;
import test.com.top_logic.basic.TestLayoutsNormalized;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.jsp.TestJSPContent;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayout;
import com.top_logic.basic.tooling.ModuleLayoutConstants;


/**
 * Abstract superclass of the <code>TestAll</code> in the "test" package of every module.
 * <p>
 * Use this class only for modules not depending on the "com.top_logic" module. For those, use
 * <code>AbstractTestAll</code>.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractBasicTestAll {
	
	/**
	 * Configuration options for {@link AbstractBasicTestAll}.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * Whether to activate {@link TestComment}.
		 */
		@Name("test-comment")
		@BooleanDefault(true)
		boolean getTestComment();

		/**
		 * Whether to activate {@link TestLayoutsNormalized}.
		 */
		@Name("test-layouts-normalized")
		@BooleanDefault(true)
		boolean getTestLayoutsNormalized();

		/**
		 * Whether to activate {@link TestJSPContent}.
		 */
		@Name("test-jsp-content")
		@BooleanDefault(true)
		boolean getTestJSPContent();

	}

	/** {@link ModuleLayout} defining the module structure for this {@link AbstractBasicTestAll}. */
	public static ModuleLayout MODULE_LAYOUT;
	static {
		MODULE_LAYOUT =
			new ModuleLayout(new AssertProtocol(AbstractBasicTestAll.class.getName()), new File("."));
	}

	/**
	 * Constant for invoking a main method without arguments.
	 */
	protected static final String[] NO_ARGS = new String[0];

	/**
	 * Creates an {@link AbstractBasicTestAll} and prepares the system for tests.
	 * <p>
	 * Prints the Java version, enables assertions and configures the {@link Logger}.
	 * </p>
	 */
	public AbstractBasicTestAll() {
		System.out.println("Java Version    : " + System.getProperty ("java.vm.version"));
		// enforce usage of assertions
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		// Configure the Logger according to system property Logger4.STDOUT_LEVEL_PROPERTY. (Default: Only errors and worse)
		Logger.configureStdout();
	}
	
	/**
	 * Builds the {@link Test} for the current module.
	 * <p>
	 * The "current" module is determined by the current working directory.
	 * </p>
	 */
	public final Test buildSuite() {
		return ConfigLoaderTestUtil.INSTANCE.runWithLoadedConfig(() -> {
			Test t = buildSuiteInternal();
			t = LoggingTestSetup.newLoggingTestSetup(t);
			return t;
		});
	}

	final Test buildSuiteInternal() {
		try {
			return getTests();
		} catch (ThreadDeath ex) {
			throw ex;
		} catch (RuntimeException | Error ex) {
			String testName = getClass().getSimpleName();
			String message = "Failed to build the test suite. Cause: " + ex.getMessage();
			return SimpleTestFactory.newBrokenTest(testName, new RuntimeException(message, ex));
		}
	}

	private Test getTests() {
		ServiceLoader<TestCollector> testCollectors = ServiceLoader.load(TestCollector.class);
		String targetPath = getTargetPath();
		if (isEmpty(targetPath)) {
			return getAllTests(testCollectors);
		} else {
			return getTests(testCollectors, targetPath);
		}
	}

	private String getTargetPath() {
		String propertySelectedTest = "TestAll.target";
		return System.getProperty(propertySelectedTest);
	}

	private Test getAllTests(Iterable<TestCollector> collectors) {
		TestSuite suite = new TestSuite("TestAll for " + MODULE_LAYOUT.getModuleDir().getName());
		suite.addTest(getInternalModuleIndependentTests(collectors));
		suite.addTest(getInternalModuleSpecificTests(collectors));
		return suite;
	}

	private Test getTests(Iterable<TestCollector> collectors, String targetPath) {
		File targetFile = FileUtilities.canonicalize(new File(targetPath));
		if (!targetFile.exists()) {
			throw errorNoSuchFile(targetFile);
		}
		if (targetFile.isDirectory()) {
			boolean collectRecursively = shouldCollectRecursively();
			return getInternalTestsForDirectory(collectors, targetFile, collectRecursively);
		}
		if (targetFile.isFile()) {
			return getTestsForFile(collectors, targetFile);
		}
		throw errorUnsupportedFileKind(targetFile);
	}

	private RuntimeException errorNoSuchFile(File targetFile) {
		String message = "Collecting the tests failed. The target file does not exist."
			+ " File: '" + targetFile.getPath() + "'";
		throw new IllegalArgumentException(message);
	}

	/**
	 * Creates a test name for the given file.
	 * 
	 * @param testPath
	 *        {@link File} to create test name for.
	 */
	public static String createTestName(File testPath) {
		String fullPath = FileUtilities.canonicalize(testPath).getPath();

		checkInSourceDir(fullPath);

		/* Cut of everything before the project name, as it is irrelevant. */
		String workspacePath = MODULE_LAYOUT.getWorkspaceDir().getPath();
		String relativePath = fullPath.substring(workspacePath.length() + 1);
		/* Always use "\" instead of "/" in Test names. */
		return relativePath.replaceAll("\\\\", "/");
	}

	private static RuntimeException errorNoSourceDirectory(String filePath) {
		StringBuilder msg = new StringBuilder();
		msg.append("The file is not in the '");
		msg.append(ModuleLayoutConstants.SRC_TEST_DIR);
		msg.append("' directory: ");
		msg.append(MODULE_LAYOUT.getTestSourceDir());
		msg.append(". File: '");
		msg.append(filePath);
		msg.append("'");
		throw new IllegalArgumentException(msg.toString());
	}

	static void checkInSourceDir(String path) {
		if (!path.startsWith(MODULE_LAYOUT.getTestSourceDir().getPath())) {
			throw errorNoSourceDirectory(path);
		}
	}

	private Test getInternalTestsForDirectory(Iterable<TestCollector> collectors, File directory, boolean recursive) {
		File testDirectory = toTestDirectory(directory);
		if (testDirectory == null) {
			throw errorInvalidDirectory(directory);
		}
		TestSuite suite = new TestSuite("Tests in '" + createTestName(testDirectory) + "'");
		if (testDirectory.exists()) {
			for (TestCollector collector : collectors) {
				collector.addTestForDirectory(suite, testDirectory, recursive);
			}
		}
		if (suite.countTestCases() == 0) {
			String testName = "No tests in directory '" + createTestName(testDirectory) + "'.";
			suite.addTest(SimpleTestFactory.newSuccessfulTest(testName));
		}
		TestUtils.rearrange(suite);
		return suite;
	}

	private RuntimeException errorInvalidDirectory(File targetDirectory) {
		throw new IllegalArgumentException("The given directory represent neither a <i>TopLogic</i> module"
			+ " nor a source-directory with in one nor a test package. Directory: " + targetDirectory.getPath());
	}

	private boolean shouldCollectRecursively() {
		return Boolean.parseBoolean(System.getProperty("TestAll.recursive"));
	}

	private File toTestDirectory(File directory) {
		File result = FileUtilities.canonicalize(directory);
		if (!isInProjectDirectory(result)) {
			return null;
		}
		if (isProjectDirectory(result)) {
			result = MODULE_LAYOUT.getTestSourceDir();
		}
		if (isTestSourceDirectory(result)) {
			result = testDir();
		}
		if (!isInTestDirectory(result)) {
			return null;
		}
		return result;
	}

	private File testDir() {
		return new File(MODULE_LAYOUT.getTestSourceDir(), "test");
	}

	private boolean isInProjectDirectory(File targetFile) {
		return isInDirectory(targetFile, MODULE_LAYOUT.getModuleDir());
	}

	private boolean isProjectDirectory(File file) {
		return MODULE_LAYOUT.getModuleDir().equals(file);
	}

	private boolean isTestSourceDirectory(File file) {
		return MODULE_LAYOUT.getTestSourceDir().equals(file);
	}

	private boolean isInTestDirectory(File targetFile) {
		return isInDirectory(targetFile, testDir());
	}

	private boolean isInDirectory(File targetFile, File directory) {
		File file = targetFile;
		while (file != null) {
			if (file.equals(directory)) {
				return true;
			}
			file = file.getParentFile();
		}
		return false;
	}

	/**
	 * Creates the {@link Test} for the given {@link File}.
	 * <p>
	 * Subclasses overriding this method have to call <code>super</code>.
	 * </p>
	 * <p>
	 * If the given {@link File} is not a supported file type, an exception is thrown.
	 * </p>
	 */
	protected Test getTestsForFile(Iterable<TestCollector> collectors, File targetFile) {
		Test result = null;
		for (TestCollector collector : collectors) {
			result = collector.getTestsForFile(targetFile);
			if (result != null) {
				// First wins
				break;
			}
		}
		if (result == null) {
			throw errorUnsupportedFileType(targetFile);
		}
		return result;
	}

	/** The file has an unsupported content type. */
	private RuntimeException errorUnsupportedFileType(File targetFile) {
		String message = "Collecting the tests failed. The target file is neither a Java file nor a scripted-test file."
			+ " Target file: '" + targetFile + "'";
		throw new IllegalArgumentException(message);
	}

	/** The file is neither a directory nor a normal file. */
	private RuntimeException errorUnsupportedFileKind(File targetFile) {
		String message = "Collecting the tests failed. The target file is neither a directory nor a normal file."
			+ " Target file: '" + targetFile + "'";
		throw new IllegalArgumentException(message);
	}

	/**
	 * {@link Test Tests} that are independent of the module.
	 * <p>
	 * For example a test whether all JSPs can be compiled.
	 * </p>
	 */
	protected TestSuite getInternalModuleIndependentTests(Iterable<TestCollector> collectors) {

		TestSuite suite = new TestSuite("Module Independent Tests");
		for (TestCollector collector : collectors) {
			collector.addModuleIndependentTests(suite);
		}
		return suite;
	}

	/**
	 * {@link Test Tests} that are specific to the current module.
	 */
	private Test getInternalModuleSpecificTests(Iterable<TestCollector> collectors) {
		TestSuite suite = new TestSuite("Module specific Tests");
		suite.addTest(getInternalTestsForDirectory(collectors, MODULE_LAYOUT.getModuleDir(), true));
		if (suite.countTestCases() == 0) {
			suite.addTest(SimpleTestFactory.newSuccessfulTest("No module specific tests."));
		}
		TestUtils.rearrange(suite);
		return suite;
	}

	/**
	 * The web application unter test.
	 */
	public static File webapp() {
		return MODULE_LAYOUT.getWebappDir();
	}

	/**
	 * Returns the Layout directory, as in {@link ModuleLayout#getLayoutDir()} in
	 * {@link #MODULE_LAYOUT}. In contrast to that method the folder may not exist.
	 */
	public static File potentiallyNotExistingLayoutDir() {
		return new File(webapp(), ModuleLayoutConstants.LAYOUT_PATH);
	}
	
}
