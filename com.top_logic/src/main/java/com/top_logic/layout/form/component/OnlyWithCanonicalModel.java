/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that hides a command, if it is not executed on its canonical model
 * (selected by {@link CommandHandler#getTargetModel(LayoutComponent, Map)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class OnlyWithCanonicalModel implements ExecutabilityRule {
	private AbstractCommandHandler _handler;

	/**
	 * Creates a {@link OnlyWithCanonicalModel}.
	 *
	 * @param handler
	 *        The {@link CommandHandler} that is checked.
	 */
	public OnlyWithCanonicalModel(AbstractCommandHandler handler) {
		_handler = handler;
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
			Map<String, Object> someValues) {

		// Note: Here is not the actual target model requested (this is passed as the model
		// argument) but the target model that the command would normally select when
		// executed on a component.
		@SuppressWarnings("deprecation")
		Object canonicalTargetModel = _handler.getTargetModel(aComponent, someValues);

		if (model == canonicalTargetModel) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}
}