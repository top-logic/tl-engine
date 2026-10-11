/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.research.ResearchRuns;
import com.top_logic.tl.ai.core.scripting.AIScriptFunctions;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.tools.web.WebSearchHit;
import com.top_logic.tl.ai.core.tools.web.WebToolsService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.model.ModelService;

/**
 * {@code aiResearch}: a research run of Albert's {@code TL_deepResearch} skill without a chat window
 * (#767), started the way a scheduled {@code ScriptTask} starts it - in a system context, naming the
 * owner - and run on a scripted model and the {@link FixtureWeb}.
 *
 * <p>
 * What the model answers is scripted; the rest is the real thing: the background thread, the owner
 * the run acts as, the skill, the worker it spawns, the citation check of the report and the chat
 * session the report is recorded in.
 * </p>
 */
public class TestHeadlessResearch extends BasicTestCase {

	private static final String WORKER = "TL_ResearchWorker";

	private static final String QUESTION = "Which heat pump subsidy does Austria pay in 2026?";

	private static final String URL = "https://kesseltausch.at.test/heat-pumps";

	private static final String FACT = "Austria pays up to 7500 euros for a heat pump.";

	private static final String QUERY = "heat pump subsidy Austria 2026";

	/** The report the lead keeps and answers with. */
	private static final String REPORT = "# Austria\n[" + FACT + "](" + URL + ")\n";

	private static final long TIMEOUT_MILLIS = 60_000;

	/** What the lead was told in its first request. */
	private static final List<String> LEAD_PROMPTS = Collections.synchronizedList(new ArrayList<>());

	/** How often the model was called at all. */
	private static final AtomicInteger CALLS = new AtomicInteger();

	private static int _accounts;

