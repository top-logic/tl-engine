/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfo;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfoProvider;
import com.top_logic.layout.table.provider.generic.TableConfigModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.ui.TLIDColumn;
import com.top_logic.model.annotate.ui.TLSortColumns;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TableConfigurationProvider} using information provided by the application {@link TLModel}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GenericTableConfigurationProvider extends DefaultTableConfigurationProvider {

	private static final class ShowDefaultColumnsProvider extends NoDefaultColumnAdaption {

		@SuppressWarnings("hiding")
		static final TableConfigurationProvider INSTANCE = new ShowDefaultColumnsProvider();

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			if (!CollectionUtil.isEmptyOrNull(table.getDefaultColumns())) {
				showDefaultColumns(table, table.getDefaultColumns());
			}
		}

	}

	/**
	 * {@link NoDefaultColumnAdaption} that removes some columns.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class ExcludeColumnsProvider extends NoDefaultColumnAdaption {

		private final Collection<String> _excludeColumns;

		ExcludeColumnsProvider(Collection<String> excludeColumns) {
			_excludeColumns = excludeColumns;
		}

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			excludeColumns(table, _excludeColumns);
		}
	}

	/**
	 * {@link NoDefaultColumnAdaption} that shows only the default columns.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class ShowColumnsProvider extends NoDefaultColumnAdaption {

		private final Collection<String> _visibleColumns;

		ShowColumnsProvider(Collection<String> visibleColumns) {
			_visibleColumns = visibleColumns;
		}

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			showDefaultColumns(table, _visibleColumns);
		}
	}

	/** The name of the "name" column. */
	public static final String NAME_COLUMN = AbstractWrapper.NAME_ATTRIBUTE;

	/** The supported class object. */
	protected final TLClass commonSuperType;

	/** The columns defined by the held {@link TLClass}. */
	protected final Map<String, ColumnInfo> columnInfosByName;

	private final Set<String> _mainColumns;

	/**
	 * A {@link TableConfigurationProvider} for the given full qualified type name.
	 * 
	 * @param qualifiedTypeName
	 *        Full qualified type name.
	 * 
	 * @see TLModelUtil#findType(String)
	 */
	public static TableConfigurationProvider getTableConfigurationProvider(String qualifiedTypeName) {
		return getTableConfigurationProvider(Collections.singleton(qualifiedTypeName));
	}

	/**
	 * A {@link TableConfigurationProvider} created for the given set of full qualified type names.
	 * 
	 * @param qualifiedTypeNames
	 *        Collection of full qualified type names.
	 * 
	 * @see TLModelUtil#findType(String)
	 */
	public static TableConfigurationProvider getTableConfigurationProvider(Collection<String> qualifiedTypeNames) {
		Protocol protocol = new LogProtocol(GenericTableConfigurationProvider.class);

		Set<TLClass> classes = TLModelUtil.findClasses(protocol, qualifiedTypeNames);

		if (protocol.hasErrors()) {
			return DefaultTableConfigurationProvider.INSTANCE;
		}

		return new GenericTableConfigurationProvider(classes);
	}

	/**
	 * A {@link TableConfigurationProvider} for the given type.
	 * 
	 * @param type
	 *        {@link TLClass} to create a {@link TableConfigurationProvider} for.
	 */
	public static TableConfigurationProvider getTableConfigurationProvider(TLClass type) {
		return new GenericTableConfigurationProvider(Collections.singleton(type));
	}

	/**
	 * Creates a {@link GenericTableConfigurationProvider}.
	 * 
	 * @param contentTypes
	 *        The (concrete) content types that define all table columns.
	 */
	public GenericTableConfigurationProvider(Set<? extends TLClass> contentTypes) {
		TableConfigModelInfo modelInfo = getModelInfo(contentTypes);
		commonSuperType = getFirst(modelInfo.getCommonSuperClasses());
		_mainColumns = modelInfo.getMainColumns();
		columnInfosByName = modelInfo.getColumnInfos();
	}

	private TableConfigModelInfo getModelInfo(Set<? extends TLClass> contentTypes) {
		TableConfigModelInfoProvider modelInfoProvider = TableConfigModelService.getInstance().getModelInfoProvider();
		return modelInfoProvider.getModelInfo(contentTypes);
	}

	@Override
	protected Accessor<?> getAccessor() {
		return getDefaultAccessor();
	}

	/**
	 * The default {@link ColumnInfo#getAccessor()}.
	 */
	public static Accessor<?> getDefaultAccessor() {
		return WrapperAccessor.INSTANCE;
	}

	/**
	 * Adapt the given column description manager for the held {@link TLClass}.
	 * 
	 * @param table
	 *        The column description manager to be adapted, must not be <code>null</code>.
	 */
	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		super.adaptConfigurationTo(table);
		boolean isInitialDeclaration = containsDefaultColumnsOnly(table);
		for (Entry<String, ColumnInfo> entry : columnInfosByName.entrySet()) {
			configureColumn(table, entry.getKey(), entry.getValue(), isInitialDeclaration);
		}

		if (commonSuperType != null) {
			adaptIDColumn(table);
			adaptSortColumns(table);
		}
	}


	private void adaptIDColumn(TableConfiguration table) {
		TLIDColumn idColumnAnnotation = getAnnotation(commonSuperType, TLIDColumn.class);

		if (idColumnAnnotation != null) {
			table.setIDColumn(idColumnAnnotation.getValue());
		} else {
			if (columnInfosByName.get(NAME_COLUMN) != null) {
				table.setIDColumn(NAME_COLUMN);
			}
		}
	}

	private void adaptSortColumns(TableConfiguration table) {
		TLSortColumns sortColumnsAnnotation = getAnnotation(commonSuperType, TLSortColumns.class);

		if (sortColumnsAnnotation != null) {
			table.setDefaultSortOrder(sortColumnsAnnotation.getOrder());
		}
	}

	private <A extends TLAnnotation> A getAnnotation(TLType type, Class<A> annotationClass) {
		A annotation = type.getAnnotation(annotationClass);

		if (annotation != null) {
			return annotation;
		}

		TLClass primaryGeneralization = TLModelUtil.getPrimaryGeneralization(type);
		if (primaryGeneralization != null) {
			return getAnnotation(primaryGeneralization, annotationClass);
		}

		return null;
	}

	private boolean containsDefaultColumnsOnly(TableConfiguration table) {
		Collection<? extends ColumnConfiguration> declaredColumns = table.getDeclaredColumns();
		if (declaredColumns.size() > 1) {
			return false;
		}

		if (declaredColumns.size() == 1) {
			return TableControl.SELECT_COLUMN_NAME.equals(declaredColumns.iterator().next().getName());
		}

		return true;
	}

	/**
	 * Declares the column with the given name and calls
	 * {@link #configureColumn(ColumnConfiguration, ColumnInfo, boolean)} on it.
	 */
	protected void configureColumn(TableConfiguration table, String columnName, ColumnInfo info,
			boolean isInitialDeclaration) {
		ColumnConfiguration column = table.declareColumn(columnName);
		configureColumn(column, info, isInitialDeclaration);
	}

	/**
	 * Applies the configuration to the given {@link ColumnConfiguration}.
	 */
	protected void configureColumn(ColumnConfiguration column, ColumnInfo info, boolean isInitialDeclaration) {
		info.adapt(column);
		if (isInitialDeclaration && info.getVisibility() == null) {
			column.setVisibility(getDefaultVisibility(column.getName()));
		}
	}

	private DisplayMode getDefaultVisibility(String name) {
		if (isMainColumn(name)) {
			return DisplayMode.visible;
		}
		return DisplayMode.hidden;
	}

	private boolean isMainColumn(String name) {
		return _mainColumns.contains(name);
	}

	public static void adjustColumns(TableConfiguration table, Collection<String> defaultColumns,
			Collection<String> excludeColumns) {
		excludeColumns(table, excludeColumns);
		showDefaultColumns(table, defaultColumns);
	}

	static void showDefaultColumns(TableConfiguration table, Collection<String> defaultColumns) {
		// ensure that contains is efficient.
		defaultColumns = CollectionUtil.toSet(defaultColumns);

		// Hide all but the default column.
		for (ColumnConfiguration column : table.getElementaryColumns()) {
			if (isIgnoredColumn(column)) {
				// E.g. select column and mandatory columns are always visible.
				continue;
			}
			boolean isDefaultColumn = defaultColumns.contains(column.getName());
			setColumnVisibility(column, isDefaultColumn);
		}
	}

	private static void setColumnVisibility(ColumnConfiguration column, boolean isVisible) {
		if (isVisible || column.getVisibility() != DisplayMode.excluded) {
			column.setVisible(isVisible);
		}
	}

	private static boolean isIgnoredColumn(ColumnConfiguration column) {
		return isMandatoryColumn(column) || isSelectColumn(column);
	}

	private static boolean isMandatoryColumn(ColumnConfiguration column) {
		return column.getVisibility() == DisplayMode.mandatory;
	}

	private static boolean isSelectColumn(ColumnConfiguration column) {
		return column.getName().equals(TableControl.SELECT_COLUMN_NAME);
	}

	static void excludeColumns(TableConfiguration table, Collection<String> excludeColumns) {
		// Remove excluded.
		for (String column : excludeColumns) {
			table.removeColumn(column);
		}
	}

	/**
	 * Returns an {@link TableConfigurationProvider} that adapts the table by showing the visible
	 * columns and excluding the exclude columns.
	 * 
	 * @param visibleColumns
	 *        Names of the columns that should be visible. All others (except the mandatory) will be
	 *        hidden.
	 * @param excludeColumns
	 *        Names of the columns to remove from table.
	 * 
	 * @see #excludeColumns(Collection)
	 * @see #showColumns(Collection)
	 */
	public static TableConfigurationProvider adjustColumns(Collection<String> visibleColumns,
			Collection<String> excludeColumns) {
		return TableConfigurationFactory.combine(excludeColumns(excludeColumns), showColumns(visibleColumns));
	}

	/**
	 * Returns an {@link TableConfigurationProvider} that adapts the table by showing the given
	 * columns.
	 * 
	 * @param columns
	 *        Names of the columns that should be visible. All others (except the mandatory) will be
	 *        hidden.
	 * 
	 * @since 5.7.5
	 */
	public static TableConfigurationProvider showColumns(Collection<String> columns) {
		return new ShowColumnsProvider(columns);
	}

	/**
	 * Returns an {@link TableConfigurationProvider} that adapts the table by showing the columns of
	 * {@link TableConfiguration#getDefaultColumns()}.
	 */
	public static TableConfigurationProvider showDefaultColumns() {
		return ShowDefaultColumnsProvider.INSTANCE;
	}

	/**
	 * Returns an {@link TableConfigurationProvider} that adapts the table by excluding the given
	 * columns.
	 * 
	 * @param columns
	 *        Names of the columns to remove from table.
	 * 
	 * @since 5.7.5
	 */
	public static TableConfigurationProvider excludeColumns(Collection<String> columns) {
		return new ExcludeColumnsProvider(columns);
	}

}
