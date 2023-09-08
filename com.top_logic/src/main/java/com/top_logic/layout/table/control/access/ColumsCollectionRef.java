/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control.access;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.shared.collection.map.MappedIterator;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.template.CollectionWithProperties;
import com.top_logic.layout.template.WithProperties;

/**
 * Reference to the set of displayed columns of a table to be iterated or accessed through
 * {@link WithProperties} by a {@link TemplateExpression rendering template}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColumsCollectionRef extends CollectionWithProperties<CellRef> {

	private final CellRef _cell = new CellRef();

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
	public ColumsCollectionRef init(TableControl table, Object rowObject) {
		_table = table;
		_rowObject = rowObject;
		return this;
	}

	@Override
	public Iterator<CellRef> iterator() {
		TableControl table = _table;
		Object rowObject = _rowObject;
		CellRef cell = _cell;

		return new MappedIterator<>(getColumnNames().iterator()) {
			@Override
			protected CellRef map(String columnName) {
				return cell.init(table, rowObject, columnName);
			}
		};
	}

	private List<String> getColumnNames() {
		return _table.getViewModel().getColumnNames();
	}

	@Override
	public int size() {
		return getColumnNames().size();
	}
}
