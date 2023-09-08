/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.io.File;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestComment;
import test.com.top_logic.basic.TestLayoutsNormalized;
import test.com.top_logic.basic.TestNameClashClassVsPackage;
import test.com.top_logic.basic.jsp.TestCompileJSPs;
import test.com.top_logic.basic.jsp.TestJSPContent;
import test.com.top_logic.basic.junit.DirectoryLocalTestCollector;
import test.com.top_logic.basic.test.TestFileNames;
import test.com.top_logic.basic.util.AbstractBasicTestAll.GlobalConfig;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.RuntimeExceptionProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * {@link TestCollector} for basic tests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BasicTestCollector implements TestCollector {

	@Override
	public Test getTestsForFile(File targetFile) {
		if (isJavaFile(targetFile)) {
			return getJavaTest(targetFile);
		}
		return null;
	}

	private boolean isJavaFile(File file) {
		String name = file.getName();
		return name.endsWith(".java") || name.endsWith(".class");
	}

	private Test getJavaTest(File targetFile) {
		String path = targetFile.getPath();
		String srcMark = File.separator + ModuleLayoutConstants.SRC_TEST_DIR + File.separator;
		String localPath = path.substring(path.indexOf(srcMark) + srcMark.length());
		assert localPath.indexOf(srcMark) == -1;
		// Remove file ending:
		String pathWithoutEnding = localPath.substring(0, localPath.lastIndexOf("."));
		String qualifiedJavaName = pathWithoutEnding.replace(File.separator, ".");
		TestSuite suite = new TestSuite(AbstractBasicTestAll.createTestName(targetFile));
		DirectoryLocalTestCollector.addTest(qualifiedJavaName, suite, RuntimeExceptionProtocol.INSTANCE);
		return suite;
	}

	@Override
	public void addTestForDirectory(TestSuite suite, File testDir, boolean recursive) {
		suite.addTest(getTestsForPackage(toPackage(testDir), recursive));
	}

	private Test getTestsForPackage(String packageName, boolean recursive) {
		return BasicTestCase.createTests(packageName, new LogProtocol(AbstractBasicTestAll.class), recursive);
	}

	/**
	 * @param targetDirectory
	 *        Has to be within the source directory of a <i>TopLogic</i> project.
	 */
	private String toPackage(File targetDirectory) {
		assert targetDirectory.exists() : "Directory does not exist: " + targetDirectory.getPath();
		assert targetDirectory.isDirectory() : "Not a directory: " + targetDirectory.getPath();
		targetDirectory = FileUtilities.canonicalize(targetDirectory);
		if (targetDirectory.equals(AbstractBasicTestAll.MODULE_LAYOUT.getTestSourceDir())) {
			// default package
			return StringServices.EMPTY_STRING;
		}
		String path = targetDirectory.getPath();

		AbstractBasicTestAll.checkInSourceDir(path);
		String sourcePath = AbstractBasicTestAll.MODULE_LAYOUT.getTestSourceDir().getPath();
		String[] directories = path.substring(sourcePath.length() + 1).split(Pattern.quote(File.separator));
		return StringServices.join(directories, '.');
	}

	@Override
	public void addModuleIndependentTests(TestSuite suite) {
		GlobalConfig config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
		suite.addTest(TestFileNames.suite());
		if (config.getTestComment()) {
			suite.addTest(TestComment.suite());
		}
		suite.addTest(TestNameClashClassVsPackage.suiteWithConfig());
		if (config.getTestLayoutsNormalized()) {
			suite.addTest(TestLayoutsNormalized.suite());
		}
		if (config.getTestJSPContent()) {
			suite.addTest(TestJSPContent.suite());
		}
		suite.addTest(TestCompileJSPs.suite());
		suite.addTest(TestI18NConstantsTranslations.suite());

	}

}

