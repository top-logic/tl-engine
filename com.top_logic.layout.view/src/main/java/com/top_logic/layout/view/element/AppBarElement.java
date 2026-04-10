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

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.nav.ReactAppBarControl;
import com.top_logic.layout.react.control.nav.ReactAppBarControl.AppBarVariant;
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
import com.top_logic.util.Resources;

/**
 * UIElement that wraps {@link ReactAppBarControl}.
 *
 * <p>
 * Renders a top-level application bar with a title and optional variant/color configuration.
 * Commands configured in the {@code <commands>} section with {@link CommandPlacement#TOOLBAR
 * TOOLBAR} placement are automatically rendered as trailing action buttons in the app bar.
 * </p>
 */
public class AppBarElement implements UIElement {

	/**
	 * Configuration for {@link AppBarElement}.
	 */
	@TagName("app-bar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AppBarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getVariant()}. */
		String VARIANT = "variant";

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/**
		 * The bar title.
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * The visual variant.
		 */
		@Name(VARIANT)
		AppBarVariant getVariant();

		/**
		 * Commands to render as trailing action buttons in the app bar.
		 *
		 * <p>
		 * Commands with {@link CommandPlacement#TOOLBAR TOOLBAR} placement are rendered as
		 * icon/text buttons in the app bar's trailing actions area.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
	}

	private final ResKey _title;

	private final AppBarVariant _variant;

	private final List<ViewCommand> _commands;

	private final List<ViewCommand.Config> _commandConfigs;

	/**
	 * Creates a new {@link AppBarElement} from configuration.
	 */
	@CalledByReflection
	public AppBarElement(InstantiationContext context, Config config) {
		_title = config.getTitle();
		_variant = config.getVariant();

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
		String title = _title != null ? Resources.getInstance().getString(_title) : "";

		// Build command models.
		List<ViewCommandModel> commandModels = buildCommandModels(context);

		// Create command scope and derived context.
		CommandScope scope = new CommandScope(commandModels);
		ViewContext derivedContext = context.withCommandScope(scope);

		// Create the app bar control.
		ReactAppBarControl appBar = new ReactAppBarControl(derivedContext, title, _variant, null, List.of());

		// Sync toolbar-placed commands as action buttons.
		Map<CommandModel, ReactButtonControl> actionButtons = new HashMap<>();
		syncActionButtons(appBar, derivedContext, scope, actionButtons);
		scope.addListener(() -> syncActionButtons(appBar, derivedContext, scope, actionButtons));

		// Register cleanup for command model lifecycle.
		appBar.addCleanupAction(() -> {
			for (ViewCommandModel model : commandModels) {
				model.detach();
			}
		});

		return appBar;
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

	private static ViewExecutabilityRule buildExecutabilityRule(ViewCommand.Config cmdConfig) {
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs = cmdConfig.getExecutability();
		if (ruleConfigs.isEmpty()) {
			return input -> ExecutableState.EXECUTABLE;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(AppBarElement.class);
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewExecutabilityRule> ruleConfig : ruleConfigs) {
			ViewExecutabilityRule rule = instantiation.getInstance(ruleConfig);
			if (rule != null) {
				rules.add(rule);
			}
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	private static ViewCommandConfirmation buildConfirmation(ViewCommand.Config cmdConfig) {
		PolymorphicConfiguration<? extends ViewCommandConfirmation> confirmConfig = cmdConfig.getConfirmation();
		if (confirmConfig == null) {
			return null;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(AppBarElement.class);
		return instantiation.getInstance(confirmConfig);
	}

	private static void syncActionButtons(ReactAppBarControl appBar, ViewContext context,
			CommandScope scope, Map<CommandModel, ReactButtonControl> actionButtons) {
		List<CommandModel> currentCommands = scope.getAllCommands();

		actionButtons.entrySet().removeIf(entry -> {
			if (!currentCommands.contains(entry.getKey())) {
				appBar.removeToolbarButton(entry.getValue());
				return true;
			}
			return false;
		});

		for (CommandModel model : currentCommands) {
			if (!actionButtons.containsKey(model)
				&& CommandModel.PLACEMENT_TOOLBAR.equals(model.getPlacement())) {
				ReactButtonControl button = new ReactButtonControl(context, model);
				appBar.addToolbarButton(button);
				actionButtons.put(model, button);
			}
		}
	}
}
