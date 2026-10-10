/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * One business task put to the harness, with everything needed to decide afterwards whether it was
 * done.
 *
 * <p>
 * A scenario is run once per {@link EvalConfig.Arm arm} and per repeat, and each such run is graded
 * independently. It carries no setup of its own: the whole suite runs against one seeded knowledge
 * base, which the suite reseeds between runs, so a scenario describes a task and its
 * {@link #getExpectations() expectations} and nothing else.
 * </p>
 *
 * <h2>The oracle</h2>
 *
 * <p>
 * {@link #getOracle()} is mandatory, and that is the point of it. It is the reference solution -
 * the tool calls a competent operator would make - written down in prose. Without one, a scenario
 * that every model fails is indistinguishable from a scenario that is impossible, and a benchmark
 * whose unsolvable tasks nobody noticed measures nothing. It is not executed; it is read by whoever
 * has to decide whether a red cell is the model's fault.
 * </p>
 */
public interface EvalScenario extends ConfigurationItem {

	/** @see #getId() */
	String ID = "id";

	/** @see #getPrompt() */
	String PROMPT = "prompt";

	/** @see #getOracle() */
	String ORACLE = "oracle";

	/** @see #getArms() */
	String ARMS = "arms";

	/** @see #getRepeats() */
	String REPEATS = "repeats";

	/** @see #getExpectations() */
	String EXPECTATIONS = "expectations";

	/** Value of {@link #getRepeats()} meaning "whatever the suite says". */
	int INHERIT_REPEATS = 0;

	/**
	 * Identifies the scenario in the CSV and in every report.
	 *
	 * @see #ID
	 */
	@Name(ID)
	@Mandatory
	String getId();

	/**
	 * The user message the run starts from - the whole input the model gets, beside the agent's own
	 * system prompt.
	 *
	 * @see #PROMPT
	 */
	@Name(PROMPT)
	@Mandatory
	String getPrompt();

	/**
	 * The documented reference solution: how this task is done, and why the expectations below are
	 * the right ones.
	 *
	 * @see #ORACLE
	 */
	@Name(ORACLE)
	@Mandatory
	String getOracle();

	/**
	 * Which arms this scenario runs on, by {@link EvalConfig.Arm#getName() arm name}; empty means
	 * every arm of the suite.
	 *
	 * <p>
	 * Restricting a scenario to some arms is for a task that only one side can be asked - a
	 * capability an arm's agent definition does not carry, say. Narrowing it to save money makes the
	 * matrix unbalanced and the pass rates incomparable, so prefer dropping repeats.
	 * </p>
	 *
	 * @see #ARMS
	 */
	@Name(ARMS)
	@Format(CommaSeparatedStrings.class)
	List<String> getArms();

	/**
	 * How often each arm runs this scenario, or {@link #INHERIT_REPEATS} to take the suite's
	 * default.
	 *
	 * <p>
	 * Repeats exist because no provider here offers a sampling seed and temperature is not a fix:
	 * lowering it trades one kind of variance for another and can bias the result. So a cell is
	 * reported as k of n, never as a single pass or fail.
	 * </p>
	 *
	 * @see #REPEATS
	 */
	@Name(REPEATS)
	@IntDefault(INHERIT_REPEATS)
	int getRepeats();

	/**
	 * What must hold after the run. All of them must pass for the run to count as passed.
	 *
	 * <p>
	 * Must not be empty - a scenario that asserts nothing would report a pass for every model.
	 * </p>
	 *
	 * @see #EXPECTATIONS
	 */
	@Name(EXPECTATIONS)
	@Subtypes({
		@Subtype(tag = KbQuery.TAG_NAME, type = KbQuery.class),
		@Subtype(tag = ToolSequence.TAG_NAME, type = ToolSequence.class),
		@Subtype(tag = Presented.TAG_NAME, type = Presented.class),
		@Subtype(tag = CitationExpectation.TAG_NAME, type = CitationExpectation.class),
		@Subtype(tag = JudgeExpectation.TAG_NAME, type = JudgeExpectation.class),
	})
	List<Expectation> getExpectations();
}
