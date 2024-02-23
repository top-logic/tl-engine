/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.col.AbstractFreezable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.security.ModelMappingConfig;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.model.TLObject;

/**
 * Operational settings of a table.
 * 
 * @see ColumnConfiguration
 * @see TableConfig
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TableConfiguration extends AbstractFreezable implements ColumnContainer<ColumnConfiguration> {

	enum SortDirection {
		/**
		 * Sort in ascending order
		 */
		ascending,

		/**
		 * Sort in descending order
		 */
		descending
	}

	/**
	 * The globally defined table configuration in the {@link ApplicationConfig}.
	 *
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link TableConfigurationFactory#table()} instead.
	 */
	@Deprecated
	public static TableConfiguration defaultTable() {
		return TableConfigurationFactory.getInstance().DEFAULT_TABLE;
	}

	/**
	 * Creates a {@link TableConfiguration}.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link TableConfigurationFactory#table()}, for regular tables instead. When
	 *             empty {@link TableConfiguration}s are needed, use
	 *             {@link TableConfigurationFactory#emptyTable()}.
	 */
	@Deprecated
	public static TableConfiguration table() {
		return TableConfigurationFactory.table();
	}

	/**
	 * The configuration name of this table.
	 */
	public abstract String getTableName();

	/** @see #getTableName() */
	public abstract void setTableName(String value);

	/**
	 * Whether the {@link #getTableRenderer()} property is initialized.
	 */
	public final boolean hasTableRenderer() {
		return getTableRenderer() != null;
	}

	/**
	 * The amount of columns, which shall be displayed as fixed columns.
	 */
	public abstract int getFixedColumnCount();

	/**
	 * @see #getFixedColumnCount()
	 */
	public abstract void setFixedColumnCount(int fixedColumnCount);

	/**
	 * The {@link TableRenderer} to use for writing the table.
	 */
	public abstract ITableRenderer getTableRenderer();

	/**
	 * @see TableConfig#getDragSource()
	 */
	public abstract TableDragSource getDragSource();

	/**
	 * @see #getDragSource()
	 */
	public abstract void setDragSource(TableDragSource instance);

	/**
	 * @see TableConfig#getDropTargets()
	 */
	public abstract List<TableDropTarget> getDropTargets();

	/**
	 * @see #getDropTargets()
	 */
	public abstract void setDropTargets(List<TableDropTarget> dropTargets);

	/**
	 * @see TableConfig#getContextMenu()
	 */
	public abstract ContextMenuProvider getContextMenu();

	/**
	 * @see #getContextMenu()
	 */
	public abstract void setContextMenu(ContextMenuProvider instance);

	/**
	 * Setter for {@link #getTableRenderer()}.
	 */
	public abstract void setTableRenderer(ITableRenderer tableRenderer);

	/**
	 * The {@link TableFilterProvider}, whose {@link TableFilter}s will be combined with the
	 * {@link TableFilter}s of the default {@link TableFilterProvider}
	 */
	public abstract TableFilterProvider getFilterProvider();

	/**
	 * Setter for {@link #getFilterProvider()}.
	 */
	public abstract void setFilterProvider(TableFilterProvider filterProvider);

	/**
	 * @see TableConfig#getFilterDisplayParents()
	 */
	public abstract boolean getFilterDisplayParents();

	/**
	 * Setter for {@link #getFilterDisplayParents()}.
	 */
	public abstract void setFilterDisplayParents(boolean value);

	/**
	 * @see TableConfig#getFilterDisplayChildren()
	 */
	public abstract boolean getFilterDisplayChildren();

	/**
	 * Setter for {@link #getFilterDisplayChildren()}.
	 */
	public abstract void setFilterDisplayChildren(boolean value);

	/**
	 * The default {@link TableFilterProvider} of the table.
	 */
	public abstract TableFilterProvider getDefaultFilterProvider();

	/**
	 * Setter for {@link #getDefaultFilterProvider()}.
	 */
	public abstract void setDefaultFilterProvider(TableFilterProvider filterProvider);

	/**
	 * filters, that shall be displayed in filter sidebar.
	 */
	public abstract List<String> getSidebarFilters();

	/**
	 * @see #getSidebarFilters()
	 */
	public abstract void setSidebarFilters(List<String> sidebarFilters);

	/**
	 * The {@link RowClassProvider} to build CSS classes for table content rows.
	 */
	public abstract RowClassProvider getRowClassProvider();

	/**
	 * Setter for {@link #getRowClassProvider()}.
	 */
	public abstract void setRowClassProvider(RowClassProvider value);

	/**
	 * The {@link ResourceProvider} used among others to render the preview image of the dragged
	 * rows.
	 */
	public abstract ResourceProvider getRowObjectResourceProvider();

	/**
	 * Setter for {@link #getRowObjectResourceProvider()}.
	 */
	public abstract void setRowObjectResourceProvider(ResourceProvider resourceProvider);

	/**
	 * Whether columns can be re-arranged by the user.
	 */
	public abstract ColumnCustomization getColumnCustomization();

	/** @see #getColumnCustomization() */
	public abstract void setColumnCustomization(ColumnCustomization value);

	/**
	 * Whether this table has a title row.
	 */
	public abstract boolean getShowTitle();

	/** @see #getShowTitle() */
	public abstract void setShowTitle(boolean value);

	/**
	 * Whether this table has a header row to display column names.
	 */
	public abstract boolean getShowColumnHeader();

	/** @see #getShowColumnHeader() */
	public abstract void setShowColumnHeader(boolean value);

	/**
	 * Column for which the cell value is rendered as link with an image of the row objects type
	 * before.
	 */
	public abstract String getIDColumn();

	/** @see #getIDColumn() */
	public abstract void setIDColumn(String string);

	/**
	 * True if this table has a tree like structure (tree grid, tree table), otherwise false.
	 */
	public abstract boolean isTree();

	/** @see #isTree() */
	public abstract void setTree(boolean isTree);

	/**
	 * Whether this table has a footer row.
	 */
	public abstract boolean getShowFooter();

	/** @see #getShowFooter() */
	public abstract void setShowFooter(boolean value);

	/**
	 * The default style to apply to each table row (TR element).
	 */
	public abstract String getRowStyle();

	/** @see #getRowStyle() */
	public abstract void setRowStyle(String value);

	/** Row height in pixel */
	public abstract int getRowHeight();

	/**
	 * The the {@link ResPrefix} defining the table resources if not specialized otherwise.
	 * 
	 * @see #getTitleKey()
	 * @see ColumnConfiguration#getColumnLabelKey()
	 */
	public abstract ResourceView getResPrefix();

	/** @see #getResPrefix() */
	public abstract void setResPrefix(ResourceView value);

	/**
	 * The the {@link ResKey} defining the table title.
	 */
	public abstract ResKey getTitleKey();

	/** @see #getTitleKey() */
	public abstract void setTitleKey(ResKey value);

	/**
	 * The style to apply to the table title (TH element).
	 */
	public abstract String getTitleStyle();

	/** @see #getTitleStyle() */
	public abstract void setTitleStyle(String value);

	/** Title height in pixel */
	public abstract int getTitleHeight();

	/**
	 * The style to apply to the table header (TR element).
	 */
	public abstract String getHeaderStyle();

	/** @see #getHeaderStyle() */
	public abstract void setHeaderStyle(String value);

	/** Header row height in pixel */
	public abstract int getHeaderRowHeight();

	/**
	 * The style to apply to the table footer (TH element).
	 */
	public abstract String getFooterStyle();

	/** @see #getFooterStyle() */
	public abstract void setFooterStyle(String value);

	/** Footer height in pixel */
	public abstract int getFooterHeight();

	/**
	 * Internal setter to apply theme defaults for {@link #getTitleHeight()}.
	 */
	abstract void internalSetTitleHeight(int value);

	/**
	 * Internal setter to apply theme defaults for {@link #getHeaderRowHeight()}.
	 */
	abstract void internalSetHeaderHeight(int value);
	
	/**
	 * Internal setter to apply theme defaults for {@link #getRowHeight()}.
	 */
	abstract void internalSetRowHeight(int value);

	/**
	 * Internal setter to apply theme defaults for {@link #getFooterHeight()}.
	 */
	abstract void internalSetFooterHeight(int value);
	
	/**
	 * The maximum number of columns that can be selected for display by the user.
	 */
	public abstract int getMaxColumns();

	/** @see #getMaxColumns() */
	public abstract void setMaxColumns(int value);

	/**
	 * The user-selectable options for the table page size.
	 */
	public abstract int[] getPageSizeOptions();

	/** @see #getPageSizeOptions() */
	public abstract void setPageSizeOptions(int... sizeOptions);

	/**
	 * Whether to enable the multiple sort dialog.
	 * 
	 * {@link Enabled#auto} means that the dialog should be shown, if more than one sortable column
	 * exists in the table.
	 */
	public abstract Enabled getMultiSort();

	/** @see #getMultiSort() */
	public abstract void setMultiSort(Enabled value);

	/**
	 * The default sort configuration of the table
	 */
	public abstract Iterable<SortConfig> getDefaultSortOrder();

	/**
	 * @see #getDefaultSortOrder()
	 */
	public abstract void setDefaultSortOrder(Iterable<SortConfig> sortConfig);

	/**
	 * columns, that shall be displayed by default in defined order.
	 * 
	 * @see TableConfig#getDefaultColumns()
	 */
	public abstract List<String> getDefaultColumns();

	/**
	 * @see #getDefaultColumns()
	 */
	public abstract void setDefaultColumns(List<String> defaultColumns);

	/**
	 * @see TableConfig#getModelMapping()
	 */
	public abstract Mapping<Object, ? extends TLObject> getModelMapping();

	/** @see #getModelMapping() */
	public abstract void setModelMapping(Mapping<Object, ? extends TLObject> value);

	/**
	 * @see TableConfig#getExporter()
	 */
	public abstract TableDataExport getExporter();

	/**
	 * @see #getExporter()
	 */
	public abstract void setExporter(TableDataExport exporter);

	/**
	 * Configured table commands by command clique.
	 * 
	 * @see TableConfig#getCommands()
	 */
	public abstract Map<String, Collection<TableCommandProvider>> getCommands();

	/**
	 * @see #getCommands()
	 */
	public abstract void setCommands(Map<String, Collection<TableCommandProvider>> value);

	/**
	 * @see TableConfig#getSupportsMultipleSettings()
	 */
	public abstract boolean getSupportMultipleSettings();

	/**
	 * Setter for {@link #getSupportMultipleSettings()}.
	 */
	public abstract void setSupportMultipleSettings(boolean value);

	/**
	 * @see TableConfig#getMultipleSettingsKey()
	 */
	public abstract String getMultipleSettingsKey();

	/**
	 * Setter for {@link #getMultipleSettingsKey()}
	 */
	public abstract void setMultipleSettingsKey(String key);

	@Override
	public final ColumnConfiguration removeColumn(String columnName) {
		return columns().removeColumn(columnName);
	}

	@Override
	public final ColumnConfiguration getDefaultColumn() {
		return columns().getDefaultColumn();
	}

	@Override
	public final ColumnConfiguration getDeclaredColumn(String aName) {
		return columns().getDeclaredColumn(aName);
	}

	@Override
	public final ColumnConfiguration getCol(String aName) {
		return columns().getCol(aName);
	}

	@Override
	public final ColumnConfiguration declareColumn(String columnName) {
		return columns().declareColumn(columnName);
	}

	@Override
	public final Collection<? extends ColumnConfiguration> getDeclaredColumns() {
		return columns().getDeclaredColumns();
	}

	@Override
	public final Map<String, ColumnConfiguration> createColumnIndex() {
		return columns().createColumnIndex();
	}

	@Override
	public final List<ColumnConfiguration> getElementaryColumns() {
		return columns().getElementaryColumns();
	}

	@Override
	public final List<String> getElementaryColumnNames() {
		return columns().getElementaryColumnNames();
	}

	/**
	 * The {@link ColumnContainer} in use.
	 */
	protected abstract ColumnContainer<? extends ColumnConfiguration> columns();

	/**
	 * Creates a deep copy of this {@link TableConfiguration}.
	 */
	public abstract TableConfiguration copy();

	/**
	 * Copies the global table settings to the given copy.
	 * 
	 * <p>
	 * Note: No column properties are copied.
	 * </p>
	 * 
	 * @param tableCopy
	 *        The {@link TableConfiguration} to copy global settings to.
	 */
	public void copyGlobalSettingsTo(TableConfiguration tableCopy) {
		tableCopy.setShowColumnHeader(getShowColumnHeader());
		tableCopy.setIDColumn(getIDColumn());
		tableCopy.setTree(isTree());
		tableCopy.setShowTitle(getShowTitle());
		tableCopy.setTableName(getTableName());
		tableCopy.setRowStyle(getRowStyle());
		tableCopy.setResPrefix(getResPrefix());
		tableCopy.setTitleKey(getTitleKey());
		tableCopy.setTitleStyle(getTitleStyle());
		tableCopy.setFooterStyle(getFooterStyle());
		tableCopy.setHeaderStyle(getHeaderStyle());
		tableCopy.setMaxColumns(getMaxColumns());
		tableCopy.setMultiSort(getMultiSort());
		int[] pageSizeOptions = getPageSizeOptions();
		tableCopy.setPageSizeOptions(Arrays.copyOf(pageSizeOptions, pageSizeOptions.length));
	
		ArrayList<SortConfig> sortOrderCopy = new ArrayList<>();
		for (SortConfig sortOrder : getDefaultSortOrder()) {
			sortOrderCopy.add(sortOrder);
		}
		tableCopy.setDefaultSortOrder(sortOrderCopy);
		tableCopy.setFixedColumnCount(getFixedColumnCount());
		tableCopy.setTableRenderer(getTableRenderer());
		tableCopy.setDragSource(getDragSource());
		tableCopy.setDropTargets(getDropTargets());
		tableCopy.setContextMenu(getContextMenu());
		tableCopy.setRowClassProvider(getRowClassProvider());
		tableCopy.setRowObjectResourceProvider(getRowObjectResourceProvider());
		tableCopy.setColumnCustomization(getColumnCustomization());
		tableCopy.setSidebarFilters(getSidebarFilters());
		tableCopy.setFilterProvider(getFilterProvider());
		tableCopy.setFilterDisplayParents(getFilterDisplayParents());
		tableCopy.setFilterDisplayChildren(getFilterDisplayChildren());
		tableCopy.setDefaultFilterProvider(getDefaultFilterProvider());
		tableCopy.setShowFooter(getShowFooter());
		tableCopy.setModelMapping(getModelMapping());
		tableCopy.setExporter(getExporter());

		HashMap<String, Collection<TableCommandProvider>> newCommands = new HashMap<>();
		for (Entry<String, Collection<TableCommandProvider>> entry : getCommands().entrySet()) {
			for (TableCommandProvider command : entry.getValue()) {
				TableConfigUtil.addCommand(newCommands, entry.getKey(), command);
			}
		}
		tableCopy.setCommands(newCommands);
		tableCopy.setSupportMultipleSettings(getSupportMultipleSettings());
		tableCopy.setMultipleSettingsKey(getMultipleSettingsKey());
	}

	/**
	 * Default value for {@link TableConfiguration#getDefaultFilterProvider()}.
	 */
	public static TableFilterProvider getDefaultTableFilterProvider() {
		return TableConfigurationFactory.getInstance().DEFAULT_TABLE_FILTER_PROVIDER;
	}

	/**
	 * Default value for {@link TableConfiguration#getFilterDisplayParents()}.
	 */
	public static boolean getDefaultFilterDisplayParents() {
		return TableConfigurationFactory.getInstance().DEFAULT_FILTER_DISPLAY_PARENTS;
	}

	/**
	 * Default value for {@link TableConfiguration#getFilterDisplayChildren()}.
	 */
	public static boolean getDefaultFilterDisplayChildren() {
		return TableConfigurationFactory.getInstance().DEFAULT_FILTER_DISPLAY_CHILDREN;
	}

	/**
	 * Default value for {@link TableConfiguration#getPageSizeOptions()}.
	 */
	public static int[] getDefaultPageSizeOptions() {
		return TableConfigurationFactory.getInstance().DEFAULT_PAGE_SIZE_OPTIONS;
	}

	/**
	 * Default value for {@link TableConfiguration#getMaxColumns()}.
	 */
	public static int getDefaultMaxColumns() {
		return TableConfigurationFactory.getInstance().DEFAULT_MAX_COLUMNS;
	}

	/**
	 * Default value for {@link TableConfiguration#getMultiSort()}.
	 */
	public static Enabled getDefaultMultiSort() {
		return TableConfigurationFactory.getInstance().DEFAULT_MULTI_SORT;
	}

	/**
	 * Default value for {@link TableConfiguration#getColumnCustomization()}.
	 */
	public static ColumnCustomization getDefaultColumnCustomization() {
		return TableConfigurationFactory.getInstance().DEFAULT_COLUMN_CUSTOMIZATION;
	}

	/**
	 * Default value for {@link TableConfiguration#getModelMapping()}.
	 */
	public static Mapping<Object, ? extends TLObject> getDefaultModelMapping() {
		return TableConfigurationFactory.getInstance().DEFAULT_MODEL_MAPPING;
	}

	/**
	 * Default value for {@link TableConfiguration#getExporter()}.
	 */
	public static TableDataExport getDefaultExporter() {
		return TableConfigurationFactory.getInstance().DEFAULT_EXPORTER;
	}

	/**
	 * Default value for {@link TableConfiguration#getCommands()}.
	 */
	public static Map<String, Collection<TableCommandProvider>> getDefaultCommands() {
		return TableConfigurationFactory.getInstance().DEFAULT_ADDITIONAL_COMMANDS;
	}

	/**
	 * Default value for {@link TableConfiguration#getShowFooter()}.
	 */
	public static boolean isDefaultShowFooter() {
		return TableConfigurationFactory.getInstance().DEFAULT_SHOW_FOOTER;
	}

	/**
	 * Default value for {@link TableConfiguration#getShowColumnHeader()}.
	 */
	public static boolean isDefaultShowColumnHeader() {
		return TableConfigurationFactory.getInstance().DEFAULT_SHOW_COLUMN_HEADER;
	}

	/**
	 * Default value for {@link TableConfiguration#getShowTitle()}.
	 */
	public static boolean isDefaultShowTitle() {
		return TableConfigurationFactory.getInstance().DEFAULT_SHOW_TITLE;
	}

	/**
	 * Default value for {@link TableConfiguration#getRowObjectResourceProvider()}.
	 */
	public static ResourceProvider getDefaultRowObjectResourceProvider() {
		return TableConfigurationFactory.getInstance().DEFAULT_ROW_OBJECT_RESOURCE_PROVIDER;
	}

	/**
	 * Default value for {@link TableConfiguration#getRowClassProvider()}.
	 */
	public static RowClassProvider getDefaultRowClassProvider() {
		return TableConfigurationFactory.getInstance().DEFAULT_ROW_CLASS_PROVIDER;
	}

	static abstract class Property extends AbstractProperty<TableConfiguration> {
		protected Property(String configName) {
			super(configName);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Map<String, AbstractProperty> PROPERTIES = TableUtil.index(
		new Property(TableConfig.TABLE_NAME_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setTableName((String) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getTableName();
			}
		},
		new Property(TableConfig.TABLE_RENDERER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setTableRenderer((ITableRenderer) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getTableRenderer();
			}
		},
		new Property(TableConfig.TABLE_FILTER_PROVIDER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setFilterProvider((TableFilterProvider) value);
			}
			
			@Override
			public Object get(TableConfiguration self) {
				return self.getFilterProvider();
			}
		},
		new Property(TableConfig.FILTER_DISPLAY_PARENTS) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setFilterDisplayParents(
					(value == null) ? TableConfiguration.getDefaultFilterDisplayParents() : (Boolean) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getFilterDisplayParents();
			}
		},
		new Property(TableConfig.FILTER_DISPLAY_CHILDREN) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setFilterDisplayChildren(
					(value == null) ? TableConfiguration.getDefaultFilterDisplayChildren() : (Boolean) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getFilterDisplayChildren();
			}
		},
		new Property(TableConfig.DEFAULT_TABLE_FILTER_PROVIDER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setDefaultFilterProvider((TableFilterProvider) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getDefaultFilterProvider();
			}
		},
		new Property(TableConfig.ROW_CLASS_PROVIDER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setRowClassProvider((RowClassProvider) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getRowClassProvider();
			}
		},
		new Property(TableConfig.ROW_OBJECT_RESOURCE_PROVIDER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setRowObjectResourceProvider((ResourceProvider) value);
			}
			
			@Override
			public Object get(TableConfiguration self) {
				return self.getRowObjectResourceProvider();
			}
		},
		new Property(TableConfig.SHOW_TITLE_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setShowTitle((Boolean) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getShowTitle();
			}
		},
		new Property(TableConfig.SHOW_COLUMN_HEADER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setShowColumnHeader((Boolean) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getShowColumnHeader();
			}
		},
		new Property(TableConfig.SHOW_FOOTER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setShowFooter((Boolean) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getShowFooter();
			}
		},
		new Property(TableConfig.ROW_STYLE_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setRowStyle((String) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getRowStyle();
			}
		},
		new Property(TableConfig.RES_PREFIX_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setResPrefix((ResPrefix) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getResPrefix();
			}
		},
		new Property(TableConfig.TITLE_KEY_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setTitleKey((ResKey) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getTitleKey();
			}
		},
		new Property(TableConfig.TITLE_STYLE_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setTitleStyle((String) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getTitleStyle();
			}
		},
		new Property(TableConfig.HEADER_STYLE_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setHeaderStyle((String) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getHeaderStyle();
			}
		},
		new Property(TableConfig.FOOTER_STYLE_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setFooterStyle((String) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getFooterStyle();
			}
		},
		new Property(TableConfig.MULTI_SORT_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setMultiSort((Enabled) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getMultiSort();
			}
		},
		new Property(TableConfig.DEFAULT_SORT_ORDER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setDefaultSortOrder((Iterable<SortConfig>) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getDefaultSortOrder();
			}
		},
		new Property(TableConfig.DEFAULT_COLUMNS_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setDefaultColumns((List<String>) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getDefaultColumns();
			}
		},
		new Property(TableConfig.MAX_COLUMNS_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setMaxColumns((value == null) ? TableConfiguration.getDefaultMaxColumns() : (Integer) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getMaxColumns();
			}
		},
		new Property(TableConfig.PAGE_SIZE_OPTIONS_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setPageSizeOptions((int[]) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getPageSizeOptions();
			}
		},
		new Property(TableConfig.COLUMN_CUSTOMIZATION_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setColumnCustomization((ColumnCustomization) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getColumnCustomization();
			}
		},
		new Property(ModelMappingConfig.MODEL_MAPPING_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setModelMapping((Mapping<Object, ? extends TLObject>) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getModelMapping();
			}
		},
		new Property(TableConfig.EXPORTER_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setExporter((TableDataExport) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getExporter();
			}
		},
		new Property(TableConfig.SUPPORTS_MULTIPLE_SETTINGS_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setSupportMultipleSettings((Boolean) value);
			}
			
			@Override
			public Object get(TableConfiguration self) {
				return self.getSupportMultipleSettings();
			}
		},
		new Property(TableConfig.MULTIPLE_SETTINGS_KEY_ATTRIBUTE) {
			@Override
			public void set(TableConfiguration self, Object value) {
				self.setMultipleSettingsKey((String) value);
			}

			@Override
			public Object get(TableConfiguration self) {
				return self.getMultipleSettingsKey();
			}
		});

}
