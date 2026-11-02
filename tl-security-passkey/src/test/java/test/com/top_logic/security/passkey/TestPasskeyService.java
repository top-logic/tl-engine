/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.passkey.I18NConstants;
import com.top_logic.security.passkey.PasskeyCredentialRepository;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PendingAuthentication;
import com.top_logic.security.passkey.PendingRegistration;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Registration and login ceremonies of {@link PasskeyService} against a
 * {@link SoftwareAuthenticator}, with the relying party derived from the request.
 */
public class TestPasskeyService extends BasicTestCase {

	/** The host of the {@link #request(String, String, int) requests} of the tests. */
	public static final String HOST = "localhost";

	/** The port of the {@link #request(String, String, int) requests} of the tests. */
	public static final int PORT = 8080;

	/** The origin of a page served for a request to {@link #HOST} and {@link #PORT}. */
	public static final String ORIGIN = "http://" + HOST + ":" + PORT;

	private static final String LABEL = "Test device";

	private final List<Person> _accounts = new ArrayList<>();

	private HttpServletRequest _request;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_request = request("http", HOST, PORT);
	}

	@Override
	protected void tearDown() throws Exception {
		for (Person account : _accounts) {
			if (account.isAlive()) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accounts.clear();
		super.tearDown();
	}

	/** The relying party and origin come from the request when they are not configured. */
	public void testRelyingPartyFromRequest() throws Exception {
		Person account = account("passkeyRp");
		PendingRegistration pending = service().startRegistration(account, _request);

		assertEquals(HOST, pending.getRequest().getRp().getId());
		assertTrue(pending.toOptionsJson(), pending.toOptionsJson().startsWith("{\"publicKey\":"));
		assertEquals(PasskeyCredentialRepository.userHandle(account), pending.getRequest().getUser().getId());
		assertEquals(account.getName(), pending.getRequest().getUser().getName());
	}

	/** A passkey is registered and logs in without a given account. */
	public void testRegisterAndPasswordlessLogin() throws Exception {
		Person account = account("passkeyLogin");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);

		Passkey passkey = register(account, authenticator);
		assertEquals(LABEL, passkey.getLabel());
		assertEquals(authenticator.getCredentialId(), passkey.getCredentialId());
		assertEquals(account, PasskeyCredentialRepository.getOwner(passkey));
		assertTrue(passkey.getBackupEligible());
		assertTrue(passkey.getBackedUp());
		assertEquals("hybrid,internal", passkey.getTransports());
		assertNotNull(passkey.getCreatedAt());
		assertNull(passkey.getLastUsed());
		assertTrue(service().isEnrolled(account));
		assertEquals(List.of(passkey), service().getPasskeys(account));

		PendingAuthentication login = service().startAuthentication(null, _request);
		assertTrue(login.isPasswordless());
		assertTrue(login.requiresUserVerification());
		Person loggedIn = service().finishAuthentication(login, authenticator.get(login.toOptionsJson()));

		assertEquals(account, loggedIn);
		assertEquals(1, passkey.getSignatureCount());
		assertNotNull(passkey.getLastUsed());
	}

	/** A passkey logs in an account given at the start, as second factor. */
	public void testLoginWithGivenAccount() throws Exception {
		Person account = account("passkeySecond");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		PendingAuthentication login = service().startAuthentication(account, _request);
		assertFalse(login.isPasswordless());
		Set<String> allowed = login.getRequest().getPublicKeyCredentialRequestOptions().getAllowCredentials()
			.orElseThrow().stream().map(descriptor -> descriptor.getId().getBase64Url()).collect(Collectors.toSet());
		assertEquals(Set.of(authenticator.getCredentialId()), allowed);

		assertEquals(account, service().finishAuthentication(login, authenticator.get(login.toOptionsJson())));
	}

	/** A credential created on another origin is refused. */
	public void testWrongOriginRejected() throws Exception {
		Person account = account("passkeyOrigin");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator("http://evil.example:" + PORT, 0);

		PendingRegistration registration = service().startRegistration(account, _request);
		String credential = authenticator.create(registration.toOptionsJson());
		assertFails(I18NConstants.ERROR_REGISTRATION_FAILED, "Incorrect origin",
			() -> service().finishRegistration(registration, credential, LABEL));
		assertFalse(service().isEnrolled(account));

		authenticator.setOrigin(ORIGIN);
		register(account, authenticator);
		authenticator.setOrigin("https://" + HOST + ":" + PORT);
		PendingAuthentication login = service().startAuthentication(null, _request);
		String assertion = authenticator.get(login.toOptionsJson());
		assertFails(I18NConstants.ERROR_AUTHENTICATION_FAILED, "Incorrect origin", () -> service().finishAuthentication(login, assertion));
	}

	/** An answer to an earlier challenge is refused, and a ceremony is answered only once. */
	public void testReplayRejected() throws Exception {
		Person account = account("passkeyReplay");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		PendingAuthentication first = service().startAuthentication(null, _request);
		String firstAnswer = authenticator.get(first.toOptionsJson());
		assertEquals(account, service().finishAuthentication(first, firstAnswer));

		assertFails(I18NConstants.ERROR_REQUEST_EXPIRED, () -> service().finishAuthentication(first, firstAnswer));

		PendingAuthentication second = service().startAuthentication(null, _request);
		assertFails(I18NConstants.ERROR_AUTHENTICATION_FAILED, "Incorrect challenge",
			() -> service().finishAuthentication(second, firstAnswer));
	}

	/** The passkey of another account is refused when the account is given at the start. */
	public void testCredentialOfOtherAccountRejected() throws Exception {
		Person account = account("passkeyOwner");
		Person other = account("passkeyOther");
		register(account, new SoftwareAuthenticator(ORIGIN, 0));
		SoftwareAuthenticator otherAuthenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(other, otherAuthenticator);

		PendingAuthentication login = service().startAuthentication(account, _request);
		String assertion = otherAuthenticator.get(login.toOptionsJson());
		assertFails(I18NConstants.ERROR_AUTHENTICATION_FAILED, "Unrequested credential ID", () -> service().finishAuthentication(login, assertion));
	}

	/** A login with a passkey alone is refused if the authenticator did not verify the user. */
	public void testMissingUserVerificationRejected() throws Exception {
		Person account = account("passkeyUv");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		authenticator.setUserVerification(false);
		PendingAuthentication login = service().startAuthentication(null, _request);
		String assertion = authenticator.get(login.toOptionsJson());
		assertFails(I18NConstants.ERROR_AUTHENTICATION_FAILED, "User Verification is required", () -> service().finishAuthentication(login, assertion));
	}

	/** A signature counter that does not increase (a cloned authenticator) is refused. */
	public void testCounterRegressionRejected() throws Exception {
		Person account = account("passkeyCounter");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 5);
		Passkey passkey = register(account, authenticator);
		assertEquals(5, passkey.getSignatureCount());

		PendingAuthentication login = service().startAuthentication(null, _request);
		service().finishAuthentication(login, authenticator.get(login.toOptionsJson()));
		assertEquals(6, passkey.getSignatureCount());

		authenticator.setSignatureCount(2);
		PendingAuthentication clone = service().startAuthentication(null, _request);
		String assertion = authenticator.get(clone.toOptionsJson());
		assertFails(I18NConstants.ERROR_AUTHENTICATION_FAILED, "InvalidSignatureCount", () -> service().finishAuthentication(clone, assertion));
		assertEquals(6, passkey.getSignatureCount());
	}

	/** An account with multi-factor authentication disabled and the anonymous account cannot register. */
	public void testDisabledAccountCannotRegister() throws Exception {
		Person account = account("passkeyDisabled");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Disable MFA"))) {
			account.setMFARequirement(MfaRequirement.DISABLED);
			tx.commit();
		}
		assertFalse(service().mayUsePasskeys(account));
		assertFails(I18NConstants.ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT.fill(account.getName()),
			() -> service().startRegistration(account, _request));

		Person anonymous = PersonManager.getManager().getAnonymous();
		assertFails(I18NConstants.ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT.fill(anonymous.getName()),
			() -> service().startRegistration(anonymous, _request));
	}

	/** A further registration excludes the passkeys the account already has. */
	public void testExcludeCredentials() throws Exception {
		Person account = account("passkeyExclude");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		PendingRegistration again = service().startRegistration(account, _request);
		Set<PublicKeyCredentialDescriptor> excluded = again.getRequest().getExcludeCredentials().orElseThrow();
		assertEquals(Set.of(authenticator.getCredentialId()),
			excluded.stream().map(descriptor -> descriptor.getId().getBase64Url()).collect(Collectors.toSet()));
		assertTrue(again.toOptionsJson(), again.toOptionsJson().contains(authenticator.getCredentialId()));
	}

	/** Deleting an account deletes its passkeys. */
	public void testAccountDeletionDeletesPasskeys() throws Exception {
		Person account = account("passkeyDelete");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		Passkey passkey = register(account, authenticator);
		ByteArray credentialId = ByteArray.fromBase64Url(authenticator.getCredentialId());
		assertEquals(passkey, PasskeyCredentialRepository.findPasskey(credentialId));

		TestPerson.deletePersonAndUser(account);

		assertFalse(passkey.tValid());
		assertNull(PasskeyCredentialRepository.findPasskey(credentialId));
	}

	private Passkey register(Person account, SoftwareAuthenticator authenticator) throws Exception {
		PendingRegistration registration = service().startRegistration(account, _request);
		return service().finishRegistration(registration, authenticator.create(registration.toOptionsJson()), LABEL);
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accounts.add(account);
		return account;
	}

	private static PasskeyService service() {
		return PasskeyService.getInstance();
	}

	private static void assertFails(ResKey expected, Runnable action) {
		assertFails(expected, null, action);
	}

	/**
	 * Asserts that the action fails with the given error, caused by a refusal of the library whose
	 * description contains the given fragment, which tells which check refused.
	 */
	private static void assertFails(ResKey expected, String causeFragment, Runnable action) {
		try {
			action.run();
			fail("Expected failure: " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected.plain().getKey(), ex.getErrorKey().plain().getKey());
			if (causeFragment != null) {
				assertNotNull("No cause.", ex.getCause());
				assertTrue(String.valueOf(ex.getCause()), String.valueOf(ex.getCause()).contains(causeFragment));
			}
		}
	}

	/**
	 * A request answering only the questions of the relying party: scheme, host and port.
	 */
	public static HttpServletRequest request(String scheme, String host, int port) {
		return (HttpServletRequest) Proxy.newProxyInstance(TestPasskeyService.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "getScheme":
						return scheme;
					case "getServerName":
						return host;
					case "getServerPort":
						return port;
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	/**
	 * The suite, with a knowledge base holding the passkey model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPasskeyService.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}
