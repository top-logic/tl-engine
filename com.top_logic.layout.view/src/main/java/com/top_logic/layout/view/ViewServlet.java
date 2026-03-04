/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultViewDisplayContext;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that bootstraps the view-based UI.
 *
 * <p>
 * Loads a {@code .view.xml} file, parses it via {@link TypedConfiguration} into a shared
 * {@link UIElement} tree, creates per-session control trees via
 * {@link UIElement#createControl(ViewContext)}, and renders the initial HTML page using
 * {@link TagWriter} and {@link ViewControl#write(ViewDisplayContext, TagWriter)}.
 * </p>
 *
 * <p>
 * The servlet is mapped to {@code /view/*}. The path info determines which view file to load (e.g.,
 * {@code /view/main.view.xml} loads {@code /WEB-INF/views/main.view.xml}).
 * </p>
 *
 * <p>
 * <b>Scope management:</b> The servlet creates a {@link DefaultViewDisplayContext} that provides ID
 * allocation and SSE queue access without depending on the traditional
 * {@link com.top_logic.mig.html.layout.MainLayout} component tree or old-world
 * {@link com.top_logic.layout.DisplayContext} scope chain.
 * </p>
 */
public class ViewServlet extends TopLogicServlet {

	/** Base path for view XML files within the webapp. */
	private static final String VIEW_BASE_PATH = "/WEB-INF/views/";

	/**
	 * Global cache of parsed {@link ViewElement}s, keyed by view path. The element tree is
	 * stateless and shared across all sessions.
	 */
	private final ConcurrentHashMap<String, ViewElement> _viewCache = new ConcurrentHashMap<>();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}

		String viewPath = resolveViewPath(request);

		ViewElement view;
		try {
			view = getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to load view: " + viewPath, ex, ViewServlet.class);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Failed to load view: " + ex.getMessage());
			return;
		}

		ViewDisplayContext displayContext = new DefaultViewDisplayContext(
			request.getContextPath(), "view", SSEUpdateQueue.forSession(session));
		ViewContext viewContext = new ViewContext(displayContext);

		ViewControl rootControl = view.createControl(viewContext);

		renderPage(request, response, rootControl, displayContext);
	}

	/**
	 * Resolves the view file path from the request's path info.
	 *
	 * <p>
	 * When no explicit path is given (e.g. {@code /view/}), falls back to the default view
	 * configured in {@link ViewConfig#getDefaultView()}.
	 * </p>
	 */
	private String resolveViewPath(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && pathInfo.length() > 1) {
			// Explicit path: /view/foo.view.xml -> /WEB-INF/views/foo.view.xml
			String viewName = pathInfo.substring(1);
			return VIEW_BASE_PATH + viewName;
		}
		// Fall back to the configured default view.
		String defaultView = ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView();
		return VIEW_BASE_PATH + defaultView;
	}

	/**
	 * Retrieves a cached {@link ViewElement} or loads and caches it from the view XML file.
	 */
	private ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		ViewElement cached = _viewCache.get(viewPath);
		if (cached != null) {
			return cached;
		}

		ViewElement view = loadView(viewPath);
		_viewCache.putIfAbsent(viewPath, view);
		return _viewCache.get(viewPath);
	}

	/**
	 * Parses a {@code .view.xml} file into a {@link ViewElement}.
	 */
	private ViewElement loadView(String viewPath) throws ConfigurationException {
		BinaryData source = FileManager.getInstance().getDataOrNull(viewPath);
		if (source == null) {
			throw new ConfigurationException("View file not found: " + viewPath);
		}

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewServlet.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource((Content) source);

		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		UIElement uiElement = context.getInstance(config);
		if (!(uiElement instanceof ViewElement)) {
			throw new ConfigurationException(
				"Expected ViewElement but got: " + uiElement.getClass().getName());
		}
		return (ViewElement) uiElement;
	}

	/**
	 * Renders the HTML page with the root control using the {@link ViewControl#write} path.
	 *
	 * <p>
	 * Uses the {@link ViewDisplayContext} directly, without the old-world
	 * {@link com.top_logic.layout.DisplayContext} scope chain.
	 * </p>
	 */
	private void renderPage(HttpServletRequest request, HttpServletResponse response,
			ViewControl rootControl, ViewDisplayContext viewContext) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String contextPath = viewContext.getContextPath();

		TagWriter out = new TagWriter(response.getWriter());

		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginBeginTag(HTMLConstants.HTML);
		out.writeAttribute("lang", "en");
		out.endBeginTag();

		out.beginBeginTag(HTMLConstants.HEAD);
		out.endBeginTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("charset", "UTF-8");
		out.endEmptyTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("name", "viewport");
		out.writeAttribute("content", "width=device-width, initial-scale=1.0");
		out.endEmptyTag();
		out.beginBeginTag(HTMLConstants.TITLE);
		out.endBeginTag();
		out.writeText("TopLogic View");
		out.endTag(HTMLConstants.TITLE);
		// Include the React bridge script from the react module's webapp resources.
		out.beginBeginTag(HTMLConstants.SCRIPT);
		out.writeAttribute("src", contextPath + "/script/tl-react/tl-react-bridge.js");
		out.endBeginTag();
		out.endTag(HTMLConstants.SCRIPT);
		out.endTag(HTMLConstants.HEAD);

		out.beginBeginTag(HTMLConstants.BODY);
		out.endBeginTag();

		// Delegate rendering to the control itself. ReactControl.internalWrite() writes
		// the mount-point div and the TLReact.mount() bootstrap script.
		rootControl.write(viewContext, out);

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();
	}

}
