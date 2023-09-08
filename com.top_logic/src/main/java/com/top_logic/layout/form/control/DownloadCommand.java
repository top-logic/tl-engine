/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Map;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command which triggers delivering of an {@link BinaryData} to the client
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class DownloadCommand extends ControlCommand {

	public static final DownloadCommand INSTANCE = new DownloadCommand("download");

	protected DownloadCommand(String id) {
		super(id);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		IDownloadControl downloadControl = (IDownloadControl) control;
		final BinaryDataSource dataItem = downloadControl.dataItem();
		if (dataItem == null) {
			HandlerResult result = new HandlerResult();
			result.addErrorMessage(I18NConstants.ERROR_DOWNLOAD_NO_MODEL);
			return result;
		}

		commandContext.getWindowScope().deliverContent(dataItem, downloadControl.downloadInline());
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.DOWNLOAD_DATA_ITEM;
	}

}