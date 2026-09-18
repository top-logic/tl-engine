/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.element.TabBarElement;
import com.top_logic.layout.view.navigation.Binding;
import com.top_logic.layout.view.navigation.DisplayTarget;
import com.top_logic.layout.view.navigation.DisplayTargets;
import com.top_logic.layout.view.navigation.ObjectNavigation;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.layout.view.navigation.ShowObjectAction;
import com.top_logic.layout.view.navigation.ShowStep;
import com.top_logic.layout.view.navigation.ViewMounts;
import com.top_logic.layout.view.tiles.ReactTileStackControl;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileStackElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for {@link ObjectNavigation} displaying an object where the application shows objects of its
 * type.
 *
 * <p>
 * The scenario is a root view holding a tab bar whose second and third tab each embed the same view,
 * so that the view is displayed at two places and the nearer one has to be picked. The tabs create
 * their content only when they are activated, so a display request also proves that the containers
 * on the way were asked to reveal it.
 * </p>
 *
 * <p>
 * A further tab holds a stack of drilled-down views, whose frame view holds a tab bar of its own:
 * what such a frame contains is part of no place within the window, and is reached by looking for it
 * within the view the show before it displayed.
 * </p>
 */
public class TestObjectNavigation extends TestCase {

	private static final String ROOT_VIEW = "nav-root.view.xml";

	private static final String ITEM_VIEW = "nav-item.view.xml";

	/** The view the stack of drilled-down views starts with. */
	private static final String HOME_VIEW = "nav-home.view.xml";

	/** A view displayed nowhere in the window, holding a tab bar with the {@link #ITEM_VIEW}. */
	private static final String FRAME_VIEW = "nav-frame.view.xml";

	/** A view displayed in the window but in no frame. */
	private static final String OTHER_VIEW = "nav-other.view.xml";

	/** A view displayed on a tab of the {@link #FRAME_VIEW} and nowhere else. */
	private static final String DETAIL_VIEW = "nav-detail.view.xml";

	/** A view file that exists but that no reference reaches. */
	private static final String ORPHAN_VIEW = "nav-orphan.view.xml";

	private static final String ITEM_CHANNEL = "item";

	/** The channel of the root view holding the path of the stack of drilled-down views. */
	private static final String PATH_CHANNEL = "navPath";

	private static final String TAB_FIRST = "first";

	private static final String TAB_SECOND = "second";

	private static final String TAB_THIRD = "third";

	/** The tab holding the stack of drilled-down views. */
	private static final String TAB_STACK = "stack";

	/** The tab of the {@link #FRAME_VIEW} embedding the {@link #ITEM_VIEW}. */
	private static final String FRAME_TAB_DETAIL = "detail";

	/** The tab of the {@link #FRAME_VIEW} embedding the {@link #DETAIL_VIEW}. */
	private static final String FRAME_TAB_INNER = "inner";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private TLClass _type;

	private Supplier<ViewMounts> _mounts;

	private ViewContext _root;

	private TabBarElement _tabBar;

	private TileStackElement _stack;

