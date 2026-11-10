/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.selfservice.invitation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.altcha.altcha.v2.Altcha;
import org.altcha.altcha.v2.Altcha.Challenge;
import org.altcha.altcha.v2.Altcha.Solution;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.security.selfservice.invitation.RecordedMails.Mail;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.security.captcha.CaptchaService;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.invitation.SelfRegistration;
import com.top_logic.security.selfservice.invitation.SelfRegistrationMode;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.security.selfservice.model.Registration;
import com.top_logic.security.selfservice.model.RegistrationState;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of the {@link SelfRegistration} of accounts without an invitation.
 */
public class TestSelfRegistration extends BasicTestCase {

	private static final String PASSWORD = "Secret-Passw0rd";

	/** The variable defining the public address of the application, {@link AliasManager#HOST}. */
	private static final String HOST_VARIABLE = "tl_host";

	private static final String PUBLIC_ADDRESS = "https://app.example.com";

	/** Kind of the mail recorded by the test script for the registration code. */
	private static final String REGISTRATION_MAIL = "registration";

	/** Kind of the mail recorded by the test script for an address with an account. */
	private static final String EXISTS_MAIL = "registrationExists";

	/** Kind of the mail recorded by the test script for an approval. */
	private static final String APPROVAL_MAIL = "approval";

	/** Kind of the record of the test callback. */
	private static final String CALLBACK_RECORD = "callback";

	private static int _clientCounter;

