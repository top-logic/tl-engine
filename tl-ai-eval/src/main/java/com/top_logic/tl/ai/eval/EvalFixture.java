/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

/**
 * Puts the data back the way every scenario assumes it.
 *
 * <p>
 * Called by {@link EvalMatrix} before <b>every</b> run, not once per suite. A fresh knowledge base
 * per suite is not enough: the second repeat of a scenario that creates a ticket would start from a
 * database that already has one, and would be graded against an expectation written for the
 * seeded state. Every published warning about agent benchmarks names this as the failure that is
 * hardest to see afterwards, because the run does not error - it just answers a different question
 * from the one the scenario asked.
 * </p>
 *
 * <p>
 * An implementation must be idempotent and must leave the same state whether the previous run wrote
 * nothing, wrote a little or deleted everything.
 * </p>
 */
@FunctionalInterface
public interface EvalFixture {

	/**
	 * A fixture for a suite whose scenarios write nothing.
	 *
	 * <p>
	 * Only correct while that is true. A read-only suite that later gains one write scenario needs a
	 * real fixture, and nothing will announce that it does not have one.
	 * </p>
	 */
	EvalFixture READ_ONLY = () -> {
		// Nothing to put back.
	};

	/** Restores the seeded state. */
	void reset();
}
