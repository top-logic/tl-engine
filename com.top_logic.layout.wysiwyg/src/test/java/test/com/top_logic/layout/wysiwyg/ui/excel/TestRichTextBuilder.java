/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.junit.Assert;

import com.top_logic.layout.wysiwyg.ui.excel.RichTextBuilder;

/**
 * Test case fir für {@link RichTextBuilder}
 */
@SuppressWarnings("javadoc")
public class TestRichTextBuilder extends TestCase {

	public void testConvert() throws IOException, InvalidFormatException {
		Workbook workbook = createWorkbook();
		RichTextBuilder builder = new RichTextBuilder(workbook);
		Node document = Jsoup.parse("<p>Some <b>bold <i>and</i> funny</b> words.</p> <p>And another paragraph.</p>");
		builder.append(document);
		RichTextString text = builder.getText();

		//                 v    v  v     v   
		//            000000000011111111113333333
		//            012345678901234567890123456 7 8901234567890012345678
		assertEquals("Some bold and funny words.\n\nAnd another paragraph.\n", text.toString());
		assertEquals(Arrays.asList(0, 5, 10, 13, 19), runs(text));

		save(workbook, text, "target/TestRichTextBuilder-simple.xlsx", builder.getWidth());

		try (XSSFWorkbook checkWorkbook = new XSSFWorkbook(new File("target/TestRichTextBuilder-simple.xlsx"))) {
			XSSFCell cell = checkWorkbook.getSheetAt(0).getRow(0).getCell(0);
			Assert.assertEquals(CellType.STRING, cell.getCellType());
			XSSFRichTextString value = cell.getRichStringCellValue();
			Assert.assertEquals("Some bold and funny words.\n\nAnd another paragraph.\n", value.getString());
			Assert.assertEquals(5, value.numFormattingRuns());
			Assert.assertTrue(value.getFontOfFormattingRun(1).getBold());
		}
	}

	public void testHeading() throws IOException {
		Workbook workbook = createWorkbook();
		RichTextBuilder builder = new RichTextBuilder(workbook);
		Node document = Jsoup.parse(
			"<h1>Heading 1</h1> <p>Some <b>bold</b> content.</p> <h2>Sub-Heading</h2> <p>Some <i>italics</i> content.</p>");
		builder.append(document);
		RichTextString text = builder.getText();

		save(workbook, text, "target/TestRichTextBuilder-heading.xlsx", builder.getWidth());
	}

	private static List<Integer> runs(RichTextString text) {
		List<Integer> runs = new ArrayList<>();
		for (int n = 0, cnt = text.numFormattingRuns(); n < cnt; n++) {
			int run = text.getIndexOfFormattingRun(n);
			runs.add(Integer.valueOf(run));
		}
		return runs;
	}

	public void testDummy() throws IOException, InvalidFormatException {
		Workbook workbook = createWorkbook();

		Font font1 = workbook.createFont();
		font1.setColor(Font.COLOR_RED);
		Font font2 = workbook.createFont();
		font2.setBold(true);
		Font font3 = workbook.createFont();
		font3.setItalic(true);

		RichTextString value = workbook.getCreationHelper().createRichTextString("RichTextFormat");
		value.applyFont(0, 4, font1);
		value.applyFont(4, 8, font2);
		value.applyFont(8, 14, font3);

		save(workbook, value, "target/TestRichTextBuilder-dummy.xlsx", 20);

		try (XSSFWorkbook checkWorkbook = new XSSFWorkbook(new File("target/TestRichTextBuilder-dummy.xlsx"))) {
			XSSFCell cell = checkWorkbook.getSheetAt(0).getRow(0).getCell(0);
			Assert.assertEquals(CellType.STRING, cell.getCellType());
			XSSFRichTextString checkValue = cell.getRichStringCellValue();
			Assert.assertEquals("RichTextFormat", checkValue.getString());
			Assert.assertEquals(3, checkValue.numFormattingRuns());
			Assert.assertTrue(checkValue.getFontOfFormattingRun(1).getBold());
		}
	}

	private Workbook createWorkbook() {
		// Note: SXSSFWorkbook does not support rich text formatting.
		return new XSSFWorkbook();
	}

	private void save(Workbook workbook, RichTextString richTextValue, String fileName, int width) throws IOException {
		Sheet sheet = workbook.createSheet("Example");
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue(richTextValue);
		cell.getCellStyle().setVerticalAlignment(VerticalAlignment.TOP);
		cell.getCellStyle().setWrapText(true);
		sheet.setColumnWidth(0, 256 * width);
		row.setHeightInPoints(100);

		try (FileOutputStream out = new FileOutputStream(new File(fileName))) {
			workbook.write(out);
		}
	}

}
