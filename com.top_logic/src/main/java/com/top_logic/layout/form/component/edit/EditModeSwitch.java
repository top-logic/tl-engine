/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.edit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * {@link CommandHandler} toggling the {@link EditMode#isEditing() edit mode} state of a
 * {@link LayoutComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EditModeSwitch extends ToggleCommandHandler {

	/**
	 * Creates a {@link EditModeSwitch} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EditModeSwitch(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(DisplayContext context, LayoutComponent component) {
		if (component == null) {
			// Note: Required for static resource checking during startup.
			return false;
		}
		return ((EditMode) component).isEditing();
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		((EditMode) component).setEditMode(newValue);
	}

}
