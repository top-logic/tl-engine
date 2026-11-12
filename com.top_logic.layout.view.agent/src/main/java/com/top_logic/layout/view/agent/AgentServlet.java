/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommandAnswer;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.TopLogicServlet;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ReactWindowReplay;
import com.top_logic.layout.react.scripting.AssertCommand;

import de.haumacher.msgbuf.data.DataObject;

/**
 * HTTP endpoint exposing the {@link ScriptingSession headless interface} of a live application
 * session.
 *
 * <p>
 * This is the integration point a non-browser consumer (a script-recorder/replay tool or an AI agent,
 * typically through a thin MCP wrapper) uses to drive a real session:
 * </p>
 * <ul>
 * <li>{@code GET  /agent-api/windows} → {@code {"windows":[...]}} — the windows that can be
 * observed.</li>
 * <li>{@code GET  /agent-api/observe?windowName=W} → the addressable state tree of window
 * {@code W} (an {@link ScriptingNodeView} as JSON). The parameter {@link #FIELD_MODE} selects the
 * view: {@link #MODE_TREE} (the default) or {@link #MODE_ACTIONS} for the compact affordance-first
 * view, a flat list of just the actionable nodes. Another value is answered with
 * {@link HttpServletResponse#SC_BAD_REQUEST}. The nodes name the objects they display by ids,
 * which the command arguments take. The argument schema of an action is a reference into the
 * {@link ScriptingNodeView#SCHEMAS} map of the observation, which lists each schema once.</li>
 * <li>{@code POST /agent-api/act} with body {@code {"windowName":W,"address":A,"command":C,"arguments":{…}}}
 * → resolves {@code A}, invokes {@code C}, and returns {@code {"success":b,"observation":{…}}}
 * with the resulting state tree. The command arguments may be given as either {@link #FIELD_ARGUMENTS} or {@link #FIELD_ARGS}.</li>
 * <li>{@code POST /agent-api/navigate} with body {@code {"windowName":W,"url":"access-control/groups"}}
 * → navigates the window's router to a route URL (for areas loaded by routing rather than an
 * in-place {@code selectItem}), returning {@code {"success":b,"url":…,"observation":{…}}}. {@code
 * success} is {@code true} only if the router actually reached the requested URL; a route into an
 * area whose participants are not registered yet is reported as {@code false} with a {@code message},
 * not a silent no-op.</li>
 * <li>{@code POST /agent-api/record/start} / {@code /record/stop} with body {@code {"windowName":W}}
 * → begin a fresh recording of the user's interactions, or stop it; both return
 * {@code {"recording":b,"steps":[…]}}. While recording, the browser command path captures each
 * dispatched command as an {@code {address,command,arguments}} step.</li>
 * <li>{@code GET /agent-api/record/steps?windowName=W} → the current recorder state and steps.</li>
 * <li>{@code POST /agent-api/record/assert} with body {@code {"windowName":W,"address":A,"expect":{…}?}}
 * → append an assertion step — the expected state given as {@code expect}, or the node's current
 * state captured if omitted. On replay it is verified, not dispatched.</li>
 * <li>{@code POST /agent-api/replay} with body {@code {"windowName":W,"steps":[…]}} → replays a
 * recorded (or hand-written) script through the same {@code act} path (assertion steps are verified
 * against live state), stopping at the first failed step, returning a per-step {@code results} list
 * and the final {@code observation}.</li>
 * </ul>
 *
 * <p>
 * Acting dispatches through {@link com.top_logic.layout.react.control.ReactControl#executeCommand} —
 * the same path the browser command endpoint uses — as one
 * {@link ReactWindowRegistry#beginInteraction() interaction} of the window, in its subsession
 * context. After the command runs,
 * {@link ReactWindowRegistry#synthesizeModelEvents(String) model events are synthesized} so that
 * all derived state has settled before the response is built. The reply therefore carries a
 * <em>quiesced</em> observation: a headless caller never has to poll or await asynchronous SSE
 * delivery — it reads the fresh server state directly.
 * </p>
 *
 * <p>
 * Every answer is a JSON document, streamed into the response after it is completely built: compact,
 * or indented with the query parameter {@link #PARAM_PRETTY}{@code =true}. A
 * request that cannot be answered gets an error status with a JSON object whose
 * {@link JsonAnswers#FIELD_ERROR} member holds the message: {@link HttpServletResponse#SC_UNAUTHORIZED} without
 * an admitted session, {@link HttpServletResponse#SC_FORBIDDEN} for an observing credential that
 * tries to act, {@link HttpServletResponse#SC_NOT_FOUND} for an unknown path or window (of a
 * {@code GET}), {@link HttpServletResponse#SC_BAD_REQUEST} for a malformed request and
 * {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR} for a failure while handling it, see
 * {@link JsonAnswers#answer(HttpServletResponse, String, int, boolean, JsonAnswers.Producer)}.
 * </p>
 *
 * <p>
 * The endpoint runs inside the authenticated {@link TopLogicServlet} context, so it acts as the
 * logged-in user with exactly the permissions the interactive UI would have.
 * </p>
 */
