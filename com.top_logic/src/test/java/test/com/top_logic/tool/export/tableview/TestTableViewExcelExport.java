/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export.tableview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.Test;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
import com.top_logic.table.TreeStructure;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;
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
			// Displayed by a control, as an attribute column of a form displays its values.
			.renderer(status -> new CellContent.Raw(status))
			.searchText(status -> status)
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
	 * Writes the {@link Column#exportValue(Object) export value} of a column whose cell value is a
	 * carrier of the displayed value - as a column built for a filter that needs the row as well.
	 */
	public void testExportValue() throws IOException {
		record Carrier(int effort, Ticket row) {
			// The cell value of the column.
		}
		List<Column<Ticket, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<Ticket, Carrier> builder("effort", t -> new Carrier(t.effort(), t))
			.label(ResKey.text("Effort"))
			.exportValue(Carrier::effort)
			.build());
		DefaultTableView<Ticket> view = DefaultTableView.create(columns, new ListRowSource<>(tickets(), columns));

		Sheet sheet = sheet(export(), view);

		assertEquals(5.0, sheet.getRow(1).getCell(0).getNumericCellValue());
	}

	/**
	 * Writes the elements of a collection value as a single value of their kind is written.
	 */
	public void testCollectionOfBooleans() throws IOException {
		List<Column<Ticket, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<Ticket, List<Boolean>> builder("flags", t -> List.of(true, false))
			.label(ResKey.text("Flags"))
			.build());
		DefaultTableView<Ticket> view = DefaultTableView.create(columns, new ListRowSource<>(tickets(), columns));

		Sheet sheet = sheet(export(), view);

		String text = sheet.getRow(1).getCell(0).getStringCellValue();
		assertFalse("Booleans of a collection must be labelled: " + text, text.startsWith(";"));
		assertEquals(2, text.split("; ").length);
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
		BinaryData data = export.write(export.snapshot(view), "tickets.xls", ExportMonitor.NONE).data();

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
		Snapshot snapshot = export.snapshot(view);
		view.filter("name", TextFilterState.contains("Login"));

		BinaryData data = export.write(snapshot, "tickets", ExportMonitor.NONE).data();

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

	/**
	 * Decides the format by the template when there is one, and makes the name say it: a dot inside
	 * the name is no extension, and an extension contradicting the template is replaced.
	 */
	public void testTemplateDecidesFormat() throws IOException {
		DefaultTableView<Ticket> view = view();

		ExportFileCheck legacy = write(export().setTemplate(template(new HSSFWorkbook())), view, "Tickets v1.2");
		assertEquals("Tickets v1.2.xls", legacy.name());
		assertTrue(legacy.workbook() instanceof HSSFWorkbook);

		ExportFileCheck current = write(export().setTemplate(template(new XSSFWorkbook())), view, "Tickets.xls");
		assertEquals("Tickets.xlsx", current.name());
		assertTrue(current.workbook() instanceof XSSFWorkbook);
	}

	/**
	 * Recognizes an Excel extension ignoring its case.
	 */
	public void testExtensionIgnoresCase() throws IOException {
		DefaultTableView<Ticket> view = view();

		ExportFileCheck legacy = write(export(), view, "Report.XLS");
		assertEquals("Report.XLS", legacy.name());
		assertTrue(legacy.workbook() instanceof HSSFWorkbook);

		assertEquals("Report.XLSX", write(export(), view, "Report.XLSX").name());
		assertEquals("TL 8.0 Demo.xlsx", write(export(), view, "TL 8.0 Demo").name());
	}

	/**
	 * Reads the cell values when the snapshot is taken: what changes in the rows afterwards - while
	 * a background export writes - does not reach the file.
	 */
	public void testValuesReadAtSnapshot() throws IOException {
		class Counter {
			int _value = 1;
		}
		List<Counter> counters = List.of(new Counter(), new Counter());
		List<Column<Counter, ?>> columns = List.of(
			DefaultColumn.<Counter, Integer> builder("value", c -> c._value).label(ResKey.text("Value")).build());
		DefaultTableView<Counter> view = DefaultTableView.create(columns, new ListRowSource<>(counters, columns));
		TableViewExcelExport export = export();
		Snapshot snapshot = export.snapshot(view);
		counters.forEach(c -> c._value = 99);

		try (InputStream in = export.write(snapshot, "values", ExportMonitor.NONE).data().getStream();
				Workbook workbook = WorkbookFactory.create(in)) {
			assertEquals(1.0, workbook.getSheetAt(0).getRow(1).getCell(0).getNumericCellValue());
			assertEquals(1.0, workbook.getSheetAt(0).getRow(2).getCell(0).getNumericCellValue());
		}
	}

	/**
	 * Asks a renderer for its custom context once per column, not once per cell.
	 */
	public void testCustomContextPerColumn() throws IOException {
		int[] contexts = { 0 };
		ExcelCellRenderer counting = new AbstractExcelCellRenderer() {
			@Override
			public Object newCustomContext(com.top_logic.layout.table.TableModel model,
					com.top_logic.layout.table.model.Column modelColumn) {
				return Integer.valueOf(++contexts[0]);
			}

			@Override
			protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow,
					int excelColumn) {
				return new ExcelValue(excelRow, excelColumn, "ctx " + context.getCustomContext());
			}
		};
		TableViewExcelExport export = new TableViewExcelExport(column -> column.equals("name") ? counting : null);

		Sheet sheet = sheet(export, view());

		assertEquals("One context for the one column of five rows.", 1, contexts[0]);
		assertEquals("ctx 1", text(sheet, 5, 0));
	}

	/**
	 * Writes the descendants of a collapsed tree node as rows the outline hides, at their depth -
	 * nothing is left out for being collapsed.
	 */
	public void testCollapsedTreeNodes() throws IOException {
		record Node(String name, List<Node> children) {
			// Test fixture.
		}
		Node a1 = new Node("A1", List.of(new Node("A1a", List.of())));
		Node a = new Node("A", List.of(a1, new Node("A2", List.of())));
		Node b = new Node("B", List.of(new Node("B1", List.of())));
		TreeStructure<Node, Node> structure = new TreeStructure<>() {
			@Override
			public List<Node> roots() {
				return List.of(a, b);
			}

			@Override
			public List<Node> children(Node node) {
				return node.children();
			}

			@Override
			public boolean isLeaf(Node node) {
				return node.children().isEmpty();
			}

			@Override
			public Node businessObject(Node node) {
				return node;
			}
		};
		List<Column<Node, ?>> columns = List.of(
			DefaultColumn.<Node, String> builder("name", Node::name).label(ResKey.text("Name")).build());
		DefaultTableView<Node> view = DefaultTableView.create(columns, new TreeRowSource<>(structure, columns));
		// Only A is expanded: A1 and B are collapsed, A1a and B1 not displayed.
		view.setExpanded(a, true);
		assertEquals(4, view.rowCount());

		for (boolean streaming : new boolean[] { true, false }) {
			Sheet sheet = sheet(export().setStreaming(streaming), view);

			assertEquals(List.of("A", "A1", "A1a", "A2", "B", "B1"),
				List.of(text(sheet, 1, 0), text(sheet, 2, 0), text(sheet, 3, 0), text(sheet, 4, 0),
					text(sheet, 5, 0), text(sheet, 6, 0)));
			int[] levels = { 0, 1, 2, 1, 0, 1 };
			boolean[] hidden = { false, false, true, false, false, true };
			for (int n = 0; n < levels.length; n++) {
				assertEquals("Level of row " + (n + 1), levels[n], sheet.getRow(n + 1).getOutlineLevel());
				assertEquals("Hidden row " + (n + 1), hidden[n], sheet.getRow(n + 1).getZeroHeight());
			}
		}
	}

	private record ExportFileCheck(String name, Workbook workbook) {
		// Test fixture.
	}

	private static ExportFileCheck write(TableViewExcelExport export, DefaultTableView<Ticket> view, String name)
			throws IOException {
		BinaryData data = export.write(export.snapshot(view), name, ExportMonitor.NONE).data();
		try (InputStream in = data.getStream()) {
			return new ExportFileCheck(data.getName(), WorkbookFactory.create(in));
		}
	}

	private static TableViewExcelExport.Template template(Workbook workbook) throws IOException {
		workbook.createSheet("Template");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		workbook.write(buffer);
		workbook.close();
		byte[] content = buffer.toByteArray();
		return () -> new ByteArrayInputStream(content);
	}

	private static Sheet sheet(TableViewExcelExport export, DefaultTableView<?> view) throws IOException {
		BinaryData data = export.write(export.snapshot(view), "tickets", ExportMonitor.NONE).data();
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
