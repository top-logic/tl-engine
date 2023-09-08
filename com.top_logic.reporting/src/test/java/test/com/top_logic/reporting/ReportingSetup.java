/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

/**
 * The unit test setup for <code>com.top_logic.reporting</code>.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ReportingSetup {

	public static Test createReportingSetup(Test test) {
		return ModuleLicenceTestSetup.setupModule(test);
	}

	public static Test createReportingSetup(Class<? extends BasicTestCase> testClass) {
		return createReportingSetup(new TestSuite(testClass));
	}
	
}

