/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.model.TLObject;

/**
 * Factory for {@link TableConfiguration}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	// Required for applying theme defaults during table instantiation.
	ThemeFactory.Module.class,
	LabelProviderService.Module.class,
})
public class TableConfigurationFactory extends ManagedClass {

	/**
	 * Configuration options for {@link TableConfigurationFactory}.
	 */
	public interface Config extends ServiceConfiguration<TableConfigurationFactory> {

		/**
		 * The application-wide defaults for {@link TableConfiguration}.
		 */
		@ItemDefault
		TableConfig getTableDefaults();

	}

	/**
	 * Creates a new {@link TableConfiguration}, without any default settings.
	 */
	public static TableConfiguration emptyTable() {
		return getInstance().newTable();
	}

	/**
	 * Creates a new {@link TableConfiguration}, based on the application's default table settings.
	 */
	public static TableConfiguration table() {
		TableConfiguration result = getInstance().DEFAULT_TABLE.copy();

		Theme theme = ThemeFactory.getTheme();
		result.internalSetTitleHeight(
			(int) theme.getValue(com.top_logic.layout.form.treetable.component.Icons.FROZEN_TABLE_TITLE_HEIGHT)
				.getValue());
		result.internalSetHeaderHeight(
			(int) theme.getValue(Icons.FROZEN_TABLE_HEADER_ROW_HEIGHT).getValue());
		result.internalSetFooterHeight(
			(int) theme.getValue(Icons.FROZEN_TABLE_FOOTER_HEIGHT).getValue());
		result.internalSetRowHeight(
			(int) theme.getValue(Icons.FROZEN_TABLE_ROW_HEIGHT).getValue());

		ColumnConfiguration selectColumn = result.getDeclaredColumn(TableControl.SELECT_COLUMN_NAME);
		if (selectColumn != null) {
			selectColumn.setDefaultColumnWidth(
				theme.getValue(com.top_logic.layout.table.model.Icons.SELECTION_COLUMN_WIDTH).toString());
		}

		return result;
	}

	/**
	 * Creates a {@link TableConfig}.
	 */
	public static TableConfig tableConfig() {
		return TypedConfiguration.newConfigItem(TableConfig.class);
	}

	/**
	 * Converts a {@link TableConfig} to a {@link TableConfigurationProvider}.
	 */
	public static TableConfigurationProvider toProvider(InstantiationContext context, final TableConfig tableConfig) {
		context = nonFinal(context);

		if (tableConfig == null) {
			return emptyProvider();
		}

		final List<? extends TableConfigurationProvider> providers =
			TypedConfiguration.getInstanceListReadOnly(context, tableConfig.getConfigurationProviders());

		Consumer<TableConfiguration> tableConfigurator = TableConfigUtil.tableConfigurator(context, tableConfig);
		ColumnConfigurator defaultColumnConfigurator =
			TableConfigUtil.defaultColumnConfigurator(context, tableConfig);

		return new TableConfigurationProvider() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				for (TableConfigurationProvider provider : providers) {
					provider.adaptConfigurationTo(table);
				}
				tableConfigurator.accept(table);
			}

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				for (TableConfigurationProvider provider : providers) {
					provider.adaptDefaultColumn(defaultColumn);
				}
				defaultColumnConfigurator.adapt(defaultColumn);
			}
		};
	}

	/**
	 * Useful to mark a variable as non-final to prevent it from being used in an anonymous inner
	 * class.
	 */
	private static InstantiationContext nonFinal(InstantiationContext context) {
		return context;
	}

	/**
	 * Creates a new {@link TableConfiguration} from the given {@link TableConfigurationProvider}s. <br/>
	 * <p>
	 * <font color="red"><b>The order of the objects must be observed. <br/>
	 * Previous objects are supplemented and overwritten by subsequent ones.<br/>
	 * General rule should be: Generic instances first.</b></font>
	 * </p>
	 * 
	 * @param providers
	 *        The {@link TableConfigurationProvider}s to apply.
	 * 
	 * @see #build(TableConfigurationProvider)
	 */
	public static TableConfiguration build(TableConfigurationProvider... providers) {
		int size = providers.length;
		switch (size) {
			case 0:
				return table();
			case 1:
				return build(providers[0]);
			default:
				return build(combine(providers));
		}
	}

	/**
	 * Creates a new {@link TableConfiguration} from the given {@link Collection} of
	 * {@link TableConfigurationProvider}s. <br/>
	 * <p>
	 * <font color="red"><b>The order of the objects must be observed. <br/>
	 * Previous objects are supplemented and overwritten by subsequent ones.<br/>
	 * General rule should be: Generic instances first.</b></font>
	 * </p>
	 * 
	 * @param providers
	 *        The {@link TableConfigurationProvider}s to apply.
	 * 
	 * @see #build(TableConfigurationProvider)
	 */
	public static TableConfiguration build(Collection<? extends TableConfigurationProvider> providers) {
		int size = providers.size();
		switch (size) {
			case 0:
				return table();
			case 1:
				return build(providers.iterator().next());
			default:
				return build(combine(providers));
		}
	}

	/**
	 * Creates a new {@link TableConfiguration} from the given {@link TableConfigurationProvider}.
	 * 
	 * <p>
	 * First a new {@link TableConfiguration} is created, then the
	 * {@link TableConfiguration#getDefaultColumn() default column} is adjusted by the given
	 * {@link TableConfigurationProvider#adaptDefaultColumn(ColumnConfiguration) provider}, and at
	 * last the {@link TableConfigurationProvider#adaptConfigurationTo(TableConfiguration) table} is
	 * adjusted.
	 * </p>
	 * 
	 * @param provider
	 *        The {@link TableConfigurationProvider}s to apply.
	 */
	public static TableConfiguration build(TableConfigurationProvider provider) {
		TableConfiguration table = table();
		return apply(provider, table);
	}

	/**
	 * Applies the given {@link TableConfigurationProvider} to the given table.
	 * 
	 * @return The given table for chaining.
	 */
	public static TableConfiguration apply(TableConfigurationProvider provider, TableConfiguration table) {
		if (!isEmpty(provider)) {
			provider.adaptDefaultColumn(table.getDefaultColumn());
			provider.adaptConfigurationTo(table);
		}
		return table;
	}

	/**
	 * Dynamically adds a {@link ColumnConfig} to a given {@link TableConfig} if not yet present.
	 * 
	 * @param tableConfig
	 *        The {@link TableConfig} to modify.
	 * @param columnName
	 *        The name of the column, see {@link ColumnConfig#getName()}.
	 */
	public static ColumnConfig declareColumn(TableConfig tableConfig, String columnName) {
		assert columnName != null && !columnName.isEmpty();

		ColumnConfig column = tableConfig.getCol(columnName);
		if (column != null) {
			return column;
		}

		ColumnConfig newColumn = TypedConfiguration.newConfigItem(ColumnConfig.class);
		newColumn.setName(columnName);
		tableConfig.getColumns().add(newColumn);
		return newColumn;
	}

	/**
	 * {@link TableConfigurationProvider} setting the given default columns.
	 * 
	 * @param defaultColumns
	 *        New value of {@link TableConfiguration#getDefaultColumns()}
	 */
	public static TableConfigurationProvider setDefaultColumns(List<String> defaultColumns) {
		return new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setDefaultColumns(defaultColumns);
			}
		};

	}

	/**
	 * Returns a {@link TableConfigurationProvider} that does not adapt any column.
	 * 
	 * @see EmptyTableConfigurationProvider#INSTANCE
	 */
	public static TableConfigurationProvider emptyProvider() {
		return EmptyTableConfigurationProvider.INSTANCE;
	}

	/**
	 * Builds a combined {@link TableConfigurationProvider} from the given
	 * {@link TableConfigurationProvider}.
	 * 
	 * @param first
	 *        The first provider to apply.
	 * @param second
	 *        The second provider to apply.
	 */
	public static TableConfigurationProvider combine(TableConfigurationProvider first,
			TableConfigurationProvider second) {
		if (isEmpty(first)) {
			return second;
		}
		if (isEmpty(second)) {
			return first;
		}
		return newCombinedProvider(first, second);
	}

	private static boolean isEmpty(TableConfigurationProvider provider) {
		return provider == null || provider == emptyProvider();
	}

	/**
	 * Builds a combined {@link TableConfigurationProvider} from several sub-providers.
	 * 
	 * @param providers
	 *        The providers to apply in the given order.
	 */
	public static TableConfigurationProvider combine(Collection<? extends TableConfigurationProvider> providers) {
		int numberProviders = providers.size();
		switch (numberProviders) {
			case 0:
				return emptyProvider();
			case 1:
				return nonNull(providers.iterator().next());
			default:
				return newCombinedProvider(providers.toArray(new TableConfigurationProvider[numberProviders]));
		}
	}

	/**
	 * Builds a combined {@link TableConfigurationProvider} from several sub-providers.
	 * 
	 * @param providers
	 *        The providers to apply in the given order.
	 */
	public static TableConfigurationProvider combine(final TableConfigurationProvider... providers) {
		switch (providers.length) {
			case 0:
				return emptyProvider();
			case 1:
				return nonNull(providers[0]);
			default:
				return newCombinedProvider(providers);
		}
	}

	private static TableConfigurationProvider nonNull(TableConfigurationProvider provider) {
		return provider == null ? emptyProvider() : provider;
	}

	private static TableConfigurationProvider newCombinedProvider(final TableConfigurationProvider... providers) {
		return new TableConfigurationProvider() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				for (TableConfigurationProvider provider : providers) {
					if (provider == null) {
						continue;
					}
					provider.adaptConfigurationTo(table);
				}
			}

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				for (TableConfigurationProvider provider : providers) {
					if (provider == null) {
						continue;
					}
					provider.adaptDefaultColumn(defaultColumn);
				}
			}
		};
	}

	/**
	 * Application-wide default settings for {@link TableConfiguration}s.
	 */
	final TableConfiguration DEFAULT_TABLE;

	/**
	 * Default value for {@link TableConfiguration#getRowObjectResourceProvider()}.
	 */
	final ResourceProvider DEFAULT_ROW_OBJECT_RESOURCE_PROVIDER;

	/**
	 * Default value for {@link TableConfiguration#getRowClassProvider()}.
	 */
	final RowClassProvider DEFAULT_ROW_CLASS_PROVIDER;

	/**
	 * Default value for {@link TableConfiguration#getColumnCustomization()}.
	 */
	final ColumnCustomization DEFAULT_COLUMN_CUSTOMIZATION;

	/**
	 * Default value for {@link TableConfiguration#getModelMapping()}.
	 */
	final Mapping<Object, ? extends TLObject> DEFAULT_MODEL_MAPPING;

	/**
	 * Default value for {@link TableConfiguration#getExporter()}.
	 */
	final TableDataExport DEFAULT_EXPORTER;

	/**
	 * Default value for {@link TableConfiguration#getCommands()}.
	 */
	final Map<String, Collection<TableCommandProvider>> DEFAULT_ADDITIONAL_COMMANDS;

	/**
	 * Default value for {@link TableConfiguration#getMultiSort()}.
	 */
	final Enabled DEFAULT_MULTI_SORT;

	/**
	 * Default value for {@link TableConfiguration#getMaxColumns()}.
	 */
	final int DEFAULT_MAX_COLUMNS;

	/**
	 * Default value for {@link TableConfiguration#getPageSizeOptions()}.
	 */
	final int[] DEFAULT_PAGE_SIZE_OPTIONS;

	/**
	 * Default value for {@link TableConfiguration#getShowTitle()}.
	 */
	final boolean DEFAULT_SHOW_TITLE;

	/**
	 * Default value for {@link TableConfiguration#getShowColumnHeader()}.
	 */
	final boolean DEFAULT_SHOW_COLUMN_HEADER;

	/**
	 * Default value for {@link TableConfiguration#getShowFooter()}.
	 */
	final boolean DEFAULT_SHOW_FOOTER;

	/**
	 * Default value for {@link TableConfiguration#getDefaultFilterProvider()}.
	 */
	final TableFilterProvider DEFAULT_TABLE_FILTER_PROVIDER;

	/**
	 * Default value for {@link TableConfiguration#getFilterDisplayParents()}.
	 */
	final boolean DEFAULT_FILTER_DISPLAY_PARENTS;

	/**
	 * Default value for {@link TableConfiguration#getFilterDisplayChildren()}.
	 */
	final boolean DEFAULT_FILTER_DISPLAY_CHILDREN;

	/**
	 * Default value for {@link TableConfiguration#getDefaultColumns()}.
	 */
	final List<String> DEFAULT_DEFAULT_COLUMNS;

	final boolean DEFAULT_STORE_NAMED_SETTINGS;

	final String DEFAULT_NAMED_SETTINGS_KEY;

	/**
	 * Creates a {@link TableConfigurationFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableConfigurationFactory(InstantiationContext context, Config config) {
		super(context, config);

		TableConfig defaultsConfig = config.getTableDefaults();

		DEFAULT_ROW_CLASS_PROVIDER = context.getInstance(defaultsConfig.getRowClassProvider());

		DEFAULT_ROW_OBJECT_RESOURCE_PROVIDER = context.getInstance(defaultsConfig.getRowObjectResourceProvider());

		DEFAULT_SHOW_TITLE = defaultsConfig.isShowTitle();

		DEFAULT_SHOW_COLUMN_HEADER = defaultsConfig.isShowColumnHeader();

		DEFAULT_SHOW_FOOTER = defaultsConfig.isShowFooter();

		DEFAULT_COLUMN_CUSTOMIZATION = defaultsConfig.getColumnCustomization();

		DEFAULT_MULTI_SORT = defaultsConfig.getMultiSort();

		DEFAULT_MAX_COLUMNS = defaultsConfig.getMaxColumns();

		DEFAULT_PAGE_SIZE_OPTIONS = defaultsConfig.getPageSizeOptions();

		DEFAULT_TABLE_FILTER_PROVIDER = context.getInstance(defaultsConfig.getDefaultFilterProvider());

		DEFAULT_FILTER_DISPLAY_PARENTS = defaultsConfig.getFilterDisplayParents();

		DEFAULT_FILTER_DISPLAY_CHILDREN = defaultsConfig.getFilterDisplayChildren();

		DEFAULT_MODEL_MAPPING = context.getInstance(defaultsConfig.getModelMapping());

		DEFAULT_EXPORTER = context.getInstance(defaultsConfig.getExporter());
		
		DEFAULT_STORE_NAMED_SETTINGS = defaultsConfig.getSupportsMultipleSettings();

		DEFAULT_NAMED_SETTINGS_KEY = defaultsConfig.getMultipleSettingsKey();

		DEFAULT_ADDITIONAL_COMMANDS =
			TableConfigUtil.indexCommands(context, Collections.emptyMap(), defaultsConfig.getCommands());

		DEFAULT_DEFAULT_COLUMNS = defaultsConfig.getDefaultColumns();

		TableConfiguration defaultTable = newTable();
		TableConfigUtil.apply(context, defaultsConfig, defaultTable);
		defaultTable.freeze();

		DEFAULT_TABLE = defaultTable;
	}

	/**
	 * Creates a new {@link TableConfiguration}.
	 */
	public TableConfiguration newTable() {
		ColumnDescriptionManager result = new ColumnDescriptionManager(this);
		return result;
	}

	/**
	 * The {@link TableConfigurationFactory} service singleton.
	 */
	public static TableConfigurationFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for {@link TableConfigurationFactory}.
	 */
	public static final class Module extends TypedRuntimeModule<TableConfigurationFactory> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<TableConfigurationFactory> getImplementation() {
			return TableConfigurationFactory.class;
		}

	}

}
