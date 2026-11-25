/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.UUID;

import junit.framework.Test;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.tools.script.TLScriptExecutor;

/**
 * The closed door P3 of the test bed: a script reads or stops a run of another account.
 *
 * <p>
 * The row of the article: {@code ben} in the script console, with the handle that
 * {@code aiAgentChatAsync} gave {@code anna}. The console evaluates a script with the access rights
 * of the account, and so does this test. Every function that takes a run handle answers as for an
 * unknown handle. The same handle gives {@code anna} her run, so the door is closed for the others
 * only. {@code TestRunHandleAccessPolicy} of tl-ai-core covers each form of the handle, the
 * administrator and a script without access checks.
 * </p>
 */
public class SecDemoRunHandleTest extends SecDemoPersonaTest {

	private static final String FAILURE = "The provider refused the key.";

	/** The text of a transcript is not checked; it is only the value that must not leak. */
	private static final String LOG = "{\"turns\":[]}";

	/** Ben reads nothing of a run of Anna: no status, no answer, no error, no transcript. */
	public void testBenReadsNoRunOfAnna() {
		String handle = handleOf(failedRunOf(ANNA));
		actAs(BEN);

		assertNull("Not even ERROR: an answer of its own would tell that the run exists.",
			eval("h -> aiAgentRunStatus($h)", handle));
		assertNull("The failure of the run is not raised for Ben.",
			eval("h -> aiAgentRunResult($h)", handle));
		assertNull(eval("h -> aiAgentRunError($h)", handle));
		assertNull(eval("h -> aiAgentRunTranscript($h)", handle));
	}

	/** Ben stops no run of Anna. */
	public void testBenStopsNoRunOfAnna() {
		AgentExecution run = runningRunOf(ANNA);
		actAs(BEN);

		assertEquals(Boolean.FALSE, eval("h -> aiAgentRunCancel($h)", handleOf(run)));
		assertFalse("The run of Anna goes on.", AgentExecutionStore.getInstance().isCancelRequested(run));
	}

	/** Anna reads and stops her own run with the same handle. */
	public void testAnnaReadsAndStopsHerRun() {
		String failed = handleOf(failedRunOf(ANNA));
		AgentExecution running = runningRunOf(ANNA);
		actAs(ANNA);

		assertEquals(ExecutionStatus.ERROR.name(), eval("h -> aiAgentRunStatus($h)", failed));
		assertEquals(FAILURE, eval("h -> aiAgentRunError($h)", failed));
		assertEquals(LOG, eval("h -> aiAgentRunTranscript($h)", failed));
		assertEquals(Boolean.TRUE, eval("h -> aiAgentRunCancel($h)", handleOf(running)));
		assertTrue(AgentExecutionStore.getInstance().isCancelRequested(running));
	}

	/** Records a running run of the given persona, as {@code aiAgentChatAsync} starts one. */
	private static AgentExecution runningRunOf(String login) {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentExecution execution = store.beginExecution(
			store.createSession(SessionKind.BACKGROUND, account(login), "secdemo-run-handle"),
			"secdemo-run-handle-" + UUID.randomUUID(), "agent", null, null, null);
		HistoryUtils.updateSessionRevision();
		return execution;
	}

	/** Records a run of the given persona that failed. */
	private static AgentExecution failedRunOf(String login) {
		AgentExecution execution = runningRunOf(login);
		AgentExecutionStore.getInstance().finishExecution(execution, ExecutionStatus.ERROR, StopReason.ERROR,
			FAILURE, null, LOG);
		HistoryUtils.updateSessionRevision();
		return execution;
	}

	/** The handle {@code aiAgentChatAsync} returns for the run: the id of its session. */
	private static String handleOf(AgentExecution run) {
		return KBUtils.getWrappedObjectName(run.getSession()).toString();
	}

	/** Evaluates the given TL-Script with the access rights of the current account. */
	private static Object eval(String script, String handle) {
		return TLScriptExecutor.compile(script).executeWith(Args.some(handle));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoRunHandleTest.class, AgentExecutionStore.Module.INSTANCE);
	}

}
