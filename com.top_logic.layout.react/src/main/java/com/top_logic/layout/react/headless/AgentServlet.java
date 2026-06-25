/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.TopLogicServlet;

/**
 * HTTP endpoint exposing the {@link AgentSession headless agent interface} of a live application
 * session.
 *
 * <p>
 * This is the integration point a non-browser consumer (a script-recorder/replay tool or an AI agent,
 * typically through a thin MCP wrapper) uses to drive a real session:
 * </p>
 * <ul>
 * <li>{@code GET  /agent-api/windows} &rarr; {@code {"windows":[...]}} — the windows that can be
 * observed.</li>
 * <li>{@code GET  /agent-api/observe?windowName=W} &rarr; the addressable state tree of window
 * {@code W} (an {@link AgentNodeView} as JSON). Add {@code &mode=actions} for the compact
 * affordance-first view: a flat list of just the actionable nodes.</li>
 * <li>{@code POST /agent-api/act} with body {@code {"windowName":W,"address":A,"command":C,"arguments":{…}}}
 * &rarr; resolves {@code A}, invokes {@code C}, and returns {@code {"success":b,"observation":{…}}}
 * with the resulting state tree. The command arguments may be given as either {@link #FIELD_ARGUMENTS} or {@link #FIELD_ARGS}.</li>
 * <li>{@code POST /agent-api/navigate} with body {@code {"windowName":W,"url":"access-control/groups"}}
 * &rarr; navigates the window's router to a route URL (for areas loaded by routing rather than an
 * in-place {@code selectItem}), returning {@code {"success":b,"url":…,"observation":{…}}}. {@code
 * success} is {@code true} only if the router actually reached the requested URL; a route into an
 * area whose participants are not registered yet is reported as {@code false} with a {@code message},
 * not a silent no-op.</li>
 * <li>{@code POST /agent-api/record/start} / {@code /record/stop} with body {@code {"windowName":W}}
 * &rarr; begin a fresh recording of the user's interactions, or stop it; both return
 * {@code {"recording":b,"steps":[…]}}. While recording, the browser command path captures each
 * dispatched command as an {@code {address,command,arguments}} step.</li>
 * <li>{@code GET /agent-api/record/steps?windowName=W} &rarr; the current recorder state and steps.</li>
 * <li>{@code POST /agent-api/record/assert} with body {@code {"windowName":W,"address":A,"expect":{…}?}}
 * &rarr; append an assertion step — the expected state given as {@code expect}, or the node's current
 * state captured if omitted. On replay it is verified, not dispatched.</li>
 * <li>{@code POST /agent-api/replay} with body {@code {"windowName":W,"steps":[…]}} &rarr; replays a
 * recorded (or hand-written) script through the same {@code act} path (assertion steps are verified
 * against live state), returning a per-step {@code results} list and the final {@code observation}.</li>
 * </ul>
 *
 * <p>
 * Acting dispatches through {@link com.top_logic.layout.react.control.ReactControl#executeCommand} —
 * the same path the browser command endpoint uses — under the same window
 * {@link ReactWindowRegistry#getRequestLock() request lock} and subsession context. After the command
 * runs, {@link ReactWindowRegistry#synthesizeModelEvents(String) model events are synthesized} so that
 * all derived state has settled before the response is built. The reply therefore carries a
 * <em>quiesced</em> observation: a headless caller never has to poll or await asynchronous SSE
 * delivery — it reads the fresh server state directly.
 * </p>
 *
 * <p>
 * The endpoint runs inside the authenticated {@link TopLogicServlet} context, so it acts as the
 * logged-in user with exactly the permissions the interactive UI would have.
 * </p>
 */
public class AgentServlet extends TopLogicServlet {

	// Request/response JSON field names and the request parameter names.
	private static final String FIELD_WINDOWS = "windows";

	private static final String FIELD_WINDOW_NAME = "windowName";

	private static final String FIELD_ADDRESS = "address";

	private static final String FIELD_COMMAND = "command";

	private static final String FIELD_ARGUMENTS = "arguments";

	private static final String FIELD_ARGS = "args";

	private static final String FIELD_MODE = "mode";

	private static final String MODE_ACTIONS = "actions";

	private static final String FIELD_SUCCESS = "success";

	private static final String FIELD_OBSERVATION = "observation";

	private static final String FIELD_URL = "url";

	private static final String FIELD_MESSAGE = "message";

