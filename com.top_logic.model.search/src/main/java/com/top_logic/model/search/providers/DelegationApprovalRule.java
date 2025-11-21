/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Map;

import com.google.common.base.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.AbstractConfiguredApprovalRule;
import com.top_logic.tool.execution.service.CommandApprovalRule;
import com.top_logic.tool.execution.service.CommandApprovalService;

/**
 * {@link CommandApprovalRule} that delegates to another check.
 */
@InApp
public class DelegationApprovalRule extends AbstractConfiguredApprovalRule<DelegationApprovalRule.Config<?>> {

	/**
	 * Configuration options for {@link DelegationApprovalRule}.
	 */
	@TagName("delegation-rule")
	public interface Config<I extends DelegationApprovalRule> extends AbstractConfiguredApprovalRule.Config<I> {

		/**
		 * The other {@link BoundCommandGroup} to check.
		 * 
		 * <p>
		 * If not given, the original group is checked.
		 * </p>
		 */
		CommandGroupReference getCommandGroup();

		/**
		 * Function taking the target model as single argument and computing another object on which
		 * the check should take place.
		 */
		Expr getTransformation();
	}

	private QueryExecutor _transformation;

	private BoundCommandGroup _checkGroup;

	/**
	 * Creates a {@link DelegationApprovalRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DelegationApprovalRule(InstantiationContext context, Config<?> config) {
		super(context, config);

		_checkGroup = config.getCommandGroup() == null ? null : config.getCommandGroup().resolve();
		_transformation = QueryExecutor.compileOptional(config.getTransformation());
	}

	@Override
	protected ExecutableState applyRule(LayoutComponent component, BoundCommandGroup commandGroup, String commandId,
			Object model, Map<String, Object> arguments) {
		BoundCommandGroup checkGroup = _checkGroup != null ? _checkGroup : commandGroup;
		Object checkModel = _transformation == null ? model : _transformation.execute(model);

		if (checkGroup.equals(commandGroup) && Objects.equal(checkModel, model)) {
			// Make stack overflow less likely.
			return ExecutableState.EXECUTABLE;
		}

		return CommandApprovalService.getInstance().isExecutable(component, checkGroup, commandId, checkModel,
			arguments);
	}

}
