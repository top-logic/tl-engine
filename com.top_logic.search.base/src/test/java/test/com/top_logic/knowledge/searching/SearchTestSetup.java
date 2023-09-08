/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.searching;

import junit.framework.Test;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.knowledge.KBSetup;

/**
 * Setup for search test cases
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class SearchTestSetup extends TLTestSetup {

	public static Test createSearchTestSetup(Test test) {
		return ModuleLicenceTestSetup.setupModule(KBSetup.getSingleKBTest(test));
	}

}
