/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.provider.BooleanLabelProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.tool.export.AbstractExcelCellRenderer;

/**
 * Demo {@link AbstractExcelCellRenderer} that uses two columns to export boolean values.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoBooleanExcelRenderer extends AbstractExcelCellRenderer {

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		Boolean b = (Boolean) cellValue;
		if (b == null || !b.booleanValue()) {
			excelColumn++;
		}
		return new ExcelValue(excelRow, excelColumn, BooleanLabelProvider.INSTANCE.getLabel(b));
	}

	@Override
	public int colSpan(TableModel model, Column modelColumn) {
		return 2;
	}

}

