/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.form.ItemFixture;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ReloadableControl;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.tiles.ReactTileStackControl;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a frame a tile stack drops is gone: every control it displayed is disposed and no
 * longer addressable in the window, and nothing outliving the frame keeps it reachable.
 *
 * <p>
 * The frame shows what a detail frame typically shows - a form with fields, a field inside a
 * {@code <visible-if>}, a command and a channel derived from the path of the stack, which outlives
 * every frame.
 * </p>
 */
public class TestTileFrameDisposal extends BasicTestCase {

	/** The view displaying the stack. */
	private static final String STACK_VIEW = "tile-leak-stack.view.xml";

	/** The view the stack starts with. */
	private static final String HOME_VIEW = "tile-leak-home.view.xml";

	/** The view of a pushed frame. */
	private static final String DETAIL_VIEW = "tile-leak-detail.view.xml";

	/** The channel holding the path of the stack. */
	private static final String NAV_PATH = "navPath";

	/** The parameter of a detail frame naming the object its form displays. */
	private static final String ITEM = "item";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private SubscriptionCountingChannel _path;

	private ReactControl _root;

	private ItemFixture _items;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-tile-frame-disposal").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String view : List.of(STACK_VIEW, HOME_VIEW, DETAIL_VIEW)) {
			copyFixture(view, new File(views, view));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_items = new ItemFixture("test.tileFrameDisposal");

		_sseQueue = new SSEUpdateQueue();
		ReactContext reactContext = new PageContext(_sseQueue);
		ViewElement view = ViewLoader.getOrLoadView(ViewLoader.fullPath(STACK_VIEW));
		ViewContext viewContext = new DefaultViewContext(reactContext, ViewLoader.fullPath(STACK_VIEW));
		// Bound in advance, so that the view uses this channel instead of creating its own.
		_path = new SubscriptionCountingChannel(NAV_PATH);
		viewContext.registerChannel(NAV_PATH, _path);
		_root = (ReactControl) view.createControl(viewContext);
		assertSame(_path, viewContext.resolveChannel(new ChannelRef(NAV_PATH)));

