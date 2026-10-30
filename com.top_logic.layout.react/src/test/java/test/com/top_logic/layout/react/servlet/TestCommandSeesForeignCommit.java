/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.servlet.ReactServlet;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowModelEventService;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TLContext;

/**
 * Tests that a command posted to {@link ReactServlet} sees the commits other threads made since
 * the last request of its window.
 */
public class TestCommandSeesForeignCommit extends BasicTestCase {

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String REACT_SERVLET = "react";

	private static final String WINDOW = "vCommand";

	private static final String CONTROL_ID = "probe";

	private static final String COMMAND = "readLanguage";

	/** Time in milliseconds a helper thread is given to do its part. */
	private static final long THREAD_TIMEOUT_MILLIS = 10_000;

	private ServletRunner _runner;

	private ServletUnitClient _client;

	private HttpSession _session;

	private Person _person;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_runner = new ServletRunner();
		_runner.registerServlet(LOGIN_SERVLET, TLLayoutServlet.class.getName());
		_runner.registerServlet(REACT_SERVLET + "/*", ReactServlet.class.getName());
		_client = _runner.newClient();
		_person = createPerson();
		_session = login(_person);
	}

	@Override
	protected void tearDown() throws Exception {
		TLSubSessionContext subSession = sessionContext().getSubSession(WINDOW);
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> registry.windowClosed(WINDOW));
		}
		// Cleans up in a context of its own, whatever the requests served in the test thread left.
		ThreadContext.inSystemContext(TestCommandSeesForeignCommit.class, () -> {
			SessionService.getInstance().terminateSession(_session.getId());
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				_person.tDelete();
				tx.commit();
			}
		});
		_runner.shutDown();
		super.tearDown();
	}

	/**
	 * A command reads the state another thread committed after the window's last request, although
	 * the window does not observe the changed object.
	 */
	public void testCommandSeesForeignCommit() throws Exception {
		AtomicReference<Locale> seen = new AtomicReference<>();
		displayWindow(new ReactCommandTarget() {
			@Override
			public String getID() {
				return CONTROL_ID;
			}

			@Override
			public HandlerResult executeCommand(String commandName, Map<String, Object> arguments) {
				seen.set(_person.getLanguage());
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		Locale before = inWindow(() -> _person.getLanguage());
		Locale changed = Locale.GERMAN.equals(before) ? Locale.ENGLISH : Locale.GERMAN;

		inOtherThread(() -> setLanguage(_person, changed));
		assertEquals("Precondition: The window still sees the state of its last request.", before,
			inWindow(() -> _person.getLanguage()));

		InvocationContext invocation = postCommand();
		assertEquals(HttpServletResponse.SC_OK, invocation.getResponse().getStatus());
		assertEquals("The command must see the commit of the other thread.", changed, seen.get());
	}

	private InvocationContext postCommand() throws Exception {
		String body = JSON.toString(Map.of(
			"controlId", CONTROL_ID,
			"command", COMMAND,
			"windowName", WINDOW,
			"arguments", Map.of()));
		PostMethodWebRequest request = new PostMethodWebRequest("http://ignore/" + REACT_SERVLET + "/command",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "application/json");
		InvocationContext invocation = _client.newInvocation(request);
		invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());
		return invocation;
	}

	/**
	 * Creates the window with its subsession and model scope, as rendering its page does, and
	 * displays the given control in it.
	 */
	private void displayWindow(ReactCommandTarget control) {
		TLSessionContext sessionContext = sessionContext();
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(_session);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		registry.getOrCreateQueue(WINDOW).setWindowContext(WINDOW, sessionContext, registry);
		registry.getOrCreateQueue(WINDOW).registerControl(control);
		inWindow(() -> registry.getOrCreateModelScope(WINDOW));
	}

	private <T> T inWindow(Supplier<T> action) {
		TLSubSessionContext subSession = sessionContext().getSubSession(WINDOW);
		return ThreadContextManager.inContext(subSession, action::get);
	}

	private TLSessionContext sessionContext() {
		return SessionService.getInstance().getSession(_session);
	}

	private HttpSession login(Person user) throws Exception {
		InvocationContext invocation =
			_client.newInvocation(new PostMethodWebRequest("http://ignore/" + LOGIN_SERVLET));
		// Keep the session context of the test thread.
		HttpSession session = ThreadContext.inSystemContext(TestCommandSeesForeignCommit.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
		// Let the client keep the session cookie for the following requests.
		_client.getResponse(invocation);
		return session;
	}

	/**
	 * Runs the given action in a system context of a thread of its own and waits for it to
	 * complete.
	 */
	private static void inOtherThread(Runnable action) throws Exception {
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				ThreadContext.inSystemContext(TestCommandSeesForeignCommit.class, action::run);
			} catch (Throwable ex) {
				problem.set(ex);
			}
		});
		thread.start();
		thread.join(THREAD_TIMEOUT_MILLIS);
		assertFalse("The helper thread did not complete in time.", thread.isAlive());
		if (problem.get() != null) {
			throw new AssertionError("The helper thread failed.", problem.get());
		}
	}

	private static void setLanguage(Person person, Locale language) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			person.setLanguage(language);
			tx.commit();
		}
	}

	private static Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "commandSyncUser",
				PersonManager.getManager().getRoot().getAuthenticationDevice());
			tx.commit();
			return result;
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestCommandSeesForeignCommit.class),
			SessionService.Module.INSTANCE, WindowModelEventService.Module.INSTANCE, CachePolicy.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
