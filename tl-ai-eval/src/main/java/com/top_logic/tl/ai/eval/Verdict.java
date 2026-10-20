/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

/**
 * What one {@link Expectation} decided about one run.
 *
 * @param label
 *        The expectation's {@link Expectation#getLabel() label}, or a generated description of what
 *        it compared.
 * @param passed
 *        Whether the expectation held.
 * @param detail
 *        What was found, phrased so that a red cell can be understood without re-running anything:
 *        the value an expression produced beside the one it should have, or the tool names that were
 *        missing. Never empty, not even on a pass.
 */
public record Verdict(String label, boolean passed, String detail) {

	/** A passing verdict. */
	public static Verdict pass(String label, String detail) {
		return new Verdict(label, true, detail);
	}

	/** A failing verdict. */
	public static Verdict fail(String label, String detail) {
		return new Verdict(label, false, detail);
	}
}
