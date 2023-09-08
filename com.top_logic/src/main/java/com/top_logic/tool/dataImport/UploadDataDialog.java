/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.CommandModelUtilities;
import com.top_logic.layout.basic.NonEmptyFieldCommandExecutabilityListener;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.util.Resources;

/**
 * A {@link SimpleFormDialog} to upload a data file.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class UploadDataDialog extends SimpleFormDialog {
	/**
	 * Creates a {@link UploadDataDialog} for the given component.
	 */
	public UploadDataDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height) {
		super(resourcePrefix, width, height);
	}

	/**
	 * Creates a {@link UploadDataDialog} for the given component.
	 */
	public UploadDataDialog(ResPrefix uploadDialogPrefix) {
		super(uploadDialogPrefix,
			DisplayDimension.dim(330, DisplayUnit.PIXEL),
			DisplayDimension.dim(150, DisplayUnit.PIXEL));
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		addCloseCommand(buttons);
		addImportCommand(buttons);
	}

	private boolean addCloseCommand(List<CommandModel> buttons) {
		return buttons.add(MessageBox.button(ButtonType.CANCEL, getDialogModel().getCloseAction()));
	}

	private void addImportCommand(List<CommandModel> buttons) {
		CommandModel importCommand = createImportCommand();

		addExecutableListener(importCommand);

		buttons.add(importCommand);
	}

	private void addExecutableListener(CommandModel importCommand) {
		CommandModelUtilities.setNonExecutable(importCommand, I18NConstants.NO_VALUE_IMPORT_NOT_EXECUTABLE);

		getUploadField().addValueListener(createCommandExecutabilityFieldListener(importCommand));
	}

	private NonEmptyFieldCommandExecutabilityListener createCommandExecutabilityFieldListener(CommandModel command) {
		return new NonEmptyFieldCommandExecutabilityListener(command, I18NConstants.NO_VALUE_IMPORT_NOT_EXECUTABLE);
	}

	/**
	 * The field into which the theme file is uploaded.
	 */
	public DataField getUploadField() {
		return (DataField) getFormContext().getField(INPUT_FIELD);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		DataField uploadField = FormFactory.newDataField(INPUT_FIELD);

		addConstraints(uploadField);

		context.addMember(uploadField);
	}

	private void addConstraints(DataField uploadField) {
		getFieldConstraints().forEach(constraint -> uploadField.addConstraint(constraint));
	}

	private CommandModel createImportCommand() {
		CommandModel importCommand = CommandModelFactory.commandModel(getUploadDataCommand());

		importCommand.setLabel(Resources.getInstance().getString(I18NConstants.IMPORT));

		return importCommand;
	}

	/**
	 * Command executed after the import button is clicked.
	 */
	protected abstract UploadDataCommand getUploadDataCommand();

	/**
	 * Constraints for the upload field.
	 */
	protected abstract Collection<Constraint> getFieldConstraints();
}
