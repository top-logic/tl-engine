/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.services.simpleajax.AJAXServlet;
import com.top_logic.base.services.simpleajax.CommandRequest;
import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.ContentHandlersRegistry;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.CachePolicy;

/**
 * Tests that the entry points of the classic user interface act for the person of their window and
 * leave no {@link ActingPerson} on the thread: a command posted to the {@link AJAXServlet}, and the
 * content of a window that {@link ContentHandlersRegistry} delivers.
 */
public class TestClassicRequestActingPerson extends BasicTestCase {

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String LAYOUT_SERVLET = "layout";

	private static final String AJAX_SERVLET = "ajax";

	private static final String WINDOW = "classicWindow";

	/** The name of the content handler of the window, the last segment of its URL. */
	private static final String CONTENT = "probe";

	private static final long TIMEOUT_SECONDS = 60;

	/** The acting person that the last command or content request saw. */
	static final AtomicReference<Person> SEEN = new AtomicReference<>();

	/** Whether the command or the content request fails after it looked at the acting person. */
	static final AtomicBoolean FAIL = new AtomicBoolean();

	private ServletRunner _runner;

	private ServletUnitClient _client;

	private HttpSession _session;

	/** The ID of {@link #_session}, which an ended session no longer tells. */
	private String _sessionId;

