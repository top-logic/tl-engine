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
 * {@link CommandConfirmation} unconditionally displaying a generic confirm message.
 */
@InApp
public class DefaultConfirmation implements CommandConfirmation {

	/**
	 * Singleton {@link DefaultConfirmation} instance.
	 */
	public static final DefaultConfirmation INSTANCE = new DefaultConfirmation();

	private DefaultConfirmation() {
		// Singleton constructor.
	}

	@Override
	public ResKey getConfirmation(LayoutComponent component, ResKey commandLabel, Object model, Map<String, Object> arguments) {
		return model == null ? I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND.fill(commandLabel)
			: I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND_MODEL.fill(commandLabel, model);
	}

}
