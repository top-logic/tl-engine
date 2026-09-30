/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.CancelDialogCommand;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.SuspendedCommands;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.model.listen.ModelScope;

/**
 * A dialog stays open while a command it started is still running.
 *
 * <p>
 * The command of a dialog may run past the interaction that started it - a job doing the work in
 * the background, a question waiting to be answered. Leaving the dialog then would take away what
 * shows the work and what could stop it, so the dialog refuses to close and its cancel button says
 * why, until the command has settled - whichever way it did.
 * </p>
 */
public class TestSuspendedCommands extends TestCase {

	/** The dialog view: a window with a cancel button. */
	private static final String VIEW = "suspended-commands.view.xml";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private Dialogs _dialogs;

	private ViewContext _page;

	/** The dialog the commands of these tests run in. */
	private Dialog _dialog;

	/** The context of the dialog's content, which is what its buttons run commands with. */
	private ViewContext _dialogContext;

	/** What an action after the suspended one saw, one entry per run. */
	private final List<Object> _downstream = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-suspended-commands").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		copyFixture(VIEW, new File(views, VIEW));
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		_dialogs = new Dialogs();
		_sseQueue.setDialogManager(_dialogs);
		ReactContext reactContext = new PageContext(_sseQueue);
		_page = new DefaultViewContext(reactContext);

		_dialog = (Dialog) OpenDialogAction.openDialog(_page, ViewLoader.fullPath(VIEW), false, Map.of(), List.of());
		_dialogContext = (ViewContext) _dialogs.opened().getReactContext();
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Tests that the dialog is held while a command of it waits, and released when the command
	 * continues.
	 */
	public void testHeldWhileCommandWaits() {
		Suspend suspend = new Suspend();
		run(suspend, downstream());

		assertFalse("The dialog holds the running command.", _dialog.isClosable());
		assertFalse("Its window shows no way out either.", window().isClosable());
		assertEquals("The chain waits.", List.of(), _downstream);

		suspend.resume("late");

		assertTrue("The command has settled, so the dialog can be left.", _dialog.isClosable());
		assertTrue(window().isClosable());
		assertEquals(List.of("late"), _downstream);
	}

	/**
	 * Tests that a command giving up releases the dialog just as a continuing one does.
	 */
	public void testReleasedOnAbort() {
		Suspend suspend = new Suspend();
		run(suspend, downstream());

		assertFalse(_dialog.isClosable());

		suspend.abort();

		assertTrue("A command that gave up no longer holds the dialog.", _dialog.isClosable());
		assertEquals("Nothing after the aborted action runs.", List.of(), _downstream);
	}

	/**
	 * Tests that a command failing where it was taken up again releases the dialog, which would
	 * otherwise be locked by work that has already ended.
	 */
	public void testReleasedOnFailure() {
		Suspend suspend = new Suspend();
		run(suspend, failing());

		assertFalse(_dialog.isClosable());

		try {
			suspend.resume("late");
			fail("The action following the suspended one throws.");
		} catch (RuntimeException expected) {
			assertEquals("boom", expected.getMessage());
		}

		assertTrue("A failed command no longer holds the dialog.", _dialog.isClosable());
	}

	/**
	 * Tests that a command running through within its interaction leaves the dialog alone - not
	 * even for the moment it runs in.
	 */
	public void testCommandRunningThroughNeverHoldsTheDialog() {
		run(downstream());

		assertEquals(List.of("in"), _downstream);
		assertTrue(_dialog.isClosable());
		assertEquals("The dialog was never told it cannot be closed.", List.of(), _dialog.closableChanges());
	}

	/**
	 * Tests that the dialog waits for every command it holds, not just the last one to settle.
	 */
	public void testHeldUntilEveryCommandSettled() {
		Suspend first = new Suspend();
		Suspend second = new Suspend();
		run(first);
		run(second);

		assertFalse(_dialog.isClosable());

		first.resume("done");

		assertFalse("The other command is still running.", _dialog.isClosable());

		second.resume("done");

		assertTrue("Nothing of the dialog is running any more.", _dialog.isClosable());
		assertEquals("The dialog was held once, and released once.",
			List.of(Boolean.FALSE, Boolean.TRUE), _dialog.closableChanges());
	}

