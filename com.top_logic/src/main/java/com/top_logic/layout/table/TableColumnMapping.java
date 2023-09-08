/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.layout.AccessorMapping;

/**
 * {@link Mapping} that retrieves column values from a {@link TableModel} for a given row object.
 * 
 * @see AccessorMapping
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableColumnMapping implements Mapping<Object, Object> {

	/**
	 * Creates a {@link TableColumnMapping}.
	 * 
	 * @param model
	 *        The {@link TableModel} to retrieve values from.
	 * @param column
	 *        The name of the columns to access.
	 * @param valueMapping
	 *        An additional {@link Mapping} to apply to column values.
	 * @return {@link Mapping} from row objects to columns values in a {@link TableModel}.
	 */
	public static Mapping<Object, Object> createTableColumnMapping(TableModel model, String column,
			final Mapping<Object, Object> valueMapping) {
		if (valueMapping == Mappings.identity()) {
			return new TableColumnMapping(column, model);
		} else {
			return new MappedTableColumnMapping(column, model, valueMapping);
		}
	}

	private final TableModel _model;

	private final String _column;

	TableColumnMapping(String column, TableModel model) {
		_column = column;
		_model = model;
	}

	@Override
	public Object map(Object rowObject) {
		return _model.getValueAt(rowObject, _column);
	}

	private static final class MappedTableColumnMapping extends TableColumnMapping {
		private final Mapping<Object, Object> _valueMapping;

		MappedTableColumnMapping(String column, TableModel model, Mapping<Object, Object> valueMapping) {
			super(column, model);

			_valueMapping = valueMapping;
		}

		@Override
		public Object map(Object rowObject) {
			return _valueMapping.map(super.map(rowObject));
		}
	}

}