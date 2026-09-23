/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.DummyDisplayContext;
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
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.NotifyAction;
import com.top_logic.layout.view.command.NotifyAction.Display;
import com.top_logic.layout.view.command.NotifyAction.Kind;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.element.PanelElement;

/**
 * Tests {@link NotifyAction}, the action of a command chain that tells the user something.
 *
 * <p>
 * What is exercised here is what the notice does to the chain around it: which way the message
 * reaches the user, whether the chain carries its value on or ends, and that a dialog holds the
 * chain until it is closed. The dialog itself stands in as a fixture, so that what a browser shows
 * stays where only a browser can judge it.
 * </p>
 */
public class TestNotifyAction extends TestCase {

	/** What an action after the notice saw, one entry per run. */
	private final List<Object> _downstream = new ArrayList<>();

	/** What the chain settled with, one entry per run. */
	private final List<Object> _completions = new ArrayList<>();

	/** What was shown to the user of the window the chain belongs to, marked with its severity. */
	private final List<String> _shown = new ArrayList<>();

	/** What the chain did besides the notice, in the order it happened. */
	private final List<String> _log = new ArrayList<>();

	/**
	 * Tests that the message reaches the user marked with the severity it was given, and that the
	 * chain carries its value on unchanged.
	 */
	public void testSeverityMarksTheMessage() {
		assertNotifies(Kind.INFO, "info");
		assertNotifies(Kind.WARNING, "warning");
		assertNotifies(Kind.ERROR, "error");
	}

