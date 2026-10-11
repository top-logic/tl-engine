/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.List;
import java.util.Map;

import static com.top_logic.knowledge.service.KBUtils.inTransaction;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.views.tools.ViewTools;
import com.top_logic.ai.views.tools.ViewTools.Edit;
import com.top_logic.ai.views.tools.ViewTools.Op;
import com.top_logic.ai.views.tools.WindowTools;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ReloadableControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.tl.ai.core.agent.RunWindow;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.model.ModelService;

/**
 * What the agent sees of the user's window, and that a view it changes shows on
 * the open page without moving the user.
 *
 * <p>
 * The window is built from the views under {@code test-window/} the way the view servlet builds a
 * page, and handed to the tools as the run's window. What stands in for the browser is
 * {@link #render()}: rendering is what builds the content a navigation shows.
 * </p>
 */
public class TestWindowPage extends BasicTestCase {

	private static final String SHELL = ViewLoader.fullPath("test-window/shell.view.xml");

	private static final String TABS = ViewLoader.fullPath("test-window/tabs.view.xml");

	private static final String SIDEBAR = "/appShell/sidebar";

	private static final String TAB_BAR = SIDEBAR + "/item[tabs]/tabBar";

	private ReactControl _root;

	private String _tabs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tabs = ViewFiles.read(TABS);
		_root = openWindow();
		RunWindow.install((subSession, action) -> {
			action.accept(_root);
			return true;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		RunWindow.install(null);
		ViewFiles.write(TABS, _tabs);
		super.tearDown();
	}

	/** The page names where the user is, the view each part comes from, and what it shows. */
	public void testWhereAmINamesThePlaceAndItsViews() {
		String here = new WindowTools().whereAmI().text();

		assertContains("@" + SIDEBAR, here);
		assertContains("activeItemId=\"home\"", here);
		assertContains("[view test-window/home.view.xml]", here);
		assertContains("Welcome", here);
	}

	/** A window whose page is not built has nothing to read, which is no failure of the run. */
	public void testWhereAmIWithoutABuiltPage() {
		RunWindow.install((subSession, action) -> {
			action.accept(null);
			return true;
		});

		ToolResult result = new WindowTools().whereAmI();

		assertContains("no built page", result.text());
	}

	/** A view the agent changes shows on the open page, and the user stays on their item and tab. */
	public void testAnEditShowsWithoutLosingThePlace() {
		act(SIDEBAR, "selectItem", Map.of("itemId", "tabs"));
		render();
		act(TAB_BAR, "selectTab", Map.of("tabId", "second"));
		render();

		ToolResult result = inTransaction(() -> new ViewTools().editView("test-window/tabs.view.xml",
			List.of(new Edit(Op.REPLACE, "/view/tab-bar/tab[id=second]/text",
				"<text>\n\t<label>\n\t\t<en>changed tab</en>\n\t</label>\n</text>", null, null)),
			ToolContext.EMPTY));
		render();

		assertContains("shows the change already", result.text());
		assertContains("changed tab", new WindowTools().whereAmI().text());
		assertEquals("tabs", state(SIDEBAR, "activeItemId"));
		assertEquals("second", state(TAB_BAR, "activeTabId"));
		assertContains("<!-- Two tabs, the first one open; TestWindowPage changes the second. -->", ViewFiles.read(TABS));
	}

	/**
	 * A turn rolled back after a write takes neither the written file nor its change-set record:
	 * the record is committed with the file, not with the turn.
	 */
	public void testAWriteOutlivesARolledBackTurn() {
		int recorded = ViewChanges.all().size();
		Transaction turn = PersistencyLayer.getKnowledgeBase().beginTransaction();
		ToolResult result;
		try {
			result = new ViewTools().editView("test-window/tabs.view.xml",
				List.of(new Edit(Op.SET, "/view/tab-bar", null, "active-tab", "second")), ToolContext.EMPTY);
		} finally {
			turn.rollback();
		}
		HistoryUtils.updateSessionRevision();

		assertContains("Saved as change set", result.text());
		assertContains("active-tab=\"second\"", ViewFiles.read(TABS));
		assertEquals(recorded + 1, ViewChanges.all().size());
	}

	/** What a click does: the user moves on the page. */
	private void act(String address, String command, Map<String, Object> arguments) {
		ScriptingSession.forRoot(_root).resolve(address).executeCommand(command, arguments);
	}

	private Object state(String address, String key) {
		return ScriptingSession.forRoot(_root).resolve(address).scriptingScalarState().get(key);
	}

	/** What the browser does with the state it receives: it renders, which builds lazy content. */
	private void render() {
		_root.stateAsJSON();
	}

	/** Builds the shell as the view servlet builds a window. */
	private static ReactControl openWindow() throws Exception {
		ReactWindowRegistry registry = new ReactWindowRegistry("session");
		SSEUpdateQueue queue = registry.getOrCreateQueue("window");
		DefaultReactContext react = new DefaultReactContext("", "window", queue, registry);
		ViewContext context = new DefaultViewContext(react, SHELL);
		ReloadableControl content =
			new ReloadableControl(SHELL, context, (ReactControl) ViewLoader.loadView(SHELL).createControl(context));
		content.setViewSource(SHELL);
		ReactControl root = new ReactStackControl(react, List.of(content));
		queue.setRootControl(root);
		root.stateAsJSON();
		return root;
	}

	/**
	 * The suite: a knowledge base for the change records, and the view layer.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(new TestSuite(TestWindowPage.class),
			ModelService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}
}
