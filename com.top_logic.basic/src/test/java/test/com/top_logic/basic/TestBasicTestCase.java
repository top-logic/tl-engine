/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests {@link BasicTestCase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBasicTestCase extends BasicTestCase {

	public void testNotEquals() {
		
		assertNotEquals("Not equals", "a", "b");
		try {
			assertNotEquals("Equals", "a", "a");
			fail("Expected assertion error.");
		} catch (AssertionFailedError ex) {
			// Expected.
		}
		
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestBasicTestCase.class));
	}
	
}
