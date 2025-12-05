/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.filter.ExecutionContextFilter;

/**
 * Base class for configured {@link CommandApprovalRule} implementations.
 */
public abstract class AbstractConfiguredApprovalRule<C extends AbstractConfiguredApprovalRule.Config<?>>
		extends AbstractConfiguredInstance<C> implements CommandApprovalRule {

	/**
	 * Configuration options for {@link AbstractConfiguredApprovalRule}.
	 */
	public interface Config<I extends AbstractConfiguredApprovalRule<?>> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getContexts()
		 */
		String CONTEXTS = "contexts";

		/**
		 * @see #getExcludedContexts()
		 */
		String EXCLUDED_CONTEXTS = "excluded-contexts";

		/**
		 * Condition that selects a context in which to apply this rule.
		 * 
		 * <p>
		 * If multiple contexts are given, the rule is applied, in any of those contexts.
		 * </p>
		 * 
		 * <p>
		 * If no context is specified at all, this means that the check is performed in all
		 * contexts.
		 * </p>
		 */
		@Name(CONTEXTS)
		List<PolymorphicConfiguration<ExecutionContextFilter>> getContexts();

		/**
		 * Exception to the condition defined in {@link #getContexts()}.
		 * 
		 * <p>
		 * A context is matched, if it is matched by a context in {@link #getContexts()} except if
		 * it is matched by a condition defined here.
		 * </p>
		 */
		@Name(EXCLUDED_CONTEXTS)
		List<PolymorphicConfiguration<ExecutionContextFilter>> getExcludedContexts();
	}

	private ExecutionContextFilter _matcher;

	/**
	 * Creates a {@link AbstractConfiguredApprovalRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractConfiguredApprovalRule(InstantiationContext context, C config) {
		super(context, config);

		_matcher =
			noneMeansAll(matcher(context, config.getContexts())).except(matcher(context, config.getExcludedContexts()));
	}

	private ExecutionContextFilter noneMeansAll(ExecutionContextFilter matcher) {
		return matcher == ExecutionContextFilter.NONE ? ExecutionContextFilter.ALL : matcher;
	}

	private static ExecutionContextFilter matcher(InstantiationContext context,
			List<PolymorphicConfiguration<ExecutionContextFilter>> contexts) {
		ExecutionContextFilter result = ExecutionContextFilter.NONE;
		for (PolymorphicConfiguration<ExecutionContextFilter> filter : contexts) {
			result = result.or(context.getInstance(filter));
		}
		return result;
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup, String commandId,
			Object model, Map<String, Object> arguments) {
		if (!_matcher.matchesExecutionContext(component, commandGroup, commandId).toBoolean()) {
			return ExecutableState.EXECUTABLE;
		}

		return applyRule(component, commandGroup, commandId, model, arguments);
	}

	/**
	 * Evaluates the rule, if the context matches.
	 */
	protected abstract ExecutableState applyRule(LayoutComponent component, BoundCommandGroup commandGroup,
			String commandId, Object model, Map<String, Object> arguments);
}
