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

	/** A session that belongs to an account. */
	private static final boolean NAMED = false;

	/** A session that belongs to no account. */
	private static final boolean ANONYMOUS = true;

	/**
	 * A URL that names no view file is a route and displays the default view.
	 */
	public void testRouteDisplaysDefaultView() {
		ViewConfig config = newConfig();

		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/", NAMED));
		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1", NAMED));
		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/some/route", NAMED));
	}

	/**
	 * The default view can be named explicitly without being registered.
	 */
	public void testDefaultViewIsEntryPoint() {
		ViewConfig config = newConfig();

		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/app.view.xml", NAMED));
	}

	/**
	 * A registered view is loaded.
	 */
	public void testRegisteredEntryPoint() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertEquals("/WEB-INF/views/demo/x.view.xml",
			ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml", NAMED));
	}

	/**
	 * A view that is not registered is refused, even though other views are.
	 */
	public void testUnregisteredViewRefused() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertNull(ViewServlet.resolveViewPath(config, "/w1/login.view.xml", NAMED));
	}

	/**
	 * An application registering nothing offers its default view alone.
	 */
	public void testNoEntryPointsRegistered() {
		ViewConfig config = newConfig();

		assertNull(ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml", NAMED));
	}

	/**
	 * A visitor sees the login view, whatever the URL names.
	 */
	public void testAnonymousSeesLoginView() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));

		assertEquals(LOGIN_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/some/route", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/app.view.xml", ANONYMOUS));
		assertEquals(LOGIN_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
	}

	/**
	 * A view that is no entry point is answered with the login view rather than with "not found":
	 * what a visitor may see is decided once they are logged in.
	 */
	public void testAnonymousSeesLoginViewForUnregisteredView() {
		ViewConfig config = withLoginView(newConfig());

		assertEquals(LOGIN_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
	}

	/**
	 * An account of their own gets the application, not the login view.
	 */
	public void testLoggedInSkipsLoginView() {
		ViewConfig config = withLoginView(newConfig("demo/x.view.xml"));

		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/some/route", NAMED));
		assertEquals("/WEB-INF/views/demo/x.view.xml",
			ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml", NAMED));
	}

	/**
	 * An application that shows itself to visitors displays what the URL names to a visitor, too.
	 */
	public void testAnonymousWithoutLoginView() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/some/route", ANONYMOUS));
		assertEquals("/WEB-INF/views/demo/x.view.xml",
			ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml", ANONYMOUS));
		assertNull(ViewServlet.resolveViewPath(config, "/w1/other.view.xml", ANONYMOUS));
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
			ViewConfig.EntryPoint entryPoint = TypedConfiguration.newConfigItem(ViewConfig.EntryPoint.class);
			entryPoint.update(
				TypedConfiguration.getConfigurationDescriptor(ViewConfig.EntryPoint.class)
					.getProperty(ViewConfig.EntryPoint.VIEW),
				view);
			result.getEntryPoints().add(entryPoint);
		}
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewEntryPoints.class, TypeIndex.Module.INSTANCE);
	}
}
