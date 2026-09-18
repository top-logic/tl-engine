/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotRegistry;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletResponse;

/**
 * The contributions a slot holds belong to controls the window displays, also when a page is loaded
 * into a display the window already holds.
 *
 * <p>
 * The scenario is the one of an application: an app shell whose app bar holds a
 * {@code <slot name="appbar-content"/>}, and a sidebar of two items whose views each project
 * something into that slot. The item switch is replayed once as the user clicking the sidebar and
 * once as a page loaded on the other item's URL, the way
 * {@link com.top_logic.layout.view.ViewServlet} does it.
 * </p>
 */
public class TestSlotUrlAdoption extends TestCase {

	private static final String ROOT_VIEW = "slot-root.view.xml";

	private static final String FIRST_VIEW = "slot-first.view.xml";

	private static final String SECOND_VIEW = "slot-second.view.xml";

	private static final String INNER_VIEW = "slot-overview.view.xml";

	private static final String DETAIL_VIEW = "slot-detail.view.xml";

	/** The ID of the first sidebar item, which is also the URL naming it. */
	private static final String FIRST_ITEM = "first";

	/** The ID of the second sidebar item, which is also the URL naming it. */
	private static final String SECOND_ITEM = "second";

	/** The URL suffix drilling an item's display into its detail frame. */
	private static final String DETAIL_URL = "/detail";

	private File _webapp;

	private FileManager _fileManagerBefore;

	private ViewContext _viewContext;

	private ReactControl _root;

	private RouteManager _routeManager;

	private SSEUpdateQueue _sseQueue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-slot-adoption").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String view : new String[] { ROOT_VIEW, FIRST_VIEW, SECOND_VIEW, INNER_VIEW, DETAIL_VIEW }) {
			copyFixture(view, new File(views, view));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		ReactContext reactContext =
			new DefaultReactContext("", "test", _sseQueue, new ReactWindowRegistry("test"));
		_routeManager = reactContext.getRouteManager();
		ViewElement rootView = ViewLoader.getOrLoadView(ViewLoader.fullPath(ROOT_VIEW));
		_viewContext = new DefaultViewContext(reactContext, ViewLoader.fullPath(ROOT_VIEW));
		_root = (ReactControl) rootView.createControl(_viewContext);
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Picking the other item in the sidebar leaves only that item's contribution.
	 */
	public void testClick() throws IOException {
		firstPage();
		assertEquals("The displayed item contributes to the app bar.", 1, contributions().size());

		sidebar().selectItem(SECOND_ITEM);

		assertOneDisplayedContribution("after the sidebar switched the item");
	}

	/**
	 * Loading the page on the other item's URL - a URL typed into the address bar of a tab that
	 * already displays the application, or a reload - displays that item, and nothing of the one
	 * left behind.
	 */
	public void testUrlAdoption() throws IOException {
		firstPage();
		assertEquals("The displayed item contributes to the app bar.", 1, contributions().size());

		renderPage(SECOND_ITEM);
		assertEquals("The rendered page displays only the item its URL names: " + describe(),
			1, contributions().size());

		openEventStream();

		assertOneDisplayedContribution("after the loaded page opened its event stream");
	}

	/**
	 * The same, for a display drilled one frame deep in each item - the scenario of the ticket.
	 */
	public void testUrlAdoptionOfDrilledItems() throws IOException {
		loadPage(FIRST_ITEM + DETAIL_URL);
		sidebar().selectItem(SECOND_ITEM);
		loadPage(SECOND_ITEM + DETAIL_URL);

		loadPage(FIRST_ITEM + DETAIL_URL);

		assertOneDisplayedContribution("after the drilled display was loaded on the other item's URL");
	}

	/**
	 * A deep link into a display built for it: the item the URL names is the only one contributing.
	 */
	public void testDeepLink() throws IOException {
		loadPage(SECOND_ITEM);

		assertOneDisplayedContribution("after a deep link was adopted by a fresh display");
	}

	/**
	 * Renders the page the window displays when a URL is entered into its address bar.
	 */
	private void firstPage() throws IOException {
		loadPage("");
	}

