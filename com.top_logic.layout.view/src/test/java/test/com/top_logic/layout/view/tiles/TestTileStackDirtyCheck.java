/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

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
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
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
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.layout.view.navigation.Binding;
import com.top_logic.layout.view.navigation.ObjectNavigation;
import com.top_logic.layout.view.navigation.ShowStep;
import com.top_logic.layout.view.tiles.ReactTileStackControl;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileStackScope;
import com.top_logic.model.TransientObject;
import com.top_logic.model.listen.ModelScope;

/**
 * Leaving a frame of a {@link ReactTileStackControl tile stack} asks about the input left unsaved in
 * it.
 *
 * <p>
 * Each pushed frame displays a form. Every navigation of the stack is a write to its path, so the
 * tests navigate through the {@link TileStackScope}, which is what the breadcrumb, the pop commands
 * and the URL use as well - or display an object in a frame drilled down to through
 * {@link ObjectNavigation}, which asks the user when the write is refused.
 * </p>
 */
public class TestTileStackDirtyCheck extends TestCase {

	/** The view displaying the tile stack. */
	private static final String VIEW = "dirty-frames.view.xml";

	/** The view the stack starts with. */
	private static final String INITIAL_VIEW = "dirty-frames-initial.view.xml";

	/** The view of a pushed frame, displaying a form. */
	private static final String FORM_VIEW = "dirty-frames-form.view.xml";

	/** The channel holding the path of the stack. */
	private static final String PATH = "navPath";

	/** The frame parameter delivering the object the form of a frame displays. */
	private static final String EDITED = "edited";

	/** The command a button executes when it is clicked. */
	private static final String CLICK = "click";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private DirtyChannel _enclosing;

	private ReactControl _root;

	private ViewChannel _path;

	private TileStackScope _scope;

	/** A context within the stack, where a display request drilling it down comes from. */
	private ViewContext _inStack;

	/** The dialogs the user was shown. */
	private Dialogs _dialogs;

	/** Number of writes of the path since the fixture was set up. */
	private int _writes;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-tile-stack-dirty-check").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String fixture : List.of(VIEW, INITIAL_VIEW, FORM_VIEW)) {
			copyFixture(fixture, new File(views, fixture));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		_dialogs = new Dialogs();
		_sseQueue.setDialogManager(_dialogs);
		ReactContext reactContext = new PageContext(_sseQueue);
		ViewElement view = ViewLoader.getOrLoadView(ViewLoader.fullPath(VIEW));
		ViewContext viewContext = new DefaultViewContext(reactContext, ViewLoader.fullPath(VIEW));

		// The stack lies within a dirty-tracked scope, a tab say.
		_enclosing = new DirtyChannel();
		viewContext.setDirtyChannel(_enclosing);

		_root = (ReactControl) view.createControl(viewContext);
		_root.attach();
		_root.write(new TagWriter());

		_path = viewContext.resolveChannel(new ChannelRef(PATH));
		_scope = new TileStackScope(_path);
		_inStack = viewContext.withScope(TileStackScope.class, _scope);
		_path.addListener((sender, oldValue, newValue) -> _writes++);
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Tests that a pop of a frame holding unsaved input is refused, naming the form holding it.
	 */
	public void testPopOfDirtyFrameIsVetoed() {
		pushFrame("A");
		pushFrame("B");
		FormControl top = editForm(2);
		Object pathBefore = _path.get();

		try {
			_scope.pop();
			fail("A frame holding unsaved input must not be left silently.");
		} catch (ChannelVetoException ex) {
			assertEquals("The form holding the input is what refuses the pop.", List.of(top), ex.getDirtyHandlers());
		}
		assertSame("The refused pop leaves the path.", pathBefore, _path.get());
		assertEquals("The refused pop leaves the frames.", 3, stack().displayedChildren().size());
	}

	/**
	 * Tests that popping to a shorter path is refused, naming the forms of all frames it drops.
	 */
	public void testPopToOfDirtyFramesIsVetoed() {
		pushFrame("A");
		pushFrame("B");
		FormControl first = editForm(1);
		FormControl second = editForm(2);
		Object pathBefore = _path.get();

		try {
			_scope.popTo(0);
			fail("Frames holding unsaved input must not be left silently.");
		} catch (ChannelVetoException ex) {
			assertEquals("Each dropped frame is asked about, in frame order.", List.of(first, second),
				ex.getDirtyHandlers());
		}
		assertSame("The refused pop leaves the path.", pathBefore, _path.get());
	}

