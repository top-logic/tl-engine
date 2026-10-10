/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link AgentExecutionStore#failOrphanedRunningExecutions(Date)}, the startup sweep that
 * ends the runs a previous process left behind.
 *
 * <p>
 * Against a real {@link com.top_logic.knowledge.service.KnowledgeBase}, because the sweep is a
 * query over the recorded rows and writes them on the store's writer thread - the caller has to
 * advance its stable view to read the result, exactly as the operator view does.
 * </p>
 */
public class TestOrphanedRunReaper extends BasicTestCase {

	/**
	 * A row left {@link ExecutionStatus#RUNNING} by an earlier process is failed and gets the end
	 * date it never wrote.
	 */
	public void testOrphanedRunIsFailed() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "reaper-orphan");
		store.beginExecution(session, "run-orphan", "orchestrator", "test-model", null, null);

		AgentExecution row = reread("run-orphan");
		assertEquals("The new row starts out running",
			ExecutionStatus.RUNNING.name(), row.getStatus().getName());

		// A boot one millisecond after the run started: nothing of this process can be that old.
		int failed = store.failOrphanedRunningExecutions(new Date(row.getStartedAt().getTime() + 1));
		assertTrue("The orphan must be among the swept rows", failed >= 1);

		row = reread("run-orphan");
		assertEquals("A cut-off run failed, it was not cancelled by anybody",
			ExecutionStatus.ERROR.name(), row.getStatus().getName());
		assertEquals(StopReason.ERROR.name(), row.getStopReason());
		assertEquals(AgentExecutionStore.ORPHANED_RUN_ERROR, row.getErrorMessage());
		assertNotNull("The sweep must end the run, so that its duration stops growing",
			row.getEndedAt());
	}

	/**
	 * A background subagent's run, left running under a lead that had finished long before, is
	 * failed like any other: no subagent survives a restart.
	 */
	public void testOrphanedSubagentRunIsFailed() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "reaper-subagent");
		AgentExecution lead = store.beginExecution(session, "run-lead", "orchestrator", "test-model", null, null);
		store.finishExecution(lead, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, TokenUsage.EMPTY, null);
		store.beginExecution(session, "run-child", "worker", "test-model", null, reread("run-lead"));

		AgentExecution child = reread("run-child");
		store.failOrphanedRunningExecutions(new Date(child.getStartedAt().getTime() + 1));

		child = reread("run-child");
		assertEquals(ExecutionStatus.ERROR.name(), child.getStatus().getName());
		assertNotNull(child.getEndedAt());
		assertEquals("The lead had ended on its own.",
			ExecutionStatus.COMPLETE.name(), reread("run-lead").getStatus().getName());
	}

	/**
	 * A run that started after the boot instant belongs to this process and is left alone, even
	 * while the remaining services are still coming up.
	 */
	public void testRunOfThisProcessIsNotSwept() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "reaper-live");
		store.beginExecution(session, "run-live", "orchestrator", "test-model", null, null);

		AgentExecution row = reread("run-live");
		// The tightest case: the run started in the very millisecond the service booted.
		store.failOrphanedRunningExecutions(row.getStartedAt());

		row = reread("run-live");
		assertEquals("A live run must survive the sweep",
			ExecutionStatus.RUNNING.name(), row.getStatus().getName());
		assertNull(row.getEndedAt());

		// Leave no running row behind for the other tests of this suite.
		store.finishExecution(row, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null, null);
	}

	/**
	 * A run that reached a terminal state is not touched, whatever the cut-off.
	 */
	public void testFinishedRunIsNotSwept() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "reaper-finished");
		store.beginExecution(session, "run-finished", "orchestrator", "test-model", null, null);

		AgentExecution row = reread("run-finished");
		Date startedAt = row.getStartedAt();
		store.finishExecution(row, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null, null);

		row = reread("run-finished");
		Date endedAt = row.getEndedAt();
		store.failOrphanedRunningExecutions(new Date(startedAt.getTime() + 60_000));

		row = reread("run-finished");
		assertEquals("A finished run keeps its status",
			ExecutionStatus.COMPLETE.name(), row.getStatus().getName());
		assertEquals("A finished run keeps its end date", endedAt, row.getEndedAt());
		assertNull(row.getErrorMessage());
	}

	/**
	 * A checkpoint that reaches the writer after the run ended does not replace the final
	 * transcript (#598): the checkpoint is what a running run leaves behind, the final transcript
	 * what it said.
	 */
	public void testCheckpointNeverOverwritesTheFinalTranscript() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "reaper-checkpoint");
		store.beginExecution(session, "run-checkpoint", "orchestrator", "test-model", null, null);

		AgentExecution row = reread("run-checkpoint");
		assertTrue(store.checkpointTranscript(row, () -> "{\"turns\":[]}"));
		store.finishExecution(row, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null, "final");
		assertEquals("final", reread("run-checkpoint").getTranscript());

		assertTrue(store.checkpointTranscript(row, () -> "late"));
		// Any write waits for the writer, which carries out the queued checkpoint first.
		store.failOrphanedRunningExecutions(new Date(0));
		assertEquals("final", reread("run-checkpoint").getTranscript());
	}

	/**
	 * The row of the given run, read at a revision that includes the writer thread's commits.
	 */
	private static AgentExecution reread(String runId) {
		HistoryUtils.updateSessionRevision();
		AgentExecution row = AgentExecutionStore.getInstance().findExecutionByRunId(runId);
		assertNotNull("The run must have been recorded: " + runId, row);
		return row;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestOrphanedRunReaper.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
