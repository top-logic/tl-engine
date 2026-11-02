/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.FloatingActionPosition;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.button.ReactFloatingActionControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.util.Resources;

/**
 * A {@link UIElement} offering its {@link ViewCommand}s as actions floating over a corner of the
 * body of the enclosing panel or window.
 *
 * <p>
 * One action is shown as a round floating action button, several as a speed dial opened from such
 * a button, with the labels of the actions as their tooltips. The actions stay in their corner
 * while the body scrolls; outside any panel or window, they float over the corner of the browser
 * window.
 * </p>
 *
 * <p>
 * The commands are configured inline, like the action of a {@code <button>} ({@link ButtonElement}),
 * and their models come from the construction path every command-hosting element shares, so each
 * action is hidden or disabled by its command's own rules.
 * </p>
 *
 * @see ReactFloatingActionControl
 */
@InApp
public class FloatingActionElement implements UIElement {

	/**
	 * Configuration for {@link FloatingActionElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-plus-circle-fill")
	public interface Config extends UIElement.Config {

		/** Configuration tag of a {@link FloatingActionElement}. */
		String TAG_NAME = "floating-action";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		/** Configuration name for {@link #getPosition()}. */
		String POSITION = "position";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		@Override
		@ClassDefault(FloatingActionElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The commands offered, at least one.
		 *
		 * <p>
		 * Each is written directly inside the element, as {@code <action class="..."/>} or by the tag
		 * of its command. In a speed dial, the first stands next to the button opening it.
		 * </p>
		 */
		@Name(ACTIONS)
		@DefaultContainer
		@EntryTag("action")
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewCommand>> getActions();

		/**
		 * The corner of the body of the enclosing panel or window the actions float over.
		 */
		@Name(POSITION)
		FloatingActionPosition getPosition();

		/**
		 * The icon of the button opening the speed dial of several actions.
		 *
		 * <p>
		 * Without a value, a plus sign is shown. A single action is shown with the icon of its
		 * command instead.
		 * </p>
		 */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();

		/**
		 * The label of the button opening the speed dial of several actions: its tooltip and the
		 * name assistive technology announces it by.
		 *
		 * <p>
		 * Without a value, a generic label is used. A single action is shown with the label of its
		 * command instead.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();
	}

	private final Config _config;

	private final List<ViewCommand> _commands = new ArrayList<>();

	private final List<ViewCommand.Config> _commandConfigs = new ArrayList<>();

	/**
	 * Creates a {@link FloatingActionElement} from configuration.
	 */
	@CalledByReflection
	public FloatingActionElement(InstantiationContext context, Config config) {
		_config = config;
		for (PolymorphicConfiguration<? extends ViewCommand> actionConfig : config.getActions()) {
			if (!(actionConfig instanceof ViewCommand.Config commandConfig)) {
				context.error("An action of <" + Config.TAG_NAME + "> must be a view command, got: " + actionConfig);
				continue;
			}
			ViewCommand command = context.getInstance(commandConfig);
			if (command != null) {
				_commands.add(command);
				_commandConfigs.add(commandConfig);
			}
		}
		if (config.getActions().isEmpty()) {
			context.error("A <" + Config.TAG_NAME + "> requires at least one action.");
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewCommandModel> models = ViewCommands.buildCommandModels(context, _commands, _commandConfigs);
		List<ReactButtonControl> actions = new ArrayList<>(models.size());
		for (ViewCommandModel model : models) {
			actions.add(new ReactButtonControl(context, model));
		}

		ReactFloatingActionControl control = new ReactFloatingActionControl(context, actions);
		control.setPosition(_config.getPosition());
		control.setTriggerImage(_config.getImage());
		ResKey label = _config.getLabel();
		control.setTriggerLabel(label == null ? null : Resources.getInstance().getString(label));
		control.setCssClass(_config.getCssClass());
		ViewCommands.registerLifecycle(context, models, control);
		return control;
	}
}
