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
 * Throws away the unsaved changes of the tool access policy and shows the saved one again.
 *
 * @see ToolAccessPolicyComponent
 */
public class DiscardToolAccessPolicyCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link DiscardToolAccessPolicyCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();
	}

	/**
	 * Creates a {@link DiscardToolAccessPolicyCommand} from configuration.
	 */
	public DiscardToolAccessPolicyCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		return ((ToolAccessPolicyComponent) component).discardChanges();
	}

}
