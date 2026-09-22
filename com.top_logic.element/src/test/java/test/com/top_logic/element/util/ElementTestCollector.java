/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.util;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.util.TestCollector;
import test.com.top_logic.element.boundsec.manager.coverage.TestSecurityCoverage;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link TestCollector} for tests depending on the "com.top_logic.element" module.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementTestCollector implements TestCollector {

	/**
	 * Configuration options for {@link ElementTestCollector}.
	 *
	 * <p>
	 * An application switches a test on in its {@code *.test.config.xml}:
	 * </p>
	 *
	 * <pre>
	 * &lt;configs&gt;
	 *   &lt;config config:interface="test.com.top_logic.element.util.ElementTestCollector$GlobalConfig"
	 *     test-security-coverage="true"
	 *   /&gt;
	 * &lt;/configs&gt;
	 * </pre>
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/** Configuration name for the value of {@link #getTestSecurityCoverage()}. */
		String TEST_SECURITY_COVERAGE = "test-security-coverage";

		/**
		 * Whether to activate {@link TestSecurityCoverage}.
		 *
		 * <p>
		 * The test checks the access definition of the application under test, which is complete
		 * only in an application that maintains it. It is therefore switched off unless an
		 * application activates it.
		 * </p>
		 */
		@Name(TEST_SECURITY_COVERAGE)
		@BooleanDefault(false)
		boolean getTestSecurityCoverage();

	}

	@Override
	public void addModuleIndependentTests(TestSuite suite) {
		GlobalConfig config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
		if (config.getTestSecurityCoverage()) {
			suite.addTest(TestSecurityCoverage.suite());
		}
	}

	@Override
	public void addTestForDirectory(TestSuite suite, File testDir, boolean recursive) {
		// No tests are derived from a directory.
	}

	@Override
	public Test getTestsForFile(File targetFile) {
		return null;
	}

}
