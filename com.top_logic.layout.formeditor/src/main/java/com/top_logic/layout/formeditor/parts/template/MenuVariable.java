/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.toolbar.Icons;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link VariableDefinition} for embedding a menu button with multiple {@link CommandHandler}s into
 * a HTML template.
 */
@Label("Menu")
public class MenuVariable extends AbstractVariableDefinition<MenuVariable.Config> {

	private List<CommandHandler> _commands;

	/**
	 * Configuration options for {@link MenuVariable}.
	 */
	public interface Config extends VariableDefinition.Config<MenuVariable> {
		/**
		 * @see #getButtons()
		 */
		String BUTTONS = "buttons";

		/**
		 * Command to embed into the template.
		 */
		@Options(fun = InAppImplementations.class)
		@Name(BUTTONS)
		List<ConfigBase<? extends CommandHandler>> getButtons();
	}

	/**
	 * Creates a {@link MenuVariable} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MenuVariable(InstantiationContext context, Config config) {
		super(context, config);

		_commands = config.getButtons().stream().map(c -> CommandHandlerFactory.getInstance().getCommand(context, c))
			.collect(Collectors.toList());
	}

	/**
	 * All commands in the menu.
	 */
	public List<CommandHandler> getCommands() {
		return _commands;
	}

	@Override
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		Map<String, Object> args = ContextMenuUtil.createArguments(model);
		Stream<CommandModel> buttonsStream = ContextMenuUtil.toButtonsStream(component, args, _commands);
		Menu menu = ContextMenuUtil.toContextMenu(buttonsStream);
		return new PopupMenuButtonControl(new DefaultPopupMenuModel(Icons.BUTTON_BURGER_MENU, menu));
	}

}
