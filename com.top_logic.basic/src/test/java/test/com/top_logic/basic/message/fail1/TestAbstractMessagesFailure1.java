/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.message.fail1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.message.AbstractMessages;
import com.top_logic.basic.message.Template;

/**
 * Test case for {@link AbstractMessages} with invalid messages definition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAbstractMessagesFailure1 extends TestCase {

	public void testInit() {
		try {
			Template t0 = Messages.MSG_0;
			fail("Class initialization must have failed.");
			
			assertNotNull(t0);
		} catch (NoClassDefFoundError ex) {
			// Expected, if this test runs after testAccess().
		} catch (AssertionError ex) {
			// Expected, if this test runs before testAccess().
		}
	}

	public void testAccess() {
		try {
			Template t0 = Messages.MSG_0;
			fail("Class initialization must have failed.");
			
			assertNotNull(t0);
		} catch (NoClassDefFoundError ex) {
			// Expected, see testInit().
		} catch (AssertionError ex) {
			// Expected, see testInit().
		}
	}

	public static Test suite() {
		return new TestSuite(TestAbstractMessagesFailure1.class);
	}
	
}
