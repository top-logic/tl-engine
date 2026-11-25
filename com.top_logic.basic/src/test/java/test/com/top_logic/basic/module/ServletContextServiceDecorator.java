/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import java.io.File;
import java.lang.reflect.Proxy;

import jakarta.servlet.ServletContext;

import test.com.top_logic.basic.TestSetupDecorator;

import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.services.ServletContextService;

/**
 * {@link TestSetupDecorator} running the decorated setup with a started
 * {@link ServletContextService}, so that services depending on it can be started in a test.
 *
 * <p>
 * The {@link ServletContext} knows nothing but the {@link #CONTEXT_PATH context path} and the
 * working directory as its root.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ServletContextServiceDecorator implements TestSetupDecorator {

	/**
	 * The context path the {@link ServletContext} reports.
	 */
	public static final String CONTEXT_PATH = "";

	/**
	 * Singleton {@link ServletContextServiceDecorator} instance.
	 */
	public static final ServletContextServiceDecorator INSTANCE = new ServletContextServiceDecorator();

	private ServletContextServiceDecorator() {
		// Singleton constructor.
	}

	@Override
	public void setup(SetupAction innerSetup) throws Exception {
		ServletContextService.startUpServletContextService(servletContext());
		innerSetup.setUpDecorated();
	}

	@Override
	public void tearDown(SetupAction innerSetup) throws Exception {
		try {
			innerSetup.tearDownDecorated();
		} finally {
			ModuleUtil.INSTANCE.shutDown(ServletContextService.Module.INSTANCE);
		}
	}

	private static ServletContext servletContext() {
		String root = new File(".").getAbsolutePath();
		return (ServletContext) Proxy.newProxyInstance(ServletContextServiceDecorator.class.getClassLoader(),
			new Class<?>[] { ServletContext.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getContextPath" -> CONTEXT_PATH;
				case "getRealPath" -> root;
				case "toString" -> ServletContext.class.getName() + "(" + root + ")";
				case "hashCode" -> Integer.valueOf(System.identityHashCode(proxy));
				case "equals" -> Boolean.valueOf(proxy == args[0]);
				default -> null;
			});
	}

}