	/**
	 * Tests that the cancel button of the dialog is not offered while a command of it is running,
	 * and says so, rather than being pressed to no effect.
	 */
	public void testCancelIsNotOfferedWhileCommandRuns() {
		CancelDialogCommand.Config config = TypedConfiguration.newConfigItem(CancelDialogCommand.Config.class);
		CancelDialogCommand cancel =
			new CancelDialogCommand(new DefaultInstantiationContext(TestSuspendedCommands.class), config);
		ViewCommandModel model = ViewCommandModel.forCommand(_dialogContext, cancel, config);
		model.attach(null);
		int[] reported = new int[1];
		model.addStateChangeListener(() -> reported[0]++);

		assertTrue("Nothing is running, so the dialog can be left.", model.isExecutable());

		Suspend suspend = new Suspend();
		run(suspend);

		assertFalse("The dialog cannot be left while its command runs.", model.isExecutable());
		assertTrue("The button is shown, giving the reason it cannot be pressed.", model.isVisible());
		assertEquals("The button gives the reason it cannot be pressed.",
			I18NConstants.ERROR_DIALOG_COMMAND_RUNNING, model.getExecutableState().getI18NReasonKey());
		assertEquals("The button was told its state changed.", 1, reported[0]);

		suspend.resume("done");

		assertTrue("The command has settled, so the dialog can be left again.", model.isExecutable());
		assertEquals(2, reported[0]);

		model.detach();
	}

	/**
	 * Tests that a command running outside a region that follows its commands is not tracked,
	 * which is what a command of the page itself does.
	 */
	public void testCommandOutsideADialogRunsUntracked() {
		Suspend suspend = new Suspend();
		new GenericViewCommand(List.of(suspend, downstream())).execute(_page, "in");

		assertNull("The page establishes no region of its own.", _page.getScope(SuspendedCommands.class));
		assertTrue("Nothing marks the dialog that was not asked to run the command.", _dialog.isClosable());

		suspend.resume("late");

		assertEquals(List.of("late"), _downstream);
	}

	/** Runs the given actions as a command of the dialog, starting with {@code "in"}. */
	private void run(ViewAction... actions) {
		new GenericViewCommand(List.of(actions)).execute(_dialogContext, "in");
	}

	/** An action recording the value that reaches it. */
	private ViewAction downstream() {
		return (context, value) -> {
			_downstream.add(value);
			return value;
		};
	}

	/** An action that fails where it stands. */
	private static ViewAction failing() {
		return (context, value) -> {
			throw new RuntimeException("boom");
		};
	}

	/** The window the dialog displays. */
	private ReactWindowControl window() {
		ReactWindowControl window = find(_dialogs.opened(), ReactWindowControl.class);
		assertNotNull("The dialog displays a window.", window);
		return window;
	}

	/**
	 * The first control of the given kind in the displayed tree, or {@code null} if the tree holds
	 * none.
	 */
	private static <T> T find(ReactControl control, Class<T> kind) {
		if (kind.isInstance(control)) {
			return kind.cast(control);
		}
		for (ReactControl child : control.displayedChildren()) {
			T found = find(child, kind);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = TestSuspendedCommands.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * An action that holds the chain until the test hands it back.
	 */
	private static class Suspend extends InterruptibleViewAction {

		private Continuation _continuation;

		@Override
		public void execute(ReactContext context, Object input, Continuation continuation) {
			_continuation = continuation;
		}

		void resume(Object value) {
			_continuation.resume(value);
		}

		void abort() {
			_continuation.abort();
		}
	}

	/**
	 * Stands in for the dialogs of a window, holding the one that was opened.
	 */
	private static class Dialogs implements DialogManager {

		private Dialog _open;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			assertNull("One dialog at a time.", _open);
			_open = new Dialog(child);
			return _open;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			assertNotNull("A dialog is open.", _open);
			if (!_open.isClosable()) {
				return;
			}
			_open = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing is stacked on the dialog of these tests.");
		}

		/** The content of the dialog the user is shown. */
		ReactControl opened() {
			assertNotNull("A dialog is open.", _open);
			return _open.content();
		}
	}

	/**
	 * The dialog the fixture opened, recording every change of whether it can be closed.
	 */
	private static class Dialog implements DialogHandle {

		private final ReactControl _content;

		private boolean _closable = true;

		private final List<Boolean> _closableChanges = new ArrayList<>();

		Dialog(ReactControl content) {
			_content = content;
		}

		ReactControl content() {
			return _content;
		}

		/** Whether the dialog was told it can be closed, in the order it was told. */
		List<Boolean> closableChanges() {
			return _closableChanges;
		}

		@Override
		public void close(DialogResult<Void> result) {
			// Not used: these tests ask whether the dialog may be closed, not to close it.
		}

		@Override
		public void setClosable(boolean closable) {
			if (closable == _closable) {
				return;
			}
			_closable = closable;
			_closableChanges.add(Boolean.valueOf(closable));
		}

		@Override
		public boolean isClosable() {
			return _closable;
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * Nothing displayed here is an object others observe, so the page reports no
	 * {@link ModelScope} - which spares the test the knowledge base one is built from.
	 * </p>
	 */
	private static final class PageContext extends DefaultReactContext {

		PageContext(SSEUpdateQueue sseQueue) {
			super("", "test", sseQueue, new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the session resources the labels are resolved
	 * with, the {@link ThemeFactory} the window's buttons take their icons from, plus the
	 * {@link SchedulerService} the event stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSuspendedCommands.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE, SchedulerService.Module.INSTANCE));
	}
}
