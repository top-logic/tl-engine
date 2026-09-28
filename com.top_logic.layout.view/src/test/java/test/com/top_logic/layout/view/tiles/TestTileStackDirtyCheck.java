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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
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
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.layout.view.tiles.ReactTileStackControl;
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
 * and the URL use as well.
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

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private DirtyChannel _enclosing;

	private ReactControl _root;

	private ViewChannel _path;

	private TileStackScope _scope;

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

	private void pushFrame(String label) {
		_scope.push(FORM_VIEW, ResKey.text(label), Map.of(EDITED, new MockTLObject()));
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