	private final List<String> _registryKeys = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		LEAD_PROMPTS.clear();
		CALLS.set(0);
		FixtureWeb.search(QUERY, new WebSearchHit("Austria subsidy page", URL, FACT, ""));
		FixtureWeb.page(URL, "Austria subsidy page", "# Subsidies\n\n" + FACT);
		ScriptedProvider.install(TestHeadlessResearch::reply);
	}

	@Override
	protected void tearDown() throws Exception {
		FixtureWeb.clear();
		for (String key : _registryKeys) {
			SubagentRegistries.remove(key);
		}
		super.tearDown();
	}

	/**
	 * Started without an account, for a named owner: the lead researches with the skill, the report
	 * with its source lands in a chat of the owner, and the run is kept for the owner (#794).
	 */
	public void testAScheduledResearchWritesTheReportIntoTheOwnersChats() throws Exception {
		Person root = PersonManager.getManager().getRoot();

		String handle = startInSystemContext(QUESTION, "quick", root.getName());
		AgentExecution run = awaitEnd(handle);

		AgentSession session = run.getSession();
		assertEquals(root, session.getOwner());
		assertEquals("A chat, so that it appears in the owner's chat list.",
			TlAiExecutionFactory.getCHATSessionKindClassifier(), session.getKind());
		assertTrue(session.getTitle(), session.getTitle().contains("Which heat pump subsidy"));

		assertEquals(run.getErrorMessage(), ExecutionStatus.COMPLETE.name(), run.getStatus().getName());
		assertEquals(1, LEAD_PROMPTS.size());
		assertTrue("The lead is told the skill and the effort: " + LEAD_PROMPTS,
			LEAD_PROMPTS.get(0).contains("TL_deepResearch") && LEAD_PROMPTS.get(0).contains("Effort: quick"));
		assertTrue("The lead is told that nobody can answer: " + LEAD_PROMPTS,
			LEAD_PROMPTS.get(0).contains("nobody can answer"));

		String report = run.getResponse();
		assertTrue("The report is the run's answer: " + report, report.startsWith("# Austria"));
		assertTrue("The link to the page the worker read becomes a footnote: " + report,
			report.contains(FACT + "[1]"));
		assertTrue("The footnote names the page, for the chat to show.",
			run.getTranscript().contains("\"citations\":[{\"index\":1") && run.getTranscript().contains(URL));
		assertTrue("The question is on record in the chat.", run.getTranscript().contains("Which heat pump"));
		assertEquals("The report is what the handle answers.", report, AIScriptFunctions.agentRunResult(handle));

		ResearchRun kept = keptRun(QUESTION);
		assertEquals("The run is kept for the owner, not for the system context it was started in.", root,
			kept.getOwner());
		assertEquals("quick", kept.getEffort());
		assertEquals(URL, kept.getCitedSources().get(0).getUrl());
	}

	/** The research run kept for the given question. */
	private static ResearchRun keptRun(String question) {
		HistoryUtils.updateSessionRevision();
		for (TLObject object : MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getResearchRunType(),
			TLObject.class)) {
			ResearchRun run = (ResearchRun) object;
			if (question.equals(run.getQuestion())) {
				return run;
			}
		}
		fail("No research run was kept for: " + question);
		return null;
	}

	/** A run of an account whose budget is spent asks no model and says why in its chat. */
	public void testASpentBudgetRefusesTheResearchAndSaysWhy() throws Exception {
		Person spent = freshAccount();
		recordSpend(spent, 5000);
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(1000));

		AgentExecution run;
		try (RunLimitSettings.Overridden budget = RunLimitSettings.override(config)) {
			run = awaitEnd(startInSystemContext(QUESTION, null, spent.getName()));
		}

		assertEquals("No model was asked.", 0, CALLS.get());
		assertEquals(ExecutionStatus.REJECTED.name(), run.getStatus().getName());
		assertEquals(StopReason.BUDGET_EXHAUSTED.name(), run.getStopReason());
		assertNotNull(run.getResponse());
		assertTrue("The answer is the refusal, naming the window: " + run.getResponse(),
			run.getResponse().contains("24"));
		assertTrue("The question and the refusal are on record in the chat.",
			run.getTranscript().contains("Which heat pump") && run.getTranscript().contains("24"));
	}

	private String startInSystemContext(String question, String effort, String owner) throws Exception {
		return ThreadContextManager.inSystemInteraction(TestHeadlessResearch.class,
			() -> AIScriptFunctions.research(question, effort, owner));
	}

	/**
	 * Waits until the run behind the handle has ended, and returns its top-level execution.
	 *
	 * <p>
	 * Notes the run's subagent registry for {@link #tearDown()}: the registries belong to the process,
	 * and the next suite's fresh database hands out the same session ids.
	 * </p>
	 */
	private AgentExecution awaitEnd(String handle) throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (System.currentTimeMillis() < deadline) {
			HistoryUtils.updateSessionRevision();
			AgentSession session = AgentExecutionStore.getInstance().findSessionById(handle);
			if (session != null) {
				for (AgentExecution execution : AgentExecutionStore.getInstance().executionsOf(session)) {
					if (execution.getParentExecution() == null && execution.getEndedAt() != null) {
						_registryKeys.add(
							SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session)));
						return execution;
					}
				}
			}
			Thread.sleep(100);
		}
		fail("The research run behind " + handle + " did not end within " + TIMEOUT_MILLIS + " ms.");
		return null;
	}

	private static Person freshAccount() {
		return TestPerson.createPerson("headless-research-" + (++_accounts));
	}

	/** Records a finished run of the account that spent the given number of tokens. */
	private static void recordSpend(Person account, int tokens) {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, account, "earlier run");
		AgentExecution execution =
			store.beginExecution(session, "run-" + UUID.randomUUID(), "TL_SingleAgent", "test-model", null, null);
		store.finishExecution(execution, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null,
			new TokenUsage(tokens, 0, 0, 0, 0), null, null);
		HistoryUtils.updateSessionRevision();
	}

	/** The lead loads the skill, spawns one worker, waits, keeps the run and writes; the worker searches and reads. */
	private static List<ContentPart> reply(ProviderRequest request, int turn) {
		CALLS.incrementAndGet();
		String prompt = String.valueOf(request.systemPrompt());
		if (prompt.startsWith("You are a research worker.")) {
			switch (turn) {
				case 0:
					return List.of(call("webSearch", "{\"query\": \"" + QUERY + "\"}"));
				case 1:
					return List.of(call("webFetch", "{\"url\": \"" + URL + "\"}"));
				default:
					return List.of(new TextContent("## Answer\n" + FACT + "\n\n## Findings\n- " + FACT
						+ " - [Austria subsidy page](" + URL + ")"));
			}
		}
		switch (turn) {
			case 0:
				LEAD_PROMPTS.add(String.valueOf(request.messages().get(0)));
				return List.of(call("loadSkill", "{\"skillId\": \"TL_deepResearch\"}"));
			case 1:
				return List.of(new TextContent("Effort: quick. Plan: 1. Austria."),
					call("spawn_subagent", "{\"agent\": \"" + WORKER + "\", \"task\": "
						+ "\"Sub-question: What heat pump subsidy does Austria pay in 2026?\\n"
						+ "Purpose: the report.\\nScope: 2026, Austria.\\nBudget: at most 10 tool calls.\"}"));
			case 2:
				return List.of(call("wait_for_subagent", "{\"timeoutMs\": 60000}"));
			case 3:
				return List.of(call("saveReport", "{\"question\": \"" + QUESTION + "\", \"report\": \""
					+ REPORT.replace("\n", "\\n") + "\", \"effort\": \"quick\"}"));
			default:
				return List.of(new TextContent(REPORT));
		}
	}

	private static ToolCall call(String tool, String arguments) {
		return new ToolCall("call-" + UUID.randomUUID(), tool, arguments);
	}

	/**
	 * The suite, on the services a research run passes through, the web tools and the scheduler that
	 * carries the background run.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestHeadlessResearch.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				WebToolsService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ResearchRuns.Module.INSTANCE));
	}
}
