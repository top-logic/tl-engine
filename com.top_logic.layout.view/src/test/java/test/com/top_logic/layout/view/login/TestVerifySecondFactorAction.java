/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
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
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.PasswordPromptDialogControl.Verifier;
import com.top_logic.layout.view.login.VerifySecondFactorAction;

/**
 * Tests {@link VerifySecondFactorAction}, the guard that lets a command chain continue only after
 * the user has entered a current one-time code of their account's second factor.
 *
 * <p>
 * What is exercised here is the guard's control over the chain: where it may not ask it aborts,
 * where there is nothing to ask it passes, where it asks it waits, and the code it gets decides
 * whether the chain continues or unwinds. The prompt is replaced by a {@link Prompt} standing in for
 * the user, and the TOTP check by a fixed set of codes the {@link Fixture} accepts, so that neither
 * the clock nor the encryption of the stored secret decide the outcome.
 * </p>
 */
public class TestVerifySecondFactorAction extends TestCase {

	/** The stored secret of the {@link Account}, as it is kept encrypted on the account. */
	private static final Password SECRET = new Password("test-verify-second-factor-secret");

	/** The code the {@link Fixture} accepts as current for {@link #SECRET}. */
	private static final String VALID_CODE = "123456";

	/** What an action after the guard saw, one entry per run. */
	private final List<Object> _downstream = new ArrayList<>();

