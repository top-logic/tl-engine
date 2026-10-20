/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

/**
 * A tool-access decision together with the rules that produced it.
 *
 * <p>
 * The read-only counterpart of the enforcement path: {@link ToolAuthorization} answers "may this run
 * call this tool" and logs a denial, this answers "and why" without any side effect. Same rules, same
 * order, no decision of its own - see {@link EffectiveToolAccess}, which composes it for an account
 * or a hypothetical role set.
 * </p>
 *
 * @param allowed
 *        Whether the tool or capability is reachable.
 * @param decidedBy
 *        The rule that produced {@link #allowed()}. Never <code>null</code>.
 * @param overridden
 *        The rule that would have decided otherwise and lost, or <code>null</code> if nothing was
 *        overridden.
 *        <p>
 *        This is what turns a verdict into a sentence an administrator can act on. "Denied" alone
 *        sends nobody anywhere; "denied because unattended runs may not, although the role
 *        {@code projectPartner} grants it" names both the control to change and the grant that is
 *        already there. It is filled for the three cases where two rules disagree: the background
 *        ceiling over a grant, a {@code <deny>} over a grant, and the administrator bypass over
 *        whatever the policy would have said.
 *        </p>
 * @see EffectiveToolAccess
 */
public record ToolAccessExplanation(boolean allowed, ToolAccessRule decidedBy, ToolAccessRule overridden) {

	/** An {@link #allowed()} decision with nothing overridden. */
	static ToolAccessExplanation allowedBy(ToolAccessRule rule) {
		return new ToolAccessExplanation(true, rule, null);
	}

	/** An {@link #allowed()} decision over a rule that would have denied. */
	static ToolAccessExplanation allowedBy(ToolAccessRule rule, ToolAccessRule overridden) {
		return new ToolAccessExplanation(true, rule, overridden);
	}

	/** A denial with nothing overridden. */
	static ToolAccessExplanation deniedBy(ToolAccessRule rule) {
		return new ToolAccessExplanation(false, rule, null);
	}

	/** A denial over a rule that would have granted. */
	static ToolAccessExplanation deniedBy(ToolAccessRule rule, ToolAccessRule overridden) {
		return new ToolAccessExplanation(false, rule, overridden);
	}

	/**
	 * The kind of the rule that decided, for a caller that only branches on it.
	 */
	public ToolAccessRule.Kind kind() {
		return decidedBy().kind();
	}

}
