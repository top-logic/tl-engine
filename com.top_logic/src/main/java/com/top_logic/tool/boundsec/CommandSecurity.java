/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;
import java.util.function.Predicate;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.TLContext;

/**
 * Security check for an operation of a certain {@link BoundCommandGroup} that is performed in the
 * context of a {@link LayoutComponent}.
 *
 * <p>
 * The check consists of the following steps:
 * </p>
 * <ol>
 * <li>An operation of a {@link BoundCommandGroup#isSystemGroup() system group} is always
 * allowed.</li>
 * <li>The {@link CommandApprovalService} must approve the operation.</li>
 * <li>The current user must be allowed to execute operations of the group at all (restricted
 * users are only allowed to execute some groups), see
 * {@link SimpleBoundCommandGroup#isAllowedCommandGroup(Person, BoundCommandGroup)}.</li>
 * <li>The current user must have a role on the security object of the operation that grants the
 * group.</li>
 * </ol>
 *
 * <p>
 * The last two steps are only performed, if the context component is a {@link BoundChecker}.
 * </p>
 *
 * @see com.top_logic.mig.html.layout.CommandDispatcher#resolveExecutableState(CommandHandler,
 *      LayoutComponent, Map)
 */
public final class CommandSecurity {

	private CommandSecurity() {
		// Static utility.
	}

	/**
	 * Checks whether the current user may perform an operation of the given group on the given
	 * model in the context of the given component.
	 *
	 * <p>
	 * The rights are checked on the security object that the component (as {@link BoundChecker})
	 * {@link BoundChecker#getSecurityObject(BoundCommandGroup, Object) computes} for the given
	 * model.
	 * </p>
	 *
	 * @see #checkSecurity(LayoutComponent, BoundCommandGroup, String, Object, Map, Predicate)
	 */
	public static ExecutableState checkSecurity(LayoutComponent component, BoundCommandGroup group,
			String commandId, Object model, Map<String, Object> arguments) {
		return checkSecurity(component, group, commandId, model, arguments,
			checker -> BoundChecker.allowCommand(checker, group, model));
	}

	/**
	 * Checks whether the current user may perform an operation of the given group on the given
	 * model in the context of the given component.
	 *
	 * @param component
	 *        The context component in which the operation is performed.
	 * @param group
	 *        The {@link BoundCommandGroup} of the operation.
	 * @param commandId
	 *        The ID of the performed command, or <code>null</code>, if the operation is not a
	 *        {@link CommandHandler command}.
	 * @param model
	 *        The target model of the operation.
	 * @param arguments
	 *        The arguments of the operation.
	 * @param rightsCheck
	 *        Check of the current user's rights for the operation in the context of the given
	 *        component (as {@link BoundChecker}). The check is responsible for resolving the
	 *        security object of the operation.
	 * @return The {@link ExecutableState} describing whether the operation is allowed.
	 */
	public static ExecutableState checkSecurity(LayoutComponent component, BoundCommandGroup group,
			String commandId, Object model, Map<String, Object> arguments, Predicate<BoundChecker> rightsCheck) {
		if (group.isSystemGroup()) {
			return ExecutableState.EXECUTABLE;
		}

		ExecutableState approvalState =
			CommandApprovalService.getInstance().isExecutable(component, group, commandId, model, arguments);
		if (!approvalState.isExecutable()) {
			return approvalState;
		}

		if (component instanceof BoundChecker) {
			Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
			if (!SimpleBoundCommandGroup.isAllowedCommandGroup(currentPerson, group)) {
				return ExecutableState.NO_EXEC_RESTRICTED_USER;
			}

			if (!rightsCheck.test((BoundChecker) component)) {
				return ExecutableState.NO_EXEC_PERMISSION;
			}
		}

		return ExecutableState.EXECUTABLE;
	}

}
