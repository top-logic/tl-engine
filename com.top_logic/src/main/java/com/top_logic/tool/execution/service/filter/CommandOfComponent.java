/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ExecutionContextFilter} selecting a specific command of a specific component.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandOfComponent extends AbstractConfiguredInstance<CommandOfComponent.Config>
		implements ExecutionContextFilter {

	/**
	 * Configuration options for {@link CommandOfComponent}.
	 */
	@TagName("command-of-component")
	public interface Config extends PolymorphicConfiguration<CommandOfComponent> {

		/**
		 * The ID of the {@link CommandHandler} to match.
		 * 
		 * <p>
		 * If no command ID is given, all commands of the given {@link #getComponent()} match.
		 * </p>
		 */
		@Nullable
		String getCommandId();

		/**
		 * The name of the component to match.
		 * 
		 * <p>
		 * If no component name is given, all commands with the given {@link #getCommandId()} of all
		 * components match.
		 * </p>
		 */
		ComponentName getComponent();

	}

	private String _commandId;

	private ComponentName _componentName;

	/**
	 * Creates a {@link CommandOfComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandOfComponent(InstantiationContext context, Config config) {
		super(context, config);
		_commandId = config.getCommandId();
		_componentName = config.getComponent();
	}

	@Override
	public MatchResult matchesExecutionContext(LayoutComponent component, BoundCommandGroup commandGroup,
			String commandId) {
		// If the actual command ID is unknown (null), but a concrete command ID is checked, the
		// match result is UNDECIDED. This happens if general component access is checked (where no
		// command ID is available). In such a case, the final check must not match.
		if (commandId == null && _commandId != null) {
			return MatchResult.UNDECIDED;
		}

		return MatchResult.fromBoolean((_commandId == null || _commandId.equals(commandId))
			&& (_componentName == null || _componentName.equals(componentName(component))));
	}

	private ComponentName componentName(LayoutComponent component) {
		return component == null ? null : component.getName();
	}

}
