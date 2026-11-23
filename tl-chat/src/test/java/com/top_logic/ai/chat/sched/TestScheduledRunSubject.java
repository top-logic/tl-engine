/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.sched;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ScriptedModel;
import com.top_logic.ai.chat.sched.TestScheduledAgentRun.ProbeTask;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ReadFilter;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * The {@code subject-expression} of an {@link AgentTask}: the run is about the object it computes,
 * if the {@code run-as} account may read it.
 *
 * <p>
 * The run goes the whole way through the chat to a scripted model, which records the system prompt
 * it is sent. The subjects are conversations; under the access policy shipped for conversations,
 * only their owner may read them.
 * </p>
 */
public class TestScheduledRunSubject extends BasicTestCase {

	/** The agent of the test application that runs on the scripted model. */
	private static final String AGENT = "TestChatPlanner";

	private static Person account;

	private static Person other;

	/** The system prompts the scripted model was sent, in order. */
	private final List<String> _systemPrompts = new CopyOnWriteArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (account == null) {
			account = TestPerson.createPerson("subject-task-account");
			other = TestPerson.createPerson("subject-task-other");
		}
		ScriptedModel.install((request, turn) -> {
			_systemPrompts.add(request.systemPrompt());
			return List.<ContentPart> of(new TextContent("Done."));
		});
	}

	/** The run is about the object the expression computes: the model reads it, and the session is about it. */
	public void testReadableSubjectReachesTheRunAndTheSession() throws Exception {
		AgentSession subject = conversation(account, "readable-task-subject");

		ProbeTask task = task("readable-task-subject");
		task.drive();

		assertFalse("The model was not asked.", _systemPrompts.isEmpty());
		assertTrue("The environment block names the subject: " + _systemPrompts.get(0),
			_systemPrompts.get(0).contains("- Subject: " + ReadFilter.identityJson(subject)));
		assertEquals("The recorded session is about the subject.", subject, recordedSession().getSubject());
	}

	/** A subject the run-as account may not read reaches neither the model nor the session. */
	public void testUnreadableSubjectIsNamedNowhere() throws Exception {
		conversation(other, "unreadable-task-subject");

		ProbeTask task = task("unreadable-task-subject");
		task.drive();

		assertFalse("The model was not asked.", _systemPrompts.isEmpty());
		assertFalse("The environment block names a subject the account may not read: " + _systemPrompts.get(0),
			_systemPrompts.get(0).contains("- Subject:"));
		assertNull("The session is about a subject the account may not read.", recordedSession().getSubject());
	}

	/** An expression that computes several objects names no subject, and the task ends with an error. */
	public void testSeveralObjectsAreAnError() throws Exception {
		conversation(account, "twice-task-subject");
		conversation(account, "twice-task-subject");

		ProbeTask task = task("twice-task-subject");
		task.drive();

		assertEquals(ResultType.ERROR, task.getLog().getLastResult().getResultType());
		assertTrue("Nothing is sent for a task whose subject is not one object.", _systemPrompts.isEmpty());
	}

	/** A conversation of the given account with the given title, which the task's expression finds. */
	private static AgentSession conversation(Person owner, String title) {
		AgentSession session = AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, owner, title);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	/** A task of {@link #AGENT}, run as {@link #account}, about the conversations with the given title. */
	private static ProbeTask task(String subjectTitle) throws Exception {
		String xml = "<task xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + AgentTask.Config.class.getName() + "'"
			+ " name='AboutOne' agent='" + AGENT + "' run-as='" + account.getName() + "'>"
			+ "<prompt>Summarise this.</prompt>"
			+ "<subject-expression>all(`tl.ai.execution:AgentSession`)"
			+ ".filter(s -> $s.get(`tl.ai.execution:AgentSession#title`) == '" + subjectTitle + "')"
			+ "</subject-expression></task>";
		AgentTask.Config config =
			(AgentTask.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		return new ProbeTask(new DefaultInstantiationContext(TestScheduledRunSubject.class), config);
	}

	/** The session the last run of a task was recorded in: the newest of {@link #account}. */
	private static AgentSession recordedSession() {
		HistoryUtils.updateSessionRevision();
		TLObject newest = ChatHistoryService.getSessionsForUser(account).get(0);
		assertEquals("AboutOne", newest.tValueByName("title"));
		return (AgentSession) newest;
	}

	/** The services of a chat run on the scripted model, and of the access policy for conversations. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScheduledRunSubject.class),
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
