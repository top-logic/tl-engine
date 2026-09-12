/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;

/**
 * Abstract base for {@link UIElement}s that carry {@link ViewCommand} configurations.
 *
 * <p>
 * Responsible for loading command configs, building {@link ViewCommandModel}s, merging child
 * elements into a single content {@link ReactControl}, and managing attach/detach lifecycle of the
 * command models.
 * </p>
 *
 * <p>
 * Does NOT create a {@link com.top_logic.layout.view.command.CommandScope} or any chrome.
 * Subclasses decide what to do with the resulting model list: {@link CommandScopeElement} wraps
 * them in a shared scope and adds toolbar chrome; {@link ContextMenuElement} uses them to populate
 * a context-menu region.
 * </p>
 */
public abstract class CommandCarrierElement extends ContainerElement {

	/**
	 * Configuration for {@link CommandCarrierElement}.
	 */
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/**
		 * Commands declared on this element.
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
	}

	private final List<ViewCommand> _commands;

	private final List<ViewCommand.Config> _commandConfigs;

	/**
	 * Creates a new {@link CommandCarrierElement}.
	 */
	protected CommandCarrierElement(InstantiationContext context, Config config) {
		super(context, config);

		_commands = new ArrayList<>();
		_commandConfigs = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewCommand> cmdConfig : config.getCommands()) {
			ViewCommand cmd = context.getInstance(cmdConfig);
			if (cmd != null) {
				_commands.add(cmd);
				if (cmdConfig instanceof ViewCommand.Config) {
					_commandConfigs.add((ViewCommand.Config) cmdConfig);
				}
			}
		}
	}

	/**
	 * Builds {@link ViewCommandModel}s for all configured commands, resolving per-command input
	 * channels, executability rules and confirmations.
	 */
	protected List<ViewCommandModel> buildCommandModels(ViewContext context) {
		return buildCommandModels(context, _commands, _commandConfigs);
	}

	/**
	 * Builds {@link ViewCommandModel}s for the given commands, resolving per-command input
	 * channels, executability rules and confirmations.
	 *
	 * @param commands
	 *        The instantiated commands.
	 * @param commandConfigs
	 *        Their configurations, in the same order.
	 */
	protected static List<ViewCommandModel> buildCommandModels(ViewContext context,
			List<ViewCommand> commands, List<ViewCommand.Config> commandConfigs) {
		return ViewCommands.buildCommandModels(context, commands, commandConfigs);
	}

	/**
	 * Registers attach/detach hooks for the given command models, so that they follow their input -
	 * the channel value and the object it holds - while the host is displayed.
	 *
	 * @param context
	 *        The context whose {@link ViewContext#getModelScope() model scope} carries the object
	 *        observation; read when the host attaches.
	 */
	protected void registerLifecycle(ViewContext context, List<ViewCommandModel> models, ReactControl host) {
		ViewCommands.registerLifecycle(context, models, host);
	}

	/**
	 * Convenience overload for {@link ToolbarControl}s (which are {@link ReactControl}s).
	 */
	protected void registerLifecycle(ViewContext context, List<ViewCommandModel> models, ToolbarControl host) {
		registerLifecycle(context, models, (ReactControl) host);
	}

	/**
	 * Creates a single content {@link ReactControl} from the children, wrapping multiples in a
	 * {@link com.top_logic.layout.react.control.layout.ReactStackControl}.
	 */
	protected ReactControl createContent(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);
		return ContentControls.combine(context, childControls);
	}
}
