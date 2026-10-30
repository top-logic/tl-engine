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
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotRegistry;
import com.top_logic.model.listen.ModelScope;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletResponse;

/**
 * Base class for tests building the control tree of a view written in {@code .view.xml} fixtures and
 * inspecting how the {@link SlotRegistry} of the window routes its slot contributions.
 *
 * <p>
 * The fixtures named by {@link #fixtures()} are resources beside the test class; they are copied
 * into the view directory of a temporary web application, from which {@link #createView(String)}
 * builds the tree of one of them.
 * </p>
 */
public abstract class AbstractSlotViewTest extends TestCase {

	private File _webapp;

	private FileManager _fileManagerBefore;

	private ReactContext _reactContext;

	private RouteManager _routeManager;

	private SSEUpdateQueue _sseQueue;

	private ViewContext _viewContext;

	private ReactControl _root;

	/**
	 * The names of the {@code .view.xml} fixtures the test builds its views from.
	 */
	protected abstract String[] fixtures();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-slot-test").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String view : fixtures()) {
			copyFixture(view, new File(views, view));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		// The page shows no persistent object, so it reports no ModelScope - which spares the test the
		// knowledge base one is built from.
		_reactContext = new DefaultReactContext("", "test", _sseQueue, new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return null;
			}
		};
		_routeManager = _reactContext.getRouteManager();
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		super.tearDown();
	}

	/**
	 * Builds the control tree of the given fixture, which becomes the {@link #root()} of the window.
	 *
	 * @param view
	 *        One of the {@link #fixtures()}.
	 */
	protected void createView(String view) throws ConfigurationException {
		ViewElement rootView = ViewLoader.getOrLoadView(ViewLoader.fullPath(view));
		_viewContext = new DefaultViewContext(_reactContext, ViewLoader.fullPath(view));
		_root = (ReactControl) rootView.createControl(_viewContext);
	}

	/**
	 * The control tree of the view built by {@link #createView(String)}.
	 */
	protected ReactControl root() {
		return _root;
	}

	/**
	 * The registry routing the slot contributions of the {@link #root()} tree.
	 */
	protected SlotRegistry registry() {
		return _viewContext.getSlotRegistry();
	}

	/**
	 * The contributions currently registered in the {@link #registry()}.
	 */
	protected List<SlotContribution> contributions() {
		return registry().getContributions();
	}

	/**
	 * Loads a page displaying the given URL into the tree the window holds, the way
	 * {@link com.top_logic.layout.view.ViewServlet} does.
	 *
	 * <p>
	 * The page being left is unloaded first, which detaches the tree the way
	 * {@link com.top_logic.layout.react.window.ReactWindowRegistry#windowUnloaded(String, String)} does, then
	 * the URL is adopted and the tree rendered, and finally the loaded page opens its event stream,
	 * which hands it the state of every registered control - the SSE endpoint of
	 * {@link com.top_logic.layout.react.servlet.ReactServlet} calling
	 * {@link SSEUpdateQueue#setConnection}.
	 * </p>
	 */
	protected void loadPage(String url) throws IOException {
		renderPage(url);
		openEventStream();
	}

	/**
	 * Unloads the page being left and renders the tree the window holds for the given URL, the way
	 * {@link com.top_logic.layout.view.ViewServlet} re-renders a display for an adopted URL.
	 */
	protected void renderPage(String url) throws IOException {
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
	protected void openEventStream() {
		_sseQueue.setConnection(sseConnection());
	}

	/**
	 * Adds the given control and every control it displays, transitively, to the given set.
	 */
	protected static void collect(ReactControl control, Set<ReactControl> result) {
		if (!result.add(control)) {
			return;
		}
		for (ReactControl child : control.displayedChildren()) {
			collect(child, result);
		}
	}

	/**
	 * The event stream of a loaded page: an {@link AsyncContext} whose response collects what is
	 * written to it.
	 */
	private static AsyncContext sseConnection() {
		StringWriter sink = new StringWriter();
		PrintWriter writer = new PrintWriter(sink);
		ServletResponse response = (ServletResponse) Proxy.newProxyInstance(
			AbstractSlotViewTest.class.getClassLoader(), new Class<?>[] { ServletResponse.class },
			(proxy, method, args) -> "getWriter".equals(method.getName()) ? writer : null);
		return (AsyncContext) Proxy.newProxyInstance(
			AbstractSlotViewTest.class.getClassLoader(), new Class<?>[] { AsyncContext.class },
			(proxy, method, args) -> "getResponse".equals(method.getName()) ? response : null);
	}

	private void copyFixture(String name, File target) throws IOException {
		try (InputStream in = getClass().getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Test suite for the given test class, requiring the {@link TypeIndex} and the session resources
	 * the labels are resolved with, plus the {@link SchedulerService} the event stream's heartbeat is
	 * scheduled on.
	 */
	protected static Test createSuite(Class<? extends AbstractSlotViewTest> testClass) {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(testClass,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}
}
