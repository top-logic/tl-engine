/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.IterableUtils;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.dnd.NoTableDrag;
import com.top_logic.layout.table.dnd.NoTableDrop;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.model.TLObject;
import com.top_logic.util.css.CssUtil;

/**
 * Configuration of a table.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ColumnDescriptionManager extends TableConfiguration {

	private String _tableName;

	private ITableRenderer _tableRenderer;

	private int _fixedColumnCount = DefaultTableRenderer.NO_FIXED_COLUMNS_CONFIGURED;

	private TableFilterProvider _filterProvider;

	private List<String> _sidebarFilters = Collections.emptyList();

	private boolean _filterDisplayParents;

	private boolean _filterDisplayChildren;

	private TableFilterProvider _defaultFilterProvider;

	private RowClassProvider _rowClassProvider;

	private ResourceProvider _rowObjectResourceProvider;

	private ColumnCustomization _columnCustomization;

	private final ColumnConfigurationContainer _columns;

	private Enabled _multiSort;
	
	private Iterable<SortConfig> _defaultSortConfig = Collections.emptyList();

	private List<String> _defaultColumns;

	private int _maxColumns;

	private int[] _pageSizeOptions;

	private boolean _showTitle;

	private boolean _showColumnHeader;

	private String _idColumn;

	private boolean _isTree;

	private boolean _showFooter;

	private String _rowStyle;

	private ResourceView _resPrefix = ResPrefix.NONE;

	private ResKey _titleKey;

	private String _titleStyle;

	private String _headerStyle;

	private String _footerStyle;

	private int titleHeight;

	private int headerRowHeight;

	private int footerHeight;

	private int rowHeight;

	private Mapping<Object, ? extends TLObject> _modelMapping;

	private TableDataExport _exporter;

	private boolean _supportsMultipleSettings;

	private String _multipleSettingsKey = StringServices.EMPTY_STRING;

	private TableDragSource _dragSource = NoTableDrag.INSTANCE;

	private TableDropTarget _dropTarget = NoTableDrop.INSTANCE;

	private ContextMenuProvider _contextMenu = NoContextMenuProvider.INSTANCE;

	private Map<String, Collection<TableCommandProvider>> _commands;

	ColumnDescriptionManager(TableConfigurationFactory settings) {
		this(settings, ColumnConfiguration.column(null));
    }

	ColumnDescriptionManager(TableConfigurationFactory settings, ColumnConfiguration aDefaultColumnDescription) {
		_columns = new ColumnConfigurationContainer(aDefaultColumnDescription);

		_filterDisplayParents = settings.DEFAULT_FILTER_DISPLAY_PARENTS;
		_filterDisplayChildren = settings.DEFAULT_FILTER_DISPLAY_CHILDREN;
		_defaultFilterProvider = settings.DEFAULT_TABLE_FILTER_PROVIDER;
		_rowClassProvider = settings.DEFAULT_ROW_CLASS_PROVIDER;
		_rowObjectResourceProvider = settings.DEFAULT_ROW_OBJECT_RESOURCE_PROVIDER;
		_columnCustomization = settings.DEFAULT_COLUMN_CUSTOMIZATION;
		_multiSort = settings.DEFAULT_MULTI_SORT;
		_maxColumns = settings.DEFAULT_MAX_COLUMNS;
		_pageSizeOptions = settings.DEFAULT_PAGE_SIZE_OPTIONS;
		_showTitle = settings.DEFAULT_SHOW_TITLE;
		_showColumnHeader = settings.DEFAULT_SHOW_COLUMN_HEADER;
		_showFooter = settings.DEFAULT_SHOW_FOOTER;
		_defaultColumns = Collections.unmodifiableList(settings.DEFAULT_DEFAULT_COLUMNS);
		_commands = copy(settings.DEFAULT_ADDITIONAL_COMMANDS);
	}

	private static Map<String, Collection<TableCommandProvider>> copy(
			Map<String, Collection<TableCommandProvider>> commands) {
		HashMap<String, Collection<TableCommandProvider>> result = new HashMap<>();
		for (Entry<String, Collection<TableCommandProvider>> entry : commands.entrySet()) {
			result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return result;
	}

	@Override
	public String getTableName() {
		return _tableName;
	}

	@Override
	public void addColumn(ColumnConfiguration columnConfiguration) {
		columns().addColumn(columnConfiguration);
	}

	@Override
	protected ColumnConfigurationContainer columns() {
		return _columns;
	}

	@Override
	public void setTableName(String value) {
		checkFrozen();
		_tableName = value;
	}

	@Override
	public final ITableRenderer getTableRenderer() {
		return _tableRenderer;
	}

	@Override
	public TableDragSource getDragSource() {
		return _dragSource;
	}

	@Override
	public void setDragSource(TableDragSource value) {
		_dragSource = value;
	}

	@Override
	public TableDropTarget getTableDrop() {
		return _dropTarget;
	}

	@Override
	public void setTableDrop(TableDropTarget value) {
		_dropTarget = value;
	}

	@Override
	public ContextMenuProvider getContextMenu() {
		return _contextMenu;
	}

	@Override
	public void setContextMenu(ContextMenuProvider value) {
		_contextMenu = value;
	}

	@Override
	public final void setTableRenderer(ITableRenderer tableRenderer) {
		checkFrozen();
		_tableRenderer = tableRenderer;
	}

	@Override
	public int getFixedColumnCount() {
		return _fixedColumnCount;
	}

	@Override
	public void setFixedColumnCount(int fixedColumnCount) {
		_fixedColumnCount = fixedColumnCount;
	}

	@Override
	public List<String> getSidebarFilters() {
		return _sidebarFilters;
	}

	@Override
	public void setSidebarFilters(List<String> sidebarFilters) {
		checkFrozen();
		_sidebarFilters = sidebarFilters;
	}

	@Override
	public final TableFilterProvider getFilterProvider() {
		return _filterProvider;
	}

	@Override
	public final void setFilterProvider(TableFilterProvider filterProvider) {
		checkFrozen();
		_filterProvider = filterProvider;
	}

	@Override
	public final boolean getFilterDisplayParents() {
		return _filterDisplayParents;
	}

	@Override
	public final void setFilterDisplayParents(boolean value) {
		checkFrozen();
		_filterDisplayParents = value;
	}

	@Override
	public final boolean getFilterDisplayChildren() {
		return _filterDisplayChildren;
	}

	@Override
	public final void setFilterDisplayChildren(boolean value) {
		checkFrozen();
		_filterDisplayChildren = value;
	}

	@Override
	public final TableFilterProvider getDefaultFilterProvider() {
		return _defaultFilterProvider;
	}

	@Override
	public final void setDefaultFilterProvider(TableFilterProvider defaultFilterProvider) {
		checkFrozen();
		_defaultFilterProvider = defaultFilterProvider;
	}

	@Override
	public final RowClassProvider getRowClassProvider() {
		return _rowClassProvider;
	}

	@Override
	public final void setRowClassProvider(RowClassProvider value) {
		checkFrozen();
		_rowClassProvider = value;
	}

	@Override
	public final ResourceProvider getRowObjectResourceProvider() {
		return _rowObjectResourceProvider;
	}

	@Override
	public final void setRowObjectResourceProvider(ResourceProvider rowObjectResourceProvider) {
		checkFrozen();
		_rowObjectResourceProvider = rowObjectResourceProvider;
	}

	@Override
	public boolean getShowTitle() {
		return _showTitle;
	}

	@Override
	public void setShowTitle(boolean value) {
		checkFrozen();
		_showTitle = value;
	}

	@Override
	public boolean getShowColumnHeader() {
		return _showColumnHeader;
	}

	@Override
	public void setShowColumnHeader(boolean value) {
		checkFrozen();
		_showColumnHeader = value;
	}

	@Override
	public String getIDColumn() {
		return _idColumn;
	}

	@Override
	public void setIDColumn(String value) {
		_idColumn = value;
	}

	@Override
	public boolean isTree() {
		return _isTree;
	}

	@Override
	public void setTree(boolean isTree) {
		_isTree = isTree;
	}

	@Override
	public boolean getShowFooter() {
		return _showFooter;
	}

	@Override
	public void setShowFooter(boolean value) {
		checkFrozen();
		_showFooter = value;
	}

	@Override
	public String getRowStyle() {
		return _rowStyle;
	}

	@Override
	public void setRowStyle(String value) {
		checkFrozen();
		_rowStyle = value;
		internalSetRowHeight(value);
	}

	@Override
	public int getRowHeight() {
		return rowHeight;
	}

	private final void internalSetRowHeight(String rowStyle) {
		int value = CssUtil.retrieveHeightValueOrDefault(rowStyle, Icons.FROZEN_TABLE_ROW_HEIGHT);
		internalSetRowHeight(value);
	}

	@Override
	void internalSetRowHeight(int value) {
		checkFrozen();
		rowHeight = value;
	}

	@Override
	public ResourceView getResPrefix() {
		return _resPrefix;
	}

	@Override
	public void setResPrefix(ResourceView value) {
		checkFrozen();
		_resPrefix = value;
	}

	@Override
	public ResKey getTitleKey() {
		return _titleKey;
	}

	@Override
	public void setTitleKey(ResKey value) {
		checkFrozen();
		_titleKey = value;
	}

	@Override
	public String getTitleStyle() {
		return _titleStyle;
	}

	@Override
	public void setTitleStyle(String value) {
		checkFrozen();
		_titleStyle = value;
		internalSetTitleHeight(value);
	}

	@Override
	public int getTitleHeight() {
		return titleHeight;
	}

	private final void internalSetTitleHeight(String titleStyle) {
		int value =
			CssUtil.retrieveHeightValueOrDefault(titleStyle,
				com.top_logic.layout.form.treetable.component.Icons.FROZEN_TABLE_TITLE_HEIGHT);
		internalSetTitleHeight(value);
	}

	@Override
	void internalSetTitleHeight(int value) {
		checkFrozen();
		titleHeight = value;
	}

	@Override
	public String getHeaderStyle() {
		return _headerStyle;
	}

	@Override
	public void setHeaderStyle(String value) {
		checkFrozen();
		_headerStyle = value;
		internalSetHeaderHeight(value);
	}

	@Override
	public int getHeaderRowHeight() {
		return headerRowHeight;
	}

	private final void internalSetHeaderHeight(String headerRowStyle) {
		int value =
			CssUtil.retrieveHeightValueOrDefault(headerRowStyle,
				Icons.FROZEN_TABLE_HEADER_ROW_HEIGHT);
		internalSetHeaderHeight(value);
	}

	@Override
	void internalSetHeaderHeight(int value) {
		checkFrozen();
		headerRowHeight = value;
	}

	@Override
	public String getFooterStyle() {
		return _footerStyle;
	}

	@Override
	public void setFooterStyle(String value) {
		checkFrozen();
		_footerStyle = value;
		internalSetFooterHeight(value);
	}

	@Override
	public int getFooterHeight() {
		return footerHeight;
	}

	private final void internalSetFooterHeight(String footerStyle) {
		int value =
			CssUtil.retrieveHeightValueOrDefault(footerStyle, Icons.FROZEN_TABLE_FOOTER_HEIGHT);
		internalSetFooterHeight(value);
	}

	@Override
	void internalSetFooterHeight(int value) {
		checkFrozen();
		footerHeight = value;
	}

	@Override
	public Enabled getMultiSort() {
		return _multiSort;
	}

	@Override
	public void setMultiSort(Enabled value) {
		checkFrozen();
		_multiSort = value;
	}
	
	@Override
	public List<String> getDefaultColumns() {
		return _defaultColumns;
	}

	@Override
	public void setDefaultColumns(List<String> defaultColumns) {
		checkFrozen();
		_defaultColumns = defaultColumns;
	}

	@Override
	public Iterable<SortConfig> getDefaultSortOrder() {
		if (IterableUtils.isEmpty(_defaultSortConfig)) {
			String idColumn = getIDColumn();

			if (!StringServices.isEmpty(idColumn)) {
				return Collections.singleton(SortConfigFactory.ascending(idColumn));
			}
		}

		return _defaultSortConfig;
	}

	@Override
	public void setDefaultSortOrder(Iterable<SortConfig> sortConfig) {
		checkFrozen();
		_defaultSortConfig = sortConfig;
	}

	@Override
	public int getMaxColumns() {
		return _maxColumns;
	}

	@Override
	public void setMaxColumns(int value) {
		checkFrozen();
		_maxColumns = value;
	}

	@Override
	public int[] getPageSizeOptions() {
		return _pageSizeOptions.clone();
	}

	@Override
	public void setPageSizeOptions(int... sizeOptions) {
		checkFrozen();
		_pageSizeOptions = sizeOptions;
	}

	@Override
	public ColumnCustomization getColumnCustomization() {
		return _columnCustomization;
	}

	@Override
	public void setColumnCustomization(ColumnCustomization value) {
		checkFrozen();
		_columnCustomization = value;
	}

	@Override
	public Mapping<Object, ? extends TLObject> getModelMapping() {
		return _modelMapping;
	}

	@Override
	public void setModelMapping(Mapping<Object, ? extends TLObject> value) {
		checkFrozen();
		_modelMapping = value;
	}

	@Override
	public TableDataExport getExporter() {
		return _exporter;
	}

	@Override
	public void setExporter(TableDataExport exporter) {
		checkFrozen();
		_exporter = exporter;
	}

	@Override
	public Map<String, Collection<TableCommandProvider>> getCommands() {
		return _commands;
	}

	@Override
	public void setCommands(Map<String, Collection<TableCommandProvider>> commands) {
		checkFrozen();
		_commands = commands;
	}

	@Override
	public boolean getSupportMultipleSettings() {
		return _supportsMultipleSettings;
	}

	@Override
	public void setSupportMultipleSettings(boolean value) {
		checkFrozen();
		_supportsMultipleSettings = value;
	}

	@Override
	public String getMultipleSettingsKey() {
		return _multipleSettingsKey;
	}

	@Override
	public void setMultipleSettingsKey(String key) {
		checkFrozen();
		_multipleSettingsKey = key;
	}

	/**
	 * Freezes this {@link ColumnDescriptionManager} and all of its {@link ColumnConfiguration}s.
	 */
	@Override
	public void freeze() {
		super.freeze();

		_columns.freeze();
	}
    
	@Override
	public TableConfiguration copy() {
		TableConfiguration tableCopy = TableConfigurationFactory.emptyTable();
		copyGlobalSettingsTo(tableCopy);
		copyColumnsTo(tableCopy);
		return tableCopy;
	}

	private void copyColumnsTo(TableConfiguration configurationCopy) {
		columns().copyTo(configurationCopy.columns());
	}

	@Override
	public String toString() {
		return NameBuilder.buildName(this, getTableName());
	}

}
