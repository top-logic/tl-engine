/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserVerificationRequirement;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.passkey.I18NConstants;
import com.top_logic.security.passkey.PasskeyCredentialRepository;
import com.top_logic.security.passkey.PasskeyRefusedException;
import com.top_logic.security.passkey.PasskeyRefusedException.Category;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PasskeyUsage;
import com.top_logic.security.passkey.PendingAuthentication;
import com.top_logic.security.passkey.PendingRegistration;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Registration and login ceremonies of {@link PasskeyService} against a
 * {@link SoftwareAuthenticator}, with the relying party taken from the public address of the
 * application, see {@link PasskeyTestHost}.
 */
public class TestPasskeyService extends BasicTestCase {

	private static final String HOST = PasskeyTestHost.HOST;

	private static final int PORT = PasskeyTestHost.PORT;

	private static final String ORIGIN = PasskeyTestHost.ORIGIN;

	private static final String LABEL = "Test device";

	private final List<Person> _accounts = new ArrayList<>();

	private CollectingLogListener _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PasskeyTestHost.useDefault();
		_log = new CollectingLogListener(EnumSet.of(Level.WARN), true);
	}

	@Override
	protected void tearDown() throws Exception {
		_log.deactivate();
		for (Person account : _accounts) {
			if (account.isAlive()) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accounts.clear();
		super.tearDown();
	}

	/** The relying party ID and the origin come from the public address of the application. */
	public void testRelyingPartyFromPublicAddress() throws Exception {
		assertTrue(service().isAvailable());
		assertEquals(HOST, service().getRpId());
		assertEquals(Set.of(ORIGIN), service().getAddress().getOrigins());

		Person account = account("passkeyRp");
		PendingRegistration pending = service().startRegistration(account);

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

		PendingAuthentication login = service().startAuthentication(null);
		assertTrue(login.isPasswordless());
		assertTrue(login.requiresUserVerification());
		Person loggedIn = service().finishAuthentication(login, authenticator.get(login.toOptionsJson()));

		assertEquals(account, loggedIn);
		assertEquals(1, passkey.getSignatureCount());
		assertNotNull(passkey.getLastUsed());
	}

	/** A passkey logs in an account given at the start, as second factor. */
	public void testLoginWithGivenAccount() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("passkeySecond");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		PendingAuthentication login = service().startAuthentication(account);
		assertFalse(login.isPasswordless());
		Set<String> allowed = login.getRequest().getPublicKeyCredentialRequestOptions().getAllowCredentials()
			.orElseThrow().stream().map(descriptor -> descriptor.getId().getBase64Url()).collect(Collectors.toSet());
		assertEquals(Set.of(authenticator.getCredentialId()), allowed);

		assertEquals(account, service().finishAuthentication(login, authenticator.get(login.toOptionsJson())));
	}

	/** The passkeys are the default usage: a login with a passkey alone. */
	public void testDefaultUsage() throws Exception {
		assertEquals(PasskeyUsage.LOGIN, service().getUsage());
	}

	/**
	 * For a login with a passkey alone, a registration asks for a discoverable passkey and always
	 * for the verification of the user, which it requires.
	 */
	public void testRegistrationForLogin() throws Exception {
		PasskeyTestHost.use(UserVerificationRequirement.DISCOURAGED);
		assertEquals(UserVerificationRequirement.REQUIRED, service().getUserVerification());
		Person account = account("passkeyRegLogin");

		PendingRegistration registration = service().startRegistration(account);
		AuthenticatorSelectionCriteria selection = registration.getRequest().getAuthenticatorSelection().orElseThrow();
		assertEquals(ResidentKeyRequirement.REQUIRED, selection.getResidentKey().orElseThrow());
		assertEquals(UserVerificationRequirement.REQUIRED, selection.getUserVerification().orElseThrow());
		assertTrue(registration.requiresUserVerification());

		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		authenticator.setUserVerification(false);
		String credential = authenticator.create(registration.toOptionsJson());
		assertRefused(Category.USER_NOT_VERIFIED, I18NConstants.ERROR_USER_NOT_VERIFIED,
			() -> service().finishRegistration(registration, credential, LABEL));
		assertFalse(service().isEnrolled(account));

		PendingAuthentication login = service().startAuthentication(null);
		assertTrue(login.requiresUserVerification());
	}

	/**
	 * For a passkey as second factor, a registration discourages a discoverable passkey and asks
	 * for the configured verification of the user.
	 */
	public void testRegistrationForSecondFactor() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		PasskeyTestHost.use(UserVerificationRequirement.PREFERRED);
		assertEquals(UserVerificationRequirement.PREFERRED, service().getUserVerification());
		Person account = account("passkeyRegSecond");

		PendingRegistration registration = service().startRegistration(account);
		AuthenticatorSelectionCriteria selection = registration.getRequest().getAuthenticatorSelection().orElseThrow();
		assertEquals(ResidentKeyRequirement.DISCOURAGED, selection.getResidentKey().orElseThrow());
		assertEquals(UserVerificationRequirement.PREFERRED, selection.getUserVerification().orElseThrow());
		assertFalse(registration.requiresUserVerification());

		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		authenticator.setUserVerification(false);
		service().finishRegistration(registration, authenticator.create(registration.toOptionsJson()), LABEL);
		assertTrue(service().isEnrolled(account));

		PendingAuthentication login = service().startAuthentication(account);
		assertFalse(login.requiresUserVerification());
		assertEquals(account, service().finishAuthentication(login, authenticator.get(login.toOptionsJson())));
	}

	/** Each usage refuses to start the kind of login it does not offer. */
	public void testLoginOfOtherUsageRefused() throws Exception {
		Person account = account("passkeyOtherUsage");

		assertFails(I18NConstants.ERROR_NO_SECOND_FACTOR, () -> service().startAuthentication(account));

		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		assertFails(I18NConstants.ERROR_NO_PASSWORDLESS_LOGIN, () -> service().startAuthentication(null));
		assertFalse(service().startAuthentication(account).isPasswordless());
	}

	/** A credential created on another origin is refused. */
	public void testWrongOriginRejected() throws Exception {
		Person account = account("passkeyOrigin");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator("http://evil.example:" + PORT, 0);

		PendingRegistration registration = service().startRegistration(account);
		String credential = authenticator.create(registration.toOptionsJson());
		PasskeyRefusedException refused = assertRefused(Category.WRONG_ORIGIN,
			I18NConstants.ERROR_WRONG_ORIGIN__ORIGIN.fill("http://evil.example:" + PORT),
			() -> service().finishRegistration(registration, credential, LABEL));
		assertEquals(authenticator.getCredentialId(), refused.getCredentialId());
		assertTrue("The authenticator stored a key the application does not keep.", refused.isCredentialUnknown());
		assertFalse(service().isEnrolled(account));
		assertWarned("Registration of a passkey refused (WRONG_ORIGIN)", authenticator.getCredentialId(),
			"account '" + account.getName() + "'", "origin of the page: http://evil.example:" + PORT, ORIGIN);

		authenticator.setOrigin(ORIGIN);
		register(account, authenticator);
		authenticator.setOrigin("https://" + HOST + ":" + PORT);
		PendingAuthentication login = service().startAuthentication(null);
		String assertion = authenticator.get(login.toOptionsJson());
		PasskeyRefusedException refusedLogin = assertRefused(Category.WRONG_ORIGIN,
			I18NConstants.ERROR_WRONG_ORIGIN__ORIGIN.fill("https://" + HOST + ":" + PORT),
			() -> service().finishAuthentication(login, assertion));
		assertFalse("A registered passkey is not forgotten.", refusedLogin.isCredentialUnknown());
	}

	/** A login with a passkey no account has is refused, and the passkey reported as unknown. */
	public void testUnknownPasskeyRejected() throws Exception {
		Person account = account("passkeyUnknown");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		Passkey passkey = register(account, authenticator);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Delete passkey"))) {
			passkey.tDelete();
			tx.commit();
		}

		PendingAuthentication login = service().startAuthentication(null);
		String assertion = authenticator.get(login.toOptionsJson());
		PasskeyRefusedException refused = assertRefused(Category.UNKNOWN_PASSKEY, I18NConstants.ERROR_UNKNOWN_PASSKEY,
			() -> service().finishAuthentication(login, assertion));
		assertEquals(authenticator.getCredentialId(), refused.getCredentialId());
		assertTrue(refused.isCredentialUnknown());
		assertWarned("Login with a passkey refused (UNKNOWN_PASSKEY)", authenticator.getCredentialId(),
			"account of the user handle '" + account.getName() + "'", "origin of the page: " + ORIGIN);
	}

	/** A registration answered twice is refused the second time, and its key reported as unknown. */
	public void testExpiredRegistrationReportsCredential() throws Exception {
		Person account = account("passkeyExpiredReg");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		PendingRegistration registration = service().startRegistration(account);
		String credential = authenticator.create(registration.toOptionsJson());
		service().finishRegistration(registration, credential, LABEL);

		SoftwareAuthenticator second = new SoftwareAuthenticator(ORIGIN, 0);
		String other = second.create(registration.toOptionsJson());
		PasskeyRefusedException refused = assertRefused(Category.EXPIRED, I18NConstants.ERROR_REQUEST_EXPIRED,
			() -> service().finishRegistration(registration, other, LABEL));
		assertEquals(second.getCredentialId(), refused.getCredentialId());
		assertTrue(refused.isCredentialUnknown());
	}

	/** An unparsable answer is refused without a credential ID. */
	public void testInvalidDataRejected() throws Exception {
		Person account = account("passkeyInvalid");
		PendingRegistration registration = service().startRegistration(account);
		PasskeyRefusedException refused = assertRefused(Category.INVALID_DATA,
			I18NConstants.ERROR_INVALID_CEREMONY_DATA,
			() -> service().finishRegistration(registration, "{\"id\": 42}", LABEL));
		assertNull(refused.getCredentialId());
		assertFalse(refused.isCredentialUnknown());
	}

	/** Without a public address, passkeys are not available and no ceremony starts. */
	public void testNoPublicAddress() throws Exception {
		Person account = account("passkeyNoHost");
		try {
			PasskeyTestHost.use("");
			assertFalse(service().isAvailable());
			assertNull(service().getRpId());
			assertFails(I18NConstants.ERROR_NOT_SET_UP, () -> service().startRegistration(account));
			assertFails(I18NConstants.ERROR_NOT_SET_UP, () -> service().startAuthentication(null));
			assertFails(I18NConstants.ERROR_NOT_SET_UP, () -> service().startAuthentication(account));

			PasskeyTestHost.use("not an address");
			assertFalse("A malformed public address sets up no passkeys.", service().isAvailable());
			assertFails(I18NConstants.ERROR_NOT_SET_UP, () -> service().startRegistration(account));
		} finally {
			PasskeyTestHost.useDefault();
		}
	}

	/** The public address is normalized to the origin a browser reports. */
	public void testPublicAddressNormalized() throws Exception {
		try {
			PasskeyTestHost.use("HTTPS://App.Example.COM:443/tl-demo/");
			assertEquals("app.example.com", service().getRpId());
			assertEquals(Set.of("https://app.example.com"), service().getAddress().getOrigins());
		} finally {
			PasskeyTestHost.useDefault();
		}
	}

	/** An answer to an earlier challenge is refused, and a ceremony is answered only once. */
	public void testReplayRejected() throws Exception {
		Person account = account("passkeyReplay");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		PendingAuthentication first = service().startAuthentication(null);
		String firstAnswer = authenticator.get(first.toOptionsJson());
		assertEquals(account, service().finishAuthentication(first, firstAnswer));

		PasskeyRefusedException expired = assertRefused(Category.EXPIRED, I18NConstants.ERROR_REQUEST_EXPIRED,
			() -> service().finishAuthentication(first, firstAnswer));
		assertFalse(expired.isCredentialUnknown());

		PendingAuthentication second = service().startAuthentication(null);
		PasskeyRefusedException failed = assertFails(I18NConstants.ERROR_AUTHENTICATION_FAILED__REASON.fill(""),
			"Incorrect challenge", () -> service().finishAuthentication(second, firstAnswer));
		assertEquals(Category.FAILED, failed.getCategory());
		assertTrue("The reason of the library is shown.",
			Resources.getInstance().getString(failed.getErrorKey()).contains("Incorrect challenge"));
		assertWarned("Login with a passkey refused (FAILED)", "Incorrect challenge");
	}

	/** The passkey of another account is refused when the account is given at the start. */
	public void testCredentialOfOtherAccountRejected() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("passkeyOwner");
		Person other = account("passkeyOther");
		register(account, new SoftwareAuthenticator(ORIGIN, 0));
		SoftwareAuthenticator otherAuthenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(other, otherAuthenticator);

		PendingAuthentication login = service().startAuthentication(account);
		String assertion = otherAuthenticator.get(login.toOptionsJson());
		PasskeyRefusedException refused = assertRefused(Category.OTHER_ACCOUNT,
			I18NConstants.ERROR_OTHER_ACCOUNT__ACCOUNT.fill(account.getName()),
			() -> service().finishAuthentication(login, assertion));
		assertEquals(otherAuthenticator.getCredentialId(), refused.getCredentialId());
		assertFalse("A passkey of another account is not forgotten.", refused.isCredentialUnknown());
		assertWarned("(OTHER_ACCOUNT)", "expected account '" + account.getName() + "'",
			"owner of the passkey '" + other.getName() + "'");
	}

	/** A login with a passkey alone is refused if the authenticator did not verify the user. */
	public void testMissingUserVerificationRejected() throws Exception {
		Person account = account("passkeyUv");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		authenticator.setUserVerification(false);
		PendingAuthentication login = service().startAuthentication(null);
		String assertion = authenticator.get(login.toOptionsJson());
		assertRefused(Category.USER_NOT_VERIFIED, I18NConstants.ERROR_USER_NOT_VERIFIED,
			() -> service().finishAuthentication(login, assertion));
	}

	/** A signature counter that does not increase (a cloned authenticator) is refused. */
	public void testCounterRegressionRejected() throws Exception {
		Person account = account("passkeyCounter");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 5);
		Passkey passkey = register(account, authenticator);
		assertEquals(5, passkey.getSignatureCount());

		PendingAuthentication login = service().startAuthentication(null);
		service().finishAuthentication(login, authenticator.get(login.toOptionsJson()));
		assertEquals(6, passkey.getSignatureCount());

		authenticator.setSignatureCount(2);
		PendingAuthentication clone = service().startAuthentication(null);
		String assertion = authenticator.get(clone.toOptionsJson());
		assertRefused(Category.SIGNATURE_COUNTER, I18NConstants.ERROR_SIGNATURE_COUNTER,
			() -> service().finishAuthentication(clone, assertion));
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
			() -> service().startRegistration(account));

		Person anonymous = PersonManager.getManager().getAnonymous();
		assertFails(I18NConstants.ERROR_PASSKEYS_NOT_ALLOWED__ACCOUNT.fill(anonymous.getName()),
			() -> service().startRegistration(anonymous));
	}

	/** A further registration excludes the passkeys the account already has. */
	public void testExcludeCredentials() throws Exception {
		Person account = account("passkeyExclude");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(ORIGIN, 0);
		register(account, authenticator);

		PendingRegistration again = service().startRegistration(account);
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
		PendingRegistration registration = service().startRegistration(account);
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

	/**
	 * Asserts that a warning containing all given fragments was logged.
	 */
	private void assertWarned(String... fragments) {
		List<String> messages = new ArrayList<>();
		for (LogEntry entry : _log.getLogEntries()) {
			messages.add(entry.getMessage());
			if (Arrays.stream(fragments).allMatch(entry.getMessage()::contains)) {
				return;
			}
		}
		fail("No warning with " + Arrays.toString(fragments) + " in " + messages);
	}

	private static void assertFails(ResKey expected, Runnable action) {
		try {
			action.run();
			fail("Expected failure: " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected.plain().getKey(), ex.getErrorKey().plain().getKey());
		}
	}

	/**
	 * Asserts that the action refuses a credential with the given category and message.
	 */
	private static PasskeyRefusedException assertRefused(Category category, ResKey expected, Runnable action) {
		try {
			action.run();
			fail("Expected refusal: " + category);
			throw new AssertionError();
		} catch (PasskeyRefusedException ex) {
			assertEquals(category, ex.getCategory());
			assertEquals(Resources.getInstance().getString(expected), Resources.getInstance().getString(ex.getErrorKey()));
			return ex;
		}
	}

	/**
	 * Asserts that the action refuses a credential with the given message, caused by a refusal of
	 * the library whose description contains the given fragment, which tells which check refused.
	 */
	private static PasskeyRefusedException assertFails(ResKey expected, String causeFragment, Runnable action) {
		try {
			action.run();
			fail("Expected failure: " + expected);
			throw new AssertionError();
		} catch (PasskeyRefusedException ex) {
			assertEquals(expected.plain().getKey(), ex.getErrorKey().plain().getKey());
			assertNotNull("No cause.", ex.getCause());
			assertTrue(String.valueOf(ex.getCause()), String.valueOf(ex.getCause()).contains(causeFragment));
			return ex;
		}
	}

	/**
	 * The suite, with a knowledge base holding the passkey model.
	 */
	public static Test suite() {
		PasskeyTestHost.init();
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPasskeyService.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}
