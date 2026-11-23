/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.ai.core.agent.subagent.SubagentWorkers;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.ExecutionRecorder;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.ai.core.provider.ScriptedProvider;
import com.top_logic.ai.core.scripting.AIScriptFunctions;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * An {@code aiAgentChatAsync} run and the detached subagent it spawns act in other systems for the
 * person who started the run in a browser window, after the request of the person has ended. A run
 * that no present person started acts for nobody.
 */
public class TestAsyncRunActingPerson extends BasicTestCase {

	private static final String LEAD = "Eval_Lead";

	private static final String WORKER = "Eval_Scripted";

	private static final String TASK = "Count the open tickets of project Alpha.";

	private static final String PROMPT = "Find out how many open tickets project Alpha has.";

	private static final long TIMEOUT_MILLIS = 60_000;

	/** The {@link ActingPerson} of each call of the model, by the lead and by its subagent. */
	private static final List<Optional<Person>> ACTING = Collections.synchronizedList(new ArrayList<>());

	private final List<String> _registryKeys = new ArrayList<>();

	private Person _owner;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ACTING.clear();
		ScriptedProvider.install(TestAsyncRunActingPerson::reply);
		// The sub-session of this thread is the window of the account: the run is derived from it.
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext window = TLContextManager.getSubSession();
		_previousPerson = window.getPerson();
		window.setPerson(_owner);
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		for (String key : _registryKeys) {
			SubagentRegistries.remove(key);
		}
		super.tearDown();
	}

	/** Started in a request of the person: the run and its subagent act for the person. */
	public void testRunOfAPresentPersonActsForThePerson() throws Exception {
		String handle;
		try (ThreadLocalBinding<Person>.Handle acting = ActingPerson.bind(_owner)) {
			handle = AIScriptFunctions.agentChatAsync(LEAD, PROMPT);
		}
		awaitCompletion(handle);

		assertEquals(Set.of(Optional.of(_owner)), Set.copyOf(ACTING));
	}

	/** Started by the account without a present person: the run and its subagent act for nobody. */
	public void testRunWithoutPresentPersonActsForNobody() throws Exception {
		String handle = AIScriptFunctions.agentChatAsync(LEAD, PROMPT);
		awaitCompletion(handle);

		assertEquals(Set.of(Optional.empty()), Set.copyOf(ACTING));
	}

	/**
	 * Waits until the run behind the handle has completed, after its lead heard back from its
	 * subagent.
	 */
	private void awaitCompletion(String handle) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (System.currentTimeMillis() < deadline) {
			HistoryUtils.updateSessionRevision();
			AgentSession session = AgentExecutionStore.getInstance().findSessionById(handle);
			if (session != null) {
				for (AgentExecution execution : AgentExecutionStore.getInstance().executionsOf(session)) {
					if (execution.getParentExecution() == null && execution.getEndedAt() != null) {
						_registryKeys.add(
							SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session)));
						assertEquals(execution.getErrorMessage(), ExecutionStatus.COMPLETE.name(),
							execution.getStatus().getName());
						assertEquals("The lead and its subagent asked the model.", 4, ACTING.size());
						return;
					}
				}
			}
			Thread.sleep(100);
		}
		fail("The run behind " + handle + " did not end within " + TIMEOUT_MILLIS + " ms.");
	}

	/** The lead spawns, waits, and reports; the subagent answers its task at once. */
	private static Reply reply(ProviderRequest request, int turn) {
		ACTING.add(Optional.ofNullable(ActingPerson.handOver()));
		if (String.valueOf(request.messages().get(0)).contains(TASK)) {
			return Reply.of(new TextContent("Project Alpha has 3 open tickets."));
		}
		switch (turn) {
			case 0:
				return Reply.of(new ToolCall("call-spawn", "spawn_subagent",
					"{\"agent\": \"" + WORKER + "\", \"task\": \"" + TASK + "\"}"));
			case 1:
				return Reply.of(new ToolCall("call-wait", "wait_for_subagent", "{}"));
			default:
				return Reply.of(new TextContent("The subagent reported back."));
		}
	}

	/**
	 * The suite, on the services a run passes through and the scheduler that carries the background
	 * run.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAsyncRunActingPerson.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				SubagentWorkers.Module.INSTANCE));
	}
}
