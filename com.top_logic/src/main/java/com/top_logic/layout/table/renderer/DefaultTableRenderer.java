/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.InvisibleView;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.TextView;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.control.TableHeaderSelectionControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.DefaultCellRenderer;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.Slice;
import com.top_logic.layout.table.control.TableControl.SortCommand;
import com.top_logic.layout.table.control.TableControl.TableCommand;
import com.top_logic.layout.table.control.TableUpdateAccumulator.UpdateRequest;
import com.top_logic.layout.table.display.ClientDisplayData;
import com.top_logic.layout.table.display.ColumnAnchor;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.model.AdditionalHeaderControlModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnBaseConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.template.MapWithProperties;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * Renderer for tables.
 *
 * <p>
 * See also:
 * http://www.w3.org/TR/xhtml1/dtds.html#dtdentry_xhtml1-strict.dtd_table
 * </p>
 *
 *          modularization.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTableRenderer extends AbstractTableRenderer<DefaultTableRenderer.Config> {

	/**
	 * CSS class name to style a table row.
	 */
	public static final String TABLE_ROW_CSS_CLASS = "tl-table__row";

	/**
	 * CSS class name to style a tables group cell.
	 */
	public static final String TABLE_GROUP_CELL_CSS_CLASS = "tl-table__group-cell";

	/**
	 * CSS class name to style a table body cell.
	 */
	public static final String TABLE_BODY_CELL_CSS_CLASS = "tl-table__cell";

	/**
	 * CSS class name to style a table header cell.
	 */
	public static final String TABLE_HEADER_CELL_CSS_CLASS = "tl-table__cell";

	/**
	 * CSS class name to style a tables header cell content.
	 */
	public static final String TABLE_HEADER_CELL_CONTENT_CSS_CLASS = "tl-table__header-cell-content";

	/**
	 * CSS class name to style a tables cell that is selected.
	 */
	public static final String TABLE_CELL_SELECTED_CSS_CLASS = "tl-table__cell--selected";

	/**
	 * CSS class name to style a tables cell that is selectable.
	 */
	public static final String TABLE_CELL_SELECTABLE_CSS_CLASS = "tl-table__cell--selectable";

	/**
	 * CSS class name to style a tables paging footer.
	 */
	public static final String TABLE_FOOTER_PAGER_CSS_CLASS = "tl-table__footer-pager";

	/**
	 * CSS class name to style a tables counting footer.
	 */
	public static final String TABLE_FOOTER_COUNTER_CSS_CLASS = "tl-table__footer-counter";

	/**
	 * CSS class name to style a tables container that contains the icons to filter and sort a
	 * table.
	 */
	private static final String TABLE_FILTER_SORT_ICON_CONTAINER_CSS_CLASS = "tl-table__filter-sort-container";

	/**
	 * CSS class name to style a tables icons to filter and sort a table.
	 */
	private static final String TABLE_FILTER_SORT_ICON_CSS_CLASS = "tl-table__filter-sort-icons";

	private static final String RIGHT_CSS_CLASS = "tblRight";
	
	public static final String CELL_INNER_SPACER_CSS_CLASS = "tblCellInnerSpacer";

	public static final String CELL_ADJUSTMENT_CSS_CLASS = "tblCellAdjustment";

	/**
	 * Filename without its extension for the paging icon to go to the first table page.
	 */
	public static final String TABLE_FIRST_PAGE_ICON_NAME = "tblFirst";

	/**
	 * Filename without its extension for the disabled paging icon to go to the first table page.
	 */
	public static final String TABLE_DISABLED_FIRST_PAGE_ICON_NAME = "tblFirstDisabled";

	/**
	 * Filename without its extension for the paging icon to go to the previous table page.
	 */
	public static final String TABLE_PREVIOUS_PAGE_ICON_NAME = "tblPrev";

	/**
	 * Filename without its extension for the disabled paging icon to go to the previous table page.
	 */
	public static final String TABLE_DISABLED_PREVIOUS_PAGE_ICON_NAME = "tblPrevDisabled";

	/**
	 * Filename without its extension for the paging icon to go to the next table page.
	 */
	public static final String TABLE_NEXT_PAGE_ICON_NAME = "tblNext";

	/**
	 * Filename without its extension for the disabled paging icon to go to the next table page.
	 */
	public static final String TABLE_DISABLED_NEXT_PAGE_ICON_NAME = "tblNextDisabled";

	/**
	 * Filename without its extension for the paging icon to go to the last table page.
	 */
	public static final String TABLE_LAST_PAGE_ICON_NAME = "tblLast";

	/**
	 * Filename without its extension for the disabled paging icon to go to the last table page.
	 */
	public static final String TABLE_DISABLED_LAST_PAGE_ICON_NAME = "tblLastDisabled";

	/**
	 * CSS class name to style a table header row.
	 */
	public static final String TABLE_HEADER_ROW = "header";

	/**
	 * CSS class name to style a table title row.
	 */
	public static final String TABLE_TITLE_ROW = "title";

	/**
	 * CSS class name to style a table footer row.
	 */
	public static final String TABLE_FOOTER_ROW = "footer";

	/**
	 * When the fixed columns count is not configured.
	 */
	public static final int NO_FIXED_COLUMNS_CONFIGURED = -1;

	private static final int DEFAULT_COLUMN_WIDTH = 150;

	/**
	 * Default column width in pixels.
	 */
	public static final String DEFAULT_COLUMN_WIDTH_STYLE = "width:" + DEFAULT_COLUMN_WIDTH + "px;";

	private static final String ACTIVE_FILTER_CSS_CLASS = "activeFilter";
	public interface Config extends AbstractTableRenderer.Config<DefaultTableRenderer> {
		@Name(GLOBAL_FULLFOOTER_PROPERTY_KEY)
		Boolean getFullFooter();

		void setFullFooter(Boolean value);

		@Name(ATTRIBUTE_USE_COLUMN_WIDTH_DECLARATION_BASED_LAYOUT)
		@BooleanDefault(false)
		boolean getUseColumnWidthDeclarationBasedLayout();

		void setUseColumnWidthDeclarationBasedLayout(boolean value);

		@IntDefault(1)
		@Override
		int getNumberHeaderRows();

	}

	/**
	 * Information about the columns in current rendering process.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class DefaultColumnsInfo implements ColumnsInfo {

		private final List<List<Column>> _groupRows;

		private final List<Column> _columns;

		/**
		 * Creates a new {@link DefaultColumnsInfo}.
		 */
		public DefaultColumnsInfo(Header header) {
			int headerLines = header.getHeaderLines();
			int groupRowCount = headerLines - 1;
			_groupRows = new ArrayList<>(groupRowCount);

			Collection<Column> groups = header.getGroups();
			List<Column> columns = new ArrayList<>(groups.size());
			addVisible(columns, groups);

			for (int line = 0; line < groupRowCount; line++) {
				ArrayList<Column> nextLine = new ArrayList<>();
				for (int n = 0, cnt = columns.size(); n < cnt; n++) {
					Column column = columns.get(n);

					if (header.hasLabel(column, line)) {
						addVisible(nextLine, column.getParts());
					} else {
						// Keep group for next line.
						nextLine.add(column);
					}
				}

				_groupRows.add(columns);
				columns = nextLine;
			}

			_columns = columns;
		}

		private static void addVisible(List<Column> buffer, Collection<Column> columns) {
			for (Column part : columns) {
				if (part.isVisible()) {
					buffer.add(part);
				}
			}
		}

		@Override
		public List<Column> getColumns() {
			return _columns;
		}

		@Override
		public List<List<Column>> getGroupRows() {
			return _groupRows;
		}

	}

	/**
	 * Information about the current rendering process.
	 */
	public static class DefaultRenderState extends DefaultColumnsInfo implements RenderState, Cell {

		private final TableControl _view;

		private final TableViewModel _model;

		private final DefaultTableRenderer _renderer;

		private final String _trTitleClass;

		private final String _trHeaderClass;

		private final String _trFooterClass;

		private final String _thGroupClasses;

		private final List<String> _thClasses;

		private final List<String> _tdClasses;

		private final List<String> _tdSelectedClasses;

		private int _rowIndex;

		private int _columnIndex;

		private final WithPropertiesDelegate _propertyDelegate;

		/**
		 * Creates a {@link DefaultRenderState}.
		 * 
		 * @param renderer
		 *        See {@link #getRenderer()}
		 * @param view
		 *        See {@link #getView()}.
		 */
		public DefaultRenderState(DefaultTableRenderer renderer, TableControl view) {
			super(view.getViewModel().getHeader());
			_renderer = renderer;
			_view = view;
			_model = view.getViewModel();

			_trTitleClass = renderer.computeTRTitleClass();
			_trHeaderClass = renderer.computeTRHeaderClass();
			_trFooterClass = renderer.computeTRFooterClass();
			_thGroupClasses = renderer.computeTHGroupClass();
			_thClasses = new ArrayList<>(getColumnCount());
			_tdClasses = new ArrayList<>(getColumnCount());
			_tdSelectedClasses = new ArrayList<>(getColumnCount());
			for (Column column : getColumns()) {
				_thClasses.add(renderer.computeTHClass(column));
				_tdClasses.add(renderer.computeTDClass(column));
				_tdSelectedClasses.add(renderer.computeTDClassSelected(column));
			}

			_propertyDelegate = WithPropertiesDelegateFactory.lookup(getClass());
		}

		@Override
		public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
			return _propertyDelegate.getPropertyValue(this, propertyName);
		}

		@Override
		public Optional<Collection<String>> getAvailableProperties() {
			return Optional.of(_propertyDelegate.getAvailableProperties(this));
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
			_propertyDelegate.renderProperty(context, out, this, propertyName);
		}

		@Override
		public TableRenderer<?> getRenderer() {
			return _renderer;
		}

		@Override
		public TableControl getView() {
			return _view;
		}

		@Override
		public int getRowIndex() {
			return _rowIndex;
		}

		void setRowIndex(int rowIndex) {
			_rowIndex = rowIndex;
		}

		@Override
		public int getColumnIndex() {
			return _columnIndex;
		}

		void setColumnIndex(int columnIndex) {
			_columnIndex = columnIndex;
		}

		@Override
		public String getColumnName() {
			return _model.getColumnName(_columnIndex);
		}

		@Override
		public TableViewModel getModel() {
			return _model;
		}

		@Override
		public Object getValue() {
			return _model.getValueAt(_rowIndex, _columnIndex);
		}

		@Override
		public Object getRowObject() {
			return _model.getRowObject(_rowIndex);
		}

		@Override
		public RenderState getRenderState() {
			return this;
		}

		@Override
		public boolean shouldWriteTitle() {
			return getModel().getTableConfiguration().getShowTitle() && !hasExternalToolbar();
		}

		/**
		 * Whether the table has an external {@link ToolBar}.
		 */
		protected boolean hasExternalToolbar() {
			TableData tableData = _view.getTableData();
			ToolBar toolBar = tableData.getToolBar();
			if (toolBar == null) {
				return false;
			}
			return toolBar.getOwner() != tableData;
		}

		@Override
		public boolean shouldWriteHeader() {
			return getModel().getTableConfiguration().getShowColumnHeader();
		}

		@Override
		public Column getColumn() {
			return getColumns().get(_columnIndex);
		}

		@Override
		public String getTRTitleClass() {
			return _trTitleClass;
		}

		@Override
		@TemplateVariable("headerRowClasses")
		public String getTRHeaderClass() {
			return _trHeaderClass;
		}

		/**
		 * Header row styles.
		 */
		@TemplateVariable("headerRowStyles")
		public String getHeaderRowStyles() {
			return getView().getViewModel().getTableConfiguration().getHeaderStyle();
		}

		@Override
		public String getTRFooterClass() {
			return _trFooterClass;
		}

		@Override
		public String getTHGroupClass() {
			return _thGroupClasses;
		}

		@Override
		public String getTHClass(int columnIndex) {
			return _thClasses.get(columnIndex);
		}

		@Override
		public String getTDClass(int columnIndex, boolean selected) {
			return (selected ? _tdSelectedClasses : _tdClasses).get(columnIndex);
		}

		@Override
		public Cell getCell(int row, int column) {
			setRowIndex(row);
			setColumnIndex(column);
			return this;
		}

		@Override
		public boolean useFixedRowHeight() {
			return hasFixedColumns();
		}

		@Override
		public boolean cellExists() {
			return getColumn().getConfig().getCellExistenceTester().isCellExistent(getRowObject(), getColumnName());
		}

		/**
		 * Returns the id of the underlying rendered {@link TableControl}.
		 */
		@TemplateVariable("id")
		public String getID() {
			return getView().getID();
		}

		/**
		 * Writes the tables header.
		 */
		@TemplateVariable("header")
		public void writeHeader(DisplayContext context, TagWriter out) throws IOException {
			Icons.TABLE_HEADER_TEMPLATE.get().write(context, out, this);
		}

		/**
		 * Writes the table header rows.
		 */
		@TemplateVariable("headerRows")
		public void writeHeaderRows(DisplayContext context, TagWriter out) throws IOException {
			if (shouldWriteHeader()) {
				writeGroupHeaderRows(context, out);
				writeTerminalHeaderRows(context, out);
			}
		}

		private void writeGroupHeaderRows(DisplayContext context, TagWriter out) throws IOException {
			int rowIndex = 0;
			
			for (List<Column> groupRow : getGroupRows()) {
				writeHeaderRow(context, out, createGroupRowCellsFragment(groupRow, rowIndex));

				rowIndex++;
			}
		}

		private void writeTerminalHeaderRows(DisplayContext context, TagWriter out) throws IOException {
			for (int rowIndex = 0; rowIndex < getRenderer().getAllHeaderRowCount(this); rowIndex++) {
				writeHeaderRow(context, out, createHeaderRowCellsFragment(rowIndex));
			}
		}

		private void writeHeaderRow(DisplayContext context, TagWriter out, HTMLFragment cells) throws IOException {
			MapWithProperties headerRowProperties = new MapWithProperties();

			headerRowProperties.put("headerRowStyles", getHeaderRowStyles());
			headerRowProperties.put("headerRowClasses", getTRHeaderClass());
			headerRowProperties.put("rowHeight", getHeaderRowHeight(getModel()));
			headerRowProperties.put("header_row_cells", cells);

			Icons.TABLE_HEADER_ROW_TEMPLATE.get().write(context, out, headerRowProperties);
		}

		private HTMLFragment createGroupRowCellsFragment(List<Column> groupRow, int rowIndex) {
			int startColumn = 0;
			int stopColumn = getColumns().size();
			int fixedColumns = getModel().getFixedColumnCount();
			boolean hasFixedColumn = fixedColumns > 0;

			return new HTMLFragment() {

				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					if (fixedColumns == 0) {
						writeSeparatorElement(out, HTMLConstants.TH, 0);
					}

					int groupIndex = 0;
					int fixedColumnWidth = 0;
					for (Column group : groupRow) {
						int groupStart = groupIndex;
						int groupStop = groupIndex = groupStart + group.getSpan();
						int colSpan = Math.min(stopColumn, groupStop) - Math.max(startColumn, groupStart);
						if (colSpan < 1) {
							continue;
						}

						if (hasFixedColumn && groupStart <= fixedColumns - 1) {
							fixedColumnWidth = writeFixedGroupHeaderCells(rowIndex, fixedColumns, context, out,
								fixedColumnWidth, group, groupStart, groupStop);
						} else {
							writeGroupHeaderCellsInternal(context, out, colSpan, false, 0, false, group, rowIndex);
						}

					}
				}

				private int writeFixedGroupHeaderCells(int rowIndex, int fixedColumns, DisplayContext context,
						TagWriter out, int fixedColumnWidth, Column group, int groupStart, int groupStop)
						throws IOException {
					if (fixedColumns - 1 < groupStop - 1) {
						writeGroupHeaderCellsInternal(context, out, fixedColumns - groupStart,
							true,
							fixedColumnWidth,
							true, group, rowIndex);
						writeGroupHeaderCellsInternal(context, out, groupStop - fixedColumns, false,
							0,
							false, group, rowIndex);
					} else {
						writeGroupHeaderCellsInternal(context, out, groupStop - groupStart, true,
							fixedColumnWidth,
							groupStop - 1 == fixedColumns - 1, group, rowIndex);
					}

					for (int columnIndex = groupStart; columnIndex <= Math.min(fixedColumns - 1,
						groupStop - 1); columnIndex++) {
						fixedColumnWidth += getColumnWidth(DefaultRenderState.this, columnIndex);
					}

					return fixedColumnWidth;
				}
			};
		}

		private void writeGroupHeaderCellsInternal(DisplayContext context, TagWriter out, int colspan,
				boolean isFixed, int leftFixedOffset,
				boolean isLastFixedColumn, Column group, int rowIndex) throws IOException {
			MapWithProperties groupCellProperties = new MapWithProperties();

			groupCellProperties.put("styles", null);
			groupCellProperties.put("onResizeGrabberMousedownHandler", createFragmentToResizeColumn());

			if (isFixed) {
				groupCellProperties.put("isSticky", true);
				groupCellProperties.put("offset", leftFixedOffset + "px;");
			} else {
				groupCellProperties.put("isSticky", false);
			}

			groupCellProperties.put("classes", CssUtil.joinCssClasses(getTHGroupClass(), group.getCssClasses()));
			groupCellProperties.put("colspan", colspan);
			groupCellProperties.put("isRowHeader", false);
			groupCellProperties.put("label", createGroupCellLabelFragment(group, colspan, rowIndex));

			int indexOfFirstColumn = getIndexOfFirstColumn(group);

			groupCellProperties.put("firstColumnIndex", TableUtil.getClientColumnIndex(_model, indexOfFirstColumn));
			groupCellProperties.put("lastColumnIndex",
				TableUtil.getClientColumnIndex(_model, indexOfFirstColumn + (colspan - 1)));
			groupCellProperties.put("isFixedTable", hasFixedColumns());
			groupCellProperties.put("rowHeight", getHeaderRowHeight(_model));

			Icons.TABLE_HEADER_CELL_TEMPLATE.get().write(context, out, groupCellProperties);

			if (isLastFixedColumn) {
				for (int i = indexOfFirstColumn; i < indexOfFirstColumn + colspan; i++) {
					leftFixedOffset += getColumnWidth(DefaultRenderState.this, i);
				}

				writeSeparatorElement(out, HTMLConstants.TH, leftFixedOffset);
			}
		}

		private int getIndexOfFirstColumn(Column group) {
			List<Column> parts = group.getParts();

			if (parts.isEmpty()) {
				return group.getIndex();
			} else {
				return getIndexOfFirstColumn(parts.get(0));
			}
		}

		private HTMLFragment createGroupCellLabelFragment(Column group, int colspan, int rowIndex) {
			return (context, out) -> {
				MapWithProperties groupCellLabelProperties = new MapWithProperties();

				groupCellLabelProperties.put("label", createGroupCellContentLabelFragment(group, colspan, rowIndex));
				groupCellLabelProperties.put("onMousedownHandler", createFragmentToReorderColumn());

				Icons.TABLE_HEADER_GROUP_CELL_CONTENT_TEMPLATE.get().write(context, out, groupCellLabelProperties);
			};
		}

		private HTMLFragment createGroupCellContentLabelFragment(Column group, int colspan, int rowIndex) {
			return new HTMLFragment() {

				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					writeGroupColumnLabel(colspan, group, rowIndex, context, out);
				}

				private void writeGroupColumnLabel(int colspan, Column group, int rowIndex,
						DisplayContext context, TagWriter out) throws IOException {
					String label;
					if (getModel().getHeader().hasLabel(group, rowIndex)) {
						label = group.getLabel(getView().getTableData());
					} else {
						label = NBSP;
					}

					getRenderer().writeGroupColumnContent(context, out, DefaultRenderState.this, group, colspan, 0,
						label);
				}
			};
		}

		private HTMLFragment createFragmentToReorderColumn() {
			return (context, out) -> {
				out.append("TABLE.initColumnReordering(event, ");
				out.writeJsString(getView().getID());
				out.append(");");
			};
		}

		private HTMLFragment createHeaderRowCellsFragment(int rowIndex) {
			int fixedColumns = getModel().getFixedColumnCount();

			return (context, out) -> {
				int fixedColumnWidth = 0;

				for (int columnIndex = 0; columnIndex < getModel().getColumnCount(); columnIndex++) {
					if (isFixedColumn(columnIndex)) {
						writeSeparatorElement(out, HTMLConstants.TH, fixedColumnWidth);
					}

					MapWithProperties headerCellProperties = new MapWithProperties();

					headerCellProperties.put("styles", createHeaderCellStylesFragment(columnIndex));
					headerCellProperties.put("onResizeGrabberMousedownHandler", createFragmentToResizeColumn());
					appendFixedColumnProperties(headerCellProperties, columnIndex, fixedColumnWidth, fixedColumns);

					if (fixedColumns > 0) {
						fixedColumnWidth += getColumnWidth(DefaultRenderState.this, columnIndex);
					}

					headerCellProperties.put("classes", getTHClass(columnIndex));
					headerCellProperties.put("colspan", 1);
					headerCellProperties.put("isRowHeader", false);
					headerCellProperties.put("label", createHeaderCellLabelFragment(rowIndex, columnIndex));
					headerCellProperties.put("firstColumnIndex",
						TableUtil.getClientColumnIndex(_model, columnIndex));
					headerCellProperties.put("lastColumnIndex",
						TableUtil.getClientColumnIndex(_model, columnIndex));
					headerCellProperties.put("isFixedTable", hasFixedColumns());
					headerCellProperties.put("rowHeight", getHeaderRowHeight(_model));

					Icons.TABLE_HEADER_CELL_TEMPLATE.get().write(context, out, headerCellProperties);
				}

				if (hasOnlyFixedColumns()) {
					writeSeparatorElement(out, HTMLConstants.TH, fixedColumnWidth);
				}
			};
		}

		private void writeSeparatorElement(TagWriter out, String tagName, int leftOffset) {
			out.beginBeginTag(tagName);
			out.writeAttribute("class", "tl-position--sticky tl-table__fix-flex-separator");
			out.writeAttribute("style", "left:" + leftOffset + "px;");
			out.endBeginTag();
			out.endTag(tagName);
		}

		private HTMLFragment createFragmentToResizeColumn() {
			return (context, out) -> {
				out.append("TABLE.initColumnResizing(event, ");
				out.writeJsString(getView().getID());
				out.append(");");
			};
		}

		private HTMLFragment createHeaderCellStylesFragment(int columnIndex) {
			return (context, out) -> CssUtil.appendStyleOptional(out,
				getModel().getColumnDescription(columnIndex).getHeadStyle());
		}

		private HTMLFragment createHeaderCellLabelFragment(int rowIndex, int columnIndex) {
			return (context, out) -> getRenderer().writeColumnHeader(context, out, DefaultRenderState.this, rowIndex,
				columnIndex);
		}

		/**
		 * Writes the tables body.
		 */
		@TemplateVariable("body")
		public void writeBody(DisplayContext context, TagWriter out) throws IOException {
			Icons.TABLE_BODY_TEMPLATE.get().write(context, out, this);
		}

		/**
		 * Writes the onclick event handler to select a table row.
		 */
		@TemplateVariable("selectRowHandler")
		public void writeSelectRowHandler(DisplayContext context, TagWriter out) throws IOException {
			out.append("services.form.TableControl.selectRow(arguments[0], this, ");
			out.writeJsString(getView().getID());
			out.append(");");
		}

		/**
		 * Writes the table body rows of the initial slice of a viewport table.
		 */
		@TemplateVariable("body_rows")
		public void writeBodyRows(DisplayContext context, TagWriter out) throws IOException {
			if (RangeProviderUtil.hasRows(_model)) {
				Slice slice = createInitialSlice(getView());

				for (int row = slice.getFirstRow(); row <= slice.getLastRow(); row++) {
					getView().addRowScope(TableUtil.createRendererForRow(this, row), context, out, row);
				}
			}
		}

		/**
		 * Writes all table body rows defined by the table model.
		 */
		@TemplateVariable("body_rows_all")
		public void writeBodyRowsAll(DisplayContext context, TagWriter out) throws IOException {
			for (int row = 0; row < _model.getRowCount(); row++) {
				getView().addRowScope(TableUtil.createRendererForRow(this, row), context, out, row);
			}
		}

		private Slice createInitialSlice(TableControl view) {
			RenderRange renderRange = RenderRangeProvider.createRange(view.getViewModel());
			int startRow = renderRange.getFirstRow();
			int stopRow = renderRange.getLastRow();
			Slice slice = new Slice(startRow, stopRow);
			view.getViewModel().addSlice(slice);
			return slice;
		}

		@Override
		public void writeTable(DisplayContext context, TagWriter out) throws IOException {
			Icons.TABLE_CONTAINER_TEMPLATE.get().write(context, out, this);
		}

		/**
		 * The script to initialize the client-side state of the table.
		 * 
		 * <p>
		 * Must be rendered after the table container element.
		 * </p>
		 */
		@TemplateVariable("tableInitializer")
		public void writeTableInitializer(DisplayContext context, TagWriter out) throws IOException {
			out.beginScript();
			out.append("services.ajax.executeAfterRendering(window, function() {");
			out.append("TABLE.initTable('");
			out.append(_view.getID());
			out.append("', ");
			appendTableInformerCreator(_view, _model, out);
			out.append(",");
			appendClientDisplayData(out, _model);
			out.append(");");
			HTMLUtil.endScriptAfterRendering(out);
		}

		private void appendClientDisplayData(TagWriter out, TableViewModel viewModel) throws IOException {
			ClientDisplayData clientDisplayData = viewModel.getClientDisplayData();

			out.append("function getDisplayData() {");

			appendPaneRequest(out, viewModel);
			appendViewportState(out, viewModel, clientDisplayData.getViewportState());

			out.append("return { visiblePane:visiblePane, viewportState:viewportState }");
			out.append("}()");
		}

		private void appendPaneRequest(TagWriter out, TableViewModel model) throws IOException {
			VisiblePaneRequest paneRequest = PagePaneProvider.getPane(model);

			IndexRange rowRange = paneRequest.getRowRange();
			IndexRange columnRange = paneRequest.getColumnRange();

			out.append("var visiblePane = new Object();");
			out.append("visiblePane.rowRange = new Object();");
			out.append("visiblePane.rowRange.firstIndex = " + rowRange.getFirstIndex() + ";");
			out.append("visiblePane.rowRange.lastIndex = " + rowRange.getLastIndex() + ";");
			out.append(
				"visiblePane.rowRange.forcedVisibleIndexInRange = " + rowRange.getForcedVisibleIndexInRange() + ";");

			out.append("visiblePane.columnRange = new Object();");
			out.append("visiblePane.columnRange.firstIndex = "
				+ TableUtil.getClientColumnIndex(model, columnRange.getFirstIndex()) + ";");
			out.append("visiblePane.columnRange.lastIndex = "
				+ TableUtil.getClientColumnIndex(model, columnRange.getLastIndex()) + ";");
			out.append("visiblePane.columnRange.forcedVisibleIndexInRange = "
				+ TableUtil.getClientColumnIndex(model, columnRange.getForcedVisibleIndexInRange()) + ";");
		}

		private void appendViewportState(TagWriter out, TableViewModel model, ViewportState state) throws IOException {
			RowIndexAnchor rowAnchor = state.getRowAnchor();
			ColumnAnchor columnAnchor = state.getColumnAnchor();

			int columnIndex = TableUtil.getClientColumnIndex(model, model.getColumnIndex(columnAnchor.getColumnName()));

			out.append("var viewportState = new Object();");
			out.append("viewportState.rowAnchor = new Object();");
			out.append("viewportState.rowAnchor.index = " + rowAnchor.getIndex() + ";");
			out.append("viewportState.rowAnchor.indexPixelOffset = " + rowAnchor.getIndexPixelOffset() + ";");
			
			out.append("viewportState.columnAnchor = new Object();");
			out.append("viewportState.columnAnchor.index = " + columnIndex + ";");
			out.append("viewportState.columnAnchor.indexPixelOffset = " + columnAnchor.getIndexPixelOffset() + ";");
		}

		private void appendTableInformerCreator(TableControl view, TableViewModel viewModel, Appendable out)
				throws IOException {
			out.append("function createTableInformer() {");
			out.append("var tableInformer = new Object();");
			appendDisplayVersion(view, out);
			appendNumberOfColumns(viewModel, out);
			appendNumberOfHeaderRows(view, out);
			appendNumberOfFixedColumns(out);
			appendLineHeight(viewModel, out);
			appendSeperatorForMovingFixedColumns(out);
			appendRangeOfRenderedRowIndices(RenderRangeProvider.createRange(viewModel), out);
			appendRangeOfRowIndicesFitOnPage(view, out);
			out.append("return tableInformer;");
			out.append("}()");
		}

		private Appendable appendDisplayVersion(TableControl view, Appendable out) throws IOException {
			return out.append("tableInformer.displayVersion = " + String.valueOf(view.getDisplayVersion()) + ";");
		}

		private Appendable appendNumberOfColumns(TableViewModel viewModel, Appendable out) throws IOException {
			int numberOfColumns = viewModel.getColumnCount();

			if (hasFixedColumns()) {
				numberOfColumns++;
			}

			return out.append("tableInformer.numberOfColumns = " + String.valueOf(numberOfColumns) + ";");
		}

		private void appendNumberOfHeaderRows(TableControl view, Appendable out) throws IOException {
			int numberOfHeaderRows = _renderer.getNumberHeaderRows() + countAdditionalHeaders(view);

			out.append("tableInformer.numberOfHeaderRows = " + String.valueOf(numberOfHeaderRows) + ";");
		}

		private int countAdditionalHeaders(TableControl view) {
			int count = 0;
			TableViewModel viewModel = view.getTableData().getViewModel();
			for (int i = 0; i < viewModel.getColumnCount(); i++) {
				int additionalHeaders = viewModel.getColumnDescription(i).getAdditionalHeaders().size();
				count = Math.max(count, additionalHeaders);
			}
			return count;
		}

		private void appendNumberOfFixedColumns(Appendable out) throws IOException {
			String fixedColumnCount = String.valueOf(getModel().getFixedColumnCount());

			out.append("tableInformer.numberOfFixedColumns = " + fixedColumnCount + ";");
		}

		private void appendLineHeight(TableViewModel viewModel, Appendable out) throws IOException {
			out.append("tableInformer.lineHeight = " + viewModel.getTableConfiguration().getRowHeight() + ";");
		}

		private void appendSeperatorForMovingFixedColumns(Appendable out) throws IOException {
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();

			String tooltip = displayContext.getResources().getString(I18NConstants.CHANGE_FIXED_COLUMNS);

			out.append("tableInformer.tableSeparatorTooltip = ");
			TagUtil.writeJsString(out, tooltip.replace("\n", "<br/>"));
			out.append(";");
		}

		private void appendRangeOfRenderedRowIndices(RenderRange range, Appendable out) throws IOException {
			out.append("tableInformer.rangeOfRenderedRowIndices = new Object();");
			out.append("tableInformer.rangeOfRenderedRowIndices.firstRowIndex = " + range.getFirstRow() + ";");
			out.append("tableInformer.rangeOfRenderedRowIndices.lastRowIndex = " + range.getLastRow() + ";");
		}

		private void appendRangeOfRowIndicesFitOnPage(TableControl view, Appendable out) throws IOException {
			PagingModel page = view.getPagingModel();

			out.append("tableInformer.rangeOfRowIndicesFitOnPage = new Object();");
			out.append("tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex = " + getFirstPageRowIndex(page) + ";");
			out.append("tableInformer.rangeOfRowIndicesFitOnPage.lastRowIndex = " + getLastPageRowIndex(page) + ";");
		}

		private int getFirstPageRowIndex(PagingModel page) {
			if (hasPageRows(page)) {
				return page.getFirstRowOnCurrentPage();
			} else {
				return -1;
			}
		}

		private int getLastPageRowIndex(PagingModel page) {
			if (hasPageRows(page)) {
				return Math.max(0, page.getLastRowOnCurrentPage());
			} else {
				return -1;
			}
		}

		private boolean hasPageRows(PagingModel page) {
			return page.getCurrentPageSize() > 0;
		}

		@Override
		public void writeRow(DisplayContext context, TagWriter out, int rowIndex) throws IOException {
			boolean isSelected = isRowSelected(rowIndex);

			MapWithProperties rowProperties = new MapWithProperties();

			rowProperties.put("id", getRenderer().getRowID(getView(), rowIndex));
			rowProperties.put("isSelected", isSelected);
			rowProperties.put("selectedClass", "tblSelected");
			rowProperties.put("defaultClasses", getTableBodyRowClasses(rowIndex));
			rowProperties.put("body_row_cells", createBodyRowCellsFragment(rowIndex, isSelected));
			rowProperties.put("rowHeight", getBodyRowHeight(getModel()));

			appendDragProperties(rowProperties, rowIndex);

			Icons.TABLE_BODY_ROW_TEMPLATE.get().write(context, out, rowProperties);
		}

		private String getTableBodyRowClasses(int rowIndex) {
			RowClassProvider rowClassProvider = getModel().getTableConfiguration().getRowClassProvider();
			
			int options = getRowOptions(rowIndex);
			int pageRowIndex = getRowIndexOnCurrentPage(rowIndex);

			return rowClassProvider.getTRClass(_view, options, pageRowIndex, rowIndex);
		}

		private int getRowIndexOnCurrentPage(int rowIndex) {
			return rowIndex - getView().getPagingModel().getFirstRowOnCurrentPage();
		}

		private boolean isRowSelected(int rowIndex) {
			return (getRowOptions(rowIndex) & TableRenderer.SELECTED) == TableRenderer.SELECTED;
		}

		private int getRowOptions(int rowIndex) {
			int options = 0;

			if (_view.isSelectedRow(rowIndex)) {
				options = options | TableRenderer.SELECTED;
			}

			if (rowIndex == getModel().getPagingModel().getLastRowOnCurrentPage()) {
				options = options | TableRenderer.LAST;
			}

			return options;
		}

		private void appendDragProperties(MapWithProperties properties, int rowIndex) {
			TableData tableData = getView().getTableData();
			Object rowObject = getModel().getRowObject(rowIndex);

			if (tableData.getDragSource().dragEnabled(tableData, rowObject)) {
				properties.put("isDraggable", true);
				properties.put("dragImage", createDragImageFragment(rowObject, getModel().getTableConfiguration()));
			} else {
				properties.put("isDraggable", false);
			}
		}

		private HTMLFragment createDragImageFragment(Object rowObject, TableConfiguration tableConfig) {
			return (context, out) -> {
				ResourceProvider rowObjectResourceProvider = tableConfig.getRowObjectResourceProvider();

				if (rowObjectResourceProvider != null) {
					HTMLUtil.writeDragImageContent(context, out, rowObjectResourceProvider, rowObject);
				} else {
					HTMLUtil.writeDragImageContent(context, out, MetaResourceProvider.INSTANCE, rowObject);
				}

			};
		}

		private HTMLFragment createBodyRowCellsFragment(int rowIndex, boolean isSelected) {
			int fixedColumns = getModel().getFixedColumnCount();

			return (context, out) -> {
				DefaultRenderState state = DefaultRenderState.this;
				TableRenderer<?> renderer = state.getRenderer();

				int fixedColumnWidth = 0;

				for (int columnIndex = 0; columnIndex < getModel().getColumnCount(); columnIndex++) {
					if (isFixedColumn(columnIndex)) {
						writeSeparatorElement(out, HTMLConstants.TD, fixedColumnWidth);
					}

					renderer.writeColumn(out, context, state, isSelected, columnIndex, rowIndex, rowIndex,
						fixedColumnWidth);

					if (fixedColumns > 0) {
						fixedColumnWidth += getColumnWidth(DefaultRenderState.this, columnIndex);
					}
				}

				if (hasOnlyFixedColumns()) {
					writeSeparatorElement(out, HTMLConstants.TD, fixedColumnWidth);
				}
			};
		}

		/**
		 * Writes the tables footer.
		 */
		@TemplateVariable("footer")
		public void writeFooter(DisplayContext context, TagWriter out) throws IOException {
			if (_view.getViewModel().getTableConfiguration().getShowFooter()) {
				_renderer.writeFooter(context, out, this, _model.getPagingModel().getCurrentPageSize());
			}
		}

		/**
		 * True if the table layout is fixed, otherwise returns false.
		 */
		@TemplateVariable("hasFixedColumns")
		public boolean hasFixedColumns() {
			return getModel().getFixedColumnCount() >= 0;
		}

		/**
		 * Writes the tables title.
		 */
		@TemplateVariable("title")
		public void writeTitle(DisplayContext context, TagWriter out) throws IOException {
			if (shouldWriteTitle()) {
				Icons.TABLE_TITLE_TEMPLATE.get().write(context, out, this);
			}
		}

		/**
		 * An optional style value to add to the table title row.
		 */
		@TemplateVariable("titleStyle")
		public String getTitleStyle() {
			String titleStyle = _model.getTableConfiguration().getTitleStyle();
			return titleStyle;
		}

		/**
		 * Buttons to render in the tabel title row.
		 */
		@TemplateVariable("titleButtons")
		public void writeTitleButtons(DisplayContext context, TagWriter out) throws IOException {
			List<HTMLFragment> titleBarButtons = getView().getTitleBarButtons();
			for (int cnt = titleBarButtons.size(), n = 0; n < cnt; n++) {
				HTMLFragment button = titleBarButtons.get(n);
				TableButtons.writeToolbarView(context, out, button);
			}
			ToolBar toolBar = getView().getTableData().getToolBar();
			ToolbarControl.writeToolbarContents(context, out, SPAN, null, toolBar);
		}

		/**
		 * The table title.
		 */
		@TemplateVariable("titleContents")
		public void writeTitleContents(DisplayContext context, TagWriter out) throws IOException {
			_renderer.getTitleView(getView()).write(context, out);
		}

		/**
		 * Writes the tables colgroup.
		 */
		@TemplateVariable("colgroup")
		public void writeColgroup(DisplayContext context, TagWriter out) throws IOException {
			out.beginTag(COLGROUP);

			for (int columnIndex = 0; columnIndex < getModel().getColumnCount(); columnIndex++) {
				if (isFixedColumn(columnIndex)) {
					writeSeparatorColgroupColumn(out);
				}

				writeColgroupColumn(out, columnIndex);
			}

			if (hasOnlyFixedColumns()) {
				writeSeparatorColgroupColumn(out);
			}

			out.endTag(COLGROUP);
		}

		private boolean isFixedColumn(int columnIndex) {
			return columnIndex == getModel().getFixedColumnCount();
		}

		private boolean hasOnlyFixedColumns() {
			return getModel().getColumnCount() == getModel().getFixedColumnCount();
		}

		private void writeSeparatorColgroupColumn(TagWriter out) {
			out.beginBeginTag(COL);
			out.writeAttribute(STYLE_ATTR, "width: --var(TABLE_SEPARATOR_WIDTH)");
			out.endEmptyTag();
		}

		private void writeColgroupColumn(TagWriter out, int columnIndex) throws IOException {
			out.beginBeginTag(COL);
			if (hasFixedColumns()) {
				out.writeAttribute(STYLE_ATTR,
					"width:" + String.valueOf(getColumnWidth(DefaultRenderState.this, columnIndex)) + "px;");
			} else {
				out.beginAttribute(STYLE_ATTR);
				writeColumnWidthStyle(out, this, columnIndex, getColumnConfiguration(this, columnIndex));
				out.endAttribute();
			}
			out.endEmptyTag();
		}

		@Override
		public String toString() {
			return new NameBuilder(this)
				.add("table", getModel().getTableConfiguration().getTableName())
				.add("row", getRowObject())
				.add("column", getColumnName())
				.build();
		}

	}

	private static final String ADJUSTABLE_COLUMN_CLASS = "adjustableColumn";

	private static final String UNSELECTABLE_CONTENT_CLASS = "unselectableContent";

	private static final String FILTER_BUTTON_CSS_CLASS = "fltButton";
	
	/**
	 * Default renderer for table cells using {@link ResourceRenderer}.
	 */
	public static final CellRenderer DEFAULT_CELL_RENDERER = DefaultCellRenderer.INSTANCE;

	protected static final ResPrefix GENERAL_PREFIX = I18NConstants.TABLE;

	/** {@link ResKey} for ascending sort order to show the status in the tooltip of the icon. */
	protected static final ResKey SORT_ASC_KEY = GENERAL_PREFIX.key("asc");

	/** {@link ResKey} for descending sort order to show the status in the tooltip of the icon. */
	protected static final ResKey SORT_DESC_KEY = GENERAL_PREFIX.key("desc");

	/** {@link ResKey} for the tooltip of a filter icon */
	protected static final ResKey FILTER_KEY = GENERAL_PREFIX.key("filter");

	/** {@link ResKey} for the tooltip of a filter-sort icon */
	protected static final ResKey FILTER_SORT_KEY = GENERAL_PREFIX.key("filterSort");

	/** {@link ResKey} for the tooltip of a sort icon */
	protected static final ResKey SORT_KEY = GENERAL_PREFIX.key("sort");

	protected static final ThemeImage EMPTY_IMAGE = ThemeImage.themeIcon("TABLE_EMPTY");

	protected static final String RES_SUMMARY = "summary";

	protected static final String RES_FOOTER = "footer";

	/** Resource key to show for an empty List */
	protected static final String RES_EMPTY = "listEmpty";

	/** Reference key for {@link #USE_FULL_FOOTER_DEFAULT} */
	private static final String GLOBAL_FULLFOOTER_PROPERTY_KEY = "fullFooter";

	/** The summary name of this table. */
	protected String summary;

	private static final String ATTRIBUTE_USE_COLUMN_WIDTH_DECLARATION_BASED_LAYOUT = "useColumnWidthDeclarationBasedLayout";

	private boolean useColumnWidthDeclarationBasedLayout;

	/** @see #setCustomCssClass(String) */
	private String _customCssClass;

	/**
	 * Flag to indicate, whether the footer content shall include the information about the element
	 * range of the currently displayed page and the overall sum of elements, or not.
	 */
	protected boolean useFullFooter;
	
	/** Default value of property {@link #useFullFooter}, retrieved from application xml. */
	private static final boolean USE_FULL_FOOTER_DEFAULT;

    /** Holds custom styles for table TDs. */
    protected Map<String, String> columnTDStyles;

    /** Holds custom styles for table TRs. */
    protected Map<Integer, String> rowTRStyles;

    /** Holds custom styles for table TDs for concrete cells. */
    protected Map<String, String> cellTDStyles;

    /** The header alignment map. */
    protected Map<String, String> headerAlignmentMap;

    /** The header alignment map. */
    protected String defaultHeaderAlignment;

	private int _numberHeaderRows = 1;

	static {
		// Global full footer definition
		String useFullFooterDefinition =
			TableControl.GLOBAL_TABLE_PROPERTIES.getProperty(GLOBAL_FULLFOOTER_PROPERTY_KEY, "true");
		USE_FULL_FOOTER_DEFAULT = StringServices.parseBoolean(useFullFooterDefinition);
	}

	/**
	 * Create a new configured DefaultTableRenderer.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DefaultTableRenderer(InstantiationContext context, Config config) {
		super(context, config);
		this.useFullFooter = (config.getFullFooter() == null) ? USE_FULL_FOOTER_DEFAULT : config.getFullFooter();
		this.useColumnWidthDeclarationBasedLayout = config.getUseColumnWidthDeclarationBasedLayout();
		this._numberHeaderRows = config.getNumberHeaderRows();
	}

	public final void setNumberHeaderRows(int numberOfHeaderRows) {
		this._numberHeaderRows = numberOfHeaderRows;
	}

	@Override
	public final int getNumberHeaderRows() {
		return this._numberHeaderRows;
	}

	@Override
	protected final void writeControlContents(DisplayContext context, TagWriter out, TableControl control)
			throws IOException {
		if (control.getViewModel() != null) {
			write(context, new DefaultRenderState(this, control), out);
		}
	}

	/**
	 * Writes the table.
	 */
	@Override
	public void write(DisplayContext context, RenderState state, TagWriter out) throws IOException {
		state.writeTable(context, out);
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TableControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		TableViewModel viewModel = control.getViewModel();
		if (viewModel != null) {
			viewModel.clearSlices();
		}
		LayoutControlRenderer.writeLayoutConstraintInformation(out, DisplayDimension.HUNDERED_PERCENT);
	}

	public static void writeColumnDefinitionId(RenderState state, TagWriter out, int i) {
		out.writeAttribute(ID_ATTR, state.getView().getID() + "_basicColumnDefinition_" + i);
	}

	public static void writeColumnWidth(RenderState state, TagWriter out, int columnIndex)
			throws IOException {
		ColumnConfiguration theCD = getColumnConfiguration(state, columnIndex);

		out.beginAttribute(STYLE_ATTR);
		writeColumnWidthStyle(out, state, columnIndex, theCD);
		out.endAttribute();
	}

	public final int getRow(TableControl view, String rowID) {
		return Integer.parseInt(rowID.substring(view.getID().length() + 1));
	}

	@Override
	public final String getRowID(TableControl view, int row) {
		return view.getID() + '.' + row;
	}

	@Override
	public int getRow(String rowId) {
		int sepIdx = rowId.indexOf('.');
		if (sepIdx >= 0) {
			return Integer.parseInt(rowId.substring(sepIdx + 1));
		}
		return -1;
	}

	public void setUseFullFooter(boolean value) {
	    this.useFullFooter = value;
	}
	
	@Override
	public void appendControlCSSClasses(Appendable out, TableControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, _customCssClass);
	}

	@Override
	public void setCustomCssClass(String value) {
		this._customCssClass = StringServices.nonEmpty(value);
	}


	/**
	 * Sets custom styles for table TDs.
	 * 
	 * <p>
	 * <b>Attention:</b> <br/>
	 * Use of this method will modify the style values of the argument map, in case they are not
	 * terminated.
	 * </p>
	 * 
	 * @param columnTDStyles
	 *        map column name --> TD style string e.g. "HEADER_COLUMN" --> "background-color:red;"
	 */
    public void setColumnTDStyles(Map<String, String> columnTDStyles) {
    	terminateStyles(columnTDStyles);
        this.columnTDStyles = columnTDStyles;
    }

    /**
     * Gets the custom style map for table TDs.
     *
     * @return map column name --> TD style string
     */
    public Map<String, String> getColumnTDStyles() {
        return columnTDStyles;
    }

	/**
	 * Sets custom styles for table TRs.
	 * 
	 * <p>
	 * <b>Attention:</b> <br/>
	 * Use of this method will modify the style values of the argument map, in case they are not
	 * terminated.
	 * </p>
	 * 
	 * @param columnTRStyles
	 *        map row index --> TR style string e.g. Integer.valueOf(5) --> "background-color:red;"
	 */
    public void setRowTRStyles(Map<Integer, String> columnTRStyles) {
    	terminateStyles(columnTRStyles);
		if (this.rowTRStyles == null) {
			this.rowTRStyles = columnTRStyles;
		} else {
			this.rowTRStyles.putAll(columnTRStyles);
		}
    }

    /**
     * Gets the custom style map for table TRs.
     *
     * @return map row index --> TR style string
     */
    @Override
	public Map<Integer, String> getRowTRStyles() {
        return rowTRStyles;
    }

	/**
	 * Sets custom styles for table TDs.
	 * 
	 * <p>
	 * <b>Attention:</b> <br/>
	 * Use of this method will modify the style values of the argument map, in case they are not
	 * terminated.
	 * </p>
	 * 
	 * @param cellTDStyles
	 *        map column name | row index --> TD style string e.g. "HEADER_COLUMN|5" -->
	 *        "background-color:red;"
	 */
    public void setCellTDStyles(Map<String, String> cellTDStyles) {
    	terminateStyles(cellTDStyles);
        this.cellTDStyles = cellTDStyles;
    }

    /**
     * Gets the custom style map for table TDs.
     *
     * @return map column name | row index --> TD style string
     */
    public Map<String, String> getCellTDStyles() {
        return cellTDStyles;
    }

    /**
     * Sets alignments for table headers. Currently supports only
     * HTMLConstants.LEFT_VALUE and HTMLConstants.RIGHT_VALUE.
     *
     * @param headerAlignmentMap
     *        map column name --> header alignment
     *            e.g. COST_COLUMN --> LEFT_VALUE
     */
    public void setHeaderAlignmentMap(Map<String, String> headerAlignmentMap) {
        this.headerAlignmentMap = headerAlignmentMap; 
    }

    /**
     * Gets alignment map for table header alignments.
     *
     * @return map column name --> header alignment
     */
    public Map<String, String> getHeaderAlignmentMap() {
        return headerAlignmentMap;
    }

    /**
     * Sets the default alignment for table headers.
     * (HTMLConstants.LEFT_VALUE or HTMLConstants.RIGHT_VALUE).
     *
     * @param defaultHeaderAlignment
     *        the default header alignment, may be <code>null</code>
     */
    public void setDefaultHeaderAlignment(String defaultHeaderAlignment) {
        this.defaultHeaderAlignment = defaultHeaderAlignment; 
    }

    /**
     * Gets the default alignment for table headers.
     *
     * @return the default alignment for table headers, may be <code>null</code>
     */
    public String getDefaultHeaderAlignment() {
        return defaultHeaderAlignment;
    }

    /**
     * Gets the header alignment for the given column.
     * 
     * @param aColumnName
     *        the column name to get the alignment for
     * @return the header alignment for the given column; may be <code>null</code>
     */
    public String getHeaderAlignmentFor(String aColumnName) {
        String theAlign = headerAlignmentMap == null ? null : headerAlignmentMap.get(aColumnName);
        return StringServices.isEmpty(theAlign) ? defaultHeaderAlignment : theAlign;
    }

	/**
	 * The initial sorting will be ascending when this function returns true.
	 *
	 * @return true meaning ascending
	 */
	protected boolean getDefaultSortDirection() {
		return true;
	}

	@Override
	public void writeGroupColumnContent(DisplayContext context, TagWriter out, RenderState state, Column group,
			int columnIndex, int groupSpan,
			String label) {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, CELL_ADJUSTMENT_CSS_CLASS);
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, CELL_INNER_SPACER_CSS_CLASS);
			out.endBeginTag();
			{
				out.writeText(label);
			}
			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	/**
	 * Write the header row of the table.
	 * 
	 * @param rowNumber
	 *        the number of the header row to write
	 */
	public void writeHeaderRow(DisplayContext context, TagWriter out, RenderState state, int rowNumber)
			throws IOException {
		beginHeaderTR(out, state);
		out.writeAttribute(TL_COMPLEX_WIDGET_ATTR, true);
		endHeaderTR(out);
		List<Column> columns = state.getColumns();
		for (int cnt = columns.size(), column = 0; column < cnt; column++) {
			ColumnConfiguration theCD = columns.get(column).getConfig();
			out.beginBeginTag(TH);

			/* Size must also be set on cell as IE<=7 cannot determine the size of the cell using
			 * size of div.tblCellAdjustment */
			writeHeadStyle(out, state, column, theCD);
            endHeaderTR(out);

			/* DIV is used as a TH or TD can not be smaller than its content: "overflow:hidden" does
			 * work.
			 * 
			 * Moreover neither TD not TH can be offset parent for positioning of content in IE>=8
			 * or FF.
			 * 
			 * De facto the div must have annotated the same size as the enclosing TH (or TD).
			 * 
			 * The annotation is also needed on client side JS-code (table.js) */
            out.beginBeginTag(DIV);
			writeHeadContentStyle(out, state, column, theCD);
            endHeaderTR(out);
			{
				/* There must be space between the cell and its content. That cannot be done using a
				 * padding in div.tblCellAdjustment, as in IE<=7 the right padding is not applied if
				 * overflowing content is cut. Therefore this div has margin which also does that
				 * work.
				 * 
				 * Not actually needed on client for JS */
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, CELL_INNER_SPACER_CSS_CLASS);
				endHeaderTR(out);
				{
					state.getRenderer().writeColumnHeader(context, out, state, rowNumber, column);
				}
				out.endTag(DIV);
			}
            out.endTag(DIV);
            
			out.endTag(TH);
        }
        out.endTag(TR);
    }

	private void writeHeadStyle(TagWriter out, RenderState state, int column, ColumnConfiguration theCD)
			throws IOException {
		out.beginAttribute(STYLE_ATTR);
		writeColumnWidthStyle(out, state, column, theCD);
		writeConfiguredHeadStyle(out, theCD);
		out.endAttribute();
	}

	private void writeHeadContentStyle(TagWriter out, RenderState state, int column, ColumnConfiguration theCD)
			throws IOException {
		out.beginAttribute(STYLE_ATTR);
		writeContentWidthStyle(out, state, column, theCD);
		writeConfiguredHeadStyle(out, theCD);
		out.endAttribute();
	}

	private void writeConfiguredHeadStyle(TagWriter out, ColumnConfiguration theCD) throws IOException {
		CssUtil.appendStyleOptional(out, theCD.getHeadStyle());
	}

	static final void writeHeaderTR(TagWriter out, RenderState state) throws IOException {
		beginHeaderTR(out, state);
        endHeaderTR(out);
	}

	private static void beginHeaderTR(TagWriter out, RenderState state) throws IOException {
		out.beginBeginTag(TR);
		out.writeAttribute(CLASS_ATTR, state.getTRHeaderClass());
		writeHeaderStyleAttribute(out, state.getView());
	}

	private static void endHeaderTR(TagWriter out) {
		out.endBeginTag();
	}

	protected static final void writeHeaderStyleAttribute(TagWriter out, TableControl view) throws IOException {
		String headerStyle = view.getViewModel().getTableConfiguration().getHeaderStyle();
		if (!StringServices.isEmpty(headerStyle)) {
			out.writeAttribute(STYLE_ATTR, headerStyle);
		}
	}

	protected static void writeColumnWidthStyle(TagWriter out, RenderState state, int column, ColumnConfiguration theCD)
			throws IOException {
		int userAdjustedColumnWidth = state.getModel().getProgrammaticColumnWidth(column);
		if (userAdjustedColumnWidth > -1) {
			out.append("width:");
			out.writeInt(userAdjustedColumnWidth);
			out.append("px;");
		} else {
			CssUtil.appendStyleOptional(out, state.getRenderer().hookGetColumnWidth(theCD));
		}
	}

	/**
	 * Append the CSS height-attribute with the row height of the {@link TableConfiguration} only if
	 * the {@link com.top_logic.layout.table.TableRenderer.RenderState} tells to use a fixed row
	 * height.
	 */
	protected static void writeColumnHeightStyle(TagWriter out, RenderState state, boolean maxHight)
			throws IOException {
		if (state.useFixedRowHeight()) {
			int height = state.getModel().getTableConfiguration().getRowHeight();
			if (maxHight) {
				out.append("max-height:");
			} else {
				out.append("height:");
			}
			out.writeInt(height - (int) ThemeFactory.getTheme().getValue(Icons.TABLE_COLUMN_BORDER_WIDTH).getValue());
			out.append("px;");
		}
	}

	/**
	 * Produce a label for a column.
	 * 
	 * @return The label for the column.
	 */
	public static String getColumnLabel(RenderState state, int columnIndex) {
		return getColumn(state, columnIndex).getLabel(state.getView().getTableData());
	}

	private static Column getColumn(RenderState state, int column) {
		return state.getColumns().get(column);
	}

	/**
	 * Write the header content for a column.
	 * @param column
	 *            The name of the header.
	 */
	@Override
	public void writeColumnHeader(DisplayContext context, TagWriter out, RenderState state, int rowNumber, int column) throws IOException {
		if (isAdditionalHeader(rowNumber)) {
			int normalHeaderRows = state.getRenderer().getNumberHeaderRows();
			int headerRowNumber = rowNumber - normalHeaderRows;
			writeAdditionalHeader(context, out, state, headerRowNumber, column);
			return;
		}
		boolean customColumnOrder = hasCustomColumnOrder(state.getView());
		boolean sortable = state.getModel().isSortable(column);
		
		MapWithProperties headerColumnFilterProperties = new MapWithProperties();
		headerColumnFilterProperties.put("label", (HTMLFragment) (context1, out1) -> {
			Control control = (Control) DefaultTableRenderer.createColumnControl(state, column,
				ColumnConfiguration.COLUMN_CONTROL_TYPE_HEADER);

			if (control != null) {
				control.write(context1, out1);
			} else {
				out1.beginBeginTag(SPAN);
				if (customColumnOrder || sortable) {
					CssUtil.writeCombinedCssClasses(out1, ADJUSTABLE_COLUMN_CLASS, UNSELECTABLE_CONTENT_CLASS);
					out1.writeAttribute(ONSELECTSTART_ATTR, "return false;");
					// writeOnMouseDownMakeDragable(out, state.getView(), column,
					// customColumnOrder, sortable);
				}
				String tooltip = getTooltip(state, column);
				String tooltipCaption = getTooltipCaption(state.getView(), column);

				out1.endBeginTag();
				{
					if (tooltip != null) {
						out1.beginBeginTag(SPAN);
						OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context1, out1, tooltip,
							tooltipCaption);
						out1.endBeginTag();
					}
					writeHeaderContent(state, out1, column);

					if (tooltip != null) {
						out1.endTag(SPAN);
					}
				}
				out1.endTag(SPAN);
			}
		});
		headerColumnFilterProperties.put("filterSortButtons",
			(HTMLFragment) (context1, out1) -> writeFilterSortTag(context1, out1, state, column));
		headerColumnFilterProperties.put("onMousedownHandler", (HTMLFragment) (context1, out1) -> {
			out1.append("TABLE.initColumnReordering(event, ");
			out1.writeJsString(state.getView().getID());
			out1.append(");");
		});

		Icons.TABLE_HEADER_CELL_CONTENT_TEMPLATE.get().write(context, out, headerColumnFilterProperties);
	}

	/**
	 * Write the Icon to open a filter dialog and show the current sort and filter state.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 * @param state
	 *        The current {@link com.top_logic.layout.table.TableRenderer.RenderState}.
	 * @param column
	 *        Index of column in which the dialog was opened.
	 */
	protected void writeFilterSortTag(DisplayContext context, TagWriter out, RenderState state,
			int column)
			throws IOException {

		ThemeImage sortImage = null;
		ThemeImage filterImage = null;
		String filterCss = null;
		ResKey sortKey = null;

		TableViewModel model = state.getModel();
		boolean sortable = model.isSortable(column);
		boolean filterable = model.canFilter(column);

		if (filterable) {
			filterImage = Icons.FILTER;
			boolean filterActive = model.hasActiveFilterForColumn(column);
			if (filterActive) {
				filterCss = ACTIVE_FILTER_CSS_CLASS;
			}
		}


		if (sortable) {
			boolean isSortedColumn = model.isSorted(column);
			if (isSortedColumn) {
				boolean sortedAsc = model.getAscending(column);
				if (filterable) {
					sortImage = sortedAsc ? Icons.SORT_ASC_SMALL : Icons.SORT_DESC_SMALL;
				} else {
					sortImage = sortedAsc ? Icons.SORT_ASC : Icons.SORT_DESC;
				}
				sortKey = sortedAsc ? SORT_ASC_KEY : SORT_DESC_KEY;
			} else {
				sortImage = Icons.SORTABLE;
			}
		}
		writeFilterSortIcon(context, out, state, column, model, sortImage, filterImage, sortKey, filterCss);
	}


	/**
	 * Writes a combined filter and sort icon or a single filter/sort icon.
	 * 
	 * <p>
	 * If the column is sortable and filterable this method will write a combined icon to open a
	 * dialog. If it's only sortable (filterImage == null) or only filterable (sortImage == null)
	 * only one icon will be rendered but bigger as there is more space without the second icon.
	 * </p>
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 * @param state
	 *        The current {@link com.top_logic.layout.table.TableRenderer.RenderState}.
	 * @param column
	 *        Index of column in which the dialog was opened.
	 * @param model
	 *        {@link TableViewModel} to get column information.
	 * @param sortImage
	 *        The icon to display the sort state. <code>null</code> if the column is not sortable.
	 * @param filterImage
	 *        The icon to display the filter state. <code>null</code> if the column is not
	 *        filterable.
	 * @param sortStateKey
	 *        Key for the state of sorting.
	 * @param filterCss
	 *        CSS class for filter. <code>null</code> if no additional class is set.
	 */
	private void writeFilterSortIcon(DisplayContext context, TagWriter out, RenderState state, int column,
			TableViewModel model, ThemeImage sortImage, ThemeImage filterImage, ResKey sortStateKey, String filterCss)
			throws IOException {
		if (filterImage == null && sortImage == null) {
			return;
		}

		TableControl view = state.getView();
		String columnName = state.getModel().getColumnName(column);
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, view.getColumnActivateElementID(model.getColumnName(column)));
		out.writeAttribute(CLASS_ATTR, TABLE_FILTER_SORT_ICON_CONTAINER_CSS_CLASS);
		boolean filterable = filterImage == null ? false : true;
		writeOnClickOpenDialog(out, view, columnName, filterable);
		out.endBeginTag();
		{
			String headerLabel = getColumnLabel(state, column);
			String tooltip;
			String sortStateMessage = " " + Resources.getInstance().getString(sortStateKey);
			if (filterImage == null) {
				tooltip = context.getResources().getMessage(SORT_KEY, headerLabel);
				tooltip = sortStateKey != null ? tooltip + sortStateMessage : tooltip;
				writeSortImage(context, out, sortImage, null, tooltip);
			} else if (sortImage == null) {
				tooltip = context.getResources().getMessage(FILTER_KEY, headerLabel);
				writeFilterIcon(context, out, filterImage, filterCss, tooltip);
			} else {
				tooltip = context.getResources().getMessage(FILTER_SORT_KEY, headerLabel);
				tooltip = sortStateKey != null ? tooltip + sortStateMessage : tooltip;
				writeSortImage(context, out, sortImage, TABLE_FILTER_SORT_ICON_CSS_CLASS, tooltip);
				writeFilterIcon(context, out, filterImage, filterCss, tooltip);
			}

		}
		out.endTag(DIV);
	}

	/**
	 * Write icon button to open a filter dialog.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 * @param filterIcon
	 *        Icon to display the filter.
	 * @param filterCss
	 *        CSS class to style filter e.g. if the filter is active
	 * @param tooltip
	 *        Tooltip and alt-attribute of the icon.
	 */
	protected void writeFilterIcon(DisplayContext context, TagWriter out, ThemeImage filterIcon,
			String filterCss, String tooltip) throws IOException {
		XMLTag tag = filterIcon.toIcon();
		tag.beginBeginTag(context, out);
		out.beginCssClasses();
		out.append(FILTER_BUTTON_CSS_CLASS);
		out.append(filterCss);
		out.endCssClasses();
		out.writeAttribute(ALT_ATTR, tooltip);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip, null);
		tag.endEmptyTag(context, out);
	}

	/**
	 * Write the onclick function to open a filter and/or sort dialog.
	 * 
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 * @param view
	 *        The currently rendered {@link TableControl}
	 * @param columnName
	 *        Name of the column in which the dialog was opened.
	 * @param filterable
	 *        If true a filter dialog will be opened (with {@link SortCommand}s if the column is
	 *        sortable). If false only a sort dialog will be opened.
	 */
	public static void writeOnClickOpenDialog(TagWriter out, TableControl view, String columnName, boolean filterable)
			throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		if (filterable) {
			view.appendOpenFilterAction(out, columnName);
		} else {
			view.appendOpenSortAction(out, columnName);
		}
		out.endAttribute();
	}

	/**
	 * Appends the {@link #writeOnClickOpenDialog(TagWriter, TableControl, String, boolean)} with
	 * the actual open filter action.
	 * 
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 * @param view
	 *        The currently rendered {@link TableControl}
	 * @param columnName
	 *        Name of the column in which the dialog was opened.
	 */
	public static void writeOnClickOpenFilter(TagWriter out, TableControl view, String columnName) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		view.appendOpenFilterAction(out, columnName);
		out.endAttribute();
	}

	/**
	 * Write the real content of the header (without technical sorting code, etc...)
	 */
	protected void writeHeaderContent(RenderState state, TagWriter out, int column) throws IOException {
		ColumnConfiguration theColDesc = getColumnConfiguration(state, column);
		if (!theColDesc.isShowHeader()) {
			return;
		}
		String columnLabel = getColumnLabel(state, column);
		if (StringServices.isEmpty(columnLabel)) {
			columnLabel = NBSP;
		}
		out.writeText(columnLabel);
	}

	/**
	 * Hook for subclasses to add a tooltip.
	 * 
	 * @param column  the column index
	 * @param state   the current rendering state.
	 * 
	 * @return the tooltip for this cell or <code>null</code> if no tooltip should be rendered.
	 */
	protected String getTooltip(RenderState state, int column) {
		return getColumn(state, column).getTooltip(state.getView().getTableData());
	}
	
	/**
	 * Hook for subclasses to add a tooltip caption.
	 * 
	 * @param column  the column index
	 * @param view    the underlying {@link TableControl}
	 * 
	 * @return the tooltip caption for this cell or <code>null</code> if no caption should be added.
	 */
	protected String getTooltipCaption(TableControl view, int column) {
		return null;
	}

	/**
	 * Write Icon to display the current sort order.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link Writer} to write a form fragment to.
	 * @param image
	 *        The image displaying the sort direction.
	 * @param cssClass
	 *        Css class to style and position the icon.
	 * @param tooltip
	 * 		  Tooltip and alt-Attribute of the icon.
	 */
	protected void writeSortImage(DisplayContext context, TagWriter out, ThemeImage image, String cssClass, String tooltip)
			throws IOException {

		XMLTag tag = image.toIcon();
		tag.beginBeginTag(context, out);
		out.beginCssClasses();
		out.append(FILTER_BUTTON_CSS_CLASS);
		out.append(cssClass);
		out.endCssClasses();
		out.writeAttribute(ALT_ATTR, tooltip);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip, null);
		out.writeAttribute(BORDER_ATTR, 0);
		out.writeAttribute(ONDRAGSTART_ATTR, "return false;");
		tag.endEmptyTag(context, out);
	}

	private boolean hasCustomColumnOrder(TableControl view) {
		return view.getApplicationModel().getTableConfiguration().getColumnCustomization() != ColumnCustomization.NONE;
	}

	/**
	 * The {@link View} that writes the title of the table.
	 * 
	 * @param control
	 *        Never null.
	 * 
	 * @return Never null. Use the {@link InvisibleView} if there is no title.
	 */
	protected View getTitleView(TableControl control) {
		return new TextView(control.getTitle());
	}

	@Override
	public void writeAdditionalHeader(DisplayContext context, TagWriter out, RenderState state, int headerRowNumber,
			int columnNumber) throws IOException {
		HTMLFragmentProvider fragmentProvider = getAdditionalHeader(state, headerRowNumber, columnNumber);
		if (fragmentProvider == null) {
			return;
		}
		String controlStyle = ColumnConfiguration.COLUMN_CONTROL_TYPE_HEADER;
		AdditionalHeaderControlModel controlModel = new AdditionalHeaderControlModel(state, columnNumber);
		HTMLFragment fragment = createAdditionalHeaderControl(fragmentProvider, controlStyle, controlModel);
		if (fragment == null) {
			return;
		}
		fragment.write(context, out);
	}

	/**
	 * @param state
	 *        Is not allowed to be null.
	 * @param headerRowNumber
	 *        The number in the list of additional headers, not the total row number. That means the
	 *        first additional row number is 0, even if there are normal header rows before it.
	 * @return Null, if there is no additional header for this column at this header row index.
	 */
	protected HTMLFragmentProvider getAdditionalHeader(RenderState state, int headerRowNumber, int columnNumber) {
		List<HTMLFragmentProvider> additionalHeaders = getAdditionalHeaders(state, columnNumber);
		if (headerRowNumber >= additionalHeaders.size()) {
			return null;
		}
		return additionalHeaders.get(headerRowNumber);
	}

	/**
	 * @param state
	 *        Is not allowed to be null.
	 * @return Null, if there is no additional header for this column.
	 */
	protected List<HTMLFragmentProvider> getAdditionalHeaders(RenderState state, int columnNumber) {
		return state.getColumn(columnNumber).getConfig().getAdditionalHeaders();
	}

	/**
	 * @param fragmentProvider
	 *        If this is null, null is returned.
	 * @param controlModel
	 *        Is not allowed to be null.
	 * @param controlStyle
	 *        See the second parameter of
	 *        {@link HTMLFragmentProvider#createFragment(Object, String)}. If null, this style is
	 *        ignored.
	 * @return Null, if there is no {@link HTMLFragment} or {@link HTMLFragmentProvider} for this
	 *         {@link ColumnBaseConfig#getAdditionalHeaders() additional header}.
	 */
	protected HTMLFragment createAdditionalHeaderControl(HTMLFragmentProvider fragmentProvider, String controlStyle,
			AdditionalHeaderControlModel controlModel) {
		if (fragmentProvider == null) {
			return null;
		}
		return fragmentProvider.createFragment(controlModel, controlStyle);
	}

	/**
	 * Write the footer area of the table. The optionally displayed size can be set manually (e.g.
	 * fetched from return value of AbstractTable#writeContent(...), see AbstractTable#write(...) ).
	 *
	 * @param numberOfRenderedRows
	 *        The number of written elements.
	 */
	@Override
	public void writeFooter(DisplayContext context, TagWriter out, RenderState state, int numberOfRenderedRows)
			throws IOException {
		TableControl view = state.getView();
		int currentPage = view.getCurrentPage();

		boolean hasPreviousPage = currentPage > 0;
		boolean hasNextPage = currentPage < view.getPagingModel().getPageCount() - 1;

		MapWithProperties footerProperties = new MapWithProperties();

		footerProperties.put("isPagination", hasPaginationFooter(state, numberOfRenderedRows));
		footerProperties.put("styles", view.getViewModel().getTableConfiguration().getFooterStyle());
		footerProperties.put("firstPageButton", createFirstPageButtonFragment(view, hasPreviousPage));
		footerProperties.put("previousPageButton", createPreviousPageButtonFragment(view, hasPreviousPage));
		footerProperties.put("currentPage", createCurrentPageFragment(view));
		footerProperties.put("nextPageButton", createNextPageButtonFragment(view, hasNextPage));
		footerProperties.put("lastPageButton", createLastPageButtonFragment(view, hasNextPage));
		footerProperties.put("pageSize", createPageSizeFragment(view));
		footerProperties.put("pageInfo", createPageInfoFragment(view));
		footerProperties.put("text", getFooterText(view));

		Icons.TABLE_FOOTER_TEMPLATE.get().write(context, out, footerProperties);
	}

	/**
	 * Hook to customize the footer text.
	 * 
	 * <p>
	 * By default the number of rows is displayed.
	 * </p>
	 * 
	 * @param view
	 *        Underlying control that display the {@link TableModel}..
	 */
	protected String getFooterText(TableControl view) {
		int numberOfRenderedRows = view.getPagingModel().getCurrentPageSize();

		return Resources.getInstance().getString(I18NConstants.NUMBER_OF_ROWS_TEXT__ROWS.fill(numberOfRenderedRows));
	}

	private Object createCurrentPageFragment(TableControl view) {
		return (HTMLFragment) (context, out) -> {
			Integer[] tableInfo = view.computeTableInfo();

			out.writeText(
				Resources.getInstance().getMessage(I18NConstants.PAGING_MESSAGE_START, (Object[]) tableInfo));
			Control pageInput = view.getPageInputControl();
			if (pageInput != null) {
				pageInput.write(context, out);
			} else {
				out.writeText(String.valueOf(view.getCurrentPage()));
			}
			out.writeText(
				Resources.getInstance().getMessage(I18NConstants.PAGING_MESSAGE_END, (Object[]) tableInfo));
		};
	}

	private Object createPageInfoFragment(TableControl view) {
		return (HTMLFragment) (context, out) -> {
			if (useFullFooter) {
				out.writeText(
					Resources.getInstance().getMessage(I18NConstants.PAGING_MESSAGE_START_FULL,
						(Object[]) view.computeTableInfo()));
			}
		};
	}

	private HTMLFragment createPageSizeFragment(TableControl view) {
		return (context, out) -> {
			Control pageSize = view.getPageSizeControl();
			if (pageSize != null) {
				out.writeText(Resources.getInstance().getString(I18NConstants.PAGING_OPTIONS_START));
				pageSize.write(context, out);
				out.writeText(Resources.getInstance().getString(I18NConstants.PAGING_OPTIONS_END));
			}
		};
	}

	private HTMLFragment createFirstPageButtonFragment(TableControl view, boolean hasPreviousPage) {
		return (context, out) -> {
			if (hasPreviousPage) {
				writePagingCommand(context, out, view, TABLE_FIRST_PAGE_ICON_NAME, Icons.TBL_FIRST,
					view.getFirstPageCommand());
			} else {
				writePagingCommand(context, out, view, TABLE_DISABLED_FIRST_PAGE_ICON_NAME,
					Icons.TBL_FIRST_DISABLED, null);
			}
		};
	}

	private HTMLFragment createPreviousPageButtonFragment(TableControl view, boolean hasPreviousPage) {
		return (context, out) -> {
			if (hasPreviousPage) {
				writePagingCommand(context, out, view, TABLE_PREVIOUS_PAGE_ICON_NAME, Icons.TBL_PREV,
					view.getPreviousPageCommand());
			} else {
				writePagingCommand(context, out, view, TABLE_DISABLED_PREVIOUS_PAGE_ICON_NAME,
					Icons.TBL_PREV_DISABLED, null);
			}
		};
	}

	private HTMLFragment createNextPageButtonFragment(TableControl view, boolean hasNextPage) {
		return (context, out) -> {
			if (hasNextPage) {
				writePagingCommand(context, out, view, TABLE_NEXT_PAGE_ICON_NAME, Icons.TBL_NEXT,
					view.getNextPageCommand());
			} else {
				writePagingCommand(context, out, view, TABLE_DISABLED_NEXT_PAGE_ICON_NAME,
					Icons.TBL_NEXT_DISABLED, null);
			}
		};
	}

	private HTMLFragment createLastPageButtonFragment(TableControl view, boolean hasNextPage) {
		return (context, out) -> {
			if (hasNextPage) {
				writePagingCommand(context, out, view, TABLE_LAST_PAGE_ICON_NAME, Icons.TBL_LAST,
					view.getLastPageCommand());
			} else {
				writePagingCommand(context, out, view, TABLE_DISABLED_LAST_PAGE_ICON_NAME,
					Icons.TBL_LAST_DISABLED, null);
			}
		};
	}

	private boolean hasPaginationFooter(RenderState state, int numberOfRenderedRows) {
		return (state.getModel().getRowCount() > numberOfRenderedRows) || state.getView().getPageSizeControl() != null;
	}

    /** 
     * TODO fsc add comment
     */
    protected void writeContentBetweenPagingCommands(DisplayContext context, TagWriter out,
            TableControl view, int currentPage, Integer[] theNumbers) throws IOException {
		out.writeContent(Resources.getInstance().getMessage(I18NConstants.PAGING_MESSAGE_START, (Object[]) theNumbers));
        Control pageInput = view.getPageInputControl();
        if (pageInput != null) {
            pageInput.write(context, out);
        }
        else {
            out.writeText(String.valueOf(currentPage));
        }
		out.writeContent(Resources.getInstance().getMessage(I18NConstants.PAGING_MESSAGE_END, (Object[]) theNumbers));
    }
	

	/**
	 * Write the given paging command to the given writer.
	 *
	 * This method is used by the {@link #writeFooter(DisplayContext, TagWriter, RenderState, int)}
	 * to switch through the table content.
	 * 
	 * @param out
	 *        The writer to write the command.
	 * @param key
	 *        The name of the image (without page and extension).
	 * @param command
	 *        The command to execute.
	 *
	 * @throws IOException
	 *         If writing fails.
	 */
	protected void writePagingCommand(DisplayContext context, TagWriter out,
			TableControl view, String key, ThemeImage image, TableCommand command)
			throws IOException {
		String tooltipText = view.getResources().getStringResource(key, null);
		if (tooltipText == null) {
			// Fallback, use default fallback resources.
			tooltipText = Resources.getInstance().getString(GENERAL_PREFIX.key(key));
		}

		if (command != null) {
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, "#");
			writeOnClick(out, view, command);
			out.endBeginTag();
			image.writeWithCssPlainTooltip(context, out, null, tooltipText);
			out.endTag(ANCHOR);
		} else {
			image.writeWithCssPlainTooltip(context, out, FormConstants.DISABLED_CSS_CLASS, tooltipText);
		}
	}

	private void writeOnClick(TagWriter out, TableControl view, TableCommand command) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		command.writeInvokeExpression(out, view);
		out.append(";");
		out.endAttribute();
	}

	/**
	 * Write the content area of the table.
	 *
	 * @return the number of rows actually written
	 */
	public final int writeContent(DisplayContext context, TagWriter out,
			RenderState state) throws IOException {
		int writtenRowsCount = 0;
		TableViewModel model = state.getModel();
		model.clearSlices();
		int rowCount = model.getRowCount();

		if (rowCount > 0) {
			// Only write the body structuring element, if some body rows are
			// written (see DTD).
			out.beginBeginTag(TBODY);
			out.writeAttribute(TL_COMPLEX_WIDGET_ATTR, true);
			out.endBeginTag();

			PagingModel pagingModel = state.getView().getPagingModel();
			int firstRow = pagingModel.getFirstRowOnCurrentPage();
			int lastRow = pagingModel.getLastRowOnCurrentPage();
			model.addSlice(new Slice(firstRow, lastRow));
			model.checkDisplayedRows(firstRow, lastRow);
			
			TableUtil.createTableRowsFragment(state, firstRow, lastRow).write(context, out);
			out.endTag(TBODY);
		}

		return writtenRowsCount;
	}

	public void writeColumns(DisplayContext context, TagWriter out,
			RenderState state, boolean isSelected, int displayedRow, int row) throws IOException {
		int fixedColumns = state.getModel().getFixedColumnCount();
		int fixedColumnWidth = 0;

		for (int column = 0, columnCount = getColumnCount(state); column < columnCount; column++) {
			if (fixedColumns > 0) {
				fixedColumnWidth += getColumnWidth(state, column);
			}

			state.getRenderer().writeColumn(out, context, state, isSelected, column, displayedRow, row,
				fixedColumnWidth);
		}
	}

	/**
	 * Write the content cell for the given Object.
	 * 
	 * @param isSelected
	 *        Flag, if the current object is the selected one.
	 * @param columnIndex
	 *        The column to be written (0..n)
	 * @param displayedRowIndex
	 *        The current row written (0..m)
	 * @param rowIndex
	 *        The current row.
	 */
	@Override
	public void writeColumn(TagWriter out, DisplayContext context, RenderState state, boolean isSelected,
			int columnIndex, int displayedRowIndex, int rowIndex, int leftOffset) throws IOException {
		MapWithProperties rowColumnProperties = new MapWithProperties();

		TableViewModel model = state.getModel();
		
		appendFixedColumnProperties(rowColumnProperties, columnIndex, leftOffset, model.getFixedColumnCount());

		rowColumnProperties.put("width", getColumnWidth(state, columnIndex) + "px");
		rowColumnProperties.put("label", createBodyCellLabelFragment(state, rowIndex, columnIndex));
		rowColumnProperties.put("classes", writeBodyCellClasses(state, rowIndex, isSelected, columnIndex));
		rowColumnProperties.put("isSelected", isSelected);
		rowColumnProperties.put("styles", writeBodyCellStyles(state, displayedRowIndex, columnIndex));
		rowColumnProperties.put("rowHeight", getBodyRowHeight(model));

		Icons.TABLE_BODY_CELL_TEMPLATE.get().write(context, out, rowColumnProperties);
	}

	private static String getHeaderRowHeight(TableModel model) {
		return model.getTableConfiguration().getHeaderRowHeight() + "px";
	}

	private static String getBodyRowHeight(TableModel model) {
		return model.getTableConfiguration().getRowHeight() + "px";
	}

	/**
	 * Adds properties for fixed columns.
	 */
	public static void appendFixedColumnProperties(MapWithProperties properties, int columnIndex, int leftOffset,
			int fixedColumns) {
		if (columnIndex < fixedColumns) {
			properties.put("isSticky", true);
			properties.put("offset", leftOffset + "px;");
		} else {
			properties.put("isSticky", false);
		}
	}

	private Object writeBodyCellStyles(RenderState state, int displayedRowIndex, int columnIndex) {
		return (HTMLFragment) (context, out) -> writeColumnStyleCustom(out, state, columnIndex, displayedRowIndex,
			getColumnConfiguration(state, columnIndex));
	}

	/**
	 * Returns the width of the column specified by the given index.
	 */
	public static int getColumnWidth(RenderState state, int columnIndex) {
		int columnWidth = state.getModel().getProgrammaticColumnWidth(columnIndex);

		if (!isPersonalizedColumnWidth(columnWidth)) {
			String configuredDefaultColumnWidth = getDefaultColumnWidth(state, columnIndex);

			if (StringServices.isEmpty(configuredDefaultColumnWidth)) {
				return DEFAULT_COLUMN_WIDTH;
			} else {
				columnWidth = TableUtil.parseColumnWidth(configuredDefaultColumnWidth);
			}
		}

		return columnWidth;
	}

	private static String getDefaultColumnWidth(RenderState state, int columnIndex) {
		return state.getColumn(columnIndex).getConfig().getDefaultColumnWidth();
	}

	private static boolean isPersonalizedColumnWidth(int columnWidth) {
		return columnWidth != TableViewModel.NO_COLUMN_WIDTH_PERSONALIZATION;
	}

	private HTMLFragment writeBodyCellClasses(RenderState state, int rowIndex, boolean isSelected,
			final int columnIndex) {
		return (context, out) -> {
			ColumnConfiguration columnConfiguration = state.getColumn(columnIndex).getConfig();

			out.append(state.getTDClass(columnIndex, isSelected));
			out.append(getCellSelectableClass(state, rowIndex, columnConfiguration));
			out.append(
				columnConfiguration.getCssClassProvider().getCellClass(state.getCell(rowIndex, columnIndex)));
		};
	}

	private String getCellSelectableClass(RenderState state, int rowIndex, ColumnConfiguration columnConfiguration) {
		return state.getView().isRowSelectable(rowIndex) && columnConfiguration.isSelectable() ? TABLE_CELL_SELECTABLE_CSS_CLASS
			: null;
	}

	/**
	 * Creates the {@link HTMLFragment} that display the table cells content.
	 */
	public HTMLFragment createBodyCellLabelFragment(RenderState state, int rowIndex, final int columnIndex) {
		return (context, out) -> writeCellContent(context, out, state, false, columnIndex, rowIndex);
	}

	private void writeContentWidthStyle(TagWriter out, RenderState state, int column, ColumnConfiguration theCD)
			throws IOException {
		if (!theCD.isDefaultColumnWidthRelative()) {
			writeColumnWidthStyle(out, state, column, theCD);
		}
	}

	private void writeColumnStyleCustom(TagWriter out, RenderState state, int column, int displayedRow,
			ColumnConfiguration theCD) throws IOException {
		final String configuredStyle = theCD.getCellStyle();
		if (StringServices.isEmpty(configuredStyle)) {
			String columnName = state.getModel().getColumnName(column);
			if (columnTDStyles != null) {
				CssUtil.appendStyleOptional(out, columnTDStyles.get(columnName));
			}
		
			if (cellTDStyles != null) {
				int applicationRow = state.getView().getViewModel().getApplicationModelRow(displayedRow);
				String key = columnName + "|" + applicationRow;
				CssUtil.appendStyleOptional(out, cellTDStyles.get(key));
			}
		} else {
			CssUtil.appendStyle(out, configuredStyle);
		}
	}

	/**
	 * Write <code>onclick</code> attribute that selects the given row only if the event was not
	 * triggered from an active element, that would consume a clicks by itself.
	 * 
	 * @param column
	 *        The column to be written (0..n)
	 */
	protected void writeSelectOnclick(TagWriter out, TableControl view, int row, int column) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		view.appendSelectAction(out, row, column);
		out.endAttribute();
	}

	@Override
	public String hookGetColumnWidth(ColumnConfiguration columnConfiguration) {
		if (hasNoConfiguredDefaultWidth(columnConfiguration)) {
			return DEFAULT_COLUMN_WIDTH_STYLE;
		} else {
			return columnConfiguration.getDefaultColumnWidth();
		}
	}

	private boolean hasNoConfiguredDefaultWidth(ColumnConfiguration columnConfiguration) {
		return StringServices.isEmpty(columnConfiguration.getDefaultColumnWidth());
	}

	/**
	 * Terminates each style entry of a style map.
	 * 
	 * @param stylesMapping
	 *        - map of styles
	 */
	private static <T> void terminateStyles(Map<T, String> stylesMapping) {
		Set<Map.Entry<T, String>> styleEntriesSet = stylesMapping.entrySet();
		for (Map.Entry<T, String> styleEntry : styleEntriesSet) {
			String style = styleEntry.getValue();
			if (!StringServices.isEmpty(style)) {
				styleEntry.setValue(CssUtil.terminateStyleDefinition(styleEntry.getValue()));
			}
		}
	}

	/**
	 * If necessary get additional attributes to be written inside the cell
	 * td-tag.
	 * 
	 * @param self
	 *        the {@link TableRenderer} which is use to write the column
	 * @param context
	 *        the context in which rendering occurs
	 * @param out
	 *        writer to write content to
	 * @param view
	 *        table currently written
	 * @param isSelected
	 *        Flag, if the current object is the selected one.
	 * @param column
	 *        The column to be written (0..n)
	 * @param displayedRow
	 *        The current row written (0..m)
	 * @param row
	 *        The current row.
	 */
	protected String getAdditionalColumnTDContent(TableRenderer<?> self, DisplayContext context, TagWriter out,
			TableControl view, boolean isSelected, int column, int displayedRow, int row) {
		return null;
	}

	/**
	 * The style for the counter (or the empty list message).
	 * 
	 * @param usePagingFooter
	 *        Whether a paging footer CSS class should be used or a counter CSS class.
	 */
	protected void writeCounterClassesContent(Appendable out, boolean usePagingFooter) throws IOException {
		String theClass = (usePagingFooter) ? TABLE_FOOTER_PAGER_CSS_CLASS : TABLE_FOOTER_COUNTER_CSS_CLASS;
		out.append(theClass);
	}

	@Override
	public String computeTHClass(Column column) {
		return CssUtil.joinCssClasses(TABLE_HEADER_CELL_CSS_CLASS, column.getCssClasses());
	}
	
	@Override
	public String computeTHGroupClass() {
		return CssUtil.joinCssClasses(TABLE_HEADER_CELL_CSS_CLASS, TABLE_GROUP_CELL_CSS_CLASS);
	}

	@Override
	public String computeTRTitleClass() {
		return CssUtil.joinCssClasses(getTRClass(), TABLE_TITLE_ROW);
	}

	@Override
	public String computeTRHeaderClass() {
		return CssUtil.joinCssClasses(getTRClass(), TABLE_HEADER_ROW);
	}

	@Override
	public String computeTRFooterClass() {
		return CssUtil.joinCssClasses(getTRClass(), TABLE_FOOTER_ROW);
	}

	/**
	 * Row class for title, header and footer rows.
	 */
	protected String getTRClass() {
		return DefaultTableRenderer.TABLE_ROW_CSS_CLASS;
	}

	@Override
	public String computeTDClass(Column column) {
		return CssUtil.joinCssClasses(TABLE_BODY_CELL_CSS_CLASS, column.getCssClasses());
	}

	@Override
	public String computeTDClassSelected(Column column) {
		return CssUtil.joinCssClasses(computeTDClass(column), TABLE_CELL_SELECTED_CSS_CLASS);
	}

	/**
	 * Write the current cell.
	 */
	@Override
	public void writeCellContent(DisplayContext context, TagWriter out,
			RenderState state, boolean isSelected,
			int column, int row) throws IOException {

		CellRenderer cellRenderer = getColumn(state, column).getValueRenderer();
        
		Cell cell = state.getCell(row, column);
		if (!cell.cellExists()) {
			// No cell value given
			return;
		}
		cellRenderer.writeCell(context, out, cell);
	}

	/**
	 * Hook for adding additional columns that are not provided by the model to the rendered output.
	 */
	protected int getColumnCount(RenderState state) {
		return state.getColumns().size();
	}

	@Override
	public void updateRows(TableControl view, List<UpdateRequest> updateRequests, UpdateQueue actions) {
		RenderState state = new DefaultRenderState(this, view);
		for (UpdateRequest updateRequest : updateRequests) {
			int firstRow = updateRequest.getFirstAffectedRow();
			int lastRow = updateRequest.getLastAffectedRow();

			state.getRenderer().addUpdateActions(actions, state, firstRow, lastRow);
		}
	}

	public static Object createColumnControl(RenderState state, int viewColumn, String aType) {
		ColumnConfiguration config = getColumnConfiguration(state, viewColumn);
		ControlProvider theCP = getHeaderControlProvider(state, config);
		if (theCP == null) {
			return null;
		} else {
			TableViewModel viewModel = state.getModel();
			int applicationColumn = viewModel.getApplicationModelColumn(viewColumn);
			TableModel model = viewModel.getApplicationModel();
			if (model instanceof FormTableModel) {
				FormContainer columnGroup =
					((FormTableModel) viewModel.getApplicationModel()).getColumnGroup(applicationColumn);
				return theCP.createControl(columnGroup, aType);
			} else {
				return theCP.createControl(state, aType);
			}
		}
	}

	private static ControlProvider getHeaderControlProvider(RenderState state, ColumnConfiguration config) {
		if (TableControl.SELECT_COLUMN_NAME.equals(config.getName())) {
			SelectionModel selectionModel = state.getView().getTableData().getSelectionModel();

			if (selectionModel.isMultiSelectionSupported()) {
				return new ControlProvider() {
					@Override
					public Control createControl(Object model, String style) {
						Set<Object> allRows = CollectionUtil.toSet(state.getModel().getDisplayedRows());
				
						return new TableHeaderSelectionControl(selectionModel, allRows);
					}
				};
			}
		} else {
			return config.getHeadControlProvider();
		}

		return null;
	}

	protected static ColumnConfiguration getColumnConfiguration(RenderState state, int viewColumn) {
		return getColumn(state, viewColumn).getConfig();
	}
	
	protected static Config newConfigForImplClass(Class<? extends DefaultTableRenderer> implClass) {
		try {
			return (Config) TypedConfiguration.createConfigItemForImplementationClass(implClass);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	public static DefaultTableRenderer newInstance() {
		return newInstance(USE_FULL_FOOTER_DEFAULT);
	}

	public static DefaultTableRenderer newInstance(boolean useFullFooter) {
		Config config = newConfigForImplClass(DefaultTableRenderer.class);
		config.setFullFooter(Boolean.valueOf(useFullFooter));
		return TypedConfigUtil.createInstance(config);
	}

}
