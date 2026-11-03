/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.security.passkey.PasskeyTestHost;
import test.com.top_logic.security.passkey.SoftwareAuthenticator;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
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
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.security.passkey.PasskeyCredentialRepository;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PasskeyUsage;
import com.top_logic.security.passkey.PendingRegistration;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.view.I18NConstants;
import com.top_logic.security.passkey.view.PasskeyAuthentication;
import com.top_logic.security.passkey.view.PasskeyCeremony;
import com.top_logic.security.passkey.view.PasskeyLoginElement;
import com.top_logic.security.passkey.view.PasskeyRegistration;
import com.top_logic.security.passkey.view.PasskeyRegistrationElement;
import com.top_logic.security.passkey.view.PasskeySecondFactor;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link PasskeyCeremony}: the credential the browser answers with is checked by the
 * {@link PasskeyService}, the account it established reaches the success handler, and every answer
 * - accepted, refused or failed in the browser - leaves fresh options for the next attempt; a page
 * on an address the relying party does not accept gets no options; the browser is told which
 * credentials to forget. Also tests {@link PasskeySecondFactor} and the account check of
 * {@link PasskeyRegistrationElement}.
 */
public class TestPasskeyCeremony extends BasicTestCase {

	private static final String LABEL = "Test passkey";

	private final List<Person> _accounts = new ArrayList<>();

	private final List<Object> _successes = new ArrayList<>();

	/** The texts of the messages the ceremonies reported to the user. */
	private final List<String> _reports = new ArrayList<>();

	/** The number of signals sent to the browser when the success handler ran. */
	private final List<Integer> _signalsAtSuccess = new ArrayList<>();

	private RecordingQueue _queue;

	private CollectingLogListener _log;

	private ReactContext _context;

	/**
	 * Keeps the events handed to the queue, as the client would receive them.
	 */
	private static final class RecordingQueue extends SSEUpdateQueue {

		final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
			super.enqueue(event);
		}

		/** The signals to the browser: the arguments of the calls of the signal function. */
		List<List<?>> signals() throws JSON.ParseException {
			List<List<?>> result = new ArrayList<>();
			for (SSEEvent event : _events) {
				if (event instanceof FunctionCall call
					&& ReactWebAuthnControl.CLIENT_FUNCTION.equals(call.getFunctionName())) {
					result.add((List<?>) JSON.fromString(call.getArguments()));
				}
			}
			return result;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PasskeyTestHost.useDefault();
		_queue = new RecordingQueue();
		_context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
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
		_successes.clear();
		_reports.clear();
		_signalsAtSuccess.clear();
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
		assertEquals(List.of(), _reports);
		assertNull(control.getTooltip());
		assertNotNull("Fresh options for a further login.", control.getOptions());
		assertFalse(options.equals(control.getOptions()));
	}

	/** A ceremony offered for an account refuses the passkey of another account. */
	public void testOtherAccountRefused() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
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
		assertEquals(
			List.of(text(com.top_logic.security.passkey.I18NConstants.ERROR_OTHER_ACCOUNT__ACCOUNT.fill(other.getName()))),
			_reports);
		assertEquals("A passkey of another account is not forgotten.", List.of(), _queue.signals());
		assertNotNull("Fresh options after a refusal.", control.getOptions());
		assertFalse(options.equals(control.getOptions()));

