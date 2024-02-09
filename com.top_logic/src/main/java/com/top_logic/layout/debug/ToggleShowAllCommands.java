/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.debug;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} making all commands visible.
 * 
 * <p>
 * For debugging only.
 * </p>
 * 
 * @see ExecutableState#allVisible()
 */
public class ToggleShowAllCommands extends ToggleCommandHandler {

	/**
	 * Creates a {@link ToggleShowAllCommands} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ToggleShowAllCommands(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(LayoutComponent component) {
		return ExecutableState.allVisible();
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		ExecutableState.setAllVisible(newValue);

		MainLayout mainLayout = component.getMainLayout();
		mainLayout.invalidate();
	}

}