/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.ai.chat.TestChatServletOwnership.Recording;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.ChatServlet;
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
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
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

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

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
		_servlet = new TestChatServletOwnership.Probe();

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

	private String get(String path) throws Exception {
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
		assertEquals(response._body.toString(), 200, response._status);
		return response._body.toString();
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
