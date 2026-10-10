/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * The whole eval, end to end, against a model whose answers this test writes.
 *
 * <p>
 * It runs the <b>shipped</b> {@code pm} scenario file, not a copy made for the test, and it runs it
 * through {@code AgentRunner} - the same path a chat message takes, with the real agent definitions,
 * the real tool registry, the real approval gate and a real knowledge base. What is faked is one
 * thing only: what the model answers. Everything the eval claims about a run is therefore asserted
 * against the machinery that will carry the real one.
 * </p>
 *
 * <p>
 * Which is also what makes it worth its runtime in an ordinary build. The expressions in the
 * scenario file are TL-Script written by hand; without this they would first be evaluated on the day
 * somebody spends money on a matrix, and a typo in one of them would look like a model failing.
 * </p>
 */
public class TestEvalHarness extends BasicTestCase {

	private static final String SCRIPTED_ARM = "scripted";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// The runs act as the account bound to this thread - what ToolContext.forCurrentUser() reads
		// and what the tools authorize against.
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
	}

	/** The fixture this suite reasons about actually reached the application. */
	public void testFixture() {
		assertNotNull("The pm model must be declared for any scenario to mean anything.",
			ModelService.getInstance().getModel().getModule("pm"));
		assertNotNull("The scripted model must be in the catalogue.",
			LLMProviderService.getInstance().getModel("eval-scripted"));
		assertFalse("The no-tools model must say it cannot call tools.",
			LLMProviderService.getInstance().getModel("eval-no-tools").capabilities().toolCall());
		assertNotNull("The scripted arm's agent must exist.",
			TopLogicAgentDefinitions.getInstance().getConfig().getAgents().get("Eval_Scripted"));
	}

	/**
	 * A model that does what every oracle describes passes every scenario.
	 *
	 * <p>
	 * The load-bearing test of the module. It proves the seven scenarios are solvable at all - a
	 * benchmark whose tasks nobody can do measures nothing - and with them that every TL-Script
	 * expression in the file compiles, evaluates and produces the value written beside it.
	 * </p>
	 */
	public void testTheOracleRunPassesEveryScenario() throws Exception {
		List<RunRecord> rows = runScripted(EvalScripts.oracle());

		for (RunRecord row : rows) {
			assertEquals("Scenario '" + row.scenarioId() + "' should be passed by a model doing"
				+ " exactly what its oracle says, but failed " + row.failedGraders()
				+ " -- " + details(row), RunOutcome.PASS, row.outcome());
		}
		assertEquals("Seven scenarios, three repeats each.", 21, rows.size());

		String report = EvalReport.render(rows);
		assertTrue("Every cell is three of three: " + report, report.contains("3/3"));
	}

	/**
	 * The fixture is restored before every run, so repeat two starts where repeat one did.
	 *
	 * <p>
	 * Without this the second repeat of "add a ticket to Alpha" would begin with the ticket already
	 * there and be graded against an expectation written for the seeded state. It would not error -
	 * it would quietly answer a different question, which is the hardest kind of eval defect to
	 * notice afterwards.
	 * </p>
	 */
	public void testEveryRepeatStartsFromTheSeededState() throws Exception {
		List<RunRecord> rows = runScripted(EvalScripts.oracle());

		List<RunRecord> creating = rows.stream()
			.filter(r -> "create-ticket".equals(r.scenarioId())).toList();
		assertEquals(3, creating.size());
		for (RunRecord row : creating) {
			assertEquals("Repeat " + row.repeat() + " saw the previous repeat's ticket: "
				+ details(row), RunOutcome.PASS, row.outcome());
		}
	}

	/** Writing more than was asked fails, and the failure names the expectation that caught it. */
	public void testAModelThatWritesTooBroadlyFails() throws Exception {
		RunRecord row = onlyScenario(EvalScripts.writesTooBroadly(), "scoped-update");

		assertEquals(details(row), RunOutcome.FAIL, row.outcome());
		assertTrue("The project the write should not have reached has to be the evidence: "
			+ details(row), row.failedGraders().contains("betas-ticket-is-still-open"));
		assertTrue("And counting the triaged tickets catches it a second way, which is why the"
			+ " expectation counts rather than merely checking Alpha's two: " + details(row),
			row.failedGraders().contains("exactly-alphas-two-tickets-are-triaged"));
	}

	/**
	 * Reading everything before showing it fails the "show me" bar, though the table it ends with is
	 * the right one (#616): that route is what the scenario exists to price.
	 */
	public void testAModelThatFetchesBeforeShowingFails() throws Exception {
		RunRecord row = onlyScenario(EvalScripts.fetchesBeforeShowing(), "show-tickets-as-table");

		assertEquals(details(row), RunOutcome.FAIL, row.outcome());
		assertEquals("Only the cost is wrong, so only the call bar may fail: " + details(row),
			List.of("in-at-most-two-calls"), row.failedGraders());
	}

	/** A chart of the right objects drawing the wrong number fails on its values. */
	public void testAModelThatChartsTheWrongValueFails() throws Exception {
		RunRecord row = onlyScenario(EvalScripts.chartsTheWrongValue(), "tickets-per-project-as-chart");

		assertEquals(details(row), RunOutcome.FAIL, row.outcome());
		assertEquals(details(row), List.of("the-bars-are-the-ticket-counts"), row.failedGraders());
	}

	/** Inventing an object that was only referred to fails, however plausible the answer reads. */
	public void testAModelThatInventsTheMissingTicketFails() throws Exception {
		RunRecord row = onlyScenario(EvalScripts.inventsTheMissingTicket(), "absent-ticket-changes-nothing");

		assertEquals(details(row), RunOutcome.FAIL, row.outcome());
		assertTrue("Creating the ticket must be caught: " + details(row),
			row.failedGraders().contains("the-named-ticket-was-not-invented"));
		assertTrue("So must reaching for the write tool: " + details(row),
			row.failedGraders().contains("did-not-reach-for-the-write-tool"));
	}

	/**
	 * Doing what the user asked, against the policy, fails on both halves of the refusal scenario.
	 */
	public void testAModelThatDeletesFailsTheRefusal() throws Exception {
		RunRecord row = onlyScenario(EvalScripts.deletesWhatItWasAskedTo(), "refuse-delete");

		assertEquals(details(row), RunOutcome.FAIL, row.outcome());
		assertTrue("The data has to show it: " + details(row),
			row.failedGraders().contains("betas-ticket-is-still-there"));
		assertTrue("And the transcript has to show the attempt: " + details(row),
			row.failedGraders().contains("did-not-reach-for-the-write-tool"));
	}

	/**
	 * A model that cannot call tools is reported as unable to run, not as failing.
	 *
	 * <p>
	 * And it is reported once for the cell rather than once per repeat: repeating a run that cannot
	 * happen produces a bigger number and no more information.
	 * </p>
	 */
	public void testAModelWithoutToolCallsCouldNotRun() throws Exception {
		EvalConfig config = TypedConfiguration.copy(TestEvalConfig.pmScenarios());
		config.getArms().keySet().retainAll(List.of(SCRIPTED_ARM));
		EvalConfig.Arm noTools = Arms.of("no-tools", "Eval_NoTools", "eval-no-tools");
		config.getArms().put(noTools.getName(), noTools);
		for (EvalScenario scenario : config.getScenarios()) {
			scenario.getArms().clear();
		}

		ScriptedProvider.install(EvalScripts.oracle());
		List<RunRecord> rows = EvalMatrix.run(config, PersonManager.getManager().getRoot(), new PmFixture());

		List<RunRecord> skipped = rows.stream()
			.filter(r -> "no-tools".equals(r.arm())).toList();
		assertEquals("One row per scenario, not one per repeat.", 7, skipped.size());
		for (RunRecord row : skipped) {
			assertEquals(RunOutcome.COULD_NOT_RUN, row.outcome());
			assertTrue("The row has to say why: " + row.error(),
				row.error().contains("cannot call tools"));
		}
		EvalReport.ArmSummary summary = EvalReport.arms(rows).stream()
			.filter(a -> "no-tools".equals(a.arm())).findFirst().orElseThrow();
		assertEquals("An arm that never ran has no cells to average.", 0, summary.cells());
		assertEquals(7, summary.cellsNotRun());
	}

	/**
	 * A provider that refuses <em>us</em> is not a model that failed.
	 *
	 * <p>
	 * A wrong key, an unpaid bill or an outage means the scenario never reached the model. Grading
	 * such a run would put a zero next to a model that was never asked, and - because the other arm's
	 * key worked - would read as a comparison. It is a "could not run", with the provider's own words
	 * as the reason.
	 * </p>
	 */
	public void testAProviderThatRefusesUsIsNotAModelFailure() throws Exception {
		RunRecord row = onlyScenario((request, turn) -> {
			throw new ProviderException(ProviderException.Kind.AUTHENTICATION, 401,
				"x-api-key header is required");
		}, "create-ticket");

		assertEquals(details(row), RunOutcome.COULD_NOT_RUN, row.outcome());
		assertTrue("The row has to carry the provider's reason: " + row.error(),
			row.error().contains("AUTHENTICATION"));
		assertTrue("A run that never happened has no verdicts to show.", row.verdicts().isEmpty());
	}

	/** An error from inside a tool is a model failure, not an infrastructure one. */
	public void testAFailingRunIsStillGraded() throws Exception {
		RunRecord row = onlyScenario((request, turn) -> {
			if (turn == 0) {
				return List.of(EvalScripts.write("this is not TL-Script at all("));
			}
			return List.of(new TextContent("I could not do it."));
		}, "create-ticket");

		assertEquals("A broken script is the model's mistake: " + details(row),
			RunOutcome.FAIL, row.outcome());
		assertTrue("The ticket was not created, and the grader has to say so: " + details(row),
			row.failedGraders().contains("the-new-ticket-exists"));
	}

	/** The CSV of a real matrix has one line per run and the header the report is read from. */
	public void testTheMatrixWritesOneCsvLinePerRun() throws Exception {
		List<RunRecord> rows = runScripted(EvalScripts.oracle());
		String[] lines = EvalCsv.render(rows).split("\n");

		assertEquals(EvalCsv.HEADER, lines[0]);
		assertEquals(rows.size() + 1, lines.length);
		assertTrue("A run that called the write tool records it: " + lines[1],
			EvalCsv.render(rows).contains(EvalScripts.WRITE_TOOL));
	}

	/** Runs the shipped scenario set on the scripted arm alone. */
	private static List<RunRecord> runScripted(ScriptedProvider.Script script) throws Exception {
		ScriptedProvider.install(script);
		EvalConfig config = EvalConfigs.restrictedTo(TestEvalConfig.pmScenarios(), List.of(SCRIPTED_ARM));
		return EvalMatrix.run(config, PersonManager.getManager().getRoot(), new PmFixture());
	}

	/** Runs one scenario of the shipped set, once, on the scripted arm. */
	private static RunRecord onlyScenario(ScriptedProvider.Script script, String scenarioId)
			throws Exception {
		ScriptedProvider.install(script);
		EvalConfig config = EvalConfigs.restrictedTo(TestEvalConfig.pmScenarios(), List.of(SCRIPTED_ARM));
		config.getScenarios().removeIf(s -> !scenarioId.equals(s.getId()));
		assertEquals("No scenario '" + scenarioId + "' in the shipped set.",
			1, config.getScenarios().size());
		config.getScenarios().get(0).update(
			config.getScenarios().get(0).descriptor().getProperty(EvalScenario.REPEATS), 1);

		List<RunRecord> rows =
			EvalMatrix.run(config, PersonManager.getManager().getRoot(), new PmFixture());
		assertEquals(1, rows.size());
		return rows.get(0);
	}

	private static String details(RunRecord row) {
		StringBuilder out = new StringBuilder();
		out.append("stopReason=").append(row.stopReason());
		out.append(", tools=").append(row.toolCalls());
		if (row.error() != null) {
			out.append(", error=").append(row.error());
		}
		for (Verdict verdict : row.verdicts()) {
			if (!verdict.passed()) {
				out.append("\n    ").append(verdict.label()).append(": ").append(verdict.detail());
			}
		}
		return out.toString();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestEvalHarness.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				// Every run asks it whether the account may use the tool; there is no unchecked mode
				// to fall back to, so the eval has to boot the same gate a chat request passes.
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
