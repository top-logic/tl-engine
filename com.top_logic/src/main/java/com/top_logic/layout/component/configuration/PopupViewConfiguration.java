/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.form.control.IButtonRenderer;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl.HorizontalPopupPosition;
import com.top_logic.layout.structure.PopupDialogControl.VerticalPopupPosition;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Resources;

/**
 * {@link ViewConfiguration} that displays a pop-up menu with commands configured by a list of
 * {@link CommandModelConfiguration}s.
 * 
 * @see MultiViewConfiguration
 */
public class PopupViewConfiguration extends AbstractConfiguredInstance<PopupViewConfiguration.Config>
		implements ViewConfiguration {

	/**
	 * Configuration options for {@link PopupViewConfiguration}.
	 */
	public static interface Config extends PolymorphicConfiguration<PopupViewConfiguration> {
		
		/**
		 * @see #getLabel()
		 */
		String LABEL = "label";

		/**
		 * @see #getIcon()
		 */
		String ICON = "icon";

		/**
		 * @see #getCssClass()
		 */
		String CSS_CLASS = "cssClass";

		/**
		 * @see #getRenderer()
		 */
		String RENDERER = "renderer";

		/**
		 * @see #getHorizontalPosition()
		 */
		String HORIZONTAL_POSITION = "horizontalPosition";

		/**
		 * @see #getVerticalPosition()
		 */
		String VERTICAL_POSITION = "verticalPosition";

		/**
		 * @see #getCommands()
		 */
		String COMMAND_CONFIGURATIONS = "commandConfigurations";

		/**
		 * The label of the button opening the popup dialog.
		 * 
		 * @see #getRenderer()
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * The icon of the button opening the popup dialog.
		 * 
		 * @see #getRenderer()
		 */
		@Name(ICON)
		ThemeImage getIcon();

		/**
		 * The CSS class to add to the button opening the popup dialog.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCssClass();

		/** The renderer to display the button opening the popup dialog. */
		@Name(RENDERER)
		@InstanceFormat
		IButtonRenderer getRenderer();

		/**
		 * Horizontal position where the pop-up is opened.
		 */
		@Name(HORIZONTAL_POSITION)
		HorizontalPopupPosition getHorizontalPosition();

		/**
		 * Vertical position where the pop-up is opened.
		 */
		@Name(VERTICAL_POSITION)
		VerticalPopupPosition getVerticalPosition();

		/** The list of configured view configurations. */
		@Name(COMMAND_CONFIGURATIONS)
		@DefaultContainer
		List<PolymorphicConfiguration<? extends CommandModelConfiguration>> getCommands();
	}

	private final ResKey _label;

	private final ThemeImage _icon;

	private final IButtonRenderer _renderer;

	private final List<CommandModelConfiguration> _commands;

	private final String _cssClass;

	/**
	 * Creates a {@link PopupViewConfiguration}.
	 */
	public PopupViewConfiguration(InstantiationContext context, PopupViewConfiguration.Config config) {
		super(context, config);

		_label = config.getLabel();
		_icon = config.getIcon();
		_cssClass = config.getCssClass();
		_renderer = optional(config.getRenderer(), ButtonRenderer.INSTANCE);
		_commands = TypedConfiguration.getInstanceList(context, config.getCommands());
	}

	private static <T> T optional(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		Map<String, List<CommandModel>> commandsByClique = indexCommands(component);
		ToolBar toolbar = createToolBar(commandsByClique);
		DefaultPopupMenuModel menuModel = createMenuModel(toolbar);
		return createPopupButton(menuModel);
	}

	private HTMLFragment createPopupButton(DefaultPopupMenuModel menuModel) {
		PopupMenuButtonControl result = new PopupMenuButtonControl(menuModel, _renderer);
		result.setHorizontalPosition(getConfig().getHorizontalPosition());
		result.setVerticalPosition(getConfig().getVerticalPosition());
		return result;
	}

	private Map<String, List<CommandModel>> indexCommands(LayoutComponent component) {
		Map<String, List<CommandModel>> commands = new HashMap<>();
		for (CommandModelConfiguration commandConfig : _commands) {
			CommandModel commandModel = commandConfig.createCommandModel(component);

			if (commandModel == null) {
				Logger.error("Faild to create command model: " + commandConfig, PopupViewConfiguration.class);
				continue;
			}

			String clique;
			CommandHandler handler = handler(commandModel);
			if (handler == null) {
				clique = CommandHandlerFactory.ADDITIONAL_GROUP;
			} else {
				clique = handler.getClique();
			}
			commands.computeIfAbsent(clique, x -> new ArrayList<>()).add(commandModel);
		}
		return commands;
	}

	private ToolBar createToolBar(Map<String, List<CommandModel>> commandsByClique) {
		DefaultPopupDialogModel popupDialogModel = new DefaultPopupDialogModel(DefaultLayoutData.scrollingLayout(0, 0));
		ToolBar toolbar = popupDialogModel.getToolBar();

		CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
		List<Entry<String, List<CommandModel>>> cliqueEntries = new ArrayList<>(commandsByClique.entrySet());
		Collections.sort(cliqueEntries, factory.getCliqueToolBarOrder());
		for (Entry<String, List<CommandModel>> entry : cliqueEntries) {
			String clique = entry.getKey();
			String cliqueGroup = factory.getCliqueGroup(clique);

			ToolBarGroup group = toolbar.defineGroup(cliqueGroup);
			List<CommandModel> handlers = entry.getValue();
			for (CommandModel command : handlers) {
				group.addButton(command);
			}
		}
		return toolbar;
	}

	private DefaultPopupMenuModel createMenuModel(ToolBar toolbar) {
		Menu menu = ToolbarControl.createMenu(toolbar, false);
		DefaultPopupMenuModel menuModel = new DefaultPopupMenuModel(_icon, menu);
		menuModel.setCssClasses(_cssClass);
		Resources resources = Resources.getInstance();
		if (_label != null) {
			menuModel.setLabel(resources.getString(_label, null));
			menuModel.setTooltip(resources.getString(_label.tooltipOptional()));
		}
		return menuModel;
	}

	private static CommandHandler handler(CommandModel commandModel) {
		if (commandModel instanceof ComponentCommandModel) {
			return ((ComponentCommandModel) commandModel).getCommandHandler();
		}
		return null;
	}

}
