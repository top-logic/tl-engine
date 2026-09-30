/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui.excel;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.base.office.excel.streaming.ExcelWriter;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.excel.StructuredTextExcelHandler;

/**
 * Test case for {@link StructuredTextExcelHandler}.
 */
@SuppressWarnings("javadoc")
public class TestStructuredTextExcelHandler extends TestCase {

	public void testWrite() throws IOException, InvalidFormatException {
		// Note: SXSSFWorkbook does not support rich text formatting.
		ExcelWriter writer = ExcelWriter.createWriter();
		writer.newTable("Data");
		writer.write("Some text");
		writer.newRow();
		writer.write(new StructuredText("Hello <b>world</b>!"));
		File file = writer.close();

		try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
			XSSFCell cell = workbook.getSheetAt(0).getRow(1).getCell(0);
			Assert.assertEquals(CellType.STRING, cell.getCellType());
			XSSFRichTextString value = cell.getRichStringCellValue();
			Assert.assertEquals("Hello world!", value.getString());
			Assert.assertEquals(3, value.numFormattingRuns());
			Assert.assertTrue(value.getFontOfFormattingRun(1).getBold());
		}
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestStructuredTextExcelHandler.class,
				POITypeProvider.Module.INSTANCE));
	}
}
