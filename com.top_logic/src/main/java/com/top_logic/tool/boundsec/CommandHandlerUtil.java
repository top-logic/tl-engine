/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
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

	/**
	 * Default confirm key for commands deleting objects.
	 * 
	 * @param toDelete
	 *        The object to delete. May be a collection of elements to delete.
	 * 
	 * @return {@link ResKey} to display in a confirm dialog.
	 */
	public static ResKey defaultDeletionConfirmKey(Object toDelete) {
		if (toDelete instanceof Collection<?>) {
			Collection<?> elements = (Collection<?>) toDelete;
			if (elements.size() == 1) {
				return I18NConstants.CONFIRM_DELETE_ONE_ELEMENT__ELEMENT.fill(elements.iterator().next());
			}
			String arg = elements
				.stream()
				.map(MetaLabelProvider.INSTANCE::getLabel)
				.collect(Collectors.joining(", "));
			return I18NConstants.CONFIRM_DELETE_MORE_ELEMENTS__ELEMENTS.fill(arg);
		}
		return I18NConstants.CONFIRM_DELETE_ONE_ELEMENT__ELEMENT.fill(toDelete);
	}

}
