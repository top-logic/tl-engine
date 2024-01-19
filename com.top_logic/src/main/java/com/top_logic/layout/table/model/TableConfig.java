/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.contextmenu.component.config.WithContextMenu;
import com.top_logic.layout.security.ModelMappingConfig;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.command.TableCommandConfig;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.filter.GlobalTextFilterProvider;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Configuration of an operational {@link TableConfiguration}.
 * 
 * <p>
 * {@link TableConfig}s are created and manipulated by {@link TableConfigurationFactory}.
 * </p>
 * 
 * @see TableConfigurationFactory
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TableConfig extends ColumnContainerConfig, ModelMappingConfig, WithContextMenu {
	
	String TABLE_NAME_ATTRIBUTE = "name";

	/**
	 * @see #getConfigurationProviders()
	 */
	String CONFIGURATION_PROVIDERS_ATTRIBUTE = "configurationProviders";

	String SHOW_TITLE_ATTRIBUTE = "showTitle";

	String SHOW_COLUMN_HEADER_ATTRIBUTE = "showColumnHeader";

	String SHOW_FOOTER_ATTRIBUTE = "showFooter";

	String ROW_STYLE_ATTRIBUTE = "rowStyle";

	/**
	 * @see #getResPrefix()
	 */
	String RES_PREFIX_ATTRIBUTE = "resPrefix";

	/**
	 * @see #getTitleKey()
	 */
	String TITLE_KEY_ATTRIBUTE = "titleKey";

	String TITLE_STYLE_ATTRIBUTE = "titleStyle";

	String HEADER_STYLE_ATTRIBUTE = "headerStyle";

	String FOOTER_STYLE_ATTRIBUTE = "footerStyle";

	String COLUMN_CUSTOMIZATION_ATTRIBUTE = "columnCustomization";

	String MULTI_SORT_ATTRIBUTE = "multiSort";

	String DEFAULT_SORT_ORDER_ATTRIBUTE = "defaultSortOrder";

	/** Property name of {@link #getDefaultColumns()} */
	String DEFAULT_COLUMNS_ATTRIBUTE = "defaultColumns";

	String MAX_COLUMNS_ATTRIBUTE = "maxColumns";

	String TABLE_RENDERER_ATTRIBUTE = "tableRenderer";

	String ROW_CLASS_PROVIDER_ATTRIBUTE = "rowClassProvider";

	/** Property name of {@link #getRowObjectResourceProvider()} */
	String ROW_OBJECT_RESOURCE_PROVIDER_ATTRIBUTE = "rowObjectResourceProvider";

	String PAGE_SIZE_OPTIONS_ATTRIBUTE = "pageSizeOptions";

	/**
	 * @see #getIDColumn()
	 */
	String ID_COLUMN = "id-column";

	String COLUMN_DEFAULT_ATTRIBUTE = "column-default";

	String DEFAULT_TABLE_FILTER_PROVIDER_ATTRIBUTE = "defaultFilterProvider";

	String TABLE_FILTER_PROVIDER_ATTRIBUTE = "filterProvider";
	
	/** Property name of {@link #getSidebarFilters()} */
	String SIDEBAR_FILTERS_ATTRIBUTE = "sidebarFilters";

	/** Property name of {@link #getFilterDisplayParents()}. */
	String FILTER_DISPLAY_PARENTS = "filterDisplayParents";

	/** Property name of {@link #getFilterDisplayChildren()}. */
	String FILTER_DISPLAY_CHILDREN = "filterDisplayChildren";

	/** Property name of {@link #getFixedColumnCount()}. */
	String FIXED_COLUMN_COUNT = "fixedColumns";

	/** Property name of {@link #getExporter()}. */
	String EXPORTER_ATTRIBUTE = "exporter";

	/** Property name of {@link #getDragSource()}. */
	String DRAG_SOURCE_ATTRIBUTE = "dragSource";

	/** Property name of {@link #getDropTargets()}. */
	String DROP_TARGETS_ATTRIBUTE = "dropTargets";

	/** Entry tag name of {@link #getDropTargets()}. */
	String DROP_TARGET_ENTRY_TAG = "dropTarget";

	/** Property name of {@link #getCommands()}. */
	String COMMANDS = "commands";

	/** Property name of {@link #getSupportsMultipleSettings()}. */
	String SUPPORTS_MULTIPLE_SETTINGS_ATTRIBUTE = "supportMultipleSettings";

	/** Property name of {@link #getMultipleSettingsKey()}. */
	String MULTIPLE_SETTINGS_KEY_ATTRIBUTE = "multipleSettingsKey";

	/**
	 * Name of the table.
	 * 
	 * @see TableConfiguration#getTableName()
	 */
	@Name(TABLE_NAME_ATTRIBUTE)
	String getTableName();

	/**
	 * @see #getTableName()
	 */
	void setTableName(String value);

	/**
	 * {@link TableConfigurationProvider}s to dynamically extends this configuration.
	 */
	@Name(CONFIGURATION_PROVIDERS_ATTRIBUTE)
	List<PolymorphicConfiguration<? extends TableConfigurationProvider>> getConfigurationProviders();

	/**
	 * Whether the title of the table should be shown.
	 * 
	 * @see TableConfiguration#getShowTitle()
	 */
	@Name(SHOW_TITLE_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isShowTitle();

	/**
	 * @see #isShowTitle()
	 */
	void setShowTitle(boolean value);

	/**
	 * Whether the column headers must be shown.
	 * 
	 * @see TableConfiguration#getShowColumnHeader()
	 */
	@Name(SHOW_COLUMN_HEADER_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isShowColumnHeader();

	/**
	 * @see #isShowColumnHeader()
	 */
	void setShowColumnHeader(boolean value);

	/**
	 * Whether the footer of the table must be shown.
	 * 
	 * @see TableConfiguration#getShowFooter()
	 */
	@Name(SHOW_FOOTER_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isShowFooter();

	/**
	 * @see #isShowFooter()
	 */
	void setShowFooter(boolean value);

	/**
	 * CSS style for each content row in the table.
	 * 
	 * @see TableConfiguration#getRowStyle()
	 * @see TableConfig#getTitleStyle()
	 * @see TableConfig#getHeaderStyle()
	 * @see TableConfig#getFooterStyle()
	 */
	@Name(ROW_STYLE_ATTRIBUTE)
	String getRowStyle();

	/**
	 * @see #getRowStyle()
	 */
	void setRowStyle(String value);

	/**
	 * The {@link ResPrefix} deriving column names and titles from, if no specialized keys are set.
	 * 
	 * @see #getTitleKey()
	 * @see ColumnConfig#getLabelKey()
	 */
	@Name(RES_PREFIX_ATTRIBUTE)
	ResPrefix getResPrefix();
	
	/**
	 * @see #getResPrefix()
	 */
	void setResPrefix(ResPrefix value);

	/**
	 * The {@link ResKey} defining the table title.
	 * 
	 * @see TableConfiguration#getTitleKey()
	 */
	@Name(TITLE_KEY_ATTRIBUTE)
	ResKey getTitleKey();

	/**
	 * @see #getTitleKey()
	 */
	void setTitleKey(ResKey value);

	/**
	 * CSS style for the title row of the table.
	 * 
	 * @see TableConfiguration#getTitleStyle()
	 * @see TableConfig#getHeaderStyle()
	 * @see TableConfig#getRowStyle()
	 */
	@Name(TITLE_STYLE_ATTRIBUTE)
	String getTitleStyle();

	/**
	 * @see #getTitleStyle()
	 */
	void setTitleStyle(String value);

	/**
	 * CSS style for the header row of the table.
	 * 
	 * @see TableConfiguration#getHeaderStyle()
	 * @see TableConfig#getTitleStyle()
	 * @see TableConfig#getRowStyle()
	 * @see TableConfig#getFooterStyle()
	 */
	@Name(HEADER_STYLE_ATTRIBUTE)
	String getHeaderStyle();

	/**
	 * @see #getHeaderStyle()
	 */
	void setHeaderStyle(String value);

	/**
	 * CSS style for the footer row of the table.
	 * 
	 * @see TableConfiguration#getHeaderStyle()
	 * @see TableConfig#getTitleStyle()
	 * @see TableConfig#getHeaderStyle()
	 * @see TableConfig#getRowStyle()
	 */
	@Name(FOOTER_STYLE_ATTRIBUTE)
	String getFooterStyle();

	/**
	 * @see #getFooterStyle()
	 */
	void setFooterStyle(String value);

	/**
	 * @see TableConfiguration#getColumnCustomization()
	 */
	@Name(COLUMN_CUSTOMIZATION_ATTRIBUTE)
	@ComplexDefault(ColumnCustomization.SelectionDefaultValueProvider.class)
	ColumnCustomization getColumnCustomization();

	/**
	 * @see #getColumnCustomization()
	 */
	void setColumnCustomization(ColumnCustomization value);

	/**
	 * @see TableConfiguration#getMultiSort()
	 */
	@Name(MULTI_SORT_ATTRIBUTE)
	@ComplexDefault(Enabled.AutoDefaultValueProvider.class)
	Enabled getMultiSort();

	/**
	 * @see #getMultiSort()
	 */
	void setMultiSort(Enabled value);

	/**
	 * The available page size options of the table.
	 * 
	 * @see TableConfiguration#getPageSizeOptions()
	 */
	@Name(PAGE_SIZE_OPTIONS_ATTRIBUTE)
	@Format(PageSizeOptionsFormat.class)
	@FormattedDefault(PageSizeOptionsFormat.ALL_VALUE)
	int[] getPageSizeOptions();

	/**
	 * @see #getPageSizeOptions()
	 */
	void setPageSizeOptions(int... value);

	/**
	 * Column for which the cell value is rendered as link with an image of the row objects type
	 * before.
	 * 
	 * @see TableConfiguration#getIDColumn()
	 */
	@Name(ID_COLUMN)
	String getIDColumn();

	/**
	 * @see #getIDColumn()
	 */
	void setIDColumn(String value);

	/**
	 * @see TableConfiguration#getDefaultSortOrder()
	 */
	@Format(DefaultSortOrderFormat.class)
	@Name(DEFAULT_SORT_ORDER_ATTRIBUTE)
	List<SortConfig> getDefaultSortOrder();

	/**
	 * @see #getDefaultSortOrder()
	 */
	void setDefaultSortOrder(List<SortConfig> value);

	/**
	 * columns, that shall be displayed by default in defined order.
	 */
	@Name(DEFAULT_COLUMNS_ATTRIBUTE)
	@Format(CommaSeparatedStrings.class)
	List<String> getDefaultColumns();

	/**
	 * @see #getDefaultColumns()
	 */
	void setDefaultColumns(List<String> defaultColumns);

	/**
	 * Maximal number of displayed columns.
	 * 
	 * @see TableConfiguration#getMaxColumns()
	 */
	@Name(MAX_COLUMNS_ATTRIBUTE)
	@IntDefault(Integer.MAX_VALUE)
	int getMaxColumns();

	/**
	 * @see #getMaxColumns()
	 */
	void setMaxColumns(int value);

	/**
	 * {@link RowClassProvider} to define CSS classes for the content of the table.
	 * 
	 * @see TableConfiguration#getRowClassProvider()
	 */
	@Name(ROW_CLASS_PROVIDER_ATTRIBUTE)
	@ItemDefault(DefaultRowClassProvider.Config.class)
	PolymorphicConfiguration<? extends RowClassProvider> getRowClassProvider();

	/**
	 * @see #getRowClassProvider()
	 */
	void setRowClassProvider(PolymorphicConfiguration<? extends RowClassProvider> value);

	/**
	 * {@link ResourceProvider} for the table internal row objects.
	 * 
	 * @see TableConfiguration#getRowObjectResourceProvider()
	 */
	@Name(ROW_OBJECT_RESOURCE_PROVIDER_ATTRIBUTE)
	PolymorphicConfiguration<? extends ResourceProvider> getRowObjectResourceProvider();

	/**
	 * @see #getRowObjectResourceProvider()
	 */
	void setRowObjectResourceProvider(PolymorphicConfiguration<? extends ResourceProvider> resourceProvider);

	/**
	 * @see TableConfiguration#getSidebarFilters()
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(SIDEBAR_FILTERS_ATTRIBUTE)
	List<String> getSidebarFilters();

	/**
	 * @see TableConfiguration#setSidebarFilters(List)
	 */
	void setSidebarFilters(List<String> sidebarFilters);

	/**
	 * @see TableConfiguration#getFilterProvider()
	 */
	@Name(TABLE_FILTER_PROVIDER_ATTRIBUTE)
	PolymorphicConfiguration<? extends TableFilterProvider> getFilterProvider();

	/**
	 * @see #getFilterProvider()
	 */
	void setFilterProvider(PolymorphicConfiguration<? extends TableFilterProvider> value);

	/**
	 * Whether to display the parents if a child matches the filter.
	 */
	@BooleanDefault(false)
	@Name(FILTER_DISPLAY_PARENTS)
	boolean getFilterDisplayParents();

	/**
	 * @see #getFilterDisplayParents()
	 */
	void setFilterDisplayParents(boolean value);

	/**
	 * Whether to display the children if its parent matches the filter.
	 */
	@BooleanDefault(false)
	@Name(FILTER_DISPLAY_CHILDREN)
	boolean getFilterDisplayChildren();

	/**
	 * @see #getFilterDisplayChildren()
	 */
	void setFilterDisplayChildren(boolean value);

	/**
	 * @see TableConfig#getDefaultFilterProvider()
	 */
	@Name(DEFAULT_TABLE_FILTER_PROVIDER_ATTRIBUTE)
	@ItemDefault(GlobalTextFilterProvider.Config.class)
	PolymorphicConfiguration<? extends TableFilterProvider> getDefaultFilterProvider();

	/**
	 * @see #getDefaultFilterProvider()
	 */
	void setDefaultFilterProvider(PolymorphicConfiguration<? extends TableFilterProvider> value);

	/**
	 * Configuration for the {@link TableRenderer} to render this table.
	 * 
	 * @see TableConfiguration#getTableRenderer()
	 */
	@Name(TABLE_RENDERER_ATTRIBUTE)
	@ImplementationClassDefault(DefaultTableRenderer.class)
	PolymorphicConfiguration<? extends ITableRenderer> getTableRenderer();

	/**
	 * The {@link TableDragSource} algorithm to use.
	 */
	@Name(DRAG_SOURCE_ATTRIBUTE)
	PolymorphicConfiguration<TableDragSource> getDragSource();

	/**
	 * The {@link TableDropTarget} algorithm to use.
	 */
	@Name(DROP_TARGETS_ATTRIBUTE)
	@EntryTag(DROP_TARGET_ENTRY_TAG)
	List<PolymorphicConfiguration<TableDropTarget>> getDropTargets();

	/**
	 * Default column.
	 * 
	 * @see TableConfiguration#getDefaultColumn()
	 */
	@Name(COLUMN_DEFAULT_ATTRIBUTE)
	@ItemDefault
	ColumnConfig getDefaultColumn();

	/**
	 * Number of fixed table columns.
	 * 
	 * @see TableConfiguration#getFixedColumnCount()
	 */
	@Name(FIXED_COLUMN_COUNT)
	@IntDefault(DefaultTableRenderer.NO_FIXED_COLUMNS_CONFIGURED)
	int getFixedColumnCount();

	/**
	 * Returns the algorithm that exports the configured table to Excel.
	 * 
	 * @return May be <code>null</code>. In such case no export is possible.
	 */
	@Name(EXPORTER_ATTRIBUTE)
	@ImplementationClassDefault(SimpleTableDataExport.class)
	PolymorphicConfiguration<? extends TableDataExport> getExporter();

	/**
	 * Setter for {@link #getExporter()}.
	 */
	void setExporter(PolymorphicConfiguration<? extends TableDataExport> exporter);

	/**
	 * Custom commands to be displayed in the table header.
	 * 
	 * @see TableCommandConfig#getButton()
	 */
	@Name(COMMANDS)
	List<TableCommandConfig> getCommands();

	/**
	 * Setter for {@link #getCommands()}.
	 */
	void setCommands(List<TableCommandConfig> commands);

	/**
	 * Whether this table supports storing multiple table settings. When enabled, the table provides
	 * additional save- and load commands in the user interface, which allow the user to store and load
	 * different table settings (filters, columns) as named presets.
	 */
	@Name(SUPPORTS_MULTIPLE_SETTINGS_ATTRIBUTE)
	boolean getSupportsMultipleSettings();

	/**
	 * Setter for {@link #getSupportsMultipleSettings()}.
	 */
	void setSupportsMultipleSettings(boolean value);

	/**
	 * If {@link #getSupportsMultipleSettings()}, this property can specify a key as storage identifier
	 * for these settings. This key is used for different component instances to share (identify) the
	 * same set of stored settings (by using the same key). If no value is given, the stored settings
	 * are visible just to their respective component instance.
	 */
	@Name(MULTIPLE_SETTINGS_KEY_ATTRIBUTE)
	@Nullable
	String getMultipleSettingsKey();

	/**
	 * Setter for {@link #getMultipleSettingsKey()}.
	 */
	void setMultipleSettingsKey(String key);
}
