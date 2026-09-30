/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.context.DefaultSessionContext;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.layout.react.window.WindowOptions;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Tests for {@link ReactWindowRegistry}.
 */
public class TestReactWindowRegistry extends TestCase {

	/** Session ID for the registries under test; they are never looked up by it here. */
	private static final String SESSION_ID = "testSession";

	public void testOpenWindowCreatesEntry() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);
		WindowOptions options = new WindowOptions().setWidth(1024).setTitle("Test");

		String windowId = registry.openWindow(openerCtx, options);

		assertNotNull(windowId);
		assertTrue("Window ID must start with 'v'", windowId.startsWith("v"));

		WindowEntry entry = registry.getWindow(windowId);
		assertNotNull(entry);
		assertEquals(windowId, entry.getWindowId());
		assertEquals("vOpener", entry.getOpenerWindowId());
		assertEquals(1024, entry.getOptions().getWidth());
		assertFalse(entry.isConnected());
	}

	public void testWindowIdsAreUnique() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext ctx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		String id1 = registry.openWindow(ctx, new WindowOptions());
		String id2 = registry.openWindow(ctx, new WindowOptions());

		assertNotSame(id1, id2);
		assertFalse(id1.equals(id2));
	}

	public void testWindowClosed() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext ctx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		String windowId = registry.openWindow(ctx, new WindowOptions());
		assertNotNull(registry.getWindow(windowId));

		registry.windowClosed(windowId);
		assertNull(registry.getWindow(windowId));
	}

	public void testGetNonexistentWindow() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		assertNull(registry.getWindow("vDoesNotExist"));
	}

	public void testOpenWindowWithProvider() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);

		WindowOptions options = new WindowOptions().setWidth(800).setTitle("Provider Test");

		String windowId = registry.openWindow(openerCtx,
			(ctx, model) -> new ReactControl(ctx, model, "test-module"),
			"testModel", options);

		assertNotNull(windowId);
		WindowEntry entry = registry.getWindow(windowId);
		assertNotNull(entry);
		assertNotNull("Provider must be stored", entry.getControlProvider());
		assertEquals("testModel", entry.getModel());
		assertNull("Root control must be null before ViewServlet builds it",
			entry.getRootControl());

		// Simulate what ViewServlet does: build the tree with the child's context.
		SSEUpdateQueue childQueue = registry.getOrCreateQueue(windowId);
		ReactContext childCtx = new DefaultReactContext("", windowId, childQueue, registry);
		ReactControl builtControl = entry.getControlProvider().createControl(
			childCtx, entry.getModel());
		entry.setRootControl(builtControl);
		// Rendering the page attaches its root, which makes it addressable.
		builtControl.attach();

		assertNotNull("Root control must be set after building", entry.getRootControl());
		// Verify the control is registered on the child's queue, not the opener's.
		assertNotNull("Control must be findable on child queue",
			childQueue.getControl(builtControl.getID()));
		assertNull("Control must NOT be on opener queue",
			openerQueue.getControl(builtControl.getID()));
	}

	/**
	 * A rebuild marks every window of the session and tells each of them to reload.
	 */
	public void testRebuildWindows() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		// The tab the user is working in, and a window opened from it.
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		WindowEntry opener = registry.getWindow("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);
		String openedId = registry.openWindow(openerCtx, new WindowOptions());
		WindowEntry opened = registry.getWindow(openedId);
		SSEUpdateQueue openedQueue = opened.getQueue();

		// The tab displays a tree; the opened window is rendered only once its browser asks.
		opener.setRootControl(new ReactControl(openerCtx, null, "Demo"));
		int openerBefore = openerQueue.pendingEventCount();
		int openedBefore = openedQueue.pendingEventCount();
		assertFalse(opener.isRebuildRequested());
		assertFalse(opened.isRebuildRequested());

		registry.rebuildWindows();

		assertTrue("The tab must be rebuilt.", opener.isRebuildRequested());
		assertTrue("The opened window must be rebuilt.", opened.isRebuildRequested());
		assertEquals("One reload for the tab.", openerBefore + 1, openerQueue.pendingEventCount());
		assertEquals("One reload for the opened window.", openedBefore + 1,
			openedQueue.pendingEventCount());
		assertNotNull("The tree is disposed by the render the reload brings, not here.",
			opener.getRootControl());
	}

	/**
	 * A window that displays nothing yet is marked like any other, and a session without windows has
	 * nothing to rebuild.
	 */
	public void testRebuildWindowsWithoutTree() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		// Nothing registered at all: must not fail.
		registry.rebuildWindows();

		WindowEntry entry = registry.getOrCreateWindow("vTab");
		assertNull(entry.getRootControl());

		registry.rebuildWindows();

		assertTrue(entry.isRebuildRequested());
		assertEquals(1, entry.getQueue().pendingEventCount());
	}

	/**
	 * The unload report of the displayed page detaches its tree and marks the window unloaded.
	 */
	public void testUnloadOfDisplayedPage() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		WindowEntry entry = registry.getOrCreateWindow("vTab");
		ReactControl tree = displayPage(registry, entry);
		String pageLoad = registry.issuePageLoad("vTab");

		registry.windowUnloaded("vTab", pageLoad);

		assertFalse("The tree of the unloaded page stops observing the model.", tree.isAttached());
		assertTrue("The window waits for its page to come back.", entry.getUnloadedAt() != 0);
	}

	/**
	 * The unload report of a page a reload has already replaced leaves the displayed page alone.
	 */
	public void testUnloadOfReplacedPage() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		WindowEntry entry = registry.getOrCreateWindow("vTab");
		ReactControl tree = displayPage(registry, entry);
		String replaced = registry.issuePageLoad("vTab");
		// The reload renders its page before the unload report of the page it replaces arrives.
		String displayed = registry.issuePageLoad("vTab");
		assertFalse(replaced.equals(displayed));

		registry.windowUnloaded("vTab", replaced);

		assertTrue("The displayed tree must stay attached.", tree.isAttached());
		assertEquals("The displayed window must not be collected.", 0, entry.getUnloadedAt());
		assertEquals(displayed, entry.getPageLoad());
	}

	/**
	 * An unload report naming no page speaks of the displayed one.
	 */
	public void testUnloadWithoutPageLoad() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		WindowEntry entry = registry.getOrCreateWindow("vTab");
		ReactControl tree = displayPage(registry, entry);
		registry.issuePageLoad("vTab");

		registry.windowUnloaded("vTab", null);

		assertFalse(tree.isAttached());
		assertTrue(entry.getUnloadedAt() != 0);
	}

	/**
	 * Page-load tokens are unique within the session, across windows as well.
	 */
	public void testPageLoadsAreUnique() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);

		String first = registry.issuePageLoad("vTab");
		String other = registry.issuePageLoad("vOther");
		String again = registry.issuePageLoad("vTab");

		assertFalse(first.equals(other));
		assertFalse(first.equals(again));
		assertFalse(other.equals(again));
		assertEquals(again, registry.getWindow("vTab").getPageLoad());
		assertEquals(other, registry.getWindow("vOther").getPageLoad());
	}

	/**
	 * The tree of an unloaded page is detached in the window's own subsession, although the unload
	 * report arrives in a request that has none, and the request's context is restored afterwards.
	 */
	public void testUnloadDetachesInWindowSubSession() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		TLSessionContext session = DefaultSessionContext.newDefaultSessionContext();
		TLSubSessionContext windowSubSession = createSubSession(session, "vTab", Locale.GERMAN);
		WindowEntry entry = registry.getOrCreateWindow("vTab");
		ReactControl tree = displayPage(registry, entry);
		List<Observation> seen = new ArrayList<>();
		tree.addDetachListener(() -> seen.add(Observation.current()));
		String pageLoad = registry.issuePageLoad("vTab");

		inRequest(session, null, () -> {
			registry.windowUnloaded("vTab", pageLoad);

			assertNull("The request's missing subsession must be restored.",
				TLContextManager.getSubSession());
			assertSame(session, TLContextManager.getSession());
		});

		assertEquals(List.of(new Observation(windowSubSession, Locale.GERMAN)), seen);
	}

	/**
	 * The close callback and the disposal of a closed window's tree run in the window's own
	 * subsession, although the close is reported in a request of another window, and the
	 * subsession of that request is restored afterwards.
	 */
	public void testCloseRunsInWindowSubSession() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		TLSessionContext session = DefaultSessionContext.newDefaultSessionContext();
		TLSubSessionContext openerSubSession = createSubSession(session, "vOpener", Locale.ENGLISH);
		SSEUpdateQueue openerQueue = registry.getOrCreateQueue("vOpener");
		ReactContext openerCtx = new DefaultReactContext("", "vOpener", openerQueue, registry);
		List<Observation> seen = new ArrayList<>();
		String windowId = registry.openWindow(openerCtx,
			(ctx, model) -> new ReactControl(ctx, model, "test-module"),
			null, new WindowOptions(), () -> seen.add(Observation.current()));
		TLSubSessionContext windowSubSession = createSubSession(session, windowId, Locale.GERMAN);
		ReactControl tree = displayPage(registry, registry.getWindow(windowId));
		tree.addCleanupAction(() -> seen.add(Observation.current()));

		inRequest(session, openerSubSession, () -> {
			registry.windowClosed(windowId);

			assertSame("The request's subsession must be restored.", openerSubSession,
				TLContextManager.getSubSession());
			assertEquals(Locale.ENGLISH, Resources.getCurrentLocale());
		});

		Observation inWindow = new Observation(windowSubSession, Locale.GERMAN);
		assertEquals("Close callback and tree disposal.", List.of(inWindow, inWindow), seen);
		assertNull(registry.getWindow(windowId));
	}

	/**
	 * A window whose page was never rendered has no subsession, and is closed in the context of
	 * the request.
	 */
	public void testCloseWithoutWindowSubSession() {
		ReactWindowRegistry registry = new ReactWindowRegistry(SESSION_ID);
		TLSessionContext session = DefaultSessionContext.newDefaultSessionContext();
		TLSubSessionContext callerSubSession = createSubSession(session, "vOther", Locale.ENGLISH);
		ReactControl tree = displayPage(registry, registry.getOrCreateWindow("vTab"));
		List<Observation> seen = new ArrayList<>();
		tree.addCleanupAction(() -> seen.add(Observation.current()));

		inRequest(session, callerSubSession, () -> registry.windowClosed("vTab"));

		assertEquals(List.of(new Observation(callerSubSession, Locale.ENGLISH)), seen);
		assertNull(registry.getWindow("vTab"));
	}

	/**
	 * What a control saw of its thread's context.
	 *
	 * @param subSession
	 *        The installed subsession.
	 * @param locale
	 *        The locale resources were resolved in.
	 */
	private record Observation(SubSessionContext subSession, Locale locale) {

		static Observation current() {
			return new Observation(TLContextManager.getSubSession(), Resources.getCurrentLocale());
		}
	}

	/**
	 * Creates the subsession of a window, as rendering the window's page does.
	 */
	private static TLSubSessionContext createSubSession(TLSessionContext session, String windowId,
			Locale locale) {
		TLContext subSession = new TLContext();
		subSession.setSessionContext(session);
		subSession.setCurrentLocale(locale);
		session.setIfAbsent(windowId, subSession);
		return subSession;
	}

	/**
	 * Runs the given action as a request of the given session does.
	 *
	 * @param subSession
	 *        The subsession of the window the request serves, or {@code null} for a request that
	 *        serves no window.
	 */
	private static void inRequest(TLSessionContext session, TLSubSessionContext subSession, Runnable action) {
		ThreadContextManager manager = ThreadContextManager.getManager();
		InteractionContext interaction = manager.newInteraction(null, null, null);
		manager.setInteraction(interaction);
		try {
			interaction.installSessionContext(session);
			if (subSession != null) {
				interaction.installSubSessionContext(subSession);
			}
			action.run();
		} finally {
			manager.removeInteraction();
		}
	}

	/**
	 * Installs an attached tree into the given window, as rendering its page does.
	 */
	private static ReactControl displayPage(ReactWindowRegistry registry, WindowEntry entry) {
		SSEUpdateQueue queue = registry.getOrCreateQueue(entry.getWindowId());
		ReactContext ctx = new DefaultReactContext("", entry.getWindowId(), queue, registry);
		ReactControl tree = new ReactControl(ctx, null, "Demo");
		entry.setRootControl(tree);
		tree.attach();
		assertTrue(tree.isAttached());
		return tree;
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactWindowRegistry.class,
				TypeIndex.Module.INSTANCE, ThreadContextManager.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}
}
