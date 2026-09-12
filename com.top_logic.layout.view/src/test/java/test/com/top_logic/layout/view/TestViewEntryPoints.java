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

	/**
	 * A URL that names no view file is a route and displays the default view.
	 */
	public void testRouteDisplaysDefaultView() {
		ViewConfig config = newConfig();

		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/"));
		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1"));
		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/some/route"));
	}

	/**
	 * The default view can be named explicitly without being registered.
	 */
	public void testDefaultViewIsEntryPoint() {
		ViewConfig config = newConfig();

		assertEquals(DEFAULT_VIEW_PATH, ViewServlet.resolveViewPath(config, "/w1/app.view.xml"));
	}

	/**
	 * A registered view is loaded.
	 */
	public void testRegisteredEntryPoint() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertEquals("/WEB-INF/views/demo/x.view.xml",
			ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml"));
	}

	/**
	 * A view that is not registered is refused, even though other views are.
	 */
	public void testUnregisteredViewRefused() {
		ViewConfig config = newConfig("demo/x.view.xml");

		assertNull(ViewServlet.resolveViewPath(config, "/w1/login.view.xml"));
	}

	/**
	 * An application registering nothing offers its default view alone.
	 */
	public void testNoEntryPointsRegistered() {
		ViewConfig config = newConfig();

		assertNull(ViewServlet.resolveViewPath(config, "/w1/demo/x.view.xml"));
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
