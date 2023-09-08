/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.security.util.SignatureService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * {@link TestSetup} that creates a user for testing.
 * 
 * @see #getTestPerson()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPersonSetup extends ThreadContextSetup {

	/**
	 * The user ID of the person that is created by this setup.
	 */
	public static final String USER_ID = "test";

	/**
	 * The role of the person that is created by this setup.
	 */
	public static final String USER_ROLE = "user";

	/**
	 * The password of the person that is created by this setup.
	 */
	public static final String USER_PASSWORD = "test-password";

	private static Person _testPerson;

	/**
	 * Creates a {@link TestPersonSetup} for the given test.
	 */
	private TestPersonSetup(Test test) {
		super(test);
	}

	@Override
	protected void doSetUp() throws Exception {
		Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
		{
			_testPerson = PersonManager.getManager().createPerson(USER_ID, "dbSecurity", "dbSecurity", Boolean.FALSE);
			_testPerson.getUser().setAttributeValue(PersonAttributes.PASSWORD,
				SignatureService.getInstance().sign(USER_PASSWORD));
			_testPerson.getUser().setAttributeValue(PersonAttributes.USER_ROLE, USER_ROLE);
			tx.commit();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
		{
			PersonManager.getManager().deleteUser(_testPerson);
			tx.commit();
		}
	}

	/**
	 * The {@link Person} that is created by this setup.
	 * 
	 * @see #USER_ID
	 * @see #USER_ROLE
	 * @see #USER_PASSWORD
	 */
	public static Person getTestPerson() {
		return _testPerson;
	}

	/**
	 * Wraps the given test with a {@link TestPersonSetup}.
	 */
	public static Test wrap(Test test) {
		return new TestPersonSetup(test);
	}

	/**
	 * Wraps the test created by given {@link TestFactory} with a {@link TestPersonSetup}.
	 */
	public static TestFactory wrap(TestFactory f) {
		return new TestFactoryProxy(f) {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				return wrap(super.createSuite(testCase, suiteName));
			}
		};
	}

}
