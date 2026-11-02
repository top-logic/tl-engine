/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactDialogManagerControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a modal dialog takes no part in the address of the page it is shown on.
 *
 * <p>
 * A dialog is not a page: the URL names what the page displays, and a tab bar inside a dialog
 * names nothing of it. Opening the dialog, switching its tabs and closing it leave the address bar
 * as it is - a segment of the dialog in the URL would survive the dialog, lead the back button
 * through tabs of a dialog that is gone, and be dropped silently on the next reload.
 * </p>
 */
public class TestDialogRouting extends TestCase {

	/** The tab of the page that is displayed. */
	private static final String TABLE = "table";

	/** Another tab of the page. */
	private static final String CHART = "chart";

	/** The tab the dialog displays first. */
	private static final String ACCOUNT = "account";

	/** Another tab of the dialog. */
	private static final String SECURITY = "security";

	/** The React module of the content a tab displays. */
	private static final String CONTENT_MODULE = "TestContent";

	private ReactContext _context;

	private RouteManager _routeManager;

	private ReactDialogManagerControl _dialogs;

	private ReactTabBarControl _pageTabs;

	private ReactControl _root;

	/** The URLs reported to the client, in the order reported. */
	private List<String> _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SSEUpdateQueue queue = new SSEUpdateQueue();
		_context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
		_routeManager = _context.getRouteManager();
		queue.setRouteManager(_routeManager);
		_reported = new ArrayList<>();
		_routeManager.setUrlChangeHandler((url, title, replace) -> _reported.add(url));

		_pageTabs = tabBar(TABLE, CHART);
		_dialogs = new ReactDialogManagerControl(_context);
		_root = new ReactStackControl(_context, List.of(_pageTabs, _dialogs));
		queue.setRootControl(_root);
		_root.attach();
		render();
		assertEquals("The page names its tab.", TABLE, _routeManager.currentUrl());
		_reported.clear();
	}

	/**
	 * Opening a dialog with a tab bar, switching its tab and closing it reports nothing: the URL
	 * stays the one of the page throughout.
	 */
	public void testDialogTabsStayOutOfTheUrl() {
		ReactTabBarControl dialogTabs = tabBar(ACCOUNT, SECURITY);
		DialogHandle dialog = open(dialogTabs);
		assertEquals("Opening a dialog changed the URL of the page.", TABLE, _routeManager.currentUrl());

		dialogTabs.selectTab(SECURITY);
		render();
		assertEquals("The tab bar of a dialog still switches its tabs.", SECURITY, dialogTabs.getActiveTabId());
		assertEquals("Switching a tab of a dialog changed the URL of the page.", TABLE,
			_routeManager.currentUrl());

		dialog.close(DialogResult.cancelled());
		assertEquals(TABLE, _routeManager.currentUrl());
		assertEquals("A dialog made the page report a URL.", List.of(), _reported);
	}

	/**
	 * The title of the page is not taken from a route inside a dialog either.
	 */
	public void testDialogTabsStayOutOfTheTitle() {
		String title = _routeManager.currentTitle();
		ReactTabBarControl dialogTabs = tabBar(ACCOUNT, SECURITY);
		open(dialogTabs);
		dialogTabs.selectTab(SECURITY);
		render();
		assertEquals("A tab of a dialog renamed the page.", title, _routeManager.currentTitle());
	}

	/**
	 * A URL adopted while a dialog is open is not offered to a participant inside it: a segment the
	 * page cannot take up is dropped, even where it names a tab of the dialog.
	 */
	public void testDialogTakesUpNoRouteOfAnAdoptedUrl() {
		_routeManager.adoptUrl(TABLE + "/" + SECURITY);
		_routeManager.resolvePending();

		// The dialog appears while the segment behind the page's tab is still pending.
		ReactTabBarControl dialogTabs = tabBar(ACCOUNT, SECURITY);
		open(dialogTabs);
		_routeManager.finishAdoption();

		assertEquals("A tab bar inside a dialog took up a route of the page's URL.", ACCOUNT,
			dialogTabs.getActiveTabId());
		assertEquals("The segment no participant of the page takes up is dropped.", TABLE,
			_routeManager.currentUrl());
		assertEquals(List.of(TABLE), _reported);
	}

	/**
	 * The page goes on routing next to and after a dialog: switching its tab after the dialog is
	 * closed is a navigation of its own, and no segment of the dialog survives it.
	 */
	public void testPageRoutesAfterTheDialogIsClosed() {
		ReactTabBarControl dialogTabs = tabBar(ACCOUNT, SECURITY);
		DialogHandle dialog = open(dialogTabs);
		dialogTabs.selectTab(SECURITY);
		render();
		dialog.close(DialogResult.cancelled());

		_pageTabs.selectTab(CHART);
		render();
		assertEquals(List.of(CHART), _reported);
		assertEquals(CHART, _routeManager.currentUrl());
	}

	private DialogHandle open(ReactControl content) {
		DialogHandle handle = _dialogs.openDialog(false, content, result -> {
			// No result to process.
		});
		render();
		return handle;
	}

	private ReactTabBarControl tabBar(String first, String second) {
		return new ReactTabBarControl(_context, null, List.of(
			new TabDefinition(first, first, this::content, null).withRoute(first),
			new TabDefinition(second, second, this::content, null).withRoute(second)), first);
	}

	private ReactControl content() {
		return new ReactControl(_context, null, CONTENT_MODULE);
	}

	private void render() {
		try {
			_root.write(new TagWriter());
		} catch (IOException ex) {
			throw new AssertionError("Rendering the page failed.", ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * <p>
	 * The page title is resolved by {@link com.top_logic.util.Resources}, which the services of the
	 * test setup provide.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestDialogRouting.class),
				ResourcesModule.Module.INSTANCE));
	}

}