	/**
	 * Tests that the pop goes through once the input is discarded, which is what the user answering
	 * the question does before the continuation of the veto runs.
	 */
	public void testContinuationPopsAfterDiscard() {
		pushFrame("A");
		pushFrame("B");
		FormControl top = editForm(2);

		ChannelVetoException veto = null;
		try {
			_scope.pop();
			fail("A frame holding unsaved input must not be left silently.");
		} catch (ChannelVetoException ex) {
			veto = ex;
		}

		top.executeDiscard();
		veto.getContinuation().run();

		assertEquals("Nothing is left unsaved, so the frame is popped.", 1, _scope.getPath().size());
		assertEquals(2, stack().displayedChildren().size());
	}

	/**
	 * Tests that a frame holding nothing unsaved is left without a question.
	 */
	public void testCleanFramePops() {
		pushFrame("A");
		pushFrame("B");

		_scope.pop();
		assertEquals(1, _scope.getPath().size());

		_scope.popTo(0);
		assertEquals(0, _scope.getPath().size());
	}

	/**
	 * Tests that pushing a frame is never refused: it drops nothing, even when the frame it covers
	 * holds unsaved input.
	 */
	public void testPushIsNotVetoed() {
		pushFrame("A");
		editForm(1);

		pushFrame("B");

		assertEquals(2, _scope.getPath().size());
	}

	/**
	 * Tests that a pop asks only about the frames it drops: unsaved input in a frame the shorter path
	 * keeps does not refuse it.
	 */
	public void testKeptFrameIsNotAsked() {
		pushFrame("A");
		pushFrame("B");
		FormControl kept = editForm(1);

		_scope.pop();

		assertEquals(1, _scope.getPath().size());
		assertTrue("The kept frame still holds its input.", kept.isDirty());
	}

	/**
	 * Tests that what a form of a frame holds unsaved is held unsaved in the scope enclosing the
	 * stack as well, so that leaving that scope asks about it.
	 */
	public void testDirtyFrameReachesEnclosingScope() {
		pushFrame("A");
		FormControl form = editForm(1);

		assertEquals(List.of(form), _enclosing.getDirtyHandlers());
		assertEquals("Any write of the path may drop the frame.", List.of(form), _path.dirtyHandlers());

		form.executeDiscard();

		assertEquals(List.of(), _enclosing.getDirtyHandlers());
		assertEquals(List.of(), _path.dirtyHandlers());
	}

	/**
	 * Tests that replacing the upper part of the path is one write, which keeps the frames below.
	 */
	public void testReplaceFromWritesOnce() {
		pushFrame("A");
		pushFrame("B");
		pushFrame("C");
		ReactControl kept = frameControl(1);
		int writesBefore = _writes;

		_scope.replaceFrom(1, List.of(formFrame(new MockTLObject()), formFrame(new MockTLObject())));

		assertEquals("The whole path is written at once.", writesBefore + 1, _writes);
		assertEquals(3, _scope.getPath().size());
		assertSame("The kept frame is displayed as it was.", kept, frameControl(1));
	}

	/**
	 * Tests that replacing a frame holding unsaved input is refused, and done by the continuation of
	 * the refusal once the input is discarded.
	 */
	public void testReplaceFromOfDirtyFrameIsVetoed() {
		pushFrame("A");
		pushFrame("B");
		FormControl dropped = editForm(2);
		Object pathBefore = _path.get();
		int writesBefore = _writes;
		TileFrame replacement = formFrame(new MockTLObject());

		ChannelVetoException veto = null;
		try {
			_scope.replaceFrom(1, List.of(replacement));
			fail("A frame holding unsaved input must not be replaced silently.");
		} catch (ChannelVetoException ex) {
			veto = ex;
		}
		assertEquals(List.of(dropped), veto.getDirtyHandlers());
		assertSame("The refused write leaves the path.", pathBefore, _path.get());
		assertEquals(writesBefore, _writes);

		dropped.executeDiscard();
		veto.getContinuation().run();

		assertEquals(writesBefore + 1, _writes);
		assertEquals(2, _scope.getPath().size());
		assertTrue(_scope.getPath().get(1).showsSame(replacement));
	}

