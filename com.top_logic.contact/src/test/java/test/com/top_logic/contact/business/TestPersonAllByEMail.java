/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.contact.business.Account;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test of {@link Person#allByEMail(String)} with accounts whose e-mail address is stored in their
 * contact ({@link Account#getUser()}).
 */
public class TestPersonAllByEMail extends BasicTestCase {

	private final List<Person> _accounts = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction()) {
				for (Person account : _accounts) {
					if (!account.tValid()) {
						continue;
					}
					Object contact = account.getUser();
					account.tDelete();
					if (contact instanceof PersonContact personContact && personContact.tValid()) {
						personContact.tDelete();
					}
				}
				tx.commit();
			}
		});
		_accounts.clear();
		super.tearDown();
	}

	/**
	 * An address is found ignoring case and surrounding whitespace, both of the searched address and
	 * of the stored one.
	 */
	public void testIgnoresCaseAndWhitespace() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person account = createAccount("allByEMailJdoe", "  John.Doe@example.com ");

			assertEquals(List.of(account), Person.allByEMail("john.doe@EXAMPLE.com"));
			assertEquals(List.of(account), Person.allByEMail(" JOHN.DOE@example.com  "));
		});
	}

	/**
	 * All accounts with the address are found.
	 */
	public void testSeveralHits() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person first = createAccount("allByEMailFirst", "shared@example.com");
			Person second = createAccount("allByEMailSecond", "Shared@Example.com");
			createAccount("allByEMailOther", "other@example.com");

			assertEquals(Set.of(first, second), Set.copyOf(Person.allByEMail("shared@example.com")));
		});
	}

	/**
	 * An address no account carries finds nothing, also not an account having the address as name.
	 */
	public void testNoHit() {
		ThreadContext.withoutSecurityChecks(() -> {
			createAccount("allByEMailSomeone", "someone@example.com");
			createAccount("nobody@example.com", null);

			assertEquals(List.of(), Person.allByEMail("nobody@example.com"));
		});
	}

	private Person createAccount(String name, String eMail) {
		try (Transaction tx = kb().beginTransaction()) {
			Person result = Person.create(kb(), name,
				TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice());
			if (eMail != null) {
				result.getUser().setEMail(eMail);
			}
			tx.commit();
			_accounts.add(result);
			return result;
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests to perform.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestPersonAllByEMail.class),
				TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE));
	}

}
