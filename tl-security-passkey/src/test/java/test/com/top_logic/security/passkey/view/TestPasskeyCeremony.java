/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey.view;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.security.passkey.SoftwareAuthenticator;
import test.com.top_logic.security.passkey.TestPasskeyService;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.security.passkey.PasskeyCredentialRepository;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PendingRegistration;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.view.I18NConstants;
import com.top_logic.security.passkey.view.PasskeyAuthentication;
import com.top_logic.security.passkey.view.PasskeyCeremony;
import com.top_logic.security.passkey.view.PasskeyRegistration;
import com.top_logic.security.passkey.view.PasskeyRegistrationElement;
import com.top_logic.security.passkey.view.PasskeySecondFactor;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link PasskeyCeremony}: the credential the browser answers with is checked by the
 * {@link PasskeyService}, the account it established reaches the success handler, and every answer
 * - accepted, refused or failed in the browser - leaves fresh options for the next attempt. Also
 * tests {@link PasskeySecondFactor} and the account check of {@link PasskeyRegistrationElement}.
 */
public class TestPasskeyCeremony extends BasicTestCase {

	private static final String LABEL = "Test passkey";

	private final List<Person> _accounts = new ArrayList<>();

	private final List<Object> _successes = new ArrayList<>();

	private ReactContext _context;

