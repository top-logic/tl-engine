/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collections;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandSecurity;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Security check of a drop operation configured by a {@link DropSecurityConfig}.
 *
 * <p>
 * The drop is checked like a command of the configured {@link DropSecurityConfig#getGroup() command
 * group} with the drop's target object as model, see
 * {@link CommandSecurity#checkSecurity(LayoutComponent, BoundCommandGroup, String, Object, java.util.Map)}.
 * </p>
 */
public class DropSecurity {

	private final CommandGroupReference _groupRef;

	private final QueryExecutor _target;

	private BoundCommandGroup _group;

	/**
	 * Creates a {@link DropSecurity}.
	 *
	 * @param config
	 *        The security options of the drop.
	 */
	public DropSecurity(DropSecurityConfig config) {
		_groupRef = config.getGroup();

		Expr target = config.getTarget();
		_target = target == null ? null
			: QueryExecutor.compile(PersistencyLayer.getKnowledgeBase(), ModelService.getApplicationModel(), target);
	}

	/**
	 * The {@link BoundCommandGroup} whose permission is required for the drop.
	 */
	public BoundCommandGroup getGroup() {
		if (_group == null) {
			BoundCommandGroup group = _groupRef.resolve();
			if (group == null) {
				throw new IllegalStateException("Command group '" + _groupRef.id() + "' of a drop does not exist.");
			}
			_group = group;
		}
		return _group;
	}

	/**
	 * Computes the object on which the permission for a drop is checked.
	 *
	 * @param component
	 *        The component in which the drop happens.
	 * @param arguments
	 *        The arguments of the drop functions, the dragged objects followed by the
	 *        drop-position-specific arguments.
	 * @param defaultTarget
	 *        The target object if no {@link DropSecurityConfig#getTarget() target function} is
	 *        configured.
	 * @return The business object on which the permission is checked. A {@link TLTreeNode} is
	 *         unwrapped to its business object. If no target exists, the
	 *         component's model is returned.
	 */
	public Object resolveTarget(LayoutComponent component, Args arguments, Object defaultTarget) {
		Object target = _target == null ? defaultTarget : _target.executeWith(arguments);
		target = TLTreeModelUtil.getInnerBusinessObject(target);
		if (target == null) {
			return component.getModel();
		}
		return target;
	}

	/**
	 * Whether the current user is allowed to perform the drop.
	 *
	 * @param component
	 *        The component in which the drop happens.
	 * @param arguments
	 *        See {@link #resolveTarget(LayoutComponent, Args, Object)}.
	 * @param defaultTarget
	 *        See {@link #resolveTarget(LayoutComponent, Args, Object)}.
	 * @return The {@link ExecutableState} describing whether the drop is allowed.
	 */
	public ExecutableState check(LayoutComponent component, Args arguments, Object defaultTarget) {
		Object target = resolveTarget(component, arguments, defaultTarget);
		return CommandSecurity.checkSecurity(component, getGroup(), null, target, Collections.emptyMap());
	}

	/**
	 * Whether the current user is allowed to perform the drop.
	 *
	 * @see #check(LayoutComponent, Args, Object)
	 */
	public boolean isAllowed(LayoutComponent component, Args arguments, Object defaultTarget) {
		return check(component, arguments, defaultTarget).isExecutable();
	}

	/**
	 * Ensures that the current user is allowed to perform the drop.
	 *
	 * @throws TopLogicException
	 *         If the drop is not allowed.
	 *
	 * @see #check(LayoutComponent, Args, Object)
	 */
	public void checkAllowed(LayoutComponent component, Args arguments, Object defaultTarget)
			throws TopLogicException {
		ExecutableState state = check(component, arguments, defaultTarget);
		if (!state.isExecutable()) {
			ResKey reason = state.getI18NReasonKey();
			if (reason == null) {
				reason = ExecutableState.NO_EXEC_PERMISSION.getI18NReasonKey();
			}
			throw new TopLogicException(I18NConstants.ERROR_DROP_NOT_ALLOWED__REASON.fill(reason));
		}
	}

}
