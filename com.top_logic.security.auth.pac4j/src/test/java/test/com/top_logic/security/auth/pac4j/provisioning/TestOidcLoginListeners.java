/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.provisioning;

import static test.com.top_logic.security.auth.pac4j.provisioning.TestOidcProvisioningUserMapping.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalLoginListener;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.auth.pac4j.provisioning.OidcIdentities;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Tests the calls of the {@link ExternalLoginListener}s of {@link OidcProvisioningUserMapping}.
 */
public class TestOidcLoginListeners extends BasicTestCase {

	private static final String CLIENT = "listener-idp";

	private static final String GROUPS_CLAIM = "groups";

	/**
	 * {@link ExternalLoginListener} recording its calls in {@link #CALLS}.
	 */
	public static class RecordingListener implements ExternalLoginListener {

		/**
		 * The calls, as <code>create:&lt;account&gt;</code> and <code>login:&lt;account&gt;</code>.
		 */
		static final List<String> CALLS = Collections.synchronizedList(new ArrayList<>());

		@Override
		public void onCreate(Person account, ExternalIdentity identity) {
			CALLS.add("create:" + account.getName());
		}

		@Override
		public void onLogin(Person account, ExternalIdentity identity) {
			assertTrue("Called after the group synchronization.",
				identity.getClaim(GROUPS_CLAIM) == null || !Group.getGroups(account).isEmpty());
			CALLS.add("login:" + account.getName());
		}

	}

	/**
	 * {@link ExternalLoginListener} refusing every login with {@link #REASON}.
	 */
	public static class RefusingListener implements ExternalLoginListener {

		/**
		 * The reason of the refusal.
		 */
		static final ResKey REASON = ResKey.text("Refused by a listener in this test.");

		@Override
		public void onLogin(Person account, ExternalIdentity identity) {
			throw new LoginDeniedException(REASON, "Refused by a listener in this test.");
		}

	}

	/**
	 * {@link ExternalLoginListener} failing at the creation of an account with an internal error.
	 */
	public static class FailingCreateListener implements ExternalLoginListener {

		@Override
		public void onCreate(Person account, ExternalIdentity identity) {
			throw new IllegalStateException("Failure of a listener in this test.");
		}

	}

	private final List<String> _accountNames = new ArrayList<>();

	private Group _developers;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RecordingListener.CALLS.clear();
		try (Transaction tx = kb().beginTransaction()) {
			_developers = Group.createGroup("ListenerDevelopers");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		for (String name : _accountNames) {
			Person account = Person.byName(name);
			if (account != null) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accountNames.clear();
		try (Transaction tx = kb().beginTransaction()) {
			_developers.tDelete();
			tx.commit();
		}
		RecordingListener.CALLS.clear();
		super.tearDown();
	}

	/**
	 * {@link ExternalLoginListener#onCreate(Person, ExternalIdentity)} is called once, when the
	 * account is created, {@link ExternalLoginListener#onLogin(Person, ExternalIdentity)} at every
	 * login.
	 */
	public void testCalls() throws Exception {
		OidcProvisioningUserMapping mapping = listenerMapping(RecordingListener.class);

		Person account = login(mapping, identity("sub-calls", "lCalls"));
		assertEquals(List.of("create:lCalls", "login:lCalls"), RecordingListener.CALLS);

		assertSame(account, login(mapping, identity("sub-calls", "lCalls")));
		assertSame(account, login(mapping, identity("sub-calls", "lCalls")));
		assertEquals(List.of("create:lCalls", "login:lCalls", "login:lCalls", "login:lCalls"),
			RecordingListener.CALLS);
	}

	/**
	 * An account linked by name is not created: only
	 * {@link ExternalLoginListener#onLogin(Person, ExternalIdentity)} is called.
	 */
	public void testNoCreateForLinkedAccount() throws Exception {
		Person existing = account("lLinked");
		OidcProvisioningUserMapping mapping = listenerMapping(RecordingListener.class);

		assertSame(existing, login(mapping, identity("sub-linked", "lLinked")));

		assertEquals(List.of("login:lLinked"), RecordingListener.CALLS);
	}

	/**
	 * A listener refusing the login of a new account rolls back its creation, and the reason
	 * reaches the user.
	 */
	public void testRefusalRollsBackCreation() throws Exception {
		OidcProvisioningUserMapping mapping = listenerMapping(RecordingListener.class, RefusingListener.class);

		try {
			login(mapping, identity("sub-refused", "lRefused"));
			fail("The listener must refuse the login.");
		} catch (LoginDeniedException ex) {
			assertEquals(RefusingListener.REASON, ex.getReason());
		}

		assertNull("Creation not rolled back.", Person.byName("lRefused"));
		assertNull(OidcIdentities.findIdentity(kb(), CLIENT, "sub-refused"));
		assertEquals(List.of("create:lRefused", "login:lRefused"), RecordingListener.CALLS);
	}

	/**
	 * A listener refusing the login of an existing account rolls back the link of the identity and
	 * the group memberships.
	 */
	public void testRefusalRollsBackLinkAndGroups() throws Exception {
		Person existing = account("lExisting");
		OidcProvisioningUserMapping mapping = listenerMapping(RefusingListener.class);

		try {
			login(mapping, identity("sub-existing", "lExisting"));
			fail("The listener must refuse the login.");
		} catch (LoginDeniedException ex) {
			assertEquals(RefusingListener.REASON, ex.getReason());
		}

		assertTrue("Link not rolled back.", OidcIdentities.getIdentities(existing).isEmpty());
		assertFalse("Group membership not rolled back.", _developers.getDirectMembers().contains(existing));
	}

	/**
	 * A listener failing with an internal error refuses the login and rolls the creation back.
	 */
	public void testFailureRollsBackCreation() throws Exception {
		OidcProvisioningUserMapping mapping = listenerMapping(FailingCreateListener.class);

		try {
			login(mapping, identity("sub-failing", "lFailing"));
			fail("The failing listener must refuse the login.");
		} catch (IllegalStateException ex) {
			// expected
		}

		assertNull("Creation not rolled back.", Person.byName("lFailing"));
	}

	@SafeVarargs
	private static OidcProvisioningUserMapping listenerMapping(Class<? extends ExternalLoginListener>... listeners)
			throws ConfigurationException {
		List<PolymorphicConfiguration<? extends ExternalLoginListener>> configs = new ArrayList<>();
		for (Class<? extends ExternalLoginListener> listener : listeners) {
			configs.add(TypedConfiguration.createConfigItemForImplementationClass(listener));
		}
		return mapping(Config.CREATE_ACCOUNTS, true, Config.GROUP_CLAIM, GROUPS_CLAIM,
			Config.GROUP_MAPPING, Map.of("devs", Set.of("ListenerDevelopers")), Config.LISTENERS, configs);
	}

	private Person login(OidcProvisioningUserMapping mapping, ExternalIdentity identity) {
		Person account = mapping.findAccountForIdentity(identity);
		_accountNames.add(account.getName());
		return account;
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accountNames.add(name);
		return account;
	}

	private static ExternalIdentity identity(String subject, String userName) {
		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put(OidcProvisioningUserMapping.PREFERRED_USERNAME_CLAIM, userName);
		claims.put(GROUPS_CLAIM, List.of("devs"));
		return new ExternalIdentity(CLIENT, subject, userName, claims);
	}

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite, with a knowledge base holding the model of the external identities.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestOidcLoginListeners.class),
				ModelService.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}

}