	private HttpServletRequest _request;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_request = TestPasskeyService.request("http", TestPasskeyService.HOST, TestPasskeyService.PORT);
	}

	@Override
	protected void tearDown() throws Exception {
		for (Person account : _accounts) {
			if (account.isAlive()) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accounts.clear();
		_successes.clear();
		super.tearDown();
	}

	/** A passkey alone logs its account in. */
	public void testPasswordlessLogin() throws Exception {
		Person account = account("ceremonyPasswordless");
		SoftwareAuthenticator authenticator = registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Sign in", false);
		assertNull("No ceremony before it is offered.", control.getOptions());
		ceremony.offer(null);
		String options = control.getOptions();
		assertNotNull(options);

		ceremony.onCredential(_context, authenticator.get(options));

		assertEquals(List.of(account), _successes);
		assertNull(control.getError());
		assertNotNull("Fresh options for a further login.", control.getOptions());
		assertFalse(options.equals(control.getOptions()));
	}

	/** A ceremony offered for an account refuses the passkey of another account. */
	public void testOtherAccountRefused() throws Exception {
		Person account = account("ceremonyOwner");
		Person other = account("ceremonyOther");
		SoftwareAuthenticator authenticator = registered(account);
		registered(other);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(other);
		String options = control.getOptions();

		ceremony.onCredential(_context, authenticator.get(options));

		assertEquals(List.of(), _successes);
		assertEquals(text(com.top_logic.security.passkey.I18NConstants.ERROR_AUTHENTICATION_FAILED),
			control.getError());
		assertNotNull("Fresh options after a refusal.", control.getOptions());
		assertFalse(options.equals(control.getOptions()));

		ceremony.offer(account);
		ceremony.onCredential(_context, authenticator.get(control.getOptions()));
		assertEquals(List.of(account), _successes);
		assertNull(control.getError());
	}

	/** Shortly before the pending ceremony expires, fresh options replace it without a message. */
	public void testRefreshBeforeExpiry() throws Exception {
		Person account = account("ceremonyRefresh");
		SoftwareAuthenticator authenticator = registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);
		String options = control.getOptions();

		ceremony.onExpired(_context);

		String fresh = control.getOptions();
		assertNotNull(fresh);
		assertFalse("Fresh options with a new challenge.", options.equals(fresh));
		assertNull(control.getError());

		ceremony.onCredential(_context, authenticator.get(options));
		assertEquals("The replaced options are no longer accepted.", List.of(), _successes);

		ceremony.onCredential(_context, authenticator.get(control.getOptions()));
		assertEquals(List.of(account), _successes);
	}

	/** Options and acceptance of a pending ceremony expire after the same time. */
	public void testOptionsCarryTheTimeout() throws Exception {
		Person account = account("ceremonyTimeout");
		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);

		long timeout = PasskeyService.getInstance().getConfig().getTimeout();
		assertTrue(control.getOptions(), control.getOptions().contains("\"timeout\":" + timeout));
	}

	/** A credential answering no offered ceremony is refused. */
	public void testWithdrawnCeremonyRefused() throws Exception {
		Person account = account("ceremonyWithdrawn");
		SoftwareAuthenticator authenticator = registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);
		String options = control.getOptions();
		ceremony.withdraw(null);
		assertNull(control.getOptions());

		ceremony.onCredential(_context, authenticator.get(options));

		assertEquals(List.of(), _successes);
		assertEquals(text(com.top_logic.security.passkey.I18NConstants.ERROR_REQUEST_EXPIRED), control.getError());
		assertNull(control.getOptions());
	}

	/** A cancelled browser dialog shows a hint and offers fresh options; a refused automatic start shows nothing. */
	public void testBrowserFailure() throws Exception {
		Person account = account("ceremonyCancel");
		registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", true);
		ceremony.offer(account);
		String options = control.getOptions();

		ceremony.onFailure(_context, PasskeyCeremony.NOT_ALLOWED_ERROR, "No user activation.");
		assertNull("A refused automatic start is not reported.", control.getError());
		assertFalse(options.equals(control.getOptions()));

		ceremony.onFailure(_context, PasskeyCeremony.NOT_ALLOWED_ERROR, "Cancelled.");
		assertEquals(text(I18NConstants.ERROR_CANCELLED), control.getError());
		assertNotNull(control.getOptions());

		ceremony.onFailure(_context, "UnknownError", "");
		assertEquals(text(I18NConstants.ERROR_BROWSER__NAME.fill("UnknownError")), control.getError());
	}

	/** A registration stores a passkey for the offered account and hands it to the success handler. */
	public void testRegistration() throws Exception {
		Person account = account("ceremonyRegister");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(TestPasskeyService.ORIGIN, 0);

		PasskeyRegistration ceremony = new PasskeyRegistration((context, passkey) -> _successes.add(passkey)) {
			@Override
			protected HttpServletRequest request() {
				return _request;
			}

			@Override
			protected String newLabel() {
				return LABEL;
			}
		};
		ReactWebAuthnControl control = ceremony.createControl(_context, "Create", false);
		ceremony.offer(account);
		String options = control.getOptions();

		ceremony.onCredential(_context, authenticator.create(options));

		assertEquals(1, _successes.size());
		Passkey passkey = (Passkey) _successes.get(0);
		assertEquals(account, PasskeyCredentialRepository.getOwner(passkey));
		assertEquals(LABEL, passkey.getLabel());
		assertNull(control.getError());
		assertTrue("The next registration excludes the new passkey.",
			control.getOptions().contains(authenticator.getCredentialId()));

		ceremony.onCredential(_context, authenticator.create(options));
		assertEquals("A replayed registration is refused.", 1, _successes.size());
		assertNotNull(control.getError());
	}

	/** The passkey second factor is set up with a passkey and allowed unless MFA is disabled. */
	public void testSecondFactor() throws Exception {
		PasskeySecondFactor.Config<?> config = TypedConfiguration.newConfigItem(PasskeySecondFactor.Config.class);
		config.setName("passkey");
		config.update(config.descriptor().getProperty(PasskeySecondFactor.Config.LABEL), ResKey.text("Passkey"));
		PasskeySecondFactor method =
			(PasskeySecondFactor) new DefaultInstantiationContext(TestPasskeyCeremony.class).getInstance(config);

		Person account = account("ceremonyFactor");
		assertFalse(method.isEnrolled(account));
		assertTrue(method.canEnroll(account));

		registered(account);
		assertTrue(method.isEnrolled(account));

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Disable MFA"))) {
			account.setMFARequirement(MfaRequirement.DISABLED);
			tx.commit();
		}
		assertFalse(method.canEnroll(account));
		assertFalse(method.canEnroll(PersonManager.getManager().getAnonymous()));
	}

	/** Outside the login, a passkey is created only for the account logged in. */
	public void testRegistrationOnlyForCurrentUser() throws Exception {
		Person account = account("ceremonySelf");
		Person other = account("ceremonyForeign");

		assertNull(PasskeyRegistrationElement.refusal(account, false, account));
		assertEquals(I18NConstants.ERROR_NOT_CURRENT_USER, PasskeyRegistrationElement.refusal(other, false, account));
	}

	/** A registration completes the login only for an account setting up its second factor. */
	public void testLoginRegistrationOnlyWhileEnrolling() throws Exception {
		Person account = account("ceremonyEnrolled");
		registered(account);

		assertNotNull("An account with a second factor confirms itself with it.",
			PasskeyRegistrationElement.refusal(account, true, null));
	}

	private PasskeyAuthentication authentication() {
		return new PasskeyAuthentication((context, account) -> {
			assertSame(_context, context);
			_successes.add(account);
		}) {
			@Override
			protected HttpServletRequest request() {
				return _request;
			}
		};
	}

	private SoftwareAuthenticator registered(Person account) throws Exception {
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(TestPasskeyService.ORIGIN, 0);
		PasskeyService service = PasskeyService.getInstance();
		PendingRegistration registration = service.startRegistration(account, _request);
		service.finishRegistration(registration, authenticator.create(registration.toOptionsJson()), LABEL);
		return authenticator;
	}

	private Person account(String name) {
		Person account = TestPerson.createPerson(name);
		_accounts.add(account);
		return account;
	}

	private static String text(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	/**
	 * The suite, with a knowledge base holding the passkey model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPasskeyCeremony.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}