	private void assertNotifies(Kind kind, String marking) {
		_shown.clear();
		_downstream.clear();
		_completions.clear();

		run(notify(says("Nothing selected."), kind, Display.SNACKBAR, false), "in", windowContext());

		assertEquals(List.of(marking + ": Nothing selected."), _shown);
		assertEquals("The notice hands the chain's value on unchanged.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that a notice told to stop ends the chain after the message: nothing after it runs, and
	 * what ran before it is compensated.
	 */
	public void testStopEndsChain() {
		ViewAction notify = notify(says("Nothing selected."), Kind.ERROR, Display.SNACKBAR, true);

		ViewActionChain.run(windowContext(), List.of(compensate("before"), notify, downstream()), "in",
			_completions::add);

		assertEquals(List.of("error: Nothing selected."), _shown);
		assertTrue("Nothing after a stopping notice runs.", _downstream.isEmpty());
		assertEquals("An ended chain settles without a value.", Collections.singletonList(null), _completions);
		assertEquals("What ran before the notice is compensated.", List.of("before compensated"), _log);
	}

	/**
	 * Tests that a notice with nothing to say - no message, or an empty one - shows nothing and
	 * leaves the chain untouched, even when it would otherwise end it.
	 */
	public void testNothingToSayPassesThrough() {
		run(notify((context, input) -> null, Kind.ERROR, Display.SNACKBAR, true), "in", windowContext());

		assertTrue("Nothing is shown for a message there is none of.", _shown.isEmpty());
		assertEquals("The chain runs on although the notice would have ended it.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);

		_downstream.clear();
		_completions.clear();

		Dialogs dialogs = new Dialogs();
		run(notify(says(""), Kind.ERROR, Display.DIALOG, true), "in", windowContext(dialogs));

		assertNull("An empty message opens no dialog.", dialogs.opened());
		assertTrue(_shown.isEmpty());
		assertEquals(List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that a chain outside a window - one with nowhere to show a message - runs on rather
	 * than failing over what it cannot say.
	 */
	public void testWithoutSomewhereToShowChainRunsOn() {
		run(notify(says("Nothing selected."), Kind.ERROR, Display.SNACKBAR, false), "in", headlessContext());

		assertEquals(List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that a notice shown as a dialog holds the chain until the user has closed it, and that
	 * the chain then carries its value on.
	 */
	public void testDialogHoldsChainUntilClosed() {
		Dialogs dialogs = new Dialogs();

		run(notify(says("Nothing selected."), Kind.ERROR, Display.DIALOG, false), "in", windowContext(dialogs));

		assertNotNull("The user is shown a dialog.", dialogs.opened());
		assertTrue("A dialog is not also said in passing.", _shown.isEmpty());
		assertTrue("The chain waits for the dialog.", _downstream.isEmpty());
		assertEquals(List.of(), _completions);

		dialogs.close();

		assertEquals("The notice hands the chain's value on unchanged.", List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that a dialog told to stop ends the chain once it is closed.
	 */
	public void testDialogStopEndsChainOnClose() {
		Dialogs dialogs = new Dialogs();

		ViewAction notify = notify(says("Nothing selected."), Kind.ERROR, Display.DIALOG, true);
		ViewActionChain.run(windowContext(dialogs), List.of(compensate("before"), notify, downstream()), "in",
			_completions::add);

		assertTrue("The chain waits for the dialog.", _downstream.isEmpty());
		assertEquals(List.of(), _completions);

		dialogs.close();

		assertTrue("Nothing after a stopping notice runs.", _downstream.isEmpty());
		assertEquals(Collections.singletonList(null), _completions);
		assertEquals("What ran before the notice is compensated.", List.of("before compensated"), _log);
	}

	/**
	 * Tests that a notice asking for a dialog where there is none to open - a chain running
	 * headless - is said in passing instead, and the chain runs on without waiting.
	 */
	public void testDialogWithoutSomewhereToOpenIsSaidInPassing() {
		run(notify(says("Nothing selected."), Kind.WARNING, Display.DIALOG, false), "in",
			headlessContext().withErrorSink(sink()));

		assertEquals(List.of("warning: Nothing selected."), _shown);
		assertEquals(List.of("in"), _downstream);
		assertEquals(List.of("in"), _completions);
	}

	/**
	 * Tests that the notice's tag is what a command chain writes it as, that it is read with the
	 * severity and display the configuration names, and what it falls back to when they are not
	 * named.
	 */
	public void testConfiguration() throws Exception {
		NotifyAction.Config plain = (NotifyAction.Config) parseAction("<notify expr=\"x -> $x\"/>");
		assertEquals("A message not called anything else is an information.", Kind.INFO, plain.getKind());
		assertEquals("A notice is said in passing unless a dialog is asked for.", Display.SNACKBAR,
			plain.getDisplay());
		assertFalse("A notice lets the chain run on unless it is told to stop.", plain.isStop());
		assertNull("Without a configured title the dialog falls back to the one of its severity.",
			plain.getTitle());

		NotifyAction.Config configured = (NotifyAction.Config) parseAction(
			"<notify expr=\"x -> $x\" kind=\"warning\" display=\"dialog\" stop=\"true\"/>");
		assertEquals(Kind.WARNING, configured.getKind());
		assertEquals(Display.DIALOG, configured.getDisplay());
		assertTrue(configured.isStop());
	}

	/** The single action of a command chain written as the given XML. */
	private static Object parseAction(String actionXml) throws Exception {
		String view = """
				<view>
					<panel>
						<commands>
							<generic-command name="select">
								%s
							</generic-command>
						</commands>
					</panel>
				</view>
				""".formatted(actionXml);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNotifyAction.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(view, "test-notify.view.xml"));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) config.getContent();
		GenericViewCommand.Config command = (GenericViewCommand.Config) panel.getCommands().get(0);
		return command.getActions().get(0);
	}

	/** A notice with the given message function and settings. */
	private static NotifyAction notify(ActionScript message, Kind kind, Display display, boolean stop) {
		return new NotifyAction(message, kind, display, stop, null);
	}

	/** A message function delivering the given text for every value. */
	private static ActionScript says(String text) {
		return (context, input) -> text;
	}

	private void run(ViewAction notify, Object input, ReactContext context) {
		ViewActionChain.run(context, List.of(notify, downstream()), input, _completions::add);
	}

	/** An action recording the value that reaches it. */
	private ViewAction downstream() {
		return (context, value) -> {
			_downstream.add(value);
			return value;
		};
	}

	/** An action that registers a compensation logging its name. */
	private ViewAction compensate(String name) {
		return new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				continuation.onAbort(() -> _log.add(name + " compensated"));
				continuation.resume(input);
			}
		};
	}

	/** A sink collecting what is shown in the window, marked with the severity it is shown with. */
	private ErrorSink sink() {
		return new ErrorSink() {
			@Override
			public void showError(HTMLFragment content) {
				_shown.add("error: " + message(content));
			}

			@Override
			public void showWarning(HTMLFragment content) {
				_shown.add("warning: " + message(content));
			}

			@Override
			public void showInfo(HTMLFragment content) {
				_shown.add("info: " + message(content));
			}
		};
	}

	/** The text a message shown to the user reads, with the markup around it stripped. */
	private static String message(HTMLFragment shown) {
		TagWriter out = new TagWriter();
		try {
			shown.write(new DummyDisplayContext(), out);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		return out.toString().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	}

	/**
	 * A context of a display that reaches no browser: without an update queue it has no
	 * {@link DialogManager}, which is what a chain running outside a session sees.
	 */
	private static ViewContext headlessContext() {
		return new DefaultViewContext(new DefaultReactContext("", "test", null, new ReactWindowRegistry("test")));
	}

	/** A context of a window that shows what is said in it, but opens no dialog. */
	private ViewContext windowContext() {
		return windowContext(new Dialogs());
	}

	/** A context of a window that shows what is said in it and opens dialogs on the given fixture. */
	private ViewContext windowContext(DialogManager dialogManager) {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		queue.setDialogManager(dialogManager);
		return new DefaultViewContext(new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test")))
			.withErrorSink(sink());
	}

	/**
	 * Stands in for the dialogs of a window: it holds the one that was opened and reports it closed
	 * exactly as the window does, through the handler every way of leaving a dialog passes through.
	 */
	private static class Dialogs implements DialogManager {

		private ReactControl _opened;

		private DialogResultHandler<Void> _handler;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			assertNull("One dialog at a time.", _opened);
			_opened = child;
			_handler = handler;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			assertNotNull("A dialog is open.", _opened);
			DialogResultHandler<Void> handler = _handler;
			_opened = null;
			_handler = null;
			handler.onResult(result);
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing is stacked on the dialog of a notice.");
		}

		/** The dialog the user is shown, or {@code null} if there is none. */
		ReactControl opened() {
			return _opened;
		}

		/** Leaves the dialog, as the user acknowledging it does. */
		void close() {
			closeTopDialog(DialogResult.ok(null));
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, the resources the shown messages are
	 * resolved from, and the {@link ThemeFactory} the dialog's button takes its icon from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestNotifyAction.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}
}
