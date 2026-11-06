/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.protocol.RouteResumeEvent;
import com.top_logic.layout.react.servlet.ReactServlet;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowModelEventService;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TLContext;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Tests that the {@link ReactCommandReply} a command handler attaches to its result reaches the
 * caller in the answer {@link ReactServlet} writes for the command, and the answer to a command
 * whose control is not displayed.
 */
public class TestCommandReply extends BasicTestCase {

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String REACT_SERVLET = "react";

	private static final String WINDOW = "vReply";

	private static final String CONTROL_ID = "replier";

	private static final String GONE_CONTROL_ID = "gone";

	private static final String COMMAND = "create";

	private static final String FIELD_ID = "id";

	private static final String FIELD_COUNT = "count";

	private static final String FIELD_DETAILS = "details";

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
	 * The fields of the reply arrive in the answer next to {@code success}, in the order the
	 * handler put them, with structured values.
	 */
	public void testReplyFieldsInAnswer() throws Exception {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("names", List.of("a", "b"));
		details.put("done", Boolean.TRUE);
		displayWindow(arguments -> ReactCommandReply.create()
			.put(FIELD_ID, "s-" + arguments.get("name"))
			.put(FIELD_COUNT, Integer.valueOf(3))
			.put(FIELD_DETAILS, details)
			.toResult());

		InvocationContext invocation = postCommand(Map.of("name", "x"));

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		String answer = invocation.getServletResponse().getText();
		assertEquals("{\"success\":true,\"id\":\"s-x\",\"count\":3,\"details\":{\"names\":[\"a\",\"b\"],\"done\":true}}",
			answer);
	}

	/**
	 * A command whose handler replies nothing is answered with {@code success} alone.
	 */
	public void testNoReply() throws Exception {
		displayWindow(arguments -> HandlerResult.DEFAULT_RESULT);

		InvocationContext invocation = postCommand(Map.of());

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":true}", invocation.getServletResponse().getText());
	}

	/**
	 * A command for a control that is not displayed in a window that still has its controls is
	 * answered with {@link ReactServlet#ERROR_CODE_CONTROL_GONE}.
	 */
	public void testCommandToGoneControl() throws Exception {
		displayWindow(arguments -> HandlerResult.DEFAULT_RESULT);
		// Lets the client read the body of an error answer.
		_client.setExceptionsThrownOnErrorStatus(false);

		InvocationContext invocation = postCommand(GONE_CONTROL_ID, Map.of());

		assertEquals(HttpServletResponse.SC_NOT_FOUND, invocation.getResponse().getStatus());
		Map<?, ?> answer = (Map<?, ?>) JSON.fromString(invocation.getServletResponse().getText());
		assertEquals(Boolean.FALSE, answer.get(ReactCommandReply.FIELD_SUCCESS));
		assertEquals(ReactServlet.ERROR_CODE_CONTROL_GONE, answer.get(ReactCommandReply.FIELD_ERROR_CODE));
	}

	/**
	 * A reply attached to the shared {@link HandlerResult#DEFAULT_RESULT} ends up on a result of its
	 * own, and the shared result stays without reply.
	 */
	public void testAttachToDefaultResult() {
		HandlerResult result = ReactCommandReply.create().put(FIELD_ID, "a").attachTo(HandlerResult.DEFAULT_RESULT);

		assertNotSame(HandlerResult.DEFAULT_RESULT, result);
		assertTrue(result.isSuccess());
		assertEquals("a", ReactCommandReply.of(result).getFields().get(FIELD_ID));
		assertNull(ReactCommandReply.of(HandlerResult.DEFAULT_RESULT));
	}

	/**
	 * The fields of the answer itself are refused as names of reply fields.
	 */
	public void testReservedFieldsRefused() {
		for (String reserved : List.of(ReactCommandReply.FIELD_SUCCESS, ReactCommandReply.FIELD_ERROR,
			ReactCommandReply.FIELD_ERROR_CODE)) {
			try {
				ReactCommandReply.create().put(reserved, "x");
				fail("The reserved field '" + reserved + "' must be refused.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
		}
	}

	/**
	 * A value that is no JSON value is refused when it is put, not when the answer is written.
	 */
	public void testNonJsonValueRefused() {
		try {
			ReactCommandReply.create().put(FIELD_DETAILS, Map.of("when", new Object()));
			fail("A value that is no JSON value must be refused.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/**
	 * A failed result writes its reply as well: the failure is reported in the window, the answer
	 * tells the caller what the handler attached.
	 */
	public void testFailedResultKeepsReply() {
		HandlerResult failed = HandlerResult.error(ResKey.text("failed"));
		ReactCommandReply.create().put(FIELD_ID, "r").attachTo(failed);

		assertEquals("{\"success\":true,\"id\":\"r\"}", ReactCommandReply.answerJson(failed));
	}

	/**
	 * A msgbuf message as field value is written as its own JSON serialization.
	 */
	public void testDataObjectValue() throws Exception {
		RouteResumeEvent event = RouteResumeEvent.create().setUrl("/view/x");
		StringW expected = new StringW();
		try (JsonWriter json = new JsonWriter(expected)) {
			event.writeTo(json);
		}

		String answer = ReactCommandReply.answerJson(ReactCommandReply.create().put(FIELD_DETAILS, event));

		assertEquals("{\"success\":true,\"" + FIELD_DETAILS + "\":" + expected + "}", answer);
	}

	private InvocationContext postCommand(Map<String, Object> arguments) throws Exception {
		return postCommand(CONTROL_ID, arguments);
	}

	private InvocationContext postCommand(String controlId, Map<String, Object> arguments) throws Exception {
		String body = JSON.toString(Map.of(
			"controlId", controlId,
			"command", COMMAND,
			"windowName", WINDOW,
			"arguments", arguments));
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
	private void displayWindow(Function<Map<String, Object>, HandlerResult> handler) {
		ReactCommandTarget control = new ReactCommandTarget() {
			@Override
			public String getID() {
				return CONTROL_ID;
			}

			@Override
			public HandlerResult executeCommand(String commandName, Map<String, Object> arguments) {
				return handler.apply(arguments);
			}
		};
		TLSessionContext sessionContext = sessionContext();
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		registry.getOrCreateQueue(WINDOW).setWindowContext(WINDOW, sessionContext, registry);
		registry.getOrCreateQueue(WINDOW).registerControl(control);
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
