/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.Control;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that bootstraps the view-based UI.
 *
 * <p>
 * Loads a {@code .view.xml} file, parses it via {@link TypedConfiguration} into a shared
 * {@link UIElement} tree, creates per-session control trees via
 * {@link UIElement#createControl(ViewContext)}, and renders the initial HTML page that the React
 * client mounts.
 * </p>
 *
 * <p>
 * The servlet is mapped to {@code /view/*}. The path info determines which view file to load (e.g.,
 * {@code /view/main.view.xml} loads {@code /WEB-INF/views/main.view.xml}).
 * </p>
 *
 * <p>
 * <b>Scope management:</b> {@link ReactControl#internalWrite} requires a fully initialized scope
 * chain including a {@link com.top_logic.layout.LayoutContext}, which depends on a
 * {@link com.top_logic.mig.html.layout.MainLayout}. Since the view system is independent of the
 * traditional layout component tree, this servlet renders the HTML page frame directly and
 * initializes controls via the {@link ReactControl} state API rather than calling
 * {@link Control#write}. A future iteration will provide a proper view-system scope chain so that
 * the standard {@link Control#write} path can be used.
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
		if (viewPath == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
				"No view path specified. Use /view/<name>.view.xml");
			return;
		}

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

		renderPage(request, response, rootControl, frameScope, queue);
	}

	/**
	 * Resolves the view file path from the request's path info.
	 *
	 * @return The webapp-relative path to the view XML file, or {@code null} if no path was
	 *         specified.
	 */
	private String resolveViewPath(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.length() <= 1) {
			return null;
		}
		// Strip leading slash from pathInfo.
		String viewName = pathInfo.substring(1);
		return VIEW_BASE_PATH + viewName;
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
	 * Renders the HTML page with the root control's mount point.
	 *
	 * <p>
	 * Since {@link ReactControl#internalWrite} depends on a
	 * {@link com.top_logic.layout.LayoutContext} from the traditional component tree, the servlet
	 * renders the page frame and mount-point directly. The control is registered with the
	 * {@link SSEUpdateQueue} for command dispatch, and its state is serialized as JSON for the React
	 * client.
	 * </p>
	 */
	private void renderPage(HttpServletRequest request, HttpServletResponse response,
			Control rootControl, ViewFrameScope frameScope, SSEUpdateQueue queue) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String contextPath = request.getContextPath();

		// Initialize the root ReactControl: assign ID and register with SSE queue.
		// This is normally done during internalWrite(), but we bootstrap manually here.
		String controlId;
		String reactModule;
		String stateJson;

		if (rootControl instanceof ReactControl) {
			ReactControl rc = (ReactControl) rootControl;
			rc.fetchID(frameScope);
			controlId = rc.getID();
			reactModule = rc.getReactModule();
			stateJson = ReactControl.toJsonString(rc.getReactState(), frameScope, queue);

			// Register the control so that ReactServlet can dispatch commands to it.
			queue.registerControl(rc);
		} else {
			// Fallback: unsupported control type.
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Root control is not a ReactControl. View system currently requires React-based controls.");
			return;
		}

		PrintWriter out = response.getWriter();
		out.write("<!DOCTYPE html>\n");
		out.write("<html lang=\"en\">\n");
		out.write("<head>\n");
		out.write("  <meta charset=\"UTF-8\">\n");
		out.write("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
		out.write("  <title>TopLogic View</title>\n");
		// Include the React bridge script from the react module's webapp resources.
		out.write("  <script src=\"");
		out.write(contextPath);
		out.write("/script/tl-react/tl-react-bridge.js\"></script>\n");
		out.write("</head>\n");
		out.write("<body>\n");
		// Mount-point div for the React component.
		out.write("  <div id=\"");
		out.write(controlId);
		out.write("\" data-react-module=\"");
		out.write(reactModule);
		out.write("\" data-react-state=\"");
		out.write(escapeHtmlAttribute(stateJson));
		out.write("\"></div>\n");
		// Bootstrap script: mount the React component.
		// TODO: The windowName parameter is currently empty because the view system does not
		// use the traditional subsession/window mechanism. A future iteration will provide a
		// view-specific subsession identifier so that ReactServlet.installSubSession() can
		// resolve the correct context for command dispatch.
		out.write("  <script>\n");
		out.write("    TLReact.mount('");
		out.write(controlId);
		out.write("', '");
		out.write(reactModule);
		out.write("', ");
		out.write(stateJson);
		out.write(", '', '");
		out.write(contextPath);
		out.write("');\n");
		out.write("  </script>\n");
		out.write("</body>\n");
		out.write("</html>\n");
		out.flush();
	}

	/**
	 * Escapes a string for use in an HTML attribute value (double-quoted context).
	 */
	private static String escapeHtmlAttribute(String value) {
		StringBuilder sb = new StringBuilder(value.length() + 16);
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '&':
					sb.append("&amp;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				default:
					sb.append(c);
					break;
			}
		}
		return sb.toString();
	}
}
