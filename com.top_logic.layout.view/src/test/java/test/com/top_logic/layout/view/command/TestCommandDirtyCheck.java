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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.I18NConstants;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.command.DirtyCheckScope;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.layout.view.login.LogoutCommand;
import com.top_logic.model.TransientObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link ViewCommand} asks about the unsaved changes its
 * {@link com.top_logic.layout.view.command.ViewCommand.Config#getCheckDirty() dirty check} names
 * before it runs.
 *
 * <p>
 * The window displays a tab bar whose active tab holds a form. The form reports what it holds
 * unsaved to the tab, and the tab to the window.
 * </p>
 */
public class TestCommandDirtyCheck extends TestCase {

	/** The view displaying a tab bar whose active tab holds a form. */
	private static final String VIEW = "command-dirty-check.view.xml";

	/** The channel delivering the object the form displays. */
	private static final String EDITED = "edited";

	/** The command a button executes when it is clicked. */
	private static final String CLICK = "click";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private ViewContext _window;

	private ReactControl _root;

	private Dialogs _dialogs;

	/** Number of times the tested command ran. */
	private int _executed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-command-dirty-check").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		copyFixture(VIEW, new File(views, VIEW));
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		_dialogs = new Dialogs();
		_sseQueue.setDialogManager(_dialogs);
		ReactContext reactContext = new PageContext(_sseQueue);
		ViewElement view = ViewLoader.getOrLoadView(ViewLoader.fullPath(VIEW));
		_window = new DefaultViewContext(reactContext, ViewLoader.fullPath(VIEW));
		_root = (ReactControl) view.createControl(_window);

		// The form displays an object, without which it holds nothing that could be left unsaved.
		_window.resolveChannel(new ChannelRef(EDITED)).set(new MockTLObject());

		_root.attach();
		_root.write(new TagWriter());
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Tests that the window tracks the forms of its tabs, although nothing configures that.
	 */
	public void testFormInTabReachesWindow() {
		DirtyChannel windowChannel = _window.getDirtyChannel();
		assertNotNull("Every window is a dirty-tracked scope.", windowChannel);
		assertSame("Nothing encloses the window.", windowChannel, windowChannel.root());

		FormControl form = editedForm();
		assertEquals(List.of(form), windowChannel.getDirtyHandlers());

		form.executeDiscard();
		assertEquals(List.of(), windowChannel.getDirtyHandlers());
	}

	/**
	 * Tests that a context created within the window - a dialog, say - is a scope of its own that
	 * lies within the window.
	 */
	public void testNestedRootContextLiesWithinWindow() {
		ViewContext dialog = new DefaultViewContext(_window);

		assertNotSame(_window.getDirtyChannel(), dialog.getDirtyChannel());
		assertSame(_window.getDirtyChannel(), dialog.getDirtyChannel().root());
	}

	/**
	 * Tests that a command checking the whole window does not run while a form anywhere in the
	 * window holds unsaved changes, and names that form.
	 */
	public void testViewCheckVetoes() {
		FormControl form = editedForm();
		ViewCommandModel model = commandModel(siblingScope(), DirtyCheckScope.VIEW);

		try {
			model.executeCommand(_window);
			fail("A command checking the window must not run while the window holds unsaved changes.");
		} catch (ChannelVetoException ex) {
			assertEquals(List.of(form), ex.getDirtyHandlers());
		}
		assertEquals("The command did not run.", 0, _executed);
	}

	/**
	 * Tests that the continuation of the refusal runs the command once the changes are discarded.
	 */
	public void testViewCheckContinuationRunsCommand() {
		FormControl form = editedForm();
		ViewCommandModel model = commandModel(_window, DirtyCheckScope.VIEW);

		ChannelVetoException veto = null;
		try {
			model.executeCommand(_window);
			fail("A command checking the window must not run while the window holds unsaved changes.");
		} catch (ChannelVetoException ex) {
			veto = ex;
		}

		form.executeDiscard();
		veto.getContinuation().run();

		assertEquals("Nothing is left unsaved, so the command runs.", 1, _executed);
	}

	/**
	 * Tests that clicking the button of a command checking the window asks the user, and runs the
	 * command once the user discarded the changes.
	 */
	public void testButtonAsksAndRunsAfterDiscard() {
		FormControl form = editedForm();
		ReactButtonControl button = new ReactButtonControl(_window, commandModel(_window, DirtyCheckScope.VIEW));

		HandlerResult result = button.executeCommand(CLICK, Map.of());

		assertTrue(result.isSuccess());
		assertEquals("The user is asked about the unsaved changes.", 1, _dialogs.opened());
		assertEquals("Until they answered, the command does not run.", 0, _executed);

		_dialogs.click(Resources.getInstance().getString(I18NConstants.BUTTON_DISCARD));

		assertFalse("The changes are discarded.", form.isDirty());
		assertEquals("The command runs after the discard.", 1, _executed);
	}

	/**
	 * Tests that a command checking its own scope asks about the forms within that scope.
	 */
	public void testSelfCheckAsksOwnScope() {
		FormControl form = editedForm();
		ViewCommandModel model = commandModel(_window, DirtyCheckScope.SELF);

		try {
			model.executeCommand(_window);
			fail("The tab holding the form lies within the scope of the command.");
		} catch (ChannelVetoException ex) {
			assertEquals(List.of(form), ex.getDirtyHandlers());
		}
		assertEquals(0, _executed);
	}

	/**
	 * Tests that a command checking its own scope runs, although a form in a sibling scope holds
	 * unsaved changes.
	 */
	public void testSelfCheckIgnoresSiblingScope() {
		editedForm();
		ViewCommandModel model = commandModel(siblingScope(), DirtyCheckScope.SELF);

		model.executeCommand(_window);

		assertEquals(1, _executed);
	}

	/**
	 * Tests that a command without a configured check runs without asking, whatever is unsaved.
	 */
	public void testDefaultDoesNotCheck() {
		editedForm();
		ViewCommand.Config config = TypedConfiguration.newConfigItem(ViewCommand.Config.class);
		assertEquals(DirtyCheckScope.NONE, config.getCheckDirty());

		ViewCommandModel model = ViewCommandModel.forCommand(_window, this::run, config);
		model.executeCommand(_window);

		assertEquals(1, _executed);
	}

	/**
	 * Tests that logging out asks about the unsaved changes of the whole window without further
	 * configuration.
	 */
	public void testLogoutChecksWindow() {
		LogoutCommand.Config config = TypedConfiguration.newConfigItem(LogoutCommand.Config.class);

		assertEquals(DirtyCheckScope.VIEW, config.getCheckDirty());
	}

	private HandlerResult run(ReactContext context, Object input) {
		_executed++;
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * A model of a command counting its runs, displayed in the given context and checking the given
	 * scope.
	 */
	private ViewCommandModel commandModel(ViewContext context, DirtyCheckScope check) {
		ViewCommand.Config config = TypedConfiguration.newConfigItem(ViewCommand.Config.class);
		config.update(config.descriptor().getProperty(ViewCommand.Config.CHECK_DIRTY), check);
		return ViewCommandModel.forCommand(context, this::run, config);
	}

	/**
	 * A scope within the window next to the tab bar, a second tab bar say.
	 */
	private ViewContext siblingScope() {
		ViewContext sibling = _window.childContext("sibling");
		sibling.setDirtyChannel(new DirtyChannel(_window.getDirtyChannel()));
		return sibling;
	}

	/**
	 * The form of the displayed tab, holding input the user typed and has not saved.
	 */
	private FormControl editedForm() {
		FormControl form = find(_root, FormControl.class);
		assertNotNull("The displayed tab builds the form.", form);

		form.enterEditMode();
		form.registerParticipant(new UnsavedInput());
		form.updateDirtyState();

		assertTrue("The form holds unsaved input.", form.isDirty());
		return form;
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
		try (InputStream in = TestCommandDirtyCheck.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * The object the form displays is transient and observed by nobody, so the page reports no
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
	 * Input the user typed into a form and has not saved.
	 */
	private static class UnsavedInput implements FormParticipant {

		private boolean _dirty = true;

		@Override
		public boolean validate() {
			return true;
		}

		@Override
		public void persist(Transaction tx) {
			// Nothing to persist.
		}

		@Override
		public void cancel() {
			_dirty = false;
		}

		@Override
		public void revealAll() {
			// No hidden errors.
		}

		@Override
		public boolean isDirty() {
			return _dirty;
		}
	}

	/**
	 * Stands in for the dialogs of the window, counting the ones opened and answering the one open.
	 */
	private static class Dialogs implements DialogManager {

		private final List<ReactControl> _open = new ArrayList<>();

		private int _opened;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_opened++;
			_open.add(child);
			return new DialogHandle() {
				@Override
				public void close(DialogResult<Void> result) {
					_open.remove(child);
				}

				@Override
				public void setClosable(boolean closable) {
					// Always closable in these tests.
				}

				@Override
				public boolean isClosable() {
					return true;
				}
			};
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			assertFalse("A dialog is open.", _open.isEmpty());
			_open.remove(_open.size() - 1);
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing is stacked in these tests.");
		}

		/** Number of dialogs opened so far. */
		int opened() {
			return _opened;
		}

		/**
		 * Clicks the button with the given label in the topmost dialog.
		 */
		void click(String label) {
			assertFalse("A dialog is open.", _open.isEmpty());
			ReactButtonControl button = findButton(_open.get(_open.size() - 1), label);
			assertNotNull("The dialog offers '" + label + "'.", button);
			assertTrue(button.executeCommand(CLICK, Map.of()).isSuccess());
		}

		private static ReactButtonControl findButton(ReactControl control, String label) {
			if (control instanceof ReactButtonControl button
				&& button.scriptingScalarState().containsValue(label)) {
				return button;
			}
			for (ReactControl child : control.displayedChildren()) {
				ReactButtonControl found = findButton(child, label);
				if (found != null) {
					return found;
				}
			}
			return null;
		}
	}

	/**
	 * The object the form displays; no attribute of it is read by these tests.
	 */
	private static class MockTLObject extends TransientObject {
		// Nothing but the identity of the object is used.
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the session resources the labels are resolved
	 * with, plus the {@link SchedulerService} the event stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCommandDirtyCheck.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}

}
