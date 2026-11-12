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
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.CancelDialogCommand;
import com.top_logic.layout.view.command.CloseDialogAction;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.SuspendedCommands;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
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
		assertEquals("The chain goes on with its unchanged argument.", List.of("in"), _downstream);
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
	 * Tests that a command closing its own dialog after it was taken up again closes the dialog at
	 * once: the command running the close is no longer held.
	 */
	public void testCloseInContinuationClosesAtOnce() {
		Suspend suspend = new Suspend();
		run(suspend, closeDialog(), (context, value) -> {
			_downstream.add(Boolean.valueOf(_dialogs.isOpen(_dialog)));
			return value;
		});

		assertTrue("The command waits, nothing is closed yet.", _dialogs.isOpen(_dialog));

		suspend.resume("late");

		assertEquals("The close took effect before the next action ran.", List.of(Boolean.FALSE), _downstream);
		assertFalse(_dialogs.isOpen(_dialog));
	}

	/**
	 * Tests that the actions continuing a command see it no longer suspended: the action that held
	 * it has handed it back.
	 */
	public void testNotSuspendedWhileContinuationRuns() {
		Suspend suspend = new Suspend();
		run(suspend, recordClosable());

		assertFalse(_dialog.isClosable());

		suspend.resume("late");

		assertEquals("The continuation runs with the dialog closable.", List.of(Boolean.TRUE), _downstream);
		assertTrue(_dialog.isClosable());
	}

	/**
	 * Tests that a command held again by a later action counts as suspended again, and is released
	 * when that action hands it back.
	 */
	public void testHeldAgainByLaterAction() {
		Suspend first = new Suspend();
		Suspend second = new Suspend();
		run(first, recordClosable(), second, recordClosable());

		assertFalse(_dialog.isClosable());

		first.resume("one");

		assertEquals("Between the holds, the command is not suspended.", List.of(Boolean.TRUE), _downstream);
		assertFalse("The second action holds the command again.", _dialog.isClosable());

		second.resume("two");

		assertEquals(List.of(Boolean.TRUE, Boolean.TRUE), _downstream);
		assertTrue(_dialog.isClosable());
		assertEquals("Held, handed back, held again, handed back.",
			List.of(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE), _dialog.closableChanges());
	}

	/**
	 * Tests that the follow-up actions an action runs over its result when it hands the chain back
	 * (as a finished job does) see the command no longer suspended, and that a hold inside them
	 * suspends it again.
	 */
	public void testFollowUpsRunNotSuspended() {
		Suspend inner = new Suspend();
		FollowUpLater job = new FollowUpLater(List.of(recordClosable(), inner, recordClosable()));
		run(job, recordClosable());

		assertFalse(_dialog.isClosable());

		job.finish("result");

		assertEquals(List.of(Boolean.TRUE), _downstream);
		assertFalse("The follow-up actions are held again.", _dialog.isClosable());

		inner.resume("inner");

		assertEquals("The follow-up actions and the actions after the step ran with the command released.",
			List.of(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE), _downstream);
		assertTrue(_dialog.isClosable());
	}

	/**
	 * Tests that a command held inside a branch counts as suspended, and is released when the branch
	 * is handed back, although the branch action contains the hold.
	 */
	public void testHeldInsideNestedChain() {
		Suspend inner = new Suspend();
		ViewAction branch = new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				ViewActionChain.nest(context, List.of(inner, recordClosable()), ActionArguments.NONE, continuation);
			}
		};
		run(branch, recordClosable());

		assertFalse(_dialog.isClosable());

		inner.resume("late");

		assertEquals(List.of(Boolean.TRUE, Boolean.TRUE), _downstream);
		assertTrue(_dialog.isClosable());
	}

	/**
	 * Tests that a command failing in the actions continuing it after a second hold leaves no
	 * suspension behind.
	 */
	public void testReleasedOnFailureAfterSecondHold() {
		Suspend first = new Suspend();
		Suspend second = new Suspend();
		run(first, second, failing());

		first.resume("one");
		assertFalse(_dialog.isClosable());

		try {
			second.resume("two");
			fail("The action following the suspended one throws.");
		} catch (RuntimeException expected) {
			assertEquals("boom", expected.getMessage());
		}

		assertTrue("A failed command no longer holds the dialog.", _dialog.isClosable());
		assertEquals(List.of(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE), _dialog.closableChanges());
	}

	/**
	 * Tests that a command failing outside of every action - in the compensation its abort runs -
	 * leaves no suspension behind.
	 */
	public void testReleasedOnFailingCompensation() {
		Suspend suspend = new Suspend();
		ViewAction compensated = new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				continuation.onAbort(() -> {
					throw new RuntimeException("undo failed");
				});
				continuation.resume(input);
			}
		};
		run(compensated, suspend);

		assertFalse(_dialog.isClosable());

		try {
			suspend.abort();
			fail("The compensation throws.");
		} catch (RuntimeException expected) {
			assertEquals("undo failed", expected.getMessage());
		}

		assertTrue("A failed command no longer holds the dialog.", _dialog.isClosable());
	}

	/**
	 * Tests that a close requested by another command while one of the dialog is held waits for
	 * that command, and then closes the dialog it was requested in - not the one on top by then.
	 */
	public void testCloseFromOtherCommandWaitsAndClosesOwnDialog() {
		Suspend suspend = new Suspend();
		run(suspend);

		run(closeDialog());

		assertTrue("A command of the dialog is held, the dialog stays.", _dialogs.isOpen(_dialog));

		Dialog report = (Dialog) OpenDialogAction.openDialog(_page, ViewLoader.fullPath(VIEW), false, Map.of(),
			List.of());

		suspend.resume("late");

		assertFalse("The dialog the close was requested in is closed, not just the one on top.",
			_dialogs.isOpen(_dialog));
		assertFalse("Closing a dialog closes the ones stacked on it.", _dialogs.isOpen(report));
	}

	/**
	 * Tests that a command running through closes its dialog at once.
	 */
	public void testCloseWithoutSuspensionClosesAtOnce() {
		run(closeDialog());

		assertFalse(_dialogs.isOpen(_dialog));
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

		assertEquals("The chain goes on with its unchanged argument.", List.of("in"), _downstream);
	}

	private static ViewAction closeDialog() {
		CloseDialogAction.Config config = TypedConfiguration.newConfigItem(CloseDialogAction.Config.class);
		return new CloseDialogAction(new DefaultInstantiationContext(TestSuspendedCommands.class), config);
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

	/** An action recording whether the dialog can be closed when it runs. */
	private ViewAction recordClosable() {
		return (context, value) -> {
			_downstream.add(Boolean.valueOf(_dialog.isClosable()));
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
	 * An action that holds the chain like a job and, when the test finishes it, runs follow-up
	 * actions over its result before handing the chain on.
	 */
	private static class FollowUpLater extends InterruptibleViewAction {

		private final List<ViewAction> _followUps;

		private ReactContext _context;

		private Continuation _continuation;

		FollowUpLater(List<ViewAction> followUps) {
			_followUps = followUps;
		}

		@Override
		public void execute(ReactContext context, Object input, Continuation continuation) {
			_context = context;
			_continuation = continuation;
		}

		void finish(Object result) {
			ViewActionChain.followUp(_context, _followUps, ActionArguments.NONE, result, _continuation);
		}
	}

	/**
	 * Stands in for the dialogs of a window, holding the stack of opened ones.
	 */
	private static class Dialogs implements DialogManager {

		private final List<Dialog> _stack = new ArrayList<>();

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			Dialog dialog = new Dialog(this, child);
			_stack.add(dialog);
			return dialog;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			assertFalse("A dialog is open.", _stack.isEmpty());
			close(_stack.get(_stack.size() - 1));
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing reveals a dialog in these tests.");
		}

		/**
		 * Closes the given dialog together with the ones above it, unless one of them must stay.
		 */
		void close(Dialog dialog) {
			int index = _stack.indexOf(dialog);
			if (index < 0) {
				return;
			}
			for (int n = index; n < _stack.size(); n++) {
				if (!_stack.get(n).isClosable()) {
					return;
				}
			}
			while (_stack.size() > index) {
				_stack.remove(_stack.size() - 1);
			}
		}

		/** Whether the given dialog is still open. */
		boolean isOpen(Dialog dialog) {
			return _stack.contains(dialog);
		}

		/** The content of the dialog the user is shown. */
		ReactControl opened() {
			assertFalse("A dialog is open.", _stack.isEmpty());
			return _stack.get(_stack.size() - 1).content();
		}
	}

	/**
	 * A dialog the fixture opened, recording every change of whether it can be closed.
	 */
	private static class Dialog implements DialogHandle {

		private final Dialogs _dialogs;

		private final ReactControl _content;

		private boolean _closable = true;

		private final List<Boolean> _closableChanges = new ArrayList<>();

		Dialog(Dialogs dialogs, ReactControl content) {
			_dialogs = dialogs;
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
			_dialogs.close(this);
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
				ThemeFactory.Module.INSTANCE, SchedulerService.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}
}
