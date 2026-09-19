/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * One thing a {@link EvalScenario scenario} claims about a finished run.
 *
 * <p>
 * Every expectation is <b>positive</b>: it states what must be the case, and it fails when it
 * cannot establish that. An expectation that passes because it looked for something and found
 * nothing is the defect that inflated published agent benchmarks (τ-bench scored empty responses as
 * successes), so {@link KbQuery} compares an evaluated value against a written one and
 * {@link ToolSequence} refuses to pass without at least one required tool name to look for.
 * </p>
 *
 * @see Graders#grade(Expectation, RunRecord)
 */
@Abstract
public interface Expectation extends ConfigurationItem {

	/** @see #getLabel() */
	String LABEL = "label";

	/**
	 * Short name of this expectation, used to say which one failed.
	 *
	 * <p>
	 * Reaches the CSV's {@code failedGraders} column, so it should read as a claim - {@code
	 * "alpha-has-four-tickets"} rather than {@code "check1"}. Left empty, the grader falls back to
	 * a generated description of what it compared.
	 * </p>
	 *
	 * @see #LABEL
	 */
	@Name(LABEL)
	@Nullable
	String getLabel();
}
