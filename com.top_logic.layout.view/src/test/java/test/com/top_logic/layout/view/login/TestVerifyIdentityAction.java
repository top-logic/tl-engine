/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.login.VerifyIdentityAction;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link VerifyIdentityAction}, the guard that lets a command chain continue only after the
 * user has proven again that they are the account holder.
 *
 * <p>
 * What is exercised here is the guard's control over the chain: which way it asks, where it may not
 * ask it aborts, where it asks it waits, and the answer it gets decides whether the chain continues
 * or unwinds. The prompts themselves are replaced by a {@link Prompt} and a {@link WaitingPrompt}
 * standing in for the user, so that the dialogs' own behaviour - the wrong password drawn under the
 * emptied input, the sign-in window opening from the click - stays where only a browser can judge
 * it.
 * </p>
 */
public class TestVerifyIdentityAction extends TestCase {

	/** What the action after the guard fails with. */
	private static final String FAILURE_MESSAGE = "The password of this account is kept elsewhere.";

	/** What an action after the guard saw, one entry per run. */
	private final List<Object> _downstream = new ArrayList<>();

	/** What the chain settled with, one entry per run. */
	private final List<Object> _completions = new ArrayList<>();

	/** What was shown to the user of the window the chain belongs to. */
	private final List<HTMLFragment> _shown = new ArrayList<>();

	/**
	 * Tests that a context without a dialog manager - a chain running headless - aborts instead of
	 * letting an unasked guard pass.
	 */
	public void testHeadlessAborts() {
		Fixture guard = new Fixture(password -> true);

		run(guard, "in", headlessContext());

		assertTrue("Nothing downstream of an unasked guard runs.", _downstream.isEmpty());
		assertEquals("An aborted chain settles without a value.", Collections.singletonList(null), _completions);
		assertFalse("A guard that cannot ask does not open a prompt.", guard.asked());
	}

