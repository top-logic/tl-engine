/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Clears the display description.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ClearFormCommand extends AbstractDynamicCommand {

	/**
	 * Creates a {@link CommandModel} with default visual behaviour that clears the given
	 * {@link FormDefinition}.
	 * 
	 * @param form
	 *        The {@link FormDefinition} to clear.
	 */
	public static CommandModel newClearFormCommandModel(FormDefinition form) {
		CommandModel model = CommandModelFactory.commandModel(new ClearFormCommand(form));
		model.setLabel(Resources.getInstance().getString(I18NConstants.DELETE_FORM));
		model.setImage(Icons.DELETE_FORM);
		model.setNotExecutableImage(Icons.DELETE_FORM_DISABLED);
		return model;
	}

	private static final ExecutableState NO_CONTENT =
		ExecutableState.createDisabledState(I18NConstants.DELETE_FORM_DISABLED);

	private FormDefinition _form;

	/**
	 * Creates a new {@link ClearFormCommand}.
	 * 
	 * @param form
	 *        The actual version of the {@link FormDefinition} in the form editor.
	 */
	public ClearFormCommand(FormDefinition form) {
		_form = form;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		_form.getContent().clear();

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutableState calculateExecutability() {
		if (_form.getContent().isEmpty()) {
			return NO_CONTENT;
		}
		return ExecutableState.EXECUTABLE;
	}
}