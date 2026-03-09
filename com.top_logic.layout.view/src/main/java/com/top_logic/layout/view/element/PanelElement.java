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

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandConfirmation;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * UIElement that wraps {@link ReactPanelControl}.
 *
 * <p>
 * Renders a titled panel with a toolbar header. If multiple children are configured, they are
 * wrapped in a {@link ReactStackControl}. Commands configured in the {@code <commands>} section are
 * available to child elements via {@link CommandScope} and toolbar-placed commands are automatically
 * rendered as toolbar buttons.
 * </p>
 */
public class PanelElement extends ContainerElement {

	/**
	 * Configuration for {@link PanelElement}.
	 */
	@TagName("panel")
	public interface Config extends ContainerElement.Config {

		@Override
		@ClassDefault(PanelElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/**
		 * The panel title displayed in the toolbar header.
		 */
		@Name(TITLE)
		String getTitle();

		/**
		 * Commands scoped to this panel.
		 *
		 * <p>
		 * These commands are available to child elements via
		 * {@link CommandScope#resolveCommand(String)}. Commands with
		 * {@link CommandPlacement#TOOLBAR TOOLBAR} placement are automatically rendered in the
		 * panel's toolbar.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
	}

	private final String _title;

	private final List<ViewCommand> _commands;

	private final List<ViewCommand.Config> _commandConfigs;

	/**
	 * Creates a new {@link PanelElement} from configuration.
	 */
	@CalledByReflection
	public PanelElement(InstantiationContext context, Config config) {
		super(context, config);
		_title = config.getTitle();

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
		List<ViewCommandModel> commandModels = new ArrayList<>();
		for (int i = 0; i < _commands.size() && i < _commandConfigs.size(); i++) {
			ViewCommand cmd = _commands.get(i);
			ViewCommand.Config cmdConfig = _commandConfigs.get(i);

			// Resolve input channel.
			ChannelRef inputRef = cmdConfig.getInput();
			ViewChannel inputChannel = inputRef != null ? context.resolveChannel(inputRef) : null;

			// Build executability rule.
			ViewExecutabilityRule rule = buildExecutabilityRule(cmdConfig);

			// Build confirmation.
			ViewCommandConfirmation confirmation = buildConfirmation(cmdConfig);

			// Create model (attach is deferred to button construction).
			ViewCommandModel model = new ViewCommandModel(cmd, cmdConfig, inputChannel, rule, confirmation);
			model.attach();
			commandModels.add(model);
		}

		// Phase 2: Create command scope and derived context.
		CommandScope scope = new CommandScope(commandModels);
		ViewContext derivedContext = context.withCommandScope(scope);

		// Phase 3: Create child controls using derived context.
		List<IReactControl> childControls = createChildControls(derivedContext);

		ReactControl content;
		if (childControls.size() == 1) {
			content = (ReactControl) childControls.get(0);
		} else {
			List<ReactControl> reactChildren = childControls.stream()
				.map(c -> (ReactControl) c)
				.collect(Collectors.toList());
			content = new ReactStackControl(context, reactChildren);
		}

		// Phase 4: Create panel with toolbar buttons for all TOOLBAR commands in scope.
		ReactPanelControl panel = new ReactPanelControl(context, _title, content, false, false, false);

		Map<CommandModel, ReactButtonControl> toolbarButtons = new HashMap<>();
		syncToolbarButtons(panel, context, scope, toolbarButtons);

		// React to scope changes (children adding/removing commands).
		scope.addListener(() -> syncToolbarButtons(panel, context, scope, toolbarButtons));

		// Phase 5: Register cleanup for model lifecycle.
		panel.addCleanupAction(() -> {
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});

		return panel;
	}

	private ViewExecutabilityRule buildExecutabilityRule(ViewCommand.Config cmdConfig) {
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs = cmdConfig.getExecutability();
		if (ruleConfigs.isEmpty()) {
			return input -> ExecutableState.EXECUTABLE;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(PanelElement.class);
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
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(PanelElement.class);
		return instantiation.getInstance(confirmConfig);
	}

	private void syncToolbarButtons(ReactPanelControl panel, ViewContext context,
			CommandScope scope, Map<CommandModel, ReactButtonControl> toolbarButtons) {
		List<CommandModel> currentCommands = scope.getAllCommands();

		// Remove buttons for commands no longer in scope.
		toolbarButtons.entrySet().removeIf(entry -> {
			if (!currentCommands.contains(entry.getKey())) {
				panel.removeToolbarButton(entry.getValue());
				return true;
			}
			return false;
		});

		// Add buttons for new TOOLBAR commands.
		for (CommandModel model : currentCommands) {
			if (!toolbarButtons.containsKey(model)
				&& CommandModel.PLACEMENT_TOOLBAR.equals(model.getPlacement())) {
				ReactButtonControl button = new ReactButtonControl(context, model);
				panel.addToolbarButton(button);
				toolbarButtons.put(model, button);
			}
		}
	}
}
