/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Who reads and who stops a run through the TL-Script functions that take a run handle
 * (Ticket #30482), with the shipped access policy for conversations.
 *
 * <p>
 * The owner of a run and an administrator read and stop it, and so does a script that is evaluated
 * without access checks. Every other account gets the answer of an unknown handle ({@code null} or
 * {@code false}), for each form of the handle: the run, its session, the id of the session and the
 * run id. Asked the way {@link TestChatSessionAccessPolicy} asks: TL-Script evaluated for an
 * account with the security checks on, with the roles and grants of
 * {@code tl-ai-core.chatSecurity.config.xml}.
 * </p>
 */
public class TestRunHandleAccessPolicy extends BasicTestCase {

	private static final String STATUS = "h -> aiAgentRunStatus($h)";

	private static final String RESULT = "h -> aiAgentRunResult($h)";

	private static final String ERROR = "h -> aiAgentRunError($h)";

	private static final String TRANSCRIPT = "h -> aiAgentRunTranscript($h)";

	private static final String CANCEL = "h -> aiAgentRunCancel($h)";

	private static final String ANSWER = "The answer of the run.";

	/** The text of a transcript is not checked; it is only the value that must not leak. */
	private static final String LOG = "{\"turns\":[]}";

	private static final String FAILURE = "The provider refused the key.";

	/** The accounts and runs that no test changes, made once. */
	private static Person owner;

	private static Person stranger;

	private static Person administrator;

	/** An owner that may only read. */
	private static Person restrictedOwner;

	private static Run completeRun;

	private static Run failedRun;

	/** A session whose run is not recorded yet: the state right after {@code aiAgentChatAsync}. */
	private static AgentSession queuedSession;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("run-handle-owner");
		stranger = TestPerson.createPerson("run-handle-stranger");
		administrator = TestPerson.createPerson("run-handle-admin");
		restrictedOwner = TestPerson.createPerson("run-handle-restricted");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			administrator.setAdmin(true);
			restrictedOwner.setRestrictedUser(Boolean.TRUE);
			tx.commit();
		}

		AgentExecutionStore store = AgentExecutionStore.getInstance();
		completeRun = startRun(owner);
		store.finishExecution(completeRun.execution(), ExecutionStatus.COMPLETE, StopReason.COMPLETE, null,
			null, LOG, ANSWER);
		failedRun = startRun(owner);
		store.finishExecution(failedRun.execution(), ExecutionStatus.ERROR, StopReason.ERROR, FAILURE, null,
			LOG);
		queuedSession = store.createSession(SessionKind.BACKGROUND, owner, "queued");
		HistoryUtils.updateSessionRevision();
	}

	/**
	 * Runs each test with the security checks on, so that the rights are decided for the account
	 * each call names, not bypassed for the system context the test runs in.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> withSecurityChecks(true, () -> {
			super.runTest();
			return null;
		});
	}

	/** Another account learns no status, and a queued run does not read as running. */
	public void testStrangerReadsNoStatus() {
		List<String> leaks = leaks(STATUS, completeRun.handles());
		leaks.addAll(leaks(STATUS, sessionHandles(queuedSession)));
		assertEquals(List.of(), leaks);
	}

	/** Another account reads no answer, and a failed run does not tell it the failure. */
	public void testStrangerReadsNoResult() {
		List<String> leaks = leaks(RESULT, completeRun.handles());
		leaks.addAll(leaks(RESULT, failedRun.handles()));
		assertEquals(List.of(), leaks);
	}

	/** Another account reads no error message. */
	public void testStrangerReadsNoError() {
		assertEquals(List.of(), leaks(ERROR, failedRun.handles()));
	}

	/** Another account reads no transcript, which holds the tool arguments and tool results. */
	public void testStrangerReadsNoTranscript() {
		assertEquals(List.of(), leaks(TRANSCRIPT, completeRun.handles()));
	}

	/** Another account stops no run: the answer is {@code false} and no cancel marker is set. */
	public void testStrangerStopsNoRun() {
		List<String> stops = new ArrayList<>();
		for (String form : Run.FORMS) {
			Run run = startRun(owner);
			HistoryUtils.updateSessionRevision();
			Object answer = evalAs(stranger, true, CANCEL, run.handles().get(form));
			boolean marked = AgentExecutionStore.getInstance().isCancelRequested(run.execution());
			if (!Boolean.FALSE.equals(answer) || marked) {
				stops.add(form + ": answer " + answer + ", cancel marker " + marked);
			}
		}
		assertEquals(List.of(), stops);
	}

	/**
	 * The run id of a delegated run names that run, and its rights come from the session of its
	 * parent run: the owner reads and stops it, another account does neither.
	 */
	public void testDelegatedRunByRunId() {
		Run parent = startRun(owner);
		AgentExecution delegated = AgentExecutionStore.getInstance().beginExecution(parent.session(),
			"run-handle-delegated-" + UUID.randomUUID(), "sub-agent", null, null, parent.execution());
		HistoryUtils.updateSessionRevision();
		String runId = delegated.getRunId();

		assertNull(evalAs(stranger, true, STATUS, runId));
		assertEquals(Boolean.FALSE, evalAs(stranger, true, CANCEL, runId));
		assertFalse(AgentExecutionStore.getInstance().isCancelRequested(delegated));

		assertEquals(ExecutionStatus.RUNNING.name(), evalAs(owner, true, STATUS, runId));
		assertEquals(Boolean.TRUE, evalAs(owner, true, CANCEL, runId));
		assertTrue(AgentExecutionStore.getInstance().isCancelRequested(delegated));
		assertFalse("The run id names the delegated run, not its parent.",
			AgentExecutionStore.getInstance().isCancelRequested(parent.execution()));
	}

	/** The owner reads and stops their own run. */
	public void testOwnerReadsAndStops() {
		assertReadsAndStops(owner, true);
	}

	/** An administrator reads and stops the run of another account. */
	public void testAdministratorReadsAndStops() {
		assertReadsAndStops(administrator, true);
	}

	/**
	 * A script evaluated without access checks, as a scheduler task or a privileged function is,
	 * reads and stops a run of another account.
	 */
	public void testUnsecuredScriptReadsAndStops() {
		assertReadsAndStops(stranger, false);
	}

	/**
	 * A restricted account, which may only read, reads its own run and cannot stop it, since a stop
	 * needs the right {@code Write}.
	 */
	public void testRestrictedOwnerReadsButStopsNothing() {
		Run run = startRun(restrictedOwner);
		HistoryUtils.updateSessionRevision();
		Object handle = run.handles().get("session id");

		assertEquals(ExecutionStatus.RUNNING.name(), evalAs(restrictedOwner, true, STATUS, handle));
		assertEquals(Boolean.FALSE, evalAs(restrictedOwner, true, CANCEL, handle));
		assertFalse(AgentExecutionStore.getInstance().isCancelRequested(run.execution()));
	}

	private static void assertReadsAndStops(Person account, boolean withSecurity) {
		for (Map.Entry<String, Object> handle : completeRun.handles().entrySet()) {
			String form = handle.getKey();
			assertEquals(form, ExecutionStatus.COMPLETE.name(),
				evalAs(account, withSecurity, STATUS, handle.getValue()));
			assertEquals(form, ANSWER, evalAs(account, withSecurity, RESULT, handle.getValue()));
			assertEquals(form, LOG, evalAs(account, withSecurity, TRANSCRIPT, handle.getValue()));
		}
		for (Map.Entry<String, Object> handle : failedRun.handles().entrySet()) {
			String form = handle.getKey();
			assertEquals(form, ExecutionStatus.ERROR.name(), evalAs(account, withSecurity, STATUS, handle.getValue()));
			assertEquals(form, FAILURE, evalAs(account, withSecurity, ERROR, handle.getValue()));
			try {
				evalAs(account, withSecurity, RESULT, handle.getValue());
				fail(form + ": a failed run must not report its absent answer as a value.");
			} catch (RuntimeException ex) {
				assertTrue(form + ": " + ex.getMessage(), String.valueOf(ex.getMessage()).contains(FAILURE));
			}
		}
		for (Map.Entry<String, Object> handle : sessionHandles(queuedSession).entrySet()) {
			assertEquals(handle.getKey(), ExecutionStatus.RUNNING.name(),
				evalAs(account, withSecurity, STATUS, handle.getValue()));
		}
		for (String form : Run.FORMS) {
			Run run = startRun(owner);
			HistoryUtils.updateSessionRevision();
			assertEquals(form, Boolean.TRUE, evalAs(account, withSecurity, CANCEL, run.handles().get(form)));
			assertTrue(form, AgentExecutionStore.getInstance().isCancelRequested(run.execution()));
		}
	}

	/**
	 * What the given script tells the stranger for each handle: every answer but {@code null} and
	 * {@code false}, and every failure, since a failure names the recorded error.
	 */
	private static List<String> leaks(String script, Map<String, Object> handles) {
		List<String> leaks = new ArrayList<>();
		for (Map.Entry<String, Object> handle : handles.entrySet()) {
			Object answer;
			try {
				answer = evalAs(stranger, true, script, handle.getValue());
			} catch (RuntimeException ex) {
				answer = "failure " + ex.getMessage();
			}
			if (answer != null && !Boolean.FALSE.equals(answer)) {
				leaks.add(script + " with the " + handle.getKey() + ": " + answer);
			}
		}
		return leaks;
	}

	/**
	 * Evaluates the given TL-Script for the given account.
	 *
	 * @param withSecurity
	 *        Whether the script is evaluated with the access rights of the account. Without them,
	 *        the script is evaluated as a scheduler task or a privileged function evaluates it.
	 */
	private static Object evalAs(Person account, boolean withSecurity, String script, Object handle) {
		return TLContextManager.<Object, RuntimeException, RuntimeException> inPersonContext(account, () -> {
			QueryExecutor executor = TLScriptExecutor.compile(script);
			if (!withSecurity) {
				executor.disableSecurity();
			}
			return executor.executeWith(Args.some(handle));
		});
	}

	/** Records a new running run of the given account, with a session of its own. */
	private static Run startRun(Person account) {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.BACKGROUND, account, "run-handle");
		AgentExecution execution =
			store.beginExecution(session, "run-handle-" + UUID.randomUUID(), "agent", null, null, null);
		return new Run(session, execution);
	}

	/** The forms of a handle that name a session; a session whose run is not recorded has no others. */
	private static Map<String, Object> sessionHandles(AgentSession session) {
		Map<String, Object> handles = new LinkedHashMap<>();
		handles.put("session", session);
		handles.put("session id", KBUtils.getWrappedObjectName(session).toString());
		return handles;
	}

	/** A recorded run and the session it belongs to. */
	private record Run(AgentSession session, AgentExecution execution) {

		/** The names of the forms of a handle, in the order of {@link #handles()}. */
		static final List<String> FORMS = List.of("execution", "session", "session id", "run id");

		/** Each form of a handle of this run, by the name of the form. */
		Map<String, Object> handles() {
			Map<String, Object> handles = new LinkedHashMap<>();
			handles.put("execution", execution);
			handles.putAll(sessionHandles(session));
			handles.put("run id", execution.getRunId());
			return handles;
		}
	}

	/**
	 * Creates the test suite with the services the policy is made of: the model, the access manager
	 * that derives the roles, the rights service that grants them, and the store of the
	 * conversations.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunHandleAccessPolicy.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
