/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Map;

import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;

/**
 * Interface for Components that allows (Excel) Export.
 * 
 * @author     <a href="mailto:mga@top-logic.com>Michael G&auml;nsler</a>
 */
public interface ExportAware {

    /**
	 * Set the parameters needed for the export.
	 * 
	 * The Constants for the Maps keys can be found in the {@link AbstractOfficeExportHandler}. When
	 * implementing this interface for a {@link TableComponent} it will automagically register an
	 * {@link ExcelExportHandler}.
	 * 
	 * @param progressInfo
	 *        Callback for reporting export progress.
	 * 
	 * @return a Map with the parameters needed for the export.
	 */
    public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments);

}
