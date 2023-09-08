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
 * Reference to the set of displayed rows of a table to be iterated or accessed through
 * {@link WithProperties} by a {@link TemplateExpression rendering template}.
 * 
 * @see RowDisplay
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RowsCollectionRef extends CollectionWithProperties<RowDisplay> {

	private TableControl _table;

	/**
	 * Creates a {@link RowsCollectionRef}.
	 */
	public RowsCollectionRef(TableControl table) {
		_table = table;
	}

	@Override
	public Iterator<RowDisplay> iterator() {
		TableControl table = _table;
		return new MappedIterator<Object, RowDisplay>(getRowObjects().iterator()) {
			RowDisplay _rowDisplay = new RowDisplay();

			@Override
			protected RowDisplay map(Object rowObject) {
				return _rowDisplay.init(table, rowObject);
			}
		};
	}

	@Override
	public int size() {
		return getRowObjects().size();
	}

	private List<?> getRowObjects() {
		return _table.getViewModel().getDisplayedRows();
	}

}
