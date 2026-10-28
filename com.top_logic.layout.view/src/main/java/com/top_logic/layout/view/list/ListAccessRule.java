/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.function.Function;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The rule a {@link LinkElementAction} or a {@link RemoveElementAction} brings of its own: the
 * access checks the enclosing {@link ObjectListElement} declares for the operation, applied to the
 * main argument of the command as the element.
 *
 * <p>
 * The checks are found in the {@link ObjectListScope} of the context the command is bound to.
 * Outside a list, or for a list declaring no checks, the rule is always executable.
 * </p>
 */
final class ListAccessRule implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	private final Function<ObjectListScope, ViewExecutabilityRule> _access;

	private ViewExecutabilityRule _rule = ViewExecutabilityRule.ALWAYS_EXECUTABLE;

	/**
	 * Creates a {@link ListAccessRule}.
	 *
	 * @param access
	 *        Selects the checks of the operation from the scope of the list.
	 */
	ListAccessRule(Function<ObjectListScope, ViewExecutabilityRule> access) {
		_access = access;
	}

	@Override
	public void bind(ViewContext context) {
		ObjectListScope scope = context.getScope(ObjectListScope.class);
		_rule = scope == null ? ViewExecutabilityRule.ALWAYS_EXECUTABLE : _access.apply(scope);
	}

	@Override
	public Runnable observe(Runnable revalidate) {
		if (_rule instanceof ObservableRule observable) {
			return observable.observe(revalidate);
		}
		return () -> {
			// Nothing observed.
		};
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return _rule.isExecutable(input);
	}

}
