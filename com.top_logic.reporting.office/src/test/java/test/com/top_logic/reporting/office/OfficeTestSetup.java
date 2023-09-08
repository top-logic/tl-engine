/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

/**
 * TLTestSetup that is configurable via the system environment.
 * Use the JVM parameter -Dbase.web.path=&quot;&lt;PATH&gt;&quot;
 * to specify the base web path. The top-level multi-loader.properties
 * will be used to set up the paths.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class OfficeTestSetup {

	public static Test createOfficeTestSetup(Test wrapped) {
		return ModuleLicenceTestSetup.setupModule(wrapped);
	}
	
	public static Test createOfficeTestSetup(Class<? extends BasicTestCase> testClass) {
		return createOfficeTestSetup(new TestSuite(testClass));
	}

}