public class AgentServlet extends TopLogicServlet {

	// The request paths below the servlet.
	private static final String PATH_WINDOWS = "/windows";

	static final String PATH_OBSERVE = "/observe";

	private static final String PATH_RECORD_STEPS = "/record/steps";

	private static final String PATH_ACT = "/act";

	private static final String PATH_NAVIGATE = "/navigate";

	private static final String PATH_RECORD_START = "/record/start";

	private static final String PATH_RECORD_STOP = "/record/stop";

	private static final String PATH_RECORD_ASSERT = "/record/assert";

	private static final String PATH_REPLAY = "/replay";

	// Request/response JSON field names and the request parameter names.
	private static final String FIELD_WINDOWS = "windows";

	private static final String FIELD_WINDOW_NAME = "windowName";

	private static final String FIELD_ADDRESS = "address";

	private static final String FIELD_COMMAND = "command";

	private static final String FIELD_ARGUMENTS = "arguments";

	private static final String FIELD_ARGS = "args";

	/**
	 * Parameter of {@link #PATH_OBSERVE} selecting the view, {@link #MODE_TREE} or
	 * {@link #MODE_ACTIONS}.
	 */
	static final String FIELD_MODE = "mode";

	/**
	 * Query parameter of every request: with the value {@code true} the answer is indented for a
	 * human reader, otherwise it is compact JSON.
	 */
	static final String PARAM_PRETTY = "pretty";

	/**
	 * Value of the {@link #FIELD_MODE} parameter of {@link #PATH_OBSERVE} requesting the whole
	 * state tree of the window. This is the default.
	 */
	static final String MODE_TREE = "tree";

	/**
	 * Value of the {@link #FIELD_MODE} parameter of {@link #PATH_OBSERVE} requesting the flat list
	 * of the actionable nodes.
	 */
	static final String MODE_ACTIONS = "actions";

	private static final String FIELD_SUCCESS = "success";

	private static final String FIELD_OBSERVATION = "observation";

	/**
	 * Answer field of {@code /act} holding the reply of the command's handler, see
	 * {@link com.top_logic.layout.react.control.ReactCommandHandler}.
	 */
	private static final String FIELD_REPLY = "reply";

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

	private static final String FIELD_DESCRIPTION = "description";

	/** Request attribute remembering the resolved credential, see {@link #credential}. */
	private static final String CREDENTIAL_ATTRIBUTE = AgentServlet.class.getName() + ".credential";

	/** Marker remembering that the request presents no admitted credential. */
	private static final Object NO_CREDENTIAL = new Object();

	/** Message of the {@link HttpServletResponse#SC_UNAUTHORIZED} answer to a request admitted to no session. */
	private static final String ERROR_NO_SESSION = "No session.";

	/**
	 * The check whether the caller accepts cookies is skipped.
	 *
	 * <p>
	 * The check redirects a top-level browser navigation to an HTML page that probes for cookies.
	 * An agent is no browser: it sends no session cookie and cannot pass the check, so it would be
	 * redirected to the login page on every request. A browser calling the endpoint already carries
	 * the cookie of the session its page was loaded with. A request without a session therefore
	 * ends in {@link #handleNoSession(HttpServletRequest, HttpServletResponse)}.
	 * </p>
	 */
	@Override
	protected boolean isCookieCheckRequired() {
		return false;
	}

