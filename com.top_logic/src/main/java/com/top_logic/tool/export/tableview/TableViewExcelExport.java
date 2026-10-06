/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.tableview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.streaming.ExcelWriter;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnView;
import com.top_logic.table.Group;
import com.top_logic.table.RowKind;
import com.top_logic.table.TableView;
import com.top_logic.tool.export.DefaultExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.util.Resources;

/**
 * Writes what a {@link TableView} displays into an Excel workbook.
 *
 * <p>
 * The export holds what the user sees: the {@link TableView#columns() displayed columns} in their
 * display order - without the ones that are no {@link Column#exportable() data} - and the
 * {@link TableView#rows(int, int) displayed rows} in their order, that is filtered, searched and
 * sorted as the table is. A cell is written from the typed {@link Column#exportValue(Object) value} of
 * its column, rendered by the {@link ExcelCellRenderer} configured for the column, so that a number
 * stays a number and a date a date in the sheet.
 * </p>
 *
 * <p>
 * The structure of the table is kept as an outline of the sheet: a group header row is written as a
 * bold row heading its members, which are grouped below it - all of them, the members of a group
 * the user has collapsed as rows the outline hides - and a tree node is grouped below its parent at
 * the depth it is displayed in.
 * </p>
 *
 * <p>
 * An export is made in two steps: {@link #snapshot(TableView)} takes what is to be exported from
 * the table, in the request that asks for the export, and {@link #write(Snapshot, String,
 * ExportMonitor)} writes it, which may be done on another thread while the user goes on working
 * with the table.
 * </p>
 */
public class TableViewExcelExport {

	/** Width in Excel units (1/256 of a character) of one display pixel. */
	private static final int UNITS_PER_PIXEL = 256 / 7;

	/** The largest column width Excel accepts. */
	private static final int MAX_COLUMN_WIDTH = 255 * 256;

	/** The deepest outline level Excel offers. */
	private static final int MAX_OUTLINE_LEVEL = 7;

	/** Font size of the header row, matching the export of a classic table. */
	private static final double HEADER_FONT_SIZE = 12.0;

	private final Function<String, ExcelCellRenderer> _renderers;

	private String _sheetName;

	private Template _template;

	private boolean _streaming = true;

	private boolean _autoFit = true;

	private boolean _freezeHeader = true;

	private boolean _autoFilter = true;

	/**
	 * Creates a {@link TableViewExcelExport}.
	 *
	 * @param renderers
	 *        The {@link ExcelCellRenderer} for a column, by the column's {@link Column#name() name};
	 *        {@code null} for a column written by the {@link DefaultExcelCellRenderer}.
	 */
	public TableViewExcelExport(Function<String, ExcelCellRenderer> renderers) {
		_renderers = renderers;
	}

	/**
	 * The name of the sheet the table is written to.
	 *
	 * <p>
	 * {@code null} for the sheet a {@link #setTemplate(Template) template} starts with, and for a
	 * sheet of a default name without a template.
	 * </p>
	 */
	public TableViewExcelExport setSheetName(String sheetName) {
		_sheetName = sheetName;
		return this;
	}

	/**
	 * The workbook the table is written into, {@code null} for a new, empty one.
	 *
	 * <p>
	 * The table is written into the sheet of the {@link #setSheetName(String) configured name},
	 * which is created if the template has none, and into its first sheet when no name is given.
	 * Since the rows of a template must be updated in place, a template is never written
	 * {@link #setStreaming(boolean) streaming}.
	 * </p>
	 */
	public TableViewExcelExport setTemplate(Template template) {
		_template = template;
		return this;
	}

	/**
	 * Whether the workbook is written streaming: with a small, constant amount of memory, but
	 * without the formatting of structured text within cells.
	 */
	public TableViewExcelExport setStreaming(boolean streaming) {
		_streaming = streaming;
		return this;
	}

	/**
	 * Whether the column widths are fitted to the written values, instead of following the widths
	 * the columns are displayed with.
	 */
	public TableViewExcelExport setAutoFit(boolean autoFit) {
		_autoFit = autoFit;
		return this;
	}

	/**
	 * Whether the header row - and the frozen columns of the table - stay in place while the sheet
	 * scrolls.
	 */
	public TableViewExcelExport setFreezeHeader(boolean freezeHeader) {
		_freezeHeader = freezeHeader;
		return this;
	}

