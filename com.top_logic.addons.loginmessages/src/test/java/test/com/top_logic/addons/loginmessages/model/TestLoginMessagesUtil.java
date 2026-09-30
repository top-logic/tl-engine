/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.addons.loginmessages.model;

import java.util.Date;

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
import com.top_logic.basic.DateUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link LoginMessagesUtil}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLoginMessagesUtil extends AbstractKnowledgeBaseTest {

	private Person _p1;

	private Person _p2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		_p1 = TestPerson.createPerson("p1");
		_p2 = TestPerson.createPerson("p2");
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_p1);
		TestPerson.deletePersonAndUser(_p2);
		super.tearDown();
	}

	/**
	 * Tests {@link LoginMessagesUtil#isConfirmExpired(PersonalConfiguration, LoginMessage)}.
	 */
	public void testConfirmExpired() {
		LoginMessagesWrapperFactory factory = LoginMessagesWrapperFactory.getInstance();

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		LoginMessage loginMessage;
		try (Transaction tx = kb.beginTransaction()) {
			loginMessage = factory.createLoginMessage();
			loginMessage.setName("testMSG");
			loginMessage.setActive(true);
			loginMessage.setConfirmDuration(2);
			tx.commit();
		}

		withPerson(_p1, () -> {
			Date confirmDate = LoginMessagesUtil.getConfirmDate(pc(), loginMessage);
			assertNull("Never confirmed by p1", confirmDate);

			assertTrue(LoginMessagesUtil.isConfirmExpired(pc(), loginMessage));

			try (Transaction tx = kb.beginTransaction()) {
				LoginMessagesUtil.setConfirmDate(pc(), loginMessage, DateUtil.addDays(now(), -1));
			}
			assertFalse(LoginMessagesUtil.isConfirmExpired(pc(), loginMessage));
		});

		withPerson(_p2, () -> {
			Date confirmDate = LoginMessagesUtil.getConfirmDate(pc(), loginMessage);
			assertNull("Never confirmed by p2", confirmDate);

			assertTrue(LoginMessagesUtil.isConfirmExpired(pc(), loginMessage));
		});

		withPerson(_p1, () -> {
			try (Transaction tx = kb.beginTransaction()) {
				LoginMessagesUtil.setConfirmDate(pc(), loginMessage, DateUtil.addDays(now(), -2));
			}
			assertTrue(LoginMessagesUtil.isConfirmExpired(pc(), loginMessage));
		});
	}

	private static void withPerson(Person p, Runnable runnable) {
		TLContext context = TLContext.getContext();
		Person oldP = context.getPerson();
		String contextId = context.getContextId();
		context.setPerson(p);
		try {
			runnable.run();
		} finally {
			context.setPerson(oldP);
			if (oldP == null) {
				context.setContextId(contextId);
			}
		}
	}

	private static PersonalConfiguration pc() {
		return PersonalConfiguration.getPersonalConfiguration();
	}

	private static Date now() {
		return new Date();
	}

	/**
	 * A cumulative {@link Test} for all Tests in {@link TestLoginMessagesUtil}.
	 */
	public static Test suite() {
		TestFactory f = DefaultTestFactory.INSTANCE;
		f = ServiceTestSetup.createStarterFactory(ModelService.Module.INSTANCE, f);

		return PersonManagerSetup.createPersonManagerSetup(TestLoginMessagesUtil.class, f);
	}
}

