/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.wrap;

import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.I18NConstants;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for the deletion veto of the {@link PersonManager#isAnonymous(Person) anonymous account}
 * (see {@link Person#tDeleteVeto()}), enforced at commit for accounts of the type
 * {@link Person#PERSON_TYPE}.
 */
public class TestAnonymousAccountDeletion extends BasicTestCase {

	/**
	 * Deleting the anonymous account is refused at commit with
	 * {@link I18NConstants#ERROR_ANONYMOUS_ACCOUNT_CANNOT_BE_DELETED}.
	 */
	public void testDeleteAnonymousAccountRefused() {
		Person anonymous = PersonManager.getManager().getAnonymous();
		assertNotNull(anonymous);
		assertEquals(Optional.of(I18NConstants.ERROR_ANONYMOUS_ACCOUNT_CANNOT_BE_DELETED), anonymous.tDeleteVeto());

		ThreadContext.pushSuperUser();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			anonymous.tDelete();
			tx.commit();
			fail("Deleting the anonymous account must be refused.");
		} catch (RuntimeException ex) {
			assertVeto(ex);
		} finally {
			ThreadContext.popSuperUser();
		}

		assertTrue(anonymous.tValid());
		assertEquals(anonymous, PersonManager.getManager().getAnonymous());
	}

	/**
	 * Deleting an ordinary account succeeds.
	 */
	public void testDeleteOrdinaryAccount() {
		String name = "anonymousDeletionOrdinary";
		Person person;
		ThreadContext.pushSuperUser();
		try {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				person = Person.create(kb(), name,
					TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
				tx.commit();
			}
			assertEquals(Optional.empty(), person.tDeleteVeto());

			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				person.tDelete();
				tx.commit();
			}
		} finally {
			ThreadContext.popSuperUser();
		}

		assertFalse(person.tValid());
		assertNull(Person.byName(name));
	}

	private static void assertVeto(Throwable ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof TopLogicException tlEx
				&& I18NConstants.ERROR_ANONYMOUS_ACCOUNT_CANNOT_BE_DELETED.equals(tlEx.getErrorKey())) {
				return;
			}
		}
		throw new AssertionError("Deletion failed without the anonymous account veto.", ex);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests to perform.
	 */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAnonymousAccountDeletion.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
