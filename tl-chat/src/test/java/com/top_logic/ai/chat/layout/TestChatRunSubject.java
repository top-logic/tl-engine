/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.ChatRunOptions;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentFactory;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.approval.ApprovalMode;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ReadFilter;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A chat run is about the subject of its chat session - what {@code <chat-conversation subject>}
 * wrote onto the session when the conversation began - if the user may read it.
 *
 * <p>
 * The run goes the whole way to a scripted model, which records the system prompt it is sent. The
 * subjects are conversations; under the access policy shipped for conversations, only their owner
 * may read them.
 * </p>
 */
public class TestChatRunSubject extends BasicTestCase {

	/** The agent of the test application that runs on the scripted model. */
	private static final String AGENT = "TestChatPlanner";

	private static final long TIMEOUT_MILLIS = 60_000;

	private static Person user;

	private static Person other;

	/** The system prompts the scripted model was sent, in order. */
	private final List<String> _systemPrompts = new CopyOnWriteArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("chat-subject-user");
			other = TestPerson.createPerson("chat-subject-other");
		}
		ScriptedModel.install((request, turn) -> {
			_systemPrompts.add(request.systemPrompt());
			return List.<ContentPart> of(new TextContent("Done."));
		});
	}

	/** The model reads the subject of the chat in the environment block of the run. */
	public void testSubjectOfTheChatReachesTheModel() throws Exception {
		AgentSession subject = conversation(user);

		send(subject);

		assertTrue("The environment block names the subject: " + _systemPrompts.get(0),
			_systemPrompts.get(0).contains("- " + AgentFactory.SUBJECT_FACT + ": " + ReadFilter.identityJson(subject)));
	}

	/** A chat about an object the user may not read - an old link, say - tells the model nothing of it. */
	public void testUnreadableSubjectDoesNotReachTheModel() throws Exception {
		send(conversation(other));

		assertFalse("The environment block names a subject the user may not read: " + _systemPrompts.get(0),
			_systemPrompts.get(0).contains("- " + AgentFactory.SUBJECT_FACT + ":"));
	}

	/** Sends one message, as the chat of a window does, into a new chat about the given subject. */
	private void send(TLObject subject) throws Exception {
		TLContextManager.inPersonContext(user, () -> {
			TLObject chat = ChatHistoryService.createSession("About it", user, ApprovalMode.AUTO_APPROVE, subject);
			HistoryUtils.updateSessionRevision();
			TaskProgressTracker tracker = new TaskProgressTracker();
			tracker.claim(user);
			assertTrue(tracker.setActiveSessionId(chat.tHandle().getObjectName().toExternalForm(), user));
			try {
				ChatProcessor.sendMessage(tracker, TLContextManager.getSubSession(), user.getName(),
					"Which tickets are still open here?", null, new ChatRunOptions(AGENT, null, false, List.of()));
				long end = System.currentTimeMillis() + TIMEOUT_MILLIS;
				while (ChatProcessor.isProcessing(tracker)) {
					assertTrue("The run did not end in time.", System.currentTimeMillis() < end);
					Thread.sleep(50);
				}
			} catch (InterruptedException ex) {
				throw new AssertionError(ex);
			} finally {
				tracker.close();
			}
		});
		assertFalse("The model was not asked.", _systemPrompts.isEmpty());
	}

	/** A conversation of the given account, as a subject of another chat. */
	private static AgentSession conversation(Person owner) {
		AgentSession session =
			AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, owner, "A conversation as subject");
		HistoryUtils.updateSessionRevision();
		return session;
	}

	/** The services of a chat run on the scripted model, and of the access policy for conversations. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatRunSubject.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
