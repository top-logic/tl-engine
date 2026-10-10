/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * A whole eval: the arms it compares and the scenarios it puts to them.
 *
 * <p>
 * Read from a standalone configuration file, so a scenario set is a file an application ships
 * beside its own model rather than a Java class anybody has to compile.
 * </p>
 *
 * <pre>
 * &lt;eval xmlns:config="http://www.top-logic.com/ns/config/6.0" repeats="3"&gt;
 *   &lt;arms&gt;
 *     &lt;arm name="sonnet" agent="Eval_Sonnet" model="claude-sonnet-4-6"/&gt;
 *     &lt;arm name="haiku" agent="Eval_Haiku" model="claude-haiku-4-5-20251001"/&gt;
 *   &lt;/arms&gt;
 *   &lt;scenarios&gt;
 *     &lt;scenario id="..." prompt="..." oracle="..."&gt;
 *       &lt;kb-query .../&gt;
 *     &lt;/scenario&gt;
 *   &lt;/scenarios&gt;
 * &lt;/eval&gt;
 * </pre>
 *
 * @see EvalConfigs#load(com.top_logic.basic.io.Content)
 */
@TagName(EvalConfig.TAG_NAME)
public interface EvalConfig extends ConfigurationItem {

	/** The XML tag an eval file is written as. */
	String TAG_NAME = "eval";

	/** @see #getArms() */
	String ARMS = "arms";

	/** @see #getScenarios() */
	String SCENARIOS = "scenarios";

	/** @see #getRepeats() */
	String REPEATS = "repeats";

	/** @see #getJudgeModel() */
	String JUDGE_MODEL = "judge-model";

	/** @see #getJudgeProvider() */
	String JUDGE_PROVIDER = "judge-provider";

	/**
	 * One side of the comparison: an agent definition running on a model.
	 *
	 * <p>
	 * Two things are bundled here because they cannot be separated in this harness. A per-run model
	 * override exists in Java, but the shipped tool-access policy defaults to
	 * {@code model-access default="assigned"} and refuses a model the agent is not assigned, so the
	 * matrix is built from one agent definition per model rather than from one agent and a model
	 * parameter. Configuring the arm as a pair turns that constraint into a feature: two arms naming
	 * the <em>same</em> model and different agent definitions compare two versions of the harness -
	 * two system prompts, two tool sets - on one model, which is the cheaper and often the more
	 * actionable of the two questions this suite can answer.
	 * </p>
	 */
	@TagName("arm")
	interface Arm extends NamedConfigMandatory {

		/** @see #getAgent() */
		String AGENT = "agent";

		/** @see #getModel() */
		String MODEL = "model";

		/**
		 * The agent definition this arm runs, by its configured name.
		 *
		 * @see #AGENT
		 */
		@Name(AGENT)
		@Mandatory
		String getAgent();

		/**
		 * The model {@link #getAgent()} is assigned, repeated here so that the CSV can name it
		 * without asking the service, and so that the arm can be skipped before it is started when
		 * the model cannot call tools.
		 *
		 * @see #MODEL
		 */
		@Name(MODEL)
		@Mandatory
		String getModel();
	}

	/**
	 * The arms to compare, in report order.
	 *
	 * @see #ARMS
	 */
	@Name(ARMS)
	@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
	Map<String, Arm> getArms();

	/**
	 * The tasks, in report order.
	 *
	 * @see #SCENARIOS
	 */
	@Name(SCENARIOS)
	@EntryTag("scenario")
	List<EvalScenario> getScenarios();

	/**
	 * How often every arm runs every scenario, unless the scenario says otherwise.
	 *
	 * <p>
	 * Three is the working default. Repeats buy a reliability figure (pass^k, "all k attempts
	 * succeeded"); they buy very little resolution between two models, which is what scenario count
	 * buys - see the statistics note in the module README before reading a small difference as one.
	 * </p>
	 *
	 * @see #REPEATS
	 */
	@Name(REPEATS)
	@IntDefault(3)
	int getRepeats();

	/**
	 * The model that decides every {@link JudgeExpectation} of this file; empty when no scenario has
	 * one.
	 *
	 * <p>
	 * Pinned here so that runs on different days are graded by the same judge - a comparison between
	 * runs with different judges measures the judges. Choose a model of another family than the arms,
	 * which would otherwise grade their own habits.
	 * </p>
	 *
	 * @see #JUDGE_MODEL
	 */
	@Name(JUDGE_MODEL)
	@Nullable
	String getJudgeModel();

	/**
	 * The provider serving {@link #getJudgeModel()}, or empty when only one provider exposes it.
	 *
	 * @see #JUDGE_PROVIDER
	 */
	@Name(JUDGE_PROVIDER)
	@Nullable
	String getJudgeProvider();
}
