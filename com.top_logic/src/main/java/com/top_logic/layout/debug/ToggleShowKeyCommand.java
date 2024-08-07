/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.debug;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * Command that toggles show keys in the resources.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToggleShowKeyCommand extends ToggleCommandHandler {

	/**
	 * Creates a {@link ToggleShowKeyCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ToggleShowKeyCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(LayoutComponent component) {
		return ResourcesModule.getInstance().isAlwaysShowKeys();
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		ResourcesModule.getInstance().setAlwaysShowKeys(newValue);
		ReloadI18NCommand.reload();
	}

}