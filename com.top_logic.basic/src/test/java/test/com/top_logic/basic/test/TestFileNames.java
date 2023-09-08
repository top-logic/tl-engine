/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.test;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.col.filter.FilterFactory.*;
import static java.util.stream.Collectors.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.file.FileNameConvention;
import com.top_logic.basic.io.file.FileNameRule;

/**
 * Tests that all file names against a configurable set of {@link FileNameRule}.
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestFileNames extends TestCase {

	/** To test the whole workspace, set this to true. */
	private static final boolean TEST_COMPLETE_WORKSPACE = false;

	/**
	 * Only that many failures are reported at maximum.
	 * <p>
	 * If more than that many failures are found, something else went probably wrong. For example a
	 * {@link FileNameRule} might be wrong and fail for each file.
	 * </p>
	 */
	private static final long MAX_FAILURES = 1000;

	/** The single test in this {@link TestCase}. */
	public void testAllFilesAndRules() {
		String failureMessages = FileUtilities.walk(getProjectPath())
			.parallel()
			/* The "relativize" is necessary for two reason: The local file name is not enough,
			 * because the rules or their exceptions might be specific to folders. For example:
			 * Ignore files in "dist". And the absolute path is too much, as the path to the
			 * workspace does not have to comply with the file name rules. */
			.map(getProjectPath()::relativize)
			.flatMap(this::toFileRulePair)
			.filter(not(FileNameTest::compliesWithRule))
			.filter(not(FileNameTest::isException))
			.limit(MAX_FAILURES)
			.map(FileNameTest::createErrorMessage)
			.collect(joining(LINE_BREAK));
		if (!failureMessages.isEmpty()) {
			fail("The following files do not comply with the file name rules for this project: " + failureMessages
				+ "\nNote: Only the first " + MAX_FAILURES + " failures will be reported.");
		}
	}

	private Stream<FileNameTest> toFileRulePair(Path path) {
		return getRules().stream().map(rule -> new FileNameTest(rule, path));
	}

	private Collection<FileNameRule> getRules() {
		return getConfig().getRules().values();
	}

	private FileNameConvention getConfig() {
		return ApplicationConfig.getInstance().getConfig(FileNameConvention.class);
	}

	private Path getProjectPath() {
		Path projectPath = AbstractBasicTestAll.MODULE_LAYOUT.getModuleDir().toPath();
		if (TEST_COMPLETE_WORKSPACE) {
			return projectPath.getParent();
		}
		return projectPath;
	}

	/** Creates a suite with all the tests wrapped in the necessary setups. */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestFileNames.class);
	}

}
