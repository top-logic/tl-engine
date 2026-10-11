/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.TestSubagentMessageEndpoint.Recording;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
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
 * Its run id used to be set only after the run had ended, so {@code GET /chat/subagents} offered no
 * transcript for a running child, and its recorded execution holds none until then anyway. The run id
 * is now set at the run's start, and the transcript endpoint reads the child's live log while it
 * works.
 * </p>
 */
public class TestChatSubagentLiveTranscript extends BasicTestCase {

	private static Person lena;

	private static TLObject lenasChat;

	private static Person other;

	private static TLObject othersChat;

	private static ServletRunner runner;

	private TestSubagentMessageEndpoint.Probe _servlet;

	private SubagentJob _job;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (lena == null) {
			lena = TestPerson.createPerson("lena-with-a-worker");
			lenasChat = ChatHistoryService.createSession("Lenas Chat", lena);
			HistoryUtils.updateSessionRevision();
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new TestSubagentMessageEndpoint.Probe();

		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.getSessionManager().switchSession(chatId(), lena));
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		ChatSessionRegistry.register(subSession, null, window);

		// A child as the spawner leaves it while it works: running, its run id taken from the run's
		// start, recording into its own log.
		SubagentJobRegistry registry = window.getActiveSubagentRegistry();
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
		String list = get("/chat/subagents");

		assertTrue(list, list.contains("\"subagentId\":\"" + _job.subagentId() + "\""));
		assertTrue(list, list.contains("\"hasTranscript\":true"));
	}

	/** The transcript of a running child is what it has done so far. */
	public void testTheTranscriptOfARunningChildIsItsLiveLog() throws Exception {
		String transcript = get("/chat/subagents/" + _job.subagentId() + "/transcript");

		assertTrue(transcript, transcript.contains("Searching the archive first."));
		assertTrue(transcript, transcript.contains("find X"));
	}

	/**
	 * A subagent this window holds no job for any more - an earlier server life - is found by the run
	 * its recorded part names, so its card can still open its transcript (issue #702).
	 */
	public void testARecordedRunIsFoundByTheRunIdItsPartNames() throws Exception {
		String runId = recordedRun(lenasChat, "The recorded report.");

		String transcript = get("/chat/subagents/sa-77/transcript?runId=" + runId);

		assertTrue(transcript, transcript.contains("The recorded report."));
	}

	/** Naming the run of another account's chat finds nothing. */
	public void testARunOfAnotherAccountIsNotFound() throws Exception {
		if (other == null) {
			other = TestPerson.createPerson("someone-else-with-a-worker");
			othersChat = ChatHistoryService.createSession("Other Chat", other);
		}
		String runId = recordedRun(othersChat, "Not Lena's.");

		Recording response = request("/chat/subagents/sa-77/transcript?runId=" + runId);

		assertEquals(404, response._status);
		assertFalse(response._body.toString().contains("Not Lena's."));
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

	private String get(String path) throws Exception {
		Recording response = request(path);
		assertEquals(response._body.toString(), 200, response._status);
		return response._body.toString();
	}

	private Recording request(String path) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(
			new GetMethodWebRequest("http://localhost" + path));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), lena);
		} finally {
			ThreadContext.popSuperUser();
		}
		_servlet.get(request, response.asResponse());
		return response;
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
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
