/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control.access;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.ConfiguredTableRenderer;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Reference to a rendered table row to be accessed through {@link WithProperties} by a
 * {@link TemplateExpression rendering template}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RowRef implements WithProperties {

	/** @see #id() */
	public static final String ID = "id";

	/** @see #cols() */
	public static final String COLS = "cols";

	/** @see #selected() */
	public static final String SELECTED = "selected";

	/** @see #selectAction() */
	public static final String SELECT_ACTION = "selectAction";

	private final ColumsCollectionRef _colsView = new ColumsCollectionRef();

	private TableControl _table;

	private Object _rowObject;

	/**
	 * Updates the {@link ColumsCollectionRef} to a new state.
	 *
	 * @param table
	 *        The {@link TableControl} being rendered.
	 * @param rowObject
	 *        The row object of the currently rendered row.
	 * @return This instance for call chaining.
	 */
	public RowRef init(TableControl table, Object rowObject) {
		_table = table;
		_rowObject = rowObject;
		return this;
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case ID:
				return id();
			case COLS:
				return cols();
			case SELECTED:
				return selected();
			case SELECT_ACTION:
				return selectAction();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

	/**
	 * The client-side ID to add to the row element.
	 */
	public String id() {
		return ((ConfiguredTableRenderer) _table.getRenderer()).getRowId(_table, _rowObject);
	}

	/**
	 * Access to the cells of this row.
	 */
	public Collection<CellRef> cols() {
		return _colsView.init(_table, _rowObject);
	}

	/**
	 * Whether this row is selected.
	 */
	public boolean selected() {
		return _table.getTableData().getSelectionModel().isSelected(_rowObject);
	}

	/**
	 * The script that selects this row.
	 */
	public DisplayValue selectAction() {
		TableControl table = _table;
		Object rowObject = _rowObject;
		return new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				table.appendSelectAction(out, table.getViewModel().getRowOfObject(rowObject), 0);
			}
		};
	}
}
