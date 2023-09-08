/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa;

import junit.framework.Test;
import test.com.top_logic.basic.ModuleTestSetup;


/**
 * Does top-logic.dsa specific general setup (and tear down) for test cases.
 *
 * @author <a href="mailto:thorsten.wittmann@top-logic.com">Thorsten Wittmann</a>
 */
public class DSATestSetup {

    public static Test createDSATestSetup(Test test) {
		return ModuleTestSetup.setupModule(test);
	}

}