	/** What the chain settled with, one entry per run. */
	private final List<Object> _completions = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		// Each test starts without the failures an earlier one has recorded.
		LoginFailuresModule.getInstance().notifyLoginSuccessed(LoginFailuresModule.secretKey(SECRET));
		super.tearDown();
	}

	/**
	 * Tests that a session without an account - here the account-less context of a test, as a
	 * session-less or anonymous request has - aborts the chain.
	 */
	public void testNoAccountAborts() {
		// The production action, over a context that has no session account.
		VerifySecondFactorAction guard = newAction();

		run(guard, "in");

		assertTrue("Nothing downstream of a guard without an account runs.", _downstream.isEmpty());
		assertEquals("An aborted chain settles without a value.", Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a context without a dialog manager - a chain running headless - aborts instead of
	 * letting an unasked guard pass.
	 */
	public void testHeadlessAborts() {
		Fixture guard = new Fixture(new Account(SECRET));

		run(guard, "in", headlessContext());

		assertTrue("Nothing downstream of an unasked guard runs.", _downstream.isEmpty());
		assertEquals(Collections.singletonList(null), _completions);
		assertFalse("A guard that cannot ask does not open a prompt.", guard.asked());
	}

	/**
	 * Tests that an account without a stored secret passes the chain on at once, without asking.
	 */
	public void testNoSecretPassesThrough() {
		Fixture guard = new Fixture(new Account(null));

		run(guard, "in");

		assertFalse("Without a second factor there is no code to ask for.", guard.asked());
		assertEquals("The guard hands its input on unchanged.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that the guard suspends the chain while the prompt is open, and that a valid code
	 * continues it with the input unchanged.
	 */
	public void testValidCodeResumesChain() {
		Fixture guard = new Fixture(new Account(SECRET));

		run(guard, "in");

		assertTrue("The guard asks for a code.", guard.asked());
		assertTrue("The chain waits for the answer.", _downstream.isEmpty());
		assertEquals(List.of(), _completions);

		assertTrue(guard.enter(VALID_CODE));

		assertEquals("The code is checked against the stored secret.", List.of(SECRET), guard.checkedSecrets());
		assertEquals("The guard hands its input on unchanged.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that a wrong code neither continues nor ends the chain, says so under the input and is
	 * counted for the secret: the user stays in the prompt and may try again.
	 */
	public void testWrongCodeHoldsChain() {
		Fixture guard = new Fixture(new Account(SECRET));

		run(guard, "in");

		assertFalse("A wrong code is rejected.", guard.enter("654321"));
		assertEquals("The user reads that the code was wrong.", I18NConstants.MFA_INVALID_CODE, guard.rejection());
		assertNotNull("The wrong code is counted for the secret.",
			LoginFailuresModule.getInstance().getFailureFor(LoginFailuresModule.secretKey(SECRET)));
		assertTrue("A wrong code runs nothing downstream.", _downstream.isEmpty());
		assertEquals("A wrong code neither completes nor aborts the chain.", List.of(), _completions);

		assertTrue("The next attempt is accepted.", guard.enter(VALID_CODE));
		assertEquals(List.of("in"), _downstream);
		assertNull("The accepted code clears the count.",
			LoginFailuresModule.getInstance().getFailureFor(LoginFailuresModule.secretKey(SECRET)));
	}

	/**
	 * Tests that an empty entry is rejected without being checked or counted.
	 */
	public void testEmptyCodeNotChecked() {
		Fixture guard = new Fixture(new Account(SECRET));

		run(guard, "in");

		assertFalse(guard.enter(""));
		assertEquals(I18NConstants.MFA_INVALID_CODE, guard.rejection());
		assertEquals("An empty entry is not checked.", List.of(), guard.checkedSecrets());
		assertNull("An empty entry is not counted.",
			LoginFailuresModule.getInstance().getFailureFor(LoginFailuresModule.secretKey(SECRET)));
	}

	/**
	 * Tests that a secret backing off after repeated wrong codes refuses even the valid code without
	 * checking it, and that the user reads how long to wait.
	 */
	public void testBackingOffDoesNotCheck() {
		Fixture guard = new Fixture(new Account(SECRET));
		String key = LoginFailuresModule.secretKey(SECRET);

		run(guard, "in");

		while (LoginFailuresModule.getInstance().getFailureFor(key) == null
			|| LoginFailuresModule.getInstance().getFailureFor(key).allowRetry()) {
			assertFalse(guard.enter("000000"));
			assertTrue("The secret backs off after a few wrong codes.", guard.checkedSecrets().size() < 10);
		}
		int checkedBeforeBackOff = guard.checkedSecrets().size();

		assertFalse("Even the valid code is refused while backing off.", guard.enter(VALID_CODE));
		assertEquals("The code is not checked while backing off.", checkedBeforeBackOff,
			guard.checkedSecrets().size());
		assertEquals("The user reads that there were too many attempts.",
			com.top_logic.layout.component.configuration.I18NConstants.CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT.getKey(),
			guard.rejection().getKey());
		assertTrue(_downstream.isEmpty());
		assertTrue(_completions.isEmpty());
	}

	/**
	 * Tests that leaving the prompt without a valid code aborts the chain.
	 */
	public void testCancelAborts() {
		Fixture guard = new Fixture(new Account(SECRET));

		run(guard, "in");
		guard.cancel();

		assertTrue("Nothing downstream of a cancelled guard runs.", _downstream.isEmpty());
		assertEquals(Collections.singletonList(null), _completions);
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
							<generic-command name="rekey">
								<%s/>
							</generic-command>
						</commands>
					</panel>
				</view>
				""".formatted(VerifySecondFactorAction.Config.TAG_NAME);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVerifySecondFactorAction.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(view, "test-verify-second-factor.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) config.getContent();
		GenericViewCommand.Config command = (GenericViewCommand.Config) panel.getCommands().get(0);
		assertTrue("The tag names the guard.",
			command.getActions().get(0) instanceof VerifySecondFactorAction.Config);

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
		}), ActionArguments.of("input", input), _completions::add);
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

	/** The production action, with nothing replaced. */
	private static VerifySecondFactorAction newAction() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVerifySecondFactorAction.class);
		return new VerifySecondFactorAction(context,
			TypedConfiguration.newConfigItem(VerifySecondFactorAction.Config.class));
	}

	/**
	 * A {@link VerifySecondFactorAction} whose session belongs to a given account, accepting
	 * {@link #VALID_CODE} as the current code of {@link #SECRET}, and with a {@link Prompt} in place
	 * of the dialog, so that the test can answer as the user would.
	 */
	private static class Fixture extends VerifySecondFactorAction {

		private final Person _account;

		private final List<Password> _checkedSecrets = new ArrayList<>();

		private Prompt _prompt;

		Fixture(Person account) {
			super(new DefaultInstantiationContext(TestVerifySecondFactorAction.class),
				TypedConfiguration.newConfigItem(VerifySecondFactorAction.Config.class));
			_account = account;
		}

		@Override
		protected Person sessionAccount() {
			return _account;
		}

		@Override
		protected boolean isValidCode(Password secret, String code) {
			_checkedSecrets.add(secret);
			return secret == SECRET && VALID_CODE.equals(code);
		}

		@Override
		protected void openPrompt(ReactContext context, DialogManager dialogManager, ResKey title, ResKey message,
				Verifier verifier, Runnable onVerified, Runnable onCancel) {
			assertEquals("Without a configured title the guard uses its own.",
				I18NConstants.VERIFY_SECOND_FACTOR_TITLE, title);
			_prompt = new Prompt(verifier, onVerified, onCancel);
		}

		/** Whether the guard got as far as asking for a code. */
		boolean asked() {
			return _prompt != null;
		}

		/** The secrets the entered codes were checked against, one entry per check. */
		List<Password> checkedSecrets() {
			return _checkedSecrets;
		}

		/** Enters the given code, as the user confirming the prompt does. */
		boolean enter(String code) {
			return _prompt.enter(code);
		}

		/** What the prompt shows under the input after the last rejected code. */
		ResKey rejection() {
			return _prompt.rejection();
		}

		/** Leaves the prompt without a valid code. */
		void cancel() {
			_prompt.cancel();
		}
	}

	/**
	 * Stands in for the code dialog: it applies the verifier to what is entered, keeps the reason of
	 * a rejection as the dialog shows it, and reports only an accepted code, exactly as the dialog
	 * does.
	 */
	private static class Prompt {

		private final Verifier _verifier;

		private final Runnable _onVerified;

		private final Runnable _onCancel;

		private ResKey _rejection;

		Prompt(Verifier verifier, Runnable onVerified, Runnable onCancel) {
			_verifier = verifier;
			_onVerified = onVerified;
			_onCancel = onCancel;
		}

		boolean enter(String code) {
			_rejection = _verifier.verify(code.toCharArray());
			if (_rejection != null) {
				return false;
			}
			_onVerified.run();
			return true;
		}

		/** The reason of the last rejection, or {@code null} if the last code was accepted. */
		ResKey rejection() {
			return _rejection;
		}

		void cancel() {
			_onCancel.run();
		}
	}

	/** An account answering the given stored secret, without a knowledge base behind it. */
	private static class Account extends Person {

		private final Password _secret;

		/**
		 * Creates an {@link Account}.
		 *
		 * @param secret
		 *        The stored second-factor secret, {@code null} for an account without one.
		 */
		Account(Password secret) {
			super(null);
			_secret = secret;
		}

		@Override
		public Password getMFASecret() {
			return _secret;
		}

		@Override
		public String getName() {
			return "test";
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, the resources the messages are resolved
	 * from, and the {@link LoginFailuresModule} counting the entered codes.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestVerifySecondFactorAction.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				LoginFailuresModule.Module.INSTANCE));
	}
}
