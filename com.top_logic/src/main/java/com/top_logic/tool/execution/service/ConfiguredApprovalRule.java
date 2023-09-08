/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.filter.ExecutionContextFilter;

/**
 * {@link CommandApprovalRule} that can be configured.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ConfiguredApprovalRule extends AbstractConfiguredInstance<ConfiguredApprovalRule.Config>
		implements CommandApprovalRule {

	/**
	 * Configuration options for {@link ConfiguredApprovalRule}.
	 */
	public interface Config extends PolymorphicConfiguration<ConfiguredApprovalRule> {
		/**
		 * @see #getContexts()
		 */
		String CONTEXTS = "contexts";

		/**
		 * @see #getExcludedContexts()
		 */
		String EXCLUDED_CONTEXTS = "excluded-contexts";

		/**
		 * @see #getExecutability()
		 */
		String EXECUTABILITY = "executability";

		/**
		 * Condition that selects a context in which to apply the {@link #getExecutability()} to a
		 * command execution.
		 * 
		 * <p>
		 * If multiple contexts are given, the {@link #getExecutability()} is applied, in any of
		 * those contexts.
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

		/**
		 * The {@link ExecutabilityRule} to invoke for computing whether the command can be executed
		 * on the target model.
		 */
		@Name(EXECUTABILITY)
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();
	}

	private ExecutabilityRule _rule;

	private ExecutionContextFilter _matcher;

	/**
	 * Creates a {@link ConfiguredApprovalRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredApprovalRule(InstantiationContext context, ConfiguredApprovalRule.Config config) {
		super(context, config);

		_matcher =
			noneMeansAll(matcher(context, config.getContexts())).except(matcher(context, config.getExcludedContexts()));
		_rule =
			CombinedExecutabilityRule.combine(TypedConfiguration.getInstanceList(context, config.getExecutability()));
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

		return _rule.isExecutable(component, model, arguments);
	}

}