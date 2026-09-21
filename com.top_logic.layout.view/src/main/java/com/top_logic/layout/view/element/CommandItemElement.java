/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.sidebar.CommandItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.element.SidebarElement.ItemSite;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemConfig;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemElement;

/**
 * An item of a {@link SidebarElement} that runs a {@link ViewCommand} instead of leading somewhere.
 *
 * <p>
 * Logging out, switching the language, opening a dialog: an action that belongs into the navigation
 * although it displays no page of its own. Label and icon are the ones the command carries, so the
 * item reads like the button of the same command elsewhere.
 * </p>
 *
 * <p>
 * The item shows what the command's executability rules decide, for as long as the sidebar stands:
 * an item whose command is not offered right now is withheld or shown out of reach, and takes its
 * place back as soon as the rules allow it again.
 * </p>
 */
public class CommandItemElement implements SidebarItemElement {

	/**
	 * Configuration for {@link CommandItemElement}.
	 */
	@TagName("command-item")
	public interface Config extends SidebarItemConfig {

		@Override
		@ClassDefault(CommandItemElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();

		/** Configuration name for {@link #getAction()}. */
		String ACTION = "action";

		/**
		 * The command this item executes.
		 *
		 * <p>
		 * Configured as {@code <action class="..." .../>} inside the {@code <command-item>} element.
		 * Its label and icon are what the item displays, and its executability rules decide whether
		 * the item is offered.
		 * </p>
		 */
		@Name(ACTION)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ViewCommand> getAction();
	}

	private final String _id;

	private final ViewCommand _command;

	private final ViewCommand.Config _commandConfig;

	/**
	 * Creates a {@link CommandItemElement} from configuration.
	 */
	@CalledByReflection
	public CommandItemElement(InstantiationContext context, Config config) {
		_id = config.getId();
		PolymorphicConfiguration<? extends ViewCommand> actionConfig = config.getAction();
		_commandConfig = actionConfig instanceof ViewCommand.Config commandConfig ? commandConfig : null;
		_command = context.getInstance(actionConfig);
	}

	@Override
	public SidebarItem createSidebarItem(ViewContext context, ItemSite site) {
		if (_command == null || _commandConfig == null) {
			// Nothing to run: an item offering no action is no offer at all.
			return null;
		}
		ViewCommandModel model = ViewCommandModel.forCommand(context, _command, _commandConfig);

		ThemeImage image = model.getImage();
		CommandItem item = new CommandItem(_id, model.getLabel(), image == null ? null : image.toEncodedForm(),
			model::executeCommand);

		// The state the rules assign right now is part of what the sidebar is first sent.
		model.revalidate();
		item.setHidden(!model.isVisible());
		item.setDisabled(!model.isExecutable());

		site.addBinding(sidebar -> {
			sidebar.addAttachListener(() -> model.attach(context.getModelScope()));
			sidebar.addDetachListener(model::detach);

			Runnable stateListener = () -> {
				boolean hidden = !model.isVisible();
				boolean disabled = !model.isExecutable();
				if (hidden != item.isHidden() || disabled != item.isDisabled()) {
					item.setHidden(hidden);
					item.setDisabled(disabled);
					sidebar.refreshItems();
				}
			};
			model.addStateChangeListener(stateListener);
			sidebar.addCleanupAction(() -> model.removeStateChangeListener(stateListener));
		});

		return item;
	}

}
