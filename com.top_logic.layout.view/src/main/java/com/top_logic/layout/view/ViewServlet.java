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
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LocalScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.TLContext;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that bootstraps the view-based UI.
 *
 * <p>
 * Loads a {@code .view.xml} file, parses it via {@link TypedConfiguration} into a shared
 * {@link UIElement} tree, creates per-session control trees via
 * {@link UIElement#createControl(ViewContext)}, and renders the initial HTML page using
 * {@link TagWriter} and {@link Control#write(DisplayContext, TagWriter)}.
 * </p>
 *
 * <p>
 * The servlet is mapped to {@code /view/*}. The path info determines which view file to load (e.g.,
 * {@code /view/main.view.xml} loads {@code /WEB-INF/views/main.view.xml}).
 * </p>
 *
 * <p>
 * <b>Scope management:</b> The servlet sets up a view-specific scope chain consisting of a
 * {@link ViewFrameScope} (for ID generation and command listener management), a
 * {@link ViewLayoutContext} (providing a {@link WindowId} for React client mount), and a
 * {@link LocalScope} as the root {@link ControlScope}. This allows the standard
 * {@link Control#write(DisplayContext, TagWriter)} path to work without depending on the traditional
 * {@link com.top_logic.mig.html.layout.MainLayout} component tree.
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

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		ViewFrameScope frameScope = new ViewFrameScope();
		ViewContext viewContext = new ViewContext(frameScope, queue);

		Control rootControl = view.createControl(viewContext);

		renderPage(request, response, rootControl, frameScope);
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
	 * Renders the HTML page with the root control using the standard {@link Control#write} path.
	 *
	 * <p>
	 * Sets up the view-specific scope chain on the current {@link DisplayContext} and delegates
	 * rendering to the control itself via {@link Control#write(DisplayContext, TagWriter)}.
	 * </p>
	 */
	private void renderPage(HttpServletRequest request, HttpServletResponse response,
			Control rootControl, ViewFrameScope frameScope) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		// Install view-specific LayoutContext on the current SubSession so that
		// ReactControl.internalWrite() can resolve the WindowId.
		TLContext subSession = (TLContext) displayContext.getSubSessionContext();
		subSession.initLayoutContext(new ViewLayoutContext(new WindowId("view")));

		// Set up the root ControlScope so that Control.write() can attach controls.
		ControlScope rootScope = new LocalScope(frameScope, false);
		displayContext.initScope(rootScope);

		String contextPath = displayContext.getContextPath();

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
		rootControl.write(displayContext, out);

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();
	}
}
