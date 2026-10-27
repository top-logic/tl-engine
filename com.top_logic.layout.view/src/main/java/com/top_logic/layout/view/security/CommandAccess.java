/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.CommandArguments;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The {@link ViewCommand.Config#getAccess() access} a command declares: all its
 * {@link AccessCheck}s must pass.
 *
 * <p>
 * When one of the checks can never pass for the current user, the command is hidden. Otherwise,
 * the first failing check disables it with its reason.
 * </p>
 *
 * <p>
 * The access comes first among the rules of the command (see
 * {@link com.top_logic.layout.view.command.ViewExecutabilityRules#build(ViewCommand.Config, String, ViewContext)}),
 * so it decides both the state of the button and whether a dispatched command runs. It is
 * re-evaluated when an input of the command or an input of an enclosing unit whose object is
 * checked changes.
 * </p>
 */
public final class CommandAccess implements ViewExecutabilityRule, ObservableRule {

	private final List<AccessCheck> _checks;

	private CommandAccess(List<AccessCheck> checks) {
		_checks = checks;
	}

	/**
	 * Creates the access of a command displayed in the given context.
	 *
	 * <p>
	 * The checks are instantiated in the scope of the arguments of the command and bound to the
	 * context. Problems of their configuration - an operation not applying to the checked object,
	 * an unknown type, a reference that is no composition of the created type - are reported.
	 * </p>
	 *
	 * @param config
	 *        The configuration of the command.
	 * @param scope
	 *        The names of the arguments of the command, see {@link CommandArguments#scope()}.
	 * @param context
	 *        The build-time context of the command, {@code null} for a command built outside a
	 *        view.
	 * @return {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} for a command declaring no access.
	 * @throws ConfigurationError
	 *         If a check is invalid.
	 */
	public static ViewExecutabilityRule create(ViewCommand.Config config, ActionScope scope, ViewContext context) {
		return create(config.getAccess(), scope, context, "the command at " + config.location());
	}

	/**
	 * Creates the combination of the given access checks, e.g. the checks an element declares for
	 * an action performed on one of its objects.
	 *
	 * @param configs
	 *        The configured checks.
	 * @param scope
	 *        The names of the arguments the checks decide over.
	 * @param context
	 *        The build-time context the checks are bound to, {@code null} outside a view.
	 * @param owner
	 *        Description of the declaring place in messages.
	 * @return {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} for no checks.
	 * @throws ConfigurationError
	 *         If a check is invalid.
	 */
	public static ViewExecutabilityRule create(List<PolymorphicConfiguration<? extends AccessCheck>> configs,
			ActionScope scope, ViewContext context, String owner) {
		if (configs.isEmpty()) {
			return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
		}
		DefaultInstantiationContext problems = new DefaultInstantiationContext(CommandAccess.class);
		InstantiationContext instantiation = scope.bind(problems);
		List<AccessCheck> checks = new ArrayList<>(configs.size());
		for (PolymorphicConfiguration<? extends AccessCheck> checkConfig : configs) {
			AccessCheck check = instantiation.getInstance(checkConfig);
			if (check != null) {
				check.bind(problems, context);
				checks.add(check);
			}
		}
		if (problems.hasErrors()) {
			throw new ConfigurationError("Invalid access of " + owner + ".");
		}
		return new CommandAccess(checks);
	}

	@Override
	public Runnable observe(Runnable revalidate) {
		List<Runnable> stops = new ArrayList<>(_checks.size());
		for (AccessCheck check : _checks) {
			stops.add(check.observe(revalidate));
		}
		return () -> stops.forEach(Runnable::run);
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return decide(check -> check.isExecutable(input));
	}

	@Override
	public ExecutableState isExecutableFor(ActionArguments arguments) {
		return decide(check -> check.isExecutableFor(arguments));
	}

	private ExecutableState decide(Function<AccessCheck, ExecutableState> decision) {
		ExecutableState firstRefusal = null;
		for (AccessCheck check : _checks) {
			ExecutableState state = decision.apply(check);
			if (state.isHidden()) {
				return state;
			}
			if (!state.isExecutable() && firstRefusal == null) {
				firstRefusal = state;
			}
		}
		return firstRefusal != null ? firstRefusal : ExecutableState.EXECUTABLE;
	}

}