	private TabBarElement _frameTabBar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-object-navigation").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String view : new String[] { ROOT_VIEW, ITEM_VIEW, HOME_VIEW, FRAME_VIEW, DETAIL_VIEW,
			OTHER_VIEW, ORPHAN_VIEW }) {
			copyFixture(view, new File(views, view));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test.navigation");
		_type = TLModelUtil.addClass(module, "Shown");

		ViewMounts mounts = ViewMounts.forRootView(ROOT_VIEW);
		_mounts = () -> mounts;

		ViewElement rootView = ViewLoader.getOrLoadView(ViewLoader.fullPath(ROOT_VIEW));
		_root = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test")));
		rootView.createControl(_root);
		_tabBar = (TabBarElement) ((ChildGroup.Elements) rootView.getChildGroups().get(0)).children().get(0);
		_stack = (TileStackElement) tabContent(_tabBar, TAB_STACK);

		ViewElement frameView = ViewLoader.getOrLoadView(ViewLoader.fullPath(FRAME_VIEW));
		_frameTabBar = (TabBarElement) ((ChildGroup.Elements) frameView.getChildGroups().get(0)).children().get(0);
	}

	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Displaying an object opens the tab holding the view of its type and writes the object into the
	 * view's channel.
	 */
	public void testShowInMountedView() {
		TLObject object = newObject();
		Recorder chain = new Recorder();

		ObjectNavigation.show(_root, targets(showing(ITEM_VIEW)), object, chain);

		assertEquals("The tab holding the view is displayed.", TAB_SECOND, activeTab());
		assertSame("The view displayed there received the object.", object, itemOf(TAB_SECOND));
		assertEquals("The chain continued with the displayed object.", List.of(object), chain._resumed);
	}

	/**
	 * A view displayed at two places is opened at the one nearest to where the request comes from.
	 */
	public void testNearestMountWins() {
		TLObject object = newObject();
		ViewContext caller = _root.withScope(RevealPath.class, RevealPath.ROOT.append(_tabBar, TAB_THIRD));

		ObjectNavigation.show(caller, targets(showing(ITEM_VIEW)), object, new Recorder());

		assertEquals("The mount under the caller wins over the one declared first.",
			TAB_THIRD, activeTab());
		assertSame(object, itemOf(TAB_THIRD));
		assertNull("The other place stayed untouched.", instanceOf(ITEM_VIEW, TAB_SECOND));
	}

	/**
	 * Without a caller, the place found first is opened.
	 */
	public void testFirstMountWithoutHint() {
		TLObject object = newObject();

		ObjectNavigation.show(_root, targets(showing(ITEM_VIEW)), object, new Recorder());

		assertEquals(TAB_SECOND, activeTab());
	}

	/**
	 * A view sitting on a tab of a frame drilled down to is revealed on that tab, rather than being
	 * drilled down to itself.
	 */
	public void testShowInTabOfDrilledDownFrame() {
		TLObject object = newObject();
		Recorder chain = new Recorder();

		ObjectNavigation.show(_root,
			targets(target(plainShow(HOME_VIEW), plainShow(FRAME_VIEW), itemShow(ITEM_VIEW))), object, chain);

		assertEquals("The tab holding the stack is displayed.", TAB_STACK, activeTab());
		assertEquals("The frame alone was pushed: what it holds was revealed within it, not drilled"
			+ " down to as a further frame.", List.of(FRAME_VIEW), pushedViews());
		assertEquals("The tab of the frame holding the view is displayed.",
			FRAME_TAB_DETAIL, frameActiveTab());
		assertSame("The view on that tab received the object.", object,
			itemOf(frameInstance(ITEM_VIEW, FRAME_TAB_DETAIL)));
		assertEquals("The chain continued with the displayed object.", List.of(object), chain._resumed);
	}

	/**
	 * A view that only the drilled-down frame holds is revealed on its tab of the frame.
	 *
	 * <p>
	 * The view is displayed at no place within the window at all, so it is found solely by looking
	 * within the frame the show before it displayed: without that, it would be drilled down to as a
	 * second frame, which is what the length of the stack's path rules out.
	 * </p>
	 */
	public void testShowViewOnlyInsideTheFrame() {
		TLObject object = newObject();
		Recorder chain = new Recorder();

		ObjectNavigation.show(_root,
			targets(target(plainShow(HOME_VIEW), plainShow(FRAME_VIEW), itemShow(DETAIL_VIEW))), object, chain);

		assertEquals("The tab holding the stack is displayed.", TAB_STACK, activeTab());
		assertEquals("The frame alone was pushed: the view it holds was revealed within it, not"
			+ " drilled down to as a further frame.", List.of(FRAME_VIEW), pushedViews());
		assertEquals("The tab of the frame holding the view is displayed.",
			FRAME_TAB_INNER, frameActiveTab());
		assertSame("The view on that tab received the object.", object,
			itemOf(frameInstance(DETAIL_VIEW, FRAME_TAB_INNER)));
		assertEquals("The chain continued with the displayed object.", List.of(object), chain._resumed);
	}

	/**
	 * A view the frame does not hold is still revealed at its place within the window.
	 */
	public void testFallBackToPlaceInWindow() {
		TLObject object = newObject();

		ObjectNavigation.show(_root,
			targets(target(plainShow(HOME_VIEW), plainShow(FRAME_VIEW), itemShow(OTHER_VIEW))), object,
			new Recorder());

		assertEquals("The tab holding the view is displayed.", TAB_FIRST, activeTab());
		assertSame("The view displayed there received the object.", object,
			itemOf(instanceOf(OTHER_VIEW, TAB_FIRST)));
		assertEquals("The view was revealed where the window displays it, not drilled down to.",
			List.of(FRAME_VIEW), pushedViews());
	}

	/**
	 * Only a show following the one that displayed the frame looks inside it: on its own, a view is
	 * looked for within the window.
	 */
	public void testFrameContentNeedsThePrecedingShow() {
		ObjectNavigation.show(_root, targets(target(plainShow(HOME_VIEW), plainShow(FRAME_VIEW))),
			newObject(), new Recorder());
		assertEquals(List.of(FRAME_VIEW), pushedViews());

		TLObject object = newObject();
		ObjectNavigation.show(_root, targets(showing(ITEM_VIEW)), object, new Recorder());

		assertEquals("The view was displayed where the window holds it.", TAB_SECOND, activeTab());
		assertSame(object, itemOf(instanceOf(ITEM_VIEW, TAB_SECOND)));
		assertNull("The tab of the frame was not opened.",
			frameInstance(ITEM_VIEW, FRAME_TAB_DETAIL));
	}

	/**
	 * An object of a type the application does not display is reported.
	 */
	public void testMissingTarget() {
		TLObject object = newObject();

		try {
			ObjectNavigation.show(_root, targets(), object, new Recorder());
			fail("A type without a display target cannot be displayed.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("Shown"));
		}
	}

	/**
	 * A view that is displayed nowhere is a drilled-down frame, which needs a stack to be pushed
	 * onto.
	 */
	public void testUnmountedViewNeedsStack() {
		TLObject object = newObject();

		try {
			ObjectNavigation.show(_root, targets(showing(ORPHAN_VIEW)), object, new Recorder());
			fail("Nothing here holds a stack of drilled-down views.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(ORPHAN_VIEW));
		}
	}

	/**
	 * Nothing to display passes the surrounding command chain on unchanged.
	 */
	public void testNoInputPassesThrough() {
		ShowObjectAction action = newShowObjectAction();
		Recorder chain = new Recorder();

		action.execute(_root, null, chain);

		assertEquals(Collections.singletonList(null), chain._resumed);
		assertFalse(chain._aborted);
	}

	/**
	 * An empty selection is nothing to display either.
	 */
	public void testEmptySelectionPassesThrough() {
		ShowObjectAction action = newShowObjectAction();
		Recorder chain = new Recorder();
		List<Object> selection = List.of();

		action.execute(_root, selection, chain);

		assertEquals(List.of(selection), chain._resumed);
	}

	/**
	 * The id of the tab the tab bar currently displays.
	 */
	private String activeTab() {
		return ((ReactTabBarControl) registry().getContainer(_tabBar, RevealPath.ROOT)).getActiveTabId();
	}

	/**
	 * The active tab of the tab bar the frame at the bottom of the stack holds.
	 */
	private String frameActiveTab() {
		return ((ReactTabBarControl) registry().getContainer(_frameTabBar, framePlace())).getActiveTabId();
	}

	/**
	 * The instance of the given view file displayed on the given tab of the frame at the bottom of
	 * the stack, {@code null} while that tab was never opened.
	 */
	private ViewContext frameInstance(String viewRef, String tabId) {
		return registry().getView(viewRef, framePlace().append(_frameTabBar, tabId));
	}

	/**
	 * The place of the frame at the bottom of the stack.
	 */
	private RevealPath framePlace() {
		return RevealPath.ROOT.append(_tabBar, TAB_STACK).append(_stack, ReactTileStackControl.frameKey(0));
	}

	/**
	 * The views the stack was drilled down to, in the order they were pushed.
	 */
	private List<String> pushedViews() {
		Object path = _root.resolveChannel(new ChannelRef(PATH_CHANNEL)).get();
		if (path == null) {
			return List.of();
		}
		return ((List<?>) path).stream().map(frame -> ((TileFrame) frame).getViewRef()).toList();
	}

	/**
	 * The value the view embedded in the given tab holds in its {@link #ITEM_CHANNEL}.
	 */
	private Object itemOf(String tabId) {
		return itemOf(instanceOf(ITEM_VIEW, tabId));
	}

	/**
	 * The value the given view instance holds in its {@link #ITEM_CHANNEL}.
	 */
	private static Object itemOf(ViewContext instance) {
		assertNotNull("The view is not displayed.", instance);
		return instance.resolveChannel(new ChannelRef(ITEM_CHANNEL)).get();
	}

	/**
	 * The instance of the given view file displayed in the given tab of the root view's tab bar,
	 * {@code null} while the tab was never opened.
	 */
	private ViewContext instanceOf(String viewRef, String tabId) {
		return registry().getView(viewRef, RevealPath.ROOT.append(_tabBar, tabId));
	}

	/**
	 * The single element the given tab of the given tab bar holds.
	 */
	private static UIElement tabContent(TabBarElement tabBar, String tabId) {
		for (ChildGroup group : tabBar.getChildGroups()) {
			if (tabId.equals(group.key())) {
				return ((ChildGroup.Elements) group).children().get(0);
			}
		}
		throw new AssertionError("The tab bar holds no tab '" + tabId + "'.");
	}

	private RevealRegistry registry() {
		return _root.getRevealRegistry();
	}

	private DisplayTargets targets(DisplayTarget... targets) {
		return new DisplayTargets(List.of(targets), _mounts);
	}

	/**
	 * A target displaying the given view with the shown object in its {@link #ITEM_CHANNEL}.
	 */
	private DisplayTarget showing(String viewRef) {
		return target(itemShow(viewRef));
	}

	/**
	 * A target displaying the given views in turn.
	 */
	private DisplayTarget target(ShowStep... shows) {
		return new DisplayTarget(_type, false, List.of(shows));
	}

	/**
	 * Displaying the given view with the shown object in its {@link #ITEM_CHANNEL}.
	 */
	private static ShowStep itemShow(String viewRef) {
		return new ShowStep(viewRef, false, null, null, List.of(new Binding(ITEM_CHANNEL, null)));
	}

	/**
	 * Displaying the given view, which receives nothing.
	 */
	private static ShowStep plainShow(String viewRef) {
		return new ShowStep(viewRef, false, null, null, List.of());
	}

	private TLObject newObject() {
		return TransientObjectFactory.INSTANCE.createObject(_type, null);
	}

	private static ShowObjectAction newShowObjectAction() {
		ShowObjectAction.Config config = TypedConfiguration.newConfigItem(ShowObjectAction.Config.class);
		return (ShowObjectAction) new DefaultInstantiationContext(TestObjectNavigation.class).getInstance(config);
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = TestObjectNavigation.class.getResourceAsStream(name)) {
			assertNotNull("Missing test fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * The {@link Continuation} of a command chain, recording what the display request did with it.
	 */
	private static final class Recorder implements Continuation {

		/** The values the chain was resumed with. */
		final List<Object> _resumed = new ArrayList<>();

		/** Whether the chain was cancelled. */
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
			// Nothing to compensate in a test.
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the session resources the tab labels are
	 * resolved with.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObjectNavigation.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
