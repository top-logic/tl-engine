/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.eval;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.TransactionScope;
import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.limits.RunLimitExceededException;
import com.top_logic.ai.core.limits.RunLimitSettings;
import com.top_logic.ai.core.limits.RunLimits;
import com.top_logic.ai.core.limits.RunLimitsConfig;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.ai.core.provider.ScriptedProvider;
import com.top_logic.ai.core.scripting.AIScriptFunctions;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.RunEntryPoint;
import com.top_logic.ai.core.security.RunPrincipal;
import com.top_logic.ai.core.security.RunSecurity;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * That the TL-Script function {@code aiChat} is a run under the limits of the acting account, as
 * {@code aiAgentChat} is.
 *
 * <p>
 * Each case compares the two functions for the same account at the same limit. The model is the
 * scripted model of this module, so that no request leaves the test.
 * </p>
 */
public class TestAiChatRunLimits extends BasicTestCase {

	/** The agent of this module that runs on the scripted model. */
	private static final String AGENT = "Eval_Scripted";

	/** A tool of this module, so that a call of {@code aiChat} gets more than one turn. */
	private static final String TOOL = "model-instances.listInstances";

	private static final String ANSWER = "The scripted answer.";

	/** What one turn of the model costs in the budget test. */
	private static final int TOKENS_PER_TURN = 200;

	/** Makes each account name unique in the suite. */
	private static int _accounts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RunLimits.resetForTest();
		ScriptedProvider.install((request, turn) -> Reply.text(ANSWER));
	}

	@Override
	protected void tearDown() throws Exception {
		RunLimits.resetForTest();
		super.tearDown();
	}

	/** Without a configured limit, {@code aiChat} runs as before. */
	public void testNoLimitChangesNothing() {
		Person account = freshAccount();

		assertEquals(ANSWER, asAccount(account, () -> aiChat(null)));
		assertEquals(1, ScriptedProvider.requests().size());
	}

	/**
	 * An account at its limit of concurrent runs: {@code aiAgentChat} is refused, and so must be
	 * {@code aiChat}, before the model is called.
	 */
	public void testTheConcurrencyLimitRefusesAiChat() {
		Person account = freshAccount();
		try (RunLimitSettings.Overridden limits = RunLimitSettings.override(cap(1));
				RunLimits.Admission running = RunLimits.admit(runOf(account))) {
			assertRefused("aiAgentChat", () -> asAccount(account, () -> aiAgentChat()));
			assertRefused("aiChat", () -> asAccount(account, () -> aiChat(null)));
			assertEquals("No request may reach the model.", 0, ScriptedProvider.requests().size());
			assertEquals("A refused call holds no slot.", 1, RunLimits.runningFor(account));
		}
	}

	/**
	 * An account that has spent its token budget: {@code aiAgentChat} is refused, and so must be
	 * {@code aiChat}, before the model is called.
	 */
	public void testASpentBudgetRefusesAiChat() {
		Person account = freshAccount();
		recordSpend(account, 5000);
		try (RunLimitSettings.Overridden limits = RunLimitSettings.override(tokenBudget(1000))) {
			assertRefused("aiAgentChat", () -> asAccount(account, () -> aiAgentChat()));
			assertRefused("aiChat", () -> asAccount(account, () -> aiChat(null)));
			assertEquals("No request may reach the model.", 0, ScriptedProvider.requests().size());
		}
	}

	/**
	 * A call of {@code aiChat} that spends the rest of the budget stops after the turn that spent it,
	 * with {@link StopReason#BUDGET_EXHAUSTED}, as an agent run does.
	 */
	public void testAiChatStopsWhenItSpendsTheBudget() {
		Person account = freshAccount();
		ScriptedProvider.install((request, turn) -> turn < 5
			? Reply.of(new ToolCall("call-" + UUID.randomUUID(), "listInstances", "{}"))
				.withUsage(new TokenUsage(TOKENS_PER_TURN, 0, 0, 0, 0))
			: Reply.text(ANSWER));
		try (RunLimitSettings.Overridden limits =
			RunLimitSettings.override(tokenBudget(2 * TOKENS_PER_TURN - 1))) {
			try {
				String answer = asAccount(account, () -> aiChat(List.of(TOOL)));
				fail("aiChat ran past the budget of the account and answered '" + answer + "' after "
					+ ScriptedProvider.requests().size() + " requests.");
			} catch (TopLogicException expected) {
				assertTrue("The failure names the reason: " + expected.getErrorKey(),
					List.of(expected.getErrorKey().arguments()).contains(StopReason.BUDGET_EXHAUSTED.name()));
			}
		}
		assertEquals("The turn that spent the rest is the last one.", 2, ScriptedProvider.requests().size());
	}

	private static void assertRefused(String function, Runnable call) {
		try {
			call.run();
			fail(function + " ran although the account is at its limit. Requests that reached the model: "
				+ ScriptedProvider.requests().size());
		} catch (RunLimitExceededException expected) {
			assertNotNull("The user is told why.", expected.reason());
		}
	}

	private static String aiChat(List<String> tools) {
		return AIScriptFunctions.chat(List.of("Hello"), null, null, tools);
	}

	private static String aiAgentChat() {
		return AIScriptFunctions.agentChat(AGENT, "Hello");
	}

	/** Calls the function as the given account, as a script of that account calls it. */
	private static String asAccount(Person account, Supplier<String> call) {
		return TLContextManager.inPersonContext(account, call::get);
	}

	/** A run of the given account that holds a slot while the test calls the functions. */
	private static ToolContext runOf(Person account) {
		return new ToolContext(RunSecurity.contextFor(new RunPrincipal(account, RunEntryPoint.SCRIPT, false)),
			ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP,
			CancellationToken.UNCANCELLABLE);
	}

	private static Person freshAccount() {
		return TestPerson.createPerson("ai-chat-run-limits-" + (++_accounts));
	}

	/** Records a finished run of the account that spent the given number of tokens. */
	private static void recordSpend(Person account, int tokens) {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, account, "earlier run");
		AgentExecution execution =
			store.beginExecution(session, "run-" + UUID.randomUUID(), AGENT, "eval-scripted", null, null);
		store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null,
			new TokenUsage(tokens, 0, 0, 0, 0), null, null);
		HistoryUtils.updateSessionRevision();
	}

	private static RunLimitsConfig cap(int runs) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.CONCURRENT_RUNS_PER_ACCOUNT),
			Integer.valueOf(runs));
		return config;
	}

	private static RunLimitsConfig tokenBudget(long tokens) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(tokens));
		return config;
	}

	/** The services that a call of {@code aiChat} and of {@code aiAgentChat} passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAiChatRunLimits.class),
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
