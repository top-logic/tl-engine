/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A chat run acts for the person who sent its message in a browser window, on the thread of the run
 * and until the run ends. A run whose message no present person sent - the run of a scheduled task -
 * acts for nobody.
 */
public class TestChatRunActingPerson extends BasicTestCase {

	private static final String AGENT = "TestChatPlanner";

	private static final long TIMEOUT_SECONDS = 60;

	private Person _owner;

	private Person _previousPerson;

	private String _chat;

	/** What the run saw as {@link ActingPerson#handOver()} when it asked the model. */
	private final AtomicReference<Optional<Person>> _seen = new AtomicReference<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_chat = ChatHistoryService.createSession("Acting person", _owner).tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();

		ScriptedModel.install((request, turn) -> {
			_seen.set(Optional.ofNullable(ActingPerson.handOver()));
			return List.<ContentPart> of(new TextContent("Done."));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/** A message the person sent in the window: the run acts for the person. */
	public void testRunActsForThePersonWhoSent() throws Exception {
		ChatProcessor.AIProcessingState run;
		try (ThreadLocalBinding<Person>.Handle acting = ActingPerson.bind(_owner)) {
			run = send();
		}

		assertSame(_owner, run.getActingPerson());
		assertEquals("The run did not act for the person who sent.", Optional.of(_owner), _seen.get());
	}

	/** A message that no present person sent, as a scheduled task sends one: the run acts for nobody. */
	public void testRunWithoutPresentPersonActsForNobody() throws Exception {
		ChatProcessor.AIProcessingState run = send();

		assertNull(run.getActingPerson());
		assertEquals("The run acted for a person nobody had present.", Optional.empty(), _seen.get());
	}

	private ChatProcessor.AIProcessingState send() throws Exception {
		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.setActiveSessionId(_chat, _owner));
		ChatProcessor.SendResult sent = ChatProcessor.sendMessage(window, TLContextManager.getSubSession(),
			_owner.getName(), "Who am I acting for?", null,
			new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
		assertNotNull("The send was refused.", sent.getTaskId());
		ChatProcessor.AIProcessingState run = window.getProcessingState(sent.getTaskId());
		run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		assertNull("The run failed: " + run.getError(), run.getError());
		HistoryUtils.updateSessionRevision();
		return run;
	}

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatRunActingPerson.class),
				ModelService.Module.INSTANCE,
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
