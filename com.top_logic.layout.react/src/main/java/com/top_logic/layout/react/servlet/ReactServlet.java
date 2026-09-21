/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.AbstractCssClassUpdate;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.DOMModification;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.DefaultInfoServiceItem;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.event.infoservice.InfoServiceXMLStringConverter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.Property;
import com.top_logic.layout.react.protocol.RouteVetoEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.window.ElementPicker;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.RevalidationVisitor;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that handles React client commands.
 *
 * <p>
 * Extends {@link TopLogicServlet} to get a proper {@link DisplayContext} with session and subsession
 * setup. The client sends JSON-encoded command requests via POST. The servlet resolves the target
 * control from the session-scoped {@link SSEUpdateQueue} and dispatches the command. Any resulting
 * state updates are delivered via SSE.
 * </p>
 *
 * <p>
 * When a React command or upload modifies the model of a traditional (legacy) control, that
 * control's pending repaint is collected via the standard {@link RevalidationVisitor} and forwarded
 * as SSE {@link com.top_logic.layout.react.protocol.ElementReplacement} events, so the browser DOM
 * is updated without an extra AJAX round-trip.
 * </p>
 */
@MultipartConfig
public class ReactServlet extends TopLogicServlet {

	/**
	 * Machine-readable error code signaling that the server-side UI state for the requesting page
	 * no longer exists: the request has no session, or the session does not know the page's
	 * window (e.g. the session was replaced underneath the open page by a login or logout, or the
	 * server was restarted).
	 *
	 * <p>
	 * The command channel of the React client reacts to a rejection carrying this code by
	 * re-bootstrapping the page, instead of leaving a page whose interactions are all rejected
	 * appearing frozen. The client-side counterpart of this constant is
	 * {@code ERROR_CODE_STALE_UI} in {@code command-channel.ts}.
	 * </p>
	 */
	public static final String ERROR_CODE_STALE_UI = "stale-ui";

	/** Name of the global command the client sends when the browser navigated in its history. */
	private static final String CMD_NAVIGATE_TO_ROUTE = "navigateToRoute";

	/** Name of the {@link #CMD_NAVIGATE_TO_ROUTE} argument holding the URL to adopt. */
	private static final String ARG_URL = "url";

	/**
	 * Name of the global command the client sends when it refused a selected file because it
	 * exceeds {@link UploadSupport#maxUploadSize()}.
	 */
	private static final String CMD_UPLOAD_REJECTED = "uploadRejected";

	/** Name of the {@link #CMD_UPLOAD_REJECTED} argument holding the name of the refused file. */
	private static final String ARG_FILE_NAME = "fileName";

	/** Name of the {@link #CMD_UPLOAD_REJECTED} argument holding the size of the refused file. */
	private static final String ARG_SIZE = "size";

	/**
	 * Request attribute through which Jetty takes the {@link MultipartConfigElement} to apply to
	 * the body of the current request, mirroring {@code ServletContextRequest.MULTIPART_CONFIG_ELEMENT}
	 * of Jetty's Servlet integration.
	 *
	 * <p>
	 * Set before the parts are requested, it supersedes the {@code multipart-config} of the servlet
	 * declaration, so the size limit is taken from the application configuration instead of the
	 * deployment descriptor. Containers that do not know the attribute ignore it; there, the limit
	 * is enforced by the explicit checks in {@link #handleUpload}.
	 * </p>
	 */
	private static final String MULTIPART_CONFIG_ATTRIBUTE = "org.eclipse.jetty.multipartConfig";

	/**
	 * Size in bytes up to which an uploaded part is buffered in memory instead of being written to
	 * a temporary file. Equals the {@code file-size-threshold} of the servlet's
	 * {@code multipart-config} declaration.
	 */
	private static final int FILE_SIZE_THRESHOLD = 8192;

	/**
	 * This endpoint answers {@code XMLHttpRequest}s, for whose caller the check's redirect to an
	 * HTML page is of no use. Skipping it also keeps a command that arrives while the session is
	 * ending from racing the reloading page for the check's one-shot marker - a race whose loser is
	 * told that cookies cannot be set.
	 */
	@Override
	protected boolean isCookieCheckRequired() {
		return false;
	}

