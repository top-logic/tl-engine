/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.navigation.Binding;
import com.top_logic.layout.view.navigation.DisplayTarget;
import com.top_logic.layout.view.navigation.DisplayTargets;
import com.top_logic.layout.view.navigation.ObjectNavigation;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.ShowObjectAction;
import com.top_logic.layout.view.navigation.ShowStep;
import com.top_logic.layout.view.navigation.ViewMounts;
import com.top_logic.layout.view.tiles.TileFrame;
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
 */
public class TestObjectNavigation extends AbstractNavigationTest {

	private TLClass _type;

	private Supplier<ViewMounts> _mounts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test.navigation");
		_type = TLModelUtil.addClass(module, "Shown");

		ViewMounts mounts = ViewMounts.forRootView(ROOT_VIEW);
		_mounts = () -> mounts;
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
			targets(target(plainShow(HOME_VIEW), frameShow(FRAME), itemShow(ITEM_VIEW))), object, chain);

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
			targets(target(plainShow(HOME_VIEW), frameShow(FRAME), itemShow(DETAIL_VIEW))), object, chain);

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
			targets(target(plainShow(HOME_VIEW), frameShow(FRAME), itemShow(OTHER_VIEW))), object,
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
		ObjectNavigation.show(_root, targets(target(plainShow(HOME_VIEW), frameShow(FRAME))),
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
	 * A view that is displayed nowhere cannot be revealed: a frame to drill down to is named by its
	 * declaration.
	 */
	public void testUnmountedViewIsNotDisplayed() {
		TLObject object = newObject();

		try {
			ObjectNavigation.show(_root, targets(showing(ORPHAN_VIEW)), object, new Recorder());
			fail("The view is displayed nowhere.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(ORPHAN_VIEW));
		}
	}

	/**
	 * A frame is drilled down to only within a stack.
	 */
	public void testFrameNeedsStack() {
		try {
			ObjectNavigation.show(_root, targets(target(frameShow(FRAME))), newObject(), new Recorder());
			fail("Nothing here holds a stack of drilled-down views.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(FRAME));
		}
	}

	/**
	 * The frames of consecutive steps follow the frame graph of the stack: the path the display
	 * arrives at holds the frames of the declarations the steps name, with the parameters their
	 * bindings compute.
	 */
	public void testFrameChainFollowsGraph() {
		TLObject object = newObject();
		Recorder chain = new Recorder();

		ObjectNavigation.show(_root,
			targets(target(plainShow(HOME_VIEW), frameShow(FRAME), itemFrameShow(ORPHAN_FRAME))), object, chain);

		assertEquals(List.of(FRAME_VIEW, ORPHAN_VIEW), pushedViews());
		List<?> path = (List<?>) _root.resolveChannel(new ChannelRef(PATH_CHANNEL)).get();
		TileFrame orphan = (TileFrame) path.get(1);
		assertEquals(ORPHAN_FRAME, orphan.getFrameId());
		assertEquals(Map.of(ITEM_CHANNEL, object), orphan.getParams());
		assertEquals(List.of(object), chain._resumed);
	}

	/**
	 * A step naming a frame that does not follow the frame of the step before is reported.
	 */
	public void testFrameChainViolatingGraphIsReported() {
		try {
			ObjectNavigation.show(_root, targets(target(plainShow(HOME_VIEW), itemFrameShow(ORPHAN_FRAME))),
				newObject(), new Recorder());
			fail("The orphan frame follows the frame, not the initial view.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(ORPHAN_FRAME));
		}
		assertEquals("Nothing is drilled down to.", List.of(), pushedViews());

		try {
			ObjectNavigation.show(_root, targets(target(plainShow(HOME_VIEW), frameShow(FRAME), frameShow(FRAME))),
				newObject(), new Recorder());
			fail("The frame does not follow itself.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(FRAME));
		}
	}

	/**
	 * A step naming a frame without binding all of its parameters is reported.
	 */
	public void testFrameStepMissingParameterIsReported() {
		try {
			ObjectNavigation.show(_root,
				targets(target(plainShow(HOME_VIEW), frameShow(FRAME), frameShow(ORPHAN_FRAME))), newObject(),
				new Recorder());
			fail("The orphan frame holds the parameter item.");
		} catch (TopLogicException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(ITEM_CHANNEL));
		}
	}

	/**
	 * A display passing through several routes on its way to the object reports one history entry,
	 * for the place it arrives at.
	 */
	public void testDisplayIsOneHistoryEntry() {
		RouteManager routes = _root.getRouteManager();
		Watcher watcher = new Watcher();
		routes.register(watcher);
		// The page is loaded with an address the display reproduces.
		routes.adoptUrl("");
		routes.finishAdoption();
		List<String> urls = new ArrayList<>();
		List<Boolean> replaceFlags = new ArrayList<>();
		routes.setUrlChangeHandler((url, title, replace) -> {
			urls.add(url);
			replaceFlags.add(replace);
		});
		// Drilling down changes the route twice, as revealing an area and then writing the path do.
		_root.resolveChannel(new ChannelRef(PATH_CHANNEL)).addListener((sender, oldValue, newValue) -> {
			watcher.show("area");
			watcher.show("area/frame");
		});

		TLObject object = newObject();
		Recorder chain = new Recorder();
		ObjectNavigation.show(_root,
			targets(target(plainShow(HOME_VIEW), frameShow(FRAME), itemShow(ITEM_VIEW))), object, chain);

		assertEquals("The display took place.", List.of(object), chain._resumed);
		assertEquals(List.of("area/frame"), urls);
		assertEquals(List.of(Boolean.FALSE), replaceFlags);
	}

	/**
	 * A display failing on its way leaves the address bar following the display afterwards.
	 */
	public void testFailedDisplayEndsItsNavigation() {
		RouteManager routes = _root.getRouteManager();
		Watcher watcher = new Watcher();
		routes.register(watcher);
		// The page is loaded with an address the display reproduces.
		routes.adoptUrl("");
		routes.finishAdoption();
		List<String> urls = new ArrayList<>();
		routes.setUrlChangeHandler((url, title, replace) -> urls.add(url));

		try {
			ObjectNavigation.show(_root, targets(showing(ORPHAN_VIEW)), newObject(), new Recorder());
			fail("Nothing here holds a stack of drilled-down views.");
		} catch (TopLogicException expected) {
			// Expected.
		}

		watcher.show("elsewhere");
		assertEquals(List.of("elsewhere"), urls);
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
	 * A participant of the window's routing whose route the test sets.
	 */
	private static final class Watcher implements RoutingParticipant {

		private final List<RouteChangeListener> _listeners = new ArrayList<>();

		private RouteSegment _segment;

		/**
		 * Displays the given route and reports the change.
		 */
		void show(String path) {
			_segment = new RouteSegment(path);
			for (RouteChangeListener listener : new ArrayList<>(_listeners)) {
				listener.onRouteChange(this, _segment);
			}
		}

		@Override
		public List<RoutePattern> declaredRoutes() {
			return List.of();
		}

		@Override
		public void activateRoute(RouteMatch match) {
			// Declares no route.
		}

		@Override
		public RouteSegment activeRouteSegment() {
			return _segment;
		}

		@Override
		public void addRouteChangeListener(RouteChangeListener listener) {
			_listeners.add(listener);
		}

		@Override
		public void removeRouteChangeListener(RouteChangeListener listener) {
			_listeners.remove(listener);
		}
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
	 * Drilling down to the frame of the given declaration, which holds no parameters.
	 */
	private static ShowStep frameShow(String frame) {
		return ShowStep.ofFrame(frame, List.of());
	}

	/**
	 * Drilling down to the frame of the given declaration with the shown object as its
	 * {@link #ITEM_CHANNEL} parameter.
	 */
	private static ShowStep itemFrameShow(String frame) {
		return ShowStep.ofFrame(frame, List.of(new Binding(ITEM_CHANNEL, null)));
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
