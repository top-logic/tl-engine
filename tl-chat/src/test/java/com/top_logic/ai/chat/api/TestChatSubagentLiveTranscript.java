/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A running subagent's transcript opens while it works (issue #700).
 *
 * <p>
 * Its run id used to be set only after the run had ended, so the subagent list offered no
 * transcript for a running child, and its recorded execution holds none until then anyway. The run id
 * is now set at the run's start, and the transcript a chat control serves
 * ({@link ChatTranscripts#subagentTranscript}) is the child's live log while it works.
 * </p>
 */
public class TestChatSubagentLiveTranscript extends BasicTestCase {

	private static Person lena;

	private static TLObject lenasChat;

	private static Person other;

	private static TLObject othersChat;


	private SubagentJob _job;

	/** The window of the chat whose lead started the subagent. */
	private TaskProgressTracker _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (lena == null) {
			lena = TestPerson.createPerson("lena-with-a-worker");
			lenasChat = ChatHistoryService.createSession("Lenas Chat", lena);
			HistoryUtils.updateSessionRevision();
		}

		_window = new TaskProgressTracker();
		assertTrue(_window.getSessionManager().switchSession(chatId(), lena));
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		ChatSessionRegistry.register(subSession, null, _window);

		// A child as the spawner leaves it while it works: running, its run id taken from the run's
		// start, recording into its own log.
		SubagentJobRegistry registry = _window.getActiveSubagentRegistry();
		_job = registry.register("researcher", "find X", new CancellationToken());
		_job.markRunning();
		_job.setChildRunId(UUID.randomUUID().toString());
		ConversationLog log = new ConversationLog();
		log.startTurn().setUserParts(
			List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), "find X")));
		log.currentTurn().addAssistantPart(
			new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), "Searching the archive first."));
		_job.setLiveLog(log);
	}

	/** The panel offers the transcript of a child that is still running. */
	public void testARunningChildHasATranscript() throws Exception {
		// The list the panel shows comes in the window's run_state frame.
		String list = DtoMapper.toJson(RunStateFrame.of(_window, lena));

		assertTrue(list, list.contains("\"subagentId\":\"" + _job.subagentId() + "\""));
		assertTrue(list, list.contains("\"hasTranscript\":true"));
	}

	/** The transcript of a running child is what it has done so far. */
	public void testTheTranscriptOfARunningChildIsItsLiveLog() throws Exception {
		String transcript = transcript(_job.subagentId(), null);

		assertTrue(transcript, transcript.contains("Searching the archive first."));
		assertTrue(transcript, transcript.contains("find X"));
	}

	/**
	 * A subagent this window holds no job for any more - an earlier server life - is found by the run
	 * its recorded part names, so its card can still open its transcript (issue #702).
	 */
	public void testARecordedRunIsFoundByTheRunIdItsPartNames() throws Exception {
		String runId = recordedRun(lenasChat, "The recorded report.");

		String transcript = transcript("sa-77", runId);

		assertTrue(transcript, transcript.contains("The recorded report."));
	}

	/** Naming the run of another account's chat finds nothing. */
	public void testARunOfAnotherAccountIsNotFound() throws Exception {
		if (other == null) {
			other = TestPerson.createPerson("someone-else-with-a-worker");
			othersChat = ChatHistoryService.createSession("Other Chat", other);
		}
		String runId = recordedRun(othersChat, "Not Lena's.");

		assertNull("Not Lena's: found as nothing.", ChatTranscripts.subagentTranscript(_window, lena, "sa-77", runId));
	}

	/** A finished child run in the given chat, with a transcript holding the given answer. */
	private static String recordedRun(TLObject chat, String answer) {
		String runId = UUID.randomUUID().toString();
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentExecution execution = store.beginExecution((AgentSession) chat, runId, "researcher", null, null, null);
		ConversationLog log = new ConversationLog();
		log.startTurn().addAssistantPart(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), answer));
		log.completeTurn();
		store.finishExecution(execution, AgentExecutionStore.ExecutionStatus.COMPLETE, StopReason.COMPLETE, null,
			TokenUsage.EMPTY, ConversationLogSerializer.toJson(log));
		HistoryUtils.updateSessionRevision();
		return runId;
	}

	/** The transcript Lena's window reads of the given subagent, as JSON. */
	private String transcript(String subagentId, String runId) throws Exception {
		var transcript = ChatTranscripts.subagentTranscript(_window, lena, subagentId, runId);
		assertNotNull("The transcript must be found.", transcript);
		return DtoMapper.toJson(transcript);
	}

	private static String chatId() {
		return lenasChat.tHandle().getObjectName().toExternalForm();
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSubagentLiveTranscript.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
