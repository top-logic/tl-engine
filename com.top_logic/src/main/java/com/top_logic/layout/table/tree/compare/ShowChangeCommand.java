/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AbstractCommandHandler}, that steps through structure comparison changes, displayed by
 * {@link CompareTreeTableComponent}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class ShowChangeCommand extends AbstractCommandHandler {

	/**
	 * Create a new {@link ShowChangeCommand}.
	 */
	public ShowChangeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		CompareTreeTableComponent compareComponent = (CompareTreeTableComponent) aComponent;
		selectFollowingChange(compareComponent);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Action, that shall be performed to select following change.
	 */
	protected abstract void selectFollowingChange(CompareTreeTableComponent compareComponent);

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return new ExecutabilityRule() {

			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
					Map<String, Object> someValues) {
				return ExecutableState.EXECUTABLE;
			}
		};
	}
}
