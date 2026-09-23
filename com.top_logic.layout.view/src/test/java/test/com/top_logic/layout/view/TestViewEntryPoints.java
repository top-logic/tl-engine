/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewConfig;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.layout.view.ViewServlet.ViewResolution;

/**
 * Tests that a URL can only name the default view or a registered entry point as the contents of a
 * browser tab.
 *
 * @see ViewConfig#getEntryPoints()
 */
public class TestViewEntryPoints extends TestCase {

	private static final String DEFAULT_VIEW_PATH = "/WEB-INF/views/app.view.xml";

	private static final String LOGIN_VIEW = "login-page.view.xml";

	private static final String LOGIN_VIEW_PATH = "/WEB-INF/views/login-page.view.xml";

	private static final String VISITOR_VIEW = "unknown-account.view.xml";

	private static final String VISITOR_VIEW_PATH = "/WEB-INF/views/unknown-account.view.xml";

	/** A session that belongs to an account. */
	private static final boolean NAMED = false;

	/** A session that belongs to no account. */
	private static final boolean ANONYMOUS = true;

	/**
	 * A URL that names no view file is a route and displays the default view.
	 */
	public void testRouteDisplaysDefaultView() {
		ViewConfig config = newConfig();

		assertEquals(DEFAULT_VIEW_PATH, viewPath(config, "/w1/", NAMED));
		assertEquals(DEFAULT_VIEW_PATH, viewPath(config, "/w1", NAMED));
		assertEquals(DEFAULT_VIEW_PATH, viewPath(config, "/w1/some/route", NAMED));
	}

	/**
	 * The default view can be named explicitly without being registered.
	 */
	public void testDefaultViewIsEntryPoint() {
		ViewConfig config = newConfig();

		assertEquals(DEFAULT_VIEW_PATH, viewPath(config, "/w1/app.view.xml", NAMED));
	}

	/**
	 * A registered view is loaded.
	 */
	public void testRegisteredEntryPoint() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertEquals("/WEB-INF/views/demo/x.view.xml", viewPath(config, "/w1/demo/x.view.xml", NAMED));
	}

	/**
	 * A view that is not registered is refused, even though other views are.
	 */
	public void testUnregisteredViewRefused() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertNull(viewPath(config, "/w1/login.view.xml", NAMED));
	}

	/**
	 * An application registering nothing offers its default view alone.
	 */
	public void testNoEntryPointsRegistered() {
		ViewConfig config = newConfig();

		assertNull(viewPath(config, "/w1/demo/x.view.xml", NAMED));
	}

	/**
	 * A visitor sees the login view, whatever the URL names.
	 */
	public void testAnonymousSeesLoginView() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));

		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/some/route", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/app.view.xml", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
	}

	/**
	 * The login view is marked as such, so that the requested URL is held rather than adopted.
	 */
	public void testLoginViewIsReported() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));

		assertTrue(ViewServlet.resolveView(config, "/w1/some/route", ANONYMOUS).loginView());
		assertTrue(ViewServlet.resolveView(config, "/w1/demo/x.view.xml", ANONYMOUS).loginView());
		assertFalse(ViewServlet.resolveView(config, "/w1/some/route", NAMED).loginView());
	}

	/**
	 * A view that is no entry point is answered with the login view rather than with "not found":
	 * what a visitor may see is decided once they are logged in.
	 */
	public void testAnonymousSeesLoginViewForUnregisteredView() {
		ViewConfig config = withLoginView(newConfig());

		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
	}

	/**
	 * An entry point written for a visitor is shown to a visitor, although a login view is
	 * configured.
	 */
	public void testAnonymousEntryPoint() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));
		addEntryPoint(config, VISITOR_VIEW, true);

		ViewResolution resolution = ViewServlet.resolveView(config, "/w1/" + VISITOR_VIEW, ANONYMOUS);
		assertEquals(VISITOR_VIEW_PATH, resolution.viewPath());
		// The page is the one the URL names, so its address is taken up rather than held.
		assertFalse(resolution.loginView());
	}

	/**
	 * Only the URL naming the anonymous entry point shows it: everything else a visitor asks for is
	 * still answered with the login view.
	 */
	public void testAnonymousEntryPointOnlyWhereNamed() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));
		addEntryPoint(config, VISITOR_VIEW, true);

		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/app.view.xml", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/some/route", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, viewPath(config, "/w1/", ANONYMOUS));
	}

	/**
	 * An account of their own reaches an anonymous entry point like any other.
	 */
	public void testAnonymousEntryPointForAccount() {
		ViewConfig config = withLoginView(newConfig());
		addEntryPoint(config, VISITOR_VIEW, true);

		ViewResolution resolution = ViewServlet.resolveView(config, "/w1/" + VISITOR_VIEW, NAMED);
		assertEquals(VISITOR_VIEW_PATH, resolution.viewPath());
		assertFalse(resolution.loginView());
	}

	/**
	 * An account of their own gets the application, not the login view.
	 */
	public void testLoggedInSkipsLoginView() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));

		assertEquals(DEFAULT_VIEW_PATH, viewPath(config, "/w1/some/route", NAMED));
		assertEquals("/WEB-INF/views/demo/x.view.xml", viewPath(config, "/w1/demo/x.view.xml", NAMED));
	}

	/**
	 * An application that shows itself to visitors displays what the URL names to a visitor, too.
	 */
	public void testAnonymousWithoutLoginView() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertEquals(DEFAULT_VIEW_PATH, viewPath(config, "/w1/some/route", ANONYMOUS));
		assertEquals("/WEB-INF/views/demo/x.view.xml", viewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
		assertNull(viewPath(config, "/w1/other.view.xml", ANONYMOUS));
	}

	private static String viewPath(ViewConfig config, String pathInfo, boolean anonymous) {
		return ViewServlet.resolveView(config, pathInfo, anonymous).viewPath();
	}

	private ViewConfig withLoginView(ViewConfig config) {
		config.update(
			TypedConfiguration.getConfigurationDescriptor(ViewConfig.class).getProperty(ViewConfig.LOGIN_VIEW),
			LOGIN_VIEW);
		return config;
	}

	private ViewConfig newConfig(String... entryPoints) {
		ViewConfig result = TypedConfiguration.newConfigItem(ViewConfig.class);
		for (String view : entryPoints) {
			addEntryPoint(result, view, false);
		}
		return result;
	}

	private void addEntryPoint(ViewConfig config, String view, boolean anonymous) {
		ViewConfig.EntryPoint entryPoint = TypedConfiguration.newConfigItem(ViewConfig.EntryPoint.class);
		entryPoint.update(
			TypedConfiguration.getConfigurationDescriptor(ViewConfig.EntryPoint.class)
				.getProperty(ViewConfig.EntryPoint.VIEW),
			view);
		entryPoint.update(
			TypedConfiguration.getConfigurationDescriptor(ViewConfig.EntryPoint.class)
				.getProperty(ViewConfig.EntryPoint.ANONYMOUS),
			Boolean.valueOf(anonymous));
		config.getEntryPoints().add(entryPoint);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewEntryPoints.class, TypeIndex.Module.INSTANCE);
	}
}
