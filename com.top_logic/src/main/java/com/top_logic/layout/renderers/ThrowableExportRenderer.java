/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.provider.label.StackTraceLabelProvider;
import com.top_logic.tool.export.AbstractExcelCellRenderer;
import com.top_logic.util.Resources;

/**
 * {@link AbstractExcelCellRenderer} that exports a {@link Throwable}.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ThrowableExportRenderer extends AbstractExcelCellRenderer {


	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		Throwable stack = (Throwable) cellValue;
		ExcelValue value;
		if (stack == null) {
			value = noStackValue(excelRow, excelColumn);
		} else {
			value = valueForStack(stack, excelRow, excelColumn);
		}
		return value;
	}

	private ExcelValue valueForStack(Throwable stack, int excelRow, int excelColumn) {
		String stackAsString = toString(stack);
		ExcelValue excelValue = new ExcelValue(excelRow, excelColumn, stackAsString);
		excelValue.setComment(stackAsString);

		excelValue.setTextWrap(Boolean.TRUE);
		// autoWidth must be disabled to use cell width.
		excelValue.setAutoWidth(false);
		excelValue.setCellWidth(30);
		return excelValue;
	}

	private String toString(Throwable stack) {
		return StackTraceLabelProvider.INSTANCE.getLabel(stack);
	}

	private ExcelValue noStackValue(int excelRow, int excelColumn) {
		String message = Resources.getInstance().getString(I18NConstants.NO_STACK_AVAILABLE);
		return new ExcelValue(excelRow, excelColumn, message);
	}

}

