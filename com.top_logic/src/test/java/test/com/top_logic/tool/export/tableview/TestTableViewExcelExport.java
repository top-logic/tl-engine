/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export.tableview;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.Test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.PaneInformation;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortSpec;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.export.AbstractExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.tableview.ExportMonitor;
import com.top_logic.tool.export.tableview.TableViewExcelExport;
import com.top_logic.tool.export.tableview.TableViewExcelExport.Snapshot;

/**
 * Test for {@link TableViewExcelExport}: the exported sheet holds what the table displays.
 */
public class TestTableViewExcelExport extends BasicTestCase {

	private record Ticket(String name, String status, int effort, Date due, List<String> tags) {
		// Test fixture.
	}

	private static final Date DUE = new Date(1767225600000L);

	private List<Ticket> tickets() {
		List<Ticket> result = new ArrayList<>();
		result.add(new Ticket("Login fails", "open", 5, DUE, List.of("auth", "bug")));
		result.add(new Ticket("Export missing", "open", 8, DUE, List.of("feature")));
		result.add(new Ticket("Slow search", "closed", 3, DUE, List.of()));
		result.add(new Ticket("Logout button", "closed", 1, DUE, List.of("ui")));
		result.add(new Ticket("Broken link", "open", 2, DUE, List.of("bug")));
		return result;
	}

	private List<Column<Ticket, ?>> columns() {
		List<Column<Ticket, ?>> columns = new ArrayList<>();
		// A per-row action in front of the data: never exported, whatever its position.
		columns.add(DefaultColumn.<Ticket, Ticket> builder("open", t -> t)
			.label(ResKey.text(""))
			.renderer(t -> CellContent.text("button"))
			.selectable(false)
			.build());
		columns.add(DefaultColumn.<Ticket, String> builder("name", Ticket::name)
			.label(ResKey.text("Name"))
			.sort(() -> Comparator.naturalOrder())
			.filter(TextColumnFilter.forStrings())
			.build());
		columns.add(DefaultColumn.<Ticket, String> builder("status", Ticket::status)
			.label(ResKey.text("Status"))
			.sort(() -> Comparator.naturalOrder())
			.build());
		columns.add(DefaultColumn.<Ticket, Integer> builder("effort", Ticket::effort)
			.label(ResKey.text("Effort"))
			.sort(() -> Comparator.naturalOrder())
			.aggregate(group -> CellContent.text(
				"Sum " + group.members().stream().mapToInt(Ticket::effort).sum()))
			.width(140)
			.build());
		columns.add(DefaultColumn.<Ticket, Date> builder("due", Ticket::due)
			.label(ResKey.text("Due"))
			.build());
		columns.add(DefaultColumn.<Ticket, List<String>> builder("tags", Ticket::tags)
			.label(ResKey.text("Tags"))
			.build());
		// A data column the application keeps out of exports.
		columns.add(DelegatingColumn.notExportable(DefaultColumn.<Ticket, String> builder("secret", t -> "x")
			.label(ResKey.text("Secret"))
			.build()));
		return columns;
	}

	private DefaultTableView<Ticket> view() {
		List<Column<Ticket, ?>> columns = columns();
		return DefaultTableView.create(columns, new ListRowSource<>(tickets(), columns));
	}

	private static TableViewExcelExport export() {
		return new TableViewExcelExport(column -> null);
	}

	/**
	 * Exports the displayed columns in display order, leaving out the per-row action column (by
	 * its identity, also when it is the first column) and the column that is not exportable.
	 */
	public void testColumnsInDisplayOrder() throws IOException {
		DefaultTableView<Ticket> view = view();
		view.setColumnOrder(List.of("open", "status", "name", "effort", "secret"));

		Sheet sheet = sheet(export().setStreaming(false), view);

		assertEquals(List.of("Status", "Name", "Effort"), rowTexts(sheet, 0));
		assertEquals(List.of("open", "Login fails", "5"), rowTexts(sheet, 1));
	}

