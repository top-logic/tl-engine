/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Checks the edited tool access policy and writes it if the next application start would accept it.
 *
 * @see ToolAccessPolicyComponent
 */
/*
 * A command handler in the component toolbar rather than a button inside the form, because that is where
 * every other administration surface of the engine puts its apply.
 */
public class ApplyToolAccessPolicyCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link ApplyToolAccessPolicyCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();
	}

	/**
	 * Creates a {@link ApplyToolAccessPolicyCommand} from configuration.
	 */
	public ApplyToolAccessPolicyCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		return ((ToolAccessPolicyComponent) component).applyPolicy();
	}

}
