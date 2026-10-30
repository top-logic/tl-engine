/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import junit.framework.TestCase;

import com.top_logic.layout.view.table.ColumnExport;

/**
 * Test for {@link ColumnExport#excelDateFormat(java.text.Format)}: a date column displayed with
 * its time of day is exported with an Excel format showing the time of day as well.
 */
@SuppressWarnings("javadoc")
public class TestColumnExport extends TestCase {

	public void testDateTimeFormat() {
		for (Locale locale : new Locale[] { Locale.GERMANY, Locale.US }) {
			assertEquals(locale.toString(), ColumnExport.EXCEL_DATE_TIME_FORMAT, ColumnExport
				.excelDateFormat(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)));
		}
		assertEquals(ColumnExport.EXCEL_DATE_TIME_FORMAT,
			ColumnExport.excelDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
	}

	public void testDateOnlyKeepsTheDefault() {
		assertNull(ColumnExport.excelDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY)));
		assertNull(ColumnExport.excelDateFormat(new SimpleDateFormat("dd.MM.yyyy")));
	}

	public void testLiteralsAreNoFields() {
		// The 'h' and 'm' of the literal are no hours and minutes.
		assertNull(ColumnExport.excelDateFormat(new SimpleDateFormat("dd.MM.yyyy 'hm'")));
	}

	public void testTimeOnly() {
		assertEquals(ColumnExport.EXCEL_TIME_FORMAT, ColumnExport.excelDateFormat(new SimpleDateFormat("HH:mm")));
		assertEquals(ColumnExport.EXCEL_TIME_SECONDS_FORMAT,
			ColumnExport.excelDateFormat(new SimpleDateFormat("HH:mm:ss")));
	}

	public void testNoDateFormat() {
		assertNull(ColumnExport.excelDateFormat(null));
		assertNull(ColumnExport.excelDateFormat(new DecimalFormat("#,##0.00")));
	}

}
