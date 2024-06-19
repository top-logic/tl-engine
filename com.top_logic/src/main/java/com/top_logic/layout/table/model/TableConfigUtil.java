/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.security.ModelMappingConfig;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.command.TableCommandConfig;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.renderer.TableRendererUtil;
import com.top_logic.model.TLObject;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * Utility for converting {@link TableConfig} to {@link TableConfiguration}.
 * 
 * @see TableConfigurationFactory#build(TableConfigurationProvider)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableConfigUtil {

	/**
	 * Externally, use
	 * {@link TableConfigurationFactory#toProvider(InstantiationContext, TableConfig)}
	 */
	static void apply(InstantiationContext context, TableConfig tableConfig, TableConfiguration result) {
		defaultColumnConfigurator(context, tableConfig).adapt(result.getDefaultColumn());
		tableConfigurator(context, tableConfig).accept(result);
	}

	static ColumnConfigurator defaultColumnConfigurator(InstantiationContext context,
			TableConfig tableConfig) {
		return plainColumnConfigurator(context, tableConfig.getDefaultColumn());
	}

	static Consumer<TableConfiguration> tableConfigurator(InstantiationContext context, TableConfig tableConfig) {
		context = nonFinal(context);

		List<Consumer<TableConfiguration>> settings = new ArrayList<>();

		settings.add(result -> {
			Map<ColumnConfig, ColumnContainerConfig> sourceParents = getColumnParents(tableConfig);
			Map<ColumnConfiguration, ColumnContainer<ColumnConfiguration>> targetParents = getColumnParents(result);
			Map<String, ColumnConfiguration> targetIndex = getColumnIndex(result);

			// Apply the structure to the target configuration.
			for (Entry<ColumnConfig, ColumnContainerConfig> entry : sourceParents.entrySet()) {
				ColumnConfig specializedConfig = entry.getKey();
				String columnName = specializedConfig.getName();

				ColumnConfiguration targetColumn = targetIndex.get(columnName);
				if (targetColumn == null) {
					targetColumn = result.declareColumn(columnName);
					targetIndex.put(columnName, targetColumn);
					targetParents.put(targetColumn, result);
				}

				// Check that the parent matches.
				ColumnContainerConfig sourceParent = entry.getValue();
				if (sourceParent != null) {
					// Only enforce parent match, if source configuration defines a group for the
					// column.
					ColumnContainer<ColumnConfiguration> actualTargetParent = targetParents.get(targetColumn);

					ColumnContainer<ColumnConfiguration> expectedTargetParent;
					if (sourceParent instanceof ColumnConfig) {
						String parentName = ((ColumnConfig) sourceParent).getName();
						expectedTargetParent = targetIndex.get(parentName);

						if (expectedTargetParent == null) {
							ColumnConfiguration newGroup = result.declareColumn(parentName);
							targetIndex.put(parentName, newGroup);
							targetParents.put(newGroup, result);

							expectedTargetParent = newGroup;
						}
					} else {
						expectedTargetParent = result;
					}

					if (actualTargetParent != expectedTargetParent) {
						actualTargetParent.removeColumn(columnName);
						expectedTargetParent.addColumn(targetColumn);
						targetParents.put(targetColumn, expectedTargetParent);
					}
				}
			}
		});

		// Apply the specialized configurations.
		for (ColumnConfig specializedConfig : tableConfig.getColumns()) {
			ColumnConfigurator columnConfigurator = columnConfigurator(context, specializedConfig);
			String columnName = specializedConfig.getName();
			settings.add(result -> {
				ColumnConfiguration targetColumn = result.getDeclaredColumn(columnName);
				assert targetColumn != null : "Structure was applied above.";
				columnConfigurator.adapt(targetColumn);
			});
		}

		addTableConfigurators(settings, context, tableConfig);

		return toTableConfigurator(settings);
	}

	static ColumnConfigurator columnConfigurator(InstantiationContext context, ColumnConfig columnConfig) {
		context = nonFinal(context);

		List<ColumnConfigurator> settings = new ArrayList<>();
		addConfigurators(settings, context, columnConfig);
		for (ColumnConfig subColumn : columnConfig.getColumns()) {
			ColumnConfigurator subConfigurator = columnConfigurator(context, subColumn);
			settings.add(column -> subConfigurator.adapt(column.declareColumn(subColumn.getName())));
		}
		return toConfigurator(settings);
	}

	private static Map<ColumnConfig, ColumnContainerConfig> getColumnParents(TableConfig table) {
		HashMap<ColumnConfig, ColumnContainerConfig> result = new LinkedHashMap<>();
		fillColumnParents(table, result, table.getColumns());
		return result;
	}

	private static Map<ColumnConfiguration, ColumnContainer<ColumnConfiguration>> getColumnParents(
			TableConfiguration table) {
		HashMap<ColumnConfiguration, ColumnContainer<ColumnConfiguration>> result =
			new LinkedHashMap<>();
		fillColumnParents(table, result, table.getDeclaredColumns());
		return result;
	}

	private static void fillColumnParents(ColumnContainer<ColumnConfiguration> parent,
			HashMap<ColumnConfiguration, ColumnContainer<ColumnConfiguration>> index,
			Collection<? extends ColumnConfiguration> columns) {
		for (ColumnConfiguration column : columns) {
			index.put(column, parent);
			fillColumnParents(column, index, column.getDeclaredColumns());
		}
	}

	private static void fillColumnParents(ColumnContainerConfig parent,
			HashMap<ColumnConfig, ColumnContainerConfig> index,
			Collection<ColumnConfig> columns) {
		for (ColumnConfig column : columns) {
			index.put(column, parent);
			fillColumnParents(column, index, column.getColumns());
		}
	}

	private static Map<String, ColumnConfiguration> getColumnIndex(TableConfiguration table) {
		HashMap<String, ColumnConfiguration> result = new LinkedHashMap<>();
		fillColumnIndex(result, table.getDeclaredColumns());
		return result;
	}

	private static void fillColumnIndex(HashMap<String, ColumnConfiguration> index,
			Collection<? extends ColumnConfiguration> columns) {
		for (ColumnConfiguration column : columns) {
			index.put(column.getName(), column);
			fillColumnIndex(index, column.getDeclaredColumns());
		}
	}

	private static Consumer<TableConfiguration> toTableConfigurator(List<Consumer<TableConfiguration>> settings) {
		return result -> {
			for (Consumer<TableConfiguration> s : settings) {
				s.accept(result);
			}
		};
	}

	private static void addTableConfigurators(List<Consumer<TableConfiguration>> settings, InstantiationContext context,
			TableConfig tableConfig) {
		ConfigurationDescriptor descriptor = tableConfig.descriptor();
		boolean fixedColumnsSet = tableConfig.valueSet(descriptor.getProperty(TableConfig.FIXED_COLUMN_COUNT)) &&
			tableConfig.getFixedColumnCount() >= 0;
		if (fixedColumnsSet) {
			settings.add(result -> result.setFixedColumnCount(tableConfig.getFixedColumnCount()));
		}
		if (fixedColumnsSet ||
			tableConfig.valueSet(descriptor.getProperty(TableConfig.TABLE_RENDERER_ATTRIBUTE))) {
			ITableRenderer tableRenderer = TableRendererUtil.getInstance(context, tableConfig.getTableRenderer());
			settings.add(result -> result.setTableRenderer(tableRenderer));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.DRAG_SOURCE_ATTRIBUTE))) {
			TableDragSource dragSource = context.getInstance(tableConfig.getDragSource());
			settings.add(result -> {
				result.setDragSource(dragSource);
			});
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.DROP_TARGETS_ATTRIBUTE))) {
			List<TableDropTarget> dropTargets =
				TypedConfiguration.getInstanceList(context, tableConfig.getDropTargets());
			settings.add(result -> result.setDropTargets(dropTargets));
		}

		// Note: Instantiation must not be lazy, since the provider might have outer references to
		// the component instance. This can only be applied, if instantiated during construction of
		// the component.
		ContextMenuProvider contextMenu = context.getInstance(tableConfig.getContextMenu());

		// Note: Defaults in configuration sub-classes must also be applied. Therefore the value-set
		// check must not happen.
		settings.add(result -> result.setContextMenu(contextMenu));

		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.COLUMN_CUSTOMIZATION_ATTRIBUTE))) {
			settings.add(result -> result.setColumnCustomization(tableConfig.getColumnCustomization()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.ID_COLUMN))) {
			settings.add(result -> result.setIDColumn(tableConfig.getIDColumn()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.DEFAULT_SORT_ORDER_ATTRIBUTE))) {
			settings.add(result -> result.setDefaultSortOrder(tableConfig.getDefaultSortOrder()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.DEFAULT_COLUMNS_ATTRIBUTE))) {
			settings.add(result -> result.setDefaultColumns(
				CollectionUtil.concatUnique(tableConfig.getDefaultColumns(), result.getDefaultColumns())));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.FOOTER_STYLE_ATTRIBUTE))) {
			settings.add(result -> result.setFooterStyle(tableConfig.getFooterStyle()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.HEADER_STYLE_ATTRIBUTE))) {
			settings.add(result -> result.setHeaderStyle(tableConfig.getHeaderStyle()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.MAX_COLUMNS_ATTRIBUTE))) {
			settings.add(result -> result.setMaxColumns(tableConfig.getMaxColumns()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.MULTI_SORT_ATTRIBUTE))) {
			settings.add(result -> result.setMultiSort(tableConfig.getMultiSort()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.PAGE_SIZE_OPTIONS_ATTRIBUTE))) {
			settings.add(result -> result.setPageSizeOptions(tableConfig.getPageSizeOptions()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.ROW_CLASS_PROVIDER_ATTRIBUTE))) {
			RowClassProvider rowClassProvider = context.getInstance(tableConfig.getRowClassProvider());
			settings.add(result -> result.setRowClassProvider(rowClassProvider));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.ROW_OBJECT_RESOURCE_PROVIDER_ATTRIBUTE))) {
			ResourceProvider resourceProvider = context.getInstance(tableConfig.getRowObjectResourceProvider());
			settings.add(result -> result.setRowObjectResourceProvider(resourceProvider));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.ROW_STYLE_ATTRIBUTE))) {
			settings.add(result -> result.setRowStyle(tableConfig.getRowStyle()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.SHOW_COLUMN_HEADER_ATTRIBUTE))) {
			settings.add(result -> result.setShowColumnHeader(tableConfig.isShowColumnHeader()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.SHOW_FOOTER_ATTRIBUTE))) {
			settings.add(result -> result.setShowFooter(tableConfig.isShowFooter()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.SHOW_TITLE_ATTRIBUTE))) {
			settings.add(result -> result.setShowTitle(tableConfig.isShowTitle()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.TABLE_NAME_ATTRIBUTE))) {
			settings.add(result -> result.setTableName(tableConfig.getTableName()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.SIDEBAR_FILTERS_ATTRIBUTE))) {
			settings.add(result -> result.setSidebarFilters(tableConfig.getSidebarFilters()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.TABLE_FILTER_PROVIDER_ATTRIBUTE))) {
			TableFilterProvider filterProvider = context.getInstance(tableConfig.getFilterProvider());
			settings.add(result -> result.setFilterProvider(filterProvider));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.FILTER_DISPLAY_PARENTS))) {
			settings.add(result -> result.setFilterDisplayParents(tableConfig.getFilterDisplayParents()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.FILTER_DISPLAY_CHILDREN))) {
			settings.add(result -> result.setFilterDisplayChildren(tableConfig.getFilterDisplayChildren()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.DEFAULT_TABLE_FILTER_PROVIDER_ATTRIBUTE))) {
			TableFilterProvider defaultFilterProvider = context.getInstance(tableConfig.getDefaultFilterProvider());
			settings.add(result -> result.setDefaultFilterProvider(defaultFilterProvider));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.RES_PREFIX_ATTRIBUTE))) {
			settings.add(result -> result.setResPrefix(tableConfig.getResPrefix()));
		} else {
			settings.add(result -> {
				if (result.getResPrefix() == ResPrefix.NONE) {
					result.setResPrefix(ResPrefix.none(tableConfig.location()));
				}
			});
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.TITLE_KEY_ATTRIBUTE))) {
			settings.add(result -> result.setTitleKey(tableConfig.getTitleKey()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.TITLE_STYLE_ATTRIBUTE))) {
			settings.add(result -> result.setTitleStyle(tableConfig.getTitleStyle()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(ModelMappingConfig.MODEL_MAPPING_ATTRIBUTE))) {
			Mapping<Object, ? extends TLObject> modelMapping = context.getInstance(tableConfig.getModelMapping());
			settings.add(result -> result.setModelMapping(modelMapping));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.EXPORTER_ATTRIBUTE))) {
			TableDataExport exporter = context.getInstance(tableConfig.getExporter());
			settings.add(result -> result.setExporter(exporter));
		}
		if (!tableConfig.getCommands().isEmpty()) {
			settings
				.add(result -> result.setCommands(
					TableConfigUtil.indexCommands(context, result.getCommands(), tableConfig.getCommands())));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.SUPPORTS_MULTIPLE_SETTINGS_ATTRIBUTE))) {
			settings.add(result -> result.setSupportMultipleSettings(tableConfig.getSupportsMultipleSettings()));
		}
		if (tableConfig.valueSet(descriptor.getProperty(TableConfig.MULTIPLE_SETTINGS_KEY_ATTRIBUTE))) {
			settings.add(result -> result.setMultipleSettingsKey(tableConfig.getMultipleSettingsKey()));
		}
	}

	/**
	 * Create a column configurator that applies all settings of the given {@link ColumnBaseConfig}
	 * to some {@link ColumnConfiguration}.
	 *
	 * @param context
	 *        The {@link InstantiationContext} to use for instantiating accessors, renderers and so
	 *        on. When creating a column configurator in a component context care must be taken to
	 *        use the component's instantiation context. Otherwise, component-local implementations
	 *        cannot be resolved.
	 * @param config
	 *        The {@link ColumnBaseConfig} to translate.
	 * @return A {@link Consumer} of {@link ColumnConfiguration}s that applies the settings to the
	 *         given object.
	 */
	public static ColumnConfigurator plainColumnConfigurator(InstantiationContext context,
			ColumnBaseConfig config) {
		List<ColumnConfigurator> settings = new ArrayList<>();
		addConfigurators(settings, context, config);
		return toConfigurator(settings);
	}

	private static ColumnConfigurator toConfigurator(List<ColumnConfigurator> settings) {
		return column -> {
			for (ColumnConfigurator s : settings) {
				s.adapt(column);
			}
		};
	}

	private static void addConfigurators(List<ColumnConfigurator> settings, InstantiationContext context,
			ColumnBaseConfig config) {
		// Prevent using the instantiation context within a closure.
		context = nonFinal(context);
		
		ConfigurationDescriptor desc = config.descriptor();
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.ACCESSOR))) {
			Accessor<?> instance = context.getInstance(config.getAccessor());
			settings.add(column -> column.setAccessor(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CELL_RENDERER))) {
			CellRenderer instance = context.getInstance(config.getCellRenderer());
			settings.add(column -> column.setCellRenderer(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CELL_STYLE))) {
			settings.add(column -> column.setCellStyle(config.getCellStyle()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CLASSIFIER))) {
			settings.add(column -> column.setClassifiers(config.getClassifier()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.COLUMN_LABEL))) {
			settings.add(column -> column.setColumnLabel(config.getColumnLabel()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.COLUMN_LABEL_KEY))) {
			settings.add(column -> column.setColumnLabelKey(config.getLabelKey()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.COMMAND_GROUP))) {
			settings.add(column -> column.setCommandGroup(config.getCommandGroup()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.COMPARATOR))) {
			Comparator<?> comparator = context.getInstance(config.getComparator());
			settings.add(column -> column.setComparator(comparator));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CONTROL_PROVIDER))) {
			ControlProvider instance = context.getInstance(config.getControlProvider());
			settings.add(column -> column.setControlProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CSS_CLASS))) {
			settings.add(column -> column.setCssClass(config.getCssClass()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CSS_CLASS_GROUP_FIRST))) {
			settings.add(column -> column.setCssClassGroupFirst(config.getCssClassGroupFirst()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CSS_CLASS_GROUP_LAST))) {
			settings.add(column -> column.setCssClassGroupLast(config.getCssClassGroupLast()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CSS_CLASS_PROVIDER))) {
			CellClassProvider instance = context.getInstance(config.getCssClassProvider());
			settings.add(column -> column.setCssClassProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.COLUMN_WIDTH))) {
			settings.add(column -> column.setDefaultColumnWidth(config.getColumnWidth()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.DESCENDING_COMPARATOR))) {
			Comparator<?> comparator = context.getInstance(config.getDescendingComparator());
			settings.add(column -> column.setDescendingComparator(comparator));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.EDIT_CONTROL_PROVIDER))) {
			ControlProvider instance = context.getInstance(config.getEditControlProvider());
			settings.add(column -> column.setEditControlProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.FIELD_PROVIDER))) {
			FieldProvider instance = context.getInstance(config.getFieldProvider());
			settings.add(column -> column.setFieldProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.EXCLUDE_FILTER_FROM_SIDEBAR))) {
			settings.add(column -> column.setExcludeFilterFromSidebar(config.hasExcludedFilterFromSidebar()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.FILTER_PROVIDER))) {
			TableFilterProvider instance = context.getInstance(config.getFilterProvider());
			settings.add(column -> column.setFilterProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.CELL_EXISTENCE_TESTER))) {
			CellExistenceTester instance = context.getInstance(config.getCellExistenceTester());
			settings.add(column -> column.setCellExistenceTester(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.ADDITIONAL_HEADERS))) {
			List<HTMLFragmentProvider> instances =
				TypedConfiguration.getInstanceList(context, config.getAdditionalHeaders());
			settings.add(column -> column.setAdditionalHeaders(instances));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.EXCEL_RENDERER))) {
			ExcelCellRenderer instance = context.getInstance(config.getExcelRenderer());
			settings.add(column -> column.setExcelRenderer(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.HEAD_CONTROL_PROVIDER))) {
			ControlProvider instance = context.getInstance(config.getHeadControlProvider());
			settings.add(column -> column.setHeadControlProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.HEAD_STYLE))) {
			settings.add(column -> column.setHeadStyle(config.getHeadStyle()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.LABEL_PROVIDER))) {
			LabelProvider instance = context.getInstance(config.getLabelProvider());
			settings.add(column -> column.setLabelProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.MANDATORY))) {
			settings.add(column -> column.setMandatory(config.isMandatory()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.PRELOAD_CONTRIBUTION))) {
			settings.add(column -> column.setPreloadContribution(config.getPreloadContribution()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.RENDERER))) {
			Renderer<?> instance = context.getInstance(config.getRenderer());
			settings.add(column -> column.setRenderer(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.PDF_RENDERER))) {
			PDFRenderer instance = context.getInstance(config.getPDFRenderer());
			settings.add(column -> column.setPDFRenderer(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.RESOURCE_PROVIDER))) {
			ResourceProvider instance = context.getInstance(config.getResourceProvider());
			settings.add(column -> column.setResourceProvider(instance));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.SELECTABLE))) {
			settings.add(column -> column.setSelectable(config.isSelectable()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.SHOW_HEADER))) {
			settings.add(column -> column.setShowHeader(config.isShowHeader()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.SORTABLE))) {
			settings.add(column -> column.setSortable(config.isSortable()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.SORT_KEY_PROVIDER))) {
			settings.add(column -> column.setSortKeyProvider(config.getSortKeyProvider()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.VISIBILITY))) {
			settings.add(column -> column.setVisibility(config.getVisibility()));
		}
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.VISIBLE))) {
			settings.add(column -> column.setVisible(config.isVisible()));
		}

		// Note: Must be handled at least after Accessor, LabelProvider and ResourceProvider,
		// because those define the default for the full-text provider.
		if (config.valueSet(desc.getProperty(ColumnBaseConfig.FULL_TEXT_PROVIDER))) {
			LabelProvider instance = context.getInstance(config.getFullTextProvider());
			settings.add(column -> column.setFullTextProvider(instance));
		}
		for (PolymorphicConfiguration<ColumnConfigurator> configuratorConfig : config.getConfigurators()) {
			settings.add(context.getInstance(configuratorConfig));
		}
	}

	/**
	 * Useful to mark a variable as non-final to prevent it from being used in an anonymous inner
	 * class.
	 */
	private static InstantiationContext nonFinal(InstantiationContext context) {
		return context;
	}

	/**
	 * Index configured {@link TableCommandProvider}s with their toolbar group as key.
	 * 
	 * @param existingCommands
	 *        Commands to extend.
	 * 
	 * @see TableCommandConfig#getToolbarGroup()
	 */
	public static Map<String, Collection<TableCommandProvider>> indexCommands(InstantiationContext context,
			Map<String, Collection<TableCommandProvider>> existingCommands, List<TableCommandConfig> commands) {
		Map<String, Collection<TableCommandProvider>> result = new HashMap<>();
		for (Entry<String, Collection<TableCommandProvider>> entry : existingCommands.entrySet()) {
			result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		for (TableCommandConfig command : commands) {
			addCommand(result, command.getToolbarGroup(), context.getInstance(command.getButton()));
		}
		return result;
	}

	/**
	 * Adds a {@link TableCommandProvider} to a given command index.
	 * 
	 * @see #indexCommands(InstantiationContext, Map, List)
	 */
	public static void addCommand(Map<String, Collection<TableCommandProvider>> commands, String toolbarGroup,
			TableCommandProvider commandProvider) {
		Collection<TableCommandProvider> group = commands.get(toolbarGroup);
		if (group == null) {
			group = new ArrayList<>();
			commands.put(toolbarGroup, group);
		}
		group.add(commandProvider);
	}
}
