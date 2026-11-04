/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.layout.person;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.ContactPersonManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link ContactPersonManager#repairAccountsWithoutContact()} and for the contact passed
 * to the creation of an account ({@link ContactPersonManager#initUser(Person, TLObject)}).
 */
public class TestContactPersonManager extends BasicTestCase {

	/**
	 * Name of the storage attribute of the contact reference of an account.
	 */
	private static final String CONTACT_STORAGE_ATTRIBUTE = "contact";

	private static final String ACCOUNT_NEW_CONTACT = "repairNewContact";

	private static final String ACCOUNT_REUSED_CONTACT = "repairReusedContact";

	private static final String ACCOUNT_WITH_CONTACT = "repairWithContact";

	private static final String ACCOUNT_CHOSEN_CONTACT = "chosenContact";

	private static final String ACCOUNT_REMOVED_LOGIN = "removedLogin";

	private static final String ACCOUNT_DUPLICATE = "duplicateLogin";

	/**
	 * A contact without account passed to the creation of an account is assigned to it, and no
	 * further contact is created.
	 */
	public void testChosenContact() {
		ThreadContext.withoutSecurityChecks(() -> {
			PersonContact contact = createContact("Chosen");
			Person account = null;
			try {
				account = createAccount(ACCOUNT_CHOSEN_CONTACT, contact);

				assertSame(contact, account.getUser());
				assertSame(account, contact.getPerson());
				assertEquals(ACCOUNT_CHOSEN_CONTACT, contact.tValueByName(ContactFactory.LOGIN_NAME));
				assertEquals("No further contact for the login.", List.of(contact),
					contactsWithLogin(ACCOUNT_CHOSEN_CONTACT));
			} finally {
				deleteAccount(account);
				deleteContact(contact);
			}
		});
	}

	/**
	 * The contact passed to the creation of an account is assigned also when other contacts
	 * without account remember the same login name, e.g. after the login was removed from them.
	 */
	public void testChosenContactAmongContactsOfSameLogin() {
		ThreadContext.withoutSecurityChecks(() -> {
			PersonContact former = createContact("Former");
			PersonContact other = createContact("Other");
			PersonContact chosen = createContact("Chosen");
			Person account = null;
			try {
				try (Transaction tx = kb().beginTransaction()) {
					former.tUpdateByName(ContactFactory.LOGIN_NAME, ACCOUNT_REMOVED_LOGIN);
					other.tUpdateByName(ContactFactory.LOGIN_NAME, ACCOUNT_REMOVED_LOGIN);
					tx.commit();
				}

				account = createAccount(ACCOUNT_REMOVED_LOGIN, chosen);

				assertSame(chosen, account.getUser());
				assertNull(former.getPerson());
				assertNull(other.getPerson());
				assertEquals(3, contactsWithLogin(ACCOUNT_REMOVED_LOGIN).size());
			} finally {
				deleteAccount(account);
				deleteContact(former);
				deleteContact(other);
				deleteContact(chosen);
			}
		});
	}

	/**
	 * The creation of an account with a login name already in use is refused and leaves the
	 * contact passed to it unchanged.
	 */
	public void testDuplicateLoginLeavesContactUnchanged() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person existing = createAccount(ACCOUNT_DUPLICATE);
			PersonContact contact = createContact("Duplicate");
			try {
				try (Transaction tx = kb().beginTransaction()) {
					Person.create(kb(), ACCOUNT_DUPLICATE,
						TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice(), contact);
					fail("A login name in use must be refused.");
				} catch (TopLogicException ex) {
					// Expected.
				}

				assertNull(contact.getPerson());
				assertNull(contact.tValueByName(ContactFactory.LOGIN_NAME));
				assertNotSame(contact, existing.getUser());
			} finally {
				deleteAccount(existing);
				deleteContact(contact);
			}
		});
	}

	/**
	 * An account whose contact was removed gets a new contact with its login name.
	 */
	public void testNewContact() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person account = createAccount(ACCOUNT_NEW_CONTACT);
			Person other = createAccount(ACCOUNT_WITH_CONTACT);
			PersonContact otherContact = (PersonContact) other.getUser();
			assertNotNull(otherContact);
			try {
				PersonContact oldContact = (PersonContact) account.getUser();
				assertNotNull(oldContact);
				commitWithoutModelCheck(() -> {
					clearContact(account);
					oldContact.tDelete();
				});
				assertNull(account.getUser());

				int repaired = manager().repairAccountsWithoutContact();

				assertTrue(repaired >= 1);
				PersonContact newContact = (PersonContact) account.getUser();
				assertNotNull(newContact);
				assertTrue(newContact.tValid());
				assertEquals(ACCOUNT_NEW_CONTACT, newContact.tValueByName(ContactFactory.LOGIN_NAME));
				assertSame(account, newContact.getPerson());

				assertSame(otherContact, other.getUser());
			} finally {
				deleteAccount(account);
				deleteAccount(other);
			}
		});
	}

	/**
	 * An account whose contact was removed gets the unassigned contact with its login name back.
	 */
	public void testReusedContact() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person account = createAccount(ACCOUNT_REUSED_CONTACT);
			try {
				PersonContact oldContact = (PersonContact) account.getUser();
				assertNotNull(oldContact);
				commitWithoutModelCheck(() -> clearContact(account));
				assertNull(account.getUser());
				assertNull(oldContact.getPerson());

				int repaired = manager().repairAccountsWithoutContact();

				assertTrue(repaired >= 1);
				assertSame(oldContact, account.getUser());
			} finally {
				deleteAccount(account);
			}
		});
	}

	/**
	 * Without accounts lacking a contact, nothing is repaired.
	 */
	public void testNothingToRepair() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person account = createAccount(ACCOUNT_WITH_CONTACT);
			try {
				Object contact = account.getUser();
				assertNotNull(contact);

				assertEquals(0, manager().repairAccountsWithoutContact());
				assertSame(contact, account.getUser());
			} finally {
				deleteAccount(account);
			}
		});
	}

	private static Person createAccount(String name) {
		return createAccount(name, null);
	}

	private static Person createAccount(String name, TLObject initialData) {
		try (Transaction tx = kb().beginTransaction()) {
			Person result = Person.create(kb(), name,
				TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice(), initialData);
			tx.commit();
			return result;
		}
	}

	private static PersonContact createContact(String name) {
		try (Transaction tx = kb().beginTransaction()) {
			PersonContact result = ContactFactory.getInstance().createNewPersonContact(name, null);
			tx.commit();
			return result;
		}
	}

	private static List<?> contactsWithLogin(String login) {
		return ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.PERSON_TYPE,
			AbstractContact.FKEY_ATTRIBUTE, login, false);
	}

	private static void deleteContact(PersonContact contact) {
		if (!contact.tValid()) {
			return;
		}
		try (Transaction tx = kb().beginTransaction()) {
			contact.tDelete();
			tx.commit();
		}
	}

	/**
	 * Removes the contact of the given account through the storage layer, as the account would be
	 * stored when created before the contact module was added.
	 */
	private static void clearContact(Person account) {
		account.tHandle().setAttributeValue(CONTACT_STORAGE_ATTRIBUTE, null);
	}

	/**
	 * Commits the given changes without the model constraint check, to produce an account
	 * violating the mandatory contact reference.
	 */
	private static void commitWithoutModelCheck(Runnable changes) {
		ModelService modelService = ModelService.getInstance();
		kb().removeCommitChecker(modelService);
		try (Transaction tx = kb().beginTransaction()) {
			changes.run();
			tx.commit();
		} finally {
			kb().addCommitChecker(modelService);
		}
	}

	private static void deleteAccount(Person account) {
		if (account == null || !account.tValid()) {
			return;
		}
		Object contact = account.getUser();
		try (Transaction tx = kb().beginTransaction()) {
			account.tDelete();
			if (contact instanceof PersonContact personContact && personContact.tValid()) {
				personContact.tDelete();
			}
			tx.commit();
		}
	}

	private static ContactPersonManager manager() {
		return (ContactPersonManager) PersonManager.getManager();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests to perform.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestContactPersonManager.class),
				TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE));
	}

}
