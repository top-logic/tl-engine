/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.stylesheet;

import java.util.List;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.constraints.RegularExpressionStringConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link SimpleFormDialog} for creating a new Stylesheet.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateStylesheetDialog extends SimpleFormDialog {

	private TableComponent _component;

	/**
	 * Creates a {@link CreateStylesheetDialog} for the given component.
	 */
	public CreateStylesheetDialog(LayoutComponent component) {
		super(I18NConstants.CREATE_STYLESHEET_DIALOG_TITLE, null, I18NConstants.CREATE_STYLESHEET_DIALOG_MESSAGE,
			DisplayDimension.dim(330, DisplayUnit.PIXEL),
			DisplayDimension.dim(210, DisplayUnit.PIXEL));

		_component = (TableComponent) component;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.CANCEL, getDialogModel().getCloseAction()));
		buttons.add(createCreateStylesheetCommand());

	}

	private CommandModel createCreateStylesheetCommand() {
		CommandModel createCommand = CommandModelFactory.commandModel(new CreateStylesheetCommand(this));
		createCommand.setLabel(I18NConstants.CREATE_STYLESHEET);

		return createCommand;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField stylesheetNameField = FormFactory.newStringField(INPUT_FIELD);

		stylesheetNameField.addConstraint(StylesheetFilenameSuffixConstraint.INSTANCE);
		stylesheetNameField.addConstraint(new RegularExpressionStringConstraint("^[a-zA-Z][a-zA-Z0-9_-]*\\.css$"));
		stylesheetNameField.addConstraint(AvailableStylesheetNameConstraint.INSTANCE);

		context.addMember(stylesheetNameField);
	}

	/**
	 * Table component where the different Stylesheets are displayed.
	 */
	public TableComponent getComponent() {
		return _component;
	}

	/**
	 * The field into which the stylesheet name is stored.
	 */
	public StringField getStylesheetNameField() {
		return (StringField) getFormContext().getField(INPUT_FIELD);
	}

}
