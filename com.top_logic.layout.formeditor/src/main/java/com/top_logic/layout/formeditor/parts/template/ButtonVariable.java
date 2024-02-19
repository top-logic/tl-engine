/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link VariableDefinition} for embedding a {@link CommandHandler} into a HTML template.
 */
@Label("Button")
public class ButtonVariable extends AbstractVariableDefinition<ButtonVariable.Config> {

	private CommandHandler _command;

	/**
	 * Configuration options for {@link ButtonVariable}.
	 */
	public interface Config extends VariableDefinition.Config<ButtonVariable> {
		/**
		 * @see #getButton()
		 */
		String BUTTON = "button";

		/**
		 * Command to embed into the template.
		 */
		@Options(fun = InAppImplementations.class)
		@Name(BUTTON)
		ConfigBase<? extends CommandHandler> getButton();
	}

	/**
	 * Creates a {@link ButtonVariable} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ButtonVariable(InstantiationContext context, Config config) {
		super(context, config);

		_command = CommandHandlerFactory.getInstance().getCommand(context, config.getButton());
	}

	/**
	 * The {@link CommandHandler} to invoke.
	 */
	public CommandHandler getCommand() {
		return _command;
	}

	@Override
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		Map<String, Object> args = ContextMenuUtil.createArguments(model);
		CommandModel commandModel = CommandModelFactory.commandModel(_command, component, args);
		return new ButtonControl(commandModel);
	}

}