		render();
	}

	@Override
	protected void tearDown() throws Exception {
		_root.cleanupTree();
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		_root = null;
		_path = null;
		_sseQueue = null;
		_items = null;

		super.tearDown();
	}

	/**
	 * Tests that popping a frame disposes every control it displayed and leaves none of them
	 * addressable in the window.
	 */
	public void testPopDisposesTheFrame() {
		_path.set(List.of(detail("first")));
		List<ReactControl> frameControls = displayedFrameControls();

		_path.set(List.of());

		assertGone(frameControls);
	}

	/**
	 * Tests that replacing a frame by another one disposes every control of the replaced frame,
	 * while the frame replacing it is displayed.
	 */
	public void testReplaceDisposesTheFrame() {
		_path.set(List.of(detail("first")));
		List<ReactControl> replaced = displayedFrameControls();

		_path.set(List.of(detail("second")));
		List<ReactControl> replacing = displayedFrameControls();

		assertGone(replaced);
		for (ReactControl control : replacing) {
			assertSame("The frame replacing the dropped one is addressable: " + control,
				control, _sseQueue.getControl(control.getID()));
		}
	}

	/**
	 * Tests that a dropped frame can be garbage collected: nothing that outlives it - the path of
	 * the stack, which the frame derives a channel from, the form model of a field or the window's
	 * queue - keeps a reference to it.
	 */
	public void testADroppedFrameIsCollected() {
		_path.set(List.of(detail("first")));
		WeakReference<ReactControl> frame = new WeakReference<>(activeFrame());
		WeakReference<FormControl> form = new WeakReference<>(find(activeFrame(), FormControl.class));
		assertNotNull("The frame displays a form.", form.get());

		_path.set(List.of());
		// Updates for the client are not what is under test; drop the ones nobody sends.
		_sseQueue.discardPendingEvents();

		assertCollected("The dropped frame is still reachable.", frame);
		assertCollected("The form of the dropped frame is still reachable.", form);
	}

	/**
	 * Tests that a dropped frame leaves nothing subscribed to the path of the stack, although its
	 * view derives a channel from that path, and that a live frame's derived channel follows it.
	 */
	public void testADroppedFrameUnsubscribesFromThePath() {
		int listeners = _path.listenerCount();
		int vetoListeners = _path.vetoListenerCount();

		_path.set(List.of(detail("first")));
		ReactControl first = activeFrame();
		render();
		assertTrue("The derived channel of a displayed frame follows the path.",
			_path.listenerCount() > listeners);
		assertTrue("The derived channel of a displayed frame answers for the path.",
			_path.vetoListenerCount() > vetoListeners);
		assertEquals("At depth 1, the frame shows the field inside its visible-if.", 2, fieldCount(first));

		// A frame covered by a drill-down stays alive, and so does what it derives from the path.
		_path.set(List.of(detail("first"), detail("second")));
		assertEquals("The covered frame's derived channel follows the path: depth 2 hides the field.",
			1, fieldCount(first));

		_path.set(List.of());

		assertEquals("A dropped frame leaves no listener on the path.", listeners, _path.listenerCount());
		assertEquals("A dropped frame leaves no veto listener on the path.",
			vetoListeners, _path.vetoListenerCount());
	}

	/**
	 * Tests that reloading the view of a frame builds it with channels of its own: the reloaded view
	 * derives its channel from the path again, and the channel of the replaced view is unsubscribed.
	 */
	public void testAReloadedFrameDerivesItsChannelsAnew() {
		_path.set(List.of(detail("first")));
		ReloadableControl frame = (ReloadableControl) activeFrame();
		render();
		int listeners = _path.listenerCount();
		int vetoListeners = _path.vetoListenerCount();

		frame.viewChanged(Set.of(ViewLoader.fullPath(DETAIL_VIEW)));
		render();

		assertEquals("The reload replaces the subscriptions of the view rather than adding to them.",
			listeners, _path.listenerCount());
		assertEquals("The reload replaces the veto listeners of the view rather than adding to them.",
			vetoListeners, _path.vetoListenerCount());
		assertEquals("At depth 1, the reloaded frame shows the field inside its visible-if.", 2,
			fieldCount(frame));

		_path.set(List.of(detail("first"), detail("second")));
		assertEquals("The reloaded frame's derived channel follows the path: depth 2 hides the field.",
			1, fieldCount(frame));
	}

	private static long fieldCount(ReactControl frame) {
		List<ReactControl> controls = new ArrayList<>();
		collect(frame, controls);
		return controls.stream().filter(ReactFormFieldChromeControl.class::isInstance).count();
	}

	private void render() {
		_root.attach();
		try {
			_root.write(new TagWriter());
		} catch (IOException ex) {
			throw new AssertionError("Cannot render the view.", ex);
		}
	}

	/**
	 * The controls of the frame displayed on top of the stack, after rendering it: a container
	 * building its content when rendered displays it only then.
	 */
	private List<ReactControl> displayedFrameControls() {
		render();

		ReactControl frame = activeFrame();
		List<ReactControl> result = new ArrayList<>();
		collect(frame, result);

		assertNotNull("The frame displays a form.", find(frame, FormControl.class));
		long fields = result.stream().filter(ReactFormFieldChromeControl.class::isInstance).count();
		assertEquals("The frame displays the field of its form and the one inside the visible-if.", 2, fields);
		for (ReactControl control : result) {
			assertTrue("A displayed control is attached: " + control, control.isAttached());
			assertSame("A displayed control is addressable: " + control,
				control, _sseQueue.getControl(control.getID()));
		}
		return result;
	}

	private ReactControl activeFrame() {
		List<ReactControl> visible = stack().visibleChildren();
		assertEquals("The stack displays one frame.", 1, visible.size());
		return visible.get(0);
	}

	private void assertGone(List<ReactControl> controls) {
		for (ReactControl control : controls) {
			assertTrue("A control of the dropped frame is disposed: " + control, control.isDisposed());
			assertFalse("A control of the dropped frame is detached: " + control, control.isAttached());
			assertNull("A control of the dropped frame is not addressable: " + control,
				_sseQueue.getControl(control.getID()));
		}
	}

	private static void assertCollected(String message, WeakReference<?> reference) {
		for (int attempt = 0; attempt < 20 && reference.get() != null; attempt++) {
			System.gc();
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		assertNull(message, reference.get());
	}

	private TileFrame detail(String name) {
		return new TileFrame(DETAIL_VIEW, ResKey.text(name), Map.of(ITEM, _items.newItem(name)));
	}

	private ReactTileStackControl stack() {
		ReactTileStackControl result = find(_root, ReactTileStackControl.class);
		assertNotNull("The view displays a tile stack.", result);
		return result;
	}

	private static void collect(ReactControl control, List<ReactControl> result) {
		result.add(control);
		for (ReactControl child : control.displayedChildren()) {
			collect(child, result);
		}
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
		try (InputStream in = TestTileFrameDisposal.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * {@link DefaultViewChannel} counting the listeners and veto listeners registered on it.
	 */
	private static final class SubscriptionCountingChannel extends DefaultViewChannel {

		private final List<ChannelListener> _listeners = new ArrayList<>();

		private final List<VetoListener> _vetoListeners = new ArrayList<>();

		SubscriptionCountingChannel(String name) {
			super(name);
		}

		@Override
		public void addListener(ChannelListener listener) {
			_listeners.add(listener);
			super.addListener(listener);
		}

		@Override
		public void removeListener(ChannelListener listener) {
			_listeners.remove(listener);
			super.removeListener(listener);
		}

		@Override
		public void addVetoListener(VetoListener listener) {
			_vetoListeners.add(listener);
			super.addVetoListener(listener);
		}

		@Override
		public void removeVetoListener(VetoListener listener) {
			_vetoListeners.remove(listener);
			super.removeVetoListener(listener);
		}

		/** The number of listeners registered and not removed. */
		int listenerCount() {
			return _listeners.size();
		}

		/** The number of veto listeners registered and not removed. */
		int vetoListenerCount() {
			return _vetoListeners.size();
		}
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * The objects the frames display are transient and observed by nobody, so the page reports no
	 * {@link ModelScope}.
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
	 * The suite of tests.
	 *
	 * @implNote The derived channel and the visible-if of a frame are TL-Script expressions, and
	 *           the fields resolve their input controls through the {@link FieldControlService};
	 *           both need the application model and the knowledge base it lives in.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTileFrameDisposal.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE, SchedulerService.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
