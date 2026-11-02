/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} deciding with another rule, and re-deciding whenever an object of
 * one of the given types is created, changed or deleted.
 *
 * <p>
 * A {@link LiveExecutability} follows the objects its arguments hold. A rule that reads further
 * objects - the other parts of the input's container, say - answers differently when those change,
 * which nobody tells the command. This is what an {@link ViewCommand#getIntrinsicRule() intrinsic
 * rule} reading such objects wraps itself into, so that it follows them the way
 * {@link ExecutabilityConfig#getObservedTypes() observed types} let a configured rule follow them.
 * </p>
 */
public class TypeObservingRule implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	private final ViewExecutabilityRule _rule;

	private final Set<TLStructuredType> _types;

	private ViewContext _context;

	/**
	 * Creates a {@link TypeObservingRule}.
	 *
	 * @param rule
	 *        The rule deciding.
	 * @param types
	 *        The types whose objects the rule reads.
	 */
	public TypeObservingRule(ViewExecutabilityRule rule, Set<TLStructuredType> types) {
		_rule = rule;
		_types = types;
	}

	@Override
	public void bind(ViewContext context) {
		_context = context;
		if (_rule instanceof ContextDependentRule contextDependent) {
			contextDependent.bind(context);
		}
	}

	@Override
	public Runnable observe(Runnable revalidate) {
		List<Registration> registrations = new ArrayList<>();
		ModelScope scope = _context == null ? null : _context.getModelScope();
		if (scope != null) {
			for (TLStructuredType type : _types) {
				registrations.add(scope.addModelListener(type, change -> revalidate.run()));
			}
		}
		Runnable inner = _rule instanceof ObservableRule observable ? observable.observe(revalidate) : () -> {
			// Nothing observed.
		};
		return () -> {
			registrations.forEach(Registration::dispose);
			inner.run();
		};
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return _rule.isExecutable(input);
	}

	@Override
	public ExecutableState isExecutableFor(ActionArguments arguments) {
		return _rule.isExecutableFor(arguments);
	}

}
