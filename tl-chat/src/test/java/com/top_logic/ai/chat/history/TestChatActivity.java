/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionType;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * What the chat list says about a chat the user is not looking at: {@code chatActivity} answers
 * whether something there waits for them, a subagent finished since they last had the chat open, or
 * an agent still works there.
 */
public class TestChatActivity extends BasicTestCase {

	private static Person owner;

	private static Person neighbour;

	private Person _previousUser;

	private AgentSession _chat;

	private AgentExecution _lead;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("lea-away-from-her-chat");
			neighbour = TestPerson.createPerson("tom-from-next-door");
		}
		_previousUser = TLContext.getContext().getCurrentPersonWrapper();
		TLContext.getContext().setCurrentPerson(owner);

		_chat = (AgentSession) ChatHistoryService.createSession("Anmeldungen", owner);
		_lead = store().beginExecution(_chat, "lead-" + UUID.randomUUID(), "Albert", "model", null, null);
		finish(_lead);
	}

	@Override
	protected void tearDown() throws Exception {
		TLContext.getContext().setCurrentPerson(_previousUser);
		super.tearDown();
	}

	/** A chat where nothing goes on says nothing. */
	public void testAQuietChatSaysNothing() {
		assertNull(ChatScriptFunctions.activity(_chat));
		assertNull("Not a chat.", ChatScriptFunctions.activity("Anmeldungen"));
	}

	/** A subagent at work, after the lead's turn has ended. */
	public void testAWorkingSubagentSaysRunning() {
		child();

		assertEquals(ChatScriptFunctions.RUNNING, ChatScriptFunctions.activity(_chat));
	}

	/** A subagent's question to the user comes first - and only for the user it is addressed to. */
	public void testAQuestionForTheUserSaysWaiting() {
		AgentExecution child = child();
		store().createInteraction(child, InteractionType.QUESTION, "q-1", "Frage", "Welche Veranstaltung?", "");
		HistoryUtils.updateSessionRevision();

		assertEquals(ChatScriptFunctions.WAITING, ChatScriptFunctions.activity(_chat));
		TLContext.getContext().setCurrentPerson(neighbour);
		assertEquals("Not waiting for somebody else.", ChatScriptFunctions.RUNNING,
			ChatScriptFunctions.activity(_chat));
	}

	/** A subagent that ended while the user was elsewhere is news, until the user opens the chat. */
	public void testAResultTheUserHasNotSeenIsNewsUntilTheChatIsOpened() throws Exception {
		ChatHistoryService.markSeen(_chat);
		AgentExecution child = child();
		Thread.sleep(5);
		finish(child);

		assertEquals(ChatScriptFunctions.NEWS, ChatScriptFunctions.activity(_chat));

		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.getSessionManager().switchSession(id(), owner));
		HistoryUtils.updateSessionRevision();
		assertNull("Opening the chat is seeing it.", ChatScriptFunctions.activity(_chat));
	}

	/** A result the user saw before is no news. */
	public void testAResultSeenAfterItEndedIsNoNews() throws Exception {
		AgentExecution child = child();
		finish(child);
		Thread.sleep(5);
		ChatHistoryService.markSeen(_chat);
		HistoryUtils.updateSessionRevision();

		assertNull(ChatScriptFunctions.activity(_chat));
	}

	/** Marking a chat seen keeps what else its metadata says - the approval mode above all (#658). */
	public void testSeeingTheChatKeepsItsMetadata() {
		ChatHistoryService.saveMetadata(_chat,
			"{\"approvedTools\":[\"query\"],\"lastModel\":\"m\",\"approvalMode\":\"AUTO_APPROVE\"}");
		ChatHistoryService.markSeen(_chat);
		HistoryUtils.updateSessionRevision();

		String metadata = ChatHistoryService.getMetadataJson(_chat);
		assertTrue(metadata, metadata.contains("\"approvedTools\":[\"query\"]"));
		assertTrue(metadata, metadata.contains("\"lastModel\":\"m\""));
		assertTrue(metadata, metadata.contains("\"approvalMode\":\"AUTO_APPROVE\""));
		assertTrue(ChatHistoryService.lastSeen(_chat) > 0);
	}

	private AgentExecution child() {
		AgentExecution child =
			store().beginExecution(_chat, "child-" + UUID.randomUUID(), "Counter", "model", null, _lead);
		HistoryUtils.updateSessionRevision();
		return child;
	}

	private static void finish(AgentExecution execution) {
		store().finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, TokenUsage.EMPTY,
			null, null);
		HistoryUtils.updateSessionRevision();
	}

	private String id() {
		return ((TLObject) _chat).tHandle().getObjectName().toExternalForm();
	}

	private static AgentExecutionStore store() {
		return AgentExecutionStore.getInstance();
	}

	/** The services the chat history needs. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatActivity.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
