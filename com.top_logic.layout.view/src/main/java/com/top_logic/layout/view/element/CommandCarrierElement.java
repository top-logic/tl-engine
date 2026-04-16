/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandConfirmation;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;

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
		List<ViewCommandModel> models = new ArrayList<>();
		for (int i = 0; i < _commands.size() && i < _commandConfigs.size(); i++) {
			ViewCommand cmd = _commands.get(i);
			ViewCommand.Config cmdConfig = _commandConfigs.get(i);

			ChannelRef inputRef = cmdConfig.getInput();
			ViewChannel inputChannel = inputRef != null ? context.resolveChannel(inputRef) : null;

			ViewExecutabilityRule rule = buildExecutabilityRule(cmdConfig);
			ViewCommandConfirmation confirmation = buildConfirmation(cmdConfig);

			ViewCommandModel model = new ViewCommandModel(cmd, cmdConfig, inputChannel, rule, confirmation);
			models.add(model);
		}
		return models;
	}

	private ViewExecutabilityRule buildExecutabilityRule(ViewCommand.Config cmdConfig) {
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs = cmdConfig.getExecutability();
		if (ruleConfigs.isEmpty()) {
			return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(CommandCarrierElement.class);
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
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(CommandCarrierElement.class);
		return instantiation.getInstance(confirmConfig);
	}

	/**
	 * Registers attach/detach hooks for the given command models so they re-evaluate executability
	 * when input channels change.
	 */
	protected void registerLifecycle(List<ViewCommandModel> models, ReactControl host) {
		host.addBeforeWriteAction(() -> {
			for (ViewCommandModel model : models) {
				model.attach();
			}
		});
		host.addCleanupAction(() -> {
			for (ViewCommandModel model : models) {
				model.detach();
			}
		});
	}

	/**
	 * Convenience overload for {@link ToolbarControl}s (which are {@link ReactControl}s).
	 */
	protected void registerLifecycle(List<ViewCommandModel> models, ToolbarControl host) {
		registerLifecycle(models, (ReactControl) host);
	}

	/**
	 * Creates a single content {@link ReactControl} from the children, wrapping multiples in a
	 * {@link ReactStackControl}.
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
}
