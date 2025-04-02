/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.confirm;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Algorithm creating a confirm message before a command is executed.
 */
public interface CommandConfirmation {

	/**
	 * Creates the confirm message that should be displayed, before a command is executed on a given
	 * model.
	 *
	 * @param component
	 *        The context component.
	 * @param commandLabel
	 *        The name of the command that is being executed.
	 * @param model
	 *        The target model of the command.
	 * @param arguments
	 *        The command arguments.
	 * @return The message to display.
	 */
	ResKey getConfirmation(LayoutComponent component, ResKey commandLabel, Object model, Map<String, Object> arguments);

}
