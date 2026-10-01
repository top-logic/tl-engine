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
import test.com.top_logic.basic.ScratchDirectory;
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
 * <p>
 * The collected tests are controlled by the following system properties:
 * </p>
 * <dl>
 * <dt>{@link #TARGET_PROPERTY}</dt>
 * <dd>A test directory or a single test file (Java test class or script) to run instead of all
 * tests of the module.</dd>
 * <dt>{@link #RECURSIVE_PROPERTY}</dt>
 * <dd>Whether a directory given in {@link #TARGET_PROPERTY} is searched recursively.</dd>
 * <dt>{@link ShardSelection#PROPERTY}</dt>
 * <dd>Whether all tests run, all tests except the scripted ones, or only the scripted tests of one
 * shard, see {@link ShardSelection}. With a directory in
 * {@link #TARGET_PROPERTY}, the selection applies to the tests of that directory. With a single
 * file in {@link #TARGET_PROPERTY}, the selection is ignored.</dd>
 * <dt>{@link ShardSelection#MODULES_PROPERTY}</dt>
 * <dd>The modules whose scripted tests are distributed over the same shards, see
 * {@link ShardSelection#moduleOffset(String, String)}.</dd>
 * <dt>{@link DBSelection#PROPERTY}</dt>
 * <dd>Whether all tests run, only the tests bound to one worker database, or all tests except the
 * ones bound to a worker database, see {@link DBSelection}. Applies like
 * {@link ShardSelection#PROPERTY} to a directory in {@link #TARGET_PROPERTY} and is ignored for a
 * single file.</dd>
 * <dt>{@link DBSelection#WORKERS_PROPERTY}</dt>
 * <dd>The databases that run their tests in separate runs, see
 * {@link DBSelection#defaultWorkers()}.</dd>
 * <dt>{@link ScratchDirectory#PROPERTY}</dt>
 * <dd>The directory for temporary test files, see {@link ScratchDirectory}.</dd>
 * </dl>
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
	 * System property selecting a test directory or file to run instead of all tests of the
	 * module.
	 */
	public static final String TARGET_PROPERTY = "TestAll.target";

	/**
	 * System property selecting whether a directory given in {@link #TARGET_PROPERTY} is searched
	 * recursively.
	 */
	public static final String RECURSIVE_PROPERTY = "TestAll.recursive";

	/**
	 * Constant for invoking a main method without arguments.
	 */
	protected static final String[] NO_ARGS = new String[0];

	/**
	 * The scripted tests to run, parsed from {@link ShardSelection#PROPERTY}.
	 */
	private ShardSelection _scripted = ShardSelection.ALL;

	/**
	 * The database tests to run, parsed from {@link DBSelection#PROPERTY}.
	 */
	private DBSelection _db = DBSelection.ALL;

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
		ScratchDirectory.applyStorageDefault();
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
		} catch (RuntimeException | Error ex) {
			String testName = getClass().getSimpleName();
			String message = "Failed to build the test suite. Cause: " + ex.getMessage();
			// A suite as root: A single test case as root is not reported by the JUnit platform,
			// since its class is not the TestAll class selected by the test run.
			TestSuite suite = new TestSuite(testName);
			suite.addTest(SimpleTestFactory.newBrokenTest(testName, new RuntimeException(message, ex)));
			return suite;
		}
	}

	private Test getTests() {
		ServiceLoader<TestCollector> testCollectors = ServiceLoader.load(TestCollector.class);
		_scripted = ShardSelection.fromSystemProperty();
		_db = DBSelection.fromSystemProperties();
		String targetPath = getTargetPath();
		if (isEmpty(targetPath)) {
			return getAllTests(testCollectors);
		} else {
			return getTests(testCollectors, targetPath);
		}
	}

	private String getTargetPath() {
		return System.getProperty(TARGET_PROPERTY);
	}

	private Test getAllTests(Iterable<TestCollector> collectors) {
		TestSuite suite = new TestSuite("TestAll for " + MODULE_LAYOUT.getModuleDir().getName());
		if (_scripted.includesNonScripted() && _db.includesUnbound()) {
			suite.addTest(getInternalModuleIndependentTests(collectors));
		}
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
		_scripted.apply(ShardSelection.moduleOffset(MODULE_LAYOUT.getModuleDir().getName()), suite);
		_db.apply(suite);
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
		return Boolean.parseBoolean(System.getProperty(RECURSIVE_PROPERTY));
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
