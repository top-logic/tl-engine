/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.DefaultExternalUserMapping;
import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.base.accesscontrol.J2eeRemoteUserAuthenticationServlet;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test for {@link ExternalUserMapping#findAccountForIdentity(ExternalIdentity)} and the default
 * {@link ExternalUserMapping} of a login.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExternalUserMapping extends BasicTestCase {

	/** Name of the client that has authenticated the identities of this test. */
	private static final String CLIENT = "test-client";

	/** Subject of the identities of this test. */
	private static final String SUBJECT = "subject-4711";

	/** Name of a claim of the identities of this test. */
	private static final String EMAIL_CLAIM = "email";

	/** Value of {@link #EMAIL_CLAIM}. */
	private static final String EMAIL = "someone@example.com";

	/**
	 * Name of an account that does not exist in this application.
	 */
	private static final String UNKNOWN_ACCOUNT = "no-such-account-xyz";

	/**
	 * {@link ExternalUserMapping} that only resolves names and remembers the name it was asked
	 * for.
	 */
	private static class NameOnlyMapping implements ExternalUserMapping {

		String _requestedName;

		@Override
		public Person findAccountForExternalName(String externalUserName) throws LoginDeniedException {
			_requestedName = externalUserName;
			return ExternalUserMapping.findAccount(externalUserName);
		}

	}

	/**
	 * A mapping that only resolves names finds, for an identity, the same account as for its name.
	 */
	public void testDefaultFindsAccountByName() {
		Person root = PersonManager.getManager().getRoot();
		String name = root.getName();

		assertSame(DefaultExternalUserMapping.INSTANCE.findAccountForExternalName(name),
			DefaultExternalUserMapping.INSTANCE.findAccountForIdentity(identity(name)));
		assertSame(root, DefaultExternalUserMapping.INSTANCE.findAccountForIdentity(identity(name)));

		NameOnlyMapping mapping = new NameOnlyMapping();
		assertSame(root, mapping.findAccountForIdentity(identity(name)));
		assertEquals("The name of the identity is looked up, not its subject.", name, mapping._requestedName);
	}

	/**
	 * An identity whose name has no account is reported as unknown under its name, so that the page
	 * for unknown accounts shows that name.
	 */
	public void testDefaultUnknownAccount() {
		assertNull("Test requires that no such account exists.", Person.byName(UNKNOWN_ACCOUNT));

		try {
			DefaultExternalUserMapping.INSTANCE.findAccountForIdentity(identity(UNKNOWN_ACCOUNT));
			fail("An account that does not exist must not be found.");
		} catch (UnknownAccountException ex) {
			assertEquals(UNKNOWN_ACCOUNT, ex.getLoginName());
		}
	}

	/**
	 * Without configuration, the header login resolves accounts by name through the singleton
	 * {@link DefaultExternalUserMapping#INSTANCE}.
	 */
	public void testDefaultMappingOfHeaderLogin() {
		J2eeRemoteUserAuthenticationServlet.Config config =
			TypedConfiguration.newConfigItem(J2eeRemoteUserAuthenticationServlet.Config.class);

		assertSame(DefaultExternalUserMapping.INSTANCE, TypedConfigUtil.createInstance(config.getUserMapping()));
	}

	private static ExternalIdentity identity(String name) {
		return new ExternalIdentity(CLIENT, SUBJECT, name, Map.of(EMAIL_CLAIM, EMAIL));
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestExternalUserMapping.class));
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
