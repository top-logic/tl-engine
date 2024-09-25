/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

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
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * A {@link SimpleFormDialog} for creating a new resource folder.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateResourceFolderDialog extends SimpleFormDialog {

	private TreeComponent _component;

	/**
	 * Creates a {@link CreateResourceFolderDialog} for the given component.
	 */
	public CreateResourceFolderDialog(LayoutComponent component) {
		super(I18NConstants.CREATE_RESOURCE_FOLDER_DIALOG,
			DisplayDimension.dim(330, DisplayUnit.PIXEL),
			DisplayDimension.dim(230, DisplayUnit.PIXEL));

		_component = (TreeComponent) component;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		CommandModel createCommand = createResourceFolderCommand();

		buttons.add(MessageBox.button(ButtonType.CANCEL, getDialogModel().getCloseAction()));
		buttons.add(createCommand);
	}

	private CommandModel createResourceFolderCommand() {
		CommandModel createCommand = CommandModelFactory.commandModel(new CreateResourceFolderCommand(this));
		createCommand.setLabel(Resources.getInstance().getString(I18NConstants.CREATE_RESOURCE_FOLDER));

		return createCommand;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField resourceFolderPathField = FormFactory.newStringField(INPUT_FIELD);
		
		resourceFolderPathField.addConstraint(NoParentPathTokenConstraint.INSTANCE);
		resourceFolderPathField.addConstraint(new RegularExpressionStringConstraint("^[a-zA-Z][a-zA-Z0-9_-]*$"));

		context.addMember(resourceFolderPathField);
	}

	/**
	 * Tree component where the different resource folders are displayed.
	 */
	public TreeComponent getComponent() {
		return _component;
	}

	/**
	 * The field into which the resource folder path is stored.
	 */
	public StringField getResourceFolderPathField() {
		return (StringField) getFormContext().getField(INPUT_FIELD);
	}
}