	/**
	 * Exports the rows the table displays: filtered, searched and sorted.
	 */
	public void testFilterSearchAndSort() throws IOException {
		DefaultTableView<Ticket> view = view();
		view.filter("name", TextFilterState.contains("s"));
		view.search(TextFilterState.contains("open"));
		view.sort(SortSpec.ascending("effort"));

		Sheet sheet = sheet(export(), view);

		// "s" in the name and "open" in any displayed column: Login fails (5), Export missing (8).
		assertEquals(3, sheet.getPhysicalNumberOfRows());
		assertEquals("Login fails", text(sheet, 1, 0));
		assertEquals("Export missing", text(sheet, 2, 0));
	}

	/**
	 * Writes a cell from the typed value of its column: a number as number, a date as date, a
	 * collection as the labels of its elements.
	 */
	public void testTypedValues() throws IOException {
		Sheet sheet = sheet(export().setStreaming(false), view());

		assertEquals(List.of("Name", "Status", "Effort", "Due", "Tags"), rowTexts(sheet, 0));
		Cell effort = sheet.getRow(1).getCell(2);
		assertEquals(CellType.NUMERIC, effort.getCellType());
		assertEquals(5.0, effort.getNumericCellValue());
		Cell due = sheet.getRow(1).getCell(3);
		assertEquals(CellType.NUMERIC, due.getCellType());
		assertTrue(DateUtil.isCellDateFormatted(due));
		assertEquals(DUE, due.getDateCellValue());
		assertEquals("auth; bug", text(sheet, 1, 4));
	}

	/**
	 * Renders the cells of a column by the {@link ExcelCellRenderer} configured for it.
	 */
	public void testConfiguredRenderer() throws IOException {
		ExcelCellRenderer upper = new AbstractExcelCellRenderer() {
			@Override
			protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow,
					int excelColumn) {
				return new ExcelValue(excelRow, excelColumn, ((String) cellValue).toUpperCase());
			}
		};
		TableViewExcelExport export =
			new TableViewExcelExport(column -> column.equals("status") ? upper : null);

		Sheet sheet = sheet(export, view());

