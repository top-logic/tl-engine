/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
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
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.TLContext;

/**
 * Tests that what is done with the result of a job in the window it was started from sees the
 * commits the job made.
 *
 * <p>
 * The body of a job runs in a sub-session of its own, so the window learns about the job's commits
 * only through its model events. The terminal snapshot of the job is delivered into the window, and
 * the work taken up with the job's result - a selection of the object the job created, for example
 * - runs from within that delivery. The window must have received the model events of the job's
 * commits by then, as it has after a command it ran itself.
 * </p>
 *
 * <p>
 * No service delivers commits of other threads to the window in this test, so that the window
 * receives them through the delivery of the job's snapshot or not at all - as when such a delivery
 * is held back by its interval, because the window received a commit shortly before.
 * </p>
 */
public class TestJobResultSeesJobCommit extends BasicTestCase {

	private static final String WINDOW = "vJob";

	/** How long the test waits for any step before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	private final List<Person> _created = new ArrayList<>();

	private int _personCount;

	private HttpSession _session;

	private ReactWindowRegistry _registry;

	private Person _user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_user = ThreadContext.inSystemContext(TestJobResultSeesJobCommit.class, () -> createPerson());
		_session = login(_user);
		_registry = ReactWindowRegistry.forSession(_session);
		displayWindow();
	}

	@Override
	protected void tearDown() throws Exception {
		TLSubSessionContext subSession = sessionContext().getSubSession(WINDOW);
		if (subSession != null) {
			ThreadContextManager.inContext(subSession, () -> _registry.windowClosed(WINDOW));
		}
		// Cleans up in a context of its own, whatever the window's sub-session has seen.
		ThreadContext.inSystemContext(TestJobResultSeesJobCommit.class, () -> {
			SessionService.getInstance().terminateSession(_session.getId());
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				synchronized (_created) {
					for (Person person : _created) {
						if (person.tValid()) {
							person.tDelete();
						}
					}
				}
				tx.commit();
			}
		});
		_created.clear();
		_session = null;
		_registry = null;
		_user = null;
		super.tearDown();
	}

	/**
	 * The window has received the creation of the object the job created when the job's result is
	 * delivered to it.
	 */
	public void testResultSeesCreation() throws Exception {
		assertResultSeesCreation(false);
	}

	/**
	 * The window has received the creation of the object the job created when the job's result is
	 * delivered to it, also when the window has committed something of its own while the job ran.
	 */
	public void testResultSeesCreationAfterCommitOfWindow() throws Exception {
		assertResultSeesCreation(true);
	}

	private void assertResultSeesCreation(boolean commitInWindow) throws Exception {
		Recorder recorder = new Recorder();
		inWindow(() -> {
			ModelScope scope = _registry.getOrCreateModelScope(WINDOW);
			_registry.synthesizeModelEvents(WINDOW);
			scope.addModelListener(recorder);
		});

		CountDownLatch windowDone = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		AtomicReference<Boolean> seenOnResult = new AtomicReference<>();
		ViewChannel channel = new DefaultViewChannel("job");
		channel.addListener((sender, oldValue, newValue) -> {
			JobState state = (JobState) newValue;
			if (state.isFinished()) {
				// What a <with-result> does with the result runs here.
				seenOnResult.set(recorder.hasCreated(state.result()));
			}
		});

		inWindow(() -> {
			new JobRunner(new WindowContext(), channel, List.of(), false, 0, state -> finished.countDown())
				.start((job, arguments) -> {
					await(windowDone);
					return createPerson();
				}, List.of());
		});

		if (commitInWindow) {
			// A request of the window that creates an object and settles the window's model.
			inWindow(() -> {
				try (Interaction interaction = _registry.beginInteraction()) {
					createPerson();
					_registry.synthesizeModelEvents(WINDOW);
				}
			});
		}
		windowDone.countDown();

		await(finished);
		JobState end = (JobState) channel.get();
		assertEquals("The job must not fail: " + end.error(), JobStatus.COMPLETED, end.status());
		assertNotNull(end.result());
		assertEquals("The window must have received the creation of the job's object before its result.",
			Boolean.TRUE, seenOnResult.get());
	}

	/**
	 * Records the objects whose creation was delivered to it.
	 */
	private static final class Recorder implements ModelListener {

		private final List<TLObject> _createdObjects = new ArrayList<>();

		@Override
		public synchronized void notifyChange(ModelChangeEvent change) {
			change.getCreated().forEach(_createdObjects::add);
		}

		/** Whether the creation of the given object was delivered. */
		public synchronized boolean hasCreated(Object object) {
			return _createdObjects.contains(object);
		}
	}

	/**
	 * The {@link ReactContext} of {@link TestJobResultSeesJobCommit#WINDOW}.
	 */
	private class WindowContext implements ReactContext {

		private int _nextId;

		@Override
		public String allocateId() {
			return "id-" + (_nextId++);
		}

		@Override
		public String getWindowName() {
			return WINDOW;
		}

		@Override
		public String getContextPath() {
			return "/test";
		}

		@Override
		public SSEUpdateQueue getSSEQueue() {
			return _registry.getQueue(WINDOW);
		}

		@Override
		public ReactWindowRegistry getWindowRegistry() {
			return _registry;
		}

		@Override
		public ModelScope getModelScope() {
			return _registry.getOrCreateModelScope(WINDOW);
		}
	}

	/**
	 * Creates the window with its sub-session, as rendering its page does.
	 */
	private void displayWindow() {
		TLSessionContext sessionContext = sessionContext();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLContext subSession = new TLContext();
		subSession.setSessionContext(sessionContext);
		subSession.setPerson(_user);
		sessionContext.setIfAbsent(WINDOW, subSession);
		subSession.updateSessionRevision(kb.getHistoryManager(), ((UpdateChainView) kb.getUpdateChain()).current());
		_registry.getOrCreateQueue(WINDOW).setWindowContext(WINDOW, sessionContext, _registry);
	}

	private void inWindow(Runnable action) {
		TLSubSessionContext subSession = sessionContext().getSubSession(WINDOW);
		ThreadContextManager.inContext(subSession, action::run);
	}

	private TLSessionContext sessionContext() {
		return SessionService.getInstance().getSession(_session);
	}

	private Person createPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result;
			synchronized (_created) {
				result = Person.create(kb, "jobResultUser" + (++_personCount),
					PersonManager.getManager().getRoot().getAuthenticationDevice());
				_created.add(result);
			}
			tx.commit();
			return result;
		}
	}

	private static HttpSession login(Person user) throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
		// Keep the session context of the test thread.
		return ThreadContext.inSystemContext(TestJobResultSeesJobCommit.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("Timeout waiting for another thread.", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Test suite requiring a knowledge base with persons, sessions, and the
	 * {@link SchedulerService} the job runs on.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestJobResultSeesJobCommit.class),
				SessionService.Module.INSTANCE, SchedulerService.Module.INSTANCE));
	}

}