	private static final String FIELD_RECORDING = "recording";

	private static final String FIELD_STEPS = "steps";

	private static final String FIELD_EXPECT = "expect";

	private static final String FIELD_RESULTS = "results";

	private static final String FIELD_ERROR = "error";

	private static final String FIELD_KEY = "key";

	private static final String FIELD_EXPECTED = "expected";

	private static final String FIELD_ACTUAL = "actual";

	private static final String FIELD_MISMATCHES = "mismatches";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No session.");
			return;
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String pathInfo = request.getPathInfo();
		try {
			if ("/windows".equals(pathInfo)) {
				handleWindows(response, session);
			} else if ("/observe".equals(pathInfo)) {
				handleObserve(request, response, session);
			} else if ("/record/steps".equals(pathInfo)) {
				handleRecordSteps(request, response, session);
			} else {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
			}
		} catch (IllegalArgumentException ex) {
			sendError(response, HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
		} catch (Exception ex) {
			Logger.error("Error handling agent request: " + pathInfo, ex, AgentServlet.class);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error.");
		}
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
		try {
			if ("/act".equals(pathInfo)) {
				handleAct(request, response, session);
			} else if ("/navigate".equals(pathInfo)) {
				handleNavigate(request, response, session);
			} else if ("/record/start".equals(pathInfo)) {
				handleRecordStartStop(request, response, session, true);
			} else if ("/record/stop".equals(pathInfo)) {
				handleRecordStartStop(request, response, session, false);
			} else if ("/record/assert".equals(pathInfo)) {
				handleRecordAssert(request, response, session);
			} else if ("/replay".equals(pathInfo)) {
				handleReplay(request, response, session);
			} else {
				sendError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
			}
		} catch (IllegalArgumentException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		} catch (Exception ex) {
			Logger.error("Error handling agent request: " + pathInfo, ex, AgentServlet.class);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error.");
		}
	}

	private void handleWindows(HttpServletResponse response, HttpSession session) throws IOException {
		List<String> windows = new ArrayList<>(ReactWindowRegistry.forSession(session).windowNames());
		write(response, "{\"" + FIELD_WINDOWS + "\":" + JSON.toString(windows) + "}");
	}

	private void handleObserve(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String windowName = request.getParameter(FIELD_WINDOW_NAME);
		SSEUpdateQueue queue = requireQueue(session, windowName);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		boolean actionsMode = MODE_ACTIONS.equals(request.getParameter(FIELD_MODE));

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			AgentSession agentSession = agentSession(queue);
			write(response, actionsMode ? agentSession.observeActionsJson() : agentSession.observeJson());
		} finally {
			requestLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private void handleAct(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		Object parsed;
		try {
			parsed = JSON.fromString(new String(request.getInputStream().readAllBytes(), "UTF-8"));
		} catch (JSON.ParseException ex) {
			throw new IllegalArgumentException("Invalid JSON: " + ex.getMessage());
		}
		if (!(parsed instanceof Map)) {
			throw new IllegalArgumentException("Expected a JSON object body.");
		}
		Map<String, Object> body = (Map<String, Object>) parsed;
		String windowName = (String) body.get(FIELD_WINDOW_NAME);
		String address = (String) body.get(FIELD_ADDRESS);
		String command = (String) body.get(FIELD_COMMAND);
		Object argumentsValue = body.containsKey(FIELD_ARGUMENTS) ? body.get(FIELD_ARGUMENTS) : body.get(FIELD_ARGS);
		Map<String, Object> arguments = (Map<String, Object>) argumentsValue;
		if (address == null || command == null) {
			throw new IllegalArgumentException("Missing 'address' or 'command'.");
		}

		SSEUpdateQueue queue = requireQueue(session, windowName);
		AgentSession agentSession = agentSession(queue);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			HandlerResult result;
			try {
				result = agentSession.act(address, command, arguments);
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Let derived/observable state settle before observing, so the returned tree is quiesced.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);

			String observation = agentSession.observeJson();
			write(response, "{\"" + FIELD_SUCCESS + "\":" + result.isSuccess() + errorField(result)
				+ ",\"" + FIELD_OBSERVATION + "\":" + observation + "}");
		} finally {
			requestLock.unlock();
		}
	}

