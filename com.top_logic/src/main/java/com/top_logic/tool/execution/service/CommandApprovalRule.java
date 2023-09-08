/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Algorithm to check whether a {@link CommandHandler} can be executed in a certain context on a
 * certain target model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CommandApprovalRule {

	/**
	 * The trivial checker that allows all executions.
	 */
	CommandApprovalRule ALLOW = new CommandApprovalRule() {
		@Override
		public ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup,
				String commandId, Object model, Map<String, Object> arguments) {
			return ExecutableState.EXECUTABLE;
		}

		@Override
		public CommandApprovalRule combine(CommandApprovalRule other) {
			return other;
		}
	};

	/**
	 * Checks whether a command described by the given parameters can be executed.
	 *
	 * @param component
	 *        The context component in which the command should be executed. A value of
	 *        <code>null</code> means to check whether a command with the other parameter value can
	 *        be executed in some component.
	 * @param commandGroup
	 *        The command group of the executed command.
	 * @param commandId
	 *        The concrete {@link CommandHandler#getID() ID} of the executed command. A value of
	 *        <code>null</code> means to check, if some command with the given command group can be
	 *        executed.
	 * @param model
	 *        The target model of the command.
	 * @param arguments
	 *        The concrete arguments passed to the command, normally empty.
	 * @return A decision whether a described command can be executed.
	 */
	ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup, String commandId,
			Object model, Map<String, Object> arguments);

	/**
	 * Combines this {@link CommandApprovalRule} with the given one.
	 * 
	 * <p>
	 * The resulting checker performs all checks of this and the given one.
	 * </p>
	 */
	default CommandApprovalRule combine(CommandApprovalRule other) {
		if (other == ALLOW) {
			return this;
		}
		CommandApprovalRule self = this;
		return new CommandApprovalRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup,
					String commandId, Object model, Map<String, Object> arguments) {
				ExecutableState s1 = self.isExecutable(component, commandGroup, commandId, model, arguments);
				if (s1.isHidden()) {
					return s1;
				}
				ExecutableState s2 = other.isExecutable(component, commandGroup, commandId, model, arguments);
				return s1.combine(s2);
			}
		};
	}

}