	/**
	 * Tests that displaying the object a frame already displays leaves the frame and its unsaved
	 * input alone, although the frame is named differently than the display request names it.
	 */
	public void testShowObjectOfDisplayedFrameKeepsIt() {
		MockTLObject edited = new MockTLObject();
		_scope.push(FORM_VIEW, ResKey.text("Named by the push"), Map.of(EDITED, edited));
		FormControl form = editForm(1);
		ReactControl frame = frameControl(1);
		Object pathBefore = _path.get();
		int writesBefore = _writes;
		Recorder chain = new Recorder();

		ObjectNavigation.show(_inStack, List.of(formShow()), edited, chain);

		assertEquals("Nothing is dropped, so nothing is asked.", 0, _dialogs.opened());
		assertEquals("The path is not written.", writesBefore, _writes);
		assertSame(pathBefore, _path.get());
		assertSame("The frame stays displayed as it is.", frame, frameControl(1));
		assertTrue("The form stays in edit mode.", form.isEditMode());
		assertTrue("The form keeps its input.", form.isDirty());
		assertEquals(List.of(edited), chain._resumed);
	}

	/**
	 * Tests that displaying another object while a frame holds unsaved input asks once, and after the
	 * input is discarded reaches the frame of the object in one write of the path.
	 */
	public void testShowObjectAsksOnceAndWritesOnce() {
		pushFrame("A");
		pushFrame("B");
		editForm(2);
		Object pathBefore = _path.get();
		int writesBefore = _writes;
		MockTLObject shown = new MockTLObject();
		Recorder chain = new Recorder();

		ObjectNavigation.show(_inStack, List.of(formShow()), shown, chain);

		assertEquals("The user is asked about the frame holding unsaved input.", 1, _dialogs.opened());
		assertSame("Until they answered, the path stays.", pathBefore, _path.get());
		assertEquals(writesBefore, _writes);
		assertEquals(List.of(), chain._resumed);

		_dialogs.click(Resources.getInstance().getString(I18NConstants.BUTTON_DISCARD));

		assertEquals("The user is asked once.", 1, _dialogs.opened());
		assertEquals("The target path is reached in one write.", writesBefore + 1, _writes);
		List<TileFrame> path = _scope.getPath();
		assertEquals(1, path.size());
		assertTrue(path.get(0).showsSame(formFrame(shown)));
		assertEquals(List.of(shown), chain._resumed);
		assertFalse(chain._aborted);
	}

	private void pushFrame(String label) {
		_scope.push(FORM_VIEW, ResKey.text(label), Map.of(EDITED, new MockTLObject()));
	}

	/**
	 * An unnamed frame displaying the given object in its form.
	 */
	private static TileFrame formFrame(Object edited) {
		return new TileFrame(FORM_VIEW, null, Map.of(EDITED, edited));
	}

	/**
	 * Displaying the form frame with the shown object, named by nothing of its own.
	 */
	private static ShowStep formShow() {
		return new ShowStep(FORM_VIEW, false, null, null, List.of(new Binding(EDITED, null)));
	}

	/**
	 * The control displaying the frame at the given position of the stack, the initial view being
	 * position 0.
	 */
	private ReactControl frameControl(int position) {
		return stack().displayedChildren().get(position);
	}

	/**
	 * Enters input into the form of the frame at the given position of the stack, the initial view
	 * being position 0.
	 */
	private FormControl editForm(int position) {
		FormControl form = find(stack().displayedChildren().get(position), FormControl.class);
		assertNotNull("The frame displays a form.", form);

		form.enterEditMode();
		form.registerParticipant(new UnsavedInput());
		form.updateDirtyState();

		assertTrue("The form holds unsaved input.", form.isDirty());
		return form;
	}

	private ReactTileStackControl stack() {
		return find(_root, ReactTileStackControl.class);
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
		try (InputStream in = TestTileStackDirtyCheck.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * The objects the forms display are transient and observed by nobody, so the page reports no
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
			// Nothing to cancel.
		}

		@Override
		public void revealAll() {
			// No hidden errors.
		}

		@Override
		public boolean isDirty() {
			return true;
		}
	}

	/**
	 * The continuation of a display request, recording how it ended.
	 */
	private static class Recorder implements Continuation {

		/** The values the request was resumed with. */
		final List<Object> _resumed = new ArrayList<>();

		/** Whether the request was cancelled. */
		boolean _aborted;

		@Override
		public void resume(Object value) {
			_resumed.add(value);
		}

		@Override
		public void abort() {
			_aborted = true;
		}

		@Override
		public void onAbort(Runnable compensation) {
			// Nothing to compensate.
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
	 * The object a form displays; no attribute of it is read by these tests.
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
			ServiceTestSetup.createSetup(TestTileStackDirtyCheck.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}

}