	private final List<String> _accounts = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RecordedMails.take();
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction(ResKey.text("Delete test registrations"))) {
			for (String email : _accounts) {
				Person account = Person.byName(kb(), email);
				if (account == null) {
					continue;
				}
				Registration registration = registrationOf(account);
				if (registration != null) {
					registration.tDelete();
				}
				TestPerson.deletePersonAndUser(account);
			}
			tx.commit();
		}
		_accounts.clear();
		configure(SelfRegistrationMode.OFF, false, null);
		RecordedMails.take();
		super.tearDown();
	}

	/** Without a mode, no code is sent and no account is registered. */
	public void testOffRefuses() throws Exception {
		configure(SelfRegistrationMode.OFF, false, null);
		assertFalse(registration().isEnabled());

		String email = "off@example.com";
		assertRefused(I18NConstants.ERROR_SELF_REGISTRATION_DISABLED,
			() -> registration().requestCode(email, null, newClient()));
		assertRefused(I18NConstants.ERROR_SELF_REGISTRATION_DISABLED,
			() -> registration().register(email, "Doe", "Jane", PASSWORD.toCharArray()));

		assertTrue(RecordedMails.take().isEmpty());
		assertNull(Person.byName(kb(), email));
	}

	/**
	 * In mode {@link SelfRegistrationMode#DIRECT}, the code goes to the address, and the account
	 * can log in right away.
	 */
	public void testDirect() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false, null);
		assertTrue(registration().isEnabled());
		assertTrue(MailSenderService.isConfigured());

		String email = "direct@example.com";
		VerificationCode code = registration().requestCode(email, null, newClient());

		List<Mail> mails = RecordedMails.take();
		assertEquals(List.of(new Mail(REGISTRATION_MAIL, email, code.getCode())), mails);

		Person account = register(email);
		assertSame(account, Person.byName(kb(), email));
		assertFalse(account.isLocked());
		assertEquals("Doe", account.getUser().getName());
		assertEquals("Jane", account.getUser().getFirstName());
		assertEquals(email, account.getUser().getEMail());
		assertEquals(InvitationModule.getInstance().getConfig().getAuthenticationDevice(),
			account.getAuthenticationDeviceID());
		assertTrue(account.getAuthenticationDevice()
			.authentify(LoginCredentials.fromUserAndPassword(account, PASSWORD.toCharArray())));

		Registration record = registrationOf(account);
		assertNotNull(record);
		assertEquals(email, record.getEmail());
		assertNotNull(record.getApproved());
		assertNull(record.getApprovedBy());
		assertNotNull(record.getCreatedAt());
		assertEquals(RegistrationState.APPROVED_CLASSIFIER, record.getState().getName());
		assertNull(SelfRegistration.pendingRegistration(account));

		assertRefused(I18NConstants.ERROR_REGISTRATION_NOT_PENDING, () -> registration().approve(record));
	}

	/** The callback runs with each registered account. */
	public void testCallbackAccepts() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false,
			"account -> {testSelfServiceMail(\"" + CALLBACK_RECORD
				+ "\", $account.get(`tl.accounts:Person#name`), null); null}");

		String email = "callback@example.com";
		Person account = register(email);

		assertEquals(List.of(new Mail(CALLBACK_RECORD, email, null)), RecordedMails.take());
		assertNotNull(registrationOf(account));
	}

	/** A callback refusing the account leaves neither account nor record. */
	public void testCallbackRefuses() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false, "account -> \"Refused by test\"");

		String email = "refused@example.com";
		_accounts.add(email);
		assertRefused(I18NConstants.ERROR_ACCOUNT_CREATION_DENIED,
			() -> registration().register(email, "Doe", null, PASSWORD.toCharArray()));

		assertNull(Person.byName(kb(), email));
		assertTrue(registrations(email).isEmpty());
	}

	/**
	 * In mode {@link SelfRegistrationMode#APPROVAL}, the account is locked until an administrator
	 * approves the registration.
	 */
	public void testApproval() throws Exception {
		configure(SelfRegistrationMode.APPROVAL, false, null);

		String email = "approval@example.com";
		Person account = register(email);
		assertTrue(account.isLocked());

		Registration record = SelfRegistration.pendingRegistration(account);
		assertNotNull(record);
		assertTrue(SelfRegistration.isPending(record));
		assertEquals(RegistrationState.PENDING_CLASSIFIER, record.getState().getName());
		assertNull(record.getApproved());

		Person approver = createApprover();
		Object sent = TLContextManager.<Object, Exception, RuntimeException> inPersonContext(approver,
			() -> withPublicAddress(PUBLIC_ADDRESS, () -> registration().approve(record)));
		assertEquals(Boolean.TRUE, sent);

		assertFalse(account.isLocked());
		assertNotNull(record.getApproved());
		assertNotNull(record.getApprovedBy());
		assertSame(approver, record.getApprovedBy());
		assertEquals(RegistrationState.APPROVED_CLASSIFIER, record.getState().getName());
		assertNull(SelfRegistration.pendingRegistration(account));

		List<Mail> mails = RecordedMails.take();
		assertEquals(1, mails.size());
		Mail approval = mails.get(0);
		assertEquals(APPROVAL_MAIL, approval.kind());
		assertEquals(record.getEmail(), approval.to());
		assertTrue(approval.argument(), approval.argument().startsWith(PUBLIC_ADDRESS + "/"));

		withPublicAddress(PUBLIC_ADDRESS, () -> {
			assertRefused(I18NConstants.ERROR_REGISTRATION_NOT_PENDING, () -> registration().approve(record));
			return null;
		});
		assertTrue(RecordedMails.take().isEmpty());
	}

	/**
	 * An account of mode {@link SelfRegistrationMode#APPROVAL} unlocked directly, e.g. in the account
	 * administration, can log in, so its registration no longer waits for an approval and cannot be
	 * approved any more; no mail is sent.
	 */
	public void testUnlockingAccountEndsPending() throws Exception {
		configure(SelfRegistrationMode.APPROVAL, false, null);

		String email = "unlocked@example.com";
		Person account = register(email);
		Registration record = SelfRegistration.pendingRegistration(account);
		assertNotNull(record);

		try (Transaction tx = kb().beginTransaction(ResKey.text("Unlock account"))) {
			account.setLocked(false);
			tx.commit();
		}

		assertFalse(SelfRegistration.isPending(record));
		assertNull(SelfRegistration.pendingRegistration(account));
		assertEquals(RegistrationState.APPROVED_CLASSIFIER, record.getState().getName());
		assertNull(record.getApproved());
		assertNull(record.getApprovedBy());

		withPublicAddress(PUBLIC_ADDRESS, () -> {
			assertRefused(I18NConstants.ERROR_REGISTRATION_NOT_PENDING, () -> registration().approve(record));
			return null;
		});
		assertTrue(RecordedMails.take().isEmpty());
	}

	/**
	 * Deleting the account of a registration, e.g. to reject a pending registration, deletes the
	 * registration.
	 */
	public void testDeletingAccountDeletesRegistration() throws Exception {
		configure(SelfRegistrationMode.APPROVAL, false, null);

		String email = "rejected@example.com";
		Person account = register(email);
		Registration record = SelfRegistration.pendingRegistration(account);
		assertNotNull(record);

		try (Transaction tx = kb().beginTransaction(ResKey.text("Reject registration"))) {
			account.tDelete();
			tx.commit();
		}

		assertFalse(record.tValid());
		assertTrue(registrations(email).isEmpty());
		assertNull(Person.byName(kb(), email));
		assertNull(SelfRegistration.pendingRegistration(null));
	}

	/**
	 * For an address with an account, a mail telling so is sent instead of the code; the caller
	 * gets a code nevertheless.
	 */
	public void testExistingAccount() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false, null);
		String email = "exists@example.com";
		register(email);
		RecordedMails.take();

		VerificationCode code = registration().requestCode(email, null, newClient());
		assertNotNull(code);

		assertEquals(List.of(new Mail(EXISTS_MAIL, email, null)), RecordedMails.take());
	}

	/** An invalid address is refused before anything is sent. */
	public void testInvalidAddressRefused() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false, null);

		assertRefused(I18NConstants.ERROR_INVALID_EMAIL__ADDRESS,
			() -> registration().requestCode("no address", null, newClient()));
		assertRefused(I18NConstants.ERROR_INVALID_EMAIL__ADDRESS,
			() -> registration().register("no address", "Doe", null, PASSWORD.toCharArray()));
		assertTrue(RecordedMails.take().isEmpty());
	}

	/** A second request for the same address must wait, also from another client. */
	public void testThrottlePerAddress() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false, null);
		String email = "burst@example.com";

		registration().requestCode(email, null, newClient());
		assertRefused(I18NConstants.ERROR_VERIFICATION_CODE_REQUESTED_TOO_OFTEN__SECONDS,
			() -> registration().requestCode(email.toUpperCase(), null, newClient()));

		assertEquals(1, RecordedMails.take().size());
	}

	/** A second request of the same client must wait, also for another address. */
	public void testThrottlePerClient() throws Exception {
		configure(SelfRegistrationMode.DIRECT, false, null);
		String client = newClient();

		registration().requestCode("client-1@example.com", null, client);
		assertRefused(I18NConstants.ERROR_VERIFICATION_CODE_REQUESTED_TOO_OFTEN__SECONDS,
			() -> registration().requestCode("client-2@example.com", null, client));

		assertEquals(1, RecordedMails.take().size());
	}

	/** With a required captcha, a code is only sent for a solved captcha. */
	public void testCaptcha() throws Exception {
		configure(SelfRegistrationMode.DIRECT, true, null);
		String email = "captcha@example.com";

		assertRefused(com.top_logic.security.captcha.I18NConstants.ERROR_CAPTCHA_INVALID,
			() -> registration().requestCode(email, null, newClient()));
		assertRefused(com.top_logic.security.captcha.I18NConstants.ERROR_CAPTCHA_INVALID,
			() -> registration().requestCode(email, "invalid", newClient()));
		assertTrue(RecordedMails.take().isEmpty());

		String payload = solve(CaptchaService.getInstance().createChallenge());
		VerificationCode code = registration().requestCode(email, payload, newClient());
		assertEquals(List.of(new Mail(REGISTRATION_MAIL, email, code.getCode())), RecordedMails.take());
	}

	/**
	 * A plain account approving a registration, deleted after the test.
	 */
	private Person createApprover() {
		String name = "registration-approver";
		_accounts.add(name);
		return TestPerson.createPerson(name);
	}

	private Person register(String email) {
		_accounts.add(email);
		return registration().register(email, "Doe", "Jane", PASSWORD.toCharArray());
	}

	private static Registration registrationOf(Person account) {
		List<Registration> registrations = registrations(account.getName());
		return registrations.isEmpty() ? null : registrations.get(0);
	}

	private static List<Registration> registrations(String email) {
		List<Registration> result = new ArrayList<>();
		for (KnowledgeObject item : kb().getAllKnowledgeObjects(Registration.REGISTRATION_TYPE)) {
			Registration registration = item.getWrapper();
			if (email.equals(registration.getEmail())) {
				result.add(registration);
			}
		}
		return result;
	}

	/**
	 * A client address of its own, so that the throttling of one test does not affect another.
	 */
	private static String newClient() {
		return "192.0.2." + (++_clientCounter);
	}

	/**
	 * Configures the self-registration and restarts the {@link InvitationModule} with it.
	 */
	private static void configure(SelfRegistrationMode mode, boolean captcha, String callback) throws Exception {
		InvitationModule.Config config = (InvitationModule.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(InvitationModule.class);
		SelfRegistration.Config registration = config.getSelfRegistration();
		update(registration, SelfRegistration.Config.MODE, mode);
		update(registration, SelfRegistration.Config.CAPTCHA, Boolean.valueOf(captcha));
		update(registration, SelfRegistration.Config.CALLBACK,
			callback == null ? null : ExprFormat.INSTANCE.getValue(SelfRegistration.Config.CALLBACK, callback));
		ModuleUtil.INSTANCE.restart(InvitationModule.Module.INSTANCE, null);
	}

	private static void update(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	/**
	 * Solves the given captcha challenge and returns the payload the widget submits.
	 */
	private static String solve(String challenge) throws Exception {
		Challenge parsed = Altcha.parsePayload(payload(challenge, 0, "00")).challenge();
		Solution solution = Altcha.solveChallenge(parsed, Altcha.kdf(parsed.parameters().algorithm()));
		assertNotNull("Challenge not solved: " + challenge, solution);
		return payload(challenge, solution.counter(), solution.derivedKey());
	}

	private static String payload(String challenge, long counter, String derivedKey) {
		String json = "{\"challenge\":" + challenge + ",\"solution\":{\"counter\":" + counter
			+ ",\"derivedKey\":\"" + derivedKey + "\"}}";
		return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Runs the given computation with the given public address of the application
	 * ({@link AliasManager#HOST}, from the variable {@link #HOST_VARIABLE}).
	 */
	private static <T> T withPublicAddress(String host, Callable<T> computation) throws Exception {
		String before = System.getProperty(HOST_VARIABLE);
		System.setProperty(HOST_VARIABLE, host);
		try {
			AliasManager.getInstance().reload();
			return computation.call();
		} finally {
			if (before == null) {
				System.clearProperty(HOST_VARIABLE);
			} else {
				System.setProperty(HOST_VARIABLE, before);
			}
			AliasManager.getInstance().reload();
		}
	}

	private static void assertRefused(ResKey1 expected, Runnable action) {
		assertRefused(expected.fill(null), action);
	}

	private static void assertRefused(ResKey expected, Runnable action) {
		try {
			action.run();
			fail("Expected failure: " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected.plain().getKey(), ex.getErrorKey().plain().getKey());
		}
	}

	private static SelfRegistration registration() {
		return SelfRegistration.getInstance();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite, with a knowledge base holding the self-service model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSelfRegistration.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				LoginFailuresModule.Module.INSTANCE,
				MailSenderService.Module.INSTANCE,
				CaptchaService.Module.INSTANCE,
				InvitationModule.Module.INSTANCE));
	}

}
