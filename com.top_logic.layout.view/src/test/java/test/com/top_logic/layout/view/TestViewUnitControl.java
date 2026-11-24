/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.responsive.DisplayClass;
import com.top_logic.layout.responsive.DisplayClassModel;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewUnitControl;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.Resources;

/**
 * Tests that a unit of the view failing while it is built replaces only itself with a placeholder,
 * and that the placeholder builds the unit again on request - for a referenced view, and for
 * contents built on demand: a navigation item, a tab, and the detail of a master-detail element
 * built anew after its selection changed.
 */
public class TestViewUnitControl extends BasicTestCase {

	/** The view embedding a working element and a view that fails while it is built. */
	private static final String OUTER_VIEW = "unit-outer.view.xml";

	/** The view referenced by {@link #OUTER_VIEW}, containing the {@link #BROKEN} probe. */
	private static final String BROKEN_VIEW = "unit-broken.view.xml";

	/** The view embedding a working element and a view file that does not exist. */
	private static final String MISSING_REF_VIEW = "unit-missing-ref.view.xml";

	/** The view file that does not exist. */
	private static final String MISSING_VIEW = "unit-missing.view.xml";

	/** A sidebar whose active start item fails while it is built. */
	private static final String SIDEBAR_VIEW = "unit-sidebar.view.xml";

	/** A tab bar whose active tab fails while it is built. */
	private static final String TABS_VIEW = "unit-tabs.view.xml";

	/** A master-detail element whose detail fails while it is built. */
	private static final String ADAPTIVE_VIEW = "unit-adaptive.view.xml";

	/** The selection channel of {@link #ADAPTIVE_VIEW}. */
	private static final String SELECTED = "selected";

	/** Name of the probe embedded directly in the outer views. */
	private static final String SIBLING = "sibling";

	/** Name of the probe in {@link #BROKEN_VIEW}. */
	private static final String BROKEN = "broken";

	/** The message of the failure of a {@link UnitProbe}. */
	private static final String FAILURE_MESSAGE = "Probe failure.";

	/** Names of the {@link UnitProbe}s failing when built. */
	static final Set<String> FAILING = new HashSet<>();

	/** Names of the {@link UnitProbe}s built successfully, in build order. */
	static final List<String> BUILT = new ArrayList<>();

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private ReactControl _root;

	private ViewContext _viewContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		FAILING.clear();
		BUILT.clear();

