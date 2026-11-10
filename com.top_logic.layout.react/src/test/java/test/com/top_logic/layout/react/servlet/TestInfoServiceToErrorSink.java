/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.ReactServlet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowModelEventService;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TLContext;

import de.haumacher.msgbuf.data.DataObject;

/**
 * Tests that a message a command reports through {@link InfoService} reaches the
 * {@link ErrorSink} of the window the command was sent to, when {@link ReactServlet} answers the
 * command.
 */
public class TestInfoServiceToErrorSink extends BasicTestCase {

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String REACT_SERVLET = "react";

	private static final String WINDOW = "vInfo";

	private static final String COMMAND = "report";

	private static final String SUMMARY = "Changes saved.";

	private static final String DETAIL = "Two related objects were adjusted.";

	/** The kind of message an {@link ErrorSink} was asked to show. */
	private enum Kind {
		/** {@link ErrorSink#showError(HTMLFragment)}. */
		ERROR,
		/** {@link ErrorSink#showWarning(HTMLFragment)}. */
		WARNING,
		/** {@link ErrorSink#showInfo(HTMLFragment)}. */
		INFO;
	}

	/** One message shown to an {@link ErrorSink}, as the HTML it renders to. */
	private record Shown(Kind kind, String html) {
		// Pure value.
	}

	/**
	 * An {@link ErrorSink} recording what it is asked to show.
	 *
	 * <p>
	 * Like the snackbar of a window, it renders the content right away in the display context of
	 * the request.
	 * </p>
	 */
	private static final class RecordingSink implements ErrorSink {

		final List<Shown> _shown = new ArrayList<>();

		@Override
		public void showError(HTMLFragment content) {
			record(Kind.ERROR, content);
		}

		@Override
		public void showWarning(HTMLFragment content) {
			record(Kind.WARNING, content);
		}

		@Override
		public void showInfo(HTMLFragment content) {
			record(Kind.INFO, content);
		}

		private void record(Kind kind, HTMLFragment content) {
			DisplayContext context = DefaultDisplayContext.getDisplayContext();
			TagWriter out = new TagWriter();
			try {
				content.write(context, out);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			_shown.add(new Shown(kind, out.toString()));
		}
	}

	private ServletRunner _runner;

	private ServletUnitClient _client;

	private HttpSession _session;

	private RecordingSink _sink;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_runner = new ServletRunner();
		_runner.registerServlet(LOGIN_SERVLET, TLLayoutServlet.class.getName());
		_runner.registerServlet(REACT_SERVLET + "/*", ReactServlet.class.getName());
		_client = _runner.newClient();
		_session = login();
		_sink = new RecordingSink();
	}

	@Override
	protected void tearDown() throws Exception {
		TLSubSessionContext subSession = sessionContext().getSubSession(WINDOW);
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> registry.windowClosed(WINDOW));
		}
		ThreadContext.inSystemContext(TestInfoServiceToErrorSink.class,
			() -> SessionService.getInstance().terminateSession(_session.getId()));
		_runner.shutDown();
		super.tearDown();
	}

	public void testInfoWithDetail() throws Exception {
		assertShown(Kind.INFO, () -> InfoService.showInfo(ResKey.text(SUMMARY), ResKey.text(DETAIL)));
	}

	public void testWarningWithDetail() throws Exception {
		assertShown(Kind.WARNING, () -> InfoService.showWarning(ResKey.text(SUMMARY), ResKey.text(DETAIL)));
	}

	public void testErrorWithDetail() throws Exception {
		assertShown(Kind.ERROR, () -> InfoService.showError(ResKey.text(SUMMARY), ResKey.text(DETAIL)));
	}

	public void testInfoWithoutDetail() throws Exception {
		String control = displayWindow(() -> InfoService.showInfo(ResKey.text(SUMMARY)));

		postCommand(control);

		assertEquals(1, _sink._shown.size());
		Shown shown = _sink._shown.get(0);
		assertEquals(Kind.INFO, shown.kind());
		assertTrue(shown.html(), shown.html().contains(SUMMARY));
		assertFalse(shown.html(), shown.html().contains(DETAIL));
	}

	private void assertShown(Kind kind, Runnable report) throws Exception {
		String control = displayWindow(report);

		postCommand(control);

		assertEquals(1, _sink._shown.size());
		Shown shown = _sink._shown.get(0);
		assertEquals(kind, shown.kind());
		String html = shown.html();
		assertTrue(html, html.contains(SUMMARY));
		assertTrue(html, html.contains(DETAIL));
		assertTrue("Summary not before detail: " + html, html.indexOf(SUMMARY) < html.indexOf(DETAIL));
	}

	private void postCommand(String controlId) throws Exception {
		String body = JSON.toString(Map.of(
			"controlId", controlId,
			"command", COMMAND,
			"windowName", WINDOW,
			"arguments", Map.of()));
		PostMethodWebRequest request = new PostMethodWebRequest("http://ignore/" + REACT_SERVLET + "/command",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "application/json");
		InvocationContext invocation = _client.newInvocation(request);
		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());

		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("{\"success\":true}", invocation.getServletResponse().getText());
	}

	/**
	 * Creates the window with its subsession and model scope, as rendering its page does, and
	 * displays a control in it whose command runs the given report.
	 *
	 * @return The ID of the displayed control.
	 */
	private String displayWindow(Runnable report) {
		TLSessionContext sessionContext = sessionContext();
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		SSEUpdateQueue queue = registry.getOrCreateQueue(WINDOW);
		queue.setWindowContext(WINDOW, sessionContext, registry);
		ThreadContextManager.inContext(sessionContext.getSubSession(WINDOW),
			() -> registry.getOrCreateModelScope(WINDOW));

		DefaultReactContext context = new DefaultReactContext("", WINDOW, queue, registry) {
			@Override
			public ErrorSink getErrorSink() {
				return _sink;
			}
		};
		ReactControl control = new ReactControl(context, null, "TLTest") {
			@Override
			public DataObject executeCommand(String commandName, Map<String, Object> arguments) {
				report.run();
				return null;
			}
		};
		queue.registerControl(control);
		return control.getID();
	}

	private TLSessionContext sessionContext() {
		return SessionService.getInstance().getSession(_session);
	}

	private HttpSession login() throws Exception {
		InvocationContext invocation =
			_client.newInvocation(new PostMethodWebRequest("http://ignore/" + LOGIN_SERVLET));
		HttpSession session = ThreadContext.inSystemContext(TestInfoServiceToErrorSink.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(),
				PersonManager.getManager().getRoot()));
		_client.getResponse(invocation);
		return session;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestInfoServiceToErrorSink.class),
			SessionService.Module.INSTANCE, WindowModelEventService.Module.INSTANCE, CachePolicy.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
