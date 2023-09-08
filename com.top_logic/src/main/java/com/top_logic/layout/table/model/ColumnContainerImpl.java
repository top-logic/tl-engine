/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ColumnContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ColumnContainerImpl<C extends ColumnConfiguration> extends AbstractColumnContainer<C> {

	private Map<String, C> _columns;

	private C _defaultColumn;

	public ColumnContainerImpl(C defaultColumn) {
		_defaultColumn = defaultColumn;
	}

	@Override
	public void freeze() {
		super.freeze();

		C defaultColumn = getDefaultColumn();
		if (defaultColumn != null) {
			defaultColumn.freeze();
		}
		for (C column : columns().values()) {
			column.freeze();
		}
	}

	@Override
	public void addColumn(C columnConfiguration) {
		checkFrozen();
		mkColumns().put(columnConfiguration.getName(), columnConfiguration);
	}

	@Override
	public C removeColumn(String columnName) {
		checkFrozen();
		return mkColumns().remove(columnName);
	}

	@Override
	public C getDefaultColumn() {
		return _defaultColumn;
	}

	@Override
	public final C getDeclaredColumn(String aName) {
		return columns().get(aName);
	}

	@Override
	public Collection<? extends C> getDeclaredColumns() {
		return Collections.unmodifiableCollection(columns().values());
	}

	@Override
	public Map<String, ColumnConfiguration> createColumnIndex() {
		Map<String, ColumnConfiguration> buffer = new HashMap<>();
		putColumns(buffer, columns().values());
		return buffer;
	}

	private static void putColumns(Map<String, ColumnConfiguration> buffer,
			Collection<? extends ColumnConfiguration> columns) {
		for (ColumnConfiguration column : columns) {
			buffer.put(column.getName(), column);
			putColumns(buffer, column.getDeclaredColumns());
		}
	}

	@Override
	public List<ColumnConfiguration> getElementaryColumns() {
		List<ColumnConfiguration> buffer = new ArrayList<>();
		addElementaryColumns(buffer, columns().values());
		return buffer;
	}

	private static void addElementaryColumns(Collection<ColumnConfiguration> buffer,
			Collection<? extends ColumnConfiguration> columns) {
		for (ColumnConfiguration column : columns) {
			Collection<? extends ColumnConfiguration> parts = column.getDeclaredColumns();
			if (parts.isEmpty()) {
				buffer.add(column);
			} else {
				addElementaryColumns(buffer, parts);
			}
		}
	}

	@Override
	public List<String> getElementaryColumnNames() {
		ArrayList<String> buffer = new ArrayList<>();
		addElementaryColumnNames(buffer, columns().values());
		return buffer;
	}

	private static void addElementaryColumnNames(Collection<String> buffer,
			Collection<? extends ColumnConfiguration> columns) {
		for (ColumnConfiguration column : columns) {
			Collection<? extends ColumnConfiguration> parts = column.getDeclaredColumns();
			if (parts.isEmpty()) {
				buffer.add(column.getName());
			} else {
				addElementaryColumnNames(buffer, parts);
			}
		}
	}

	private Map<String, C> columns() {
		return _columns == null ? Collections.emptyMap() : _columns;
	}

	private Map<String, C> mkColumns() {
		if (_columns == null) {
			_columns = new LinkedHashMap<>();
		}
		return _columns;
	}
}
