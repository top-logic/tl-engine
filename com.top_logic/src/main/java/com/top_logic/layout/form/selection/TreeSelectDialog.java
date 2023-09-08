/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.structure.DialogWindowControl;

/**
 * {@link SelectDialogBase} displaying an option tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TreeSelectDialog extends SelectDialogBase {

	/**
	 * Creates a new {@link TreeSelectDialog}.
	 */
	public TreeSelectDialog(SelectField targetSelectField, SelectDialogConfig config) {
		super(targetSelectField, config);
	}

	/**
	 * Service method to create the actual {@link FormContext} for the selection dialog.
	 * 
	 * @param dialog
	 *        The dialog displaying the option tree.
	 */
	protected TreeSelectorContext createSelectorContext(DialogWindowControl dialog) {
		Command closeAction = dialog.getDialogModel().getCloseAction();

		TreeSelectorContext context = new TreeSelectorContext(getTargetField(), closeAction, getConfig());
		return context;
	}

}

