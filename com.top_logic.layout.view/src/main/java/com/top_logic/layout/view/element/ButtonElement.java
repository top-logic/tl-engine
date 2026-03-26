/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandConfirmation;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link UIElement} that renders a button executing a {@link ViewCommand}.
 *
 * <p>
 * The command is configured inline within the {@code <button>} element. The button resolves input
 * channels, executability rules, and confirmation from the command configuration and creates a
 * {@link ViewCommandModel} to bridge the command to the UI.
 * </p>
 */
public class ButtonElement implements UIElement {

	/**
	 * Configuration for {@link ButtonElement}.
	 */
	@TagName("button")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ButtonElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getAction()}. */
		String ACTION = "action";

		/** Configuration name for {@link #getStyle()}. */
		String STYLE = "style";

		/** Configuration name for {@link #getSize()}. */
		String SIZE = "size";

		/**
		 * The command this button executes.
		 *
		 * <p>
		 * Configured as {@code <action class="..." .../>} inside the {@code <button>} element.
		 * </p>
		 */
		@Name(ACTION)
		@Nullable
		PolymorphicConfiguration<? extends ViewCommand> getAction();

		/**
		 * Button display style.
		 */
		@Name(STYLE)
		ButtonStyle getStyle();

		/**
		 * Button size.
		 */
		@Name(SIZE)
		ButtonSize getSize();
	}

	private final ViewCommand _command;

	private final ViewCommand.Config _commandConfig;

	private final Config _config;

	/**
	 * Creates a new {@link ButtonElement} from configuration.
	 */
	@CalledByReflection
	public ButtonElement(InstantiationContext context, Config config) {
		_config = config;
		PolymorphicConfiguration<? extends ViewCommand> actionConfig = config.getAction();
		if (actionConfig instanceof ViewCommand.Config) {
			_commandConfig = (ViewCommand.Config) actionConfig;
		} else {
			_commandConfig = null;
		}
		_command = context.getInstance(actionConfig);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		if (_command == null || _commandConfig == null) {
			// No command configured - render a static button.
			return new ReactButtonControl(context, "", ctx -> HandlerResult.DEFAULT_RESULT);
		}

		// Resolve input channel.
		ChannelRef inputRef = _commandConfig.getInput();
		ViewChannel inputChannel = inputRef != null ? context.resolveChannel(inputRef) : null;

		// Build executability rule.
		ViewExecutabilityRule rule = buildExecutabilityRule();

		// Build confirmation.
		ViewCommandConfirmation confirmation = buildConfirmation();

		// Create model and button. The button reads label/disabled from the model internally.
		ViewCommandModel model = new ViewCommandModel(_command, _commandConfig, inputChannel, rule, confirmation);
		model.attach();

		ReactButtonControl control = new ReactButtonControl(context, model);
		control.addCleanupAction(model::detach);
		return control;
	}

	private ViewExecutabilityRule buildExecutabilityRule() {
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs =
			_commandConfig.getExecutability();
		if (ruleConfigs.isEmpty()) {
			return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
		}
		DefaultInstantiationContext instantiation =
			new DefaultInstantiationContext(ButtonElement.class);
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewExecutabilityRule> ruleConfig : ruleConfigs) {
			ViewExecutabilityRule rule = instantiation.getInstance(ruleConfig);
			if (rule != null) {
				rules.add(rule);
			}
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	private ViewCommandConfirmation buildConfirmation() {
		PolymorphicConfiguration<? extends ViewCommandConfirmation> confirmConfig =
			_commandConfig.getConfirmation();
		if (confirmConfig == null) {
			return null;
		}
		DefaultInstantiationContext instantiation =
			new DefaultInstantiationContext(ButtonElement.class);
		return instantiation.getInstance(confirmConfig);
	}
}