	/**
	 * The {@code ,"error":"…"} JSON fragment carrying a failed {@link HandlerResult}'s message, or the
	 * empty string when the result succeeded or carries no message.
	 */
	private static String errorField(HandlerResult result) {
		if (result.isSuccess()) {
			return "";
		}
		String text = errorText(result);
		return text == null ? "" : ",\"" + FIELD_ERROR + "\":" + JSON.toString(text);
	}

	/**
	 * The human-readable message of a failed {@link HandlerResult} — its encoded error keys and any
	 * carried exception, resolved to the request locale — or {@code null} if there is none.
	 */
	private static String errorText(HandlerResult result) {
		Resources resources = Resources.getInstance();
		List<String> messages = new ArrayList<>();
		for (ResKey error : result.getEncodedErrors()) {
			messages.add(resources.getString(error));
		}
		I18NRuntimeException exception = result.getException();
		if (exception != null) {
			messages.add(resources.getString(exception.getErrorKey()));
		}
		return messages.isEmpty() ? null : String.join("; ", messages);
	}

	/**
	 * Navigates the window to a route URL (e.g. {@code "access-control/groups"}), the way a
	 * route-based sidebar item does when clicked. This lets an agent reach areas whose content is
	 * loaded by the router rather than by an in-place {@code selectItem} content swap.
	 */
	@SuppressWarnings("unchecked")
	private void handleNavigate(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		Object parsed;
		try {
			parsed = JSON.fromString(new String(request.getInputStream().readAllBytes(), "UTF-8"));
		} catch (JSON.ParseException ex) {
			throw new IllegalArgumentException("Invalid JSON: " + ex.getMessage());
		}
		if (!(parsed instanceof Map)) {
			throw new IllegalArgumentException("Expected a JSON object body.");
		}
		Map<String, Object> body = (Map<String, Object>) parsed;
		String windowName = (String) body.get(FIELD_WINDOW_NAME);
		String url = (String) body.get(FIELD_URL);
		if (url == null) {
			throw new IllegalArgumentException("Missing 'url'.");
		}

		SSEUpdateQueue queue = requireQueue(session, windowName);
		RouteManager routeManager = queue.getRouteManager();
		if (routeManager == null) {
			throw new IllegalArgumentException("Window '" + windowName + "' has no router.");
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			boolean ok = true;
			String message = null;
			try {
				routeManager.navigateToRoute(url);
			} catch (Exception ex) {
				// e.g. a dirty-form veto refuses navigation.
				ok = false;
				message = ex.getMessage();
			}
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);
			String reachedUrl = routeManager.currentUrl();
			if (ok && !stripLeadingSlash(url).equals(reachedUrl)) {
				// navigateToRoute resolves only against already-registered routing participants. A route
				// into an area that is not loaded yet (no participant declares its leading segment) leaves
				// the URL unconsumed and the view unchanged — report that honestly instead of a false OK.
				ok = false;
				message = "Did not reach '" + url + "' (stopped at '" + reachedUrl
					+ "'). The target area may not be loaded; activate it first (e.g. sidebar 'selectItem').";
			}
			String observation = agentSession(queue).observeJson();
			write(response, "{\"" + FIELD_SUCCESS + "\":" + ok
				+ ",\"" + FIELD_URL + "\":" + JSON.toString(reachedUrl)
				+ (message != null ? ",\"" + FIELD_MESSAGE + "\":" + JSON.toString(message) : "")
				+ ",\"" + FIELD_OBSERVATION + "\":" + observation + "}");
		} finally {
			requestLock.unlock();
		}
	}

	/**
	 * {@code GET /agent-api/record/steps} — the current recorder state and captured steps.
	 */
	private void handleRecordSteps(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		SSEUpdateQueue queue = requireQueue(session, request.getParameter(FIELD_WINDOW_NAME));
		writeRecorderState(response, queue.getRecorder());
	}

	/**
	 * {@code POST /agent-api/record/start|stop} — begin a fresh recording or stop the current one,
	 * returning the recorder state and (for stop) the captured steps.
	 */
	private void handleRecordStartStop(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, boolean start) throws IOException {
		Map<String, Object> body = parseObjectBody(request);
		SSEUpdateQueue queue = requireQueue(session, (String) body.get(FIELD_WINDOW_NAME));
		ScriptRecorder recorder = queue.getRecorder();
		if (start) {
			recorder.start();
		} else {
			recorder.stop();
		}
		writeRecorderState(response, recorder);
	}

	private static void writeRecorderState(HttpServletResponse response, ScriptRecorder recorder) throws IOException {
		List<Map<String, Object>> steps = new ArrayList<>();
		for (RecordedStep step : recorder.steps()) {
			steps.add(step.toMap());
		}
		write(response, "{\"" + FIELD_RECORDING + "\":" + recorder.isRecording()
			+ ",\"" + FIELD_STEPS + "\":" + JSON.toString(steps) + "}");
	}

	/**
	 * {@code POST /agent-api/record/assert} with body {@code {"windowName":W,"address":A,"expect":{…}?}}
	 * &rarr; appends an assertion step. With {@code expect} it records exactly those expected state
	 * entries; without it, it captures the node's current state — so a recorder can mark "the state
	 * here is the expected state". On replay the step is verified rather than dispatched.
	 */
	@SuppressWarnings("unchecked")
	private void handleRecordAssert(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		Map<String, Object> body = parseObjectBody(request);
		String windowName = (String) body.get(FIELD_WINDOW_NAME);
		String address = (String) body.get(FIELD_ADDRESS);
		if (address == null) {
			throw new IllegalArgumentException("Missing 'address'.");
		}
		SSEUpdateQueue queue = requireQueue(session, windowName);
		AgentSession agentSession = agentSession(queue);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			Map<String, Object> expected = (Map<String, Object>) body.get(FIELD_EXPECT);
			if (expected == null) {
				expected = AgentTreeProjector.nodeState(agentSession.resolve(address));
			}
			queue.getRecorder().record(RecordedStep.assertion(address, expected));
			writeRecorderState(response, queue.getRecorder());
		} finally {
			requestLock.unlock();
		}
	}

	/**
	 * {@code POST /agent-api/replay} with body {@code {"windowName":W,"steps":[{address,command,arguments},…]}}
	 * &rarr; replays each step through {@link AgentSession#act} in order, settling derived state between
	 * steps so each address resolves against the state its predecessors produced. Returns a top-level
	 * {@code success} (true only when every step succeeded — the replay-as-regression verdict), a
	 * per-step {@code results} list, and the final quiesced {@code observation}.
	 */
	@SuppressWarnings("unchecked")
	private void handleReplay(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		Map<String, Object> body = parseObjectBody(request);
		String windowName = (String) body.get(FIELD_WINDOW_NAME);
		Object stepsValue = body.get(FIELD_STEPS);
		if (!(stepsValue instanceof List)) {
			throw new IllegalArgumentException("Missing 'steps' list.");
		}
		List<Object> steps = (List<Object>) stepsValue;

		SSEUpdateQueue queue = requireQueue(session, windowName);
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		ReentrantLock requestLock = ReactWindowRegistry.forSession(session).getRequestLock();
		requestLock.lock();
		try {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			List<Map<String, Object>> results = new ArrayList<>();
			try {
				for (Object stepObj : steps) {
					results.add(replayStep(session, queue, windowName, (Map<String, Object>) stepObj));
				}
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
			boolean allOk = true;
			for (Map<String, Object> stepResult : results) {
				if (!Boolean.TRUE.equals(stepResult.get(FIELD_SUCCESS))) {
					allOk = false;
					break;
				}
			}
			String observation = agentSession(queue).observeJson();
			write(response, "{\"" + FIELD_SUCCESS + "\":" + allOk
				+ ",\"" + FIELD_RESULTS + "\":" + JSON.toString(results)
				+ ",\"" + FIELD_OBSERVATION + "\":" + observation + "}");
		} finally {
			requestLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> replayStep(HttpSession session, SSEUpdateQueue queue, String windowName,
			Map<String, Object> step) {
		String address = (String) step.get(FIELD_ADDRESS);
		String command = (String) step.get(FIELD_COMMAND);
		Map<String, Object> arguments = (Map<String, Object>) step.get(FIELD_ARGUMENTS);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put(FIELD_ADDRESS, address);
		result.put(FIELD_COMMAND, command);
		if (address == null || command == null) {
			result.put(FIELD_SUCCESS, false);
			result.put(FIELD_ERROR, "Step has no address or command.");
			return result;
		}
		if (RecordedStep.ASSERT_COMMAND.equals(command)) {
			return verifyAssertion(queue, address, result, arguments);
		}
		try {
			HandlerResult handlerResult = agentSession(queue).act(address, command, arguments);
			result.put(FIELD_SUCCESS, handlerResult.isSuccess());
			if (!handlerResult.isSuccess()) {
				result.put(FIELD_ERROR, errorText(handlerResult));
			}
			// Settle derived state so the next step's address resolves against the produced state.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);
		} catch (RuntimeException ex) {
			result.put(FIELD_SUCCESS, false);
			result.put(FIELD_ERROR, ex.getMessage());
		}
		return result;
	}

	/**
	 * Verifies an assertion step: the node at {@code address} must have, for each expected key, a state
	 * value equal to the recorded one (subset match). Comparison is by canonical JSON so numeric and
	 * representation differences do not cause false mismatches.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> verifyAssertion(SSEUpdateQueue queue, String address, Map<String, Object> result,
			Map<String, Object> arguments) {
		Map<String, Object> expected = arguments == null
			? Map.of() : (Map<String, Object>) arguments.getOrDefault(RecordedStep.ASSERT_STATE_ARG, Map.of());
		try {
			Map<String, Object> actual = AgentTreeProjector.nodeState(agentSession(queue).resolve(address));
			List<String> mismatchKeys = RecordedStep.mismatchingKeys(expected, actual);
			result.put(FIELD_SUCCESS, mismatchKeys.isEmpty());
			if (!mismatchKeys.isEmpty()) {
				List<Map<String, Object>> mismatches = new ArrayList<>();
				for (String key : mismatchKeys) {
					Map<String, Object> mismatch = new LinkedHashMap<>();
					mismatch.put(FIELD_KEY, key);
					mismatch.put(FIELD_EXPECTED, expected.get(key));
					mismatch.put(FIELD_ACTUAL, actual.get(key));
					mismatches.add(mismatch);
				}
				result.put(FIELD_MISMATCHES, mismatches);
			}
		} catch (RuntimeException ex) {
			result.put(FIELD_SUCCESS, false);
			result.put(FIELD_ERROR, ex.getMessage());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseObjectBody(HttpServletRequest request) throws IOException {
		Object parsed;
		try {
			parsed = JSON.fromString(new String(request.getInputStream().readAllBytes(), "UTF-8"));
		} catch (JSON.ParseException ex) {
			throw new IllegalArgumentException("Invalid JSON: " + ex.getMessage());
		}
		if (!(parsed instanceof Map)) {
			throw new IllegalArgumentException("Expected a JSON object body.");
		}
		return (Map<String, Object>) parsed;
	}

	/**
	 * Builds a session rooted at the window's displayed root control.
	 */
	private static AgentSession agentSession(SSEUpdateQueue queue) {
		return AgentSession.forRoot(queue.getRootControl());
	}

	private SSEUpdateQueue requireQueue(HttpSession session, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			throw new IllegalArgumentException("Missing 'windowName'.");
		}
		SSEUpdateQueue queue = ReactWindowRegistry.forSession(session).getQueue(windowName);
		if (queue == null) {
			throw new IllegalArgumentException("Unknown window: " + windowName);
		}
		return queue;
	}

	/**
	 * Installs the subsession context for the given window so command execution and label resolution
	 * have an ambient {@link com.top_logic.util.TLContext}. Mirrors the React command servlet.
	 *
	 * @return The {@link SubsessionHandler} if the window uses the traditional layout engine, else
	 *         {@code null}.
	 */
	private SubsessionHandler installSubSession(DisplayContext displayContext, String windowName) {
		if (StringServices.isEmpty(windowName)) {
			return null;
		}
		TLSessionContext sessionContext = TLContextManager.getSession();
		if (sessionContext == null) {
			return null;
		}
		TLSubSessionContext subSession = sessionContext.getSubSession(windowName);
		if (subSession != null) {
			displayContext.installSubSessionContext(subSession);
		} else {
			Logger.warn("No SubSession found for window '" + windowName
				+ "'. The view page may not have been loaded yet.", AgentServlet.class);
		}
		ContentHandlersRegistry handlersRegistry = sessionContext.getHandlersRegistry();
		return handlersRegistry.getContentHandler(windowName);
	}

	private static String stripLeadingSlash(String url) {
		return url.startsWith("/") ? url.substring(1) : url;
	}

	private static void write(HttpServletResponse response, String json) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.flush();
	}

	private static void sendError(HttpServletResponse response, int status, String message)
			throws IOException {
		response.setStatus(status);
		write(response, "{\"" + FIELD_ERROR + "\":" + JSON.toString(message == null ? "" : message) + "}");
	}
}
