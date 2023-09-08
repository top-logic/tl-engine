/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.util.Resources;

/**
 * A utility class to create buttons for a SelectorContext.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class SelectorContextUtil {

	/**
	 * Creates a {@link FormGroup} filled with buttons. The order entering the commands is the order
	 * displayed at the GUI.
	 * 
	 * @param firstButton
	 *        Accept {@link CommandField}.
	 * @param secondeButton
	 *        Cancel {@link CommandField}.
	 * 
	 * @return {@link FormGroup} of buttons.
	 */
	public static FormGroup createButtonGroup(CommandField firstButton, CommandField secondeButton,
			ResourceView resources) {
		FormGroup buttons = new FormGroup(SelectorContext.BUTTONS, resources);
		buttons.addMember(firstButton);
		buttons.addMember(secondeButton);

		return buttons;
	}

	/**
	 * Creates an accept button. The name and label are constants. The property to show progress is
	 * set to <code>Boolean.TRUE</code>.
	 * 
	 * @see SelectorContext#ACCEPT_SELECTION
	 * @see I18NConstants#POPUP_SELECT_SUBMIT
	 * 
	 * @return A {@link CommandField} for the given {@link Command}.
	 */
	public static CommandField createAcceptButton(Command acceptAction) {
		String name = SelectorContext.ACCEPT_SELECTION;
		ResKey label = I18NConstants.POPUP_SELECT_SUBMIT;
		final CommandField accept = createCommandField(name, acceptAction, label);
		accept.setShowProgress();

		return accept;
	}

	/**
	 * Creates a cancel button. The name and label are constants.
	 * 
	 * @see SelectorContext#ACCEPT_SELECTION
	 * @see I18NConstants#POPUP_SELECT_SUBMIT
	 * 
	 * @return A {@link CommandField} for the given {@link Command}.
	 */
	public static CommandField createCancelButton(Command closeAction) {
		String name = SelectorContext.CANCEL_SELECTION;
		ResKey label = I18NConstants.POPUP_SELECT_CANCEL;
		CommandField cancel = createCommandField(name, closeAction, label);

		return cancel;
	}

	/**
	 * Create a {@link CommandField} for a button in a selector context.
	 * 
	 * @param name
	 *        Name of the command field in the {@link FormContext}.
	 * @param command
	 *        The actual command to execute.
	 * @param label
	 *        Label for the button.
	 */
	public static CommandField createCommandField(String name, Command command, ResKey label) {
		CommandField field = FormFactory.newCommandField(name, command);
		field.setLabel(Resources.getInstance().getString(label));

		return field;
	}
}
