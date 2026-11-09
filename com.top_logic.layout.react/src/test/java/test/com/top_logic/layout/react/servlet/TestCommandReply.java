/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
import com.top_logic.layout.react.control.ReactCommandAnswer;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommandTarget;
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
 * {@link ReactServlet} writes for the command, and how a failure of the command is answered, see
 * {@link ReactCommandAnswer}.
 */
public class TestCommandReply extends BasicTestCase {

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String REACT_SERVLET = "react";

	private static final String WINDOW = "vReply";

	private static final String CONTROL_ID = "replier";

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

	private InvocationContext postCommand(Map<String, Object> arguments) throws Exception {
		String body = JSON.toString(Map.of(
			"controlId", CONTROL_ID,
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
	private void displayWindow(Function<Map<String, Object>, DataObject> handler) {
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