		_webapp = Files.createTempDirectory("tl-view-unit").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		write(views, OUTER_VIEW, "<view><stack>"
			+ "<test-unit-probe name='" + SIBLING + "'/>"
			+ "<view-ref view='" + BROKEN_VIEW + "'/>"
			+ "</stack></view>");
		write(views, BROKEN_VIEW, "<view><test-unit-probe name='" + BROKEN + "'/></view>");
		write(views, MISSING_REF_VIEW, "<view><stack>"
			+ "<test-unit-probe name='" + SIBLING + "'/>"
			+ "<view-ref view='" + MISSING_VIEW + "'/>"
			+ "</stack></view>");
		write(views, SIDEBAR_VIEW, "<view><sidebar active-item='" + BROKEN + "'><items>"
			+ "<nav-item id='" + BROKEN + "'><label><en>Broken</en></label>"
			+ "<test-unit-probe name='" + BROKEN + "'/></nav-item>"
			+ "<nav-item id='" + SIBLING + "'><label><en>Sibling</en></label>"
			+ "<test-unit-probe name='" + SIBLING + "'/></nav-item>"
			+ "</items></sidebar></view>");
		write(views, TABS_VIEW, "<view><tab-bar active-tab='" + BROKEN + "'><tabs>"
			+ "<tab id='" + BROKEN + "'><label><en>Broken</en></label>"
			+ "<test-unit-probe name='" + BROKEN + "'/></tab>"
			+ "<tab id='" + SIBLING + "'><label><en>Sibling</en></label>"
			+ "<test-unit-probe name='" + SIBLING + "'/></tab>"
			+ "</tabs></tab-bar></view>");
		write(views, ADAPTIVE_VIEW, "<view><channels><channel name='" + SELECTED + "'/></channels>"
			+ "<adaptive-detail selection='" + SELECTED + "'>"
			+ "<selector><test-unit-probe name='" + SIBLING + "'/></selector>"
			+ "<detail><test-unit-probe name='" + BROKEN + "'/></detail>"
			+ "</adaptive-detail></view>");
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
	}

	@Override
	protected void tearDown() throws Exception {
		if (_root != null) {
			_root.cleanupTree();
			_root = null;
		}
		_viewContext = null;
		_sseQueue.shutdown();
		_sseQueue = null;
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);
		FAILING.clear();
		BUILT.clear();
		super.tearDown();
	}

	/**
	 * Tests that a working view is shown without a placeholder.
	 */
	public void testWorkingViewHasNoPlaceholder() {
		_root = build(OUTER_VIEW);

		assertEquals(List.of(SIBLING, BROKEN), BUILT);
		assertNull("A view built without failure shows no placeholder.", find(_root, ReactAlertControl.class));
	}

	/**
	 * Tests that a view failing while it is built is replaced by the placeholder, while the
	 * enclosing view is built and rendered. A user who is no administrator does not see the reason.
	 */
	public void testFailingViewIsReplacedByPlaceholder() {
		FAILING.add(BROKEN);

		_root = buildAsUser(OUTER_VIEW);
		render();

		assertEquals("The enclosing view is built.", List.of(SIBLING), BUILT);
		ReactAlertControl alert = find(_root, ReactAlertControl.class);
		assertNotNull("The failing view is replaced by a placeholder.", alert);
		assertEquals(Variant.ERROR, alert.getVariant());
		assertEquals(Resources.getInstance().getString(I18NConstants.ERROR_VIEW_NOT_DISPLAYED), alert.getTitle());
		assertFalse("A user who is no administrator does not see the reason: " + alert.getMessage(),
			alert.getMessage().contains(FAILURE_MESSAGE));
		assertFalse("A user who is no administrator does not see the view: " + alert.getMessage(),
			alert.getMessage().contains(BROKEN_VIEW));
		assertEquals("The placeholder offers to try again.", 1, alert.actions().size());
	}

	/**
	 * Tests that an administrator sees the view and the reason of the failure in the placeholder.
	 */
	public void testAdministratorSeesTheReason() {
		FAILING.add(BROKEN);

		_root = ThreadContext.withoutSecurityChecks(() -> {
			return build(OUTER_VIEW);
		});

		ReactAlertControl alert = find(_root, ReactAlertControl.class);
		assertNotNull(alert);
		assertTrue("An administrator sees the reason: " + alert.getMessage(),
			alert.getMessage().contains(FAILURE_MESSAGE));
		assertTrue("An administrator sees the view: " + alert.getMessage(),
			alert.getMessage().contains(BROKEN_VIEW));
	}

	/**
	 * Tests that trying again builds the view once its failure is gone, and shows the placeholder
	 * again as long as it persists.
	 */
	public void testRetryBuildsTheView() {
		FAILING.add(BROKEN);
		_root = build(OUTER_VIEW);
		render();

		retry(find(_root, ReactAlertControl.class));
		assertEquals("A view still failing is not built.", List.of(SIBLING), BUILT);
		ReactAlertControl alert = find(_root, ReactAlertControl.class);
		assertNotNull("A view still failing shows the placeholder again.", alert);

		FAILING.clear();
		retry(alert);
		render();

		assertEquals("Trying again builds the view.", List.of(SIBLING, BROKEN), BUILT);
		assertNull("The rebuilt view replaces the placeholder.", find(_root, ReactAlertControl.class));
		assertTrue("The replaced placeholder is disposed.", alert.isDisposed());
	}

	/**
	 * Tests that a view that cannot be loaded is shown as the load error placeholder: naming the
	 * view and the reason to everybody, without offering to try again.
	 */
	public void testLoadErrorShowsLoadPlaceholder() {
		_root = buildAsUser(MISSING_REF_VIEW);
		render();

		assertEquals(List.of(SIBLING), BUILT);
		ReactAlertControl alert = find(_root, ReactAlertControl.class);
		assertNotNull("The view that cannot be loaded is replaced by a placeholder.", alert);
		assertEquals(Resources.getInstance().getString(I18NConstants.ERROR_VIEW_NOT_LOADED), alert.getTitle());
		assertTrue("The placeholder names the view: " + alert.getMessage(),
			alert.getMessage().contains(MISSING_VIEW));
		assertTrue("A load error offers no retry.", alert.actions().isEmpty());
	}

	/**
	 * Tests that a navigation item failing while it is built as the active start item leaves the
	 * window intact: the state of the window is written completely, with the placeholder in place of
	 * the item's content, and the other items work.
	 */
	public void testFailingStartItemOfSidebar() {
		FAILING.add(BROKEN);
		_root = build(SIDEBAR_VIEW);
		render();

		writtenState();
		assertNotNull("The failing item shows a placeholder.", find(_root, ReactAlertControl.class));

		find(_root, ReactSidebarControl.class).selectItem(SIBLING);
		writtenState();
		assertEquals("Another item works.", List.of(SIBLING), BUILT);
		assertNull("The other item shows its content.", find(_root, ReactAlertControl.class));
	}

	/**
	 * Tests that a tab failing while it is built as the active tab leaves the window intact: the
	 * state of the window is written completely, and the other tabs work.
	 */
	public void testFailingActiveTab() {
		FAILING.add(BROKEN);
		_root = build(TABS_VIEW);
		render();

		writtenState();
		assertNotNull("The failing tab shows a placeholder.", find(_root, ReactAlertControl.class));

		find(_root, ReactTabBarControl.class).selectTab(SIBLING);
		writtenState();
		assertEquals("Another tab works.", List.of(SIBLING), BUILT);
		assertNull("The other tab shows its content.", find(_root, ReactAlertControl.class));
	}

	/**
	 * Tests that a detail of a master-detail element failing while it is built anew after the
	 * selection changed replaces only the detail, and is built on request once the failure is gone.
	 */
	public void testFailingDetailAfterSelectionChange() {
		DisplayClassModel display = DisplayClassModel.forCurrentSubSession();
		DisplayClass displayBefore = display.getDisplayClass();
		// On a narrow viewport, a selection change replaces the selector by the detail.
		display.setDisplayClass(DisplayClass.COMPACT);
		try {
			FAILING.add(BROKEN);
			_root = build(ADAPTIVE_VIEW);
			render();
			assertEquals(List.of(SIBLING), BUILT);

			ViewChannel selection = _viewContext.resolveChannel(new ChannelRef(SELECTED));
			selection.set("item");
			writtenState();
			ReactAlertControl alert = find(_root, ReactAlertControl.class);
			assertNotNull("The failing detail shows a placeholder.", alert);
			assertEquals(List.of(SIBLING), BUILT);

			FAILING.clear();
			retry(alert);
			writtenState();
			assertEquals("Trying again builds the detail.", List.of(SIBLING, BROKEN), BUILT);
			assertNull(find(_root, ReactAlertControl.class));

			selection.set(null);
			writtenState();
			assertEquals("The selector is shown again.", List.of(SIBLING, BROKEN, SIBLING), BUILT);
		} finally {
			display.setDisplayClass(displayBefore);
		}
	}

	/**
	 * The state of the window, as written to the client: it must be a complete JSON document.
	 */
	private Map<?, ?> writtenState() {
		try {
			return (Map<?, ?>) JSON.fromString(_root.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state of the window is not written completely.", ex);
		}
	}

	private ReactControl build(String view) {
		String path = ViewLoader.fullPath(view);
		ReactContext reactContext = new PageContext(_sseQueue);
		ViewContext viewContext = new DefaultViewContext(reactContext, path);
		_viewContext = viewContext;
		try {
			return ViewUnitControl.forView(viewContext, path, ViewLoader.getOrLoadView(path));
		} catch (Exception ex) {
			throw new AssertionError("Cannot load the view.", ex);
		}
	}

	/**
	 * Builds the view with the security checks of a user who is no administrator: the test itself
	 * runs in a system context.
	 */
	private ReactControl buildAsUser(String view) {
		return ThreadContext.withSecurityChecks(true, () -> {
			assertFalse("Built for a user who is no administrator.", ThreadContext.isAdmin());
			return build(view);
		});
	}

	private void render() {
		_root.attach();
		try {
			_root.write(new TagWriter());
		} catch (IOException ex) {
			throw new AssertionError("Cannot render the view.", ex);
		}
	}

	private static void retry(ReactAlertControl alert) {
		ReactButtonControl button = (ReactButtonControl) alert.actions().get(0);
		button.executeCommand(ReactButtonControl.CMD_CLICK, Map.of());
	}

	private static <T> T find(ReactControl control, Class<T> kind) {
		if (kind.isInstance(control)) {
			return kind.cast(control);
		}
		for (ReactControl child : control.displayedChildren()) {
			T result = find(child, kind);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private static void write(File dir, String name, String content) throws IOException {
		Files.writeString(new File(dir, name).toPath(), content, StandardCharsets.UTF_8);
	}

	/**
	 * Element standing for any content of a view, failing while it is built when its name is among
	 * the {@link TestViewUnitControl#FAILING} ones.
	 */
	public static class UnitProbe implements UIElement {

		/**
		 * Configuration of a {@link UnitProbe}.
		 */
		@TagName("test-unit-probe")
		public interface Config extends UIElement.Config {

			@Override
			@ClassDefault(UnitProbe.class)
			Class<? extends UIElement> getImplementationClass();

			/**
			 * The name of the probe.
			 */
			@Name("name")
			@Mandatory
			String getName();
		}

		private final String _name;

		/**
		 * Creates a {@link UnitProbe}.
		 */
		@CalledByReflection
		public UnitProbe(InstantiationContext context, Config config) {
			_name = config.getName();
		}

		@Override
		public IReactControl createControl(ViewContext context) {
			if (FAILING.contains(_name)) {
				throw new IllegalStateException(FAILURE_MESSAGE);
			}
			BUILT.add(_name);
			return new ReactStackControl(context, List.of());
		}
	}

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
	 * The suite of tests: the view elements are found through the type index, the placeholder
	 * resolves its texts and its theme icon.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestViewUnitControl.class,
			TypeIndex.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
