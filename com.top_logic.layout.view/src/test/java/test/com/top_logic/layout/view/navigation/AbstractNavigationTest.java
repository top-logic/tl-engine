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
import java.util.List;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
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
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.layout.view.tiles.ReactTileStackControl;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileStackElement;

/**
 * A window displaying the view files next to this class, as the scenario of a display request.
 *
 * <p>
 * The root view holds a tab bar whose second and third tab each embed the same view, so that the
 * view is displayed at two places and the nearer one has to be picked. The tabs create their
 * content only when they are activated, so a display request also proves that the containers on the
 * way were asked to reveal it.
 * </p>
 *
 * <p>
 * A further tab holds a stack of drilled-down views, whose frame view holds a tab bar of its own:
 * what such a frame contains is part of no place within the window, and is reached by looking for
 * it within the view the show before it displayed.
 * </p>
 *
 * @implNote Runs each test within an interaction, which the TL-Script of a binding is compiled in.
 */
public abstract class AbstractNavigationTest extends BasicTestCase {

	/** The view file the window displays. */
	protected static final String ROOT_VIEW = "nav-root.view.xml";

	/** A view displayed on two tabs of the window and on a tab of the {@link #FRAME_VIEW}. */
	protected static final String ITEM_VIEW = "nav-item.view.xml";

	/** The view the stack of drilled-down views starts with. */
	protected static final String HOME_VIEW = "nav-home.view.xml";

	/** A view displayed nowhere in the window, holding a tab bar with the {@link #ITEM_VIEW}. */
	protected static final String FRAME_VIEW = "nav-frame.view.xml";

	/** A view displayed in the window but in no frame. */
	protected static final String OTHER_VIEW = "nav-other.view.xml";

	/** A view displayed on a tab of the {@link #FRAME_VIEW} and nowhere else. */
	protected static final String DETAIL_VIEW = "nav-detail.view.xml";

	/** A view file that exists but that no reference reaches. */
	protected static final String ORPHAN_VIEW = "nav-orphan.view.xml";

	/** The channel receiving what a view displays. */
	protected static final String ITEM_CHANNEL = "item";

	/** A second channel of the {@link #ITEM_VIEW}. */
	protected static final String RAW_CHANNEL = "raw";

	/** The channel of the root view holding the path of the stack of drilled-down views. */
	protected static final String PATH_CHANNEL = "navPath";

	/** The tab holding the {@link #OTHER_VIEW}. */
	protected static final String TAB_FIRST = "first";

	/** The first tab holding the {@link #ITEM_VIEW}. */
	protected static final String TAB_SECOND = "second";

	/** The second tab holding the {@link #ITEM_VIEW}. */
	protected static final String TAB_THIRD = "third";

	/** The tab holding the stack of drilled-down views. */
	protected static final String TAB_STACK = "stack";

	/** The tab of the {@link #FRAME_VIEW} embedding the {@link #ITEM_VIEW}. */
	protected static final String FRAME_TAB_DETAIL = "detail";

	/** The tab of the {@link #FRAME_VIEW} embedding the {@link #DETAIL_VIEW}. */
	protected static final String FRAME_TAB_INNER = "inner";

	/** The view files of the scenario. */
	private static final String[] FIXTURES = {
		ROOT_VIEW, ITEM_VIEW, HOME_VIEW, FRAME_VIEW, DETAIL_VIEW, OTHER_VIEW, ORPHAN_VIEW };

	private File _webapp;

	private FileManager _fileManagerBefore;

	/** The window's root display, where a display request comes from. */
	protected ViewContext _root;

	/** The tab bar of the root view. */
	protected TabBarElement _tabBar;

	/** The stack of drilled-down views on the {@link #TAB_STACK} tab. */
	protected TileStackElement _stack;

	/** The tab bar the {@link #FRAME_VIEW} holds. */
	protected TabBarElement _frameTabBar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-object-navigation").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String view : FIXTURES) {
			copyFixture(view, new File(views, view));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		ViewElement rootView = ViewLoader.getOrLoadView(ViewLoader.fullPath(ROOT_VIEW));
		_root = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), ROOT_VIEW);
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
	 * The id of the tab the window's tab bar currently displays.
	 */
	protected String activeTab() {
		return ((ReactTabBarControl) registry().getContainer(_tabBar, RevealPath.ROOT)).getActiveTabId();
	}

	/**
	 * The active tab of the tab bar the frame at the bottom of the stack holds.
	 */
	protected String frameActiveTab() {
		return ((ReactTabBarControl) registry().getContainer(_frameTabBar, framePlace())).getActiveTabId();
	}

	/**
	 * The instance of the given view file displayed on the given tab of the frame at the bottom of
	 * the stack, {@code null} while that tab was never opened.
	 */
	protected ViewContext frameInstance(String viewRef, String tabId) {
		return registry().getView(viewRef, framePlace().append(_frameTabBar, tabId));
	}

	/**
	 * The place of the frame at the bottom of the stack.
	 */
	protected RevealPath framePlace() {
		return RevealPath.ROOT.append(_tabBar, TAB_STACK).append(_stack, ReactTileStackControl.frameKey(0));
	}

	/**
	 * The views the stack was drilled down to, in the order they were pushed.
	 */
	protected List<String> pushedViews() {
		Object path = _root.resolveChannel(new ChannelRef(PATH_CHANNEL)).get();
		if (path == null) {
			return List.of();
		}
		return ((List<?>) path).stream().map(frame -> ((TileFrame) frame).getViewRef()).toList();
	}

	/**
	 * The value the view embedded in the given tab of the window holds in its
	 * {@link #ITEM_CHANNEL}.
	 */
	protected Object itemOf(String tabId) {
		return itemOf(instanceOf(ITEM_VIEW, tabId));
	}

	/**
	 * The value the given view instance holds in its {@link #ITEM_CHANNEL}.
	 */
	protected static Object itemOf(ViewContext instance) {
		return channelOf(instance, ITEM_CHANNEL);
	}

	/**
	 * The value the given view instance holds in the given channel.
	 */
	protected static Object channelOf(ViewContext instance, String channel) {
		assertNotNull("The view is not displayed.", instance);
		return instance.resolveChannel(new ChannelRef(channel)).get();
	}

	/**
	 * The instance of the given view file displayed in the given tab of the window's tab bar,
	 * {@code null} while the tab was never opened.
	 */
	protected ViewContext instanceOf(String viewRef, String tabId) {
		return registry().getView(viewRef, RevealPath.ROOT.append(_tabBar, tabId));
	}

	/**
	 * What the window displays, and where.
	 */
	protected RevealRegistry registry() {
		return _root.getRevealRegistry();
	}

	/**
	 * The single element the given tab of the given tab bar holds.
	 */
	protected static UIElement tabContent(TabBarElement tabBar, String tabId) {
		for (ChildGroup group : tabBar.getChildGroups()) {
			if (tabId.equals(group.key())) {
				return ((ChildGroup.Elements) group).children().get(0);
			}
		}
		throw new AssertionError("The tab bar holds no tab '" + tabId + "'.");
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = AbstractNavigationTest.class.getResourceAsStream(name)) {
			assertNotNull("Missing test fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * The {@link Continuation} of a command chain, recording what the display request did with it.
	 */
	protected static final class Recorder implements Continuation {

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
}
