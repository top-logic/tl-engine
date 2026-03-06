/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.JSFileCompiler;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultViewDisplayContext;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that bootstraps the view-based UI.
 *
 * <p>
 * Loads a {@code .view.xml} file via {@link ViewLoader}, creates per-session control trees via
 * {@link UIElement#createControl(ViewContext)}, and renders the initial HTML page using
 * {@link TagWriter} and {@link ViewControl#write(ViewDisplayContext, TagWriter)}.
 * </p>
 *
 * <p>
 * The servlet is mapped to {@code /view/*}. The URL structure is:
 * </p>
 * <ul>
 * <li>{@code /view/} - Serves the window-name bootstrap page</li>
 * <li>{@code /view/<windowName>/} - Renders the default view for the given tab</li>
 * <li>{@code /view/<windowName>/some.view.xml} - Renders a specific view for the given tab</li>
 * </ul>
 *
 * <p>
 * <b>Tab identity:</b> Each browser tab is identified by a unique window name. The browser's
 * {@code window.name} property persists across page reloads (F5) but is empty in new or duplicated
 * tabs. On the first request without a window name, the servlet serves a small JavaScript bootstrap
 * page that checks or creates {@code window.name} and redirects to a URL that includes the window
 * name as the first path segment. This ensures that each tab gets its own independent subsession.
 * </p>
 */
public class ViewServlet extends TopLogicServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}

		String pathInfo = request.getPathInfo();
		String windowName = extractWindowName(pathInfo);

		if (windowName == null) {
			writeBootstrapPage(request, response);
			return;
		}

		String viewPath = resolveViewPath(pathInfo);

		ViewElement view;
		try {
			view = ViewLoader.getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to load view: " + viewPath, ex, ViewServlet.class);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Failed to load view: " + ex.getMessage());
			return;
		}

		ViewDisplayContext displayContext = new DefaultViewDisplayContext(
			request.getContextPath(), windowName, SSEUpdateQueue.forSession(session));
		ViewContext viewContext = new ViewContext(displayContext);

		ViewControl rootControl = view.createControl(viewContext);

		renderPage(request, response, rootControl, displayContext);
	}

	/**
	 * Extracts the window name from the first path segment.
	 *
	 * <p>
	 * Window names start with {@code v} followed by alphanumeric characters (generated client-side).
	 * If the first path segment contains a dot, it is interpreted as a view file name rather than a
	 * window name.
	 * </p>
	 *
	 * @return the window name, or {@code null} if no valid window name is present.
	 */
	private String extractWindowName(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1) {
			return null;
		}
		// Remove leading slash.
		String path = pathInfo.substring(1);
		int slashIdx = path.indexOf('/');
		String firstSegment = slashIdx >= 0 ? path.substring(0, slashIdx) : path;

		if (firstSegment.isEmpty() || firstSegment.indexOf('.') >= 0) {
			// Contains a dot -> view file name, not a window name.
			return null;
		}
		if (firstSegment.charAt(0) != 'v') {
			// Window names start with 'v'.
			return null;
		}

		return firstSegment;
	}

	/**
	 * Resolves the view file path from the request's path info.
	 *
	 * <p>
	 * Skips the first path segment (window name) and uses the rest as the view file name. When no
	 * view file is specified, falls back to the default view configured in
	 * {@link ViewConfig#getDefaultView()}.
	 * </p>
	 */
	private String resolveViewPath(String pathInfo) {
		// pathInfo is like /v1a2b3c/ or /v1a2b3c/app.view.xml
		String path = pathInfo.substring(1);
		int slashIdx = path.indexOf('/');
		if (slashIdx >= 0 && slashIdx < path.length() - 1) {
			String viewName = path.substring(slashIdx + 1);
			if (!viewName.isEmpty()) {
				return ViewLoader.VIEW_BASE_PATH + viewName;
			}
		}
		String defaultView = ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView();
		return ViewLoader.VIEW_BASE_PATH + defaultView;
	}

	/**
	 * Serves a small HTML page that checks or creates the browser's {@code window.name} and
	 * redirects to a URL containing the window name.
	 *
	 * <p>
	 * {@code window.name} persists across page reloads (F5) within the same tab, but is empty in
	 * newly opened or duplicated tabs. This ensures that each tab gets a unique window name, while
	 * reloads reuse the existing one.
	 * </p>
	 */
	private void writeBootstrapPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		String basePath = request.getContextPath() + request.getServletPath();

		TagWriter out = new TagWriter(response.getWriter());
		out.writeContent(HTMLConstants.DOCTYPE_HTML);
		out.beginBeginTag(HTMLConstants.HTML);
		out.endBeginTag();

		out.beginBeginTag(HTMLConstants.HEAD);
		out.endBeginTag();
		out.beginBeginTag(HTMLConstants.META);
		out.writeAttribute("charset", "UTF-8");
		out.endEmptyTag();
		out.endTag(HTMLConstants.HEAD);

		out.beginBeginTag(HTMLConstants.BODY);
		out.endBeginTag();

		out.beginScript();
		out.writeScript("(function() {");
		out.writeScript("var wn = window.name;");
		out.writeScript("if (!wn || wn.charAt(0) !== 'v') {");
		out.writeScript("var arr = new Uint8Array(8);");
		out.writeScript("crypto.getRandomValues(arr);");
		out.writeScript("wn = 'v';");
		out.writeScript("for (var i = 0; i < arr.length; i++) {");
		out.writeScript("wn += arr[i].toString(16).padStart(2, '0');");
		out.writeScript("}");
		out.writeScript("window.name = wn;");
		out.writeScript("}");
		out.writeScript("var base = ");
		out.writeJsString(basePath);
		out.writeScript(";");
		out.writeScript("var suffix = location.search + location.hash;");
		out.writeScript("window.location.replace(base + '/' + encodeURIComponent(wn) + '/' + suffix);");
		out.writeScript("})();");
		out.endScript();

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();
	}

	/**
	 * Renders the HTML page with the root control using the {@link ViewControl#write} path.
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
		// Load BAL (Browser Abstraction Layer) required by React control mount scripts.
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/bal.js");
		// Load all configured scripts (compiled TL bundle, React bridge as ES6 module, etc.).
		JSFileCompiler.getInstance().writeJavascriptRef(out, contextPath);
		// Load the theme stylesheet.
		ThemeFactory.getTheme().writeStyles(contextPath, out);
		out.endTag(HTMLConstants.HEAD);

		out.beginBeginTag(HTMLConstants.BODY);
		out.writeAttribute("data-window-name", viewContext.getWindowName());
		out.writeAttribute("data-context-path", viewContext.getContextPath());
		out.endBeginTag();

		// Delegate rendering to the control itself. ReactControl.write() outputs a
		// declarative div with data-react-module/data-react-state attributes.
		// The static tl-react-bridge script discovers and mounts these elements.
		rootControl.write(viewContext, out);

		out.endTag(HTMLConstants.BODY);
		out.endTag(HTMLConstants.HTML);

		out.flushBuffer();
	}

}
