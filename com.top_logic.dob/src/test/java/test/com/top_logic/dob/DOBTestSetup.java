/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob;

import junit.framework.Test;
import test.com.top_logic.basic.ModuleTestSetup;

/**
 * Performs general test setup for test cases in module tl-dob.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DOBTestSetup {

	public static Test createDOBTestSetup(Test suite) {
		return ModuleTestSetup.setupModule(suite);
	}

}
