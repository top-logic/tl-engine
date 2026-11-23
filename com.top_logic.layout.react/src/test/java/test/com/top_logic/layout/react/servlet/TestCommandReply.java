/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.control.ReactCommandAnswer;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.protocol.RouteRefusal;
import com.top_logic.layout.react.protocol.StateEvent;
import com.top_logic.layout.react.servlet.ReactServlet;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowModelEventService;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

import de.haumacher.msgbuf.data.DataObject;

/**
 * Tests that the reply a {@link ReactCommandHandler} returns reaches the caller in the answer
 * {@link ReactServlet} writes for the command, how a failure of the command is answered, see
 * {@link ReactCommandAnswer}, the answer to a command whose control is not displayed, with and
 * without {@link ReactServlet#FIELD_IGNORE_STALE_UI}, the answer to a command without a window
 * name or with an empty window name, and the answer to a report of a closed window without its
 * window.
 */
public class TestCommandReply extends BasicTestCase {

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String REACT_SERVLET = "react";

	private static final String WINDOW = "vReply";

	private static final String UNKNOWN_WINDOW = "vUnknown";

	private static final String CONTROL_ID = "replier";

	private static final String GONE_CONTROL_ID = "gone";

	private static final String COMMAND = "create";

	private static final String ARG_NAME = "name";

	private ServletRunner _runner;

	private ServletUnitClient _client;

