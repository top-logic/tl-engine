/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.CommandPlacement;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandConfirmation;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Base class for {@link UIElement}s that establish a {@link CommandScope} and synchronize toolbar
 * buttons with a {@link ToolbarControl}.
 *
 * <p>
 * Subclasses implement {@link #createChromeControl(ViewContext, ReactControl)} to create their
 * specific chrome (panel, window, etc.). This base class handles:
 * </p>
 * <ul>
 * <li>Building {@link ViewCommandModel}s from configured commands</li>
 * <li>Creating a {@link CommandScope} and derived {@link ViewContext}</li>
 * <li>Creating child content controls</li>
 * <li>Synchronizing toolbar-placed commands as buttons on the chrome control</li>
 * <li>Cleanup of command models</li>
 * </ul>
 */
public abstract class CommandScopeElement extends ContainerElement {

	/**
	 * Configuration for {@link CommandScopeElement}.
	 */
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/**
		 * Commands scoped to this element.
		 *
		 * <p>
		 * These commands are available to child elements via the command scope. Commands with
		 * {@link CommandPlacement#TOOLBAR TOOLBAR} placement are automatically rendered in the
		 * element's toolbar.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
	}

	private final List<ViewCommand> _commands;

	private final List<ViewCommand.Config> _commandConfigs;

	/**
	 * Creates a new {@link CommandScopeElement}.
	 */
	protected CommandScopeElement(InstantiationContext context, Config config) {
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

	@Override
	public IReactControl createControl(ViewContext context) {
		// Phase 1: Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Phase 2: Create command scope and derived context.
		CommandScope scope = new CommandScope(commandModels);
		ViewContext derivedContext = context.withCommandScope(scope);

		// Phase 3: Create child content controls.
		ReactControl content = createContent(derivedContext);

		// Phase 4: Let subclass create the chrome control.
		ToolbarControl chrome = createChromeControl(derivedContext, content);

		// Phase 5: Sync toolbar buttons.
		Map<CommandModel, ReactButtonControl> toolbarButtons = new HashMap<>();
		syncToolbarButtons(chrome, context, scope, toolbarButtons);
		scope.addListener(() -> syncToolbarButtons(chrome, context, scope, toolbarButtons));

		// Phase 6: Register cleanup for model lifecycle.
		chrome.addCleanupAction(() -> {
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});

		return chrome;
	}

	/**
	 * Creates the chrome control wrapping the given content.
	 *
	 * @param context
	 *        The derived view context (with command scope set).
	 * @param content
	 *        The child content control.
	 * @return The chrome control (panel, window, etc.) that provides toolbar button support.
	 */
	protected abstract ToolbarControl createChromeControl(ViewContext context, ReactControl content);

	/**
	 * Creates a single content control from the children.
	 */
	protected ReactControl createContent(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);
		if (childControls.size() == 1) {
			return (ReactControl) childControls.get(0);
		}
		List<ReactControl> reactChildren = childControls.stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());
		return new ReactStackControl(context, reactChildren);
	}

	private List<ViewCommandModel> buildCommandModels(ViewContext context) {
		List<ViewCommandModel> models = new ArrayList<>();
		for (int i = 0; i < _commands.size() && i < _commandConfigs.size(); i++) {
			ViewCommand cmd = _commands.get(i);
			ViewCommand.Config cmdConfig = _commandConfigs.get(i);

			ChannelRef inputRef = cmdConfig.getInput();
			ViewChannel inputChannel = inputRef != null ? context.resolveChannel(inputRef) : null;

			ViewExecutabilityRule rule = buildExecutabilityRule(cmdConfig);
			ViewCommandConfirmation confirmation = buildConfirmation(cmdConfig);

			ViewCommandModel model = new ViewCommandModel(cmd, cmdConfig, inputChannel, rule, confirmation);
			model.attach();
			models.add(model);
		}
		return models;
	}

	private ViewExecutabilityRule buildExecutabilityRule(ViewCommand.Config cmdConfig) {
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs = cmdConfig.getExecutability();
		if (ruleConfigs.isEmpty()) {
			return input -> ExecutableState.EXECUTABLE;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(CommandScopeElement.class);
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewExecutabilityRule> ruleConfig : ruleConfigs) {
			ViewExecutabilityRule rule = instantiation.getInstance(ruleConfig);
			if (rule != null) {
				rules.add(rule);
			}
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	private ViewCommandConfirmation buildConfirmation(ViewCommand.Config cmdConfig) {
		PolymorphicConfiguration<? extends ViewCommandConfirmation> confirmConfig = cmdConfig.getConfirmation();
		if (confirmConfig == null) {
			return null;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(CommandScopeElement.class);
		return instantiation.getInstance(confirmConfig);
	}

	private void syncToolbarButtons(ToolbarControl chrome, ViewContext context,
			CommandScope scope, Map<CommandModel, ReactButtonControl> toolbarButtons) {
		List<CommandModel> currentCommands = scope.getAllCommands();

		toolbarButtons.entrySet().removeIf(entry -> {
			if (!currentCommands.contains(entry.getKey())) {
				chrome.removeToolbarButton(entry.getValue());
				return true;
			}
			return false;
		});

		for (CommandModel model : currentCommands) {
			if (!toolbarButtons.containsKey(model)
				&& CommandModel.PLACEMENT_TOOLBAR.equals(model.getPlacement())) {
				ReactButtonControl button = new ReactButtonControl(context, model);
				chrome.addToolbarButton(button);
				toolbarButtons.put(model, button);
			}
		}
	}
}
