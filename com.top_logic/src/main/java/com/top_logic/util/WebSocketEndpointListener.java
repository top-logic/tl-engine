/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;

import com.top_logic.basic.Logger;

/**
 * {@link ServletContextListener} registering Jakarta websocket endpoint classes with the
 * {@link ServerContainer} of the servlet container.
 *
 * <p>
 * The endpoint classes are named in the context parameter {@value #ENDPOINTS_PARAM}, a list of
 * fully qualified class names separated by whitespace or commas. Each class must be annotated
 * with {@link ServerEndpoint}; it is registered through {@link ServerContainer#addEndpoint(Class)}.
 * An endpoint implemented as a subclass of {@link jakarta.websocket.Endpoint} needs a
 * {@link jakarta.websocket.server.ServerEndpointConfig} and cannot be registered by this listener.
 * </p>
 *
 * <p>
 * Usage in the application's {@code web.xml} (or a {@code web-fragment.xml}):
 * </p>
 *
 * <pre>
 * &lt;context-param&gt;
 *     &lt;param-name&gt;{@value #ENDPOINTS_PARAM}&lt;/param-name&gt;
 *     &lt;param-value&gt;
 *         my.app.ChatEndpoint
 *         my.app.NotificationEndpoint
 *     &lt;/param-value&gt;
 * &lt;/context-param&gt;
 *
 * &lt;listener&gt;
 *     &lt;listener-class&gt;{@link com.top_logic.util.WebSocketEndpointListener}&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 *
 * <p>
 * The registration uses the standard API only and therefore works on every servlet container
 * providing Jakarta websocket: Tomcat, Jetty with its websocket module, and the development start
 * of an application. The automatic discovery of {@link ServerEndpoint}-annotated classes, in
 * contrast, depends on a container that scans the class path of the web application.
 * </p>
 *
 * <p>
 * Failures do not stop the application: a missing {@link ServerContainer}, an endpoint class that
 * cannot be loaded, and an endpoint the container rejects are each logged as an error.
 * </p>
 */
public class WebSocketEndpointListener implements ServletContextListener {

	/**
	 * Name of the servlet context parameter listing the fully qualified class names of the
	 * {@link ServerEndpoint}-annotated classes to register, separated by whitespace or commas.
	 */
	public static final String ENDPOINTS_PARAM = "websocket.endpoints";

	/**
	 * Name of the servlet context attribute under which the container provides its
	 * {@link ServerContainer}.
	 */
	public static final String SERVER_CONTAINER_ATTRIBUTE = ServerContainer.class.getName();

	private static final String SEPARATOR_PATTERN = "[\\s,]+";

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		String endpoints = context.getInitParameter(ENDPOINTS_PARAM);
		if (endpoints == null || endpoints.isBlank()) {
			return;
		}

		Object container = context.getAttribute(SERVER_CONTAINER_ATTRIBUTE);
		if (!(container instanceof ServerContainer)) {
			Logger.error("Cannot register the WebSocket endpoints of context parameter '" + ENDPOINTS_PARAM
				+ "': The servlet container provides no '" + SERVER_CONTAINER_ATTRIBUTE + "' (found: " + container
				+ ").", WebSocketEndpointListener.class);
			return;
		}
		ServerContainer serverContainer = (ServerContainer) container;

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : endpoints.trim().split(SEPARATOR_PATTERN)) {
			Class<?> endpointClass;
			try {
				endpointClass = Class.forName(className, true, classLoader);
			} catch (ClassNotFoundException | LinkageError ex) {
				Logger.error("Cannot load WebSocket endpoint class '" + className + "' given in context parameter '"
					+ ENDPOINTS_PARAM + "'.", ex, WebSocketEndpointListener.class);
				continue;
			}

			try {
				serverContainer.addEndpoint(endpointClass);
				Logger.info("Registered WebSocket endpoint '" + className + "'.", WebSocketEndpointListener.class);
			} catch (DeploymentException | RuntimeException ex) {
				Logger.error("Cannot register WebSocket endpoint '" + className + "' given in context parameter '"
					+ ENDPOINTS_PARAM + "'.", ex, WebSocketEndpointListener.class);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// The container removes its endpoints itself.
	}

}
