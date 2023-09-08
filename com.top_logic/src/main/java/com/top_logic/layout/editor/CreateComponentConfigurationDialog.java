/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.editor.commands.AbstractComponentConfigurationCommandHandler;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Dialog for creating component configurations disabling script recording of the Ok button.
 * 
 * @see ComponentConfigurationDialogBuilder
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class CreateComponentConfigurationDialog extends CreateConfigurationsDialog {

	/**
	 * Dialog to create component configurations.
	 * 
	 * @see CreateConfigurationsDialog
	 */
	public CreateComponentConfigurationDialog(List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions,
			ResKey title, DisplayDimension width, DisplayDimension height) {
		super(partDefinitions, title, width, height);
	}

	@Override
	protected CommandModel getOkButton() {
		CommandModel okButton = super.getOkButton();

		/* Klicking the OK button must not be recorded, because it is that the scopes of the newly
		 * created components are stored in the test. Therefore a manual action is added instead of
		 * the generic "OK" klick. */
		ScriptingRecorder.annotateAsDontRecord(okButton);

		return okButton;
	}

	@Override
	public HandlerResult open(DisplayContext context) {
		/* This dialog was opened due to a selection in a ComponentSelectionDialog. There the OK was
		 * not recorded. This must be done here. */
		AbstractComponentConfigurationCommandHandler.ComponentSelectionDialog.recordOkButton(context);
		return super.open(context);
	}

}