		assertEquals("OPEN", text(sheet, 1, 1));
		assertEquals("Login fails", text(sheet, 1, 0));
	}

	/**
	 * Writes a group as a bold header row heading its members in the outline of the sheet; the
	 * members of a collapsed group are exported as hidden rows.
	 */
	public void testGrouping() throws IOException {
		DefaultTableView<Ticket> view = view();
		view.group(new GroupSpec(List.of("status")));
		Object closedGroup = view.rows(0, view.rowCount()).stream()
			.filter(row -> row.group() != null && "closed".equals(row.group().key().values().get(0)))
			.findFirst().get().key();
		view.setExpanded(closedGroup, false);

		for (boolean streaming : new boolean[] { true, false }) {
			Sheet sheet = sheet(export().setStreaming(streaming), view);

			// Groups ordered by the grouping column: closed (collapsed, 2 members), open (3 members).
			assertEquals("closed (2)", text(sheet, 1, 0));
			assertEquals("Sum 4", text(sheet, 1, 2));
			assertTrue(isBold(sheet, 1));
			assertEquals(0, sheet.getRow(1).getOutlineLevel());
			for (int n = 2; n <= 3; n++) {
				assertEquals(1, sheet.getRow(n).getOutlineLevel());
				assertTrue("Member of a collapsed group must be hidden.", sheet.getRow(n).getZeroHeight());
			}
			assertEquals("open (3)", text(sheet, 4, 0));
			for (int n = 5; n <= 7; n++) {
				assertEquals(1, sheet.getRow(n).getOutlineLevel());
				assertFalse(sheet.getRow(n).getZeroHeight());
			}
			assertEquals("open", text(sheet, 5, 1));
			assertFalse(sheet.getRowSumsBelow());
		}
	}

	/**
	 * Freezes the header row and the frozen columns, offers the filter on the header and, without
	 * fitting, takes the widths the columns are displayed with.
	 */
	public void testSheetLayout() throws IOException {
		DefaultTableView<Ticket> view = view();
		// The action column counts in the frozen prefix of the display but is not exported.
		view.setFrozenColumnCount(2);

		Sheet sheet = sheet(export().setAutoFit(false).setStreaming(false), view);

		PaneInformation pane = sheet.getPaneInformation();
		assertNotNull(pane);
		assertTrue(pane.isFreezePane());
		assertEquals(1, pane.getVerticalSplitPosition());
		assertEquals(1, pane.getHorizontalSplitPosition());
		assertNotNull(((org.apache.poi.xssf.usermodel.XSSFSheet) sheet).getCTWorksheet().getAutoFilter());
		assertEquals(140 * (256 / 7), sheet.getColumnWidth(2));
	}

	/**
	 * Writes the legacy format for a download name asking for it.
	 */
	public void testLegacyFormat() throws IOException {
		DefaultTableView<Ticket> view = view();
		TableViewExcelExport export = export();
		BinaryData data = export.write(export.snapshot(view), "tickets.xls", ExportMonitor.NONE);

		assertEquals("tickets.xls", data.getName());
		try (InputStream in = data.getStream(); Workbook workbook = WorkbookFactory.create(in)) {
			assertEquals("Login fails", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
		}
	}

	/**
	 * Takes the rows at the time the export is asked for: a later change of the table does not
	 * change the export.
	 */
	public void testSnapshot() throws IOException {
		DefaultTableView<Ticket> view = view();
		TableViewExcelExport export = export();
		Snapshot<Ticket> snapshot = export.snapshot(view);
		view.filter("name", TextFilterState.contains("Login"));

		BinaryData data = export.write(snapshot, "tickets", ExportMonitor.NONE);

		assertEquals("tickets.xlsx", data.getName());
		assertEquals(5, snapshot.rowCount());
		try (InputStream in = data.getStream(); Workbook workbook = WorkbookFactory.create(in)) {
			assertEquals(6, workbook.getSheetAt(0).getPhysicalNumberOfRows());
		}
	}

	/**
	 * Reports the progress per row and stops when the monitor asks for it.
	 */
	public void testProgressAndCancel() throws IOException {
		DefaultTableView<Ticket> view = view();
		TableViewExcelExport export = export();
		List<Integer> reported = new ArrayList<>();
		ExportMonitor monitor = new ExportMonitor() {
			@Override
			public void progress(int done, int total) {
				assertEquals(5, total);
				reported.add(done);
			}

			@Override
			public void checkCancelled() {
				if (reported.size() == 3) {
					throw new AbortExecutionException("cancelled", null);
				}
			}
		};

		try {
			export.write(export.snapshot(view), "tickets", monitor);
			fail("Export must stop when cancelled.");
		} catch (AbortExecutionException ex) {
			// Expected.
		}
		assertEquals(List.of(1, 2, 3), reported);
	}

	private static Sheet sheet(TableViewExcelExport export, DefaultTableView<Ticket> view) throws IOException {
		BinaryData data = export.write(export.snapshot(view), "tickets", ExportMonitor.NONE);
		try (InputStream in = data.getStream()) {
			return WorkbookFactory.create(in).getSheetAt(0);
		}
	}

	private static List<String> rowTexts(Sheet sheet, int row) {
		List<String> result = new ArrayList<>();
		for (Cell cell : sheet.getRow(row)) {
			result.add(cell.getCellType() == CellType.NUMERIC
				? Integer.toString((int) cell.getNumericCellValue())
				: cell.getStringCellValue());
		}
		return result;
	}

	private static String text(Sheet sheet, int row, int column) {
		return sheet.getRow(row).getCell(column).getStringCellValue();
	}

	private static boolean isBold(Sheet sheet, int row) {
		Row sheetRow = sheet.getRow(row);
		Font font = sheet.getWorkbook().getFontAt(sheetRow.getCell(0).getCellStyle().getFontIndex());
		return font.getBold();
	}

	/**
	 * The suite running this test with the services the export needs.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestTableViewExcelExport.class,
				LabelProviderService.Module.INSTANCE,
				POITypeProvider.Module.INSTANCE));
	}

}
