/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.DOMModification;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.event.infoservice.InfoServiceXMLStringConverter;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.RevalidationVisitor;
import com.top_logic.tool.boundsec.HandlerResult;
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

		if (controlId == null || commandName == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId or command.");
			return;
		}

		if (arguments == null) {
			arguments = Map.of();
		}

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		CommandListener control = queue.getControl(controlId);
		if (control == null) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, "Control not found: " + controlId);
			return;
		}

		// Obtain the real DisplayContext set up by TopLogicServlet.
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		// Install subsession context and enable command phase.
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		HandlerResult result;
		boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
		try {
			result = control.executeCommand(displayContext, commandName, arguments);
		} finally {
			if (rootHandler != null) {
				rootHandler.enableUpdate(updateBefore);
			}
		}

		// Forward side effects: InfoService messages and legacy control repaints.
		forwardPendingUpdates(displayContext, rootHandler, queue);

		if (result.isSuccess()) {
			sendSuccess(response);
		} else {
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Command failed.");
		}
	}

	@SuppressWarnings("unchecked")
	private void handleState(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String controlId = request.getParameter("controlId");
		if (controlId == null) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing controlId parameter.");
			return;
		}

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		CommandListener control = queue.getControl(controlId);
		if (control instanceof ReactControl) {
			ReactControl reactControl = (ReactControl) control;
			PrintWriter writer = response.getWriter();
			writer.write(ReactControl.toJsonString(reactControl.getReactState()));
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

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(session);
		CommandListener control = queue.getControl(controlId);
		if (!(control instanceof UploadHandler)) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND,
				"Control does not support uploads: " + controlId);
			return;
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		// Install subsession context and enable command phase.
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

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
		forwardPendingUpdates(displayContext, rootHandler, queue);

		if (result.isSuccess()) {
			sendSuccess(response);
		} else {
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Upload handling failed.");
		}
	}

	/**
	 * Installs the subsession context for the given window name.
	 *
	 * @return The {@link SubsessionHandler} if found, or {@code null}.
	 */
	private SubsessionHandler installSubSession(DisplayContext displayContext, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			return null;
		}
		TLSessionContext sessionContext = TLContextManager.getSession();
		if (sessionContext == null) {
			return null;
		}
		ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
		SubsessionHandler rootHandler = handlersRegistry.getContentHandler(windowName);
		if (rootHandler != null) {
			TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
			displayContext.installSubSessionContext(subSession);
		}
		return rootHandler;
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
			SSEUpdateQueue queue) {
		// Forward InfoService messages.
		if (displayContext.isSet(InfoService.INFO_SERVICE_ENTRIES)) {
			List<HTMLFragment> entries = displayContext.get(InfoService.INFO_SERVICE_ENTRIES);
			if (!entries.isEmpty()) {
				String jsCode = InfoServiceXMLStringConverter.getJSInvocation(displayContext, entries);
				queue.enqueue(JSSnipplet.create().setCode(jsCode));
			}
		}

		// Collect and forward pending legacy control repaints.
		forwardLegacyControlUpdates(displayContext, rootHandler, queue);
	}

	/**
	 * Runs the standard {@link RevalidationVisitor} to collect pending control repaints and
	 * forwards them as SSE events.
	 *
	 * <p>
	 * This mirrors the revalidation step in AJAXServlet, but instead of serializing the
	 * {@link ClientAction}s to XML, the actions are converted to SSE protocol events
	 * ({@link com.top_logic.layout.react.protocol.ElementReplacement},
	 * {@link com.top_logic.layout.react.protocol.ContentReplacement}) and delivered via the SSE
	 * channel.
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

		CollectingUpdateWriter collector =
			new CollectingUpdateWriter(displayContext, new TagWriter(new StringWriter()), "UTF-8", null);
		RevalidationVisitor.runValidation(mainLayout, collector);

		for (ClientAction action : collector.getCollectedActions()) {
			SSEEvent event = toSSEEvent(displayContext, action);
			if (event != null) {
				queue.enqueue(event);
			}
		}
	}

	/**
	 * Converts a traditional {@link ClientAction} to the corresponding SSE protocol event.
	 *
	 * @return The SSE event, or {@code null} if the action type is not supported.
	 */
	private SSEEvent toSSEEvent(DisplayContext context, ClientAction action) {
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
		}
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
	 * An {@link UpdateWriter} that collects {@link ClientAction} objects instead of serializing
	 * them to XML.
	 */
	private static class CollectingUpdateWriter extends UpdateWriter {

		private final List<ClientAction> _collected = new ArrayList<>();

		CollectingUpdateWriter(DisplayContext context, TagWriter out, String encoding, Integer sequence) {
			super(context, out, encoding, sequence);
		}

		@Override
		public void add(ClientAction action) {
			if (action != null) {
				_collected.add(action);
			}
		}

		List<ClientAction> getCollectedActions() {
			return _collected;
		}
	}

}
