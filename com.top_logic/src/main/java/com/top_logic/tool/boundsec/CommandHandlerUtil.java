/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Static utilities for {@link CommandHandler}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandHandlerUtil {

	/**
	 * Command argument to inject a target model into a command execution (bypassing the current
	 * component state).
	 */
	public static final String TARGET_MODEL_ARGUMENT = "TARGET_MODEL";

	public static HandlerResult handleCommand(CommandHandler handler, DisplayContext aContext,
			LayoutComponent aComponent, Map<String, Object> arguments) {
		return handler.handleCommand(aContext, aComponent, CommandHandlerUtil.getTargetModel(handler, aComponent, arguments), arguments);
	}

	/**
	 * The model object to invoke the given command.
	 */
	public static Object getTargetModel(CommandHandler commandHandler, LayoutComponent component,
			Map<String, Object> arguments) {
		Object result = arguments.get(CommandHandlerUtil.TARGET_MODEL_ARGUMENT);
		if (result != null) {
			return result;
		}

		return commandHandler.getTargetModel(component, arguments);
	}

	/**
	 * Creates a {@link ResKey} for the confirm message of a given {@link CommandHandler}.
	 */
	public static ResKey getConfirmKey(CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
		return command.getConfirmKey(component, arguments);
	}

}
