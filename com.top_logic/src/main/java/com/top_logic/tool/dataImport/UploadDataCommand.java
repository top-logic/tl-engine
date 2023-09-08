/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.Objects;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Abstract command to upload data.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class UploadDataCommand implements Command {

	private final UploadDataDialog _uploadDataDialog;

	/**
	 * Creates a {@link UploadDataCommand} for the given upload dialog.
	 */
	public UploadDataCommand(UploadDataDialog uploadDataDialog) {
		_uploadDataDialog = Objects.requireNonNull(uploadDataDialog);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		if (!checkFormContext()) {
			return createErrorResult(_uploadDataDialog.getFormContext());
		}

		uploadPostProcess(_uploadDataDialog.getUploadField().getDataItem());

		closeDialog(context);

		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult closeDialog(DisplayContext context) {
		return _uploadDataDialog.getDialogModel().getCloseAction().executeCommand(context);
	}

	private boolean checkFormContext() {
		return _uploadDataDialog.getFormContext().checkAll();
	}

	private HandlerResult createErrorResult(FormContext formContext) {
		HandlerResult result = new HandlerResult();

		AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, result);

		return result;
	}

	/**
	 * Post processing function invoked after the data upload is finished.
	 */
	protected abstract void uploadPostProcess(BinaryData data);
}
