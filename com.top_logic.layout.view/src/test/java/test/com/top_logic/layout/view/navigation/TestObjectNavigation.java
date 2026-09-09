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
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
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
 */
public class TestObjectNavigation extends TestCase {

	private static final String ROOT_VIEW = "nav-root.view.xml";

	private static final String ITEM_VIEW = "nav-item.view.xml";

	/** A view file that exists but that no reference reaches. */
	private static final String ORPHAN_VIEW = "nav-orphan.view.xml";

	private static final String ITEM_CHANNEL = "item";

	private static final String TAB_SECOND = "second";

	private static final String TAB_THIRD = "third";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private TLClass _type;

	private Supplier<ViewMounts> _mounts;

	private ViewContext _root;

	private TabBarElement _tabBar;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-object-navigation").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String view : new String[] { ROOT_VIEW, ITEM_VIEW, ORPHAN_VIEW }) {
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
		_root = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue()));
		rootView.createControl(_root);
		_tabBar = (TabBarElement) ((ChildGroup.Elements) rootView.getChildGroups().get(0)).children().get(0);
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
		assertNull("The other place stayed untouched.", instanceOf(TAB_SECOND));
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
	 * The value the view embedded in the given tab holds in its {@link #ITEM_CHANNEL}.
	 */
	private Object itemOf(String tabId) {
		ViewContext instance = instanceOf(tabId);
		assertNotNull("The view of tab '" + tabId + "' is not displayed.", instance);
		return instance.resolveChannel(new ChannelRef(ITEM_CHANNEL)).get();
	}

	/**
	 * The instance of the embedded view displayed in the given tab, {@code null} while the tab was
	 * never opened.
	 */
	private ViewContext instanceOf(String tabId) {
		return registry().getView(ITEM_VIEW, RevealPath.ROOT.append(_tabBar, tabId));
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
		return new DisplayTarget(_type, false,
			List.of(new ShowStep(viewRef, false, null, List.of(new Binding(ITEM_CHANNEL, null)))));
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
