/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

/**
 * How one run of one scenario on one arm ended.
 */
public enum RunOutcome {

	/** Every expectation held. */
	PASS,

	/** The run happened and at least one expectation did not hold. */
	FAIL,

	/**
	 * The run happened, threw, and was graded anyway - the expectations decide.
	 *
	 * <p>
	 * A model that errors halfway and still leaves the right data behind has not passed; a model
	 * that errors and leaves nothing behind fails on the expectations. So this is a detail column,
	 * not a third outcome, and appears in the CSV as the {@code stopReason} {@code ERROR}.
	 * </p>
	 */
	ERROR,

	/**
	 * The arm was never started, because the harness cannot run it.
	 *
	 * <p>
	 * Distinct from {@link #FAIL} on purpose. A model whose catalogue entry says it cannot call
	 * tools has not failed our tool descriptions - it was never asked. Counting it as a failure
	 * would put a number on a question nobody put to it, and averaging that number into a pass rate
	 * would understate every other model in the same table.
	 * </p>
	 */
	COULD_NOT_RUN;

	/** Whether this outcome counts towards the numerator of a pass rate. */
	public boolean passed() {
		return this == PASS;
	}

	/** Whether a run took place at all, i.e. whether this outcome belongs in a pass rate. */
	public boolean ran() {
		return this != COULD_NOT_RUN;
	}
}
