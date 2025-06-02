/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;

/**
 * Mapping of object for Excel export.
 * 
 * @see MappingExcelCellRenderer
 */
public interface ExcelMapping {

	/**
	 * Maps an object for export.
	 */
	Object apply(RenderContext context, Object obj);

}
