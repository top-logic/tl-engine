/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.tool.export.AbstractExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * {@link ExcelCellRenderer} for a {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class I18NStructuredTextExcelRenderer extends AbstractExcelCellRenderer {

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		if (cellValue instanceof I18NStructuredText) {
			StructuredText localizedText = ((I18NStructuredText) cellValue).localize();

			if (localizedText != null) {
				try {
					String codeWithInlinedImages = StructuredTextUtil.getCodeWithInlinedImages(localizedText);

					return new ExcelValue(excelRow, excelColumn, codeWithInlinedImages);
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			} else {
				return new ExcelValue(excelRow, excelColumn, null);
			}
		} else {
			return new ExcelValue(excelRow, excelColumn, cellValue);
		}
	}

}
