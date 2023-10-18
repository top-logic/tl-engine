/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.Slice;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnBaseConfig;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.renderer.TableRendererUtil;
import com.top_logic.layout.template.WithProperties;

/**
 * Common interface for all renderers that render tables.
 * 
 *          <code>AbstractTable</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableRenderer<C extends TableRenderer.Config<?>> extends ITableRenderer, ConfiguredInstance<C> {

	/**
	 * Configuration of a {@link TableRenderer}.
	 * 
	 * <p>
	 * Instances of the configured renderer should be resolved using
	 * {@link TableRendererUtil#getInstance(com.top_logic.basic.config.InstantiationContext, PolymorphicConfiguration)}
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends TableRenderer<?>> extends PolymorphicConfiguration<I> {

		/** Name of {@link #getNumberHeaderRows()}. */
		String ATTRIBUTE_NUMBER_HEADER_ROWS = "numberHeaderRows";

		/**
		 * Number of rows in header of the table.
		 */
		@Name(Config.ATTRIBUTE_NUMBER_HEADER_ROWS)
		int getNumberHeaderRows();

		/**
		 * @see #getNumberHeaderRows()
		 */
		void setNumberHeaderRows(int numberRows);

		@ClassDefault(DefaultTableRenderer.class)
		@Override
		Class<? extends I> getImplementationClass();
	}

	/**
	 * Context information during the rendering process.
	 */
	public interface RenderInfo {

		/**
		 * The {@link TableControl} currently being rendered.
		 */
		TableControl getView();

		/**
		 * The {@link TableViewModel} of the currently rendered {@link #getView() control}.
		 */
		TableViewModel getModel();

	}

	/**
	 * Context information during the rendering process.
	 */
	public interface ColumnsInfo {

		/**
		 * The header rows for column groups.
		 * 
		 * @return List of group rows containing {@link Column column groups}, or <code>null</code>,
		 *         if the corresponding column has no group at the corresponding level.
		 */
		List<List<Column>> getGroupRows();

		/**
		 * The rendered content columns.
		 */
		List<Column> getColumns();

		/**
		 * The number of rendered content columns.
		 */
		@TemplateVariable("columnCount")
		default int getColumnCount() {
			return getColumns().size();
		}

		/**
		 * The {@link Column} at the given index.
		 */
		default Column getColumn(int columnIndex) {
			return getColumns().get(columnIndex);
		}

	}

	/**
	 * Context information during the rendering process.
	 */
	public interface RenderState extends RenderInfo, ColumnsInfo, WithProperties {

		/**
		 * The outermost {@link TableRenderer} currently rendering.
		 * 
		 * <p>
		 * All this-calls within a {@link TableRenderer} to methods that are implementations of
		 * methods declared in the interface {@link TableRenderer} must be directed over an explicit
		 * proxy reference. This allows to dynamically plug in a renderer extension that partially
		 * extend the functionality without declaring a subclass of this renderer.
		 * </p>
		 */
		TableRenderer<?> getRenderer();

		/**
		 * Whether the title bar should be rendered.
		 */
		boolean shouldWriteTitle();

		/**
		 * See {@link TableConfiguration#getShowColumnHeader()}
		 */
		boolean shouldWriteHeader();

		/**
		 * The CSS class to assign to title rows.
		 * 
		 * @see TableRenderer#computeTRTitleClass()
		 */
		String getTRTitleClass();

		/**
		 * The CSS class to assign to header rows.
		 * 
		 * @see TableRenderer#computeTRHeaderClass()
		 */
		String getTRHeaderClass();

		/**
		 * The CSS class to assign to footer rows.
		 * 
		 * @see TableRenderer#computeTRFooterClass()
		 */
		String getTRFooterClass();

		/**
		 * The CSS class to assign to grouped table header cells.
		 * 
		 * @see TableRenderer#computeTHGroupClass()
		 */
		String getTHGroupClass();

		/**
		 * The CSS class to assign to header cells of the given column.
		 * 
		 * @see TableRenderer#computeTHClass(Column)
		 */
		String getTHClass(int columnIndex);

		/**
		 * The CSS class to assign to content cells of the given column.
		 * 
		 * @param selected
		 *        Whether the cell's row is selected.
		 * 
		 * @see TableRenderer#computeTHClass(Column)
		 */
		String getTDClass(int columnIndex, boolean selected);

		/**
		 * State identifying the given coordinates.
		 * 
		 * @param row
		 *        The cell's row.
		 * @param column
		 *        The cell's column.
		 * @return Context object for the given cell.
		 */
		Cell getCell(int row, int column);

		/**
		 * Whether a fixed row height is used.
		 * 
		 * @return <code>true</code> use fixed row height, <code>false</code> row height is variable
		 */
		boolean useFixedRowHeight();

		/**
		 * Writes the table.
		 */
		public void writeTable(DisplayContext context, TagWriter out) throws IOException;

		/**
		 * Writes the given row index.
		 */
		public void writeRow(DisplayContext context, TagWriter out, int row) throws IOException;
	}

	/**
	 * Reference to the cell currently being rendered.
	 */
	public interface Cell extends RenderInfo {

		/**
		 * Index of the currently rendered row.
		 */
		int getRowIndex();

		/**
		 * Index of the currently rendered column.
		 */
		int getColumnIndex();

		/**
		 * The currently rendered {@link Column}.
		 */
		Column getColumn();

		/**
		 * Name of the currently rendered column.
		 */
		String getColumnName();

		/**
		 * The application value of the current cell.
		 */
		Object getValue();

		/**
		 * The row object of the current cell.
		 * 
		 * @see TableModel#getRowObject(int)
		 */
		Object getRowObject();

		/**
		 * The current {@link RenderState}.
		 */
		RenderState getRenderState();

		/**
		 * Whether the value for the cell exists.
		 * 
		 * @see CellExistenceTester
		 */
		boolean cellExists();
	}

	/** flag used in writeRow to indicate that the row is selected */
	public static final int SELECTED = 0x01;

	/** flag used in writeRow to indicate that the row is the last one */
	public static final int LAST = 0x04;

	/**
     * Write this table to HTML.
     */
    public void write(DisplayContext context, RenderState state, TagWriter out)
        throws IOException;

	/**
	 * Writes the header of the column with the given index
	 * @param out
	 *        the stream to write content to
	 * @param row
	 *        the number of the header row to write
	 * @param column
	 *        the index of the column whose header must be written
	 * 
	 * @throws IOException
	 *         iff output stream throws some
	 */
	public void writeColumnHeader(DisplayContext context, TagWriter out, RenderState state, int row, int column) 
		throws IOException;

	/**
	 * Writes a cell of the table
	 * 
	 * @param out
	 *        the stream to write content to
	 * @param isSelected
	 *        whether the row is selected
	 * @param column
	 *        the index of the column whose header must be written
	 * @param displayedRow
	 *        the index of the row in the list of displayed rows
	 * @param row
	 *        the index of the row currently written
	 * @param leftOffset
	 *        Summed with of all previous fixed columns.
	 * @throws IOException
	 *         iff output stream throws some
	 */
	public void writeColumn(TagWriter out, DisplayContext context, RenderState state, boolean isSelected, int column,
			int displayedRow, int row, int leftOffset) throws IOException;

	/**
	 * Renders the content of the cell at given column and row.
	 * 
	 * @param out
	 *        the stream to write content to
	 * @param isSelected
	 *        whether the written row is selected
	 * @param column
	 *        the index of the column whose header must be written
	 * @param row
	 *        the index of the row currently written
	 * 
	 * @throws IOException
	 *         iff output stream throws some
	 */
	public void writeCellContent(DisplayContext context, TagWriter out, RenderState state, boolean isSelected,
			int column, int row) throws IOException;
    
	/**
	 * Write the footer area of the table.
	 * The optionally displayed size can be set manually (e.g. fetched from returnvalue of 
	 * AbstractTable#writeContent(...), see AbstractTable#write(...) ).
	 * @param    aNumber     The number of written elements.
	 */
    public void writeFooter(DisplayContext context, TagWriter out, RenderState state, int aNumber) throws IOException;

	/**
	 * Additional CSS class rendered to the table control tag.
	 * 
	 * <p>
	 * This class can be used to customize styles by prepending this class to selectors, e.g.:
	 * </p>
	 * 
	 * <pre>
	 * div.myCustom td.{@link DefaultTableRenderer#TABLE_BODY_CELL_CSS_CLASS} {
	 *    ...
	 * }
	 * </pre>
	 */
    public void setCustomCssClass(String value);

	/**
	 * Creates a new copy of this renderer, i.e. writing a table with the copy is the same as
	 * writing with this instance
	 */
    public TableRenderer<?> cloneRenderer();

	/**
	 * Produces {@link ClientAction}s to the given {@link UpdateQueue} to bring the client in a
	 * consistent state.
	 * 
	 * <p>
	 * <b>Note:</b> In case of updates within table rows implementors must ensure to completely
	 * redraw the table row using the hook
	 * {@link TableControl#addRowScope(Renderer, DisplayContext, TagWriter, int)}.
	 * </p>
	 * 
	 * @param updates
	 *        the queue to add updates to
	 * @param firstRow
	 *        the first row which needs to be redrawn
	 * @param lastRow
	 *        the last row which needs to be redrawn
	 */
	public default void addUpdateActions(UpdateQueue updates, RenderState state, int firstRow, int lastRow) {
		final TableViewModel model = state.getView().getViewModel();

		int firstRowToUpdate = firstRow;
		int lastRowToUpdate = lastRow;

		for (Slice sliceRequest : model.getSliceRequest()) {
			if (hasIntersectedRange(firstRowToUpdate, lastRowToUpdate, sliceRequest)) {
				firstRowToUpdate = Math.min(firstRowToUpdate, sliceRequest.getFirstRow());
				lastRowToUpdate = Math.max(lastRowToUpdate, sliceRequest.getLastRow());

				model.removeSliceRequest(sliceRequest);
				model.addSlice(sliceRequest);
			}
		}

		updates.add(createRowReplacementUpdate(state, firstRowToUpdate, lastRowToUpdate));
	}
	
	private boolean hasIntersectedRange(int firstRow, int lastRow, final Slice sliceRequest) {
		return sliceRequest.getFirstRow() <= lastRow && sliceRequest.getLastRow() >= firstRow;
	}

	private ClientAction createRowReplacementUpdate(RenderState state, int firstRow, int lastRow) {
		HTMLFragment rowsFragment = TableUtil.createTableRowsFragment(state, firstRow, lastRow);

		String firstRowID = getRowID(state.getView(), firstRow);
		String lastRowID = getRowID(state.getView(), lastRow);

		return new RangeReplacement(firstRowID, lastRowID, rowsFragment);
	}

	/**
	 * Gets custom styles for table TRs.
	 * 
	 * @return map row index --> TR style string e.g. Integer.valueOf(5) --> "background-color:red;"
	 */
    public Map<Integer, String> getRowTRStyles();

	/**
	 * Construct the CSS class attribute for title rows.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTRTitleClass();

	/**
	 * Construct the CSS class attribute for header rows.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTRHeaderClass();

	/**
	 * Construct the CSS class attribute for footer rows.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTRFooterClass();

	/**
	 * Construct the CSS class attribute for grouped header cells.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTHGroupClass();

	/**
	 * Construct the CSS class attribute for header cells of the given column.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTHClass(Column column);

	/**
	 * Construct the CSS class attribute for content cells of the given column.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTDClass(Column column);

	/**
	 * Construct the CSS class attribute for cells of a selected row in the given column.
	 * 
	 * <p>
	 * A value of <code>null</code> means no CSS class.
	 * </p>
	 */
	String computeTDClassSelected(Column column);

	/**
	 * Hook to provide a renderer-specific column width.
	 * 
	 * @param theCD
	 *        The {@link ColumnConfiguration} of the column for which a width is requested.
	 * @return The width style of the column.
	 */
	@FrameworkInternal
	public String hookGetColumnWidth(ColumnConfiguration theCD);

	/**
	 * This method returns the client side ID of the part of the row.
	 * 
	 * @param row
	 *        the row for which the ID is demanded.
	 * @return never <code>null</code>
	 */
	public String getRowID(TableControl view, int row);

	/**
	 * @deprecated Use nested {@link ColumnConfiguration}s instead
	 */
	@Deprecated
	int getNumberHeaderRows();

	/**
	 * Including the {@link ColumnBaseConfig#getAdditionalHeaders() additional headers}, if there
	 * are such.
	 */
	default int getAllHeaderRowCount(RenderState state) {
		int normalHeaderRows = state.getRenderer().getNumberHeaderRows();
		return normalHeaderRows + countAdditionalHeaders(state);
	}

	void writeGroupColumnContent(DisplayContext context, TagWriter out, RenderState state, Column group,
			int columnIndex, int groupSpan,
			String label) throws IOException;

	/**
	 * Writes the {@link ColumnBaseConfig#getAdditionalHeaders() additional header}.
	 * <p>
	 * If there is no header row at the this index, nothing is done.
	 * </p>
	 * 
	 * @param context
	 *        Is not allowed to be null.
	 * @param out
	 *        Is not allowed to be null.
	 * @param state
	 *        Is not allowed to be null.
	 * @param headerRowNumber
	 *        The number in the list of additional headers, not the total row number. That means the
	 *        first additional row number is 0, even if there are normal header rows before it.
	 */
	void writeAdditionalHeader(DisplayContext context, TagWriter out, RenderState state,
			int headerRowNumber, int columnNumber) throws IOException;

	/**
	 * The number of {@link ColumnBaseConfig#getAdditionalHeaders() additional headers}.
	 * 
	 * @param state
	 *        Is not allowed to be null.
	 */
	default int countAdditionalHeaders(RenderState state) {
		return state
			.getColumns()
			.stream()
			.map(Column::getConfig)
			.map(ColumnConfiguration::getAdditionalHeaders)
			.mapToInt(Collection::size)
			.max()
			.orElse(0);
	}

	/**
	 * Whether the row with the given number is an {@link ColumnConfig#getAdditionalHeaders()
	 * additional header}.
	 */
	default boolean isAdditionalHeader(int rowNumber) {
		return rowNumber >= getNumberHeaderRows();
	}

}
