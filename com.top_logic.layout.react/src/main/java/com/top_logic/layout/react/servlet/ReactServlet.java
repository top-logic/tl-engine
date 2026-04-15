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
import java.util.concurrent.locks.ReentrantLock;

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
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.DefaultInfoServiceItem;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.event.infoservice.InfoServiceXMLStringConverter;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.Property;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.RevalidationVisitor;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
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

		SSEUpdateQueue queue = getWindowQueue(session, windowName);
		if (queue == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = queue.getControl(controlId);
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

		SSEUpdateQueue queue = getWindowQueue(session, windowName);
		if (queue == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = queue.getControl(controlId);
		if (!(control instanceof TooltipProvider)) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND,
				"Control does not provide tooltips: " + controlId);
			return;
		}

		String key = request.getParameter("key");
		TooltipContent content = ((TooltipProvider) control).getTooltipContent(key);
		if (content == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No session.");
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
			// Acquire the request lock so the close callback (which may patch the opener's
			// snackbar state and flush SSE events) does not race with concurrent commands.
			ReentrantLock requestLock = registry.getRequestLock();
			requestLock.lock();
			try {
				registry.windowClosed(closedWindowId);
			} finally {
				requestLock.unlock();
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

		if (controlId == null || commandName == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId or command.");
			return;
		}

		if (arguments == null) {
			arguments = Map.of();
		}

		SSEUpdateQueue queue = getWindowQueue(session, windowName);
		if (queue == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = queue.getControl(controlId);
		if (control == null) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
			return;
		}

		// Obtain the real DisplayContext set up by TopLogicServlet.
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		// Install subsession context and enable command phase.
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			HandlerResult result;
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				result = control.executeCommand(commandName, arguments);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Forward side effects: InfoService messages and legacy control repaints.
			forwardPendingUpdates(displayContext, rootHandler, queue, control);

			// Synthesize model events so that observable models receive changes
			// from this command before the SSE queue is flushed.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);

			if (result.isSuccess()) {
				sendSuccess(response);
			} else {
				// Show error in snackbar instead of returning HTTP 500.
				showCommandError(result, queue, control);
				sendSuccess(response);
			}
		} finally {
			requestLock.unlock();
		}
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

		SSEUpdateQueue queue = getWindowQueue(session, windowName);
		if (queue == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown window: " + windowName);
			return;
		}
		ReactCommandTarget control = queue.getControl(controlId);
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

		SSEUpdateQueue queue = getWindowQueue(session, windowName);
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

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		// Install subsession context and enable command phase.
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			HandlerResult result;
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			try {
				Collection<Part> parts = request.getParts();
				result = ((UploadHandler) control).handleUpload(displayContext, parts);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Forward side effects: InfoService messages and legacy control repaints.
			forwardPendingUpdates(displayContext, rootHandler, queue, control);

			if (result.isSuccess()) {
				sendSuccess(response);
			} else {
				sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Upload handling failed.");
			}
		} finally {
			requestLock.unlock();
		}
	}

	/**
	 * Installs the subsession context for the given window name.
	 *
	 * <p>
	 * The {@link TLSubSessionContext} is created by the <code>ViewServlet</code> when the React
	 * page is first rendered and is stored in the {@link TLSessionContext} under the window name.
	 * This method looks it up and installs it on the {@link DisplayContext} so that
	 * {@link com.top_logic.util.TLContext#getContext()} is available during command execution.
	 * </p>
	 *
	 * @return The {@link SubsessionHandler} if found, or {@code null}. The handler is only present
	 *         for windows that use the traditional layout engine.
	 */
	private SubsessionHandler installSubSession(DisplayContext displayContext, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			return null;
		}
		TLSessionContext sessionContext = TLContextManager.getSession();
		if (sessionContext == null) {
			return null;
		}

		// Install SubSession on the current interaction (created by ViewServlet at page load).
		TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
		if (subSession != null) {
			displayContext.installSubSessionContext(subSession);
		} else {
			Logger.warn("No SubSession found for window '" + windowName
				+ "'. The view page may not have been loaded yet.", ReactServlet.class);
		}

		// SubsessionHandler is only present for windows with traditional layout.
		ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
		return handlersRegistry.getContentHandler(windowName);
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
				ErrorSink errorSink = control instanceof ReactControl rc ? rc.getReactContext().getErrorSink() : null;
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
	 * Shows a command error in the snackbar via {@link ErrorSink}.
	 *
	 * <p>
	 * Instead of returning HTTP 500, the error message from the {@link HandlerResult} is forwarded
	 * to the snackbar so the user sees what went wrong.
	 * </p>
	 */
	private void showCommandError(HandlerResult result, SSEUpdateQueue queue, ReactCommandTarget control) {
		ErrorSink errorSink = control instanceof ReactControl rc ? rc.getReactContext().getErrorSink() : null;
		if (errorSink != null) {
			Resources resources = Resources.getInstance();

			ResKey titleKey = result.getErrorTitle();
			String title = titleKey != null ? resources.getString(titleKey) : "Command failed.";

			ResKey messageKey = result.getErrorMessage();
			String details = messageKey != null ? resources.getString(messageKey) : null;

			String text;
			if (details != null && !details.isEmpty() && !details.equals("null")) {
				text = title + " " + details;
			} else {
				text = title;
			}
			errorSink.showError(com.top_logic.layout.basic.fragments.Fragments.text(text));
		} else {
			Logger.warn("No ErrorSink available to show command error: " + result.getErrorTitle(),
				ReactServlet.class);
		}
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
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.write("{\"success\":false,\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
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
