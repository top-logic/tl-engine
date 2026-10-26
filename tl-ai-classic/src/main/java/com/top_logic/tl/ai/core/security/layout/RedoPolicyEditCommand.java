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
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Goes forward one undone step in the editing of the tool access policy.
 *
 * @see ToolAccessPolicyComponent
 */
public class RedoPolicyEditCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link RedoPolicyEditCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();
	}

	/**
	 * Creates a {@link RedoPolicyEditCommand} from configuration.
	 */
	public RedoPolicyEditCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		return ((ToolAccessPolicyComponent) component).redo();
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return (component, model, values) -> ((ToolAccessPolicyComponent) component).session().canRedo()
			? ExecutableState.EXECUTABLE
			: ExecutableState.createDisabledState(I18NConstants.NOTHING_TO_REDO);
	}

}
