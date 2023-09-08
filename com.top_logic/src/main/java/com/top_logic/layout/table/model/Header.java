/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.layout.security.SecurityAddingTableConfiguration;
import com.top_logic.layout.table.control.TableControl;

/**
 * Description of the table header.
 * 
 * @see Column
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Header {

	private final TableConfiguration _tableConfiguration;

	private List<Column> _groups;

	private List<Column> _columns;

	private int _headerLines;

	private Map<String, Column> _allColumnsByName;

	private final OnColumnChange _onChange;

	/**
	 * Creates an unobserved {@link Header} from a {@link TableConfiguration}.
	 * 
	 * @see #Header(TableConfiguration, List, boolean, OnColumnChange)
	 */
	public Header(TableConfiguration tableConfiguration, List<String> columnNames, boolean allVisible) {
		this(tableConfiguration, columnNames, allVisible, null);
	}
	
	/**
	 * Creates a {@link Header} from a {@link TableConfiguration}.
	 * 
	 * @param columnNames
	 *        The order of visible columns.
	 * 
	 * @param allVisible
	 *        Whether all columns should be marked as visible independently of the
	 *        {@link ColumnConfiguration#getVisibility() visibility} in the
	 *        {@link ColumnConfiguration}.
	 * @param onChange
	 *        Listener for column changes.
	 */
	public Header(TableConfiguration tableConfiguration, List<String> columnNames, boolean allVisible,
			OnColumnChange onChange) {
		_tableConfiguration = copyTableConfiguration(tableConfiguration);
		_onChange = nonNull(onChange);

		init(columnNames, allVisible);
	}

	private TableConfiguration copyTableConfiguration(TableConfiguration tableConfiguration) {
		tableConfiguration = tableConfiguration.copy();
		TableConfigurationFactory.apply(SecurityAddingTableConfiguration.INSTANCE, tableConfiguration);
		return tableConfiguration;
	}

	private static OnColumnChange nonNull(OnColumnChange onChange) {
		return onChange == null ? OnColumnChange.IGNORE_CHANGE : onChange;
	}

	/**
	 * The configuration of the owning table.
	 */
	public TableConfiguration getTableConfiguration() {
		return _tableConfiguration;
	}

	/**
	 * The number of header lines necessary to display all visible column groups.
	 */
	public int getHeaderLines() {
		revalidate();
		return _headerLines;
	}

	/**
	 * All top-level column groups.
	 */
	public Collection<Column> getGroups() {
		revalidate();
		return _groups;
	}

	/**
	 * The columns being displayed.
	 */
	public List<Column> getColumns() {
		revalidate();
		return _columns;
	}

	/**
	 * Updates the visibility and order of the given elementary columns.
	 */
	public void setVisibleColumns(List<String> newColumnNames) {
		if (newColumnNames == null || newColumnNames.isEmpty()) {
			throw new IllegalArgumentException("Column list must not be null or empty.");
		}

		applyVisibility(newColumnNames);
		applyOrder(newColumnNames);
		invalidate();
	}

	private void applyVisibility(List<String> newColumnNames) {
		hideAll(_groups);
		for (String name : newColumnNames) {
			Column column = _allColumnsByName.get(name);
			assert column != null : "No such column: " + name;
			column.setVisible(true);
		}
	}

	private void init(List<String> visibleColumnOrder, boolean allVisible) {
		Collection<? extends ColumnConfiguration> groupConfigurations = _tableConfiguration.getDeclaredColumns();
		int groupCount = groupConfigurations.size();

		// Make columns for all column configurations.
		int allDeclaredColumnsCount = 0;
		_groups = new ArrayList<>(groupCount);

		TableConfiguration defaultTable = TableConfigurationFactory.table().copy();
		for (ColumnConfiguration config : defaultTable.getDeclaredColumns()) {
			ColumnConfiguration customConfig = _tableConfiguration.getDeclaredColumn(config.getName());
			if (customConfig != null) {
				// Default column has been modified and will be added explicitly later on.
				continue;
			}
			Column column = new Column(this, config.getName(), config);
			_groups.add(column);
			allDeclaredColumnsCount += column.getSize();
		}
		for (ColumnConfiguration config : groupConfigurations) {
			Column column = new Column(this, config.getName(), config);
			_groups.add(column);
			allDeclaredColumnsCount += column.getSize();
		}

		_allColumnsByName = createColumnIndex(allDeclaredColumnsCount, _groups);

		if (visibleColumnOrder == null) {
			if (allVisible) {
				showAll(_groups);
			}
		} else {
			// Make all columns visible that are in the list of columns to be displayed.
			if (allVisible) {
				showAll(_groups);
			} else {
				hideAll(_groups);
			}
			for (String name : visibleColumnOrder) {
				Column column = _allColumnsByName.get(name);
				if (column == null) {
					column = new Column(this, name, _tableConfiguration.getDefaultColumn());

					_groups.add(column);
					updateIndex(_allColumnsByName, column);
				}
				if (allVisible) {
					column.setVisible(true);
				} else {
					column.applyVisibility(column.getConfig().getVisibility());
				}
			}

			// Order columns according the requested order.
			applyOrder(ensureSelectColumn(visibleColumnOrder));
		}

		invalidate();
	}

	private List<String> ensureSelectColumn(List<String> visibleColumnOrder) {
		List<String> enrichedColumnOrder = visibleColumnOrder;
		if (!visibleColumnOrder.contains(TableControl.SELECT_COLUMN_NAME)) {
			enrichedColumnOrder = new ArrayList<>(visibleColumnOrder);
			enrichedColumnOrder.add(0, TableControl.SELECT_COLUMN_NAME);
		}
		return enrichedColumnOrder;
	}

	void invalidate() {
		_columns = null;
		
		_onChange.handleColumnsChanged();
	}

	private void revalidate() {
		if (isInvalid()) {
			initVisibleColumns();
		}
	}

	private boolean isInvalid() {
		return _columns == null;
	}

	private void initVisibleColumns() {
		int headerLines = 1;
		int index = 0;
		for (Column group : _groups) {
			index = group.update(index);
			headerLines = Math.max(headerLines, group.getDepth());
		}

		// Create a flat list of elementary columns.
		List<Column> columns = new ArrayList<>();
		addElementaryColumns(columns, _groups);

		_columns = columns;
		_headerLines = headerLines;
	}

	private void applyOrder(List<String> visibleColumnOrder) {
		Comparator<Column> columnComparator = createColumnComparator(visibleColumnOrder);
		for (Column group : _groups) {
			group.sort(columnComparator);
		}
		Collections.sort(_groups, columnComparator);
	}

	private void showAll(Collection<Column> columns) {
		setAllVisible(columns, true);
	}

	private void hideAll(Collection<Column> columns) {
		setAllVisible(columns, false);
	}

	private void setAllVisible(Collection<Column> columns, boolean visible) {
		for (Column column : columns) {
			column.setVisible(visible);
			setAllVisible(column.getParts(), visible);
		}
	}

	private static void addElementaryColumns(List<Column> buffer, List<Column> groups) {
		for (Column column : groups) {
			if (!column.isVisible()) {
				continue;
			}

			List<Column> parts = column.getParts();
			if (parts.isEmpty()) {
				buffer.add(column);
			} else {
				addElementaryColumns(buffer, parts);
			}
		}
	}

	private static Comparator<Column> createColumnComparator(List<String> newColumnNames) {
		return createColumnComparator(createNameIndex(newColumnNames));
	}

	private static Comparator<Column> createColumnComparator(final Map<String, Integer> positionByName) {
		return new Comparator<>() {
			@Override
			public int compare(Column c1, Column c2) {
				Integer p1 = positionByName.get(representative(c1).getName());
				Integer p2 = positionByName.get(representative(c2).getName());
				if (p1 == null) {
					if (p2 == null) {
						return 0;
					}

					return 1;
				}
				if (p2 == null) {
					return -1;
				}

				return compare(p1, p2);
			}

			private Column representative(Column column) {
				if (column.getParts().isEmpty()) {
					return column;
				}

				return representative(column.getParts().get(0));
			}

			private int compare(int p1, int p2) {
				return (p1 < p2 ? -1 : (p1 == p2 ? 0 : 1));
			}
		};
	}

	private static Map<String, Integer> createNameIndex(List<String> names) {
		HashMap<String, Integer> index = MapUtil.newMap(names.size());
		Collection<? extends ColumnConfiguration> defaultColumns =
			TableConfiguration.defaultTable().getDeclaredColumns();
		int pos = 0;
		for (ColumnConfiguration config : defaultColumns) {
			index.put(config.getName(), pos++);
		}
		for (int n = 0, cnt = names.size(); n < cnt; n++) {
			index.put(names.get(n), pos++);
		}
		return index;
	}

	private static HashMap<String, Column> createColumnIndex(int expectedColumnCount, List<Column> groups) {
		HashMap<String, Column> index = MapUtil.newMap(expectedColumnCount);
		updateIndex(index, groups);
		return index;
	}

	private static void updateIndex(Map<String, Column> index, List<Column> columns) {
		for (Column column : columns) {
			updateIndex(index, column);
		}
	}

	private static void updateIndex(Map<String, Column> index, Column column) {
		index.put(column.getName(), column);
		updateIndex(index, column.getParts());
	}

	/**
	 * The names of the visible elementary columns.
	 */
	public List<String> getColumnNames() {
		return getColumns().stream().map(c -> c.getName()).collect(Collectors.toList());
	}

	/**
	 * The column or column group with the given name.
	 */
	public Column getColumn(String name) {
		revalidate();
		return _allColumnsByName.get(name);
	}

	/**
	 * Whether the column label is displayed in the given group line.
	 * 
	 * <p>
	 * Columns have only a label this the group line that matches the {@link Column#getDepth()} of
	 * the corresponding column.
	 * </p>
	 * 
	 * @param column
	 *        The column to check.
	 * @param line
	 *        the actual group header.
	 * @return Whether the given column has its header in the given group header line.
	 */
	public boolean hasLabel(Column column, int line) {
		return line + column.getDepth() == getHeaderLines();
	}

	/**
	 * All elementary non-{@link Column#isExcluded() excluded} {@link Column}s independently of
	 * their {@link Column#isVisible() display state}.
	 */
	public List<Column> getAllElementaryColumns() {
		ArrayList<Column> result = new ArrayList<>();
		fillElementaryColumns(result, getGroups());
		return result;
	}

	/**
	 * Elementary non-{@link Column#isExcluded() excluded} {@link Column}s independently of their
	 * {@link Column#isVisible() display state}, below given column group.
	 */
	public List<Column> getElementaryColumnsOfGroup(Column group) {
		ArrayList<Column> result = new ArrayList<>();
		fillElementaryColumns(result, Collections.singletonList(group));
		return result;
	}

	private static void fillElementaryColumns(List<Column> buffer, Collection<Column> groups) {
		for (Column column : groups) {
			if (column.isExcluded()) {
				continue;
			}
			List<Column> parts = column.getParts();
			if (parts.isEmpty()) {
				buffer.add(column);
			} else {
				fillElementaryColumns(buffer, parts);
			}
		}
	}

}
