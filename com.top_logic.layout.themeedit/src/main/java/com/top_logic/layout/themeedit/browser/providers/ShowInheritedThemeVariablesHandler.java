/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * Show inherited theme variables.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ShowInheritedThemeVariablesHandler extends ToggleCommandHandler {

	/**
	 * Creates a {@link ShowInheritedThemeVariablesHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ShowInheritedThemeVariablesHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(LayoutComponent component) {
		return VariableListModelBuilder.showInheritedThemeVariables(component);
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		VariableListModelBuilder.setShowInheritedThemeVariables(component, newValue);

		component.invalidate();
	}

}