	/**
	 * Whether the header row offers Excel's filter on every column.
	 */
	public TableViewExcelExport setAutoFilter(boolean autoFilter) {
		_autoFilter = autoFilter;
		return this;
	}

	/**
	 * Takes what is to be exported from the given table.
	 *
	 * <p>
	 * Called in the request asking for the export: the columns, the rows and the header labels are
	 * taken as the table displays them at that moment, so a later change of the table does not
	 * change the export. The cell values are read by {@link #write(Snapshot, String, ExportMonitor)}.
	 * </p>
	 *
	 * @param view
	 *        The table to export.
	 * @return Everything {@link #write(Snapshot, String, ExportMonitor)} needs.
	 */
	public <R> Snapshot<R> snapshot(TableView<R> view) {
		List<ExportColumn<R>> columns = new ArrayList<>();
		int frozen = 0;
		int frozenDisplayed = view.frozenColumnCount();
		int index = 0;
		for (ColumnView columnView : view.columns()) {
			Column<R, ?> column = view.column(columnView.name());
			if (column != null && column.exportable()) {
				ExcelCellRenderer renderer = _renderers.apply(column.name());
				if (renderer == null) {
					renderer = DefaultExcelCellRenderer.INSTANCE;
				}
				columns.add(new ExportColumn<>(column, label(columnView.label()), columnView.width(), renderer));
				if (index < frozenDisplayed) {
					frozen++;
				}
			}
			index++;
		}

		List<ExportRow<R>> rows = new ArrayList<>();
		int rowCount = view.rowCount();
		for (com.top_logic.table.Row<R> row : view.rows(0, rowCount)) {
			switch (row.kind()) {
				case DATA:
					rows.add(ExportRow.data(row.data(), row.depth(), false));
					break;
				case GROUP_HEADER:
					rows.add(ExportRow.group(groupCells(view, row, columns), row.depth(), !row.expanded()));
					if (!row.expanded()) {
						// The members of a collapsed group are not displayed, but they belong to the
						// data: they are exported as rows the outline hides.
						for (R member : row.group().members()) {
							rows.add(ExportRow.data(member, row.depth() + 1, true));
						}
					}
					break;
				case AGGREGATE:
					rows.add(ExportRow.group(groupCells(view, row, columns), row.depth(), false));
					break;
			}
		}
		return new Snapshot<>(columns, rows, frozen);
	}

	/**
	 * The texts of a group header or aggregation row: what the table displays in its cells, with
	 * the group's value and size in the first exported column.
	 */
	private static <R> List<String> groupCells(TableView<R> view, com.top_logic.table.Row<R> row,
			List<ExportColumn<R>> columns) {
		List<String> result = new ArrayList<>(columns.size());
		for (ExportColumn<R> column : columns) {
			result.add(text(view.cell(row, column.column().name())));
		}
		if (row.kind() == RowKind.GROUP_HEADER && !result.isEmpty()) {
			result.set(0, groupLabel(view, row.group()));
		}
		return result;
	}

	/**
	 * The heading of a group: its value as the column it is grouped by displays it, followed by the
	 * number of its rows.
	 *
	 * <p>
	 * The value is rendered here rather than taken from the table's first cell of the row: the
	 * first displayed column may be one that is not exported.
	 * </p>
	 */
	private static <R> String groupLabel(TableView<R> view, Group<R> group) {
		List<Object> values = group.key().values();
		List<String> grouping = view.state().getGrouping().columns();
		int level = values.size() - 1;
		String value = "";
		if (level >= 0 && level < grouping.size()) {
			Column<R, ?> groupColumn = view.column(grouping.get(level));
			if (groupColumn != null) {
				Object groupValue = values.get(level);
				value = text(render(groupColumn, groupValue));
				if (value.isEmpty() && groupValue != null) {
					// The column displays its values by a control rather than a text.
					value = MetaLabelProvider.INSTANCE.getLabel(groupValue);
				}
			}
		}
		return value + " (" + group.size() + ")";
	}

	@SuppressWarnings("unchecked")
	private static <R, V> CellContent render(Column<R, V> column, Object value) {
		// The value is a group key value taken from this very column.
		return column.renderer().render((V) value);
	}

