/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.tool.export.AbstractExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * {@link ExcelCellRenderer} for {@link CompareInfo} objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareInfoExcelRenderer extends AbstractExcelCellRenderer {

	private ExcelCellRenderer _defaultExcelCellRenderer;

	/**
	 * Creates a new {@link CompareInfoExcelRenderer}.
	 */
	public CompareInfoExcelRenderer(ExcelCellRenderer defaultExcelCellRenderer) {
		_defaultExcelCellRenderer = defaultExcelCellRenderer;
	}

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		ExcelValue result = _defaultExcelCellRenderer.renderCell(context);
		if (cellValue == null) {
			return result;
		}
		CompareInfo compareInfo = (CompareInfo) cellValue;
		ChangeInfo changeInfo = compareInfo.getChangeInfo();
		switch (changeInfo) {
			case CREATED:
			case REMOVED:
				result.setBackgroundColor(compareInfo.getExcelColor());
				break;
			default:
				break;
		}
		return result;
	}

}

