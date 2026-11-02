/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewExecutabilityRule;

/**
 * One check of the {@link ViewCommand.Config#getAccess() access} a command declares: what the
 * command does in the model, decided by the model access rights of the current user.
 *
 * <p>
 * A check is instantiated in the {@link com.top_logic.layout.view.command.ActionScope scope} of
 * the arguments of its command, so that its scripts see them as variables, and then
 * {@link #bind(InstantiationContext, ViewContext) bound} to the context the command is displayed
 * in. It answers {@link com.top_logic.tool.execution.ExecutableState#NOT_EXEC_HIDDEN hidden} when
 * the user can never pass it, and disabled with the refusal as reason when it fails on the
 * current objects.
 * </p>
 *
 * @see CommandAccess
 */
public interface AccessCheck extends ViewExecutabilityRule, ObservableRule {

	/**
	 * Configuration of an {@link AccessCheck}.
	 */
	interface Config<I extends AccessCheck> extends PolymorphicConfiguration<I>, ConfigPart {
		// Base interface.
	}

	/**
	 * Binds this check to the context of its command, reporting configuration problems that depend
	 * on the context, e.g. an operation that does not apply to the object of the enclosing unit.
	 *
	 * @param problems
	 *        The context reporting configuration problems.
	 * @param context
	 *        The build-time context of the command, {@code null} for a command built outside a
	 *        view.
	 */
	void bind(InstantiationContext problems, ViewContext context);

}
