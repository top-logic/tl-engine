/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.ModuleLicenceTestSetup;

/**
 * Sets up the element module for the given test.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementTestSetup {
	
	public static Test createElementTestSetup(Test innerTest) {
		return ModuleLicenceTestSetup.setupModule(innerTest);
	}

	public static Test createElementTestSetup(Class<? extends Test> testClass) {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(testClass));
	}
	
}

