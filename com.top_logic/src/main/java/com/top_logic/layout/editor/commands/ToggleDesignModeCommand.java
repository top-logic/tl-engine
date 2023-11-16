/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * Toggles the application between design mode and normal mode.
 * 
 * <p>
 * In design mode, the views of the application can be edited in a WYSIWYG manner. Tab bars get
 * additional commands to add/delete or rearrange tabs. Component configurations can be edited with
 * the context menu command {@link EditComponentCommand}. Views can be split to add new components
 * with the {@link EditLayoutCommand}.
 * </p>
 * 
 * @implNote The mode applies actually to the current {@link SubSessionContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToggleDesignModeCommand extends ToggleCommandHandler {

	private static final Property<Boolean> MODE_PROPERTY =
		TypedAnnotatable.property(Boolean.class, "in-design-mode", Boolean.FALSE);

	/**
	 * Creates a new {@link ToggleDesignModeCommand}.
	 */
	public ToggleDesignModeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(DisplayContext context, LayoutComponent component) {
		return ToggleDesignModeCommand.isInDesignMode(context.getSubSessionContext());
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		ToggleDesignModeCommand.setDesignMode(context.getSubSessionContext(), newValue);
		component.getMainLayout().invalidate();
	}

	/**
	 * Whether the given {@link SubSessionContext} is currently in design mode.
	 */
	public static boolean isInDesignMode(SubSessionContext subsession) {
		return subsession.get(MODE_PROPERTY);
	}

	/**
	 * Sets the value of {@link #isInDesignMode(SubSessionContext)}.
	 */
	public static void setDesignMode(SubSessionContext subsession, Boolean mode) {
		subsession.set(MODE_PROPERTY, mode);
	}

}

