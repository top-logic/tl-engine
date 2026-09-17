/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ButtonSize;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link UIElement} that renders a button executing a {@link ViewCommand}.
 *
 * <p>
 * The command is configured inline within the {@code <button>} element. The
 * {@link ViewCommandModel} bridging it to the UI - with the command's input channel resolved and
 * its executability rules built - comes from the construction path every command-hosting element
 * shares.
 * </p>
 */
@InApp
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

		/** Configuration name for {@link #getAppearance()}. */
		String APPEARANCE = "appearance";

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
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ViewCommand> getAction();

		/**
		 * Button appearance (e.g. {@link ButtonAppearance#LINK} to render as an inline text link).
		 */
		@Name(APPEARANCE)
		ButtonAppearance getAppearance();

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

		// Create model and button. The button reads label/disabled from the model internally.
		ViewCommandModel model = ViewCommandModel.forCommand(context, _command, _commandConfig);

		ReactButtonControl control = new ReactButtonControl(context, model);
		if (_commandConfig.getImage() != null) {
			control.setDisplayMode(ButtonDisplayMode.ICON_LABEL);
		}
		control.setAppearance(_config.getAppearance());
		control.setSize(_config.getSize());
		control.addAttachListener(() -> model.attach(context.getModelScope()));
		control.addDetachListener(model::detach);
		return control;
	}
}
