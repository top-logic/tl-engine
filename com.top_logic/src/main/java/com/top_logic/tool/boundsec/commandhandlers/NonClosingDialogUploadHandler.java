/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link UploadHandler} that does not close the dialog after the upload is finished.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class NonClosingDialogUploadHandler extends UploadHandler {

	/** The command provided by this instance. */
	public static final String COMMAND = "nonClosingUpload";

	public NonClosingDialogUploadHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult handleUpload(DisplayContext context, LayoutComponent aComponent, DataField dataField) throws IOException {
		HandlerResult result = super.handleUpload(context, aComponent, dataField);
		if (result.shallCloseDialog()) {
			result.setCloseDialog(false);
		}
		return result;
	}

}
