/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.TableModel;

/**
 * Instances of this class provide row based access to table models using the same technique as is
 * used for accessing tree models.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class TableRowAccessor implements Accessor<Number> {

	/**
	 * The model to provide row based access for.
	 */
	private final TableModel _model;

	/**
	 * A mapping of column name to its index for faster access.
	 */
	private final Map<String, Integer> _columnIndex;

	/**
	 * Creates an instance of this class to provide row based access to the specified model.
	 * 
	 * @param model
	 *        the {@link TableModel} to provide row based access for
	 */
	public TableRowAccessor(final TableModel model) {
		_model = model;

		// retrieve the total number of columns
		final int colCount = model.getColumnCount();

		// use the appropriate pre-sizing for better performance.
		// We actually need that one for large data export.
		_columnIndex = new HashMap<>(colCount);

		// now go through all columns in the specified model
		// and map column names to their indices.
		for (int i = 0; i < colCount; i++) {
			_columnIndex.put(_model.getColumnName(i), i);
		}
	}

	/**
	 * This implementation assumes that the specified object is the zero-based number of the row to
	 * access the specified property for.
	 */
	@Override
	public Object getValue(final Number object, final String property) {
		final int row = object.intValue();
		final int column = indexOf(property);

		// NOTE: do NOT perform any sanity checks on
		// the resolved row and column indices.
		// We really need the exception from the model itself here!
		return _model.getValueAt(row, column);
	}

	/**
	 * This implementation assumes that the specified object is the zero based number of the row to
	 * set the specified property for.
	 */
	@Override
	public void setValue(final Number object, final String property, final Object value) {
		final int row = object.intValue();
		final int column = indexOf(property);

		// NOTE: do NOT perform any sanity checks on
		// the resolved row and column indices.
		// We really need the exception from the model itself here!
		_model.setValueAt(value, row, column);
	}

	/**
	 * Returns the index of the column with the specified name.
	 * 
	 * @param property
	 *        the name of the column to retrieve the index for
	 * @return the index of the column with the specified name or -1 if no column with the specified
	 *         has been found
	 */
	protected int indexOf(final String property) {
		final Integer index = _columnIndex.get(property);
		return index != null ? index.intValue() : -1;
	}
}