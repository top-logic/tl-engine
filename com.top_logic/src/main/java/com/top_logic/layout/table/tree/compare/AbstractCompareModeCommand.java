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
 * {@link AbstractCommandHandler}, that switches {@link CompareTreeTableComponent} to specified
 * {@link CompareMode}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractCompareModeCommand extends AbstractCommandHandler {

	/**
	 * Create a new {@link AbstractCompareModeCommand}.
	 */
	public AbstractCompareModeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		((CompareTreeTableComponent) aComponent).setCompareMode(getExecutableCompareMode());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * {@link CompareMode}, when this command shall be executable.
	 */
	protected abstract CompareMode getExecutableCompareMode();

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return new ExecutabilityRule() {

			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
					Map<String, Object> someValues) {
				if (((CompareTreeTableComponent) aComponent).getCompareMode() != getExecutableCompareMode()) {
					return ExecutableState.EXECUTABLE;
				} else {
					return ExecutableState.NOT_EXEC_HIDDEN;
				}
			}
		};
	}


}
