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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;

import com.top_logic.layout.wysiwyg.ui.excel.RichTextBuilder;

/**
 * Test case fir für {@link RichTextBuilder}
 */
@SuppressWarnings("javadoc")
public class TestRichTextBuilder extends TestCase {

	public void testConvert() throws IOException {
		Workbook workbook = new HSSFWorkbook();
		RichTextBuilder builder = new RichTextBuilder(workbook);
		Node document = Jsoup.parse("<p>Some <b>bold <i>and</i> funny</b> words.</p> <p>And another paragraph.</p>");
		builder.append(document);
		RichTextString text = builder.getText();

		//                 v    v  v     v   
		//            000000000011111111113333333
		//            012345678901234567890123456 7 8901234567890012345678
		assertEquals("Some bold and funny words.\n\nAnd another paragraph.\n", text.toString());
		assertEquals(Arrays.asList(5, 10, 13, 19), runs(text));

		save(workbook, text, "target/TestRichTextBuilder-simple.xls", builder.getWidth());
	}

	public void testHeading() throws IOException {
		Workbook workbook = new HSSFWorkbook();
		RichTextBuilder builder = new RichTextBuilder(workbook);
		Node document = Jsoup.parse(
			"<h1>Heading 1</h1> <p>Some <b>bold</b> content.</p> <h2>Sub-Heading</h2> <p>Some <i>italics</i> content.</p>");
		builder.append(document);
		RichTextString text = builder.getText();

		save(workbook, text, "target/TestRichTextBuilder-heading.xls", builder.getWidth());
	}

	private static List<Integer> runs(RichTextString text) {
		List<Integer> runs = new ArrayList<>();
		for (int n = 0, cnt = text.numFormattingRuns(); n < cnt; n++) {
			int run = text.getIndexOfFormattingRun(n);
			runs.add(Integer.valueOf(run));
		}
		return runs;
	}

	public void testDummy() throws IOException {
		Workbook workbook = new HSSFWorkbook();

		Font font1 = workbook.createFont();
		Font font2 = workbook.createFont();
		Font font3 = workbook.createFont();
		font1.setColor(Font.COLOR_RED);
		font2.setBold(true);
		font3.setItalic(true);

		RichTextString value = workbook.getCreationHelper().createRichTextString("RichTextFormat");
		value.applyFont(0, 4, font1);
		value.applyFont(5, 8, font2);
		value.applyFont(8, 13, font3);

		save(workbook, value, "target/TestRichTextBuilder-dummy.xls", 20);
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
