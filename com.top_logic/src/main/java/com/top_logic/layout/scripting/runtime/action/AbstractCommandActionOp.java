/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.scripting.action.CommandAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * Base class for {@link ApplicationActionOp}s checking various aspects of commands.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractCommandActionOp<C extends CommandAction> extends CommandActionOpBase<C> {

	/**
	 * Creates a {@link AbstractCommandActionOp}.
	 */
	public AbstractCommandActionOp(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected CommandHandler getCommandHandler(ActionContext context, LayoutComponent component) {
		ApplicationAssertions.assertTrue(config,
			"Component '" + component.getName() + "' not visible.", component.isVisible());

		CommandHandler command;
		String commandName = config.getCommandName();
		if (commandName != null) {
			command = component.getCommandById(commandName);

			ApplicationAssertions.assertNotNull(config,
				"No command '" + commandName + "' in component '" + component.getName() + "', available commands are: "
					+ ids(component.getCommands()),
				command);
		} else {
			Resources resources = Resources.getInstance();
			String label = config.getCommandLabel();
			command = null;
			for (CommandHandler candidate : component.getCommands()) {
				ResKey candidateLabel = candidate.getResourceKey(component);
				if (StringServices.equals(label, resources.getStringOptional(candidateLabel))) {
					if (command != null) {
						ApplicationAssertions.fail(config,
							"Multiple comands named '" + label + "' in component '" + component.getName() + "' ()");
					}
					command = candidate;
				}
			}
			ApplicationAssertions.assertNotNull(config,
				"No command named '" + label + "' in component '" + component.getName() + "', available commands are: "
					+ component.getCommands().stream()
						.map(c -> resources.getStringOptional(c.getResourceKey(component)))
						.filter(Objects::nonNull).collect(Collectors.joining(", ")),
				command);
		}
		return command;
	}

	private static String ids(Collection<? extends CommandHandler> commands) {
		StringBuilder result = new StringBuilder();
		for (CommandHandler command : commands) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(command.getID());
		}
		return result.toString();
	}

}
