/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Imports the uploaded roles-profile file.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RolesProfileImportCommand implements Command {

	private final RolesProfileImportDialog _importDialog;

	/**
	 * Creates a {@link RolesProfileImportCommand}.
	 * 
	 * @param importDialog
	 *        Is not allowed to be null.
	 */
	public RolesProfileImportCommand(RolesProfileImportDialog importDialog) {
		_importDialog = requireNonNull(importDialog);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		if (!checkFormContext()) {
			return createErrorResult(_importDialog.getFormContext());
		}
		BinaryContent data = _importDialog.getUploadField().getDataItem();
		boolean success = _importDialog.importRolesProfile(context, data);
		if (!success) {
			_importDialog.getUploadField().setValue(null);
			return createErrorResult(I18NConstants.IMPORT_FAILED_UNKNOWN_REASON);
		}
		_importDialog.getDialogModel().getCloseAction().executeCommand(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	private boolean checkFormContext() {
		return _importDialog.getFormContext().checkAll();
	}

	private HandlerResult createErrorResult(FormContext formContext) {
		HandlerResult result = new HandlerResult();
		AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, result);
		return result;
	}

	private HandlerResult createErrorResult(ResKey reasonKey) {
		HandlerResult result = new HandlerResult();
		result.addError(reasonKey);
		return result;
	}

}
