/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.junit;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.GenericTest;
import test.com.top_logic.basic.SimpleTestFactory;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Collects all JUnit tests in the given directory.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DirectoryLocalTestCollector {

	private static final String JAVA_EXT = ".java";

	static class TestClassFilter implements FileFilter {

		/**
		 * Pattern to identify names of files which are potentially tests. It matches only files ending
		 * with `.class`. Moreover the name must not contain `$`, end with `Test` or start with `Test`
		 * followed by an uppercase letter or a number.
		 */
		private static final String TEST_CLASS_NAME_PATTERN =
			"^(?:(?:Test[A-Z0-9].*)|(?:.*Test))" + Pattern.quote(JAVA_EXT) + "$";

		private String[] excludeFiles = new String[] { "TestAll.java", "TestAllLive.java" };

		@Override
		public boolean accept(File pathname) {
			for (String exclude : excludeFiles) {
				if (pathname.getName().equals(exclude)) {
					return false;
				}
			}
			return pathname.getName().matches(TEST_CLASS_NAME_PATTERN);
		}
	}

	private static final String SUITE_METHOD_NAME = "suite";

	private static final FileFilter testClassFilterIncl = new TestClassFilter() {

		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}
			return super.accept(pathname);
		}
	};

	private static final FileFilter testClassFilterExcl = new TestClassFilter();

	private static final Comparator<File> fileNameComparator = new Comparator<>() {

		@Override
		public int compare(File file1, File file2) {
			return file1.getName().compareTo(file2.getName());
		}
	};

	private final Protocol _log;

	private final boolean _includeSubPackages;

	/**
	 * @param log
	 *        The {@link Protocol} which is used to log non-error-information. Errors are passed via
	 *        special error tests that fail.
	 * @param includeSubPackages
	 *        whether "subpackages" should be included, i.e. the tests of the packages whose names
	 *        start with the given package name.
	 */
	public DirectoryLocalTestCollector(Protocol log, boolean includeSubPackages) {
		_log = log;
		_includeSubPackages = includeSubPackages;
	}

	/**
	 * This method creates a test for all test classes in the given package, i.e
	 * all public classes in the given package are called for a &quot;public
	 * static&quot; method called suite() and returning a {@link Test}, and the
	 * returned {@link Test} is added to the result test.
	 * Only tests in the current project are found!
	 * 
	 * @param pckgName
	 *        the name of the package under test
	 */
	public Test createTests(String pckgName) {
		_log.info("Creating tests for package '" + pckgName + "'.", Protocol.VERBOSE);
		String path = pckgName.replace('.', '/');
		File workingDir = new File(".");
		File srcDir = new File(workingDir, ModuleLayoutConstants.SRC_TEST_DIR);
		File packageDir = new File(srcDir, path);
		if (!packageDir.exists()) {
			return new TestSuite(pckgName);
		}
		return createTests(packageDir, pckgName);
	}

	private Test createTests(File packageDirectory, String pckgName) {
		TestSuite result = new TestSuite(pckgName);
		File[] contents =
			packageDirectory.listFiles(_includeSubPackages ? testClassFilterIncl : testClassFilterExcl);

		// Sort the found files by some comparator to have a deterministic
		// order. It is not ensured that the array returned by
		// listFiles(FileFilter) is sorted by any order. 
		Arrays.sort(contents, fileNameComparator);

		for (File content : contents) {
			if (_includeSubPackages && content.isDirectory()) {
				String subPckg = pckgName + '.' + content.getName();
				Test subPckgTests = createTests(content, subPckg);
				if (subPckgTests != null && subPckgTests.countTestCases() > 0) {
					result.addTest(subPckgTests);
				}
				continue;
			}

			int suffixIndex = content.getName().indexOf(JAVA_EXT);
			String className = content.getName().substring(0, suffixIndex);
			String qualifiedClassName = pckgName + '.' + className;
			addTest(qualifiedClassName, result, _log);
		}
		return result;
	}

	/**
	 * Add the {@link Test} with the given class name to the given {@link TestSuite}.
	 * 
	 * @param qualifiedClassName
	 *        The fully qualified class name of the {@link Test}.
	 * @param result
	 *        Is not allowed to be null.
	 * @param log
	 *        Is not allowed to be null. Is used for non-critical information. (Infos, but not
	 *        errors.)
	 */
	public static void addTest(String qualifiedClassName, TestSuite result, Protocol log) {
		try {
			Class<?> testClass = Class.forName(qualifiedClassName);

			// check class for publicity
			int classModifiers = testClass.getModifiers();
			if (!Modifier.isPublic(classModifiers)) {
				log.info("Creating no tests for " + qualifiedClassName + ": Class is not public.");
				return;
			}
			if (Modifier.isInterface(classModifiers)) {
				log.info("Creating no tests for " + qualifiedClassName + ": Class is an interface.");
				return;
			}

			// check whether class is currently deactivated
			DeactivatedTest annotation = testClass.getAnnotation(DeactivatedTest.class);
			if (annotation != null) {
				log.info("Creating no tests for " + qualifiedClassName + ": It is deactivated for reason: '"
					+ annotation.value() + "'");
				return;
			}

			Test suite;
			try {
				// returns the method with the name which has no parameters
				Method suiteMethod = testClass.getDeclaredMethod(SUITE_METHOD_NAME);

				// check method for correct modifiers
				int suiteModifiers = suiteMethod.getModifiers();
				if (!Modifier.isPublic(suiteModifiers) || !Modifier.isStatic(suiteModifiers)) {
					log.info("Creating no tests for " + qualifiedClassName + ": Method '" + SUITE_METHOD_NAME
						+ "' is not public static.");
					return;
				}
				// check the return type of suite method
				if (!Test.class.isAssignableFrom(suiteMethod.getReturnType())) {
					log.info("Creating no tests for " + qualifiedClassName + ": The return type of method '"
						+ SUITE_METHOD_NAME + "' is not '" + Test.class + "'.");
					return;
				}

				suite = (Test) suiteMethod.invoke(testClass);
				if (suite == null) {
					String message = "Method '" + qualifiedClassName + "." + SUITE_METHOD_NAME + "' returns null.";
					error(result, qualifiedClassName, message, new NullPointerException(message));
					return;
				}
			} catch (NoSuchMethodException ex) {
				log.info("Creating no tests for " + qualifiedClassName + ": No method '" + SUITE_METHOD_NAME
					+ "()'.",
					LogProtocol.VERBOSE);
				
				if (GenericTest.class.isAssignableFrom(testClass)) {
					log.info("Creating no tests for " + qualifiedClassName + ": Generic tests are not collected.",
						LogProtocol.VERBOSE);
					return;
				}

				int modifiers = testClass.getModifiers();
				if ((modifiers & Modifier.ABSTRACT) != 0) {
					log.info("Creating no tests for " + qualifiedClassName + ": Class is abstract.",
						LogProtocol.VERBOSE);
					return;
				}
				
				if ((modifiers & Modifier.PUBLIC) == 0) {
					String message = "Test class " + qualifiedClassName + " is not public.";
					error(result, qualifiedClassName, message, ex);
					return;
				}
				
				if (!TestCase.class.isAssignableFrom(testClass)) {
					String message = "Creating no tests for " + qualifiedClassName + ": Class does not implement '"
						+ Test.class + "'.";
					log.info(message, LogProtocol.VERBOSE);
					return;
				}
				
				suite = new TestSuite(testClass);
			}

			// number of tests which will be executed by the suite
			int numberOfTestsToRun = suite.countTestCases();
			if (numberOfTestsToRun > 0) {
				result.addTest(suite);
				log.info("Added tests from class '" + testClass + "'.");
			}
		} catch (ClassNotFoundException ex) {
			String message = "There is a file for a Java class '" + qualifiedClassName
				+ "' but there is no corresponding Java class.";
			error(result, qualifiedClassName, message, ex);
		} catch (SecurityException ex) {
			String message =
				"SecurityException when getting " + qualifiedClassName + "." + SUITE_METHOD_NAME + "().";
			error(result, qualifiedClassName, message, ex);
		} catch (IllegalArgumentException ex) {
			String message = SUITE_METHOD_NAME + "() method in " + qualifiedClassName + " could not be invoked.";
			error(result, qualifiedClassName, message, ex);
		} catch (IllegalAccessException ex) {
			String message = SUITE_METHOD_NAME + "() method in " + qualifiedClassName + " could not be invoked.";
			error(result, qualifiedClassName, message, ex);
		} catch (InvocationTargetException ex) {
			String message = SUITE_METHOD_NAME + "() method in " + qualifiedClassName + " could not be invoked.";
			error(result, qualifiedClassName, message, ex);
		} catch (Throwable ex) {
			error(result, qualifiedClassName, "Unexpected exception: " + ex.getMessage(), ex);
		}
	}

	private static void error(TestSuite result, String qualifiedClassName, String message, Throwable ex) {
		String testName = "Error when creating test from '" + qualifiedClassName + "'. ";
		result.addTest(SimpleTestFactory.newBrokenTest(true, message, ex, testName));
	}

	/**
	 * Is the child {@link File} a direct or indirect child the parent?
	 * <p>
	 * Converts both {@link File}s to their {@link File#getCanonicalPath() canonical form}.
	 * </p>
	 * 
	 * @param parent
	 *        is not allowed to be null.
	 * @param child
	 *        is not allowed to be null.
	 * @return false, if the child is not a direct or indirect child of the parent. false, if the
	 *         child is the parent.
	 */
	private boolean isWithin(File parent, File child) {
		try {
			return isWithinInternal(parent.getCanonicalFile(), child.getCanonicalFile());
		} catch (IOException ex) {
			throw new RuntimeException("Failed to canonicalize file. Cause: " + ex.getMessage(), ex);
		}
	}

	private boolean isWithinInternal(File parent, File child) {
		File parentOfChild = child.getParentFile();
		if (parentOfChild == null) {
			return false;
		}
		if (parent.equals(parentOfChild)) {
			return true;
		}
		return isWithinInternal(parent, parentOfChild);
	}

	/**
	 * Determines if the given method is regarded as test method
	 * 
	 * @param m
	 *        the method under test. must not be <code>null</code>
	 * 
	 * @return <code>true</code> iff the method is regarded as test method.
	 */
	private static boolean isTestMethod(Method m) {
		if (!Modifier.isPublic(m.getModifiers())) {
			return false;
		}
		String name = m.getName();
		Class<?>[] parameters = m.getParameterTypes();
		Class<?> returnType = m.getReturnType();
		return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
	}

}