	/**
	 * Loads a page displaying the given URL into the tree the window holds, the way
	 * {@link com.top_logic.layout.view.ViewServlet} does.
	 *
	 * <p>
	 * The page being left is unloaded first, which detaches the tree the way
	 * {@link com.top_logic.layout.react.window.ReactWindowRegistry#windowUnloaded(String)} does, then
	 * the URL is adopted and the tree rendered, and finally the loaded page opens its event stream,
	 * which hands it the state of every registered control - the SSE endpoint of
	 * {@link com.top_logic.layout.react.servlet.ReactServlet} calling
	 * {@link SSEUpdateQueue#setConnection}.
	 * </p>
	 */
	private void loadPage(String url) throws IOException {
		renderPage(url);
		openEventStream();
	}

	/**
	 * Unloads the page being left and renders the tree the window holds for the given URL, the way
	 * {@link com.top_logic.layout.view.ViewServlet} re-renders a display for an adopted URL.
	 */
	private void renderPage(String url) throws IOException {
		_root.detach();

		_sseQueue.discardPendingEvents();
		_routeManager.adoptUrl(url);
		_root.attach();
		_routeManager.resolvePending();
		_root.write(new TagWriter());
		_routeManager.finishAdoption();
	}

	/**
	 * The loaded page opens its event stream, which hands it the state of every registered control -
	 * the SSE endpoint of {@link com.top_logic.layout.react.servlet.ReactServlet} calling
	 * {@link SSEUpdateQueue#setConnection}.
	 */
	private void openEventStream() {
		_sseQueue.setConnection(sseConnection());
	}

	/**
	 * Every registered contribution belongs to a control the display contains, and the app bar holds
	 * exactly one.
	 */
	private void assertOneDisplayedContribution(String step) {
		Set<ReactControl> displayed = Collections.newSetFromMap(new IdentityHashMap<>());
		collect(_root, displayed);
		for (SlotContribution contribution : contributions()) {
			assertTrue(step + ": a contribution of a control that is not displayed survives: "
				+ describe(), displayed.contains(contribution));
		}
		assertEquals(step + ": " + describe(), 1, contributions().size());
	}

	private static void collect(ReactControl control, Set<ReactControl> result) {
		if (!result.add(control)) {
			return;
		}
		for (ReactControl child : control.displayedChildren()) {
			collect(child, result);
		}
	}

	private ReactSidebarControl sidebar() {
		return findSidebar(_root);
	}

	private static ReactSidebarControl findSidebar(ReactControl control) {
		if (control instanceof ReactSidebarControl sidebar) {
			return sidebar;
		}
		for (ReactControl child : control.displayedChildren()) {
			ReactSidebarControl found = findSidebar(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private SlotRegistry registry() {
		return _viewContext.getSlotRegistry();
	}

	private List<SlotContribution> contributions() {
		return registry().getContributions();
	}

	/**
	 * The registered contributions, each named by the controls it carries, plus the number of
	 * placeholders they are routed to.
	 */
	private String describe() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("sidebar item: ");
		buffer.append(sidebar().activeRouteSegment() == null ? "-" : sidebar().activeRouteSegment().path());
		buffer.append(", contributions: ");
		for (SlotContribution contribution : contributions()) {
			buffer.append(contribution.getSlotName());
			buffer.append(contribution.getControls().stream().map(ReactControl::getID).toList());
			buffer.append(((ReactControl) contribution).isAttached() ? "(attached)" : "(detached)");
			buffer.append(' ');
		}
		buffer.append(", placeholders: ");
		buffer.append(registry().getPlaceholders().size());
		return buffer.toString();
	}

	/**
	 * The event stream of a loaded page: an {@link AsyncContext} whose response collects what is
	 * written to it.
	 */
	private static AsyncContext sseConnection() {
		StringWriter sink = new StringWriter();
		PrintWriter writer = new PrintWriter(sink);
		ServletResponse response = (ServletResponse) Proxy.newProxyInstance(
			TestSlotUrlAdoption.class.getClassLoader(), new Class<?>[] { ServletResponse.class },
			(proxy, method, args) -> "getWriter".equals(method.getName()) ? writer : null);
		return (AsyncContext) Proxy.newProxyInstance(
			TestSlotUrlAdoption.class.getClassLoader(), new Class<?>[] { AsyncContext.class },
			(proxy, method, args) -> "getResponse".equals(method.getName()) ? response : null);
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = TestSlotUrlAdoption.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the session resources the labels are resolved
	 * with, plus the {@link SchedulerService} the event stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSlotUrlAdoption.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
