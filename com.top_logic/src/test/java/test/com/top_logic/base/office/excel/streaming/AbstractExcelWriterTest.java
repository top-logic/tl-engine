/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office.excel.streaming;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.streaming.ExcelWriter;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * Test of {@link ExcelWriter}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractExcelWriterTest extends BasicTestCase {

	public static class MyValue {

		public static class MyValueLabel implements LabelProvider {

			/**
			 * Singleton {@link AbstractExcelWriterTest.MyValue.MyValueLabel} instance.
			 */
			public static final MyValueLabel INSTANCE = new MyValueLabel();

			private MyValueLabel() {
				// Singleton constructor.
			}

			@Override
			public String getLabel(Object object) {
				return "Custom: " + ((MyValue) object).getValue();
			}

		}

		private final String _value;

		public MyValue(String value) {
			_value = value;
		}

		public String getValue() {
			return _value;
		}

	}

	/**
	 * Creates a new {@link ExcelWriter} for testing.
	 */
	protected abstract ExcelWriter newExcelWriter() throws IOException;

	public void testTypes() throws ParseException, IOException {
		File xls;
		ExcelWriter writer = newExcelWriter();
		try {
			writer.newTable("types");

			writer.write("0. String");
			writer.write("foobar");
			writer.write(POIUtil.newRichTextString(writer.getWorkbook(), "foobar"));
			writer.write(null);
			writer.write('A');
			writer.write("13");
			writer.write("13.42");
			writer.write("13,42");
			writer.write("13.05.2025");
			writer.write("17:10");
			writer.newRow();

			writer.write("1. Numeric");
			writer.write((byte) 42);
			writer.write((short) 42);
			writer.write(42);
			writer.write((long) 42);
			writer.write((double) 42);
			writer.write((float) 42);
			writer.write(42.13);
			writer.write(42.13F);
			writer.write(DateUtil.createDate(2015, Calendar.OCTOBER, 19, 9, 42, 13));
			writer.write(newCalendar());
			writer.newRow();

			writer.write("2. Boolean");
			writer.write(true);
			writer.write(false);
			writer.newRow();

			writer.write("3. Custom");
			writer.write(new MyValue("foobar"));
			writer.write(new ExcelValue(99, 99, "red foo", new Color(255, 0, 0)));
			writer.write(new ExcelValue(99, 99, "green foobar", new Color(0, 255, 0)));
			writer.write(new ExcelValue(99, 99, "red bar", new Color(255, 0, 0)));
			writer.newRow();
		} finally {
			xls = writer.close();
		}

		Workbook workbook = getWorkbook(xls);
		Sheet types = workbook.getSheet("types");
		assertEquals(CellType.STRING, types.getRow(0).getCell(1).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(2).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(3).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(4).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(5).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(6).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(7).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(8).getCellType());
		assertEquals(CellType.STRING, types.getRow(0).getCell(9).getCellType());

		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(1).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(2).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(3).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(4).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(5).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(6).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(7).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(8).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(9).getCellType());
		assertEquals(CellType.NUMERIC, types.getRow(1).getCell(10).getCellType());

		assertEquals(CellType.BOOLEAN, types.getRow(2).getCell(1).getCellType());
		assertEquals(CellType.BOOLEAN, types.getRow(2).getCell(2).getCellType());

		assertEquals(CellType.STRING, types.getRow(3).getCell(1).getCellType());
		assertEquals("Custom: foobar", types.getRow(3).getCell(1).getStringCellValue());
		assertEquals(CellType.STRING, types.getRow(3).getCell(2).getCellType());
		assertEquals("red foo", types.getRow(3).getCell(2).getStringCellValue());
		assertEquals("green foobar", types.getRow(3).getCell(3).getStringCellValue());
		assertEquals("red bar", types.getRow(3).getCell(4).getStringCellValue());
	}

	private Calendar newCalendar() {
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2015, Calendar.OCTOBER, 19, 9, 42, 13);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public void testDateValueAtEndOfDay() throws ParseException, IOException {
		DateFormat format = CalendarUtil.newSimpleDateFormat("yyyy/MM/dd HH:mm:ss,SSS", Locale.GERMAN);
		Object aValue = format.parse("2014/07/22 23:59:59,999");

		String sheetName = "sheet";

		File output;
		ExcelWriter excelWriter = newExcelWriter();
		try {
			excelWriter.newTable(sheetName);
			excelWriter.write(aValue);
		} finally {
			output = excelWriter.close();
		}

		Workbook workbook = getWorkbook(output);
		Cell cell = workbook.getSheet(sheetName).getRow(0).getCell(0);
		Date dateCellValue = cell.getDateCellValue();
		// Check via formatted string instead of date, because toString of date does not contain
		// milliseconds.
		assertEquals(
			"Milliseconds has been removed during POI excel export. This is a workaround for the fact that excel rounds to next day.",
			"2014/07/22 23:59:59,000", format.format(dateCellValue));
	}

	public void testSameColumnWidth() throws IOException {
		String sheetName = "sheet";

		File output;
		String value = "Very long string value which is displayed in two columns";
		ExcelWriter excelWriter = newExcelWriter();
		try {
			excelWriter.newTable(sheetName);
			excelWriter.write(value);
			excelWriter.write(new ExcelValue(0, 1, value));
		} finally {
			output = excelWriter.close();
		}
		Workbook workbook = getWorkbook(output);
		Sheet sheet = workbook.getSheet(sheetName);
		assertEquals("Ticket #22992: value and wrapped value must have same size", sheet.getColumnWidth(0),
			sheet.getColumnWidth(1));
	}

	private Workbook getWorkbook(File f) throws FileNotFoundException, IOException {
		try (FileInputStream in = new FileInputStream(f)) {
			return POIUtil.newWorkbook(f.getName(), in);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link AbstractExcelWriterTest}.
	 */
	protected static Test suite(Class<? extends AbstractExcelWriterTest> testClass) {
		return TLTestSetup
			.createTLTestSetup(ServiceTestSetup.createSetup(testClass, LabelProviderService.Module.INSTANCE));
	}
}
