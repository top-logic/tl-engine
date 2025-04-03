/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.confirm;

import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.I18NConstants;

/**
 * {@link CommandConfirmation} that shows a generic confirm message.
 * 
 * @implNote This class is used as annotated default and therefore must not have any configurable
 *           options. Changing values of default items is not supported by the layout editor.
 */
@InApp
public class DefaultConfirmation implements CommandConfirmation {

	/**
	 * Singleton {@link DefaultConfirmation} instance.
	 */
	public static final DefaultConfirmation INSTANCE = new DefaultConfirmation();

	/**
	 * Creates a {@link DefaultConfirmation}.
	 */
	protected DefaultConfirmation() {
		// Singleton constructor.
	}

	@Override
	public ResKey getConfirmation(LayoutComponent component, ResKey commandLabel, Object model, Map<String, Object> arguments) {
		return getDefaultConfirmation(component, commandLabel, model, arguments);
	}

	/**
	 * Provides a default confirmation message, if there is none configured.
	 * 
	 * @param component
	 *        See {@link #getConfirmation(LayoutComponent, ResKey, Object, Map)}
	 * @param arguments
	 *        See {@link #getConfirmation(LayoutComponent, ResKey, Object, Map)}
	 */
	protected ResKey getDefaultConfirmation(LayoutComponent component, ResKey commandLabel, Object model,
			Map<String, Object> arguments) {
		return model == null ? I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND.fill(commandLabel)
			: I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND_MODEL.fill(commandLabel, model);
	}

}