	private Person _person;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SEEN.set(null);
		FAIL.set(false);
		_runner = new ServletRunner();
		_runner.registerServlet(LOGIN_SERVLET, TLLayoutServlet.class.getName());
		Hashtable<String, String> noHeaderCheck = new Hashtable<>();
		noHeaderCheck.put(TLLayoutServlet.DISABLE_SECURE_HEADER_CHECK, "true");
		_runner.registerServlet(LAYOUT_SERVLET + "/*", TLLayoutServlet.class.getName(), noHeaderCheck);
		_runner.registerServlet(AJAX_SERVLET, ProbeAJAXServlet.class.getName());
		_client = _runner.newClient();
		_client.setExceptionsThrownOnErrorStatus(false);
		_person = createPerson();
		_session = login(_person);
		_sessionId = _session.getId();
		openWindow();
	}

	@Override
	protected void tearDown() throws Exception {
		ThreadContext.inSystemContext(TestClassicRequestActingPerson.class, () -> {
			SessionService.getInstance().terminateSession(_sessionId);
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
	 * A command posted to the {@link AJAXServlet} acts for the person of its window, and the request
	 * leaves no acting person on its thread, also when the command fails.
	 */
	public void testCommandActsForThePersonOfItsWindow() throws Exception {
		assertNull("The request left its acting person on the thread.", serve(command()));
		assertSame(_person, SEEN.get());

		SEEN.set(null);
		FAIL.set(true);
		assertNull("The failed request left its acting person on the thread.", serve(command()));
		assertSame(_person, SEEN.get());
	}

	/**
	 * The content of a window that {@link ContentHandlersRegistry} delivers is loaded for the person
	 * of the window, and the request leaves no acting person on its thread, also when it fails.
	 */
	public void testContentActsForThePersonOfItsWindow() throws Exception {
		assertNull("The request left its acting person on the thread.", serve(content()));
		assertSame(_person, SEEN.get());

		SEEN.set(null);
		FAIL.set(true);
		assertNull("The failed request left its acting person on the thread.", serve(content()));
		assertSame(_person, SEEN.get());
	}

	/**
	 * Looks at the acting person of the current request, then fails if {@link #FAIL} is set.
	 */
	static void probe() {
		SEEN.set(ActingPerson.handOver());
		if (FAIL.get()) {
			throw new IllegalStateException("Expected failure of the request.");
		}
	}

	private WebRequest command() {
		String component = mainLayout().getName().qualifiedName();
		String body = "<envelope><header>"
			+ "<source window='" + WINDOW + "'>" + component + "</source>"
			+ "</header><body><execute>"
			+ "<component source='" + component + "'/>"
			+ "<command>probe</command>"
			+ "</execute></body></envelope>";
		return new PostMethodWebRequest("http://ignore/" + AJAX_SERVLET,
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "text/xml");
	}

	private static WebRequest content() {
		return new GetMethodWebRequest("http://ignore/" + LAYOUT_SERVLET + "/" + WINDOW + "/" + CONTENT);
	}

	/**
	 * Serves the given request on a thread of its own, as the servlet container does.
	 *
	 * @return The acting person that the request left on its thread, as the window sees it
	 *         afterwards.
	 */
	private Person serve(WebRequest request) throws Exception {
		InvocationContext invocation = _client.newInvocation(request);
		return inNewThread(() -> {
			try {
				invocation.getServlet().service(invocation.getRequest(), invocation.getResponse());
			} catch (Exception ex) {
				if (!FAIL.get()) {
					throw ex;
				}
				// The expected failure, as the servlet passes it on.
			}
			return inWindow(ActingPerson::handOver);
		});
	}

	/**
	 * Opens the window of the person as a login does: its sub-session and its handler, with a
	 * component tree and a content handler that look at the acting person.
	 */
	private void openWindow() {
		// In a context of its own: the component tree installs a sub-session for its initialization.
		MainLayout mainLayout = ThreadContext.inSystemContext(TestClassicRequestActingPerson.class,
			() -> ComponentTestUtils.newMainLayout());
		// A command names the component it is sent to; here the root of the tree.
		mainLayout.registerComponent(mainLayout);
		TLSessionContext sessionContext = sessionContext();
		SubsessionHandler window = sessionContext.getHandlersRegistry().startLogin(sessionContext, null,
			new WindowId(WINDOW), _person, null, null);

		// The end of the login, without loading a layout: the window shows the given tree.
		ReflectionUtils.setValue(window, "_mainLayout", mainLayout);
		((ScheduledFuture<?>) ReflectionUtils.setValue(window, "_cancelTimer", null)).cancel(false);

		ContentHandler content = (context, id, url) -> probe();
		window.registerContentHandler(CONTENT, content);
	}

	private MainLayout mainLayout() {
		return sessionContext().getHandlersRegistry().getContentHandler(WINDOW).getMainLayout();
	}

	private <T> T inWindow(Supplier<T> action) {
		return ThreadContextManager.inContext(sessionContext().getSubSession(WINDOW), action::get);
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestClassicRequestActingPerson.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

	private TLSessionContext sessionContext() {
		return SessionService.getInstance().getSession(_session);
	}

	private HttpSession login(Person user) throws Exception {
		InvocationContext invocation =
			_client.newInvocation(new PostMethodWebRequest("http://ignore/" + LOGIN_SERVLET));
		// Keep the session context of the test thread.
		HttpSession session = ThreadContext.inSystemContext(TestClassicRequestActingPerson.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
		// Let the client keep the session cookie for the following requests.
		_client.getResponse(invocation);
		return session;
	}

	private static Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, "classicRequestUser",
				PersonManager.getManager().getRoot().getAuthenticationDevice());
			tx.commit();
			return result;
		}
	}

	/**
	 * The {@link AJAXServlet} of the test: it processes each command by looking at the acting
	 * person.
	 */
	public static class ProbeAJAXServlet extends AJAXServlet {

		@Override
		protected void processRequest(LayoutComponent contextComponent, CommandRequest ajaxRequest,
				UpdateWriter writer) {
			probe();
		}

	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestClassicRequestActingPerson.class),
			SessionService.Module.INSTANCE,
			SchedulerService.Module.INSTANCE,
			CachePolicy.Module.INSTANCE,
			RequestLockFactory.Module.INSTANCE,
			MimeTypes.Module.INSTANCE,
			SecurityObjectProviderManager.Module.INSTANCE,
			CommandHandlerFactory.Module.INSTANCE,
			TableConfigurationFactory.Module.INSTANCE,
			SecurityComponentCache.Module.INSTANCE,
			SafeHTML.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
