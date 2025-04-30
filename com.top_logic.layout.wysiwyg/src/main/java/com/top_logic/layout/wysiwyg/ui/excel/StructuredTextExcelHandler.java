/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link PoiTypeHandler} for {@link StructuredText} values.
 */
public class StructuredTextExcelHandler implements PoiTypeHandler {

	@Override
	public Class<?> getHandlerClass() {
		return StructuredText.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		StructuredText text = (StructuredText) aValue;
		Document html = Jsoup.parse(text.getSourceCode());

		RichTextBuilder builder = new RichTextBuilder(aWorkbook).append(html);
		RichTextString rts = builder.getText();
		aCell.setCellValue(rts);

		CellStyle cellStyle = aCell.getCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setVerticalAlignment(VerticalAlignment.TOP);

		return builder.getWidth();
	}

}
