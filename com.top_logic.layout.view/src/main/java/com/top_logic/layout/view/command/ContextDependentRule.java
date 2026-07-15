/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.view.ViewContext;

/**
 * Optional mix-in for a {@link ViewExecutabilityRule} that needs the {@link ViewContext} of the
 * command it guards before it can answer {@link ViewExecutabilityRule#isExecutable(Object)}.
 *
 * <p>
 * A rule resolving an enclosing security scope or a context channel cannot do so from its
 * configuration alone. {@link ViewExecutabilityRules#build(java.util.List, ViewContext)} calls
 * {@link #bind(ViewContext)} once, at control-build time, on every rule implementing this interface,
 * so the rule can capture what it needs from the context.
 * </p>
 */
public interface ContextDependentRule {

	/**
	 * Binds this rule to the {@link ViewContext} of the command it guards.
	 *
	 * <p>
	 * Called exactly once, before the rule is first evaluated. The rule should capture any
	 * context-derived state (e.g. the enclosing {@link ViewContext#getSecurityScope() security
	 * scope} or a resolved channel) here.
	 * </p>
	 *
	 * @param context
	 *        The build-time context of the guarded command.
	 */
	void bind(ViewContext context);
}
