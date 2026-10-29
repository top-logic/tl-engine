/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.subagent.SubagentWorkers;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.tools.web.WebSearchHit;
import com.top_logic.tl.ai.core.tools.web.WebToolsService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A research question of {@code research-scenarios.config.xml} through the real runner and the real
 * graders, on a scripted model, a scripted judge and the {@link FixtureWeb} - in every build, at no
 * cost (#755).
 *
 * <p>
 * The shipped scenario {@code fact-nobel-phd} on an arm that runs the shipped Albert on
 * {@code eval-scripted}: a run that does what the oracle says passes, and each near-miss the
 * expectations exist for fails on the expectation that is about it - the lead reading a page itself,
 * a link to a page nobody read, a criterion the judge finds unmet, a judge reply nobody can read.
 * </p>
 */
public class TestResearchPlumbing extends BasicTestCase {

	private static final String SCENARIO = "fact-nobel-phd";

	private static final String PAGE = "https://nobel.test/hinton";

	private static final String QUERY = "Geoffrey Hinton PhD university";

	private static final String FACT = "Geoffrey Hinton earned his PhD at the University of Edinburgh in 1978.";

	private static final Judge SCRIPTED_JUDGE = new Judge("eval-scripted", "scripted");

	/** What the scripted lead does wrong, if anything. */
	private enum Slip {
		NONE, READS_ITSELF, LINKS_UNREAD_PAGE
	}

	private Slip _slip = Slip.NONE;

	private String _judgeReply;

	/**
	 * Runs each test as the root account, the account the runs act for: the tools authorize against
	 * the account bound to the thread and refuse to run where the security checks are bypassed.
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(PersonManager.getManager().getRoot(),
			() -> {
				super.runTest();
				return null;
			});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		FixtureWeb.search(QUERY, new WebSearchHit("Geoffrey Hinton - biography", PAGE, FACT, ""));
		FixtureWeb.page(PAGE, "Geoffrey Hinton - biography", "# Geoffrey Hinton\n\n" + FACT
			+ " He was born in 1947, John Hopfield in 1933.");
		_judgeReply = met(true, true, true);
		ScriptedProvider.install(this::reply);
	}

	@Override
	protected void tearDown() throws Exception {
		FixtureWeb.clear();
		super.tearDown();
	}

	/** The run the oracle describes passes every expectation. */
	public void testTheOracleRunPasses() throws Exception {
		RunRecord row = runScenario();

		assertEquals(row.verdicts().toString(), RunOutcome.PASS, row.outcome());
		assertEquals("judge eval-scripted: 3/3 criteria met", verdict(row, "correct").detail());
		assertEquals("1 sources from 1 hosts, 0 unread links", verdict(row, "cites-a-read-page").detail());
		assertEquals("The row counts the worker: one run, its search and its read.", 1, row.subagents().runs());
		assertEquals(2, row.subagents().toolCalls());
		assertTrue(row.subagents().inputTokens() > 0);
	}

	/** An arm whose worker runs on a model this application does not know is not started. */
	public void testAnArmWithAnUnknownWorkerModelIsNotStarted() throws Exception {
		EvalConfig.Arm arm = (EvalConfig.Arm) TypedConfiguration.parse("arm", EvalConfig.Arm.class,
			CharacterContents.newContent("<arm name='mixed' agent='TL_SingleAgent' model='eval-scripted'>"
				+ "<agent-models><agent-model agent='TL_ResearchWorker' model='no-such-model'/></agent-models>"
				+ "</arm>", "arm.xml"));

		String reason = EvalRunner.whyUnusable(arm);

		assertNotNull(reason);
		assertTrue(reason, reason.contains("'no-such-model' of agent 'TL_ResearchWorker'"));
		assertNull("The same arm with a known worker model runs.",
			EvalRunner.whyUnusable(Arms.of("plain", "TL_SingleAgent", "eval-scripted")));
	}

	/** A lead that reads the page itself fails the research shape, whatever its report says. */
	public void testALeadThatReadsFails() throws Exception {
		_slip = Slip.READS_ITSELF;

		RunRecord row = runScenario();

		assertEquals(RunOutcome.FAIL, row.outcome());
		Verdict shape = verdict(row, "research-shape");
		assertFalse(shape.passed());
		assertTrue(shape.detail(), shape.detail().contains("called the forbidden [webFetch]"));
	}

	/** A report that links a page nobody read fails the citation check. */
	public void testALinkToAnUnreadPageFails() throws Exception {
		_slip = Slip.LINKS_UNREAD_PAGE;

		RunRecord row = runScenario();

		Verdict citations = verdict(row, "cites-a-read-page");
		assertFalse(citations.passed());
		assertTrue(citations.detail(), citations.detail().contains("https://invented.test/cv"));
	}

	/** A criterion the judge finds unmet fails the judge expectation, naming the criterion. */
	public void testAnUnmetCriterionFails() throws Exception {
		_judgeReply = met(true, true, false);

		Verdict judge = verdict(runScenario(), "correct");

		assertFalse(judge.passed());
		assertTrue(judge.detail(), judge.detail().contains("2/3 criteria met; not met: Names the year 1978."));
	}

	/** A judge reply that is not the agreed JSON fails rather than passes. */
	public void testAnUnreadableJudgeFails() throws Exception {
		_judgeReply = "All criteria are met.";

		Verdict judge = verdict(runScenario(), "correct");

		assertFalse(judge.passed());
		assertTrue(judge.detail(), judge.detail().contains("not the expected JSON"));
	}

	private RunRecord runScenario() throws Exception {
		EvalScenario scenario = TestResearchEval.researchScenarios().getScenarios().stream()
			.filter(s -> SCENARIO.equals(s.getId())).findFirst().orElseThrow();
		Person root = PersonManager.getManager().getRoot();
		return EvalRunner.run(scenario, Arms.of("scripted", "TL_SingleAgent", "eval-scripted"), 1, root,
			SCRIPTED_JUDGE);
	}

	/** Albert, its worker and the judge, told apart by their system prompts. */
	private List<ContentPart> reply(ProviderRequest request, int turn) {
		String prompt = String.valueOf(request.systemPrompt());
		if (prompt.contains("You grade the answer of a research assistant")) {
			return List.of(new TextContent(_judgeReply));
		}
		if (prompt.startsWith("You are a research worker.")) {
			switch (turn) {
				case 0:
					return List.of(call("webSearch", "{\"query\": \"" + QUERY + "\"}"));
				case 1:
					return List.of(call("webFetch", "{\"url\": \"" + PAGE + "\"}"));
				default:
					return List.of(new TextContent("## Answer\n" + FACT + "\n\n## Findings\n- " + FACT
						+ " - [Geoffrey Hinton - biography](" + PAGE + ")\n\n## Conflicts and gaps\n- None."));
			}
		}
		List<ContentPart> lead = new ArrayList<>();
		switch (turn) {
			case 0:
				lead.add(call("loadSkill", "{\"skillId\": \"TL_deepResearch\"}"));
				if (_slip == Slip.READS_ITSELF) {
					lead.add(call("loadSkill", "{\"skillId\": \"TL_webAccess\"}"));
				}
				return lead;
			case 1:
				if (_slip == Slip.READS_ITSELF) {
					lead.add(call("webFetch", "{\"url\": \"" + PAGE + "\"}"));
				}
				lead.add(call("spawn_subagent", "{\"agent\": \"TL_ResearchWorker\", \"task\": "
					+ "\"Sub-question: Where and when did the younger 2024 physics laureate earn his PhD?\"}"));
				return lead;
			case 2:
				return List.of(call("wait_for_subagent", "{\"timeoutMs\": 120000}"));
			default:
				String report = "# The younger laureate's doctorate\n\nThe younger laureate, Geoffrey Hinton, "
					+ "[earned his PhD at the University of Edinburgh in 1978](" + PAGE + ").";
				if (_slip == Slip.LINKS_UNREAD_PAGE) {
					report += " See also [his CV](https://invented.test/cv).";
				}
				return List.of(new TextContent(report));
		}
	}

	private static String met(boolean... met) {
		StringBuilder result = new StringBuilder("{\"criteria\": [");
		for (int n = 0; n < met.length; n++) {
			result.append(n == 0 ? "" : ", ").append("{\"met\": ").append(met[n]).append(", \"reason\": \"r")
				.append(n).append("\"}");
		}
		return result.append("]}").toString();
	}

	private static Verdict verdict(RunRecord row, String label) {
		return row.verdicts().stream().filter(v -> label.equals(v.label())).findFirst()
			.orElseThrow(() -> new AssertionError("No verdict '" + label + "' in " + row.verdicts()));
	}

	private static ToolCall call(String tool, String arguments) {
		return new ToolCall("call-" + UUID.randomUUID(), tool, arguments);
	}

	/**
	 * The suite, on the services a research run passes through.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestResearchPlumbing.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				WebToolsService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				SubagentWorkers.Module.INSTANCE));
	}
}
