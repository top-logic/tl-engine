/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Command handler to open the {@link EnableMultiFactorAuthenticationDialog} for a person.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Enable multi-factor authentication")
public class EnableMFACommand extends AbstractCommandHandler {

	/**
	 * Creates a new {@link EnableMFACommand}.
	 */
	public EnableMFACommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), (component, model, arguments) -> {
			if (model instanceof Person account) {
				if (account.getMFARequirement() != MfaRequirement.DISABLED) {
					return ExecutableState.EXECUTABLE;
				}
			}
			return ExecutableState.NOT_EXEC_HIDDEN;
		});
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return new EnableMultiFactorAuthenticationDialog((Person) model, Command.DO_NOTHING).open(aContext);
	}

}

