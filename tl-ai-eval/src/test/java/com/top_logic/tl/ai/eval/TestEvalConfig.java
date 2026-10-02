/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * That an eval file says what it looks like it says, and that a file which could only produce a
 * misleading result is refused before anything runs.
 */
public class TestEvalConfig extends TestCase {

	/** The shipped {@code pm} scenario set parses, and is the set the README describes. */
	public void testShippedScenarioSetParses() throws Exception {
		EvalConfig config = pmScenarios();

		assertEquals("The README and the scenario file must agree on the arms.",
			List.of("scripted", "sonnet", "haiku"), List.copyOf(config.getArms().keySet()));
		assertEquals("Five writing scenarios (#433) and two showing ones (#616).", 7, config.getScenarios().size());
		assertEquals(3, config.getRepeats());

		assertEquals(List.of("create-ticket", "update-ticket-status", "scoped-update",
			"absent-ticket-changes-nothing", "refuse-delete", "show-tickets-as-table",
			"tickets-per-project-as-chart"),
			config.getScenarios().stream().map(EvalScenario::getId).toList());
	}

	/**
	 * Every scenario carries a reference solution.
	 *
	 * <p>
	 * The configuration makes it mandatory, so this cannot fail by omission - what it guards is a
	 * scenario shipped with an oracle of {@code "todo"}. Without a written solution, a scenario every
	 * model fails is indistinguishable from a scenario nobody can do.
	 * </p>
	 */
	public void testEveryScenarioHasAnOracle() throws Exception {
		for (EvalScenario scenario : pmScenarios().getScenarios()) {
			String oracle = scenario.getOracle();
			assertTrue("Scenario '" + scenario.getId() + "' has no usable reference solution: '"
				+ oracle + "'", oracle != null && oracle.length() > 40);
		}
	}

	/** Every scenario says something that a run doing nothing at all would fail. */
	public void testEveryScenarioHasAPositiveExpectation() throws Exception {
		for (EvalScenario scenario : pmScenarios().getScenarios()) {
			assertTrue("Scenario '" + scenario.getId() + "' only says what must not have happened,"
				+ " which a run that crashed immediately also satisfies.",
				scenario.getExpectations().stream().anyMatch(EvalConfigs::isPositive));
		}
	}

	/** At least two scenarios change the data, and one is a refusal - the ticket's own shape. */
	public void testTheSetCoversWritingAndRefusing() throws Exception {
		List<EvalScenario> scenarios = pmScenarios().getScenarios();
		long writing = scenarios.stream()
			.filter(s -> s.getExpectations().stream()
				.anyMatch(e -> e instanceof ToolSequence tools
					&& tools.getRequired().contains("script-exec.executeTLScript")))
			.count();
		long refusing = scenarios.stream()
			.filter(s -> s.getExpectations().stream()
				.anyMatch(e -> e instanceof ToolSequence tools
					&& tools.getForbidden().contains("script-exec.executeTLScript")))
			.count();
		assertTrue("Fewer than two scenarios write anything: " + writing, writing >= 2);
		assertTrue("No scenario requires the write tool to stay unused: " + refusing, refusing >= 1);
	}

	/** A scenario that asserts nothing is refused, because every model would pass it. */
	public void testScenarioWithoutExpectationsIsRefused() throws Exception {
		assertRefused("asserts nothing",
			scenario("empty", "<expectations/>"));
	}

	/**
	 * A scenario that only forbids is refused: a run that crashed before its first turn satisfies
	 * it.
	 */
	public void testScenarioThatOnlyForbidsIsRefused() throws Exception {
		assertRefused("only says what must NOT have happened",
			scenario("forbid-only",
				"<expectations><tool-sequence forbidden='a.b'/></expectations>"));
	}

	/** An expectation naming no tool at all is refused. */
	public void testEmptyToolSequenceIsRefused() throws Exception {
		assertRefused("naming neither a required nor a forbidden tool",
			scenario("no-names",
				"<expectations><tool-sequence/><kb-query expression='1' expected='1'/></expectations>"));
	}

	/** A scenario naming an arm nobody configured is refused, not quietly run on fewer arms. */
	public void testUnknownArmIsRefused() throws Exception {
		assertRefused("which is not configured",
			scenario("wrong-arm", "arms='ghost'",
				"<expectations><kb-query expression='1' expected='1'/></expectations>"));
	}

	/** Two scenarios of one id would merge into one cell, so the file is refused. */
	public void testDuplicateIdIsRefused() throws Exception {
		String one = scenarioXml("twice", "",
			"<expectations><kb-query expression='1' expected='1'/></expectations>");
		assertRefused("is declared twice", evalXml(one + one));
	}

	/** A scenario may narrow itself to some of the arms, and the others are then not run. */
	public void testScenarioCanNarrowItsArms() throws Exception {
		EvalConfig config = parse(evalXml(scenarioXml("narrow", "arms='scripted'",
			"<expectations><kb-query expression='1' expected='1'/></expectations>")));
		EvalScenario scenario = config.getScenarios().get(0);

		assertEquals(List.of("scripted"),
			EvalConfigs.armsOf(config, scenario).stream().map(EvalConfig.Arm::getName).toList());
		assertEquals("Without its own value a scenario inherits the suite's repeats.",
			3, EvalConfigs.repeatsOf(config, scenario));
	}

	private static void assertRefused(String expectedInMessage, String xml) throws Exception {
		try {
			parse(xml);
			fail("This configuration could only produce a misleading result and must be refused: "
				+ xml);
		} catch (IllegalArgumentException ex) {
			assertTrue("The refusal must say what is wrong. Said: " + ex.getMessage(),
				ex.getMessage().contains(expectedInMessage));
		}
	}

	private static EvalConfig parse(String xml) throws ConfigurationException {
		return EvalConfigs.load(CharacterContents.newContent(xml, "test-eval.xml"));
	}

	private static String scenario(String id, String expectations) {
		return evalXml(scenarioXml(id, "", expectations));
	}

	private static String scenario(String id, String attributes, String expectations) {
		return evalXml(scenarioXml(id, attributes, expectations));
	}

	private static String scenarioXml(String id, String attributes, String expectations) {
		return "<scenario id='" + id + "' " + attributes + ">"
			+ "<prompt>Do the thing.</prompt>"
			+ "<oracle>Call the tool, check the result.</oracle>"
			+ expectations
			+ "</scenario>";
	}

	private static String evalXml(String scenarios) {
		return "<eval xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "<arms><arm name='scripted' agent='Eval_Scripted' model='eval-scripted'/></arms>"
			+ "<scenarios>" + scenarios + "</scenarios>"
			+ "</eval>";
	}

	/**
	 * Booted as a module, because resolving the {@code <kb-query>} and {@code <tool-sequence>} tags
	 * against their configuration interfaces goes through the type index.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestEvalConfig.class));
	}

	/** The shipped scenario set, read from the module's own resources. */
	static EvalConfig pmScenarios() throws ConfigurationException {
		return EvalConfigs.load(
			new ClassRelativeBinaryContent(TestEvalConfig.class, "pm-scenarios.config.xml"));
	}
}
