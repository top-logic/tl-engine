/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.tool.export.AbstractExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * {@link ExcelCellRenderer} for a {@link StructuredText}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class StructuredTextExcelRenderer extends AbstractExcelCellRenderer {

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		if (cellValue instanceof StructuredText) {
			StructuredText text = (StructuredText) cellValue;

			try {
				return new ExcelValue(excelRow, excelColumn, StructuredTextUtil.getCodeWithInlinedImages(text));
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		} else {
			return new ExcelValue(excelRow, excelColumn, cellValue);
		}
	}

}
