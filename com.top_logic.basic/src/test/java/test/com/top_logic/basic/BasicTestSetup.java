/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;

/**
 * Does general setup (and tear down) for test cases.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BasicTestSetup {

	public static Test createBasicTestSetup(Test wrapped) {
		return ModuleTestSetup.setupModule(wrapped);
	}

	public static Test createBasicTestSetup(Class<? extends Test> wrapped) {
		return ModuleTestSetup.setupModule(wrapped);
	}

}