	private HttpSession _session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_runner = new ServletRunner();
		_runner.registerServlet(LOGIN_SERVLET, TLLayoutServlet.class.getName());
		_runner.registerServlet(REACT_SERVLET + "/*", ReactServlet.class.getName());
		_client = _runner.newClient();
		_session = login();
	}

	@Override
	protected void tearDown() throws Exception {
		TLSubSessionContext subSession = sessionContext().getSubSession(WINDOW);
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> registry.windowClosed(WINDOW));
		}
		ThreadContext.inSystemContext(TestCommandReply.class,
			() -> SessionService.getInstance().terminateSession(_session.getId()));
		_runner.shutDown();
		super.tearDown();
	}

	/**
	 * The fields of the reply arrive in the answer next to {@code success}.
	 */
	public void testReplyFieldsInAnswer() throws Exception {
		displayWindow(arguments -> RouteRefusal.create()
			.setRefused(true)
			.setCurrentUrl("/view/" + arguments.get(ARG_NAME)));

		InvocationContext invocation = postCommand(Map.of(ARG_NAME, "x"));

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":true,\"refused\":true,\"currentUrl\":\"/view/x\"}",
			invocation.getServletResponse().getText());
	}

	/**
	 * A command whose handler replies nothing is answered with {@code success} alone.
	 */
	public void testNoReply() throws Exception {
		displayWindow(arguments -> null);

		InvocationContext invocation = postCommand(Map.of());

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":true}", invocation.getServletResponse().getText());
	}

	/**
	 * A command for a control that is not displayed in a window that still has its controls is
	 * answered with {@link ReactServlet#ERROR_CODE_CONTROL_GONE}.
	 */
	public void testCommandToGoneControl() throws Exception {
		displayWindow(arguments -> null);
		// Lets the client read the body of an error answer.
		_client.setExceptionsThrownOnErrorStatus(false);

		InvocationContext invocation = postCommand(GONE_CONTROL_ID, Map.of());

		assertEquals(HttpServletResponse.SC_NOT_FOUND, invocation.getResponse().getStatus());
		Map<?, ?> answer = (Map<?, ?>) JSON.fromString(invocation.getServletResponse().getText());
		assertEquals(Boolean.FALSE, answer.get(ReactCommandAnswer.FIELD_SUCCESS));
		assertEquals(ReactServlet.ERROR_CODE_CONTROL_GONE, answer.get(ReactCommandAnswer.FIELD_ERROR_CODE));
	}

	/**
	 * A command declaring that it only tidies up is answered with success when its control is not
	 * displayed in a window that still has its controls.
	 */
	public void testTidyUpToGoneControl() throws Exception {
		displayWindow(arguments -> null);

		InvocationContext invocation = postCommand(GONE_CONTROL_ID, COMMAND, true);

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":true}", invocation.getServletResponse().getText());
	}

	/**
	 * A command declaring that it only tidies up is answered with success in a window that displays
	 * no page, and the window is not reported as stale.
	 */
	public void testTidyUpToWindowWithoutPage() throws Exception {
		createWindow();
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.INFO), true);
		InvocationContext invocation;
		try {
			invocation = postCommand(GONE_CONTROL_ID, COMMAND, true);
		} finally {
			log.deactivate();
		}

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":true}", invocation.getServletResponse().getText());
		assertEquals(List.of(), staleReports(log));
	}

	/**
	 * A command for a control of a window that displays no page is answered with
	 * {@link ReactServlet#ERROR_CODE_STALE_UI}, and the window is reported as stale.
	 */
	public void testCommandToWindowWithoutPage() throws Exception {
		createWindow();
		_client.setExceptionsThrownOnErrorStatus(false);
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.INFO), true);
		InvocationContext invocation;
		try {
			invocation = postCommand(GONE_CONTROL_ID, COMMAND, false);
		} finally {
			log.deactivate();
		}

		assertEquals(HttpServletResponse.SC_NOT_FOUND, invocation.getResponse().getStatus());
		assertErrorCode(ReactServlet.ERROR_CODE_STALE_UI, invocation);
		assertEquals(1, staleReports(log).size());
	}

	/**
	 * A field value flagged as tidy-up by the client is answered with success when its field is not
	 * displayed any more, without the flag it is answered with
	 * {@link ReactServlet#ERROR_CODE_CONTROL_GONE}: the server decides by the flag, not by the
	 * command name.
	 */
	public void testValueChangedToGoneControl() throws Exception {
		displayWindow(arguments -> null);
		_client.setExceptionsThrownOnErrorStatus(false);

		InvocationContext flagged = postCommand(GONE_CONTROL_ID, ReactFormFieldControl.CMD_VALUE_CHANGED, true);
		assertEquals(HttpServletResponse.SC_OK, flagged.getResponse().getStatus());
		assertEquals("{\"success\":true}", flagged.getServletResponse().getText());

		InvocationContext plain = postCommand(GONE_CONTROL_ID, ReactFormFieldControl.CMD_VALUE_CHANGED, false);
		assertEquals(HttpServletResponse.SC_NOT_FOUND, plain.getResponse().getStatus());
		assertErrorCode(ReactServlet.ERROR_CODE_CONTROL_GONE, plain);
	}

	/**
	 * A command declaring that it only tidies up still learns that the session does not know its
	 * window: the client loads such a page again for a field value.
	 */
	public void testTidyUpToUnknownWindow() throws Exception {
		displayWindow(arguments -> null);
		_client.setExceptionsThrownOnErrorStatus(false);

		InvocationContext invocation =
			postCommand(UNKNOWN_WINDOW, GONE_CONTROL_ID, ReactFormFieldControl.CMD_VALUE_CHANGED, true);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, invocation.getResponse().getStatus());
		assertErrorCode(ReactServlet.ERROR_CODE_STALE_UI, invocation);
	}

	/**
	 * A command without a window name is refused as a bad request, as the event stream and the poll
	 * refuse it. The answer has no {@link ReactServlet#ERROR_CODE_STALE_UI}, because the client did
	 * not send this request and must not load its page again. No error is logged.
	 */
	public void testCommandWithoutWindowName() throws Exception {
		displayWindow(arguments -> null);
		_client.setExceptionsThrownOnErrorStatus(false);
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.ERROR, Level.FATAL), true);
		InvocationContext invocation;
		try {
			invocation = postCommand(null, CONTROL_ID, COMMAND, false);
		} finally {
			log.deactivate();
		}

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, invocation.getResponse().getStatus());
		Map<?, ?> answer = (Map<?, ?>) JSON.fromString(invocation.getServletResponse().getText());
		assertEquals(Boolean.FALSE, answer.get(ReactCommandAnswer.FIELD_SUCCESS));
		assertEquals(ReactServlet.MISSING_WINDOW_NAME_MESSAGE, answer.get(ReactCommandAnswer.FIELD_ERROR));
		assertNull(answer.get(ReactCommandAnswer.FIELD_ERROR_CODE));
		assertEquals(List.of(), log.getLogEntries());
	}

	/**
	 * A command with an empty window name is answered with {@link ReactServlet#ERROR_CODE_STALE_UI}:
	 * the client loads its page again.
	 */
	public void testCommandWithEmptyWindowName() throws Exception {
		displayWindow(arguments -> null);
		_client.setExceptionsThrownOnErrorStatus(false);

		InvocationContext invocation = postCommand("", CONTROL_ID, COMMAND, false);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, invocation.getResponse().getStatus());
		assertErrorCode(ReactServlet.ERROR_CODE_STALE_UI, invocation);
	}

	/**
	 * A report of a closed or unloaded window without the argument {@code windowId} and without a
	 * window name is answered with success. It closes no window of the session and logs no error.
	 */
	public void testWindowClosedWithoutWindowId() throws Exception {
		displayWindow(arguments -> null);
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.ERROR, Level.FATAL), true);
		InvocationContext closed;
		InvocationContext unloaded;
		try {
			closed = postCommand(null, "", "windowClosed", Map.of(), false);
			unloaded = postCommand(null, "", "windowClosed", Map.of("unload", Boolean.TRUE), false);
		} finally {
			log.deactivate();
		}

		assertEquals(HttpServletResponse.SC_OK, closed.getResponse().getStatus());
		assertEquals("{\"success\":true}", closed.getServletResponse().getText());
		assertEquals(HttpServletResponse.SC_OK, unloaded.getResponse().getStatus());
		assertEquals("{\"success\":true}", unloaded.getServletResponse().getText());
		assertNotNull(ReactWindowRegistry.forSession(_session).getQueue(WINDOW));
		assertEquals(List.of(), log.getLogEntries());
	}

	/**
	 * A command that fails is answered without error status, with the message the window shows.
	 */
	public void testFailureInAnswer() throws Exception {
		displayWindow(arguments -> {
			throw new TopLogicException(ResKey.text("Storage is full."));
		});

		InvocationContext invocation = postCommand(Map.of());

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":false,\"error\":\"Storage is full.\"}",
			invocation.getServletResponse().getText());
	}

	/**
	 * A message of a type hierarchy is written as its fields alone, without the type tag that names
	 * its type among the others.
	 */
	public void testReplyOfTypeHierarchy() {
		StateEvent event = StateEvent.create().setControlId("c1").setState("{}");

		assertEquals("{\"success\":true,\"controlId\":\"c1\",\"state\":\"{}\"}",
			ReactCommandAnswer.success(event));
	}

	/**
	 * A reply type with a property of the name of a field of the answer itself is refused.
	 */
	public void testReservedPropertyRefused() {
		ReactCommandAnswer.checkReplyType(RouteRefusal.class);
		try {
			ReactCommandAnswer.checkReplyType(ReservedReply.class);
			fail("A reply type with a property 'success' must be refused.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	/**
	 * Property names of a message type, one of which clashes with the answer itself.
	 */
	public static final class ReservedReply {
		/** The name of a property. */
		public static final String SUCCESS__PROP = ReactCommandAnswer.FIELD_SUCCESS;
	}

	private static void assertErrorCode(String expected, InvocationContext invocation) throws Exception {
		Map<?, ?> answer = (Map<?, ?>) JSON.fromString(invocation.getServletResponse().getText());
		assertEquals(Boolean.FALSE, answer.get(ReactCommandAnswer.FIELD_SUCCESS));
		assertEquals(expected, answer.get(ReactCommandAnswer.FIELD_ERROR_CODE));
	}

	/**
	 * The entries of the given log that report a stale window.
	 */
	private static List<LogEntry> staleReports(CollectingLogListener log) {
		return log.getLogEntries().stream()
			.filter(entry -> entry.getCaller() == ReactWindowRegistry.class)
			.toList();
	}

	private InvocationContext postCommand(Map<String, Object> arguments) throws Exception {
		return postCommand(CONTROL_ID, arguments);
	}

	private InvocationContext postCommand(String controlId, Map<String, Object> arguments) throws Exception {
		return postCommand(WINDOW, controlId, COMMAND, arguments, false);
	}

	private InvocationContext postCommand(String controlId, String command, boolean ignoreStaleUi)
			throws Exception {
		return postCommand(WINDOW, controlId, command, ignoreStaleUi);
	}

	private InvocationContext postCommand(String window, String controlId, String command, boolean ignoreStaleUi)
			throws Exception {
		return postCommand(window, controlId, command, Map.of(), ignoreStaleUi);
	}

	private InvocationContext postCommand(String window, String controlId, String command,
			Map<String, Object> arguments, boolean ignoreStaleUi) throws Exception {
		Map<String, Object> payload = new HashMap<>();
		payload.put("controlId", controlId);
		payload.put("command", command);
		if (window != null) {
			payload.put("windowName", window);
		}
		payload.put("arguments", arguments);
		if (ignoreStaleUi) {
			payload.put(ReactServlet.FIELD_IGNORE_STALE_UI, Boolean.TRUE);
		}
		String body = JSON.toString(payload);
		PostMethodWebRequest request = new PostMethodWebRequest("http://ignore/" + REACT_SERVLET + "/command",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "application/json");
		InvocationContext invocation = _client.newInvocation(request);
		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());
		return invocation;
	}

	/**
	 * Creates the window with its subsession and model scope, as rendering its page does, and
	 * displays a control in it whose commands are answered by the given handler.
	 */
	private void displayWindow(Function<Map<String, Object>, DataObject> handler) {
		createWindow();
		ReactCommandTarget control = new ReactCommandTarget() {
			@Override
			public String getID() {
				return CONTROL_ID;
			}

			@Override
			public DataObject executeCommand(String commandName, Map<String, Object> arguments) {
				return handler.apply(arguments);
			}
		};
		ReactWindowRegistry.forSession(_session).getOrCreateQueue(WINDOW).registerControl(control);
	}

	/**
	 * Creates the window with its subsession and model scope, as rendering its page does, without
	 * displaying a control in it.
	 */
	private void createWindow() {
		TLSessionContext sessionContext = sessionContext();
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		registry.getOrCreateQueue(WINDOW).setWindowContext(WINDOW, sessionContext, registry);
		ThreadContextManager.inContext(sessionContext.getSubSession(WINDOW),
			() -> registry.getOrCreateModelScope(WINDOW));
	}

	private TLSessionContext sessionContext() {
		return SessionService.getInstance().getSession(_session);
	}

	private HttpSession login() throws Exception {
		InvocationContext invocation =
			_client.newInvocation(new PostMethodWebRequest("http://ignore/" + LOGIN_SERVLET));
		HttpSession session = ThreadContext.inSystemContext(TestCommandReply.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(),
				PersonManager.getManager().getRoot()));
		_client.getResponse(invocation);
		return session;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestCommandReply.class),
			SessionService.Module.INSTANCE, WindowModelEventService.Module.INSTANCE, CachePolicy.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