	/**
	 * Answers a request admitted to no session - one without a credential, with an unrecognized
	 * credential, or with the credential of an ended session - with
	 * {@link HttpServletResponse#SC_UNAUTHORIZED} and a JSON error object.
	 */
	@Override
	protected void handleNoSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		JsonAnswers.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ERROR_NO_SESSION);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * An agent has no session cookie; it presents a credential naming the session it was admitted
	 * to. That session is the one the request runs in, so that the agent acts in the very session
	 * the user issued the credential from.
	 * </p>
	 */
	@Override
	protected TLSessionContext getSession(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		HttpSession credentialSession = credentialSession(request);
		if (credentialSession != null) {
			return SessionService.getInstance().getSession(credentialSession);
		}
		return super.getSession(request, response);
	}

	/**
	 * The session this request acts in: the browser's own session, or the session an agent's
	 * credential admits it to.
	 */
	private HttpSession effectiveSession(HttpServletRequest request) {
		HttpSession cookieSession = request.getSession(false);
		return cookieSession != null ? cookieSession : credentialSession(request);
	}

	/**
	 * The session an agent's credential admits this request to, or <code>null</code> if the request
	 * presents no credential, an unrecognized one, or one whose session has ended.
	 */
	private HttpSession credentialSession(HttpServletRequest request) {
		AgentCredential credential = credential(request);
		return credential == null ? null : AgentAccess.getInstance().session(credential.sessionKey());
	}

	/**
	 * The caller admitted by the credential this request presents, resolved once.
	 *
	 * <p>
	 * A request asks for its credential more than once — the session it runs in is resolved before
	 * the interaction is entered, the permissions it carries while handling the request. Resolving
	 * it means reading and stamping the credential in the persistency layer, which is neither free
	 * nor possible in every phase of the request, so the outcome is remembered on the request.
	 * </p>
	 */
	private static AgentCredential credential(HttpServletRequest request) {
		Object cached = request.getAttribute(CREDENTIAL_ATTRIBUTE);
		if (cached != null) {
			return cached == NO_CREDENTIAL ? null : (AgentCredential) cached;
		}
		AgentCredential result =
			AgentAccess.Module.INSTANCE.isActive() ? AgentAccess.getInstance().authenticate(request) : null;
		request.setAttribute(CREDENTIAL_ATTRIBUTE, result == null ? NO_CREDENTIAL : result);
		return result;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = effectiveSession(request);
		if (session == null) {
			JsonAnswers.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ERROR_NO_SESSION);
			return;
		}
		String pathInfo = request.getPathInfo();
		JsonAnswers.answer(response, pathInfo, HttpServletResponse.SC_NOT_FOUND, isPretty(request), () -> {
			if (PATH_WINDOWS.equals(pathInfo)) {
				return handleWindows(session);
			} else if (PATH_OBSERVE.equals(pathInfo)) {
				return handleObserve(request, session);
			} else if (PATH_RECORD_STEPS.equals(pathInfo)) {
				return handleRecordSteps(request, session);
			} else {
				throw unknownPath(pathInfo);
			}
		});
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = effectiveSession(request);
		if (session == null) {
			JsonAnswers.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, ERROR_NO_SESSION);
			return;
		}
		AgentCredential credential = credential(request);
		if (credential != null && !credential.mayAct()) {
			// An observing agent reads the application; changing it is a permission of its own.
			JsonAnswers.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Credential may observe only.");
			return;
		}
		String pathInfo = request.getPathInfo();
		JsonAnswers.answer(response, pathInfo, HttpServletResponse.SC_BAD_REQUEST, isPretty(request), () -> {
			if (PATH_ACT.equals(pathInfo)) {
				return handleAct(request, session);
			} else if (PATH_NAVIGATE.equals(pathInfo)) {
				return handleNavigate(request, session);
			} else if (PATH_RECORD_START.equals(pathInfo)) {
				return handleRecordStartStop(request, session, true);
			} else if (PATH_RECORD_STOP.equals(pathInfo)) {
				return handleRecordStartStop(request, session, false);
			} else if (PATH_RECORD_ASSERT.equals(pathInfo)) {
				return handleRecordAssert(request, session);
			} else if (PATH_REPLAY.equals(pathInfo)) {
				return handleReplay(request, session);
			} else {
				throw unknownPath(pathInfo);
			}
		});
	}

	/**
	 * Whether the request asks for an indented answer by the query parameter {@link #PARAM_PRETTY}.
	 */
	private static boolean isPretty(HttpServletRequest request) {
		return Boolean.parseBoolean(request.getParameter(PARAM_PRETTY));
	}

	private static JsonAnswers.RequestRejected unknownPath(String pathInfo) {
		return new JsonAnswers.RequestRejected(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
	}

	private Object handleWindows(HttpSession session) {
		List<String> windows = new ArrayList<>(ReactWindowRegistry.forSession(session).windowNames());
		return Map.of(FIELD_WINDOWS, windows);
	}

	private Object handleObserve(HttpServletRequest request, HttpSession session) {
		String windowName = request.getParameter(FIELD_WINDOW_NAME);
		boolean actionsMode = isActionsMode(request.getParameter(FIELD_MODE));
		SSEUpdateQueue queue = requireQueue(session, windowName);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			ScriptingSession scriptingSession = scriptingSession(queue);
			return actionsMode ? scriptingSession.observeActions() : scriptingSession.observe().toMap();
		}
	}

	/**
	 * Whether the value of the {@link #FIELD_MODE} parameter of {@link #PATH_OBSERVE} requests the
	 * flat {@link #MODE_ACTIONS} view rather than the {@link #MODE_TREE}.
	 *
	 * @throws JsonAnswers.RequestRejected
	 *         If the value names no mode.
	 */
	static boolean isActionsMode(String mode) {
		if (mode == null || MODE_TREE.equals(mode)) {
			return false;
		}
		if (MODE_ACTIONS.equals(mode)) {
			return true;
		}
		throw new JsonAnswers.RequestRejected(HttpServletResponse.SC_BAD_REQUEST, "Unknown " + FIELD_MODE + " '" + mode
			+ "', expected '" + MODE_TREE + "' (the default) or '" + MODE_ACTIONS + "'.");
	}

	@SuppressWarnings("unchecked")
	private Object handleAct(HttpServletRequest request, HttpSession session) throws IOException {
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
		ScriptingSession scriptingSession = scriptingSession(queue);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		SubsessionHandler rootHandler = installSubSession(displayContext, windowName);

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			ReactControl target = scriptingSession.target(address);
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			DataObject reply = null;
			RuntimeException failure = null;
			try {
				reply = ScriptingSession.execute(target, address, command, arguments);
			} catch (RuntimeException ex) {
				CommandErrors.log(ex, "Agent command '" + command + "' on '" + address + "'", AgentServlet.class);
				failure = ex;
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}

			// Let derived/observable state settle before observing, so the returned tree is quiesced.
			ReactWindowRegistry.forSession(session).synthesizeModelEvents(windowName);

			Map<String, Object> answer = new LinkedHashMap<>();
			answer.put(FIELD_SUCCESS, failure == null);
			if (failure != null) {
				answer.put(FIELD_ERROR, CommandErrors.text(failure));
			}
			if (reply != null) {
				answer.put(FIELD_REPLY, replyValue(reply));
			}
			answer.put(FIELD_OBSERVATION, scriptingSession.observe().toMap());
			return answer;
		}
	}

	/**
	 * The reply of a command's handler as a plain {@link Map} of its fields.
	 */
	private static Object replyValue(DataObject reply) {
		try {
			return JSON.fromString(ReactCommandAnswer.toJson(reply));
		} catch (JSON.ParseException ex) {
			throw new IllegalStateException("Reply of a command is no valid JSON.", ex);
		}
	}

	/**
	 * Navigates the window to a route URL (e.g. {@code "access-control/groups"}), the way a
	 * route-based sidebar item does when clicked. This lets an agent reach areas whose content is
	 * loaded by the router rather than by an in-place {@code selectItem} content swap.
	 */
	@SuppressWarnings("unchecked")
	private Object handleNavigate(HttpServletRequest request, HttpSession session) throws IOException {
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

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			boolean ok = true;
			String message = null;
			try {
				routeManager.navigateToRoute(url);

				// The display has taken the URL as far as it can: a segment it cannot reproduce is
				// dropped and a display showing what the URL does not name returns, which is the state
				// the observation below reports.
				routeManager.finishAdoption();
			} catch (Exception ex) {
				// e.g. a dirty-form veto refuses navigation.
				ok = false;
				message = ex.getMessage();

				// The URL is not reached, so its adoption ends here, leaving the display as the veto
				// keeps it.
				routeManager.cancelAdoption();
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
			Map<String, Object> answer = new LinkedHashMap<>();
			answer.put(FIELD_SUCCESS, ok);
			answer.put(FIELD_URL, reachedUrl);
			if (message != null) {
				answer.put(FIELD_MESSAGE, message);
			}
			answer.put(FIELD_OBSERVATION, scriptingSession(queue).observe().toMap());
			return answer;
		}
	}

	/**
	 * {@code GET /agent-api/record/steps} — the current recorder state and captured steps.
	 */
	private Object handleRecordSteps(HttpServletRequest request, HttpSession session) {
		SSEUpdateQueue queue = requireQueue(session, request.getParameter(FIELD_WINDOW_NAME));
		return recorderState(queue.getRecorder());
	}

	/**
	 * {@code POST /agent-api/record/start|stop} — begin a fresh recording or stop the current one,
	 * returning the recorder state and (for stop) the captured steps.
	 */
	private Object handleRecordStartStop(HttpServletRequest request, HttpSession session, boolean start)
			throws IOException {
		Map<String, Object> body = parseObjectBody(request);
		SSEUpdateQueue queue = requireQueue(session, (String) body.get(FIELD_WINDOW_NAME));
		ScriptRecorder recorder = queue.getRecorder();
		if (start) {
			recorder.start();
		} else {
			recorder.stop();
		}
		return recorderState(recorder);
	}

	private static Object recorderState(ScriptRecorder recorder) {
		List<Map<String, Object>> steps = new ArrayList<>();
		for (ReactCommand step : recorder.steps()) {
			steps.add(stepWireForm(step));
		}
		Map<String, Object> answer = new LinkedHashMap<>();
		answer.put(FIELD_RECORDING, recorder.isRecording());
		answer.put(FIELD_STEPS, steps);
		return answer;
	}

	/**
	 * A recorded step in the wire form the replay endpoint accepts back: {@code address},
	 * {@code command}, its {@code arguments} and the generated human-readable {@code description} —
	 * all derived from the typed {@link ReactCommand} item.
	 */
	private static Map<String, Object> stepWireForm(ReactCommand step) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(FIELD_ADDRESS, step.getAddress());
		result.put(FIELD_COMMAND, step.getName());
		if (step instanceof AssertCommand assertion) {
			// The expected state is carried as a nested object, matching what record/assert accepted.
			result.put(FIELD_ARGUMENTS, Map.of(AssertCommand.STATE, assertion.stateEntries()));
		} else {
			result.put(FIELD_ARGUMENTS, ReactCommands.arguments(step));
		}
		try {
			result.put(FIELD_DESCRIPTION, ReactCommands.describe(step));
		} catch (RuntimeException ex) {
			// The description is informational; a rendering problem must not break the listing.
			Logger.error("Failed to describe recorded step at '" + step.getAddress() + "'.", ex,
				AgentServlet.class);
		}
		return result;
	}

	/**
	 * {@code POST /agent-api/record/assert} with body {@code {"windowName":W,"address":A,"expect":{…}?}}
	 * → appends an assertion step. With {@code expect} it records exactly those expected state
	 * entries; without it, it captures the node's current state — so a recorder can mark "the state
	 * here is the expected state". On replay the step is verified rather than dispatched.
	 */
	@SuppressWarnings("unchecked")
	private Object handleRecordAssert(HttpServletRequest request, HttpSession session) throws IOException {
		Map<String, Object> body = parseObjectBody(request);
		String windowName = (String) body.get(FIELD_WINDOW_NAME);
		String address = (String) body.get(FIELD_ADDRESS);
		if (address == null) {
			throw new IllegalArgumentException("Missing 'address'.");
		}
		SSEUpdateQueue queue = requireQueue(session, windowName);
		ScriptingSession scriptingSession = scriptingSession(queue);

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		installSubSession(displayContext, windowName);

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			ReactControl target = scriptingSession.resolve(address);
			Map<String, Object> expected = (Map<String, Object>) body.get(FIELD_EXPECT);
			if (expected == null) {
				expected = ScriptingTreeProjector.nodeState(target);
			}
			// The replay-stable form of the address, naming items by the keys of their objects.
			String recorded = target == null ? null : scriptingSession.addressOf(target);
			queue.getRecorder().recordAssertion(recorded != null ? recorded : address, expected);
			return recorderState(queue.getRecorder());
		}
	}

	/**
	 * {@code POST /agent-api/replay} with body {@code {"windowName":W,"steps":[{address,command,arguments},…]}}
	 * → replays each step through {@link ScriptingSession#act} in order, settling derived state between
	 * steps so each address resolves against the state its predecessors produced. Stops at the first
	 * failed step — later steps assume state the failed step should have established. Returns a
	 * top-level {@code success} (true only when every step succeeded — the replay-as-regression
	 * verdict), a per-step {@code results} list up to and including the failed step, and the final
	 * quiesced {@code observation}.
	 */
	@SuppressWarnings("unchecked")
	private Object handleReplay(HttpServletRequest request, HttpSession session) throws IOException {
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

		try (Interaction interaction = ReactWindowRegistry.forSession(session).beginInteraction()) {
			boolean updateBefore = rootHandler != null ? rootHandler.enableUpdate(true) : false;
			List<Map<String, Object>> results = new ArrayList<>();
			boolean allOk = true;
			try {
				for (Object stepObj : steps) {
					Map<String, Object> stepResult =
						replayStep(session, queue, windowName, (Map<String, Object>) stepObj);
					results.add(stepResult);
					if (!Boolean.TRUE.equals(stepResult.get(FIELD_SUCCESS))) {
						// Stop at the first failed step: the remaining steps assume state this
						// step should have established, so executing them would act on the wrong
						// targets or fail confusingly.
						allOk = false;
						break;
					}
				}
			} finally {
				if (rootHandler != null) {
					rootHandler.enableUpdate(updateBefore);
				}
			}
			Map<String, Object> answer = new LinkedHashMap<>();
			answer.put(FIELD_SUCCESS, allOk);
			answer.put(FIELD_RESULTS, results);
			answer.put(FIELD_OBSERVATION, scriptingSession(queue).observe().toMap());
			return answer;
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
		if (AssertCommand.COMMAND_NAME.equals(command)) {
			return verifyAssertion(queue, address, result, arguments);
		}
		try {
			ReactControl target = scriptingSession(queue).target(address);
			try {
				ScriptingSession.execute(target, address, command, arguments);
				result.put(FIELD_SUCCESS, true);
			} catch (RuntimeException ex) {
				CommandErrors.log(ex, "Replayed command '" + command + "' on '" + address + "'", AgentServlet.class);
				result.put(FIELD_SUCCESS, false);
				result.put(FIELD_ERROR, CommandErrors.text(ex));
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
	 * representation differences do not cause false mismatches. A mismatch is reported by the
	 * {@link AssertCommand#mismatchingKeys(Map, Map) path} of the differing value, together with the
	 * two values found there.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> verifyAssertion(SSEUpdateQueue queue, String address, Map<String, Object> result,
			Map<String, Object> arguments) {
		Map<String, Object> expected = arguments == null
			? Map.of() : (Map<String, Object>) arguments.getOrDefault(AssertCommand.STATE, Map.of());
		try {
			Map<String, Object> actual = ScriptingTreeProjector.nodeState(scriptingSession(queue).resolve(address));
			List<String> mismatchKeys = AssertCommand.mismatchingKeys(expected, actual);
			result.put(FIELD_SUCCESS, mismatchKeys.isEmpty());
			if (!mismatchKeys.isEmpty()) {
				List<Map<String, Object>> mismatches = new ArrayList<>();
				for (String key : mismatchKeys) {
					Map<String, Object> mismatch = new LinkedHashMap<>();
					mismatch.put(FIELD_KEY, key);
					mismatch.put(FIELD_EXPECTED, AssertCommand.valueAt(expected, key));
					mismatch.put(FIELD_ACTUAL, AssertCommand.valueAt(actual, key));
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
	private static ScriptingSession scriptingSession(SSEUpdateQueue queue) {
		return ScriptingSession.forRoot(queue.getRootControl());
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
		return ReactWindowReplay.installSubSession(displayContext, windowName);
	}

	private static String stripLeadingSlash(String url) {
		return url.startsWith("/") ? url.substring(1) : url;
	}
}
