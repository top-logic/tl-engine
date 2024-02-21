/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.awt.Color;
import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.HorizontalAlignment;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.base.office.excel.ExcelValue.MergeRegion;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.currency.Amount;
import com.top_logic.layout.Control;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.security.ProtectedValueExportMapping;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.renderer.ColumnLabelProvider;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.ColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.TableDeclaration;
import com.top_logic.mig.html.HTMLFormatterFormat;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.Resources;

/**
 * Provide some methods to extract data from a table and pack that into
 * an ExcelValue[].
 * 
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class ExcelExportSupport {

	/**
	 * Configuration for {@link ExcelExportSupport}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * Whether script reload must be forced. See {@link Config#getClassName}.
		 */
		String CLASS_NAME = "className";

		/** Getter for {@link Config#CLASS_NAME}. */
		@Name(CLASS_NAME)
		Class<ExcelExportSupport> getClassName();
	}

	private static Class<ExcelExportSupport> excelExportClass;
	
	/** Excel color. */
	public static Color LIGHT_YELLOW = new Color(255, 204, 153);
	/** Excel color. */
    public static Color LIGHT_GREEN = new Color(204, 255, 204);
    /** Excel color. */
    public static Color LIGHT_BLUE = new Color(153, 205, 255);

    /**
	 * A cache for cell styles. Key: {@link ExcelCellFormatter} Value:
	 * {@link com.top_logic.base.office.excel.ExcelValue.CellPosition}.
	 */
    private Map<ExcelCellFormatter,CellPosition> cellStyleCache;
    
	private final ExcelCellRenderer _excelCellRenderer = new AbstractExcelCellRenderer() {

		@Override
		protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
			Format format = (Format) context.getCustomContext();
			ResourceProvider resourceProvider = context.modelColumn().getConfig().getResourceProvider();
			Object theCellValue = ExcelExportSupport.this.getExportableValue(resourceProvider, cellValue, format);
			return getExcelCellValue(cellValue, theCellValue, excelRow, excelColumn);
		}

		@Override
		public Object newCustomContext(TableModel model, Column modelColumn) {
			return HTMLFormatterFormat.INSTANCE;
		}

	};

    /**
	 * Creates a new {@link ExcelExportSupport} with a new cell style cache. If
	 * the ExcelExport should be reused call the reset caches method before.
	 */
    protected ExcelExportSupport() {
    	super();
    	
    	this.cellStyleCache = new HashMap<>();
    }
    
	/**
	 * Creates a new instance of {@link ExcelExportSupport}.
	 */
    public static synchronized ExcelExportSupport newInstance() {
    	if (excelExportClass == null) {
			excelExportClass = getClassName();
        }
    	try {
			return excelExportClass.newInstance();
    	} catch (Exception ex) {
            Logger.error("Problem creating instance for " + excelExportClass.getName(), ex , ExcelExportSupport.class);
        }
    	return null;
    }

	/**
	 * {@link ExcelCellRenderer} that is used to export when the special column does not declare an
	 * own {@link ExcelCellRenderer}.
	 */
	public final ExcelCellRenderer defaultExcelCellRenderer() {
		return _excelCellRenderer;
	}

	/**
	 * Extract the values from the given tree and return this as excel values.
	 * 
	 * @return The excel based representation of the tree.
	 */
	public ExcelValue[] excelValuesFromTree(TreeUIModel<TLTreeNode<?>> treeModel, TableDeclaration tableDeclaration) {
		return excelValuesFromTree(treeModel, tableDeclaration, 0, 0);
	}

	/**
	 * Extract the values from the given tree and return this as excel values.
	 * 
	 * @param startRow
	 *        The row to start writing the excel values for.
	 * @param startColumn
	 *        The column to start writing the excel values for.
	 * @return The excel based representation of the tree.
	 */
	public ExcelValue[] excelValuesFromTree(TreeUIModel<TLTreeNode<?>> treeModel, TableDeclaration tableDeclaration,
			int startRow, int startColumn) {
		List<ExcelValue> excelValues = new ArrayList<>();
		startRow += exportHeader(excelValues, tableDeclaration, startRow, startColumn);
		exportContent(excelValues, treeModel, treeModel.getRoot(), tableDeclaration, startRow, startColumn, 0);
		return excelValues.toArray(new ExcelValue[excelValues.size()]);
	}

	/**
	 * Exports the values for the header of the tree.
	 * 
	 * @return the amount of exported rows.
	 */
	protected int exportHeader(List<ExcelValue> excelValues, TableDeclaration tableDeclaration, int startRow, int startColumn) {
		List<String> columnNames = CollectionUtil.dynamicCastView(String.class, tableDeclaration.getColumnNames());
		for (String columnName : columnNames) {
			excelValues.add(getExcelTreeHeaderValue(startRow, startColumn++, columnName, tableDeclaration));
		}
		return 1;
	}

	/**
	 * Exports the values for the content of the tree.
	 * 
	 * @return the amount of exported rows.
	 */
	protected int exportContent(List<ExcelValue> excelValues, TreeUIModel<TLTreeNode<?>> treeModel, TLTreeNode<?> node,
			TableDeclaration tableDeclaration, int startRow, int startColumn, int depth) {
		
		int row = startRow;
		if (depth > 0 || treeModel.isRootVisible()) {
			List<String> columnNames = CollectionUtil.dynamicCastView(String.class, tableDeclaration.getColumnNames());
			for (int i = 0, length = columnNames.size(); i < length; i++) {
				String column = columnNames.get(i);
				if (DefaultTableDeclaration.DEFAULT_COLUMN_NAME.equals(column)) {
				    // Set label column
					int currentDepth = depth - (treeModel.isRootVisible() ? 0 : 1);
					String prefix = currentDepth == 0 ? StringServices.EMPTY_STRING
						: StringServices.getSpaces(currentDepth * getIndentSize());
					excelValues.add(getExcelTreeNodeValue(row, startColumn + i, treeModel, node,
						tableDeclaration, prefix));
				}
				else {
					excelValues.add(getExcelTreeCellValue(row, startColumn + i, column, treeModel, node,
						tableDeclaration));
				}
			}
			row++;
		}
		
		for (TLTreeNode<?> child : node.getChildren()) {
			row += exportContent(excelValues, treeModel, child, tableDeclaration, row, startColumn, depth + 1);
		}
		return row - startRow;
	}

	/**
	 * Gets the amount of spaces to use for indenting one level
	 */
	protected int getIndentSize() {
		return 5;
	}

	/**
	 * Creates an ExcelValue for the given header column.
	 */
	protected ExcelValue getExcelTreeHeaderValue(int row, int column, String columnName, TableDeclaration tableDeclaration) {
		String value = getHeaderText(columnName, tableDeclaration);
		ExcelValue excelValue = new ExcelValue(row, column, value);
		excelValue.setBackgroundColor(Color.GRAY);
		excelValue.setBorder(ExcelValue.BORDER_THIN);
		excelValue.setCellAlignment(HorizontalAlignment.LEFT);
		return excelValue;
	}

	/**
	 * Gets the displayed header text of the given column.
	 */
	protected String getHeaderText(String columnName, TableDeclaration tableDeclaration) {
		ColumnDeclaration columnDeclaration = tableDeclaration.getColumnDeclaration(columnName);
		switch (columnDeclaration.getHeaderType()) {
			case ColumnDeclaration.NO_HEADER:
				return StringServices.EMPTY_STRING;
			case ColumnDeclaration.DEFAULT_HEADER:
				return Resources.getInstance().getString(tableDeclaration.getResourcePrefix().key(columnName));
			case ColumnDeclaration.STRING_HEADER:
				return Resources.getInstance().getString(columnDeclaration.getHeaderKey());
			case ColumnDeclaration.HTML_HEADER:
				return Resources.getInstance().getString(columnDeclaration.getHeaderKey());
			case ColumnDeclaration.RENDERED_HEADER:
				Renderer<Object> headerRenderer = columnDeclaration.getHeaderRenderer();
				if (headerRenderer instanceof ResourceRenderer) {
					return ((ResourceRenderer<?>) headerRenderer).getResourceProvider().getLabel(columnName);
				}
				//$FALL-THROUGH$ to default
			default:
				return Resources.getInstance().getString(tableDeclaration.getResourcePrefix().key(columnName));
		}
	}

	/**
	 * Creates an ExcelValue for the given node cell.
	 */
	protected ExcelValue getExcelTreeNodeValue(int row, int column, TreeUIModel<TLTreeNode<?>> treeModel,
			TLTreeNode<?> node, TableDeclaration tableDeclaration, String indent) {
		String value = StringServices.nonNull(indent) + tableDeclaration.getResourceProvider().getLabel(node);
		ExcelValue excelValue = new ExcelValue(row, column, value);
		excelValue.setBackgroundColor(Color.LIGHT_GRAY);
		excelValue.setBorder(ExcelValue.BORDER_THIN);
		return excelValue;
	}

	/**
	 * Creates an ExcelValue for the given content cell.
	 */
	protected ExcelValue getExcelTreeCellValue(int row, int column, String columnName,
			TreeUIModel<TLTreeNode<?>> treeModel, TLTreeNode<?> node, TableDeclaration tableDeclaration) {
		Object value = tableDeclaration.getAccessor().getValue(treeModel.getUserObject(node), columnName);
		value = FormFieldHelper.getProperValue(value);
		// supported types of POIExcelAccess
		if (!(value == null || value instanceof Boolean || value instanceof String || value instanceof Number
			|| value instanceof Date || value instanceof Calendar)) {
			value = MetaResourceProvider.INSTANCE.getLabel(value);
		}
		return new ExcelValue(row, column, value);
	}

	/**
	 * Extract the values from the given table and return this as excel values.
	 * 
	 * @param data
	 *        The {@link TableData} to export.
	 * @return The excel based representation of the table.
	 */
	public ExcelValue[] excelValuesFromTable(TableData data) {
		return this.excelValuesFromTable(data, 0);
    }
    
    /**
	 * Extract the values from the given table and return this as excel values.
	 * 
	 * @param data
	 *        The {@link TableData} to export.
     * @param aStartRow
	 *        The row to start writing the excel values for.
	 * @return The excel based representation of the table.
	 */
	public ExcelValue[] excelValuesFromTable(TableData data, int aStartRow) {
		return excelValuesFromTable(data, false, aStartRow);
	}

	/**
	 * Extract the values from the given table and return this as excel values.
	 * 
	 * @param tableData
	 *        The {@link TableData} to export.
	 * @param complete
	 *        Whether all information should be exported (not only those currently displayed)
	 * @param aStartRow
	 *        The row to start writing the excel values for.
	 * @return The excel based representation of the table.
	 */
	public ExcelValue[] excelValuesFromTable(TableData tableData, boolean complete, int aStartRow) {
		LabelProvider columnLabels = ColumnLabelProvider.newInstance(tableData);

		return export(tableData, columnLabels, complete, aStartRow);
	}

	private ExcelValue[] export(TableData table, LabelProvider columnLabels, boolean complete,
			int aStartRow) {
		TableViewModel exportViewModel;
		if (complete) {
			exportViewModel = allInformation(table);
		} else {
			exportViewModel = table.getViewModel();
		}

		return export(exportViewModel, columnLabels, aStartRow);
	}

	private TableViewModel allInformation(TableData table) {
		TableModel exportApplicationModel;
		TableModel originalApplicationModel = table.getTableModel();
		if (originalApplicationModel instanceof TreeTableModel) {
			TreeUIModel<?> treeModel = ((TreeTableModel) originalApplicationModel).getTreeModel();

			List<?> allNodes = allNodes(treeModel);
			exportApplicationModel = new ObjectTableModel(
				originalApplicationModel.getColumnNames(),
				originalApplicationModel.getTableConfiguration(),
				allNodes);
		} else {
			exportApplicationModel = originalApplicationModel;
		}

		TableData tableData =
			DefaultTableData.createAnonymousTableData(exportApplicationModel);
		return tableData.getViewModel();
	}

	private static <N> List<N> allNodes(TreeUIModel<N> treeModel) {
		ArrayList<N> result = new ArrayList<>();

		N root = treeModel.getRoot();
		if (treeModel.isRootVisible()) {
			result.add(root);
		}
		addDescendants(result, treeModel, root);

		return result;
	}

	private static <N> void addDescendants(List<? super N> result, TreeUIModel<N> treeModel, N node) {
		for (N child : treeModel.getChildren(node)) {
			result.add(child);
			addDescendants(result, treeModel, child);
		}
	}

	private ExcelValue[] export(TableViewModel theViewModel, LabelProvider columnLabels, int aStartRow) {
		List<ExcelValue> theResult = new ArrayList<>();
		MutableInteger row = new MutableInteger(aStartRow);
		exportColumnHeaders((val, col) -> theResult.add(val), theViewModel, columnLabels, row);

		exportTableRows(val -> theResult.add(val), theViewModel, row);
		return theResult.toArray(new ExcelValue[theResult.size()]);
	}

	/**
	 * Exports the column headers (column groups and elementary columns).
	 * 
	 * <p>
	 * This methods creates {@link ExcelValue}s for the single column headers and writes them to the
	 * given consumer.
	 * </p>
	 * 
	 * @param out
	 *        {@link Consumer} to add the created {@link ExcelValue}s to.
	 * @param table
	 *        The table to export headers.
	 * @param columnLabels
	 *        {@link LabelProvider} to render the label of the column.
	 * @param row
	 *        The excel row where the first column header row is rendered to. It is incremented such
	 *        that after the method is returned the row is pointed after the last header row.
	 */
	public void exportColumnHeaders(BiConsumer<? super ExcelValue, ? super Column> out, TableViewModel table, LabelProvider columnLabels,
			MutableInteger row) {
		exportColumnGroupHeader(out, table, columnLabels, row);
		exportElementaryColumnHeader(out, table, columnLabels, row.intValue());
		row.inc();
	}

	private void exportColumnGroupHeader(BiConsumer<? super ExcelValue, ? super Column> out, TableViewModel table,
			LabelProvider columnLabels, MutableInteger row) {
		Collection<Column> groups = table.getHeader().getGroups();
		Map<Column, Integer> spanByColumn = getSpanByColumn(table, groups);
		List<Column> columns = new ArrayList<>(groups.size());

		// Set Headers for Excel
		int columnPointer = 0;

		addExportColumns(columns, groups);
		List<Column> nextColumns = new ArrayList<>();
		for (int groupHeaderLine = 0, cnt = table.getHeader().getHeaderLines() - 1; groupHeaderLine < cnt; groupHeaderLine++) {
			for (Column group : columns) {
				int groupSpan = spanByColumn.get(group).intValue();
				ExcelValue groupCell =
					createHeaderGroupValue(table, columnLabels, group, groupSpan, row.intValue(), columnPointer,
						groupHeaderLine);
				out.accept(groupCell, group);
				columnPointer += groupSpan;
				List<Column> parts = group.getParts();
				if (parts.isEmpty()) {
					assert group.isVisible();
					nextColumns.add(group);
				} else {
					addExportColumns(nextColumns, parts);
				}
			}
			columnPointer = 0;
			row.inc();

			List<Column> tmp = columns;
			tmp.clear();

			columns = nextColumns;
			nextColumns = tmp;
		}
	}

	private void exportElementaryColumnHeader(BiConsumer<? super ExcelValue, ? super Column> out, TableViewModel table,
			LabelProvider columnLabels,
			int excelRow) {
		List<Column> columns = filterExportColumns(table.getHeader().getAllElementaryColumns());
		Map<Column, Integer> spanByColumn = getSpanByColumn(table, columns);
		int columnPointer = 0;
		for (Column column : columns) {
			String theColumnName = column.getName();
			String theText = columnLabels.getLabel(theColumnName);

			ExcelValue cell = getExcelHeaderValue(theColumnName, theText, excelRow, columnPointer);
			int span = spanByColumn.get(column).intValue();
			if (span > 1) {
				cell.setMergeRegion(new MergeRegion(excelRow, columnPointer, excelRow, columnPointer + span - 1));
			}
			out.accept(cell, column);
			columnPointer += span;
        }
	}

	private void exportTableRows(Consumer<? super ExcelValue> out, TableViewModel table, MutableInteger excelRow) {
		int theRows = table.getRowCount();
        // Convert table model to ExcelValues
		if (theRows == 0) {
			return;
		}
		List<Column> columns = filterExportColumns(table.getHeader().getAllElementaryColumns());
		Map<Column, Integer> spanByColumn = getSpanByColumn(table, columns);
		int numberElementaryColumns = columns.size();
		Object[] rendererAndContext = new Object[numberElementaryColumns * 2];
		int modelRow = 0;
		RowContext rowContext = new RowContext(table, modelRow, excelRow.intValue());
		while (modelRow < theRows) {
			int columnPointer = 0;
			for (int col = 0; col < numberElementaryColumns; col++) {
				Column column = columns.get(col);
				if (modelRow == 0) {
					// initialise renderer and context
					ExcelCellRenderer excelCellRenderer = getExcelCellRenderer(column);
					rendererAndContext[2 * col] = excelCellRenderer;
					Object customContext = excelCellRenderer.newCustomContext(table, column);
					rendererAndContext[2 * col + 1] =
							new RenderContextImpl(rowContext, customContext, column, columnPointer);
					columnPointer += spanByColumn.get(column).intValue();
				}

				ExcelCellRenderer cellRenderer = (ExcelCellRenderer) rendererAndContext[2 * col];
				RenderContext context = (RenderContext) rendererAndContext[2 * col + 1];
				ExcelValue cell = cellRenderer.renderCell(context);
				out.accept(cell);
			}
			excelRow.inc();
			modelRow++;
			rowContext.updateRows(modelRow, excelRow.intValue());
		}
	}

	private Map<Column, Integer> getSpanByColumn(TableViewModel table, Collection<Column> groups) {
		HashMap<Column, Integer> result = new HashMap<>();
		for (Column group : groups) {
			addSpanByColumn(result, table, group);
		}
		return result;
	}

	private int addSpanByColumn(HashMap<Column, Integer> out, TableViewModel table, Column column) {
		List<Column> parts = column.getParts();
		int colSpan = 0;
		if (parts.isEmpty()) {
			// elementary column
			if (isExportColumn(column)) {
				colSpan = getExcelCellRenderer(column).colSpan(table, column);
			}
		} else {
			for (Column part : parts) {
				colSpan += addSpanByColumn(out, table, part);
			}
		}
		out.put(column, colSpan);
		return colSpan;
	}

	private ExcelCellRenderer getExcelCellRenderer(Column column) {
		return column.getConfig().finalExcelCellRenderer(this);
	}

	private void addExportColumns(Collection<? super Column> exportColumns, Iterable<? extends Column> allColumns) {
		for (Column column : allColumns) {
			if (isExportColumn(column)) {
				exportColumns.add(column);
			}
		}
	}

	/**
	 * From the given list, gets only export {@link Column}s.
	 */
	public List<Column> filterExportColumns(Iterable<? extends Column> allColumns) {
		List<Column> result = new ArrayList<>();
		addExportColumns(result, allColumns);
		return result;
	}

	private boolean isExportColumn(Column column) {
		return column.isVisible() && !excludeColumnFromExport(column);
	}

	protected ExcelValue getExcelCellValue(Object theRawValue, Object theCellValue,
			int theRow, int theColumn) {
		return new ExcelValue(theRow, theColumn, theCellValue);
	}

	protected ExcelValue getExcelHeaderValue(String aColumnName, String theHeaderText,
			int theRow, int theColumn) {
		return new ExcelValue(theRow, theColumn, theHeaderText);
	}

	/**
	 * Creates an {@link ExcelValue} for the header of the given column group
	 * 
	 * @param viewModel
	 *        The exported model.
	 * @param columnLabels
	 *        Labels of the columns.
	 * @param group
	 *        The column group to create header cell for.
	 * @param colSpan
	 *        The number of Excel columns that are needed by the given group
	 * @param row
	 *        The row in the exported Excel file.
	 * @param column
	 *        The column in the exported Excel file.
	 * @param groupHeaderLine
	 *        The number of the exported header row.
	 */
	protected ExcelValue createHeaderGroupValue(TableViewModel viewModel, LabelProvider columnLabels, Column group,
			int colSpan, int row, int column, int groupHeaderLine) {
		boolean hasLabel = viewModel.getHeader().hasLabel(group, groupHeaderLine);
		String groupLabel;
		if (hasLabel) {
			groupLabel = columnLabels.getLabel(group.getName());
		} else {
			groupLabel = StringServices.EMPTY_STRING;
		}
		ExcelValue groupCell = newHeaderGroupValue(row, column, group, groupLabel);
		if (hasLabel) {
			groupCell.setCellAlignment(HorizontalAlignment.CENTER);
		}
		if (colSpan > 1) {
			MergeRegion region = new MergeRegion(row, column, row, column + colSpan - 1);
			groupCell.setMergeRegion(region);
		}
		return groupCell;
	}

	/**
	 * Create a raw header cell for the given column group.
	 * 
	 * @param row
	 *        The row in the Excel file.
	 * @param column
	 *        The column in the Excel file.
	 * @param group
	 *        The group to create value for.
	 * @param groupLabel
	 *        The label of the group.
	 */
	protected ExcelValue newHeaderGroupValue(int row, int column, Column group, String groupLabel) {
		return new ExcelValue(row, column, groupLabel);
	}

	/**
	 * Whether a column should be exported or not
	 */
	public boolean excludeColumnFromExport(Column column) {
		return column.getConfig().isClassifiedBy(ColumnConfig.CLASSIFIER_NO_EXPORT);
	}

	/**
	 * Whether a column should be exported or not
	 */
	public boolean excludeColumnFromExport(int column, TableViewModel model) {
		ColumnConfiguration columnDescription = model.getColumnDescription(column);
		if (columnDescription == null) {
			return false;
		}
		return columnDescription.isClassifiedBy(ColumnConfig.CLASSIFIER_NO_EXPORT);
	}
    
    /**
	 * This method returns a list with excel values that represents a line.The
	 * column values are set to the excel values. If no column value is
	 * available <code>null</code> is used. The color is used as cell
	 * background color.
	 * 
	 * @param row
	 *            The row number (0..n).
	 * @param startColumn
	 *            The start column index (0..n).
	 * @param endColumn
	 *            The end column index (0..n).
	 * @param formatter
	 *            The cell formatter to set the style.
	 * @param infos
	 *            The column values to set. Must NOT be <code>null</code>.
	 */
    public List<ExcelValue> createFormattedLine(int row, int startColumn, int endColumn, ExcelCellFormatter formatter, ExcelInfo[] infos) {
    	// Create map (key: index - value: ExcelInfo)
    	Map<Integer, ExcelInfo> infoMap = new HashMap<>(infos.length);
    	for (int i = 0; i < infos.length; i++) {
			ExcelInfo excelInfo = infos[i];
			infoMap.put(Integer.valueOf(excelInfo.getColumn()), excelInfo);
		}
    	
    	// Create the cells
    	List<ExcelValue> rowExcelValues = new ArrayList<>();
    	
    	for (int i = startColumn; i <= endColumn; i++) {
			ExcelInfo info = infoMap.get(Integer.valueOf(i));
    		
    		if (info != null) {
    			if (info.getFormatter() != null) {
    				rowExcelValues.add(createFormattedCell(row, i, info.getValue(), info.getFormatter()));
    			} else {
    				rowExcelValues.add(createFormattedCell(row, i, info.getValue(), formatter));
    			}
    		} else {
    			rowExcelValues.add(createFormattedCell(row, i, null, formatter));
    		}
		}
    	
    	return rowExcelValues;
    }
    
    /**
	 * This method returns a list with excel values that represents a line.All
	 * excel values are set to <code>null</code>. The cell formatter is used to set
	 * the cell style.
	 * 
	 * @param row
	 *            The row number (0..n).
	 * @param startColumn
	 *            The start column index (0..n).
	 * @param endColumn
	 *            The end column index (0..n).
	 * @param cellFormatter
	 *            The ExcelCellFormatter to format the cell.
	 */
    public List<ExcelValue> createFormattedLine(int row, int startColumn, int endColumn, ExcelCellFormatter cellFormatter) {
    	List<ExcelValue> rowExcelValues = new ArrayList<>();
    	
    	for (int i = startColumn; i <= endColumn; i++) {
    		rowExcelValues.add(createFormattedCell(row, i, null, cellFormatter));
		}
    	
    	return rowExcelValues;
    }
    
    /**
	 * This method creates a new excel and formats it with the given
	 * {@link ExcelCellFormatter}. This method uses the {@link #cellStyleCache}
	 * to avoid to many cell styles by setting the style direct. Some
	 * implementations of the excel access class create for every cell a new
	 * style and that is possible for 100 cell or so but this export has maybe
	 * 1000 cells and that are to many style. Excel can't open the export file.
	 */
    public ExcelValue createFormattedCell(int aRow, int aColumn, Object aValue, ExcelCellFormatter cellFormatter) {
        CellPosition cellPosition = this.cellStyleCache.get(cellFormatter);

        ExcelValue excelValue = new ExcelValue(aRow, aColumn, aValue);
        if (cellPosition == null) {
            this.cellStyleCache.put(cellFormatter, new ExcelValue.CellPosition(aRow, aColumn));
            cellFormatter.formatCell(excelValue);
        } else {
            excelValue.setCellStyleFrom(cellPosition);
        }

        return excelValue;
    }
    
    /** 
     * This method resets the caches. After an export the caches must be reset.
     */
    public void resetCaches() {
        this.cellStyleCache = new HashMap<>();
    }

	protected Object getExportableValue(ResourceProvider resourceProvider, Object aValue, Format aFormat) {
		aValue = ProtectedValueExportMapping.INSTANCE.map(aValue);

		if (aValue instanceof FormField && ((FormField) aValue).hasValue()) {
			aValue = ((FormField) aValue).getValue();
		}
        if (aValue instanceof Collection) {
            StringBuffer theString = new StringBuffer();
			Iterator<?> theIter = ((Collection<?>) aValue).iterator();
            
            while (theIter.hasNext()) {
				theString.append(resourceProvider.getLabel(theIter.next()));
                if (theIter.hasNext()) {
                    theString.append(", ");
                }
            }
            return theString.toString();
        }
        else if (aValue instanceof Amount) {
            aValue = Double.valueOf(((Amount) aValue).getValue());
        }
        else if (aValue instanceof Control) {
            aValue = FormFieldHelper.getProperValue(aValue);
        }
        else if ((aValue != null) 
                && !(aValue instanceof Date) 
                && !(aValue instanceof Number) 
                && !(aValue instanceof String) 
                && !(aValue instanceof WebFolder)) {
            aValue = this.getCellContentFromObject(aValue);
        }

        if (aValue == null) {
            aValue = "";
        }

        return (aValue);
    }

    /** 
     * This method returns the label for the given object.  
     * 
     * @param value A value maybe <code>null</code>.
     */
    public String getLabel(Object value) {
    	return getCellContentFromObject(value);
    }
    
    /** 
     * This method extracts information from the object for display it in an excel cell.
     * 
     * @param     aContent A {@link Wrapper}.
     * @return    Returns a string to display in an excel cell. 
     */
    protected String getCellContentFromObject(Object aContent) {
        return MetaLabelProvider.INSTANCE.getLabel(aContent);
    }

	/**
	 * Getter for the configuration.
	 */
	public static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#CLASS_NAME}.
	 */
	public static Class<ExcelExportSupport> getClassName() {
		return getConfig().getClassName();
	}

}
