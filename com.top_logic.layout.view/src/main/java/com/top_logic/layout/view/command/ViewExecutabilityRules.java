/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.security.CommandAccess;

/**
 * Factory for the combined {@link ViewExecutabilityRule} of a {@link ViewCommand} from its
 * configured {@link ViewCommand.Config#getExecutability() rules}.
 *
 * <p>
 * Instantiates the configured rules, {@link ContextDependentRule#bind(ViewContext) binds} any that
 * depend on the build-time {@link ViewContext} (e.g. security rules resolving an enclosing scope),
 * and combines them. Shared by every element that hosts commands (panels, buttons, forms, app bar)
 * so that rule construction — and in particular context binding — is uniform.
 * </p>
 *
 * <p>
 * The rules are instantiated in the {@link ActionScope} of the arguments they decide over, so that a
 * {@link ScriptedRule script rule} sees them as variables: the {@link CommandArguments arguments} of
 * a command, or the single value an element supplies to the rules of one of its properties (see
 * {@link #scope(ConfigurationItem, String)}). The script editor finds the same scope through the
 * {@link ActionScopeDefinition}s of the configurations holding the rules.
 * </p>
 */
public class ViewExecutabilityRules {

	/**
	 * Builds the combined executability rule for the given configured rules.
	 *
	 * @param ruleConfigs
	 *        The configured rules (from {@link ExecutabilityConfig#getExecutability()}).
	 * @param context
	 *        The build-time context of the guarded command, passed to
	 *        {@link ContextDependentRule#bind(ViewContext)}.
	 * @param scope
	 *        The names of the arguments the rules decide over.
	 * @return The combined rule, or {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} when no rules are
	 *         configured.
	 * @throws ConfigurationError
	 *         If one of the rules cannot be instantiated, e.g. a script using a name that is not
	 *         defined in the given scope.
	 */
	public static ViewExecutabilityRule build(
			List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs, ViewContext context,
			ActionScope scope) {
		if (ruleConfigs.isEmpty()) {
			return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
		}
		DefaultInstantiationContext problems = new DefaultInstantiationContext(ViewExecutabilityRules.class);
		InstantiationContext instantiation = scope.bind(problems);
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewExecutabilityRule> ruleConfig : ruleConfigs) {
			ViewExecutabilityRule rule = instantiation.getInstance(ruleConfig);
			if (rule != null) {
				bind(rule, context);
				rules.add(rule);
			}
		}
		if (problems.hasErrors()) {
			// A rule left out would let the guarded operation through.
			throw new ConfigurationError("Invalid executability rules at " + ruleConfigs.get(0).location() + ".");
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	/**
	 * Puts the rule a command {@link ViewCommand#getIntrinsicRule() brings of its own} in front of
	 * the rules built for its use site.
	 *
	 * <p>
	 * The command's own rule is bound to the same context as the configured ones, so both decide
	 * from the place the command is displayed in.
	 * </p>
	 *
	 * @param context
	 *        The build-time context of the guarded command, {@code null} for a command built
	 *        outside a view (its own rule is then left unbound).
	 * @param command
	 *        The command contributing a rule of its own.
	 * @param configured
	 *        The rule built from the command's configuration.
	 * @return The rule deciding over the command, both parts taken into account.
	 */
	public static ViewExecutabilityRule withIntrinsicRule(ViewContext context, ViewCommand command,
			ViewExecutabilityRule configured) {
		ViewExecutabilityRule intrinsic = command.getIntrinsicRule();
		if (intrinsic == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return configured;
		}
		if (context != null) {
			bind(intrinsic, context);
		}
		if (configured == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return intrinsic;
		}
		return CombinedViewExecutabilityRule.combine(List.of(intrinsic, configured));
	}

	/**
	 * Builds the combined rule of a command, deciding over its {@link CommandArguments arguments}.
	 *
	 * <p>
	 * The {@link CommandAccess checks} of the {@link ViewCommand.Config#getAccess() access} the
	 * command declares come first, followed by the configured
	 * {@link ViewCommand.Config#getExecutability() rules}.
	 * </p>
	 *
	 * @param config
	 *        The configuration of the command.
	 * @param trigger
	 *        The name of the value the trigger of the command supplies, {@code null} for a trigger
	 *        supplying none.
	 * @param context
	 *        The build-time context of the command.
	 */
	public static ViewExecutabilityRule build(ViewCommand.Config config, String trigger, ViewContext context) {
		ActionScope scope = CommandArguments.of(config, trigger).scope();
		ViewExecutabilityRule access = CommandAccess.create(config, scope, context);
		ViewExecutabilityRule configured = build(config.getExecutability(), context, scope);
		if (access == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return configured;
		}
		if (configured == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return access;
		}
		return CombinedViewExecutabilityRule.combine(List.of(access, configured));
	}

	/**
	 * Builds the combined rule of a rule property of an element, deciding over the value the
	 * element supplies.
	 *
	 * @param holder
	 *        The configuration holding the rules.
	 * @param property
	 *        The name of the rule property, a list of {@link ViewExecutabilityRule} configurations
	 *        annotated with {@link Supplies}.
	 * @param context
	 *        The build-time context of the element.
	 * @see #scope(ConfigurationItem, String)
	 */
	@SuppressWarnings("unchecked")
	public static ViewExecutabilityRule build(ConfigurationItem holder, String property, ViewContext context) {
		PropertyDescriptor descriptor = descriptor(holder, property);
		return build((List<PolymorphicConfiguration<? extends ViewExecutabilityRule>>) holder.value(descriptor),
			context, scope(holder, descriptor));
	}

	/**
	 * The scope of the rules in a property of an element that supplies them a value of its own,
	 * e.g. the row of a table whose drag the rules allow.
	 *
	 * <p>
	 * The property declares the supplied value with {@link Supplies}: its default name, and the
	 * property of the element renaming it, looked up like for an {@link InputScript} (see
	 * {@link InputScript#suppliedName(ConfigurationItem, Supplies)}). The supplied value is the
	 * default argument.
	 * </p>
	 *
	 * @param holder
	 *        The configuration holding the rules.
	 * @param property
	 *        The name of the rule property.
	 * @return The scope of the supplied value, {@link ActionScope#NONE} for a property declaring
	 *         none.
	 */
	public static ActionScope scope(ConfigurationItem holder, String property) {
		return scope(holder, descriptor(holder, property));
	}

	private static ActionScope scope(ConfigurationItem holder, PropertyDescriptor property) {
		List<Supplies> supplies = InputScript.supplies(property);
		if (supplies.isEmpty()) {
			return ActionScope.NONE;
		}
		List<String> names = new ArrayList<>(supplies.size());
		for (Supplies supplied : supplies) {
			names.add(InputScript.suppliedName(holder, supplied));
		}
		return ActionScope.of(names, names.get(names.size() - 1));
	}

	/**
	 * The scope of the given rule configuration when it is one of the rules of a property of the
	 * given configuration declaring a supplied value, for
	 * {@link ActionScopeDefinition#actionScope(ConfigurationItem, ActionScope)}.
	 *
	 * @param holder
	 *        The configuration holding the rules.
	 * @param part
	 *        A configuration directly contained in the holder.
	 * @return The {@link #scope(ConfigurationItem, String) scope} of the property holding the part,
	 *         {@code null} if the part is in no property declaring a supplied value.
	 */
	public static ActionScope suppliedScope(ConfigurationItem holder, ConfigurationItem part) {
		for (PropertyDescriptor property : holder.descriptor().getProperties()) {
			if (InputScript.supplies(property).isEmpty()) {
				continue;
			}
			if (holder.value(property) instanceof List<?> rules && containsPart(rules, part)) {
				return scope(holder, property);
			}
		}
		return null;
	}

	private static boolean containsPart(List<?> list, ConfigurationItem part) {
		for (Object entry : list) {
			if (entry == part) {
				return true;
			}
		}
		return false;
	}

	private static PropertyDescriptor descriptor(ConfigurationItem holder, String property) {
		PropertyDescriptor descriptor = holder.descriptor().getProperty(property);
		if (descriptor == null) {
			throw new IllegalArgumentException(
				"No property '" + property + "' in '" + holder.getConfigurationInterface().getName() + "'.");
		}
		return descriptor;
	}

	private static void bind(ViewExecutabilityRule rule, ViewContext context) {
		if (rule instanceof ContextDependentRule contextDependent) {
			contextDependent.bind(context);
		}
	}
}
