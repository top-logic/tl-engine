/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.provisioning;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.pac4j.oidc.profile.OidcProfile;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.IdentityVerifications;
import com.top_logic.base.accesscontrol.IdentityVerifications.Outcome;
import com.top_logic.base.accesscontrol.VerificationResult;
import com.top_logic.base.accesscontrol.VerifiedIdentity;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;
import com.top_logic.security.auth.pac4j.model.OidcIdentity;
import com.top_logic.security.auth.pac4j.provisioning.I18NConstants;
import com.top_logic.security.auth.pac4j.provisioning.OidcIdentities;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;
import com.top_logic.security.auth.pac4j.servlet.Pac4jAuthenticationServlet;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests connecting a further external login with an account through an identity verification
 * ({@link OidcIdentities#connectHandler(Runnable)}), and removing it again
 * ({@link OidcIdentities#removeIdentity(Person, OidcIdentity)}).
 */
public class TestOidcIdentityConnection extends BasicTestCase {

	/** The configured pac4j client of the connected login. */
	private static final String CLIENT = "test-idp";

	/** Another configured pac4j client. */
	private static final String OTHER_CLIENT = "other-idp";

	private final List<String> _accountNames = new ArrayList<>();

	private final List<Group> _groups = new ArrayList<>();

	private IdentityVerifications _verifications;

	private AtomicInteger _connected;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_verifications = new IdentityVerifications(IdentityVerifications.DEFAULT_VALIDITY, Clock.systemUTC());
		_connected = new AtomicInteger();
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
		if (!_groups.isEmpty()) {
			try (Transaction tx = kb().beginTransaction()) {
				for (Group group : _groups) {
					group.tDelete();
				}
				tx.commit();
			}
			_groups.clear();
		}
		super.tearDown();
	}

	/**
	 * The identity the provider confirms is attached to the account the verification was
	 * registered for, under the configured client, even though the profile comes from the
	 * re-authentication client derived from it; the account mapping is not asked.
	 */
	public void testConnectAttachesToRegisteredAccount() {
		Person account = account("cAlice");

		VerificationResult result = connect(account, profile(reauthentication(CLIENT), "sub-alice"), failingResolver());

		assertEquals(Outcome.VERIFIED, result.getOutcome());
		assertEquals(I18NConstants.IDENTITY_CONNECTED, result.getMessage());
		assertEquals(1, _connected.get());
		OidcIdentity identity = OidcIdentities.findIdentity(kb(), CLIENT, "sub-alice");
		assertNotNull("The identity must be stored under the configured client.", identity);
		assertSame(account, OidcIdentities.getOwner(identity));
		assertNull(OidcIdentities.findIdentity(kb(), reauthentication(CLIENT), "sub-alice"));
		assertTrue("Connecting grants no groups.", identity.getGrantedGroups().isEmpty());
	}

	/**
	 * Even with a mapping that creates accounts, connecting looks up and creates no account: the
	 * answer of the provider is no login.
	 */
	public void testConnectCreatesNoAccount() {
		Person account = account("cBob");
		OidcProvisioningUserMapping mapping = TestOidcProvisioningUserMapping.mapping(Config.CREATE_ACCOUNTS, true);
		OidcProfile profile = profile(reauthentication(CLIENT), "sub-bob");
		profile.addAttribute(OidcProvisioningUserMapping.PREFERRED_USERNAME_CLAIM, "cBobAtIdp");
		int accountsBefore = Person.all().size();

		AtomicInteger lookups = new AtomicInteger();
		VerificationResult result = connect(account, profile, () -> {
			lookups.incrementAndGet();
			return mapping.findAccountForIdentity(Pac4jAuthenticationServlet.createIdentity(profile, "cBobAtIdp"));
		});

		assertEquals(Outcome.VERIFIED, result.getOutcome());
		assertEquals("The account mapping must not be asked.", 0, lookups.get());
		assertNull("No account must be created.", Person.byName("cBobAtIdp"));
		assertEquals(accountsBefore, Person.all().size());
		assertSame(account, OidcIdentities.getOwner(OidcIdentities.findIdentity(kb(), CLIENT, "sub-bob")));
	}

	/**
	 * An identity that is part of another account is refused with a reason; both accounts stay as
	 * they are.
	 */
	public void testIdentityOfOtherAccountRefused() {
		Person owner = account("cOwner");
		Person intruder = account("cIntruder");
		addIdentity(owner, CLIENT, "sub-owned");

		VerificationResult result = connect(intruder, profile(reauthentication(CLIENT), "sub-owned"), failingResolver());

		assertEquals(Outcome.REFUSED, result.getOutcome());
		assertEquals(I18NConstants.ERROR_IDENTITY_OF_OTHER_ACCOUNT, result.getMessage());
		assertEquals(0, _connected.get());
		assertTrue(OidcIdentities.getIdentities(intruder).isEmpty());
		assertEquals(1, OidcIdentities.getIdentities(owner).size());
		assertSame(owner, OidcIdentities.getOwner(OidcIdentities.findIdentity(kb(), CLIENT, "sub-owned")));
	}

	/**
	 * Connecting an identity the account already has is no error and changes nothing.
	 */
	public void testAlreadyConnected() {
		Person account = account("cAgain");
		OidcIdentity identity = addIdentity(account, CLIENT, "sub-again");

		VerificationResult result = connect(account, profile(reauthentication(CLIENT), "sub-again"), failingResolver());

		assertEquals(Outcome.VERIFIED, result.getOutcome());
		assertEquals(List.of(identity), OidcIdentities.getIdentities(account));
	}

	/**
	 * A second identity of a client the account already has an identity of is refused.
	 */
	public void testOtherSubjectOfSameClientRefused() {
		Person account = account("cSecond");
		addIdentity(account, CLIENT, "sub-first");

		VerificationResult result = connect(account, profile(reauthentication(CLIENT), "sub-second"), failingResolver());

		assertEquals(Outcome.REFUSED, result.getOutcome());
		assertEquals(I18NConstants.ERROR_OTHER_IDENTITY__NAME.fill("cSecond").toString(),
			result.getMessage().toString());
		assertEquals(1, OidcIdentities.getIdentities(account).size());
		assertNull(OidcIdentities.findIdentity(kb(), CLIENT, "sub-second"));
	}

	/**
	 * Identities of different clients are connected side by side.
	 */
	public void testConnectFurtherClient() {
		Person account = account("cTwo");
		addIdentity(account, CLIENT, "sub-two");

		VerificationResult result = connect(account, profile(reauthentication(OTHER_CLIENT), "sub-two-other"),
			failingResolver());

		assertEquals(Outcome.VERIFIED, result.getOutcome());
		assertEquals(2, OidcIdentities.getIdentities(account).size());
	}

	/**
	 * An authentication that names no client and subject connects nothing.
	 */
	public void testNoClientIdentityRefused() {
		Person account = account("cNoClient");
		String token = _verifications.register(account, OidcIdentities.connectHandler(_connected::incrementAndGet));

		VerificationResult result = _verifications.complete(token, VerifiedIdentity.ofAccount(account));

		assertEquals(Outcome.REFUSED, result.getOutcome());
		assertEquals(I18NConstants.ERROR_NO_CLIENT_IDENTITY, result.getMessage());
		assertTrue(OidcIdentities.getIdentities(account).isEmpty());
	}

	/**
	 * A token connects once, and only in the registry (the session) it was issued by.
	 */
	public void testTokenBoundToSessionAndSingleUse() {
		Person account = account("cToken");
		String token = _verifications.register(account, OidcIdentities.connectHandler(_connected::incrementAndGet));

		IdentityVerifications otherSession =
			new IdentityVerifications(IdentityVerifications.DEFAULT_VALIDITY, Clock.systemUTC());
		assertEquals("A token of another session addresses nothing.", Outcome.UNKNOWN,
			otherSession.complete(token, verified(profile(CLIENT, "sub-token"))).getOutcome());
		assertNull(OidcIdentities.findIdentity(kb(), CLIENT, "sub-token"));

		assertEquals(Outcome.VERIFIED, _verifications.complete(token, verified(profile(CLIENT, "sub-token"))).getOutcome());
		assertEquals(Outcome.UNKNOWN,
			_verifications.complete(token, verified(profile(OTHER_CLIENT, "sub-token-2"))).getOutcome());
		assertNull(OidcIdentities.findIdentity(kb(), OTHER_CLIENT, "sub-token-2"));
		assertEquals(1, _connected.get());
	}

	/**
	 * An identity besides another one can be removed.
	 */
	public void testRemoveWithOtherIdentity() {
		Person account = account("rTwo");
		OidcIdentity first = addIdentity(account, CLIENT, "sub-r1");
		addIdentity(account, OTHER_CLIENT, "sub-r2");
		assertFalse("Test requires an account without password.", OidcIdentities.hasPasswordLogin(account));

		remove(account, first);

		assertEquals(1, OidcIdentities.getIdentities(account).size());
		assertNull(OidcIdentities.findIdentity(kb(), CLIENT, "sub-r1"));
	}

	/**
	 * The last identity of an account with a password stored by its device can be removed.
	 */
	public void testRemoveLastWithPassword() {
		Person account = account("rPassword");
		OidcIdentity identity = addIdentity(account, CLIENT, "sub-pwd");
		AuthenticationDevice device = account.getAuthenticationDevice();
		assertNotNull("Test requires an account with a device.", device);
		try (Transaction tx = kb().beginTransaction()) {
			device.setPassword(account, "secret-1234".toCharArray());
			tx.commit();
		}
		assertTrue(OidcIdentities.hasPasswordLogin(account));

		remove(account, identity);

		assertTrue(OidcIdentities.getIdentities(account).isEmpty());
	}

	/**
	 * The last identity of an account whose device holds no password for it is kept.
	 */
	public void testRemoveLastWithoutPasswordRefused() {
		Person account = account("rNoPassword");
		OidcIdentity identity = addIdentity(account, CLIENT, "sub-nopwd");
		assertNotNull("Test requires an account with a device.", account.getAuthenticationDevice());
		assertFalse(OidcIdentities.hasPasswordLogin(account));

		assertRemoveRefused(account, identity, I18NConstants.ERROR_LAST_LOGIN);
	}

	/**
	 * The last identity of an account without any device is kept.
	 */
	public void testRemoveLastWithoutDeviceRefused() {
		Person account = account("rNoDevice");
		OidcIdentity identity = addIdentity(account, CLIENT, "sub-nodevice");
		try (Transaction tx = kb().beginTransaction()) {
			account.setAuthenticationDeviceID(null);
			tx.commit();
		}
		assertNull(account.getAuthenticationDevice());

		assertRemoveRefused(account, identity, I18NConstants.ERROR_LAST_LOGIN);
	}

	/**
	 * A device that checks the password externally (e.g. LDAP) counts as a password.
	 */
	public void testExternalPasswordDevice() {
		Person account = account("rExternal");
		assertTrue(OidcIdentities.hasPasswordLogin(device(false, false), account));
		assertTrue(OidcIdentities.hasPasswordLogin(device(true, true), account));
		assertFalse(OidcIdentities.hasPasswordLogin(device(false, true), account));
		assertFalse(OidcIdentities.hasPasswordLogin(null, account));
	}

	/**
	 * An identity of another account is not removed through this one.
	 */
	public void testRemoveForeignIdentityRefused() {
		Person owner = account("rOwner");
		Person other = account("rOther");
		OidcIdentity identity = addIdentity(owner, CLIENT, "sub-foreign");
		addIdentity(owner, OTHER_CLIENT, "sub-foreign-2");

		assertRemoveRefused(other, identity, I18NConstants.ERROR_NOT_CONNECTED);
	}

	/**
	 * Removing an identity takes back the group memberships only it has granted.
	 */
	public void testRemoveRevokesGrantedGroups() {
		Person account = account("rGroups");
		OidcIdentity first = addIdentity(account, CLIENT, "sub-g1");
		OidcIdentity second = addIdentity(account, OTHER_CLIENT, "sub-g2");
		Group own = group("rGroupOwn");
		Group shared = group("rGroupShared");
		try (Transaction tx = kb().beginTransaction()) {
			own.addMember(account);
			shared.addMember(account);
			first.getGrantedGroupsModifiable().add(own);
			first.getGrantedGroupsModifiable().add(shared);
			second.getGrantedGroupsModifiable().add(shared);
			tx.commit();
		}

		remove(account, first);

		assertFalse("A membership only the removed identity granted must go.",
			own.getDirectMembers().contains(account));
		assertTrue("A membership another identity grants must stay.", shared.getDirectMembers().contains(account));
	}

	private VerificationResult connect(Person account, OidcProfile profile, Supplier<Person> resolver) {
		String token = _verifications.register(account, OidcIdentities.connectHandler(_connected::incrementAndGet));
		return _verifications.complete(token, Pac4jAuthenticationServlet.createVerifiedIdentity(profile, resolver));
	}

	private static VerifiedIdentity verified(OidcProfile profile) {
		return Pac4jAuthenticationServlet.createVerifiedIdentity(profile, failingResolver());
	}

	private static Supplier<Person> failingResolver() {
		return () -> {
			throw new AssertionError("Connecting must not resolve the account of the identity.");
		};
	}

	private void remove(Person account, OidcIdentity identity) {
		try (Transaction tx = kb().beginTransaction()) {
			OidcIdentities.removeIdentity(account, identity);
			tx.commit();
		}
	}

	private void assertRemoveRefused(Person account, OidcIdentity identity, ResKey expectedReason) {
		int before = OidcIdentities.getIdentities(OidcIdentities.getOwner(identity)).size();
		try (Transaction tx = kb().beginTransaction()) {
			OidcIdentities.removeIdentity(account, identity);
			tx.commit();
			fail("Removing must be refused.");
		} catch (TopLogicException ex) {
			assertEquals(expectedReason, ex.getErrorKey());
		}
		assertTrue(identity.tValid());
		assertEquals(before, OidcIdentities.getIdentities(OidcIdentities.getOwner(identity)).size());
	}

	private OidcIdentity addIdentity(Person account, String client, String subject) {
		try (Transaction tx = kb().beginTransaction()) {
			OidcIdentity result = OidcIdentities.addIdentity(account, client, subject);
			tx.commit();
			return result;
		}
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accountNames.add(name);
		return account;
	}

	private Group group(String name) {
		try (Transaction tx = kb().beginTransaction()) {
			Group result = Group.createGroup(name);
			tx.commit();
			_groups.add(result);
			return result;
		}
	}

	/**
	 * An {@link AuthenticationDevice} answering only whether it holds a password and whether it
	 * lets the password be changed.
	 */
	private static AuthenticationDevice device(boolean hasPassword, boolean allowPwdChange) {
		return (AuthenticationDevice) Proxy.newProxyInstance(TestOidcIdentityConnection.class.getClassLoader(),
			new Class<?>[] { AuthenticationDevice.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "hasPassword":
						return hasPassword;
					case "allowPwdChange":
						return allowPwdChange;
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	private static String reauthentication(String client) {
		return Pac4jConfigFactory.getReauthenticationName(client);
	}

	private static OidcProfile profile(String clientName, String subject) {
		OidcProfile profile = new OidcProfile();
		profile.setId(subject);
		profile.setClientName(clientName);
		return profile;
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite, with a knowledge base holding the model of the external identities.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestOidcIdentityConnection.class),
				ModelService.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE));
	}

}