	/**
	 * Tests that an account whose password cannot be checked - here the account-less context of a
	 * test, as a session-less or anonymous request has - aborts the chain.
	 */
	public void testUnverifiableAccountAborts() {
		// The production verifier, over a context that has no session account.
		VerifyIdentityAction guard = newAction();

		run(guard, "in");

		assertTrue("Nothing downstream of a guard that cannot verify runs.", _downstream.isEmpty());
		assertEquals(Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that the guard suspends the chain while the prompt is open, and that a password the
	 * verifier accepts continues it with the input unchanged.
	 */
	public void testAcceptedPasswordResumesChain() {
		Fixture guard = new Fixture(password -> "secret".equals(new String(password)));

		run(guard, "in");

		assertTrue("The chain waits for the answer.", _downstream.isEmpty());
		assertEquals(List.of(), _completions);

		assertTrue(guard.enter("secret"));

		assertEquals("The guard hands its input on unchanged.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that a password the verifier rejects neither continues nor ends the chain: the user
	 * stays in the prompt and may try again.
	 */
	public void testRejectedPasswordHoldsChain() {
		Fixture guard = new Fixture(password -> "secret".equals(new String(password)));

		run(guard, "in");

		assertFalse("The verifier rejects the wrong password.", guard.enter("wrong"));

		assertTrue("A rejected password runs nothing downstream.", _downstream.isEmpty());
		assertEquals("A rejected password neither completes nor aborts the chain.", List.of(), _completions);

		assertTrue("The next attempt is accepted.", guard.enter("secret"));
		assertEquals(List.of("in"), _downstream);
	}

	/**
	 * Tests that leaving the prompt without an accepted password aborts the chain.
	 */
	public void testCancelAborts() {
		Fixture guard = new Fixture(password -> true);

		run(guard, "in");
		guard.cancel();

		assertTrue("Nothing downstream of a cancelled guard runs.", _downstream.isEmpty());
		assertEquals(Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a session an identity provider can authenticate again is sent there: the guard
	 * announces the awaited confirmation, opens the waiting prompt on the URL that provider
	 * answered, and holds the chain until the confirmation arrives.
	 */
	public void testExternalConfirmationResumesChain() {
		Fixture guard = externalGuard();

		run(guard, "in");

		assertFalse("A session confirmed elsewhere is not asked for its password.", guard.asked());
		assertEquals("The provider is sent the token of the awaited confirmation.",
			"https://provider.example/auth?verification=" + Fixture.TOKEN, guard.waiting().url());
		assertTrue("The chain waits for the confirmation.", _downstream.isEmpty());
		assertEquals(List.of(), _completions);

		// What the authentication servlet does once the provider has confirmed the account.
		guard.confirm();

		assertTrue("The prompt closes itself once the confirmation has arrived.", guard.waiting().closed());
		assertEquals("The guard hands its input on unchanged.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
		assertEquals("A confirmation that arrived withdraws nothing.", List.of(), guard.withdrawn());
	}

	/**
	 * Tests that leaving the waiting prompt aborts the chain and withdraws the confirmation the
	 * guard no longer waits for.
	 */
	public void testExternalCancelAborts() {
		Fixture guard = externalGuard();

		run(guard, "in");
		guard.waiting().cancel();

		assertEquals("The announcement is withdrawn.", List.of(Fixture.TOKEN), guard.withdrawn());
		assertTrue("Nothing downstream of a cancelled guard runs.", _downstream.isEmpty());
		assertEquals(Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a user-level failure of the resumed chain - a password the account's device refuses
	 * to change - is shown in the window the chain belongs to, and that the request bringing the
	 * confirmation is left with nothing to answer for.
	 */
	public void testFailingChainReportsUserError() throws IOException {
		Fixture guard = externalGuard();
		runFailing(guard, new TopLogicException(ResKey.text(FAILURE_MESSAGE)));

		// Returns normally: what the chain does with the confirmed identity is not the outcome of
		// the confirmation.
		guard.confirm();

		assertTrue("The prompt is closed before the chain is taken up again.", guard.waiting().closed());
		assertEquals("The failure is reported to the window the chain belongs to.", 1, _shown.size());
		assertEquals("The user reads what the chain refused.",
			FAILURE_MESSAGE, message(_shown.get(0)));
		assertEquals("A chain that failed settles with nothing.", List.of(), _completions);
	}

	/**
	 * Tests that a failure nobody expected is reported the same way, rather than escaping into the
	 * request that carried the confirmation.
	 */
	public void testFailingChainReportsUnexpectedError() throws IOException {
		Fixture guard = externalGuard();
		runFailing(guard, new IllegalStateException(FAILURE_MESSAGE));

		guard.confirm();

		assertEquals("The failure is reported to the window the chain belongs to.", 1, _shown.size());
		assertEquals("The message of an unexpected failure is what the user is left with.",
			FAILURE_MESSAGE, message(_shown.get(0)));
	}

	/**
	 * Tests that a chain running through after the confirmation says nothing to the user.
	 */
	public void testSucceedingChainReportsNothing() {
		Fixture guard = externalGuard();

		run(guard, "in", dialogContext(sink()));
		guard.confirm();

		assertEquals("The guard hands its input on unchanged.", List.of("in"), _downstream);
		assertTrue("A chain that ran through reports nothing.", _shown.isEmpty());
	}

	/**
	 * Tests that a session no identity provider answers for is asked for its password, and that the
	 * announcement made while asking around is withdrawn again.
	 */
	public void testWithoutProviderAsksForPassword() {
		Fixture guard = new Fixture(password -> "secret".equals(new String(password)));
		guard.offer(new FakeLoginMethod("keycloak", null));

		run(guard, "in");

		assertNull("No provider answers, so no window is waited for.", guard.waitingOrNull());
		assertEquals("The announcement nobody answers is withdrawn.", List.of(Fixture.TOKEN), guard.withdrawn());
		assertTrue("The password is asked for.", guard.asked());

		assertTrue(guard.enter("secret"));
		assertEquals(List.of("in"), _downstream);
	}

	/**
	 * Tests that the guard's tag is what a command chain writes it as, and that the chain it is
	 * written into instantiates.
	 */
	public void testConfiguration() throws Exception {
		String view = """
				<view>
					<panel>
						<commands>
							<generic-command name="change">
								<%s/>
							</generic-command>
						</commands>
					</panel>
				</view>
				""".formatted(VerifyIdentityAction.Config.TAG_NAME);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVerifyIdentityAction.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(view, "test-verify-identity.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) config.getContent();
		GenericViewCommand.Config command = (GenericViewCommand.Config) panel.getCommands().get(0);
		assertTrue("The tag names the guard.", command.getActions().get(0) instanceof VerifyIdentityAction.Config);
		assertNull("Without a configured title the guard falls back to its own.",
			((VerifyIdentityAction.Config) command.getActions().get(0)).getTitle());

		assertTrue("The chain instantiates.", context.getInstance(command) instanceof GenericViewCommand);
		context.checkErrors();
	}

	private void run(ViewAction guard, Object input) {
		run(guard, input, dialogContext());
	}

	private void run(ViewAction guard, Object input, ReactContext context) {
		ViewActionChain.run(context, List.of(guard, (ctx, value) -> {
			_downstream.add(value);
			return value;
		}), input, _completions::add);
	}

	/**
	 * Runs a chain whose action after the guard throws the given failure, in a window that records
	 * what is shown in it.
	 */
	private void runFailing(ViewAction guard, RuntimeException failure) {
		ViewActionChain.run(dialogContext(sink()), List.of(guard, (ctx, value) -> {
			throw failure;
		}), "in", _completions::add);
	}

	/** A guard whose session an identity provider answers for. */
	private static Fixture externalGuard() {
		Fixture guard = new Fixture(password -> {
			throw new AssertionError("The password path is not taken.");
		});
		guard.offer(new FakeLoginMethod("keycloak", "https://provider.example/auth?verification="));
		return guard;
	}

	/** A sink collecting what is shown in the window. */
	private ErrorSink sink() {
		return new ErrorSink() {
			@Override
			public void showError(HTMLFragment content) {
				_shown.add(content);
			}

			@Override
			public void showWarning(HTMLFragment content) {
				_shown.add(content);
			}

			@Override
			public void showInfo(HTMLFragment content) {
				_shown.add(content);
			}
		};
	}

	/** The text a message shown to the user reads, with the markup around it stripped. */
	private static String message(HTMLFragment shown) throws IOException {
		TagWriter out = new TagWriter();
		shown.write(new DummyDisplayContext(), out);
		return out.toString().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	}

	/**
	 * A context of a display that reaches no browser: without an update queue it has no
	 * {@link DialogManager}, which is what a chain running outside a session sees.
	 */
	private static ReactContext headlessContext() {
		return new DefaultViewContext(new DefaultReactContext("", "test", null, new ReactWindowRegistry("test")));
	}

	/**
	 * A context that can open a dialog, so that what stops the chain is the guard's own verdict and
	 * not the absence of a place to ask in.
	 */
	private static ViewContext dialogContext() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		queue.setDialogManager(new DialogManager() {
			@Override
			public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
					DialogResultHandler<Void> handler) {
				throw new UnsupportedOperationException("The prompt is answered through the fixture.");
			}

			@Override
			public void closeTopDialog(DialogResult<Void> result) {
				throw new UnsupportedOperationException("The prompt is answered through the fixture.");
			}

			@Override
			public void closeDialogsAbove(DialogHandle dialog) {
				throw new UnsupportedOperationException("The prompt is answered through the fixture.");
			}
		});
		return new DefaultViewContext(new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test")));
	}

	/** A context reporting what it shows to the given sink. */
	private static ReactContext dialogContext(ErrorSink sink) {
		return dialogContext().withErrorSink(sink);
	}

	/** The production action, with nothing replaced. */
	private static VerifyIdentityAction newAction() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVerifyIdentityAction.class);
		return new VerifyIdentityAction(context,
			TypedConfiguration.newConfigItem(VerifyIdentityAction.Config.class));
	}

	/**
	 * A {@link VerifyIdentityAction} answering for the session's surroundings - which login methods
	 * there are, what the session's account may be confirmed with - and with a {@link Prompt} and a
	 * {@link WaitingPrompt} in place of the dialogs, so that the test can answer as the user would.
	 */
	private static class Fixture extends VerifyIdentityAction {

		/** The token the fixture issues for the confirmation it is asked to await. */
		static final String TOKEN = "verification-token";

		private final Predicate<char[]> _verifier;

		private final List<LoginMethod> _loginMethods = new ArrayList<>();

		private final List<String> _withdrawn = new ArrayList<>();

		private Runnable _onVerified;

		private Prompt _prompt;

		private WaitingPrompt _waiting;

		Fixture(Predicate<char[]> verifier) {
			super(new DefaultInstantiationContext(TestVerifyIdentityAction.class),
				TypedConfiguration.newConfigItem(VerifyIdentityAction.Config.class));
			_verifier = verifier;
		}

		/** Adds a login method the guard finds when it asks around. */
		void offer(LoginMethod method) {
			_loginMethods.add(method);
		}

		@Override
		protected List<? extends LoginMethod> loginMethods() {
			return _loginMethods;
		}

		@Override
		protected String registerVerification(Runnable onVerified) {
			_onVerified = onVerified;
			return TOKEN;
		}

		@Override
		protected void cancelVerification(String token) {
			_withdrawn.add(token);
		}

		@Override
		protected Predicate<char[]> createVerifier() {
			return _verifier;
		}

		@Override
		protected void openPrompt(ReactContext context, DialogManager dialogManager, ResKey title, ResKey message,
				Predicate<char[]> verifier, Runnable onVerified, Runnable onCancel) {
			_prompt = new Prompt(verifier, onVerified, onCancel);
		}

		@Override
		protected Runnable openWaitingPrompt(ReactContext context, DialogManager dialogManager, ResKey title,
				ResKey message, ResKey buttonLabel, String reauthenticationUrl, Runnable onCancel) {
			_waiting = new WaitingPrompt(reauthenticationUrl, onCancel);
			return _waiting::close;
		}

		@Override
		protected void runInWindow(ReactContext context, Runnable action) {
			// The window this would be carried into is the one only a browser has.
			action.run();
		}

		/** Whether the guard got as far as asking for a password. */
		boolean asked() {
			return _prompt != null;
		}

		/** Enters the given password, as the user confirming the prompt does. */
		boolean enter(String password) {
			return _prompt.enter(password);
		}

		/** Leaves the prompt without an accepted password. */
		void cancel() {
			_prompt.cancel();
		}

		/** The prompt waiting for the external confirmation. */
		WaitingPrompt waiting() {
			assertNotNull("The guard waits for an external confirmation.", _waiting);
			return _waiting;
		}

		/** The prompt waiting for the external confirmation, or {@code null} if there is none. */
		WaitingPrompt waitingOrNull() {
			return _waiting;
		}

		/** The tokens whose announcements the guard has withdrawn. */
		List<String> withdrawn() {
			return _withdrawn;
		}

		/**
		 * Reports the awaited confirmation, as the authentication servlet does once the identity
		 * provider has confirmed the expected account.
		 */
		void confirm() {
			assertNotNull("The guard awaits a confirmation.", _onVerified);
			_onVerified.run();
		}
	}

	/**
	 * Stands in for the dialog waiting for an external confirmation: it holds the URL the user is
	 * sent to and reports a cancel exactly as the dialog does, until the arriving confirmation
	 * closes it.
	 */
	private static class WaitingPrompt {

		private final String _url;

		private final Runnable _onCancel;

		private boolean _closed;

		WaitingPrompt(String url, Runnable onCancel) {
			_url = url;
			_onCancel = onCancel;
		}

		/** Where the user signs in again. */
		String url() {
			return _url;
		}

		/** Whether the prompt has been closed by an arriving confirmation. */
		boolean closed() {
			return _closed;
		}

		/** Closes the prompt, as the arriving confirmation does. */
		void close() {
			_closed = true;
		}

		/** Leaves the prompt before the confirmation has arrived. */
		void cancel() {
			if (_closed) {
				fail("A closed prompt reports no cancel.");
			}
			_onCancel.run();
		}
	}

	/**
	 * A login method answering a re-authentication URL of the test's choosing.
	 */
	private static class FakeLoginMethod implements LoginMethod {

		private final String _id;

		private final String _urlPrefix;

		/**
		 * Creates a {@link FakeLoginMethod}.
		 *
		 * @param urlPrefix
		 *        What the token is appended to, or {@code null} for a method that cannot
		 *        authenticate the user of an established session again.
		 */
		FakeLoginMethod(String id, String urlPrefix) {
			_id = id;
			_urlPrefix = urlPrefix;
		}

		@Override
		public String getId() {
			return _id;
		}

		@Override
		public ResKey getLabel() {
			return ResKey.text(_id);
		}

		@Override
		public ThemeImage getIcon() {
			return null;
		}

		@Override
		public String getInitiationUrl(String returnToUrl) {
			return "https://provider.example/login";
		}

		@Override
		public String getReauthenticationUrl(String token) {
			return _urlPrefix == null ? null : _urlPrefix + token;
		}
	}

	/**
	 * Stands in for the password dialog: it applies the verifier to what is entered and reports
	 * only an accepted password, exactly as the dialog does.
	 */
	private static class Prompt {

		private final Predicate<char[]> _verifier;

		private final Runnable _onVerified;

		private final Runnable _onCancel;

		Prompt(Predicate<char[]> verifier, Runnable onVerified, Runnable onCancel) {
			_verifier = verifier;
			_onVerified = onVerified;
			_onCancel = onCancel;
		}

		boolean enter(String password) {
			if (!_verifier.test(password.toCharArray())) {
				return false;
			}
			_onVerified.run();
			return true;
		}

		void cancel() {
			_onCancel.run();
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module and the resources the reported messages are
	 * resolved from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestVerifyIdentityAction.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