	private static String text(CellContent content) {
		if (content instanceof CellContent.Text text) {
			return text.text();
		}
		if (content instanceof CellContent.Labeled labeled) {
			return labeled.text();
		}
		return "";
	}

	private static String label(ResKey key) {
		return key == null ? "" : Resources.getInstance().getString(key);
	}

	/**
	 * Writes the given snapshot into a workbook.
	 *
	 * @param snapshot
	 *        What {@link #snapshot(TableView)} took from the table.
	 * @param downloadName
	 *        The file name of the result. A name ending in {@value POIUtil#XLS_SUFFIX} produces the
	 *        legacy Excel format, every other name is completed to end in
	 *        {@value POIUtil#XLSX_SUFFIX}.
	 * @param monitor
	 *        What the progress is reported to.
	 * @return The workbook as download.
	 */
	public <R> BinaryData write(Snapshot<R> snapshot, String downloadName, ExportMonitor monitor)
			throws IOException {
		String name = downloadName;
		boolean legacy = name.endsWith(POIUtil.XLS_SUFFIX);
		if (!legacy && !name.endsWith(POIUtil.XLSX_SUFFIX)) {
			name += POIUtil.XLSX_SUFFIX;
		}

		Workbook workbook = createWorkbook(legacy);
		ExcelWriter writer = new ExcelWriter(workbook);
		writer.setAutoFit(_autoFit);
		String sheetName = sheetName(workbook);
		writer.newTable(sheetName);

		List<ExportColumn<R>> columns = snapshot.columns();
		writeHeader(writer, columns);

		List<ExportRow<R>> rows = snapshot.rows();
		int total = rows.size();
		boolean outline = false;
		int done = 0;
		for (ExportRow<R> row : rows) {
			monitor.checkCancelled();
			writer.newRow();
			int excelRow = writer.currentRow();
			if (row.isData()) {
				writeData(writer, columns, row.data(), done, excelRow);
			} else {
				writeGroup(writer, row.groupCells());
			}
			if (row.level() > 0 || row.collapsed()) {
				outline = true;
				applyOutline(workbook.getSheet(sheetName), excelRow, row);
			}
			done++;
			monitor.progress(done, total);
		}

		Sheet sheet = workbook.getSheet(sheetName);
		if (outline) {
			// A group header heads its members, so the outline's button sits on the row above them.
			sheet.setRowSumsBelow(false);
		}
		if (_freezeHeader) {
			writer.setFreezePane(snapshot.frozenColumns(), 1);
		}
		if (_autoFilter && !columns.isEmpty()) {
			sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, total), 0, columns.size() - 1));
		}
		if (!_autoFit) {
			for (int n = 0, size = columns.size(); n < size; n++) {
				sheet.setColumnWidth(n, Math.min(MAX_COLUMN_WIDTH, columns.get(n).width() * UNITS_PER_PIXEL));
			}
		}

		File file = writer.close();
		return BinaryDataFactory.createBinaryDataWithName(file, name);
	}

	private Workbook createWorkbook(boolean legacy) throws IOException {
		if (_template != null) {
			try (InputStream in = _template.open()) {
				return WorkbookFactory.create(in);
			}
		}
		if (legacy) {
			return new HSSFWorkbook();
		}
		return _streaming ? new SXSSFWorkbook(10) : new XSSFWorkbook();
	}

	private String sheetName(Workbook workbook) {
		if (_sheetName != null) {
			// Excel restricts the characters and the length of a sheet name.
			return WorkbookUtil.createSafeSheetName(_sheetName);
		}
		if (workbook.getNumberOfSheets() > 0) {
			// The sheet a template starts with.
			return workbook.getSheetName(0);
		}
		return WorkbookUtil.createSafeSheetName(label(I18NConstants.DEFAULT_SHEET_NAME));
	}

	private static <R> void writeHeader(ExcelWriter writer, List<ExportColumn<R>> columns) throws IOException {
		for (ExportColumn<R> column : columns) {
			ExcelValue header = new ExcelValue(0, 0, column.label());
			header.setBold();
			header.setFontSize(HEADER_FONT_SIZE);
			writer.write(header);
		}
	}

	private static <R> void writeData(ExcelWriter writer, List<ExportColumn<R>> columns, R data, int modelRow,
			int excelRow) throws IOException {
		int excelColumn = 0;
		for (ExportColumn<R> column : columns) {
			ExcelCellRenderer renderer = column.renderer();
			Object value = column.column().exportValue(data);
			ViewRenderContext context =
				new ViewRenderContext(value, modelRow, excelRow, excelColumn, renderer.newCustomContext(null, null));
			writer.write(renderer.renderCell(context));
			excelColumn++;
		}
	}

	private static void writeGroup(ExcelWriter writer, List<String> cells) throws IOException {
		for (String text : cells) {
			ExcelValue cell = new ExcelValue(0, 0, text);
			cell.setBold();
			writer.write(cell);
		}
	}

	/**
	 * Puts the row into the outline of the sheet: at its level, hidden when it is a member of a
	 * collapsed group, and marked as collapsed when it is the header of one.
	 *
	 * <p>
	 * The row is still in memory when this is done, also for a streaming workbook that keeps only
	 * the last rows: the outline is set row by row, immediately after a row is written.
	 * </p>
	 */
	private static void applyOutline(Sheet sheet, int excelRow, ExportRow<?> row) {
		for (int n = 0, levels = Math.min(row.level(), MAX_OUTLINE_LEVEL); n < levels; n++) {
			sheet.groupRow(excelRow, excelRow);
		}
		Row sheetRow = sheet.getRow(excelRow);
		if (sheetRow == null) {
			return;
		}
		if (row.hidden()) {
			sheetRow.setZeroHeight(true);
		}
		if (row.collapsed()) {
			if (sheetRow instanceof SXSSFRow streamed) {
				streamed.setCollapsed(Boolean.TRUE);
			} else if (sheetRow instanceof XSSFRow xssf) {
				xssf.getCTRow().setCollapsed(true);
			}
			// The legacy format offers no way to mark a single row as collapsed: its members are
			// hidden all the same.
		}
	}

	/**
	 * The source of a template workbook.
	 */
	@FunctionalInterface
	public interface Template {

		/**
		 * Opens the template's content. The caller closes the stream.
		 */
		InputStream open() throws IOException;

	}

	/**
	 * What {@link TableViewExcelExport#snapshot(TableView)} takes from a table.
	 *
	 * @param columns
	 *        The columns to export, in display order.
	 * @param rows
	 *        The rows to export, in display order.
	 * @param frozenColumns
	 *        How many of the leading exported columns are frozen in the table.
	 */
	public record Snapshot<R>(List<ExportColumn<R>> columns, List<ExportRow<R>> rows, int frozenColumns) {

		/**
		 * The number of rows the export writes below the header.
		 */
		public int rowCount() {
			return rows.size();
		}

	}

	/**
	 * A column of an export.
	 *
	 * @param column
	 *        The column definition, reading the value of a row.
	 * @param label
	 *        The header text.
	 * @param width
	 *        The width in pixels the column is displayed with.
	 * @param renderer
	 *        How a value of the column is written into a cell.
	 */
	public record ExportColumn<R>(Column<R, ?> column, String label, int width, ExcelCellRenderer renderer) {
		// Pure value type.
	}

	/**
	 * A row of an export: a data row, or a group header written from the texts the table displays.
	 *
	 * @param data
	 *        The business object of a data row, {@code null} for a group header.
	 * @param groupCells
	 *        The cell texts of a group header, {@code null} for a data row.
	 * @param level
	 *        The outline level of the row.
	 * @param hidden
	 *        Whether the row is a member of a collapsed group.
	 * @param collapsed
	 *        Whether the row heads a collapsed group.
	 */
	public record ExportRow<R>(R data, List<String> groupCells, int level, boolean hidden, boolean collapsed) {

		static <R> ExportRow<R> data(R data, int level, boolean hidden) {
			return new ExportRow<>(data, null, level, hidden, false);
		}

		static <R> ExportRow<R> group(List<String> cells, int level, boolean collapsed) {
			return new ExportRow<>(null, cells, level, false, collapsed);
		}

		/**
		 * Whether this is a data row, rather than a group header.
		 */
		public boolean isData() {
			return groupCells == null;
		}

	}

}
