/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.io.FileUtilities.*;
import static java.util.Arrays.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.DirectoriesOnlyFilter;
import com.top_logic.basic.io.file.filter.JavaFileFilter;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Checks that no class has a name that is case-insensitive equal to a sub-package of the same
 * package.
 * <p>
 * There is a bug in some compilers that causes them to fail to compile JSPs referencing a class
 * from such a sub-package. See Ticket #19577.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestNameClashClassVsPackage extends TestSuite {

	public static class FailureMarker extends TestCase {

		private final String _package;

		private final String _java;

		public FailureMarker(String packageName, String javaName) {
			super(packageName);
			_package = packageName;
			_java = javaName;
		}

		@Override
		protected void runTest() throws Throwable {
			fail("Ticket #19577: Package '" + _package + "' and Java class '" + _java
				+ "' have names that would conflict with certain buggy JSP compilers.");
		}

	}

	public interface Config extends ConfigurationItem {

		String IGNORE = "ignore";

		@Name(IGNORE)
		@ListBinding(tag = "entry", attribute = "value")
		List<String> getIgnore();

	}

	private Set<String> _ignoredPackages;

	private final File _srcDirectory;

	public TestNameClashClassVsPackage() {
		_ignoredPackages = getIgnoredPackages();
		_srcDirectory = canonicalize(new File(ModuleLayoutConstants.SRC_MAIN_DIR));
		addAll(buildTests());
	}

	private Set<String> getIgnoredPackages() {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		return new HashSet<>(config.getIgnore());
	}

	private List<Test> buildTests() {
		List<Test> result = new ArrayList<>();
		List<File> packages = getDirectoriesRecursively(_srcDirectory);
		for (File packageFile : packages) {
			if (isIgnoredPackage(packageFile)) {
				continue;
			}
			/* Directly asking for a file with the name of the package doesn't work, as each
			 * possible capitalization would have to be checked. It is therefore easier to just
			 * check each file in the directory whether it is case-insensitive-equal to the package. */
			File[] javaFiles = packageFile.getParentFile().listFiles(JavaFileFilter.INSTANCE);
			for (File javaFile : javaFiles) {
				String className = getClassName(javaFile);
				if (className.equalsIgnoreCase(packageFile.getName())) {
					result.add(createFailureMarker(packageFile, javaFile));
					break;
				}
			}
		}
		if (result.isEmpty()) {
			result.add(createSuccess());
		}
		return result;
	}

	private boolean isIgnoredPackage(File packageFile) {
		return _ignoredPackages.contains(toPackage(packageFile));
	}

	private FailureMarker createFailureMarker(File packageFile, File javaFile) {
		return new FailureMarker(toPackage(packageFile), toPackage(javaFile));
	}

	private String toPackage(File file) {
		String result = file.getName().replace(JavaFileFilter.JAVA_FILE_ENDING, "");
		File current = file.getParentFile();
		while (!isSrcDirectory(current)) {
			result = current.getName() + "." + result;
			current = current.getParentFile();
		}
		return result;
	}

	private boolean isSrcDirectory(File current) {
		return _srcDirectory.equals(current);
	}

	private String getClassName(File javaFile) {
		String name = javaFile.getName();
		int fileEndingLength = JavaFileFilter.JAVA_FILE_ENDING.length();
		return name.substring(0, name.length() - fileEndingLength);
	}

	private List<File> getDirectoriesRecursively(File parent) {
		List<File> result = new ArrayList<>();
		List<File> directSubDirectories = getDirectories(parent);
		result.addAll(directSubDirectories);
		for (File subDirectory : directSubDirectories) {
			result.addAll(getDirectoriesRecursively(subDirectory));
		}
		return result;
	}

	private List<File> getDirectories(File parent) {
		if (!parent.exists()) {
			return Collections.emptyList();
		}
		return asList(parent.listFiles(DirectoriesOnlyFilter.INSTANCE));
	}

	private Test createSuccess() {
		return SimpleTestFactory.newSuccessfulTest(getClass().getSimpleName());
	}

	private void addAll(List<Test> tests) {
		for (Test test : tests) {
			addTest(test);
		}
	}

	public static Test suite() {
		return ConfigLoaderTestUtil.INSTANCE.runWithLoadedConfig(() -> suiteWithConfig());
	}

	public static Test suiteWithConfig() {
		return new TestNameClashClassVsPackage();
	}

}
