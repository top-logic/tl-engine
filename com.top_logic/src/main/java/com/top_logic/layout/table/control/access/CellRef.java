/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control.access;

import java.io.IOException;

import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Reference to a rendered table cell to be accessed through {@link WithProperties} by a
 * {@link TemplateExpression rendering template}.
 * 
 * @see ColumsCollectionRef
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CellRef implements WithProperties {

	/** @see #toggleSortAction() */
	public static final String TOGGLE_SORT_ACTION = "toggleSortAction";

	/** @see #showFilterAction() */
	public static final String SHOW_FILTER_ACTION = "showFilterAction";

	/** @see #canSort() */
	public static final String CAN_SORT = "canSort";

	/** @see #canFilter() */
	public static final String CAN_FILTER = "canFilter";

	/** @see #sortFilterButtonId() */
	public static final String SORT_FILTER_BUTTON_ID = "sortFilterButtonId";

	/** @see #ascending() */
	public static final String ASCENDING = "ascending";

	/** @see #sorted() */
	public static final String SORTED = "sorted";

	/** @see #value() */
	public static final String VALUE = "value";

	/** @see #label() */
	public static final String LABEL = "label";

	/** @see #columnIndex() */
	public static final String COLUMN_INDEX = "columnIndex";

	/** @see #columnName() */
	public static final String COLUMN_NAME = "columnName";

	private TableControl _table;

	private Object _rowObject;

	private String _columnName;

	/**
	 * Updates the {@link ColumsCollectionRef} to a new state.
	 *
	 * @param table
	 *        The {@link TableControl} being rendered.
	 * @param rowObject
	 *        The row object of the currently rendered row.
	 * @param columnName
	 *        The name of the currently rendered column.
	 * @return This instance for call chaining.
	 */
	public CellRef init(TableControl table, Object rowObject, String columnName) {
		_table = table;
		_rowObject = rowObject;
		_columnName = columnName;
		return this;
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case COLUMN_NAME:
				return columnName();
			case COLUMN_INDEX:
				return columnIndex();
			case LABEL:
				return label();
			case VALUE:
				return value();
			case SORTED:
				return sorted();
			case ASCENDING:
				return ascending();
			case SORT_FILTER_BUTTON_ID:
				return sortFilterButtonId();
			case CAN_FILTER:
				return canFilter();
			case CAN_SORT:
				return canSort();
			case SHOW_FILTER_ACTION:
				return showFilterAction();
			case TOGGLE_SORT_ACTION:
				return toggleSortAction();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

	/**
	 * Technical name of the column.
	 */
	public String columnName() {
		return _columnName;
	}

	/**
	 * The index of the column.
	 */
	public int columnIndex() {
		return getColumn().getIndex();
	}

	/**
	 * The label of the column.
	 */
	public Object label() {
		return Column.getColumnLabel(getTableConfiguration(), getColumnConfig(), columnName());
	}

	/**
	 * The value of the cell.
	 * 
	 * <p>
	 * This property can only be accessed in the content area of the table (the table rows).
	 * </p>
	 */
	public Object value() {
		if (isHeaderCell()) {
			throw new IllegalStateException(
				"Property '" + VALUE + "' cannot be accessed in the table header.");
		}
		return getViewModel().getValueAt(_rowObject, columnName());
	}

	/**
	 * Whether the column can be sorted.
	 */
	public boolean canSort() {
		return getViewModel().isSortable(columnIndex());
	}

	/**
	 * Whether the column is sorted.
	 */
	public boolean sorted() {
		return getViewModel().isSorted(columnIndex());
	}

	/**
	 * Whether the column is sorted in ascending order.
	 */
	public boolean ascending() {
		return getViewModel().getAscending(columnIndex());
	}

	/**
	 * Script that toggles the sort direction of the column.
	 */
	public String toggleSortAction() {
		return "dispatchControlCommand({" +
			"controlCommand: \"" + TableControl.TABLE_SORT_COMMAND + "\"," +
			"controlID: \"" + _table.getID() + "\"," +
			"column: " + columnIndex() +
			"});";
	}

	/**
	 * Whether the column can be filtered.
	 */
	public boolean canFilter() {
		return getViewModel().canFilter(columnIndex());
	}

	/**
	 * The client-side ID of the filter opening button.
	 */
	public String sortFilterButtonId() {
		return _table.getColumnActivateElementID(columnName());
	}

	/**
	 * The script that opens the filter dialog.
	 */
	public DisplayValue showFilterAction() {
		String columnName = columnName();
		TableControl table = _table;
		return new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				table.appendOpenFilterAction(out, columnName);
			}
		};
	}

	private TableViewModel getViewModel() {
		return _table.getViewModel();
	}

	private boolean isHeaderCell() {
		return _rowObject == null;
	}

	private ColumnConfiguration getColumnConfig() {
		return getColumn().getConfig();
	}

	private Column getColumn() {
		return getHeader().getColumn(columnName());
	}

	private TableConfiguration getTableConfiguration() {
		return getHeader().getTableConfiguration();
	}

	private Header getHeader() {
		return getViewModel().getHeader();
	}

}
