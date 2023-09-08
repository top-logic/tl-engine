/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.tool.export.ExportQueueManager.ExportExecutor;

/**
 * Exports using {@link AbstractWordExportHandler} will be queued in
 * {@link ExportExecutor} of type {@link ExportHandler#TECHNOLOGY_EXCEL}.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public abstract class AbstractExcelExportHandler extends AbstractExportHandler {

	public AbstractExcelExportHandler(String anExportID) {
		super(anExportID);
	}
	
	@Override
	public String getExportDuration() {
		return DURATION_SHORT;
	}

	@Override
	public String getExportTechnology() {
		return TECHNOLOGY_EXCEL;
	}

}
