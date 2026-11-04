/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.addons.loginmessages.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.AbstractKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.addons.loginmessages.model.LoginMessagesUtil;
import com.top_logic.addons.loginmessages.model.LoginMessagesWrapperFactory;
import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.addons.loginmessages.script.LoginMessagesFunctions;
import com.top_logic.basic.DateUtil;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link LoginMessagesFunctions}, the TL-Script functions showing the login messages to an
 * account and recording their confirmation.
 */
public class TestLoginMessagesFunctions extends AbstractKnowledgeBaseTest {

	private Person _p1;

	private Person _p2;

	private final List<LoginMessage> _messages = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_p1 = TestPerson.createPerson("p1");
		_p2 = TestPerson.createPerson("p2");
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction()) {
			for (LoginMessage message : _messages) {
				message.tDelete();
			}
			tx.commit();
		}
		TestPerson.deletePersonAndUser(_p1);
		TestPerson.deletePersonAndUser(_p2);
		super.tearDown();
	}

	/**
	 * Tests that only active messages in their time interval are due, sorted by name in descending
	 * order.
	 */
	public void testDue() {
		LoginMessage a = message("a", true, null, null);
		LoginMessage b = message("b", true, DateUtil.addDays(now(), -1), DateUtil.addDays(now(), 1));
		message("inactive", false, null, null);
		message("ended", true, null, DateUtil.addDays(now(), -1));
		message("future", true, DateUtil.addDays(now(), 1), null);

		assertEquals(List.of(b, a), LoginMessagesFunctions.due(_p1));
	}

	/**
	 * Tests that confirmed messages are no longer due for the confirming account, but still for
	 * others.
	 */
	public void testConfirm() {
		LoginMessage a = message("a", true, null, null);
		LoginMessage b = message("b", true, null, null);

		LoginMessagesFunctions.confirm(_p1, List.of(a));
		assertEquals(List.of(b), LoginMessagesFunctions.due(_p1));
		assertEquals(List.of(b, a), LoginMessagesFunctions.due(_p2));

		LoginMessagesFunctions.confirm(_p1, LoginMessagesFunctions.due(_p1));
		assertEquals(List.of(), LoginMessagesFunctions.due(_p1));
	}

	/**
	 * Tests that a confirmation expires after the confirmation duration of its message.
	 */
	public void testConfirmExpires() {
		LoginMessage a = message("a", true, null, null);
		try (Transaction tx = kb().beginTransaction()) {
			a.setConfirmDuration(2);
			tx.commit();
		}
		LoginMessagesFunctions.confirm(_p1, List.of(a));
		assertEquals(List.of(), LoginMessagesFunctions.due(_p1));

		TLContextManager.inPersonContext(_p1, () -> {
			LoginMessagesUtil.setConfirmDate(PersonalConfiguration.getPersonalConfiguration(), a,
				DateUtil.addDays(now(), -2));
			PersonalConfiguration.storePersonalConfiguration();
		});
		assertEquals(List.of(a), LoginMessagesFunctions.due(_p1));
	}

	/**
	 * Tests that a confirmation made before the session of the account starts - in a login step -
	 * reaches the personal configuration of the session and survives when the session writes its
	 * personal configuration back.
	 */
	public void testConfirmSurvivesSession() {
		LoginMessage a = message("a", true, null, null);

		LoginMessagesFunctions.confirm(_p1, List.of(a));

		TLContextManager.inPersonContext(_p1, () -> {
			PersonalConfiguration session = PersonalConfiguration.getPersonalConfiguration();
			assertFalse("The session sees the confirmation.", LoginMessagesUtil.isConfirmExpired(session, a));

			// The session changes something else and writes its configuration back, as at logout.
			session.setValue("testSetting", "value");
			PersonalConfiguration.storePersonalConfiguration();
		});

		assertEquals(List.of(), LoginMessagesFunctions.due(_p1));
	}

	/**
	 * Tests that confirming nothing changes nothing.
	 */
	public void testConfirmNothing() {
		LoginMessage a = message("a", true, null, null);

		LoginMessagesFunctions.confirm(_p1, List.of());
		LoginMessagesFunctions.confirm(_p1, null);

		assertEquals(List.of(a), LoginMessagesFunctions.due(_p1));
	}

	private LoginMessage message(String name, boolean active, Date start, Date end) {
		LoginMessage message;
		try (Transaction tx = kb().beginTransaction()) {
			message = LoginMessagesWrapperFactory.getInstance().createLoginMessage();
			message.setName(name);
			message.setActive(active);
			message.setStartDate(start);
			message.setEndDate(end);
			tx.commit();
		}
		_messages.add(message);
		return message;
	}

	private static Date now() {
		return new Date();
	}

	/**
	 * The suite of this test, with the model and the accounts.
	 */
	public static Test suite() {
		TestFactory f = DefaultTestFactory.INSTANCE;
		f = ServiceTestSetup.createStarterFactory(ModelService.Module.INSTANCE, f);

		return PersonManagerSetup.createPersonManagerSetup(TestLoginMessagesFunctions.class, f);
	}
}
