/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.awt.Color;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.tool.export.AbstractExcelCellRenderer;

/**
 * {@link AbstractExcelCellRenderer}, that renders cell with colored background.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoColoredCellRenderer extends AbstractExcelCellRenderer {

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		ExcelValue excelValue = new ExcelValue(excelRow, excelColumn, cellValue);
		excelValue.setBackgroundColor(Color.YELLOW);
		return excelValue;
	}

}
