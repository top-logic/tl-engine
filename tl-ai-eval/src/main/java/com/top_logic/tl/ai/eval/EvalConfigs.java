/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;

/**
 * Reading an {@link EvalConfig} from a file, and saying what is wrong with it before a single API
 * call is spent.
 */
public class EvalConfigs {

	private EvalConfigs() {
		// Static utility.
	}

	/**
	 * Parses an eval file and checks it.
	 *
	 * @param content
	 *        The {@code <eval>} document.
	 * @throws ConfigurationException
	 *         If the document does not parse.
	 * @throws IllegalArgumentException
	 *         If it parses but describes an eval that could not produce a meaningful result - see
	 *         {@link #check(EvalConfig)}.
	 */
	public static EvalConfig load(Content content) throws ConfigurationException {
		EvalConfig result = TypedConfiguration.parse(EvalConfig.TAG_NAME, EvalConfig.class, content);
		check(result);
		return result;
	}

	/**
	 * Refuses a configuration that would report numbers nobody may read.
	 *
	 * <p>
	 * Every one of these is a way to get a green matrix that means nothing: a scenario that asserts
	 * nothing passes always; a tool-sequence expectation with no required name passes a run in which
	 * the model did nothing at all; a scenario naming an arm that does not exist quietly runs on
	 * fewer arms than the report's header claims. They are refused here, at load time, rather than
	 * discovered in the numbers.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         Listing everything that is wrong, not merely the first thing.
	 */
	public static void check(EvalConfig config) {
		List<String> problems = new ArrayList<>();
		if (config.getArms().isEmpty()) {
			problems.add("No arm is configured: there is nothing to compare.");
		}
		if (config.getScenarios().isEmpty()) {
			problems.add("No scenario is configured: there is nothing to ask.");
		}
		if (config.getRepeats() < 1) {
			problems.add("repeats=" + config.getRepeats() + ": a cell needs at least one run.");
		}
		List<String> seen = new ArrayList<>();
		for (EvalScenario scenario : config.getScenarios()) {
			String id = scenario.getId();
			if (seen.contains(id)) {
				problems.add("Scenario '" + id + "' is declared twice; ids identify a cell.");
			}
			seen.add(id);
			if (scenario.getExpectations().isEmpty()) {
				problems.add("Scenario '" + id + "' asserts nothing, so every model would pass it.");
			} else if (scenario.getExpectations().stream().noneMatch(EvalConfigs::isPositive)) {
				problems.add("Scenario '" + id + "' only says what must NOT have happened, which a run"
					+ " that did nothing satisfies. Add a <" + KbQuery.TAG_NAME + "> on the state the"
					+ " task should leave behind, or name a required tool.");
			}
			for (Expectation expectation : scenario.getExpectations()) {
				if (expectation instanceof JudgeExpectation judge) {
					if (config.getJudgeModel() == null || config.getJudgeModel().isBlank()) {
						problems.add("Scenario '" + id + "' has a <" + JudgeExpectation.TAG_NAME
							+ ">, but the eval names no judge-model to decide it.");
					}
					if (Judge.criteria(judge.getRubric()).isEmpty()) {
						problems.add("Scenario '" + id + "': a <" + JudgeExpectation.TAG_NAME
							+ "> whose rubric names no criterion asserts nothing.");
					}
					if (judge.getPassRatio() <= 0 || judge.getPassRatio() > 1) {
						problems.add("Scenario '" + id + "': pass-ratio=" + judge.getPassRatio()
							+ " is not between 0 (exclusive) and 1.");
					}
				}
				if (expectation instanceof ToolSequence tools
					&& tools.getRequired().isEmpty() && tools.getForbidden().isEmpty()) {
					problems.add("Scenario '" + id + "': a <" + ToolSequence.TAG_NAME
						+ "> naming neither a required nor a forbidden tool asserts nothing.");
				}
			}
			for (String arm : scenario.getArms()) {
				if (!config.getArms().containsKey(arm)) {
					problems.add("Scenario '" + id + "' names arm '" + arm
						+ "', which is not configured. Arms: " + config.getArms().keySet());
				}
			}
			if (scenario.getRepeats() < 0) {
				problems.add("Scenario '" + id + "' has repeats=" + scenario.getRepeats() + ".");
			}
		}
		if (!problems.isEmpty()) {
			throw new IllegalArgumentException(
				"This eval configuration cannot produce a readable result:\n  "
					+ String.join("\n  ", problems));
		}
	}

	/**
	 * Whether an expectation can fail for a run in which nothing happened.
	 *
	 * <p>
	 * This is what "every grader is positive" means in practice, and it is a property of the
	 * scenario rather than of each clause. A refusal scenario legitimately says "the deleting tool
	 * was never called" - but that alone is satisfied by a run that crashed before its first turn,
	 * so it has to stand beside a claim about the state the task should have left behind.
	 * </p>
	 */
	static boolean isPositive(Expectation expectation) {
		if (expectation instanceof KbQuery || expectation instanceof Presented) {
			// A presentation fails for a run that presented nothing.
			return true;
		}
		if (expectation instanceof JudgeExpectation) {
			// A run that wrote no answer has nothing to judge, and fails.
			return true;
		}
		if (expectation instanceof CitationExpectation citations) {
			return citations.getMinSources() > 0;
		}
		return expectation instanceof ToolSequence tools && !tools.getRequired().isEmpty();
	}

	/**
	 * A copy of the configuration with only the named arms left.
	 *
	 * <p>
	 * For running the whole scenario set against one arm: the scripted arm, so that an ordinary
	 * build proves the expressions in the file still compile and still mean what they say, or one
	 * real arm as a smoke test before the money is spent on the rest. A scenario that names arms of
	 * its own keeps them; one that names an arm this copy dropped runs on the remainder.
	 * </p>
	 */
	public static EvalConfig restrictedTo(EvalConfig config, Collection<String> armNames) {
		EvalConfig result = TypedConfiguration.copy(config);
		result.getArms().keySet().retainAll(armNames);
		for (EvalScenario scenario : result.getScenarios()) {
			scenario.getArms().retainAll(armNames);
		}
		check(result);
		return result;
	}

	/** The arms the given scenario runs on, in the configuration's order. */
	public static List<EvalConfig.Arm> armsOf(EvalConfig config, EvalScenario scenario) {
		List<EvalConfig.Arm> result = new ArrayList<>();
		for (EvalConfig.Arm arm : config.getArms().values()) {
			if (scenario.getArms().isEmpty() || scenario.getArms().contains(arm.getName())) {
				result.add(arm);
			}
		}
		return result;
	}

	/** How often the given scenario runs per arm. */
	public static int repeatsOf(EvalConfig config, EvalScenario scenario) {
		return scenario.getRepeats() == EvalScenario.INHERIT_REPEATS
			? config.getRepeats()
			: scenario.getRepeats();
	}
}
