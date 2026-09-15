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
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
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
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.login.VerifyPasswordAction;

/**
 * Tests {@link VerifyPasswordAction}, the guard that lets a command chain continue only after the
 * user has re-entered their password.
 *
 * <p>
 * What is exercised here is the guard's control over the chain: where it may not ask it aborts,
 * where it asks it waits, and the answer it gets decides whether the chain continues or unwinds.
 * The prompt itself is replaced by a {@link Prompt} standing in for the user, so that the dialog's
 * own behaviour - the wrong password drawn under the emptied input, Enter confirming - stays where
 * only a browser can judge it.
 * </p>
 */
public class TestVerifyPasswordAction extends TestCase {

	/** What an action after the guard saw, one entry per run. */
	private final List<Object> _downstream = new ArrayList<>();

	/** What the chain settled with, one entry per run. */
	private final List<Object> _completions = new ArrayList<>();

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
		VerifyPasswordAction guard = newAction();

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
				""".formatted(VerifyPasswordAction.Config.TAG_NAME);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVerifyPasswordAction.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(view, "test-verify-password.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) config.getContent();
		GenericViewCommand.Config command = (GenericViewCommand.Config) panel.getCommands().get(0);
		assertTrue("The tag names the guard.", command.getActions().get(0) instanceof VerifyPasswordAction.Config);
		assertNull("Without a configured title the guard falls back to its own.",
			((VerifyPasswordAction.Config) command.getActions().get(0)).getTitle());

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
	private static ReactContext dialogContext() {
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
	private static VerifyPasswordAction newAction() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVerifyPasswordAction.class);
		return new VerifyPasswordAction(context,
			TypedConfiguration.newConfigItem(VerifyPasswordAction.Config.class));
	}

	/**
	 * A {@link VerifyPasswordAction} with a verifier of the test's choosing and a {@link Prompt}
	 * in place of the dialog, so that the test can answer as the user would.
	 */
	private static class Fixture extends VerifyPasswordAction {

		private final Predicate<char[]> _verifier;

		private Prompt _prompt;

		Fixture(Predicate<char[]> verifier) {
			super(new DefaultInstantiationContext(TestVerifyPasswordAction.class),
				TypedConfiguration.newConfigItem(VerifyPasswordAction.Config.class));
			_verifier = verifier;
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
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestVerifyPasswordAction.class, TypeIndex.Module.INSTANCE);
	}
}