		ceremony.offer(account);
		ceremony.onCredential(_context, authenticator.get(control.getOptions()));
		assertEquals(List.of(account), _successes);
		assertEquals("No further message.", 1, _reports.size());
	}

	/** Shortly before the pending ceremony expires, fresh options replace it without a message. */
	public void testRefreshBeforeExpiry() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
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
		assertEquals(List.of(), _reports);

		ceremony.onCredential(_context, authenticator.get(options));
		assertEquals("The replaced options are no longer accepted.", List.of(), _successes);

		ceremony.onCredential(_context, authenticator.get(control.getOptions()));
		assertEquals(List.of(account), _successes);
	}

	/** Options and acceptance of a pending ceremony expire after the same time. */
	public void testOptionsCarryTheTimeout() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyTimeout");
		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);

		long timeout = PasskeyService.getInstance().getConfig().getTimeout();
		assertTrue(control.getOptions(), control.getOptions().contains("\"timeout\":" + timeout));
	}

	/** A credential answering no offered ceremony is refused. */
	public void testWithdrawnCeremonyRefused() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyWithdrawn");
		SoftwareAuthenticator authenticator = registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);
		String options = control.getOptions();
		ceremony.withdraw(I18NConstants.ERROR_NOT_CURRENT_USER);
		assertNull(control.getOptions());
		assertEquals("A withdrawn ceremony explains the disabled button.", text(I18NConstants.ERROR_NOT_CURRENT_USER),
			control.getTooltip());

		ceremony.onCredential(_context, authenticator.get(options));

		assertEquals(List.of(), _successes);
		assertEquals(List.of(text(com.top_logic.security.passkey.I18NConstants.ERROR_REQUEST_EXPIRED)), _reports);
		assertNull(control.getOptions());

		ceremony.offer(account);
		assertNotNull(control.getOptions());
		assertNull("Offered again, the button needs no explanation.", control.getTooltip());
	}

	/** A cancelled browser dialog shows a hint and offers fresh options; a refused automatic start shows nothing. */
	public void testBrowserFailure() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyCancel");
		registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", true);
		ceremony.offer(account);
		String options = control.getOptions();

		ceremony.onFailure(_context, PasskeyCeremony.NOT_ALLOWED_ERROR, "No user activation.");
		assertEquals("A refused automatic start is not reported.", List.of(), _reports);
		assertFalse(options.equals(control.getOptions()));

		options = control.getOptions();
		ceremony.onFailure(_context, PasskeyCeremony.NOT_ALLOWED_ERROR, "Cancelled.");
		assertEquals(List.of(text(I18NConstants.ERROR_CANCELLED)), _reports);
		assertNotNull(control.getOptions());
		assertFalse("Fresh options after a failure.", options.equals(control.getOptions()));

		options = control.getOptions();
		ceremony.onFailure(_context, PasskeyCeremony.NOT_SUPPORTED_ERROR, "Algorithm not supported.");
		assertEquals(text(I18NConstants.ERROR_REQUEST_NOT_SUPPORTED__MESSAGE.fill("Algorithm not supported.")),
			_reports.get(1));
		assertFalse("Fresh options after a failure.", options.equals(control.getOptions()));
		assertNull("A failure is a message, not a tooltip.", control.getTooltip());
	}

	/** The message for each error name of the browser and the client. */
	public void testHints() throws Exception {
		PasskeyAuthentication ceremony = authentication();

		assertHint(I18NConstants.ERROR_CANCELLED, ceremony, PasskeyCeremony.NOT_ALLOWED_ERROR, "Cancelled.");
		assertHint(I18NConstants.ERROR_CANCELLED, ceremony, PasskeyCeremony.ABORT_ERROR, "");
		assertHint(I18NConstants.ERROR_INSECURE_CONTEXT, ceremony, ReactWebAuthnControl.INSECURE_CONTEXT_ERROR, "http://host");
		assertHint(I18NConstants.ERROR_NOT_SUPPORTED, ceremony, ReactWebAuthnControl.WEB_AUTHN_UNAVAILABLE_ERROR, "");
		assertHint(I18NConstants.ERROR_REQUEST_NOT_SUPPORTED__MESSAGE.fill("No algorithm."), ceremony,
			PasskeyCeremony.NOT_SUPPORTED_ERROR, "No algorithm.");
		assertHint(I18NConstants.ERROR_REQUEST_NOT_SUPPORTED__MESSAGE.fill(PasskeyCeremony.NOT_SUPPORTED_ERROR), ceremony,
			PasskeyCeremony.NOT_SUPPORTED_ERROR, "");
		assertHint(I18NConstants.ERROR_ALREADY_ON_AUTHENTICATOR, ceremony, PasskeyCeremony.INVALID_STATE_ERROR, "");
		assertHint(I18NConstants.ERROR_WRONG_DOMAIN, ceremony, PasskeyCeremony.SECURITY_ERROR, "");
		assertHint(I18NConstants.ERROR_BROWSER__NAME_MESSAGE.fill("UnknownError", "Boom."), ceremony, "UnknownError", "Boom.");
		assertHint(I18NConstants.ERROR_BROWSER__NAME.fill("UnknownError"), ceremony, "UnknownError", "");
	}

	private static void assertHint(ResKey expected, PasskeyCeremony<?, ?> ceremony, String errorName, String message) {
		assertEquals(errorName, text(expected), text(ceremony.hint(errorName, message)));
	}

	/** A page that cannot run a ceremony explains the disabled button in its tooltip, without a message. */
	public void testUnavailable() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyInsecure");

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);

		ceremony.onUnavailable(_context, ReactWebAuthnControl.INSECURE_CONTEXT_ERROR, "");

		assertEquals(text(I18NConstants.ERROR_INSECURE_CONTEXT), control.getTooltip());
		assertEquals(List.of(), _reports);

		ceremony.withdraw(I18NConstants.ERROR_NOT_CURRENT_USER);
		assertEquals("The page stays the more fundamental reason.", text(I18NConstants.ERROR_INSECURE_CONTEXT),
			control.getTooltip());
		ceremony.offer(account);
		assertEquals(text(I18NConstants.ERROR_INSECURE_CONTEXT), control.getTooltip());
	}

	/** A registration stores a passkey for the offered account and hands it to the success handler. */
	public void testRegistration() throws Exception {
		Person account = account("ceremonyRegister");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(PasskeyTestHost.ORIGIN, 0);

		PasskeyRegistration ceremony = registration();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Create", false);
		ceremony.offer(account);
		String options = control.getOptions();

		ceremony.onCredential(_context, authenticator.create(options));

		assertEquals(1, _successes.size());
		Passkey passkey = (Passkey) _successes.get(0);
		assertEquals(account, PasskeyCredentialRepository.getOwner(passkey));
		assertEquals(LABEL, passkey.getLabel());
		assertEquals(List.of(), _reports);
		assertEquals("A stored passkey is not forgotten.", List.of(), _queue.signals());
		assertTrue("The next registration excludes the new passkey.",
			control.getOptions().contains(authenticator.getCredentialId()));

		SoftwareAuthenticator second = new SoftwareAuthenticator(PasskeyTestHost.ORIGIN, 0);
		ceremony.onCredential(_context, second.create(options));
		assertEquals("A replayed registration is refused.", 1, _successes.size());
		assertEquals(1, _reports.size());
		assertEquals("The key created for the replayed registration is forgotten.",
			List.of(unknownSignal(second.getCredentialId())), _queue.signals());
	}

	/** A refused registration tells the browser to forget the key the authenticator stored. */
	public void testRefusedRegistrationForgetsKey() throws Exception {
		Person account = account("ceremonyRefusedReg");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator("http://evil.example:8080", 0);

		PasskeyRegistration ceremony = registration();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Create", false);
		ceremony.offer(account);

		ceremony.onCredential(_context, authenticator.create(control.getOptions()));

		assertEquals(List.of(), _successes);
		assertEquals(List.of(text(com.top_logic.security.passkey.I18NConstants.ERROR_WRONG_ORIGIN__ORIGIN
			.fill("http://evil.example:8080"))), _reports);
		assertEquals(List.of(unknownSignal(authenticator.getCredentialId())), _queue.signals());
		assertFalse(PasskeyService.getInstance().isEnrolled(account));
	}

	/** A credential answering a withdrawn registration is forgotten as well. */
	public void testWithdrawnRegistrationForgetsKey() throws Exception {
		Person account = account("ceremonyWithdrawnReg");
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(PasskeyTestHost.ORIGIN, 0);

		PasskeyRegistration ceremony = registration();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Create", false);
		ceremony.offer(account);
		String options = control.getOptions();
		ceremony.withdraw(null);

		ceremony.onCredential(_context, authenticator.create(options));

		assertEquals(List.of(), _successes);
		assertEquals(List.of(unknownSignal(authenticator.getCredentialId())), _queue.signals());
	}

	/** A login with a passkey the application does not know tells the browser to forget it. */
	public void testUnknownPasskeyForgotten() throws Exception {
		Person account = account("ceremonyUnknown");
		SoftwareAuthenticator authenticator = registered(account);
		Passkey passkey = PasskeyService.getInstance().getPasskeys(account).get(0);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Delete passkey"))) {
			passkey.tDelete();
			tx.commit();
		}

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Sign in", false);
		ceremony.offer(null);

		ceremony.onCredential(_context, authenticator.get(control.getOptions()));

		assertEquals(List.of(), _successes);
		assertEquals(List.of(text(com.top_logic.security.passkey.I18NConstants.ERROR_UNKNOWN_PASSKEY)), _reports);
		assertEquals(List.of(unknownSignal(authenticator.getCredentialId())), _queue.signals());
		assertNotNull("Fresh options after a refusal.", control.getOptions());
	}

	/** A successful login lists all passkeys of the account to the browser, before the success handler runs. */
	public void testLoginSignalsAcceptedPasskeys() throws Exception {
		Person account = account("ceremonyAccepted");
		SoftwareAuthenticator authenticator = registered(account);
		SoftwareAuthenticator further = registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Sign in", false);
		ceremony.offer(null);

		ceremony.onCredential(_context, authenticator.get(control.getOptions()));

		assertEquals(List.of(account), _successes);
		assertEquals("The signal is sent before the success handler runs.", List.of(1), _signalsAtSuccess);
		List<List<?>> signals = _queue.signals();
		assertEquals(1, signals.size());
		List<?> signal = signals.get(0);
		assertEquals(ReactWebAuthnControl.SIGNAL_ALL_ACCEPTED_CREDENTIALS, signal.get(0));
		Map<?, ?> arguments = (Map<?, ?>) signal.get(1);
		assertEquals(PasskeyTestHost.HOST, arguments.get(ReactWebAuthnControl.RP_ID));
		assertEquals(PasskeyCredentialRepository.userHandle(account).getBase64Url(),
			arguments.get(ReactWebAuthnControl.USER_ID));
		assertEquals(List.of(authenticator.getCredentialId(), further.getCredentialId()).stream().sorted().toList(),
			((List<?>) arguments.get(ReactWebAuthnControl.ALL_ACCEPTED_CREDENTIAL_IDS)).stream().map(String.class::cast)
				.sorted().toList());
	}

	/** A failed login sends no list of accepted passkeys. */
	public void testFailedLoginSignalsNothing() throws Exception {
		Person account = account("ceremonyNoSignal");
		SoftwareAuthenticator authenticator = registered(account);
		authenticator.setUserVerification(false);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Sign in", false);
		ceremony.offer(null);

		ceremony.onCredential(_context, authenticator.get(control.getOptions()));

		assertEquals(List.of(), _successes);
		assertEquals(List.of(text(com.top_logic.security.passkey.I18NConstants.ERROR_USER_NOT_VERIFIED)), _reports);
		assertEquals(List.of(), _queue.signals());
	}

	/** On a page whose origin the relying party does not accept, no ceremony is offered. */
	public void testPageOriginMismatch() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyOrigin");
		SoftwareAuthenticator authenticator = registered(account);
		String pageOrigin = "http://192.168.1.10:8080";

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.offer(account);
		String options = control.getOptions();
		assertNotNull("Options may be offered before the origin is reported.", options);

		ceremony.onPageOrigin(_context, pageOrigin);

		String reason = text(I18NConstants.ERROR_ADDRESS_NOT_SET_UP__ORIGIN_ACCEPTED.fill(pageOrigin,
			PasskeyTestHost.ORIGIN));
		assertNull("The options given before are withdrawn.", control.getOptions());
		assertWarned(pageOrigin, PasskeyTestHost.ORIGIN, PasskeyService.HOST_VARIABLE, PasskeyService.Config.ORIGINS);
		assertEquals(reason, control.getTooltip());
		assertEquals(List.of(), _reports);

		ceremony.offer(account);
		assertNull("A later offer gives no options either.", control.getOptions());
		assertEquals(reason, control.getTooltip());
		ceremony.onExpired(_context);
		assertNull(control.getOptions());
		ceremony.onFailure(_context, PasskeyCeremony.NOT_ALLOWED_ERROR, "");
		assertNull(control.getOptions());
		assertEquals(reason, control.getTooltip());

		ceremony.onCredential(_context, authenticator.get(options));
		assertEquals("A credential for the withdrawn options is refused.", List.of(), _successes);
	}

	/** On a page whose origin the relying party accepts, the ceremony stays offered. */
	public void testPageOriginAccepted() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyOriginOk");
		SoftwareAuthenticator authenticator = registered(account);

		PasskeyAuthentication ceremony = authentication();
		ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
		ceremony.onPageOrigin(_context, PasskeyTestHost.ORIGIN + "/");
		ceremony.offer(account);

		assertNotNull(control.getOptions());
		assertNull(control.getTooltip());
		ceremony.onCredential(_context, authenticator.get(control.getOptions()));
		assertEquals(List.of(account), _successes);
	}

	/** Without a public address, no ceremony starts and the button says why. */
	public void testNotSetUp() throws Exception {
		Person account = account("ceremonyNotSetUp");
		try {
			PasskeyTestHost.use("");
			PasskeyAuthentication ceremony = authentication();
			ReactWebAuthnControl control = ceremony.createControl(_context, "Confirm", false);
			ceremony.offer(account);
			ceremony.onPageOrigin(_context, PasskeyTestHost.ORIGIN);

			assertNull(control.getOptions());
			assertEquals(text(com.top_logic.security.passkey.I18NConstants.ERROR_NOT_SET_UP), control.getTooltip());
		} finally {
			PasskeyTestHost.useDefault();
		}
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

	private static List<?> unknownSignal(String credentialId) {
		return List.of(ReactWebAuthnControl.SIGNAL_UNKNOWN_CREDENTIAL,
			Map.of(ReactWebAuthnControl.RP_ID, PasskeyTestHost.HOST, ReactWebAuthnControl.CREDENTIAL_ID, credentialId));
	}

	private PasskeyRegistration registration() {
		return new PasskeyRegistration((context, passkey) -> _successes.add(passkey)) {
			@Override
			protected String newLabel() {
				return LABEL;
			}

			@Override
			protected void report(ReactContext context, ResKey message) {
				_reports.add(text(message));
			}
		};
	}

	/** The passkey second factor is set up with a passkey and allowed unless MFA is disabled. */
	public void testSecondFactor() throws Exception {
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		PasskeySecondFactor method = secondFactor();

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

	/** Passkeys that log in by themselves are no second factor, neither set up nor to set up. */
	public void testNoSecondFactorForLogin() throws Exception {
		PasskeySecondFactor method = secondFactor();
		Person account = account("ceremonyNoFactor");
		assertFalse(method.canEnroll(account));

		registered(account);
		assertTrue(PasskeyService.getInstance().isEnrolled(account));
		assertFalse(method.isEnrolled(account));
	}

	/**
	 * The button for a login with a passkey alone offers a ceremony for the usage
	 * {@link PasskeyUsage#LOGIN} and is hidden for {@link PasskeyUsage#SECOND_FACTOR}.
	 */
	public void testPasswordlessButtonFollowsUsage() throws Exception {
		ReactWebAuthnControl control = passwordlessButton();
		assertFalse(control.isHidden());
		assertNotNull(control.getOptions());

		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		ReactWebAuthnControl hidden = passwordlessButton();
		assertTrue(hidden.isHidden());
		assertNull(hidden.getOptions());
		assertEquals(text(com.top_logic.security.passkey.I18NConstants.ERROR_NO_PASSWORDLESS_LOGIN),
			hidden.getTooltip());
	}

	private ReactWebAuthnControl passwordlessButton() {
		PasskeyLoginElement.Config config = TypedConfiguration.newConfigItem(PasskeyLoginElement.Config.class);
		config.update(config.descriptor().getProperty(PasskeyLoginElement.Config.LABEL), ResKey.text("Sign in"));
		PasskeyLoginElement element =
			(PasskeyLoginElement) new DefaultInstantiationContext(TestPasskeyCeremony.class).getInstance(config);
		return (ReactWebAuthnControl) element.createControl(new DefaultViewContext(_context));
	}

	private static PasskeySecondFactor secondFactor() {
		PasskeySecondFactor.Config<?> config = TypedConfiguration.newConfigItem(PasskeySecondFactor.Config.class);
		config.setName("passkey");
		config.update(config.descriptor().getProperty(PasskeySecondFactor.Config.LABEL), ResKey.text("Passkey"));
		return (PasskeySecondFactor) new DefaultInstantiationContext(TestPasskeyCeremony.class).getInstance(config);
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
		PasskeyTestHost.use(PasskeyUsage.SECOND_FACTOR);
		Person account = account("ceremonyEnrolled");
		registered(account);

		assertNotNull("An account with a second factor confirms itself with it.",
			PasskeyRegistrationElement.refusal(account, true, null));
	}

	private PasskeyAuthentication authentication() {
		return new PasskeyAuthentication((context, account) -> {
			assertSame(_context, context);
			_successes.add(account);
			try {
				_signalsAtSuccess.add(_queue.signals().size());
			} catch (JSON.ParseException ex) {
				throw new AssertionError(ex);
			}
		}) {
			@Override
			protected void report(ReactContext context, ResKey message) {
				assertSame(_context, context);
				_reports.add(text(message));
			}
		};
	}

	private SoftwareAuthenticator registered(Person account) throws Exception {
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(PasskeyTestHost.ORIGIN, 0);
		PasskeyService service = PasskeyService.getInstance();
		PendingRegistration registration = service.startRegistration(account);
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
		PasskeyTestHost.init();
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPasskeyCeremony.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}