	/**
	 * Answers a request whose session is gone with the error code the client reloads on, rather than
	 * with the empty response the inherited implementation would produce.
	 */
	@Override
	protected void handleNoSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ERROR_CODE_STALE_UI, "No session.");
	}

	/**
	 * Announces the request to the session's windows before answering it.
	 *
	 * <p>
	 * Every request through this servlet restarts the session's inactivity timeout as a side effect.
	 * A control counting down to the end of the session has no other way of learning that, so it is
	 * told here, at the one point every React request passes through.
	 * </p>
	 *
	 * @see ReactWindowRegistry#noteActivity(HttpSession)
	 */
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			ReactWindowRegistry.forSession(session).noteActivity(session);
		}
		super.doService(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}

		String pathInfo = request.getPathInfo();
		if ("/data".equals(pathInfo)) {
			handleDataDownload(request, response, session);
		} else if ("/tooltip".equals(pathInfo)) {
			handleTooltipRequest(request, response, session);
		} else if ("/i18n".equals(pathInfo)) {
			handleI18N(request, response);
		} else {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
		}
	}

	private void handleDataDownload(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String controlId = request.getParameter("controlId");
		String windowName = request.getParameter("windowName");
		if (controlId == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId parameter.");
			return;
		}

		WindowContext window = resolveWindow(request, session, windowName);
		if (window.queue() == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = window.queue().getControl(controlId);
		if (!(control instanceof DataProvider)) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND,
				"Control does not provide data: " + controlId);
			return;
		}

		String key = request.getParameter("key");
		BinaryData data = ((DataProvider) control).getDownloadData(key);
		if (data == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType(data.getContentType());
		long size = data.getSize();
		if (size >= 0) {
			response.setContentLengthLong(size);
		}

		try (OutputStream out = response.getOutputStream()) {
			data.deliverTo(out);
		}
	}

	private void handleTooltipRequest(HttpServletRequest request, HttpServletResponse response,
			HttpSession session) throws IOException {
		String controlId = request.getParameter("controlId");
		String windowName = request.getParameter("windowName");
		if (controlId == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId parameter.");
			return;
		}

		WindowContext window = resolveWindow(request, session, windowName);
		if (window.queue() == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = window.queue().getControl(controlId);
		if (!(control instanceof TooltipProvider)) {
			// A control without tooltips (e.g. a table whose cells probe for an optional tooltip) is
			// a normal case, not an error: answer "no tooltip" rather than a 404 (which the browser
			// logs as a failed request).
			sendNoTooltip(response);
			return;
		}

		String key = request.getParameter("key");
		TooltipContent content = ((TooltipProvider) control).getTooltipContent(key);
		if (content == null) {
			sendNoTooltip(response);
			return;
		}

		response.setContentType("application/json; charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			out.write('{');
			writeTooltipJsonField(out, "html", content.getHtml());
			String caption = content.getCaption();
			if (caption != null) {
				out.write(',');
				writeTooltipJsonField(out, "caption", caption);
			}
			if (content.isInteractive()) {
				out.write(",\"interactive\":true");
			}
			out.write('}');
		}
	}

	/**
	 * Answers a tooltip request for which no tooltip is available with a normal {@code 200} response
	 * carrying a JSON {@code null} body. The client treats {@code null} as "no tooltip", so this
	 * avoids logging a spurious error for the common case of a control that simply has no tooltip.
	 */
	private static void sendNoTooltip(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json; charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			out.write("null");
		}
	}

	private static void writeTooltipJsonField(PrintWriter out, String name, String value) {
		out.write('"');
		out.write(name);
		out.write("\":");
		writeTooltipJsonString(out, value);
	}

	private static void writeTooltipJsonString(PrintWriter out, String value) {
		out.write('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"':  out.write("\\\""); break;
				case '\\': out.write("\\\\"); break;
				case '\n': out.write("\\n");  break;
				case '\r': out.write("\\r");  break;
				case '\t': out.write("\\t");  break;
				default:
					if (c < 0x20) {
						out.write(String.format("\\u%04x", (int) c));
					} else {
						out.write(c);
					}
			}
		}
		out.write('"');
	}

	private void handleI18N(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String keysParam = request.getParameter("keys");
		if (keysParam == null || keysParam.isEmpty()) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing keys parameter.");
			return;
		}

		// Install subsession so Resources.getInstance() resolves the user's locale.
		String windowName = request.getParameter("windowName");
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Resources resources = Resources.getInstance();
		Map<String, Object> result = new LinkedHashMap<>();
		for (String key : keysParam.split(",")) {
			String trimmed = key.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			ResKey resKey = ResKey.internalCreate(trimmed);
			String value = resources.getString(resKey);
			result.put(trimmed, value);
		}

		PrintWriter writer = response.getWriter();
		writer.write(JSON.toString(result));
		writer.flush();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ERROR_CODE_STALE_UI, "No session.");
			return;
		}

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing path.");
			return;
		}

		try {
			switch (pathInfo) {
				case "/command":
					handleCommand(request, response, session);
					break;
				case "/state":
					handleState(request, response, session);
					break;
				case "/upload":
					handleUpload(request, response, session);
					break;
				case ElementPicker.PICK_PATH:
					handlePick(request, response, session);
					break;
				default:
					sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
					break;
			}
		} catch (Exception ex) {
			Logger.error("Error handling React request: " + pathInfo, ex, ReactServlet.class);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error.");
		}
	}

	private SSEUpdateQueue getWindowQueue(HttpSession session, String windowName) {
		if (windowName == null || windowName.isEmpty()) {
			Logger.warn("Missing windowName in request.", ReactServlet.class);
			return null;
		}
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		return registry.getQueue(windowName);
	}

	/**
	 * The window a request addresses, resolved by {@link #resolveWindow(HttpServletRequest, HttpSession, String)}.
	 *
	 * @param displayContext
	 *        The {@link DisplayContext} of the request, carrying the window's
	 *        {@link TLSubSessionContext}.
	 * @param queue
	 *        The window's update queue, or {@code null} if the session does not know the window.
	 * @param rootHandler
	 *        The window's {@link SubsessionHandler}, or {@code null} for a window that does not use
	 *        the traditional layout engine.
	 */
	private record WindowContext(DisplayContext displayContext, SSEUpdateQueue queue, SubsessionHandler rootHandler) {
		// Pure value.
	}

	/**
	 * Resolves the window a request addresses: looks up the window's {@link SSEUpdateQueue} and
	 * installs the window's {@link TLSubSessionContext} on the request's {@link DisplayContext}.
	 *
	 * <p>
	 * Everything a request evaluates against a window - a command, a control's state, the content of
	 * a cell tooltip - runs in the context the window's controls belong to. Without it, resolving a
	 * label or a value of a model object has no user, no locale and no session-bound caches to work
	 * with.
	 * </p>
	 *
	 * @param request
	 *        The request naming the window.
	 * @param session
	 *        The session holding the window.
	 * @param windowName
	 *        The name of the addressed window.
	 * @return The resolved window. Its {@link WindowContext#queue()} is {@code null} if the session
	 *         does not know a window of that name.
	 */
	private WindowContext resolveWindow(HttpServletRequest request, HttpSession session, String windowName) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);
		return new WindowContext(displayContext, getWindowQueue(session, windowName), rootHandler);
	}

	@SuppressWarnings("unchecked")
	private void handleCommand(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String body = new String(request.getInputStream().readAllBytes(), "UTF-8");
		Object parsed;
		try {
			parsed = JSON.fromString(body);
		} catch (JSON.ParseException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + ex.getMessage());
			return;
		}
		if (!(parsed instanceof Map)) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Expected JSON object.");
			return;
		}

		Map<String, Object> commandData = (Map<String, Object>) parsed;
		String controlId = (String) commandData.get("controlId");
		String commandName = (String) commandData.get("command");
		String windowName = (String) commandData.get("windowName");
		Map<String, Object> arguments = (Map<String, Object>) commandData.get("arguments");

		// Handle window lifecycle commands (no control target).
		// Must be checked before the controlId null check since these commands have empty controlId.
		if ("windowClosed".equals(commandName)) {
			String closedWindowId = arguments != null ? (String) arguments.get("windowId") : null;
			ReactWindowRegistry registry = ReactWindowRegistry.forSession(request.getSession());
			if (arguments != null && Boolean.TRUE.equals(arguments.get("unload"))) {
				// Reported on beforeunload, which fires for a reload as well: keep the window's state
				// for a grace period instead of tearing it down.
				registry.windowUnloaded(closedWindowId);
				sendSuccess(response);
				return;
			}
			// Closing a window changes the display of the session: the close callback may patch the
			// opener's snackbar state, which must not race with concurrent commands and is delivered
			// when the interaction completes.
			try (Interaction interaction = registry.beginInteraction()) {
				registry.windowClosed(closedWindowId);
			}
			sendSuccess(response);
			return;
		}
		if ("windowBlocked".equals(commandName)) {
			// Popup was blocked by the browser. Could enqueue a snackbar event.
			// For now, just acknowledge.
			sendSuccess(response);
			return;
		}
		if (CMD_NAVIGATE_TO_ROUTE.equals(commandName)) {
			handleNavigateToRoute(request, response, session, windowName, arguments);
			return;
		}
		if (CMD_UPLOAD_REJECTED.equals(commandName)) {
			handleUploadRejected(request, response, session, windowName, controlId, arguments);
			return;
		}

		if (controlId == null || commandName == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId or command.");
			return;
		}

		if (arguments == null) {
			arguments = Map.of();
		}

		WindowContext window = resolveWindow(request, session, windowName);
		SSEUpdateQueue queue = window.queue();
		if (queue == null) {
			// Diagnostic for "controls don't react": the client posts a command for a window that
			// has no queue in this session (e.g. a stale tab after the window was discarded, or a
			// window-name mismatch). Log the known windows to compare against the requested one.
			Logger.warn("Command '" + commandName + "' for control '" + controlId
				+ "' targets unknown window '" + windowName + "'. Known windows: "
				+ ReactWindowRegistry.forSession(session).windowNames(), ReactServlet.class);
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, ERROR_CODE_STALE_UI,
				"Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = queue.getControl(controlId);
		if (control == null) {
			if (ReactFormFieldControl.CMD_VALUE_CHANGED.equals(commandName)) {
				// A debounced field value flushed after its control was disposed: the edit was
				// abandoned (e.g. the dialog was canceled), so dropping the value is the intended
				// outcome, not an error.
				Logger.debug("Dropped '" + commandName + "' for disposed control '" + controlId + "'.",
					ReactServlet.class);
				sendSuccess(response);
				return;
			}
			if (!queue.hasControls()) {
				// The window's queue holds no controls at all: it was created empty by an SSE
				// reconnect after the session was replaced underneath the open page. The page's
				// control tree lives in the discarded session - let the client re-bootstrap.
				Logger.warn("Command '" + commandName + "' for control '" + controlId + "' targets window '"
					+ windowName + "' without any registered controls (session replaced).", ReactServlet.class);
				sendError(response, HttpServletResponse.SC_NOT_FOUND, ERROR_CODE_STALE_UI,
					"Control not found: " + controlId);
				return;
			}
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
			return;
		}

		DisplayContext displayContext = window.displayContext();
		SubsessionHandler rootHandler = window.rootHandler();

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			// Capture the step for the script recorder before the command runs: the address is computed
			// against the current (pre-command) tree — exactly the state a replay resolves it against.
			recordCommand(queue, control, commandName, arguments);

			HandlerResult result;
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				result = control.executeClientCommand(commandName, arguments);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Forward side effects: InfoService messages and legacy control repaints.
			forwardPendingUpdates(displayContext, rootHandler, queue, control);

			// Synthesize model events so that observable models receive changes
			// from this command before the interaction delivers its updates.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);

			if (result.isSuccess()) {
				sendSuccess(response);
			} else {
				// Show error in snackbar instead of returning HTTP 500.
				CommandErrors.show(errorSink(control), result);
				sendSuccess(response);
			}
		}
	}

	/**
	 * Captures the command as a typed {@link com.top_logic.layout.react.control.ReactCommand} item if
	 * the window's {@link ScriptRecorder} is active.
	 *
	 * <p>
	 * The target control is translated to its stable semantic {@link ScriptingSession#addressOf(ReactControl)
	 * address}; a control that is not in the visible projection (e.g. a table cell sub-control) records
	 * with a {@code null} address rather than failing the command.
	 * </p>
	 */
	private void recordCommand(SSEUpdateQueue queue, ReactCommandTarget control, String commandName,
			Map<String, Object> arguments) {
		ScriptRecorder recorder = queue.getRecorder();
		if (!recorder.isRecording() || !(control instanceof ReactControl reactControl)) {
			return;
		}
		if (!reactControl.isRecordable(commandName)) {
			// Incidental view adjustments (scroll, column resize) and chrome are not user intent.
			return;
		}
		// Let the control rewrite session-bound arguments (e.g. option ids → business keys) into a
		// replay-stable typed item before capture.
		RecordedCommand recorded = reactControl.recordCommand(commandName, arguments);
		recorded.command().setAddress(ScriptingSession.forRoot(queue.getRootControl()).addressOf(reactControl));
		recorder.record(recorded.command(), recorded.coalescing());
	}

	/**
	 * Handles the {@link #CMD_NAVIGATE_TO_ROUTE} global command the client sends for a URL the
	 * browser moved to by itself - over the back and forward buttons, or over an address the user
	 * typed.
	 *
	 * <p>
	 * The URL is resolved against the display by {@link RouteManager#navigateToRoute(String)}. A
	 * page holding unsaved changes refuses to be left, and the refusal is complete before anything
	 * is asked: the display keeps the page, and the address bar is restored to it by a
	 * {@link RouteVetoEvent}. The user is then asked what is to become of the changes, and once they
	 * answered the display is taken to the URL as a
	 * {@link RouteManager#navigateToUrl(String) navigation}, which leaves them a history entry to
	 * come back from.
	 * </p>
	 *
	 * <p>
	 * A failure of any other kind restores the address bar in the same way and is logged, without a
	 * question the user could answer.
	 * </p>
	 */
	private void handleNavigateToRoute(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, String windowName, Map<String, Object> arguments) throws IOException {
		WindowContext window = resolveWindow(request, session, windowName);
		SSEUpdateQueue queue = window.queue();
		if (queue == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}

		RouteManager routeManager = queue.getRouteManager();
		if (routeManager == null) {
			Logger.warn("No RouteManager found for window '" + windowName + "'.", ReactServlet.class);
			sendSuccess(response);
			return;
		}

		String requested = arguments != null ? (String) arguments.get(ARG_URL) : null;
		String url = requested != null ? requested : "";

		// Adopting a URL changes the display like any other command does, and needs the same context
		// for it: the subsession the controls belong to, which the window resolution above installs -
		// without it a channel bound to a route parameter cannot look up the object the URL names -
		// the update phase that lets the controls write their state, and the interaction that keeps a
		// second request out of the tree meanwhile and delivers what the navigation changed.
		SubsessionHandler rootHandler = window.rootHandler();

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				try {
					routeManager.navigateToRoute(url);

					// The display has taken the URL as far as it can: a segment it cannot reproduce is
					// dropped, and what the display adds from here on is a navigation again.
					routeManager.finishAdoption();
					sendSuccess(response);
				} catch (ChannelVetoException veto) {
					Logger.info("Route navigation refused by unsaved changes for url '" + url + "'.",
						ReactServlet.class);

					// The refusal is complete before the question is put: display and address bar agree
					// on the page that is kept, so the user stays on it whatever they answer.
					restoreUrl(queue, routeManager);
					sendSuccess(response);

					askAndNavigate(queue, routeManager, url, veto);
				} catch (Exception ex) {
					Logger.info("Route navigation failed for url '" + url + "': " + ex.getMessage(),
						ReactServlet.class);

					restoreUrl(queue, routeManager);
					sendSuccess(response);
				}
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
		}
	}

	/**
	 * Ends the adoption of a URL the display does not take up, and restores the client's address bar
	 * to the URL the display composes.
	 *
	 * <p>
	 * The client shows the URL that was not reached - the browser moved there by itself - so it is
	 * sent the address of the page it is left on instead. That address is what the
	 * {@link RouteManager} records as shown from now on, without which the user's next navigation
	 * would be reported as a replacement of it rather than as a history entry.
	 * </p>
	 */
	private void restoreUrl(SSEUpdateQueue queue, RouteManager routeManager) {
		routeManager.cancelAdoption();

		queue.enqueue(RouteVetoEvent.create().setCurrentUrl(routeManager.currentUrl()));
	}

	/**
	 * Asks the user about the unsaved changes that refused a URL, and takes the display to that URL
	 * once they answered.
	 *
	 * <p>
	 * The client is back on the address of the page it keeps, so reaching the URL from here is a
	 * navigation the user gets a history entry for. A form deeper in the display refusing that
	 * navigation in turn is asked about exactly like the first one - which is what
	 * {@link DirtyConfirmDialogControl#guard(ReactContext, DialogManager, Runnable, Runnable)} does
	 * with the retry.
	 * </p>
	 */
	private void askAndNavigate(SSEUpdateQueue queue, RouteManager routeManager, String url,
			ChannelVetoException veto) {
		DialogManager dialogManager = queue.getDialogManager();
		ReactControl root = queue.getRootControl();
		ReactContext context = root != null ? root.getReactContext() : null;
		if (dialogManager == null || context == null) {
			Logger.warn("Nothing can ask about the unsaved changes refusing url '" + url
				+ "', which is therefore not reached.", ReactServlet.class);
			return;
		}

		DirtyConfirmDialogControl.openDialog(context, dialogManager, veto.getDirtyHandlers(),
			() -> DirtyConfirmDialogControl.guard(context, dialogManager, () -> navigate(routeManager, url), null),
			null);
	}

	/**
	 * Takes the display to the given URL, ending the adoption where the display refuses it.
	 *
	 * <p>
	 * A refusal leaves the display as it keeps it and is passed on, so that the unsaved changes
	 * behind it can be put to the user and the navigation run again. Nothing is reported to the
	 * client, which shows the address of the page it is on.
	 * </p>
	 */
	private static void navigate(RouteManager routeManager, String url) {
		try {
			routeManager.navigateToUrl(url);
		} catch (ChannelVetoException veto) {
			routeManager.cancelAdoption();
			throw veto;
		}
	}

	/**
	 * Handles the pick report posted by the picked window's client: resolves it through
	 * {@link ElementPicker} and delivers it under the requesting window's sub-session, so that the
	 * resulting channel and control updates flush to that window's SSE queue.
	 */
	@SuppressWarnings("unchecked")
	private void handlePick(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String body = new String(request.getInputStream().readAllBytes(), "UTF-8");
		Object parsed;
		try {
			parsed = JSON.fromString(body);
		} catch (JSON.ParseException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + ex.getMessage());
			return;
		}
		if (!(parsed instanceof Map)) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Expected JSON object.");
			return;
		}

		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		ElementPicker.Delivery delivery = ElementPicker.resolve(registry, (Map<String, Object>) parsed);
		if (delivery == null) {
			// Nothing to deliver, ElementPicker has logged why.
			sendSuccess(response);
			return;
		}

		String requesterWindowId = delivery.pick().requesterWindowId();
		SubsessionHandler rootHandler = resolveWindow(request, session, requesterWindowId).rootHandler();

		try (Interaction interaction = registry.beginInteraction()) {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				delivery.deliver();
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
			registry.synthesizeModelEvents(requesterWindowId);
		}
		sendSuccess(response);
	}

	@SuppressWarnings("unchecked")
	private void handleState(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String controlId = request.getParameter("controlId");
		String windowName = request.getParameter("windowName");
		if (controlId == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId parameter.");
			return;
		}

		WindowContext window = resolveWindow(request, session, windowName);
		if (window.queue() == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = window.queue().getControl(controlId);
		if (control instanceof ReactControl) {
			ReactControl reactControl = (ReactControl) control;
			PrintWriter writer = response.getWriter();

			writer.write(reactControl.stateAsJSON());
			writer.flush();
		} else {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
		}
	}

	private void handleUpload(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException, ServletException {
		String controlId = request.getParameter("controlId");
		String windowName = request.getParameter("windowName");
		if (controlId == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId parameter.");
			return;
		}

		WindowContext window = resolveWindow(request, session, windowName);
		SSEUpdateQueue queue = window.queue();
		if (queue == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = queue.getControl(controlId);
		if (!(control instanceof UploadHandler)) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND,
				"Control does not support uploads: " + controlId);
			return;
		}

		long limit = UploadSupport.maxUploadSize();
		if (limit > 0) {
			// Let the container apply the configured limit while it parses the body, instead of the
			// fixed one from the deployment descriptor.
			request.setAttribute(MULTIPART_CONFIG_ATTRIBUTE,
				new MultipartConfigElement("", limit, limit, FILE_SIZE_THRESHOLD));
		}

		DisplayContext displayContext = window.displayContext();
		SubsessionHandler rootHandler = window.rootHandler();

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			if (limit > 0 && request.getContentLengthLong() > limit) {
				rejectTooLarge(response, control, limit,
					"request of " + request.getContentLengthLong() + " bytes");
				return;
			}

			HandlerResult result;
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				Collection<Part> parts;
				try {
					parts = request.getParts();
				} catch (IllegalStateException ex) {
					if (limit <= 0) {
						// Uploads are unlimited, so the container refused the body for another
						// reason - report it like any other failure of the upload.
						throw ex;
					}
					// The size check of the container tripped while it parsed the body.
					rejectTooLarge(response, control, limit, ex.getMessage());
					return;
				}
				if (limit > 0) {
					// A request sent with chunked transfer encoding announces no content length; its
					// size is only known from the parts the container has parsed.
					Part oversized = oversizedPart(parts, limit);
					if (oversized != null) {
						rejectTooLarge(response, control, limit,
							"file '" + oversized.getSubmittedFileName() + "' of " + oversized.getSize() + " bytes");
						return;
					}
				}
				result = ((UploadHandler) control).handleUpload(displayContext, parts);
			} catch (Throwable ex) {
				result = CommandErrors.failure(ex, "Upload on " + control.getClass().getName(), ReactServlet.class);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Forward side effects: InfoService messages and legacy control repaints.
			forwardPendingUpdates(displayContext, rootHandler, queue, control);

			// Synthesize model events so that observable models (e.g. tables observing the uploaded
			// objects' type) receive the changes made during upload handling before the interaction
			// delivers its updates - otherwise the upload's effect only shows after a later rebuild.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);

			if (!result.isSuccess()) {
				// Show error in snackbar instead of returning HTTP 500.
				CommandErrors.show(errorSink(control), result);
			}
			sendSuccess(response);
		}
	}

	/**
	 * The first of the given parts whose size exceeds the given limit, <code>null</code> if all of
	 * them stay within it.
	 */
	private static Part oversizedPart(Collection<Part> parts, long limit) {
		for (Part part : parts) {
			if (part.getSize() > limit) {
				return part;
			}
		}
		return null;
	}

	/**
	 * Refuses an upload that exceeds the configured limit: tells the user about the limit and
	 * answers the request with {@link HttpServletResponse#SC_REQUEST_ENTITY_TOO_LARGE}.
	 *
	 * @param control
	 *        The control the upload was sent for, whose window shows the notice.
	 * @param limit
	 *        The limit in bytes that was exceeded.
	 * @param cause
	 *        What exceeded the limit, for the log entry.
	 */
	private void rejectTooLarge(HttpServletResponse response, ReactCommandTarget control, long limit, String cause)
			throws IOException {
		Logger.info("Upload refused, larger than the configured limit of " + limit + " bytes: " + cause,
			ReactServlet.class);
		showUploadTooLarge(control, limit);
		sendError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
			"Upload exceeds the limit of " + limit + " bytes.");
	}

	/**
	 * Shows the notice naming the upload size limit in the window of the given control.
	 *
	 * <p>
	 * A refused upload is not a malfunction, so the notice is shown as the plain message it is,
	 * rather than as the report of a failed command.
	 * </p>
	 *
	 * @param control
	 *        The control the refused upload was meant for.
	 * @param limit
	 *        The limit in bytes that was exceeded.
	 */
	private void showUploadTooLarge(ReactCommandTarget control, long limit) {
		ResKey message = I18NConstants.ERROR_UPLOAD_TOO_LARGE__LIMIT.fill(UploadSupport.sizeLabel(limit));
		ErrorSink sink = errorSink(control);
		if (sink == null) {
			Logger.warn("No ErrorSink available to show upload notice: " + message, ReactServlet.class);
			return;
		}
		sink.showError(Fragments.message(message));
	}

	/**
	 * Handles the {@link #CMD_UPLOAD_REJECTED} global command: the client refused a selected file
	 * as larger than {@link UploadSupport#maxUploadSize()} and did not transmit it, so the notice
	 * the user must see is produced here.
	 */
	private void handleUploadRejected(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, String windowName, String controlId, Map<String, Object> arguments)
			throws IOException {
		WindowContext window = resolveWindow(request, session, windowName);
		if (window.queue() == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = controlId != null ? window.queue().getControl(controlId) : null;
		Object fileName = arguments != null ? arguments.get(ARG_FILE_NAME) : null;
		Object size = arguments != null ? arguments.get(ARG_SIZE) : null;
		Logger.info("Upload of '" + fileName + "' (" + size + " bytes) refused by the client, larger than the "
			+ "configured limit.", ReactServlet.class);

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			showUploadTooLarge(control, UploadSupport.maxUploadSize());
		}
		sendSuccess(response);
	}

	/**
	 * Installs the subsession context for the given window name.
	 *
	 * <p>
	 * The {@link TLSubSessionContext} is created by the <code>ViewServlet</code> when the React
	 * page is first rendered and is stored in the {@link TLSessionContext} under the window name.
	 * This method looks it up and installs it on the {@link DisplayContext} so that
	 * {@link com.top_logic.util.TLContext#getContext()} is available while the request evaluates
	 * anything against the window's controls.
	 * </p>
	 *
	 * <p>
	 * A handler addressing a window reaches this through
	 * {@link #resolveWindow(HttpServletRequest, HttpSession, String)}, which pairs the subsession
	 * with the window's {@link SSEUpdateQueue}. {@link #handleI18N(HttpServletRequest, HttpServletResponse)}
	 * calls it directly, since it resolves resources of the window's locale without addressing any
	 * of its controls.
	 * </p>
	 *
	 * @return The {@link SubsessionHandler} if found, or {@code null}. The handler is only present
	 *         for windows that use the traditional layout engine.
	 */
	private SubsessionHandler installSubSession(DisplayContext displayContext, String windowName) {
		return ReactWindowReplay.installSubSession(displayContext, windowName);
	}

	/**
	 * Forwards any pending side effects from command execution via SSE.
	 *
	 * <p>
	 * This includes {@link InfoService} messages and pending repaints from traditional (legacy)
	 * controls whose models were modified during the React command or upload.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private void forwardPendingUpdates(DisplayContext displayContext, SubsessionHandler rootHandler,
			SSEUpdateQueue queue, ReactCommandTarget control) {
		// Forward InfoService messages.
		if (displayContext.isSet(InfoService.INFO_SERVICE_ENTRIES)) {
			List<HTMLFragment> entries = displayContext.get(InfoService.INFO_SERVICE_ENTRIES);
			if (!entries.isEmpty()) {
				ErrorSink errorSink = errorSink(control);
				if (errorSink != null) {
					forwardToErrorSink(entries, errorSink);
				} else {
					String jsCode = InfoServiceXMLStringConverter.getJSInvocation(displayContext, entries);
					queue.enqueue(JSSnipplet.create().setCode(jsCode));
				}
			}
		}

		// Collect and forward pending legacy control repaints.
		forwardLegacyControlUpdates(displayContext, rootHandler, queue);
	}

	/**
	 * The {@link ErrorSink} of the window the given command target lives in, or <code>null</code>
	 * if the target is not a {@link ReactControl}.
	 */
	private static ErrorSink errorSink(ReactCommandTarget control) {
		return control instanceof ReactControl rc ? rc.getReactContext().getErrorSink() : null;
	}

	private void forwardToErrorSink(List<HTMLFragment> entries, ErrorSink errorSink) {
		for (HTMLFragment entry : entries) {
			if (entry instanceof DefaultInfoServiceItem item) {
				HTMLFragment message = item.getMessage();
				String kindOfClass = item.getKindOfClass();

				if (InfoService.ERROR_CSS.equals(kindOfClass)) {
					errorSink.showError(message);
				} else if (InfoService.WARNING_CSS.equals(kindOfClass)) {
					errorSink.showWarning(message);
				} else {
					errorSink.showInfo(message);
				}
			} else {
				Logger.warn("InfoService entry is not a DefaultInfoServiceItem, cannot forward to ErrorSink: "
					+ entry.getClass().getName(), ReactServlet.class);
			}
		}
	}

	/**
	 * Runs the standard {@link RevalidationVisitor} to collect pending control repaints and
	 * forwards them as SSE events.
	 *
	 * <p>
	 * Each {@link ClientAction} is converted to its SSE equivalent <em>inside</em> the
	 * {@link SSEForwardingUpdateWriter#add(ClientAction)} callback, because at that point the
	 * {@link DisplayContext#getExecutionScope() execution scope} is still set to the control's
	 * {@link com.top_logic.layout.ControlScope} by
	 * {@link ControlSupport#revalidate(DisplayContext, com.top_logic.layout.UpdateQueue)}.
	 * Rendering the fragment later (after the scope is restored) would cause an "already attached
	 * to another scope" crash.
	 * </p>
	 */
	private void forwardLegacyControlUpdates(DisplayContext displayContext, SubsessionHandler rootHandler,
			SSEUpdateQueue queue) {
		if (rootHandler == null) {
			return;
		}
		MainLayout mainLayout = rootHandler.getMainLayout();
		if (mainLayout == null) {
			return;
		}

		SSEForwardingUpdateWriter forwarder =
			new SSEForwardingUpdateWriter(displayContext, new TagWriter(new StringWriter()), "UTF-8", null, queue);
		RevalidationVisitor.runValidation(mainLayout, forwarder);
	}

	private void sendSuccess(HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.write("{\"success\":true}");
		writer.flush();
	}

	private void sendError(HttpServletResponse response, int status, String message) throws IOException {
		sendError(response, status, null, message);
	}

	/**
	 * Writes a JSON error response, optionally carrying a machine-readable error code the client
	 * dispatches on (e.g. {@link #ERROR_CODE_STALE_UI}).
	 *
	 * @param errorCode
	 *        The machine-readable code identifying the error condition, or {@code null} for errors
	 *        the client only logs.
	 */
	private void sendError(HttpServletResponse response, int status, String errorCode, String message)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		StringBuilder json = new StringBuilder("{\"success\":false,");
		if (errorCode != null) {
			json.append("\"errorCode\":\"").append(errorCode).append("\",");
		}
		json.append("\"error\":\"").append(message.replace("\"", "\\\"")).append("\"}");
		writer.write(json.toString());
		writer.flush();
	}

	/**
	 * An {@link UpdateWriter} that converts each {@link ClientAction} to an SSE event immediately
	 * inside {@link #add(ClientAction)}.
	 *
	 * <p>
	 * This is critical for correctness: during {@code add()}, the {@link DisplayContext} still has
	 * the correct {@link com.top_logic.layout.ControlScope execution scope} set by
	 * {@link ControlSupport#revalidate(DisplayContext, com.top_logic.layout.UpdateQueue)}.
	 * Rendering a {@link DOMModification#getFragment() fragment} at this point calls
	 * {@link com.top_logic.layout.basic.AbstractControlBase#attach attach(scope)} which sees the
	 * same scope the control is already attached to and returns without error. Deferring the
	 * rendering to after revalidation would cause an "already attached to another scope" crash.
	 * </p>
	 */
	private static class SSEForwardingUpdateWriter extends UpdateWriter {

		private final SSEUpdateQueue _queue;

		SSEForwardingUpdateWriter(DisplayContext context, TagWriter out, String encoding, Integer sequence,
				SSEUpdateQueue queue) {
			super(context, out, encoding, sequence);
			_queue = queue;
		}

		@Override
		public void add(ClientAction action) {
			if (action == null) {
				return;
			}

			DisplayContext context = getDisplayContext();
			SSEEvent event = toSSEEvent(context, action);
			if (event != null) {
				_queue.enqueue(event);
			}
		}

		private SSEEvent toSSEEvent(DisplayContext context, ClientAction action) {
			// DOMModification subtypes: render the fragment to HTML while the scope is correct.
			if (action instanceof DOMModification) {
				DOMModification mod = (DOMModification) action;
				String elementId = mod.getElementID();
				String html = renderFragment(context, mod.getFragment());

				if (action instanceof ElementReplacement) {
					return com.top_logic.layout.react.protocol.ElementReplacement.create()
						.setElementId(elementId)
						.setHtml(html);
				}
				if (action instanceof ContentReplacement) {
					return com.top_logic.layout.react.protocol.ContentReplacement.create()
						.setElementId(elementId)
						.setHtml(html);
				}
				if (action instanceof RangeReplacement) {
					RangeReplacement range = (RangeReplacement) action;
					return com.top_logic.layout.react.protocol.RangeReplacement.create()
						.setStartId(elementId)
						.setStopId(range.getStopID())
						.setHtml(html);
				}
				if (action instanceof FragmentInsertion) {
					FragmentInsertion insertion = (FragmentInsertion) action;
					return com.top_logic.layout.react.protocol.FragmentInsertion.create()
						.setElementId(elementId)
						.setPosition(insertion.getPosition())
						.setHtml(html);
				}
			}

			// PropertyUpdate: evaluate DynamicText value to string.
			if (action instanceof PropertyUpdate) {
				PropertyUpdate propUpdate = (PropertyUpdate) action;
				String value = evaluateDynamicText(context, propUpdate.getValue());
				return com.top_logic.layout.react.protocol.PropertyUpdate.create()
					.setElementId(propUpdate.getElementID())
					.addProperty(Property.create()
						.setName(propUpdate.getProperty())
						.setValue(value));
			}

			// CssClassUpdate: evaluate CSS class content to string.
			if (action instanceof AbstractCssClassUpdate) {
				AbstractCssClassUpdate cssUpdate = (AbstractCssClassUpdate) action;
				StringBuilder sb = new StringBuilder();
				try {
					cssUpdate.writeCssClassContent(context, sb);
				} catch (IOException ex) {
					Logger.error("Failed to evaluate CSS class update.", ex, ReactServlet.class);
				}
				return com.top_logic.layout.react.protocol.CssClassUpdate.create()
					.setElementId(cssUpdate.getElementID())
					.setCssClass(sb.toString());
			}

			// JSFunctionCall: extract fields and serialize arguments as JSON.
			if (action instanceof JSFunctionCall) {
				JSFunctionCall call = (JSFunctionCall) action;
				String argsJson;
				try {
					argsJson = JSON.toString(Arrays.asList(call.getArguments()));
				} catch (Exception ex) {
					Logger.error("Failed to serialize JSFunctionCall arguments.", ex, ReactServlet.class);
					argsJson = "[]";
				}
				return FunctionCall.create()
					.setElementId(call.getElementID())
					.setFunctionRef(call.getFunctionReference())
					.setFunctionName(call.getFunctionName())
					.setArguments(argsJson);
			}

			// JSSnipplet: evaluate code or code fragment.
			if (action instanceof com.top_logic.base.services.simpleajax.JSSnipplet) {
				com.top_logic.base.services.simpleajax.JSSnipplet snipplet =
					(com.top_logic.base.services.simpleajax.JSSnipplet) action;
				String code;
				DynamicText codeFragment = snipplet.getCodeFragment();
				if (codeFragment != null) {
					code = evaluateDynamicText(context, codeFragment);
				} else {
					code = snipplet.getCode();
				}
				return JSSnipplet.create().setCode(code);
			}

			Logger.warn("Unsupported legacy ClientAction type for SSE forwarding: " + action.getClass().getName(),
				ReactServlet.class);
			return null;
		}

		private String renderFragment(DisplayContext context, HTMLFragment fragment) {
			StringWriter sw = new StringWriter();
			TagWriter tw = new TagWriter(sw);
			try {
				fragment.write(context, tw);
				tw.flush();
			} catch (IOException ex) {
				Logger.error("Failed to render legacy control fragment.", ex, ReactServlet.class);
				return "";
			}
			return sw.toString();
		}

		private String evaluateDynamicText(DisplayContext context, DynamicText text) {
			StringBuilder sb = new StringBuilder();
			try {
				text.append(context, sb);
			} catch (IOException ex) {
				Logger.error("Failed to evaluate DynamicText.", ex, ReactServlet.class);
			}
			return sb.toString();
		}
	}

}
