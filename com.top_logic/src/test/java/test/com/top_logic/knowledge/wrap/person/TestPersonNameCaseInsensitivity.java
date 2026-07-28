/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Test that account names are resolved case-insensitively while their original spelling is
 * preserved (Ticket #29423).
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPersonNameCaseInsensitivity extends BasicTestCase {

	/**
	 * An account created as "Admin" keeps that spelling but is found by any case variant.
	 */
	public void testLookupIsCaseInsensitivePreservingSpelling() {
		Person admin = TestPerson.createPerson("Admin");
		try {
			assertEquals("Original spelling must be preserved.", "Admin", admin.getName());
			assertSame("Lookup by lower case must find the account.", admin, Person.byName("admin"));
			assertSame("Lookup by upper case must find the account.", admin, Person.byName("ADMIN"));
			assertSame("Lookup by mixed case must find the account.", admin, Person.byName("Admin"));
			assertNull("A non-existing name must not resolve.", Person.byName("does-not-exist"));
		} finally {
			TestPerson.deletePersonAndUser(admin);
		}
	}

	/**
	 * Returns the suite of tests to perform.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			TestPersonSetup.wrap(new TestSuite(TestPersonNameCaseInsensitivity.class)));
	}

}
