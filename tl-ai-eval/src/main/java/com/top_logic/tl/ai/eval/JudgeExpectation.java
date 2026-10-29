/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;

/**
 * An {@link Expectation} on what the answer says, decided by a judge model against a rubric.
 *
 * <p>
 * For the half of a research task that no rule can check: whether the report answers the question,
 * names what it must name, and states the contradiction it found. The judge is one pinned model, named
 * on the eval file ({@link EvalConfig#getJudgeModel()}), so that two runs are compared by the same
 * judge; it should be of another family than the model under test, which would otherwise grade its
 * own habits.
 * </p>
 *
 * <p>
 * The rubric is one criterion per line; a leading "- " is dropped. The judge says for each whether
 * the answer meets it, and the expectation holds when the share of met criteria reaches
 * {@link #getPassRatio()}. A judge that could not be asked, or whose answer cannot be read, fails
 * the expectation with the reason - never passes it.
 * </p>
 *
 * <pre>
 * &lt;judge label="answers-the-question"&gt;
 *     &lt;reference&gt;Geoffrey Hinton, PhD from the University of Edinburgh (1978).&lt;/reference&gt;
 *     &lt;rubric&gt;
 *         - Names the University of Edinburgh.
 *         - Says that the younger laureate is Geoffrey Hinton.
 *     &lt;/rubric&gt;
 * &lt;/judge&gt;
 * </pre>
 */
@TagName(JudgeExpectation.TAG_NAME)
public interface JudgeExpectation extends Expectation {

	/** The XML tag this expectation is written as. */
	String TAG_NAME = "judge";

	/** @see #getRubric() */
	String RUBRIC = "rubric";

	/** @see #getReference() */
	String REFERENCE = "reference";

	/** @see #getPassRatio() */
	String PASS_RATIO = "pass-ratio";

	/**
	 * The criteria, one per line.
	 *
	 * @see #RUBRIC
	 */
	@Name(RUBRIC)
	@Mandatory
	String getRubric();

	/**
	 * The correct answer, for a question that has one; empty otherwise.
	 *
	 * <p>
	 * Shown to the judge as the reference the criteria are measured against - not to the agent.
	 * </p>
	 *
	 * @see #REFERENCE
	 */
	@Name(REFERENCE)
	@Nullable
	String getReference();

	/**
	 * The share of criteria that must be met, from 0 (exclusive) to 1; 1 means all of them.
	 *
	 * @see #PASS_RATIO
	 */
	@Name(PASS_RATIO)
	@DoubleDefault(1.0)
	double getPassRatio();
}
