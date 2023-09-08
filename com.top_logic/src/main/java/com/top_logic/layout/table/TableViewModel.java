 /*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilteredIterator;
import com.top_logic.basic.col.InverseComparator;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MappingIterator;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.NullList;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WrappedModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.control.TableControl.Slice;
import com.top_logic.layout.table.display.ClientDisplayData;
import com.top_logic.layout.table.display.ColumnAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.TableFilterEvent;
import com.top_logic.layout.table.filter.TableFilterListener;
import com.top_logic.layout.table.model.AbstractTableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.model.export.AccessContext;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.Utils;

/**
 * A GUI centered adapter for a {@link TableModel}.
 *
 * <p>
 * Enriches a {@link TableModel} with (single) row selection and column sorting.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableViewModel extends AbstractTableModel implements WrappedModel, TableModelListener,
		TableFilterListener, ToBeValidated {

	/**
	 * Value indicating that the default fixed column amount has not been personalized.
	 * 
	 * @see #getPersonalFixedColumns()
	 */
	public static final int NO_FIXED_COLUMN_PERSONALIZATION = -1;

	/**
	 * Value indicating that the default column width has not been personalized.
	 * 
	 * @see #getProgrammaticColumnWidth(int)
	 */
	public static final int NO_COLUMN_WIDTH_PERSONALIZATION = -1;

	/**
	 * Value indicating that no column was found.
	 * 
	 * @see #getSortedColumn()
	 * @see #getSortedApplicationModelColumn()
	 * @see #getViewModelColumn(int)
	 */
	public static final int NO_COLUMN = -1;

	/** The initial model this one is based on */
	private final TableModel applicationModel;

    /** The current version of the format, used to store sidebar filters in personal configuration */
    private static final double SIDEBAR_FILTERS_FORMAT_VERSION = 1.0d;
    
    /** The current version of the format, used to store column permutation in personal configuration */
    private static final double COLUMN_PERMUTATION_FORMAT_VERSION = 2.0d;
    
    /** The current version of the format, used to store filter configuration in personal configuration */
    private static final double FILTER_CONFIGURATION_FORMAT_VERSION = 2.0d;
    
    /** The current version of the format, used to store column widths in personal configuration */
    private static final double COLUMN_WIDTH_FORMAT_VERSION = 1.0d;
    
    /** The current version of the format, used to store fixed column count in personal configuration */
    private static final double FIXED_COLUMNS_FORMAT_VERSION = 1.0d;
    
	private static final String SIDEBAR_FILTERS_KEY = "sidebarFilters";

    /**
	 * The number of elements initially displayed per page.
	 */
	public static final int INITIAL_VIEW_PORT_ROW_COUNT = 20;
	
	/**
	 * Unique name to mark a global position in the table hierarchy, used by {@link TableFilter}s,
	 * as same as the unique names, which mark table columns.
	 */
	public static final String GLOBAL_TABLE_FILTER_ID = "globalTableFilter";

	/**
	 * @see #getOpenSlice()
	 */
	private Slice _openSlice;
    
    /**
     * Mapping of columns to their column width
     */
    private Map<String, Integer> columnWidths;

	/**
	 * The user adjusted amount of columns, which shall be displayed as fixed columns
	 */
	private int personalFixedColumns;

	/**
	 * Mapping of the columns that are changed by the user.
	 *
	 * So this list holds in the content the original value of the column,
	 * the numbering of the List represents the actual configuration of the user.
	 */
	private List<Integer> _columnPermutation;

	/**
	 * The sort direction for each column of the underlying application model.
	 *
	 * <p>
	 * This list contains an entry for each column in the application model (has
	 * the same size as {@link #getApplicationModel()} has columns). For each
	 * column, the value in this list is either {@link Boolean#TRUE} for
	 * ascending sort, {@link Boolean#FALSE} for descending sort, or
	 * <code>null</code> for a column that does not take part in sorting.
	 * </p>
	 *
	 * @see #sortColumns
	 */
	private final ArrayList<Boolean> sortDirection = new ArrayList<>();

	/**
	 * The column indices of the {@link #getApplicationModel()} columns that
	 * should be sorted. The order of the list is the priority of the column
	 * comparators.
	 *
	 * <p>
	 * For each entry in this list, a non-<code>null</code> value is stored at
	 * the corresponding index in the {@link #sortDirection} list.
	 * </p>
	 *
	 * <p>
	 * This list is empty, if this model is not sorted.
	 * </p>
	 *
	 * @see #sortDirection
	 */
	private final ArrayList<Integer> sortColumns = new ArrayList<>();

	/**
	 * Array with (ascending and descending) comparators for this table model.
	 * 
	 * <p>
	 * The array (if initialized) is twice as long as the number of columns of this table model. The
	 * first {@link #getColumnCountApplication()} entries are reserved for ascending comparators,
	 * the second half of the entries contain (optionally) descending comparators. In very rare
	 * circumstances, a table model must use hand-crafted comparators for sorting in descending
	 * order instead of simply inverting the ascending comparators.
	 * </p>
	 */
	private Comparator[] comparators;

	/**
	 * The list of possible TableFilter for a column number.
	 *
	 * <p>
	 * This array is indexed with column indices of the application model.
	 * </p>
	 */
	private TableFilter[] availableFilters;

	private TableFilter _globalFilter;

	private List<String> _sidebarFilters;

	/**
	 * Becomes <code>false</code>, if the current filter configuration has changed.
	 */
	private boolean filterValid;

	/**
	 * Becomes <code>false</code>, if the current sort configuration has changed.
	 *
	 * @see #sortColumns
	 * @see #sortDirection
	 */
	private boolean orderValid;

	private List<Slice> _sliceRequests;

	private final PagingModel _pagingModel;

	private final ConfigKey _configKey;

	private ColumnChangeVetoListener _vetoListener;

	private ClientDisplayData clientDisplayData;

	/**
	 * Create a new {@link TableViewModel} that is based on the given application {@link TableModel}.
	 */
	protected TableViewModel(TableModel applicationModel, ConfigKey configKey) {
		super(applicationModel.getTableConfiguration(), applicationModel.getColumnNames(), false);

		this.applicationModel    = applicationModel;
		clientDisplayData = new ClientDisplayData();

		this.columnWidths      = new HashMap<>();

		this.applicationModel.addTableModelListener(new WeakTableModelListener(this));

		initState();

		// Consistent state after creation.
		clearSlices();
		personalFixedColumns = NO_FIXED_COLUMN_PERSONALIZATION;

		this._configKey = configKey;
		this._pagingModel = new PagingModel(configKey, this);
		this._pagingModel.setPageSizeOptions(getTableConfiguration().getPageSizeOptions());

		loadConfiguration();
		validate();
	}

	private void initState() {
		int columnCount = applicationModel.getColumnCount();
		availableFilters = new TableFilter[columnCount];
		_sidebarFilters = new LinkedList<>();
		comparators = new Comparator[2 * columnCount];
		sortDirection.addAll(new NullList(columnCount));

		createInitialFilterConfiguration();

		initFromConfiguration();
	}

	private void initFromConfiguration() {
		for (int n = 0, cnt = applicationModel.getColumnCount(); n < cnt; n++) {
			ColumnConfiguration column = applicationModel.getColumnDescription(n);
			setColumnComparator(n, column.getComparator());
			setDescendingColumnComparator(n, column.getDescendingComparator());
		}
	}

	private void createInitialFilterConfiguration() {
		if (!isInFiniteTree()) {
			createGlobalFilter();
			createColumnFilters();
		}
	}

	private void createGlobalFilter() {
		TableFilter defaultGlobalFilter = createDefaultGlobalFilterFromConfiguration();
		TableFilter globalFilter = createGlobalFilterFromConfiguration();

		if (defaultGlobalFilter != null || globalFilter != null) {
			TableFilter filterToSet;
			if (defaultGlobalFilter != null) {
				filterToSet = defaultGlobalFilter;
				if (globalFilter != null) {
					defaultGlobalFilter.addSubFilterFrom(globalFilter);
				}
			} else {
				filterToSet = globalFilter;
			}
			setGlobalFilter(filterToSet);
		}
	}

	private void createColumnFilters() {
		int columnCount = getApplicationModel().getColumnCount();
		for (int column = 0; column < columnCount; column++) {
			ColumnConfiguration columnConfiguration = applicationModel.getColumnDescription(column);
			TableFilterProvider theProvider = columnConfiguration.getFilterProvider();
			if (theProvider != null) {
				String filterPosition = getApplicationModel().getColumnName(column);
				TableFilter filter = theProvider.createTableFilter(this, filterPosition);
				if (filter == null) {
					continue;
				}
				filter.setMatchCountingEnabled(isFilterCountingEnabled());
				setFilter(filterPosition, filter);
			}
		}
	}

	private TableFilter createGlobalFilterFromConfiguration() {
		TableFilterProvider globalFilterProvider = getTableConfiguration().getFilterProvider();
		return createFilterWithProvider(globalFilterProvider);
	}

	private TableFilter createDefaultGlobalFilterFromConfiguration() {
		TableFilterProvider defaultGlobalFilterProvider = getTableConfiguration().getDefaultFilterProvider();
		return createFilterWithProvider(defaultGlobalFilterProvider);
	}

	private TableFilter createFilterWithProvider(TableFilterProvider filterProvider) {
		if (filterProvider != null) {
			return filterProvider.createTableFilter(this, TableViewModel.GLOBAL_TABLE_FILTER_ID);
		} else {
			return null;
		}
	}

	public ConfigKey getConfigKey() {
		return _configKey;
	}

	/**
	 * The {@link PagingModel} of this table.
	 */
	public PagingModel getPagingModel() {
		return _pagingModel;
	}

	public final TableModel getApplicationModel() {
		return applicationModel;
	}

	@Override
	public void setColumns(final List<String> columnNames) throws VetoException {
		if (getColumnNames().equals(columnNames)) {
			return;
		}

		if (_vetoListener != null) {
			ColumnChangeEvtBuilder builder = new ColumnChangeEvtBuilder();
			builder.setNewValue(columnNames);
			List<Integer> columnPermutation = columnPermutation();
			ArrayList<String> oldColumnNames = new ArrayList<>(columnPermutation.size());
			for (Integer viewColumn : columnPermutation) {
				oldColumnNames.add(getApplicationModelColumnName(viewColumn));
			}
			builder.setOldValue(oldColumnNames);
			builder.setSource(this);
			_vetoListener.checkVeto(builder.toChangeEvent());
		}
		internalSetColumnNames(columnNames);
	}

	@Override
	public void handleColumnsChanged() {
		super.handleColumnsChanged();

		// Must be lazy, because events are fired during construction.
		invalidateColumnPermutation();
	}

	private void invalidateColumnPermutation() {
		_columnPermutation = null;
	}

	private List<Integer> columnPermutation() {
		revalidateColumnPermutation();
		return _columnPermutation;
	}

	private void revalidateColumnPermutation() {
		if (_columnPermutation == null) {
			_columnPermutation = createColumnPermutation(getColumnNames());
		}
	}

	void internalSetColumnNames(List<String> newColumnNames) {
		List<String> oldColumns = getColumnNames();
		newColumnNames = initColumns(newColumnNames);

		updateViewportState(newColumnNames);

		final Map<String, Integer> hiddenApplicationColumnIndicesByName = findInvisibleColumns(newColumnNames);

		this.saveColumnOrder();

		this.adjustSortOrder(hiddenApplicationColumnIndicesByName.keySet());

		boolean changed = false;
		for (int applicationColumn : hiddenApplicationColumnIndicesByName.values()) {
			if (!isVisible(getFilter(applicationColumn))) {
				changed |= resetFilter(applicationColumn);
			}
		}
		if (changed) {
			saveFilter();
		}

		fireColumnsChanged(oldColumns, newColumnNames);
	}

	private void updateViewportState(List<String> newColumnNames) {
		ViewportState viewportState = getClientDisplayData().getViewportState();

		ColumnAnchor columnAnchor = viewportState.getColumnAnchor();
		if (columnAnchor != ColumnAnchor.NONE) {
			if (!newColumnNames.contains(columnAnchor.getColumnName())) {
				viewportState.setColumnAnchor(ColumnAnchor.NONE);
			}
		}
		ColumnAnchor lastClickedColumn = viewportState.getLastClickedColumn();
		if (lastClickedColumn != ColumnAnchor.NONE) {
			if (!newColumnNames.contains(lastClickedColumn.getColumnName())) {
				viewportState.setLastClickedColumn(ColumnAnchor.NONE);
			}
		}
	}

	private List<Integer> createColumnPermutation(List<String> columnNames) {
		List<Integer> newColumnPermutation = new ArrayList<>(columnNames.size());
		for (String columnName : columnNames) {
			newColumnPermutation.add(applicationModel.getColumnIndex(columnName));
		}
		return Collections.unmodifiableList(newColumnPermutation);
	}

	private Map<String, Integer> findInvisibleColumns(final List<String> newColumnNames) {
		final Map<String, Integer> hiddenApplicationColumnIndicesByName =
			MapUtil.createIndexMap(applicationModel.getColumnNames());
		dropColumns(newColumnNames, hiddenApplicationColumnIndicesByName);
		return hiddenApplicationColumnIndicesByName;
	}

	private static void dropColumns(List<String> columnNames,
			Map<String, Integer> hiddenApplicationColumnIndicesByName) {
		for (String columnName : columnNames) {
			hiddenApplicationColumnIndicesByName.remove(columnName);
		}
	}

	private void adjustSortOrder(Set<String> hiddenColumns) {
		Map<String, SortConfig> sortConfigByName =
			MapUtil.mapValuesInto(new LinkedHashMap<>(), SortConfig.TO_NAME, getSortOrder());

		boolean changed = sortConfigByName.keySet().removeAll(hiddenColumns);
		if (changed) {
			internalSetSortOrder(sortConfigByName.values().iterator());

			saveSortOrder();
		}
	}

	public List<SortConfig> getSortOrder() {
		int sortSize = getSortColumnCount();
		if (sortSize == 0) {
			return Collections.emptyList();
		}

		ArrayList<SortConfig> result = new ArrayList<>(sortSize);
		for (int n = 0, cnt = sortSize; n < cnt; n++) {
			int applicationColumn = getSortApplicationModelColumn(n);

			boolean ascending = internalGetSortDirection(applicationColumn);
			String columnName = getApplicationModelColumnName(applicationColumn);

			result.add(SortConfigFactory.sortConfig(columnName, ascending));
		}
		return result;
	}

	public void setSortOrder(Iterable<SortConfig> sortOrder) {
		internalSetSortOrder(sortOrder.iterator());
		fireSortOrderChanged();
	}

	/**
	 * Applies the given {@link SortConfig}s.
	 * <p>
	 * Optimised variant of {@link #internalSetSortOrder(Iterator, Map)} which first checks that
	 * there is some {@link SortConfig} before creating index map
	 * </p>
	 * 
	 * @param sortOrder
	 *        collection of {@link SortConfig} to apply.
	 * 
	 * @see #internalSetSortOrder(Iterator, Map)
	 */
	private void internalSetSortOrder(Iterator<SortConfig> sortOrder) {
		if (!sortOrder.hasNext()) {
			// No need to create index map
			internalResetSortOrder();
			internalInvalidateOrder();
		} else {
			Map<String, Integer> applicationColumnIndexByName =
				MapUtil.createIndexMap(getApplicationModel().getColumnNames());

			internalSetSortOrder(sortOrder, applicationColumnIndexByName);
		}
	}

	/**
	 * Applies the given {@link SortConfig}s.
	 * 
	 * @param sortOrder
	 *        collection of {@link SortConfig} to apply.
	 * @param applicationColumnIndexByName
	 *        maps the name of {@link TableModel#getColumnNames() the columns} in the
	 *        {@link #getApplicationModel() application model} to its index in the list.
	 */
	private void internalSetSortOrder(Iterator<SortConfig> sortOrder, Map<String, Integer> applicationColumnIndexByName) {
		internalResetSortOrder();
		while (sortOrder.hasNext()) {
			SortConfig sortConfig = sortOrder.next();
			String columnName = sortConfig.getColumnName();
			Integer applicationColumnIndex = applicationColumnIndexByName.get(columnName);
			if (applicationColumnIndex == null) {
				Logger.warn("No application column found under name '" + columnName + "'", TableViewModel.class);
				continue;
			}
			if (applicationModel.getColumnDescription(applicationColumnIndex).isSortable()) {
				addSortApplicationModelColumn(applicationColumnIndex, sortConfig.getAscending());
			}
		}
		internalInvalidateOrder();
	}

	private String getSortColumnNames() {
		StringBuilder columns = new StringBuilder();
		boolean isNotFirst = false;
		columns.append("'");
		for (Integer columnIndex : sortColumns) {
			if (isNotFirst) {
				columns.append(", ");
			}
			columns.append(applicationModel.getColumnName(columnIndex));
			if (!isNotFirst) {
				isNotFirst = true;
			}

		}
		columns.append("'");
		return columns.toString();
	}

	private String getUsedFilterColumnNames() {
		StringBuilder columns = new StringBuilder();
		boolean isNotFirst = false;
		columns.append("'");
		for (ColumnFilterHolder columnFilterHolder : createUsedFilterHolders()) {
			if (isNotFirst) {
				columns.append(", ");
			}
			columns.append(columnFilterHolder.getFilterPosition());
			if (!isNotFirst) {
				isNotFirst = true;
			}

		}
		columns.append("'");
		return columns.toString();
	}

	public final int getViewModelColumn(int applicationColumn) {
		if (applicationColumn < 0) {
			return NO_COLUMN;
		}
		return columnPermutation().indexOf(Integer.valueOf(applicationColumn));
	}

	public final int getViewModelRow(int applicationRow) {
		return applicationRow;
	}

	/**
	 * The view model column that is currently sorted.
	 * 
	 * @return The currently sorted column index in this view model, {@link #NO_COLUMN}, if no
	 *         column is currently sorted.
	 */
	public final int getSortedColumn() {
		return getViewModelColumn(getSortedApplicationModelColumn());
	}

	/**
	 * Returns the sorted column index in the {@link #getApplicationModel()}.
	 */
	public final int getSortedApplicationModelColumn() {
		if (this.sortColumns.isEmpty()) {
			return NO_COLUMN;
		} else {
			return sortColumns.get(0).intValue();
		}
	}

	public final boolean isSorted(int viewColumn) {
		return isSortedApplicationModelColumn(getApplicationModelColumn(viewColumn));
	}

	private boolean isSortedApplicationModelColumn(int applicationColumn) {
		return sortDirection.get(applicationColumn) != null;
	}

	public final boolean getAscending(int sortedApplicationColumn) {
		return getAscendingApplicationModelColumn(getApplicationModelColumn(sortedApplicationColumn));
	}

	private boolean getAscendingApplicationModelColumn(int sortedApplicationColumn) {
		return sortDirection.get(sortedApplicationColumn).booleanValue();
	}

	public final boolean isSortable(int viewColumn) {
		return isSortableApplicationModelColumn(getApplicationModelColumn(viewColumn));
	}

	/**
	 * Whether the column with the given index can be filtered.
	 */
	public boolean canFilter(int column) {
		List filterList = getColumnFilters(column);
		boolean hasFilters = !filterList.isEmpty();
		return hasFilters;
	}

	private boolean isSortableApplicationModelColumn(int applicationColumn) {
		return comparators != null && applicationColumn != -1 && comparators[applicationColumn] != null;
	}

	/**
	 * @see #getGlobalFilter()
	 */
	public void setGlobalFilter(TableFilter filter) {
		assert filter != null : "Table filter must not be null";
		_globalFilter = filter;
		filter.addTableFilterListener(this);
	}

	/**
	 * true, if a global filter is defined and active, false otherwise
	 * 
	 * @see #getGlobalFilter()
	 */
	public boolean hasActiveGlobalFilter() {
		return isActive(_globalFilter);
	}

	/**
	 * This method sets a new {@link TableFilter} to a view column.
	 * 
	 * <p>
	 * <b>Note:</b> Table filters, that were previously set at the specified view column, will be
	 * replaced.
	 * </p>
	 * 
	 * @param aViewColumn
	 *        - the view column
	 * @param tableFilter
	 *        - the table filter
	 */
	public void setFilter(int aViewColumn, TableFilter tableFilter) {
		assert tableFilter != null : "Table filter must not be null";
		int applicationColumn = getApplicationModelColumn(aViewColumn);
		setFilter(getApplicationModel().getColumnName(applicationColumn), tableFilter);
	}
	
	/**
	 * This method sets a new {@link TableFilter} to a specified location.
	 * 
	 * <p>
	 * <b>Note:</b> Table filters, that were previously set at the specified location, will be
	 * replaced.
	 * </p>
	 * 
	 * @param filterPosition
	 *        - where the filter shall be applied (e.g. column name).
	 * @param tableFilter
	 *        - the table filter
	 */
	public void setFilter(String filterPosition, TableFilter tableFilter) {
		assert tableFilter != null : "Table filter must not be null";
		TableFilter oldFilter = null;
		if (filterPosition.equals(GLOBAL_TABLE_FILTER_ID)) {
			oldFilter = _globalFilter;
			_globalFilter = tableFilter;
		} else {
			int columnIndex = getApplicationModel().getColumnIndex(filterPosition);
			oldFilter = availableFilters[columnIndex];
			availableFilters[columnIndex] = tableFilter;
		}
		tableFilter.addTableFilterListener(this);
		if (oldFilter != null) {
			oldFilter.removeTableFilterListener(this);
		}
	}

	/**
	 * This method returns the {@link TableFilter} at the specified column
	 * 
	 * @param applicationColumn
	 *        - the table column
	 */
	public TableFilter getFilter(int applicationColumn) {
		return availableFilters[applicationColumn];
	}
	
	/**
	 * Either column or global filter.
	 * 
	 * @param filterPosition
	 *        Column name of a column filter, or {@link #GLOBAL_TABLE_FILTER_ID} for the global
	 *        filter.
	 */
	public TableFilter getFilter(String filterPosition) {
		if (filterPosition.equals(GLOBAL_TABLE_FILTER_ID)) {
			return getGlobalFilter();
		} else {
			return getColumnFilter(filterPosition);
		}
	}
	
	private TableFilter getColumnFilter(String columnName) {
		int applicationColumn = getApplicationModel().getColumnIndex(columnName);
		return getFilter(applicationColumn);
	}

	/**
	 * Resets the filter of specified column to inactive state.
	 * 
	 * @return true, if filter was active or visible and therefore it's state has been reseted,
	 *         false otherwise.
	 */
	public boolean resetFilter(int applicationColumn) {
		boolean changed = clearFilter(false, getFilter(applicationColumn));
		if (changed) {
			invalidateFilters();
		}
		return changed;
	}

	/** Resets all filters to inactive state and removes all regarding personal configuration. */
	public void resetAllFilters() {
		boolean changed = clearAllFilters();
		if (changed) {
			invalidateFilters();
		}
		String filterConfigKey = filterConfigKey(getConfigKey());
		if (filterConfigKey != null) {
			getPersonalConfiguration().setValue(filterConfigKey, null);
		}
	}

	private boolean clearAllFilters() {
		boolean changed = false;
		for (TableFilter filter : availableFilters) {
			changed = clearFilter(changed, filter);
		}
		changed = clearFilter(changed, _globalFilter);
		return changed;
	}

	private boolean clearFilter(boolean changed, TableFilter filter) {
		if (isActiveOrVisible(filter)) {
			filter.removeTableFilterListener(this);
			filter.reset();
			changed = true;
			filter.addTableFilterListener(this);
		}
		return changed;
	}

	public void removeAllFilters() {
		for (TableFilter tableFilter : availableFilters) {
			unsubscribeFilter(tableFilter);
		}
		Arrays.fill(this.availableFilters, null);

		unsubscribeFilter(_globalFilter);
		_globalFilter = null;
	}

	private void unsubscribeFilter(TableFilter tableFilter) {
		if (tableFilter != null) {
			tableFilter.removeTableFilterListener(this);
		}
	}

	/**
	 * This method returns all useful filters for a column
	 * @param viewColumn
	 * 		Needs the number of the column that will be filtered
	 * @return List
	 * 		Returns an ArrayList of all filters that are useful to this column.
	 * 
	 */
	public List<ConfiguredFilter> getColumnFilters(int viewColumn) {
		int applicationColumn = getApplicationModelColumn(viewColumn);
		return getSubFilters(applicationColumn);
	}

	private List<ConfiguredFilter> getSubFilters(int applicationColumn) {
		if (applicationColumn == NO_COLUMN || availableFilters == null
			|| getFilter(applicationColumn) == null) {
			return Collections.emptyList();
		}

		return getFilter(applicationColumn).getSubFilters();
	}
	
	/**
	 * This method returns all useful filters for a column
	 * 
	 * @param filterPosition
	 *        Needs the number of the column that will be filtered
	 * @return List Returns an ArrayList of all filters that are useful to this column.
	 * 
	 */
	public List<ConfiguredFilter> getColumnFilters(String filterPosition) {
		int applicationColumn = getApplicationModel().getColumnIndex(filterPosition);
		return getSubFilters(applicationColumn);
	}

	public boolean hasFilters() {
		for (Filter<?> theFilter : this.availableFilters) {
			if (theFilter != null) {
				return true;
			}
		}

		return _globalFilter != null;
	}

	public boolean hasActiveFilters() {
		for (TableFilter theFilter : this.availableFilters) {
			if (isActive(theFilter)) {
				return true;
			}
		}

		return isActive(_globalFilter);
	}

	public boolean hasActiveFilterForColumn(int viewColumn) {
		int applicationColumn = getApplicationModelColumn(viewColumn);
		
		TableFilter filter = getFilter(applicationColumn);
		if (filter == null) {
			return false;
		}
		
		return filter.isActive();
	}

	/**
	 * TODO #18915: Remove external configuration of column comparators.
	 * 
	 * @deprecated Use {@link ColumnConfiguration#setComparator(Comparator)}.
	 */
	@Deprecated
	public void setColumnComparator(int applicationColumn, Comparator comparator) {
		assert applicationColumn < getColumnCountApplication() && applicationColumn >= 0;
		comparators[applicationColumn] = comparator;
	}

	/**
	 * TODO #18915: Remove external configuration of column comparators.
	 * 
	 * @deprecated Use {@link ColumnConfiguration#setDescendingComparator(Comparator)}.
	 */
	@Deprecated
	public void setDescendingColumnComparator(int applicationColumn, Comparator descendingComparator) {
		assert applicationColumn < getColumnCountApplication() && applicationColumn >= 0;
		assert descendingComparator == null || comparators[applicationColumn] != null :
			"A column must have a comparator before setting a descending comparator.";
		comparators[getColumnCountApplication() + applicationColumn] = descendingComparator;
	}

	/**
	 * TODO #18915: Remove external configuration of column comparators.
	 * 
	 * @deprecated Use {@link ColumnConfiguration#setComparator(Comparator)}.
	 */
	@Deprecated
	public void setColumnComparators(Comparator[] newComparators) {
		assert (newComparators == null) || (newComparators.length == getColumnCountApplication() * 2) : "new: " + newComparators.length + " != appl: " + getColumnCountApplication() * 2;
		this.comparators = newComparators;
	}

	/**
	 * Number of columns in the {@link #getApplicationModel()}.
	 * 
	 * <p>
	 * The number of columns in the application model may differ from the number of visible columns
	 * in the view model, if columns have been hidden. See {@link ColumnConfiguration#isVisible()}.
	 * </p>
	 * 
	 * @see #getColumnCount()
	 */
	private int getColumnCountApplication() {
		return applicationModel.getColumnCount();
	}

	/**
	 * whether {@link #getPersonalFixedColumns()}, if defined, or
	 *         {@link #getConfiguredFixedColumns()} otherwise. Never greater than amount of
	 *         currently visible columns.
	 */
	public int getFixedColumnCount() {
		int fixedColumns = getPersonalFixedColumns();
		if (fixedColumns == NO_FIXED_COLUMN_PERSONALIZATION) {
			fixedColumns = getConfiguredFixedColumns();
		}
		return Math.min(fixedColumns, getHeader().getColumns().size());
	}

	/**
	 * The amount of fixed columns, defined by {@link TableConfiguration#getFixedColumnCount()},
	 * enhanced by some heuristics to mark technical columns (e.g. _select) as frozen additionally.
	 */
	public int getConfiguredFixedColumns() {
		return TableModelUtils.getConfiguredFixedColumns(getTableConfiguration(), getHeader().getColumns());
	}

	/**
	 * The amount of fixed columns, adjusted by this user, if defined,
	 * {@link #NO_FIXED_COLUMN_PERSONALIZATION} otherwise
	 */
	public int getPersonalFixedColumns() {
		return personalFixedColumns;
	}

	/**
	 * Setter for {@link #getPersonalFixedColumns()}.
	 */
	public void setPersonalFixedColumns(int fixedColumns) {
		this.personalFixedColumns = fixedColumns;
		if (fixedColumns > NO_FIXED_COLUMN_PERSONALIZATION) {
			savePersonalFixColumnCount();
		} else {
			internalRemovePersonalFixColumnCount(getConfigKey());
		}
		fireContentsChanged();
	}

	public int getApplicationModelColumn(int viewColumn){
		List<Integer> columnPermutation = columnPermutation();
		if (viewColumn < 0 || viewColumn >= columnPermutation.size()) {
			return NO_COLUMN;
		}

		return (columnPermutation.get(viewColumn)).intValue();
	}

	private void addSortApplicationModelColumn(int applicationColumn, boolean ascending) {
		Object oldAscending = this.sortDirection.set(applicationColumn, Boolean.valueOf(ascending));

		if (oldAscending != null && ((Boolean) oldAscending).booleanValue() == ascending) {
			// No change.
			return;
		}

		// The column is only added, if it is not already part of the current
		// sort order. Otherwise, only the direction is adjusted.
		if (oldAscending == null) {
			this.sortColumns.add(Integer.valueOf(applicationColumn));
		}
	}

	private void internalResetSortOrder() {
		if (this.sortColumns.size() == 0) {
			// No change.
			return;
		}

		this.sortColumns.clear();
		Collections.fill(this.sortDirection, null);

		internalInvalidateOrder();
	}

	public int getSortColumn(int index) {
		return getViewModelColumn(getSortApplicationModelColumn(index));
	}

	private int getSortApplicationModelColumn(int index) {
		return this.sortColumns.get(index).intValue();
	}

	public boolean getSortDirection(int sortViewColumn) {
		return internalGetSortDirection(getApplicationModelColumn(sortViewColumn));
	}

	private boolean internalGetSortDirection(int sortApplicationColumn) {
		return this.sortDirection.get(sortApplicationColumn).booleanValue();
	}

	private String getApplicationModelColumnName(int applicationColumn) {
		return applicationModel.getColumnName(applicationColumn);
	}

	@Override
	public int getRowCount() {
		validate();
		return applicationModel.getRowCount();
	}

	@Override
	public Object getValueAt(int viewRow, int viewColumn) {
	    int applicationRow    = getApplicationModelRow(viewRow);
	    int applicationColumn = getApplicationModelColumn(viewColumn);

		return applicationModel.getValueAt(applicationRow, applicationColumn);
	}

	@Override
	public ColumnConfiguration getColumnDescription(int viewColumn) {
        return this.applicationModel.getColumnDescription(this.getApplicationModelColumn(viewColumn));
    }

	@Override
	public void setValueAt(Object aValue, int viewRow, int viewColumn) {
		applicationModel.setValueAt(aValue, getApplicationModelRow(viewRow), getApplicationModelColumn(viewColumn));
	}

	/**
	 * The row number of the given view model row in the {@link #getApplicationModel()}.
	 * 
	 * @param viewRow
	 *        The row in this {@link TableViewModel}.
	 * @return The corresponding row in {@link #getApplicationModel()}, or {@link #NO_ROW} if the
	 *         given row does not exist in this model.
	 */
	public int getApplicationModelRow(int viewRow) {
		return viewRow;
	}

	/**
	 * Moves the column with the given index.
	 *
	 * <p>
	 * The moved column in removed at the given index and inserted just before
	 * the column with the insertion index.
	 * </p>
	 *
	 * @param movedColumnIndex
	 *        The index of the (view) column that is being moved.
	 * @param insertBeforeIndex
	 *        The (view) index at which the moved column is dropped. If this
	 *        index is {@link #getColumnCount()}, the column is inserted as very
	 *        last column.
	 * @throws VetoException If the change was vetoed.
	 */
	public void moveColumn(int movedColumnIndex, int insertBeforeIndex) throws VetoException{
		moveColumns(movedColumnIndex, movedColumnIndex, insertBeforeIndex);
	}

	/**
	 * Moves the column range with the given indices.
	 * 
	 * <p>
	 * The moved column range is removed at the given first index and inserted just before the
	 * column with the insertion index.
	 * </p>
	 * 
	 * @param firstMovedColumn
	 *        The first index of the (view) column range that is being moved.
	 * @param lastMovedColumn
	 *        The last index of the (view) column range that is being moved.
	 * @param insertBeforeIndex
	 *        The (view) index at which the moved columns ae dropped. If this index is
	 *        {@link #getColumnCount()}, the columns are inserted as very last columns.
	 * @throws VetoException
	 *         If the change was vetoed.
	 */
	public void moveColumns(final int firstMovedColumn, final int lastMovedColumn, final int insertBeforeIndex)
			throws VetoException {
		if (firstMovedColumn <= insertBeforeIndex && insertBeforeIndex <= lastMovedColumn) {
			// Not moved at all.
			return;
		}

		boolean moveLeft = insertBeforeIndex < firstMovedColumn;

		// Index at which the moved column must be inserted after it is removed from its original position.
		int insertIndexAfterRemove;
		if (moveLeft) {
			insertIndexAfterRemove = insertBeforeIndex;
		} else {
			insertIndexAfterRemove = insertBeforeIndex - (lastMovedColumn - firstMovedColumn + 1);

			if (firstMovedColumn == insertIndexAfterRemove) {
				// Not moved at all.
				return;
			}
		}

		List<String> oldColumnNames = getColumnNames();
		List<String> columnNames = new ArrayList<>(oldColumnNames);

		// Actually perform the move.
		List<String> movedColumns = new ArrayList<>(columnNames.subList(firstMovedColumn, lastMovedColumn + 1));
		columnNames.removeAll(movedColumns);
		columnNames.addAll(insertIndexAfterRemove, movedColumns);

		setColumns(columnNames);

		fireColumnsChanged(oldColumnNames, columnNames);
	}

	/**
	 * This method sorts the content of a column.
	 *
	 * @param newSortedViewColumn
	 * 		The displayed column index to sort
	 * @param sortAscending
	 * 		boolean value, for ascending = true, descending = false
	 */
	public void sort(int newSortedViewColumn, boolean sortAscending) {
		if (newSortedViewColumn >= 0) {
			SortConfig sortConfig = SortConfigFactory.sortConfig(getColumnName(newSortedViewColumn), sortAscending);
			setSortOrder(Collections.singletonList(sortConfig));
		}
	}

	/**
	 * A {@link Comparator}, which determines the sort order of row objects.
	 */
	public Comparator<Object> getRowComparator() {
		int sortSize = getSortColumnCount();
		if (sortSize == 0) {
			return Equality.INSTANCE;
		}

		int mainSortedApplicationColumn = getSortApplicationModelColumn(0);
		boolean ascending = internalGetSortDirection(mainSortedApplicationColumn);

		Mapping mainRowMapping = getRowMapping(mainSortedApplicationColumn);
		Comparator mainValueComparator = getComparator(mainSortedApplicationColumn, ascending);

		Comparator additionalComparator = Equality.INSTANCE;
		for (int n = sortSize - 1; n >= 1; n--) {
			int additionalSortedApplicationColumn = getSortApplicationModelColumn(n);
			boolean additionalAscending = internalGetSortDirection(additionalSortedApplicationColumn);

			// Prepend additional comparators.
			Mapping additionalRowMapping = getRowMapping(additionalSortedApplicationColumn);
			Comparator additionalValueComparator =
				getComparator(additionalSortedApplicationColumn, additionalAscending);

			additionalComparator =
				new MappedComparator(additionalRowMapping, additionalValueComparator, additionalComparator);
		}

		return new MappedComparator(mainRowMapping, mainValueComparator, additionalComparator);
	}

	private Mapping getRowMapping(int applicationColumn) {
		return getRowMapping(applicationModel.getColumnName(applicationColumn));
	}

	public Mapping getRowMapping(String column) {
		if (!column.equals(GLOBAL_TABLE_FILTER_ID)) {
			Column headerColumn = getHeader().getColumn(column);
			ColumnConfiguration columnConfiguration;
			if (headerColumn != null) {
				columnConfiguration = headerColumn.getConfig();
			} else {
				// Row mapping for a non existing column.
				columnConfiguration = getHeader().getTableConfiguration().getDefaultColumn();
			}
			final Mapping valueMapping = columnConfiguration.getSortKeyProvider();
			return TableColumnMapping.createTableColumnMapping(applicationModel, column, valueMapping);
		} else {
			return Mappings.identity();
		}
	}

	/**
	 * Finds the comparator applicable for a given sortable application model
	 * column.
	 *
	 * @param applicationColumn
	 *        The column to retrieve the comparator for.
	 * @param ascending
	 *        The sort order.
	 * @return The comparator to use.
	 */
	private Comparator getComparator(int applicationColumn, boolean ascending) {
		assert isSortableApplicationModelColumn(applicationColumn);

		if (ascending) {
		    return this.comparators[applicationColumn];
		} else {
			Comparator result = this.comparators[applicationColumn + getColumnCountApplication()];
            if (result == null) {
        		// Use the inverted ascending comparator, if no specialized descending comparator is given.
                return new InverseComparator(this.comparators[applicationColumn]);
            } else {
            	return result;
            }
		}
	}

	public int getSortColumnCount() {
		return sortColumns.size();
	}

	/**
	* The given List is meant to be the current and unmodified business model of the table
	* This method will create a copy of the given list and apply the sort order currently used
	* to display the view model.
	*/
	public <T> List<T> getDisplayedItemOrder(List<T> aList) {
		return new ArrayList<>(aList);
	}


	@Override
	public boolean removeTableModelListener(TableModelListener listener) {
		boolean result = super.removeTableModelListener(listener);
		// TODO might have a positive Effect see #789
//		if (! hasTableModelListeners()) {
//			detach();
//		}
		return result;
	}

	/**
	 * Tests whether this {@link TableViewModel} is valid.
	 * <p>
	 * <b>Note:</b> Framework internal method
	 * </p>
	 */
	public final boolean isValid() {
		return isRowsValid();
	}

	private boolean isRowsValid() {
		return this.filterValid && this.orderValid;
	}

	private void internalInvalidateAll() {
		internalInvalidateFilters();
		internalInvalidateOrder();
	}

	private void invalidateFilters() {
		if (filterValid) {
			internalInvalidateFilters();
			fireFiltersChanged();
		}
	}

	private void internalInvalidateFilters() {
		filterValid = false;
		forceValidation();
	}

	private void internalInvalidateOrder() {
		this.orderValid = false;
		forceValidation();
	}

	private void forceValidation() {
		if (!isValid()) {
			LayoutContext context = DefaultDisplayContext.getDisplayContext().getLayoutContext();
			if (context.isInCommandPhase()) {
				context.notifyInvalid(this);
			} else {
				validate();
			}
		}
	}

	/**
	 * Ensures that this model is a valid view of its {@link #applicationModel source model}.
	 */
	private void validate() {
		if (isValid()) {
			return;
		}

		boolean isFilterValid = this.filterValid;
		boolean isOrderValid = this.orderValid;

		// Prevent recursion to validate.
		this.filterValid = true;
		this.orderValid = true;

		Comparator<Object> rowComparator = getRowComparator();
		if (! isFilterValid) {
			AccessContext context =
				applicationModel.prepareRows(applicationModel.getAllRows(), accessedFilterColumns());
			{
				try {
					applicationModel.setFilter(createRowFilter(), rowComparator);
				} catch (Throwable throwable) {
					if (isOrderValid) {
						RuntimeException filterError = ExceptionUtil.createException(
							"Cannot filter table '" + getConfigKey().get() + "' for columns "
								+ getUsedFilterColumnNames() + ".",
							Collections.singletonList(throwable));
						InfoService.logError(I18NConstants.ERROR_TABLE_FILTERING, filterError.getMessage(),
							filterError, TableViewModel.class);
					} else {
						RuntimeException filterSortError =
							ExceptionUtil.createException(
								"Cannot sort table '" + getConfigKey().get() + "' by columns " + getSortColumnNames()
									+ " or filter table for columns " + getUsedFilterColumnNames()
									+ ". Deactivate sorting.",
								Collections.singletonList(throwable));
						InfoService.logError(I18NConstants.ERROR_TABLE_FILTERING_SORTING, filterSortError.getMessage(),
							filterSortError, TableViewModel.class);
					}
					if (!isOrderValid) {
						internalResetSortOrder();
						saveSortOrder();
					}
				}
			}
			context.close();
		}

		else if (!isOrderValid) {
			AccessContext accessContext =
				applicationModel.prepareRows(applicationModel.getDisplayedRows(), accessedSortColumns());
			{
				try {
					applicationModel.setOrder(rowComparator);
				} catch (Throwable throwable) {
					RuntimeException sortError =
						ExceptionUtil.createException(
							"Cannot sort table '" + getConfigKey().get() + "' by columns " + getSortColumnNames()
								+ ". Deactivate sorting.",
							Collections.singletonList(throwable));
					InfoService.logError(I18NConstants.ERROR_TABLE_SORTING, sortError.getMessage(),
						sortError, TableViewModel.class);
					internalResetSortOrder();
					saveSortOrder();
				}
			}
			accessContext.close();
		}
	}

	private List<String> accessedSortColumns() {
		List<String> accessedColumns = new ArrayList<>();
		addActiveSortColumns(accessedColumns);
		return accessedColumns;
	}

	private void addActiveSortColumns(List<String> accessedColumns) {
		for (int sortColumnIndex : sortColumns) {
			String columnName = applicationModel.getColumnName(sortColumnIndex);
			accessedColumns.add(columnName);
		}
	}

	private List<String> accessedFilterColumns() {
		List<String> accessedColumns = new ArrayList<>();
		addActiveOrVisibleFilterColumns(accessedColumns);
		addActiveSortColumns(accessedColumns);
		return accessedColumns;
	}

	private void addActiveOrVisibleFilterColumns(Collection<String> accessedColumns) {
		if (isActiveOrVisible(getGlobalFilter()) || accessedColumns.contains(GLOBAL_TABLE_FILTER_ID)) {
			List<String> allColumnNames = getColumnNames();
			for (String columnName : allColumnNames) {
				if (!accessedColumns.contains(columnName)) {
					accessedColumns.add(columnName);
				}
			}
		} else {
			for (Column column : getHeader().getAllElementaryColumns()) {
				String columnName = column.getName();
				TableFilter filter = getFilter(columnName);
				if (isActiveOrVisible(filter) && !accessedColumns.contains(columnName)) {
					accessedColumns.add(columnName);
				}

			}
		}
	}

	private TableRowFilter createRowFilter() {
		return new TableRowFilter(createUsedFilterHolders());
	}

	private List<ColumnFilterHolder> createUsedFilterHolders() {
		List<ColumnFilterHolder> columnFilterHolders = new ArrayList<>();

		for (int applicationColumn = 0, cnt = applicationModel.getColumnCount(); applicationColumn < cnt; applicationColumn++) {
			TableFilter valueFilter = getFilter(applicationColumn);
			if (isActiveOrVisible(valueFilter)) {
				String columnName = applicationModel.getColumnName(applicationColumn);
				ColumnFilterHolder columnFilterHolder = createColumnFilterHolder(columnName, valueFilter);
				columnFilterHolders.add(columnFilterHolder);
			}
		}

		if (isActiveOrVisible(_globalFilter)) {
			columnFilterHolders.add(createColumnFilterHolder(GLOBAL_TABLE_FILTER_ID, getGlobalFilter()));
		}
		return columnFilterHolders;
	}

	@SuppressWarnings("rawtypes")
	private ColumnFilterHolder createColumnFilterHolder(String columnName, TableFilter valueFilter) {
		Mapping rowValueMapping = getRowMapping(columnName);
		CellExistenceTester cellExistenceTester = getCellExistenceTester(columnName);
		ColumnFilterHolder columnFilterHolder = new ColumnFilterHolder(columnName,
			valueFilter, rowValueMapping, cellExistenceTester);
		return columnFilterHolder;
	}

	private CellExistenceTester getCellExistenceTester(String columnName) {
		if (columnName.equals(GLOBAL_TABLE_FILTER_ID)) {
			return AllCellsExist.INSTANCE;
		} else {
			return getColumnDescription(columnName).getCellExistenceTester();
		}
	}

	public ClientDisplayData getClientDisplayData() {
		return clientDisplayData;
	}

	@Override
	public void handleTableModelEvent(TableModelEvent event) {
		switch (event.getType()) {
			case TableModelEvent.COLUMNS_UPDATE: {
				initState();
				loadConfiguration();
				fireContentsChanged();
				break;
			}

			default: {
				fireTableModelEvent(event.getFirstRow(), event.getLastRow(), event.getType());
			}
		}
	}

	/**
	 * @param newSortedApplicationColumn
	 *        the initialSortColumnIdx to set
	 * 
	 * @deprecated Use {@link #setSortOrder(Iterable)} instead
	 */
	@Deprecated
	public void setSortedApplicationModelColumn(int newSortedApplicationColumn, boolean ascending) {
		if (newSortedApplicationColumn >= 0) {
			sort(getViewModelColumn(newSortedApplicationColumn), ascending);
		}
	}

    /**
	 * Method to save the personal fixed column count to the personal TL-context.
	 * 
	 * <p>
	 * <b>Format:</b>
	 * </p>
	 * 
	 * <pre>
	 * [[Format version], [fixedColumnCount]]
	 * </pre>
	 */
	public void savePersonalFixColumnCount() {
		ConfigKey configKey = getConfigKey();
		savePersonalFixColumnCount(configKey);
	}

	private void savePersonalFixColumnCount(ConfigKey configKey) {
		List<Object> config = new ArrayList<>(2);
		List<Double> formatVersion = Collections.singletonList(FIXED_COLUMNS_FORMAT_VERSION);
		List<Integer> personalFixedColumnCount = Collections.singletonList(personalFixedColumns);
		
		config.add(formatVersion);
		config.add(personalFixedColumnCount);
		
		setPersonalFixedColumns(configKey, config);
	}

	/** Restores fixed column count of frozen tables to configured value. */
	public void resetPersonalFixedColumnCount() {
		internalRemovePersonalFixColumnCount(getConfigKey());

		fireColumnsChanged(getColumnNames(), getColumnNames());
	}

	private void internalRemovePersonalFixColumnCount(ConfigKey globalKey) {
		this.personalFixedColumns = NO_FIXED_COLUMN_PERSONALIZATION;
		setPersonalFixedColumns(globalKey, null);
	}

	private void setPersonalFixedColumns(ConfigKey globalKey, List<Object> tableConfiguration) {
		String fixedColumnCountConfigKey = fixedColumnCountConfigKey(globalKey);
		if (fixedColumnCountConfigKey == null) {
			return;
		}
		getPersonalConfiguration().setJSONValue(fixedColumnCountConfigKey, tableConfiguration);
	}

	/**
	 * Method to load the personal fixed column count from the personal TL-context. For formatting
	 * details take a look at {@link #savePersonalFixColumnCount()}.
	 */
	private void loadPersonalFixColumnCount(ConfigKey configKey) {
		String fixedColumnsConfigKey = fixedColumnCountConfigKey(configKey);
		if (fixedColumnsConfigKey == null) {
			return;
		}
    	PersonalConfiguration personalConfig = getPersonalConfiguration();

		Object fixedColumnsConfig = personalConfig.getJSONValue(fixedColumnsConfigKey);
		
		if(fixedColumnsConfig != null) {
			
			try {
				// Check format version
				List<Double> formatContainer = (List<Double>) ((List<Object>)fixedColumnsConfig).get(0);
				double configFormatVersion = formatContainer.get(0);
				
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Restoring fixed columns configuration from personal configuration. Column " +
						"configuration format version '" + configFormatVersion +
						"' found.", TableViewModel.class);
				}
				
				if(configFormatVersion == FIXED_COLUMNS_FORMAT_VERSION) {
					
					// Restore fixed column count
					List<Integer> fixedColumnsSetting = (List<Integer>) ((List<Object>)fixedColumnsConfig).get(1);
					personalFixedColumns = Math.min(fixedColumnsSetting.get(0), getColumnCount());
				}
				
				// If format version of personal configuration does not match current format version
				else {
					resetPersistentFixedColumnCount(configKey, new IllegalArgumentException());
				}
			}
			
			// In case of invalid fixed column configuration format
			catch (ClassCastException e) {
				resetPersistentFixedColumnCount(configKey, e);
			}
			
			// In case of invalid fixed column configuration format
			catch (IndexOutOfBoundsException e) {
				resetPersistentFixedColumnCount(configKey, e);
			}
		}
    }

	private void resetPersistentFixedColumnCount(ConfigKey configKey, Exception e) {
		if (Logger.isDebugEnabled(TableViewModel.class)) {
			Logger.debug("Failed to restore fixed column count from personal configuration, " +
				"due to invalid configuration format. Current format version is '" +
				FIXED_COLUMNS_FORMAT_VERSION + "'. Resetting personal configuration.",
				e, TableViewModel.class);
		}
		
		internalRemovePersonalFixColumnCount(configKey);
	}

	/**
	 * Method to save the personal column order to the personal TL-context.
	 * 
	 * <p>
	 * <b>Format:</b>
	 * </p>
	 * 
	 * <pre>
	 * [[Format version], [ColumnName_1,....,ColumnName_N], [ExcludedColumn_1,..., ExcludedColumn_N]]
	 * </pre>
	 * 
	 * Where the list of excluded columns is optional.
	 */
	public void saveColumnOrder() {
		ConfigKey configKey = getConfigKey();
		saveColumnOrder(configKey);
	}

	private void saveColumnOrder(ConfigKey configKey) {
		String columnOrderConfigKey = columnOrderConfigKey(configKey);
		if (columnOrderConfigKey == null) {
			return;
		}
		List<Object> config = new ArrayList<>(3);
		List<Double> formatVersion = Collections.singletonList(COLUMN_PERMUTATION_FORMAT_VERSION);
		List<String> columnNames = getColumnNames();

		config.add(formatVersion);
		config.add(columnNames);
		
		Set<String> excludedColumns = TableModelUtils.getDefaultVisibleColumnSet(getApplicationModel());
		excludedColumns.removeAll(columnNames);
		if (!excludedColumns.isEmpty()) {
			config.add(new ArrayList<>(excludedColumns));
		}
		
		getPersonalConfiguration().setJSONValue(columnOrderConfigKey, config);
	}

	/**
	 * Method to load the personal column order from the personal TL-context. For formatting
	 * details take a look at {@link #saveColumnOrder()}.
	 */
	private void loadColumnOrder(ConfigKey globalKey) {
		String tableColumnSortConfigKey = columnOrderConfigKey(globalKey);
		if (tableColumnSortConfigKey == null) {
			return;
		}

		PersonalConfiguration personalConfig = getPersonalConfiguration();
		List<?> config = (List<?>) personalConfig.getJSONValue(tableColumnSortConfigKey);
		if(config != null) {
			boolean dirty = false;
			try {
				// Check format version
				List<?> formatContainer = (List<?>) config.get(0);
				double configFormatVersion = (Double) formatContainer.get(0);
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Restoring column configuration from personal configuration. Column " +
						"configuration format version '" + configFormatVersion +
						"' found.", TableViewModel.class);
				}

				if(configFormatVersion == COLUMN_PERMUTATION_FORMAT_VERSION) {
					// Retrieve stored column names and currently defined column names
					List<String> storedColumnNames = (List) config.get(1);
					List<String> newColumnNames = new ArrayList<>(storedColumnNames.size());
					
					Set<String> missingDefaultColumns = TableModelUtils.getDefaultVisibleColumnSet(applicationModel);
					for (int n = 0, cnt = storedColumnNames.size(); n < cnt; n++) {
						String columnName = storedColumnNames.get(n);
						if (isExcludedColumn(columnName)) {
							// Column does no longer exist.
							dirty = true;
							continue;
						}

						missingDefaultColumns.remove(columnName);
						newColumnNames.add(columnName);
					}

					missingDefaultColumns.removeAll(getExcludedColumns(config));

					if (!missingDefaultColumns.isEmpty()) {
						dirty = true;

						// Add new default columns (default columns that are neither selected nor
						// excluded).
						newColumnNames.addAll(missingDefaultColumns);
					}

					newColumnNames = initColumns(newColumnNames);
				}
				
				// If format version of personal configuration does not match current format version
				else {
					if (Logger.isDebugEnabled(TableViewModel.class)) {
						Logger.debug("Failed to restore column permutation from personal configuration, " +
							"due to invalid configuration format version. Current format version is '" +
							COLUMN_PERMUTATION_FORMAT_VERSION + "', but '" + configFormatVersion +
							"' was found. Resetting personal configuration.",
							TableViewModel.class);
					}

					dirty = true;
				}
			}
			
			// In case of invalid column configuration format
			catch (ClassCastException e) {
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Failed to restore column permutation from personal configuration, " +
						"due to invalid configuration format. Current format version is '" +
						COLUMN_PERMUTATION_FORMAT_VERSION + "'. Resetting personal configuration.",
						e, TableViewModel.class);
				}

				dirty = true;
			}
			
			// In case of invalid column configuration format
			catch (IndexOutOfBoundsException e) {
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Failed to restore column permutation from personal configuration, " +
						"due to invalid configuration format. Current format version is '" +
						COLUMN_PERMUTATION_FORMAT_VERSION + "'. Resetting personal configuration.",
						e, TableViewModel.class);
				}

				dirty = true;
			}

			// Synchronize personal configuration, in case the set of current column names
			// is not equal to the set of stored column names
			if (dirty) {
				saveColumnOrder();
			}
		}
	}

	/** Resets the displayed columns to configured state (amount, order) */
	public void resetColumnOrder() {
		Set<String> defaultVisibleColumns = TableModelUtils.getDefaultVisibleColumnSet(applicationModel);
		internalSetColumnNames(CollectionUtil.toList(defaultVisibleColumns));

		String columnOrderConfigKey = columnOrderConfigKey(getConfigKey());
		if (columnOrderConfigKey != null) {
			getPersonalConfiguration().setValue(columnOrderConfigKey, null);
		}
	}

	private String sortOrderConfigKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, "order");
	}

	private String filterOptionsConfigKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, "_filterOptions");
	}

	private String filterConfigKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, "filter");
	}

	private String sidebarFiltersColumnKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, SIDEBAR_FILTERS_KEY);
	}

	private String columnWidthConfigKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, "columnWidths");
	}

	private String fixedColumnCountConfigKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, "fixedColumnCount");
	}

	private String columnOrderConfigKey(ConfigKey globalKey) {
		return personalConfigKey(globalKey, "column");
	}

	private String personalConfigKey(ConfigKey globalKey, String tableProperty) {
		String tableId = globalKey.get();
		if (tableId == null) {
			return null;
		}
		return tableId + tableProperty;
	}

	private boolean isExcludedColumn(String columnName) {
		return applicationModel.getColumnIndex(columnName) < 0 || getColumnDescription(columnName).getVisibility() == DisplayMode.excluded;
	}

	private List<String> getExcludedColumns(List<?> config) {
		List<String> excludedColumns;
		if (config.size() > 2) {
			excludedColumns = (List) config.get(2);
		} else {
			excludedColumns = Collections.emptyList();
		}

		Iterator<String> excludedColumnsIterator = excludedColumns.iterator();
		while (excludedColumnsIterator.hasNext()) {
			String excludedColumn = excludedColumnsIterator.next();
			if (isMandatoryColumn(excludedColumn)) {
				excludedColumnsIterator.remove();
			}
		}
		return excludedColumns;
	}

	private boolean isMandatoryColumn(String excludedColumn) {
		return applicationModel.getColumnIndex(excludedColumn) >= 0
			&& getColumnDescription(excludedColumn).isMandatory();
	}

	/**
	 * Saves the personal table sort order to the personal configuration.
	 */
	public void saveSortOrder() {
		ConfigKey configKey = getConfigKey();
		saveSortOrder(configKey);
	}

	private void saveSortOrder(ConfigKey configKey) {
		String sortOrderConfigKey = sortOrderConfigKey(configKey);
		if (sortOrderConfigKey == null) {
			return;
		}
	    PersonalConfiguration personalConfig = getPersonalConfiguration();
		personalConfig.setJSONValue(sortOrderConfigKey, Mappings.map(SortConfig.SERIALIZER, getSortOrder()));
	}

	/**
     * Loads the personal table sort order from the personal configuration.
	 */
	private void loadSortOrder(ConfigKey globalConfigKey) {
		List<?> configValue = getPersistentConfigValue(globalConfigKey);
		if (configValue != null) {
			loadPersonalConfigSortOrder(configValue);
		} else {
			loadDefaultSortOrder();
		}
	}

	/** Restores the configured sort order */
	public void resetSortOrder() {
		loadDefaultSortOrder();
		String sortOrderConfigKey = sortOrderConfigKey(getConfigKey());
		if (sortOrderConfigKey != null) {
			getPersonalConfiguration().setValue(sortOrderConfigKey, null);
		}
	}

	private List<?> getPersistentConfigValue(ConfigKey globalKey) {
		List<?> configValue;
		String tableId = globalKey.get();
		if (tableId == null) {
			configValue = null;
		} else {
			PersonalConfiguration personalConfig = getPersonalConfiguration();
			configValue = (List<?>) personalConfig.getJSONValue(sortOrderConfigKey(globalKey));
		}
		return configValue;
	}

	private void loadPersonalConfigSortOrder(List<?> configValue) {
		final Map<String, Integer> applicationColumnIndexByName =
			MapUtil.createIndexMap(getApplicationModel().getColumnNames());

		Collection<Map> sortConfig = CollectionUtil.dynamicCastView(Map.class, configValue);
		MappingIterator<Map, SortConfig> mappingIterator =
			new MappingIterator<>(SortConfig.PARSER, sortConfig.iterator());
		FilteredIterator<SortConfig> iterator = new FilteredIterator<>(mappingIterator) {

			@Override
			protected boolean test(SortConfig value) {
				return applicationColumnIndexByName.containsKey(value.getColumnName());
			}

		};
		internalSetSortOrder(iterator, applicationColumnIndexByName);
	}

	private void loadDefaultSortOrder() {
		Iterable<SortConfig> defaultSortOrder = applicationModel.getTableConfiguration().getDefaultSortOrder();
		setSortOrder(defaultSortOrder);
	}

	/**
	 * Method to save the personal filters for each column to the personal TL-context.
	 * 
	 * <p>
	 * <b>Format:</b>
	 * </p>
	 * 
	 * <pre>
	 * [[Format version], [[[ColumnName_1], [FilterConfig_1]],....,[[ColumnName_N], [FilterConfig_N]]]]
	 * </pre>
	 */
	public void saveFilter() {
		internalSaveFilter(getConfigKey());
	}

	private void internalSaveFilter(ConfigKey globalKey) {
		ArrayList<Object> filterConfigurationContainer = new ArrayList<>(2);
		List<Double> formatVersion = Collections.singletonList(FILTER_CONFIGURATION_FORMAT_VERSION); 
		ArrayList<Object> globalFilterConfiguration = new ArrayList<>(getColumnCountApplication());
		for (int applicationColumn = 0, cnt = getColumnCountApplication(); applicationColumn < cnt; applicationColumn++) {
			TableFilter filter = availableFilters[applicationColumn];
			if (isActive(filter)) {
				String columnName = getApplicationModelColumnName(applicationColumn);
				addFilterState(globalFilterConfiguration, columnName, filter);
			}
		}
		if (isActive(_globalFilter)) {
			addFilterState(globalFilterConfiguration, GLOBAL_TABLE_FILTER_ID, _globalFilter);
		}
		filterConfigurationContainer.add(formatVersion);
		filterConfigurationContainer.add(globalFilterConfiguration);

		String filterConfigKey = filterConfigKey(globalKey);
		if (filterConfigKey != null) {
			getPersonalConfiguration().setJSONValue(filterConfigKey, filterConfigurationContainer);
		}
	}

	private void addFilterState(List<Object> serializedFilterConfiguration, String columnName, TableFilter filter) {
		ArrayList<Object> columnConfiguration = new ArrayList<>(2);
		columnConfiguration.add(columnName);
		columnConfiguration.add(filter.getSerializedState());
		serializedFilterConfiguration.add(columnConfiguration);
	}

	/**
	 * Method to load the personal filters for each column from the personal TL-context.
	 * For formatting details take a look at {@link #saveFilter()}.
	 */
	private void loadFilter(ConfigKey globalKey) {
		String filterConfigKey = filterConfigKey(globalKey);
		if (filterConfigKey == null) {
			return;
		}
		PersonalConfiguration config = getPersonalConfiguration();
		Object filterConfig = (List) config.getJSONValue(filterConfigKey);
		
		if(filterConfig != null) {
			
			try {
				// Check format version
				List<Double> formatContainer = (List<Double>) ((List<Object>)filterConfig).get(0);
				double configFormatVersion = formatContainer.get(0);
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Restoring filter configuration from personal configuration. Filter " +
						"configuration format version '" + configFormatVersion +
						"' found.", TableViewModel.class);
				}

				if(configFormatVersion == FILTER_CONFIGURATION_FORMAT_VERSION) {
					
					/* Ensure that filter settings of columns that have no stored filter resets
					 * their filter state. */
					boolean changed = clearAllFilters();

					List<Object> overallFilterConfiguration = (List<Object>) ((List<Object>)filterConfig).get(1);
					
					// Apply filter configurations to registered table filters
					boolean updateFilterConfiguration = false;
					for (int i = 0, size = overallFilterConfiguration.size(); i < size; i++) {
						List singleFilterConfig = (List)overallFilterConfiguration.get(i);
						String columnName = (String)singleFilterConfig.get(0);
						
						// Lookup for table filter
						boolean missedColumnConfiguration = true;
						TableFilter tableFilter = getFilter(columnName);
						if (tableFilter != null && isNotHiddenFilter(columnName)) {
							tableFilter.setSerializedState(singleFilterConfig.get(1));
							missedColumnConfiguration = false;
						}

						else {
							if (Logger.isDebugEnabled(TableViewModel.class)) {
								Logger.debug("Cannot restore filter configuration from personal configuration, " +
										"because no filter at position '" + columnName + "' has been declared!",
										TableViewModel.class);
							}
						}
						
						// Invalid filter configuration found
						if (missedColumnConfiguration) {
							updateFilterConfiguration = true;
						}
					}
					
					// If application model is not in sync with application model
					if(updateFilterConfiguration) {
						if (Logger.isDebugEnabled(TableViewModel.class)) {
							Logger.debug("Failed to restore filter configuration from personal configuration, " +
								"because the column configuration is not in sync with the table " +
								"application model. Resetting personal configuration.",
								TableViewModel.class);
						}
						internalSaveFilter(globalKey);
					}

					if (changed || !overallFilterConfiguration.isEmpty()) {
						invalidateFilters();
					}
				}
			}
			
			// In case of invalid column configuration format
			catch(ClassCastException e) {
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Failed to restore filter configuration from personal configuration, " +
						"due to invalid configuration format. Current format version is '" +
						FILTER_CONFIGURATION_FORMAT_VERSION + "'. Resetting personal configuration.",
						e, TableViewModel.class);
				}
				
				internalSaveFilter(globalKey);
			}
			
			// In case of invalid column configuration format
			catch(IndexOutOfBoundsException e) {
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Failed to restore filter configuration from personal configuration, " +
						"due to invalid configuration format. Current format version is '" +
						FILTER_CONFIGURATION_FORMAT_VERSION + "'. Resetting personal configuration.",
						e, TableViewModel.class);
				}
				internalSaveFilter(globalKey);
			}
		}
	}
	
	private boolean isNotHiddenFilter(String columnName) {
		return getColumnNames().contains(columnName) || _sidebarFilters.contains(columnName)
			|| GLOBAL_TABLE_FILTER_ID.equals(columnName);
	}

	private void saveSidebarFilters(ConfigKey globalKey) {
		String sidebarFiltersConfigKey = sidebarFiltersColumnKey(globalKey);
		if (sidebarFiltersConfigKey == null) {
			return;
		}
		List<Object> config = new ArrayList<>(2);
		List<Double> formatVersion = Collections.singletonList(SIDEBAR_FILTERS_FORMAT_VERSION);

		config.add(formatVersion);
		config.add(_sidebarFilters);
		
		getPersonalConfiguration().setJSONValue(sidebarFiltersConfigKey, config);
	}

	private void loadSidebarFilters(ConfigKey globalConfig) {
		Object configurationContainer = getSidebarFilterConfiguration(globalConfig);
		if (configurationContainer != null) {
			loadPersonalSidebarFilters(globalConfig, configurationContainer);
		} else {
			loadDefaultSidebarFilters();
		}
	}

	private Object getSidebarFilterConfiguration(ConfigKey globalKey) {
		String sidebarFiltersConfigKey = sidebarFiltersColumnKey(globalKey);
		if (sidebarFiltersConfigKey == null) {
			return null;
		}
		PersonalConfiguration config = getPersonalConfiguration();
		Object configurationContainer = (List) config.getJSONValue(sidebarFiltersConfigKey);
		return configurationContainer;
	}

	private void loadPersonalSidebarFilters(ConfigKey globalKey, Object configurationContainer) {
		try {
			List<Double> formatContainer = (List<Double>) ((List<Object>) configurationContainer).get(0);
			// Check format version
			double configFormatVersion = formatContainer.get(0);
			if (Logger.isDebugEnabled(TableViewModel.class)) {
				Logger.debug("Restoring sidebar filter configuration from personal configuration. Sidebar " +
						"configuration format version '" + configFormatVersion +
						"' found.", TableViewModel.class);
			}
			if (configFormatVersion == SIDEBAR_FILTERS_FORMAT_VERSION) {
				List<String> pcData = (List<String>) ((List<Object>) configurationContainer).get(1);
				List<String> availableSidebarFilters = getAvailableSidebarFilters();
				_sidebarFilters.addAll(pcData);
				_sidebarFilters.retainAll(availableSidebarFilters);
				invalidateFilters();
				if (hasStoredExcludedFilters(pcData, availableSidebarFilters)) {
					saveSidebarFilters(globalKey);
				}
			}
		}

		// In case of invalid sidebar configuration format
		catch (ClassCastException ex) {
			getPersonalConfiguration().setValue(sidebarFiltersColumnKey(globalKey), null);
		} catch (IndexOutOfBoundsException ex) {
			getPersonalConfiguration().setValue(sidebarFiltersColumnKey(globalKey), null);
		}
	}

	private boolean hasStoredExcludedFilters(List<String> pcData, List<String> availableFilters) {
		List<String> storedFilters = new ArrayList<>(pcData);
		storedFilters.removeAll(availableFilters);
		return !storedFilters.isEmpty();
	}

	private void loadDefaultSidebarFilters() {
		_sidebarFilters = new ArrayList<>(getTableConfiguration().getSidebarFilters());
		_sidebarFilters.retainAll(getAvailableSidebarFilters());
		if (!_sidebarFilters.isEmpty()) {
			invalidateFilters();
		}

	}

	/**
	 * Getter for programmatic column widths
	 * 
	 * @param viewColumn
	 *        The column as seen in visible order
	 * @return a positive integer if the specified view column has a user adjusted column width,
	 *         {@link #NO_COLUMN_WIDTH_PERSONALIZATION} otherwise.
	 */
	public int getProgrammaticColumnWidth(int viewColumn) {
		String columnName = getColumnName(viewColumn);
		Integer columnWidth = columnWidths.get(columnName);
		
		return (columnWidth != null) ? columnWidth.intValue() : NO_COLUMN_WIDTH_PERSONALIZATION;
	}
	
	/**
	 * Method to save user adjusted column widths to personal TL-Context. 
	 * @param viewColumn - the column as seen in visible order
	 * @param columnWidth - the width of the view column
	 */
	public void saveColumnWidth(int viewColumn, int columnWidth) {
		assert columnWidth > 0 : "Column width must be a positive integer!";
		String columnName = getColumnName(viewColumn);
		columnWidths.put(columnName, columnWidth);
		
		storeColumnWidths(getConfigKey());
	}

	private void storeColumnWidths(ConfigKey configKey) {
		String columnWidthsConfigKey = columnWidthConfigKey(configKey);
		if (columnWidthsConfigKey == null) {
			return;
		}
		List<List<? extends Object>> configurationContainer = new ArrayList<>();
		configurationContainer.add(Collections.singletonList(COLUMN_WIDTH_FORMAT_VERSION));
		configurationContainer.add(Collections.singletonList(columnWidths));

		getPersonalConfiguration().setJSONValue(columnWidthsConfigKey, configurationContainer);
	}
	
	/**
	 * This method to load user adjusted column widths from TL-Context
	 */
	private void loadColumnWidths(ConfigKey globalKey) {
		String columnWidthsConfigKey = columnWidthConfigKey(globalKey);
		if (columnWidthsConfigKey == null) {
			return;
		}
		PersonalConfiguration config = getPersonalConfiguration();
		Object configurationContainer = (List) config.getJSONValue(columnWidthsConfigKey);
		
		if(configurationContainer != null) {
			
			try {
				List<Double> formatContainer = (List<Double>) ((List<Object>) configurationContainer).get(0);
				// Check format version
				double configFormatVersion = formatContainer.get(0);
				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Restoring column width configuration from personal configuration. Column " +
						"width configuration format version '" + configFormatVersion +
						"' found.", TableViewModel.class);
				}
				if(configFormatVersion == COLUMN_WIDTH_FORMAT_VERSION) {
					List<Map<String, Integer>> pcData = (List<Map<String, Integer>>)((List<Object>)configurationContainer).get(1);
					columnWidths.putAll(pcData.get(0));
				}
			}
			
			// In case of invalid column configuration format
			catch(ClassCastException ex) {
				resetPersistentWidths(ex);
			}
			catch(IndexOutOfBoundsException ex) {
				resetPersistentWidths(ex);
			}
		}
	}

	private void resetPersistentWidths(Exception ex) {
		if (Logger.isDebugEnabled(TableViewModel.class)) {
			Logger.debug("Failed to restore column width configuration from personal configuration, " +
				"due to invalid configuration format. Current format version is '" +
				COLUMN_WIDTH_FORMAT_VERSION + "'. Resetting personal configuration.", ex,
				TableViewModel.class);
		}
	}

	/**
	 * Reset column widths of frozen table to configured values.
	 */
	public void resetColumnWidths() {
		columnWidths.clear();
		String columnWidthsConfigKey = columnWidthConfigKey(getConfigKey());
		if (columnWidthsConfigKey != null) {
			getPersonalConfiguration().setValue(columnWidthsConfigKey, null);
		}
		fireContentsChanged();
	}

	/**
	 * A method to load configuration from the personal configuration stored under the {@link #getConfigKey() default key}.
	 */
	public final void loadConfiguration() {
		ConfigKey globalKey = getConfigKey();
		loadColumnOrder(globalKey);
		loadColumnWidths(globalKey);
		loadPersonalFixColumnCount(globalKey);

		// Sidebar configuration must be loaded before filter configuration can be loaded.
		loadSidebarFilters(globalKey);
		loadFilter(globalKey);

		loadFilterOptions(globalKey);
		loadSortOrder(globalKey);
		_pagingModel.loadPageSize();
	}

	/**
	 * A method to load configuration from the personal configuration stored under the
	 * {@link #getConfigKey() given key}. Currently it loads
	 * <ol>
	 * <li>{@link #loadColumnOrder(ConfigKey) column order},</li>
	 * <li>{@link #loadColumnWidths(ConfigKey) user adjusted column widths},</li>
	 * <li>{@link #loadPersonalFixColumnCount(ConfigKey) user adjusted fixed column count},</li>
	 * <li>{@link #loadSidebarFilters(ConfigKey) sidebar filters},</li>
	 * <li>{@link #loadFilter(ConfigKey) filters},</li>
	 * <li>{@link #loadFilterOptions(ConfigKey) filter options},</li>
	 * <li>{@link #loadSortOrder(ConfigKey) sort order}, and</li>
	 * <li>{@link PagingModel#loadPageSize(ConfigKey) page size}</li>
	 * </ol>
	 * 
	 * @see #storeConfiguration(ConfigKey)
	 */
	public final void loadConfiguration(ConfigKey key) {
		loadColumnOrder(key);
		loadColumnWidths(key);
		loadPersonalFixColumnCount(key);

		// Sidebar configuration must be loaded before filter configuration can be loaded.
		loadSidebarFilters(key);
		loadFilter(key);

		loadFilterOptions(key);
		loadSortOrder(key);
		_pagingModel.loadPageSize(key);
		validate();
	}

	/**
	 * A method to store the current settings to the personal configuration stored under the
	 * {@link #getConfigKey() given key}.
	 * 
	 * @see #loadConfiguration(ConfigKey)
	 */
	public final void storeConfiguration(ConfigKey key) {
		saveColumnOrder(key);
		storeColumnWidths(key);
		savePersonalFixColumnCount(key);

		saveSidebarFilters(key);
		internalSaveFilter(key);
		if (hasFilterOptions()) {
			saveFilterOptions(key);
		}
		saveSortOrder(key);
		_pagingModel.savePageSize(key);
	}

	/**
	 * Removes the settings stored under the {@link #getConfigKey() given key}. The current settings
	 * remains untouched.
	 */
	public final void removeConfiguration(ConfigKey configKey) {
		if (configKey.get() != null) {
			PersonalConfiguration pc = getPersonalConfiguration();
			pc.setJSONValue(columnOrderConfigKey(configKey), null);
			pc.setJSONValue(columnWidthConfigKey(configKey), null);
			pc.setJSONValue(fixedColumnCountConfigKey(configKey), null);
			pc.setJSONValue(sidebarFiltersColumnKey(configKey), null);
			pc.setJSONValue(filterConfigKey(configKey), null);
			if (hasFilterOptions()) {
				pc.setJSONValue(filterOptionsConfigKey(configKey), null);
			}
			pc.setJSONValue(sortOrderConfigKey(configKey), null);
		} else {
			// all keys are null, nothing to delete
		}
		_pagingModel.removeStoredPageSize(configKey);
	}

	/**
	 * Resets all user specific settings (e.g. column order, sorted columns, active filters, etc.)
	 * to configured state.
	 */
	public void resetToConfiguration() {
		resetColumnOrder();
		resetColumnWidths();
		resetPersonalFixedColumnCount();
		resetAllFilters();
		resetFilterOptions();
		resetSortOrder();
		_pagingModel.resetPageSize();
	}

	/**
	 * Adjust filters so that a row that should be selected can be shown
	 * according to the currently active filters.
	 * <p>
	 * If the row is already {@link #containsRowObject(Object) visible} even though it does not
	 * match the filters, nothing happens.
	 * </p>
	 * <p>
	 * Adjusting the active filters according to a given row happens when a goto
	 * reaches a table. In that case, all filters that would prevent showing the
	 * selected object must be deactivated.
	 * </p>
	 */
	public void adjustFiltersForRow(Object rowObject) {
		Collection<Object> filterMasterRows = getNecessaryRows(rowObject);
		for (Object filterMasterRow : filterMasterRows) {
			adjustFiltersForRowInternal(filterMasterRow);
		}
		validate();
    }

	@SuppressWarnings("unchecked")
	private void adjustFiltersForRowInternal(Object rowObject) {
		if (getRowOfObject(rowObject) != -1) {
			return;
		}
		for (int applicationColumn = 0; applicationColumn < availableFilters.length; applicationColumn++) {
			TableFilter filter = getFilter(applicationColumn);
			if (isActive(filter)) {
				Object testValue = getRowMapping(applicationColumn).map(rowObject);
				adjustFilterFor(filter, testValue);
			}
		}
		if (isActive(_globalFilter)) {
			adjustFilterFor(_globalFilter, rowObject);
		}
		invalidateFilters();
    }

	private void adjustFilterFor(TableFilter filter, Object testValue) {
		filter.startFilterRevalidation();
		boolean filterDenies = !filter.accept(testValue);
		filter.stopFilterRevalidation();
		if (filterDenies) {
			filter.removeTableFilterListener(this);
			try {
				filter.reset();
			} finally {
				filter.addTableFilterListener(this);
			}
		}
	}

	private boolean isActive(TableFilter filter) {
		return filter != null && filter.isActive();
	}

	private boolean isVisible(TableFilter filter) {
		return filter != null && (filter.isVisible()
			|| _sidebarFilters.contains(getColumnNameOfFilterInThisModel(filter)));
	}

	private boolean isActiveOrVisible(TableFilter filter) {
		return isActive(filter) || isVisible(filter);
	}

	@Override
	public Object getWrappedModel() {
	    return this.getApplicationModel();
    }
	
	/**
	 * Currently opened slice.
	 */
	public Slice getOpenSlice() {
		return _openSlice;
	}

	/**
	 * Marks the given slice, set of table rows, as newly rendered.
	 * 
	 * @see #dropSlice(Slice)
	 */
	public void addSlice(Slice slice) {
		if (_openSlice == null) {
			_openSlice = slice;
		} else {
			if (_openSlice.getLastRow() + 1 == slice.getFirstRow()) {
				_openSlice = new Slice(_openSlice.getFirstRow(), slice.getLastRow());
			} else if (slice.getLastRow() + 1 == _openSlice.getFirstRow()) {
				_openSlice = new Slice(slice.getFirstRow(), _openSlice.getLastRow());
			}
		}
	}

	/**
	 * Unmark the given range of set of rows. The given set of rows has been removed from the
	 * clients UI.
	 * 
	 * @see #addSlice(Slice)
	 */
	public void dropSlice(Slice slice) {
		int firstRow = _openSlice.getFirstRow();
		int lastRow = _openSlice.getLastRow();

		if (firstRow <= slice.getLastRow() && lastRow >= slice.getFirstRow()) {
			int firstRowOfIntersection = Math.max(slice.getFirstRow(), firstRow);
			int lastRowOfIntersection = Math.min(slice.getLastRow(), lastRow);

			if (firstRowOfIntersection == firstRow) {
				if (lastRowOfIntersection == lastRow) {
					_openSlice = null;
				} else {
					_openSlice = new Slice(lastRowOfIntersection + 1, lastRow);
				}
			} else {
				_openSlice = new Slice(firstRow, firstRowOfIntersection - 1);
			}
		}
	}

	/**
	 * Mark all slices as removed from the UI.
	 */
	public void clearSlices() {
		_openSlice = null;
		_sliceRequests = new ArrayList<>();
	}

	/**
	 * The first visible row.
	 * 
	 * @see #checkDisplayedRows(int, int)
	 */
	public int getFirstDisplayedRow() {
		int result;
		int firstRowOfRequestSlices = getFirstRowOfRequestSlices();
		if (isAnySliceOpen()) {
			if (isNewSliceRequested() && firstRowOfRequestSlices < getFirstRowOfOpenSlices()) {
				result = firstRowOfRequestSlices;
			} else {
				result = getFirstRowOfOpenSlices();
			}
		} else {
			if (isNewSliceRequested()) {
				result = firstRowOfRequestSlices;
			} else {
				result = _pagingModel.getFirstRowOnCurrentPage();
			}
		}
		return result;
	}

	private int getFirstRowOfOpenSlices() {
		return _openSlice.getFirstRow();
	}

	/**
	 * Checks that the visible rows reported from client-side match the server-side model.
	 * 
	 * @param firstDisplayedRow
	 *        See {@link #getFirstDisplayedRow()}.
	 * @param lastDisplayedRow
	 *        See {@link #getLastDisplayedRow()}.
	 */
	public void checkDisplayedRows(int firstDisplayedRow, int lastDisplayedRow) {
		int expectedFirstDisplayedRow = getFirstDisplayedRow();
		int expectedLastDisplayedRow = getLastDisplayedRow();
		if (expectedFirstDisplayedRow != firstDisplayedRow || expectedLastDisplayedRow != lastDisplayedRow) {
			Logger.warn(
				"Mismatch of displayed rows:" +
					" Expected range: (" + expectedFirstDisplayedRow + ", " + expectedLastDisplayedRow + ")" +
					", reported: (" + firstDisplayedRow + ", " + lastDisplayedRow + ").", TableViewModel.class);
		}
	}

	/**
	 * The last visible row (inclusive).
	 * 
	 * @see #checkDisplayedRows(int, int)
	 */
	public int getLastDisplayedRow() {
		int result;
		int lastRowOfRequestSlices = getLastRowOfRequestSlices();
		if (isAnySliceOpen()) {
			if (isNewSliceRequested() && lastRowOfRequestSlices > getLastRowOfOpenSlices()) {
				result = lastRowOfRequestSlices;
			} else {
				result = getLastRowOfOpenSlices();
			}
		} else {
			if (isNewSliceRequested()) {
				result = lastRowOfRequestSlices;
			} else {
				result = _pagingModel.getLastRowOnCurrentPage();
			}
		}
		return result;
	}

	private boolean isAnySliceOpen() {
		return _openSlice != null;
	}

	private int getLastRowOfOpenSlices() {
		return _openSlice.getLastRow();
	}

	public boolean isNewSliceRequested() {
		return !CollectionUtilShared.isEmptyOrNull(_sliceRequests);
	}

	public void pushSliceRequest(Slice sliceRequest) {
		_sliceRequests.add(sliceRequest);
	}

	public List<Slice> getSliceRequest() {
		return ImmutableList.copyOf(_sliceRequests);
	}

	/**
	 * Removes the given request from the collection of all current slice requests.
	 */
	public boolean removeSliceRequest(Slice sliceRequest) {
		return _sliceRequests.remove(sliceRequest);
	}

	private int getFirstRowOfRequestSlices() {
		int min = Integer.MAX_VALUE;

		for (int i = 0; i < _sliceRequests.size(); i++) {
			int firstRow = _sliceRequests.get(i).getFirstRow();

			if (firstRow < min) {
				min = firstRow;
			}

		}
		return min;
	}

	private int getLastRowOfRequestSlices() {
		int max = Integer.MIN_VALUE;

		for (int i = 0; i < _sliceRequests.size(); i++) {
			int lastRow = _sliceRequests.get(i).getLastRow();

			if (lastRow > max) {
				max = lastRow;
			}

		}
		return max;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void handleTableFilterEvent(TableFilterEvent event) {
		TableFilter filter = event.getSource();

		switch (event.getEventType()) {
			case DEACTIVATION:
			case CONFIGURATION_UPDATE: {
				saveFilter();
			}
			//$FALL-THROUGH$
			case RE_APPLIANCE: {
				invalidateFilters();
				break;
			}

			case ERROR: {
				Logger.warn("An error occured processing table filter '" + getColumnNameOfFilterInThisModel(filter)
					+ "' of table model '" + getConfigKey().get() + "'.", TableViewModel.class);
				
				break;
			}

			default: {
				Logger.warn("The table filter '" + filter + "' sent an unknown event!", TableViewModel.class);
			}
		}
	}

	/**
	 * all filters, that can be visible at sidebar.
	 */
	public List<String> getAvailableSidebarFilters() {
		List<String> availableSidebarFilters = new ArrayList<>();
		for (Column column : getHeader().getAllElementaryColumns()) {
			if (isUsableSidebarFilter(column)) {
				availableSidebarFilters.add(column.getName());
			}
		}
		return availableSidebarFilters;
	}

	private boolean isUsableSidebarFilter(Column column) {
		return !column.isExcluded() && (getFilter(column.getName()) != null)
			&& !column.getConfig().hasExcludedFilterFromSidebar();
	}

	/**
	 * currently at sidebar visible filters.
	 */
	public List<String> getSidebarFilters() {
		return Collections.unmodifiableList(_sidebarFilters);
	}

	/**
	 * Marks the column filters of the given column names as visible at sidebar (so their match
	 * counts, etc. will be updated at filter revalidations). Formerly
	 */
	public void setSidebarFilters(List<String> columnNames) {
		// Clear sidebar filter list before deregistering them from table row filter, because during
		// deregister operation all filters will be kept, that are in sidebar filter list.
		List<String> currentlyOpenSidebarFilters = new ArrayList<>(_sidebarFilters);
		_sidebarFilters.clear();
		removeFromOpenFilters(currentlyOpenSidebarFilters);
		addToOpenFilters(columnNames);
		_sidebarFilters.addAll(columnNames);
		saveSidebarFilters(getConfigKey());
	}

	/**
	 * Marks the column filters of the given column names as visible at GUI (so their match counts,
	 * etc. will be updated at filter revalidations).
	 * 
	 * @see #removeFromOpenFilters(Collection)
	 */
	public void addToOpenFilters(Collection<String> columnNames) {
		for (String columnName : columnNames) {
			TableFilter columnFilter = getFilter(columnName);
			TableRowFilter tableRowFilter = applicationModel.getFilter();

			if (!tableRowFilter.containsFilter(columnFilter)) {
				tableRowFilter.addVisibleFilter(createColumnFilterHolder(columnName, columnFilter));
			}

		}
		List<String> visibleColumnNames = new LinkedList<>(columnNames);
		addActiveOrVisibleFilterColumns(visibleColumnNames);
		internalRevalidateMatchCount(visibleColumnNames);
	}

	/**
	 * Removes visibility markers from filters of given column names.
	 * 
	 * @see #addToOpenFilters(Collection)
	 */
	public void removeFromOpenFilters(Collection<String> columnNames) {
		for (String columnName : columnNames) {
			TableFilter columnFilter = getFilter(columnName);
			TableRowFilter tableRowFilter = applicationModel.getFilter();
			if (!isActive(columnFilter) && tableRowFilter.containsFilter(columnFilter)
				&& !_sidebarFilters.contains(columnName)) {
				tableRowFilter.removeVisibleFilter(columnFilter);
			}
		}
	}

	/**
	 * Triggers filtering of the table, using current active table filters.
	 */
	public void reapplyFilters() {
		internalInvalidateFilters();
	}

	/**
	 * Sorts the the table, using current active column comparators.
	 */
	public void reapplyOrder() {
		internalInvalidateOrder();
	}

	private String getColumnNameOfFilterInThisModel(TableFilter filter) {
		for (int filterColumn = 0; filterColumn < availableFilters.length; filterColumn++) {
			if (filter.equals(availableFilters[filterColumn])) {
				return applicationModel.getColumnName(filterColumn);
			}
		}

		if (filter.equals(_globalFilter)) {
			return GLOBAL_TABLE_FILTER_ID;
		}

		return "<no such column>";
	}

	private void fireColumnsChanged(List<String> oldColumns, List<String> newColumns) {
		fireTableColumnEvent(oldColumns, newColumns);
		fireContentsChanged();
	}

	private void fireFiltersChanged() {
		fireTableModelEvent(0, 0, TableModelEvent.COLUMN_FILTER_UPDATE);
		fireContentsChanged();
	}

	private void fireSortOrderChanged() {
		fireTableModelEvent(0, 0, TableModelEvent.COLUMN_SORT_UPDATE);
		fireContentsChanged();
	}

	private void fireContentsChanged() {
		fireTableModelEvent(0, 0, TableModelEvent.INVALIDATE);
	}

	public TableFilter[] getColumnFilters() {
		return availableFilters;
	}

	/**
	 * true, if a global filter is defined, false otherwise
	 * 
	 * @see #getGlobalFilter()
	 */
	public boolean hasGlobalFilter() {
		return _globalFilter != null;
	}

	/**
	 * the {@link TableFilter}, which allows filter operations based on multiple columns of
	 *         a row.
	 */
	public TableFilter getGlobalFilter() {
		return _globalFilter;
	}

	/**
	 * Is this guaranteed to be a <em>finite</em> tree?
	 * <p>
	 * Has to return false, if it is unknown whether the tree is finite. Returns false, if this not
	 * a tree.
	 * </p>
	 */
	public boolean isFiniteTree() {
		if (!isTree()) {
			return false;
		}
		AbstractTreeTableModel<?>.TreeTable treeTable = (AbstractTreeTableModel<?>.TreeTable) getApplicationModel();
		return treeTable.getTreeModel().isFinite();
	}

	/**
	 * true, if this table is based on a tree, false otherwise.
	 */
	public boolean isTree() {
		return getApplicationModel() instanceof TreeTable;
	}

	/**
	 * true, if the underlying application table model is a {@link TreeTable}, that holds an
	 *         infinite tree model, false otherwise.
	 */
	private boolean isInFiniteTree() {
		if (!isTree()) {
			return false;
		}
		AbstractTreeTableModel<?>.TreeTable treeTable = (AbstractTreeTableModel<?>.TreeTable) getApplicationModel();
		return !treeTable.getTreeModel().isFinite();
	}

	/**
	 * <code>true</code>, if filter options are available for this table
	 */
	public boolean hasFilterOptions() {
		return getApplicationModel() instanceof TreeTable;
	}

	/**
	 * Sets the include parents option, if the table has a tree based model.
	 */
	public void setFilterIncludeParents(boolean filterIncludeParents) {
		setFilterOptions(filterIncludeParents, isFilterIncludeChildren());
	}

	/**
	 * Sets the include children option, if the table has a tree based model.
	 */
	public void setFilterIncludeChildren(boolean filterIncludeChildren) {
		setFilterOptions(isFilterIncludeParents(), filterIncludeChildren);
	}

	/**
	 * {@link #setFilterIncludeParents} and {@link #setFilterIncludeChildren} at once.
	 */
	public void setFilterOptions(boolean filterIncludeParents, boolean filterIncludeChildren) {
		if (hasFilterOptions()) {
			TreeTable treeTable = ((TreeTable) getApplicationModel());
			boolean changed = treeTable.isFilterIncludeParents() != filterIncludeParents
				|| treeTable.isFilterIncludeChildren() != filterIncludeChildren;
			if (changed) {
				treeTable.internalSetFilterOptions(filterIncludeParents, filterIncludeChildren);
				saveFilterOptions(getConfigKey());
				invalidateFilters();
			}
		}
	}

	/**
	 * Checks whether the include parents option is set, if the table has a tree based model.
	 */
	public boolean isFilterIncludeParents() {
		return hasFilterOptions() && ((TreeTable) getApplicationModel()).isFilterIncludeParents();
	}

	/**
	 * Checks whether the include children option is set, if the table has a tree based model.
	 */
	public boolean isFilterIncludeChildren() {
		return hasFilterOptions() && ((TreeTable) getApplicationModel()).isFilterIncludeChildren();
	}

	private void saveFilterOptions(ConfigKey globalConfig) {
		String filterOptionsKey = filterOptionsConfigKey(globalConfig);
		if (filterOptionsKey == null) {
			return;
		}
		TreeTable treeTable = ((TreeTable) getApplicationModel());
		PersonalConfiguration config = getPersonalConfiguration();
		config.setJSONValue(filterOptionsKey, CollectionUtil.createList(
			Boolean.valueOf(treeTable.isFilterIncludeParents()), Boolean.valueOf(treeTable.isFilterIncludeChildren())));
	}

	private void loadFilterOptions(ConfigKey globalKey) {
		if (hasFilterOptions()) {
			String filterOptionsKey = filterOptionsConfigKey(globalKey);
			if (filterOptionsKey == null) {
				return;
			}
			TreeTable treeTable = ((TreeTable) getApplicationModel());
			PersonalConfiguration config = getPersonalConfiguration();
			Object storedSettings = config.getJSONValue(filterOptionsKey);
			if (storedSettings instanceof List) {
				List<?> settings = (List) storedSettings;
				treeTable.internalSetFilterOptions(
					Utils.getbooleanValue(settings.get(0)), Utils.getbooleanValue(settings.get(1)));
			}
		}
	}

	/**
	 * Resets the filter options of tree tables to configured values (show parents, show children).
	 */
	@SuppressWarnings("rawtypes")
	private void resetFilterOptions() {
		if (hasFilterOptions()) {
			TreeTable treeTable = ((TreeTable) getApplicationModel());
			treeTable.internalSetFilterOptions(getTableConfiguration().getFilterDisplayParents(),
				getTableConfiguration().getFilterDisplayChildren());
			String filterOptionsKey = filterOptionsConfigKey(getConfigKey());
			if (filterOptionsKey == null) {
				return;
			}
			getPersonalConfiguration().setValue(filterOptionsKey, null);
		}
	}

	/**
	 * Getter for the {@link PersonalConfiguration}.
	 */
	protected PersonalConfiguration getPersonalConfiguration() {
		return PersonalConfiguration.getPersonalConfiguration();
	}

	/**
	 * Sets a {@link ColumnChangeVetoListener} for this {@link TableViewModel}.
	 */
	public void setColumnChangeVetoListener(ColumnChangeVetoListener vetoListener) {
		this._vetoListener = vetoListener;
	}

	@Override
	public void validate(DisplayContext context) {
		validate();
	}

	@Override
	public AccessContext prepareRows(Collection<?> accessedRows, List<String> accessedColumns) {
		return applicationModel.prepareRows(accessedRows, accessedColumns);
	}

	@Override
	public Object getRowObject(int row) {
		return applicationModel.getRowObject(row);
	}

	@Override
	public int getRowOfObject(Object rowObject) {
		return applicationModel.getRowOfObject(rowObject);
	}

	@Override
	public int findNearestDisplayedRow(Object rowObject) {
		return applicationModel.findNearestDisplayedRow(rowObject);
	}

	@Override
	public Collection<Object> getNecessaryRows(Object rowObject) {
		return applicationModel.getNecessaryRows(rowObject);
	}

	@Override
	public TableRowFilter getFilter() {
		return applicationModel.getFilter();
	}

	@Override
	public void setFilter(TableRowFilter filter, Comparator order) {
		applicationModel.setFilter(filter, order);
	}

	@Override
	public void revalidateFilterMatchCount() {
		ArrayList<String> filterColumns = new ArrayList<>();
		addActiveOrVisibleFilterColumns(filterColumns);
		internalRevalidateMatchCount(filterColumns);
	}

	private void internalRevalidateMatchCount(List<String> accessedFilterColumns) {
		AccessContext context =
			applicationModel.prepareRows(applicationModel.getAllRows(), accessedFilterColumns);
		try {
			applicationModel.revalidateFilterMatchCount();
		} catch (Throwable throwable) {
			InfoService.logError(I18NConstants.ERROR_TABLE_FILTERING,
				"Cannot update filter match count of table '" + getConfigKey().get() + "' for columns "
					+ getUsedFilterColumnNames()
					+ ".",
				throwable, TableViewModel.class);
		}
		context.close();
	}

	@Override
	public Comparator getOrder() {
		return applicationModel.getOrder();
	}

	@Override
	public void setOrder(Comparator order) {
		applicationModel.setOrder(order);
	}

	@Override
	public boolean containsRowObject(Object anObject) {
		return applicationModel.containsRowObject(anObject);
	}

	@Override
	public Object getValueAt(Object rowObject, String columnName) {
		return applicationModel.getValueAt(rowObject, columnName);
	}

	/**
	 * mapped value from {@link #getValueAt(Object, String)} to value specified by
	 *         {@link ColumnConfiguration#getSortKeyProvider()}
	 */
	public Object getMappedValueAt(Object rowObject, String columnName) {
		return getRowMapping(columnName).map(rowObject);
	}

	@Override
	public List getDisplayedRows() {
		validate();
		return applicationModel.getDisplayedRows();
	}

	@Override
	public Collection getAllRows() {
		return applicationModel.getAllRows();
	}

	@Override
	public boolean isFilterCountingEnabled() {
		return applicationModel.isFilterCountingEnabled();
	}

}
