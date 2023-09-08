/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.framework.Test;


/**
 * Does top-logic specific general setup (and tear down) for test cases.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TLTestSetup {

	public static Test createTLTestSetup(Test wrapped) {
		return ModuleLicenceTestSetup.setupModule(wrapped);
	}

	public static Test createTLTestSetup(Class testClass) {
		return ModuleLicenceTestSetup.setupModule(testClass);
	}
	

}
