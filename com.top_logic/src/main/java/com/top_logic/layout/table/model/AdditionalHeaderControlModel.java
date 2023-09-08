/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.List;

import com.top_logic.layout.table.TableRenderer.RenderState;
import com.top_logic.layout.table.TableViewModel;

/**
 * Contains the information that is necessary to write an
 * {@link ColumnBaseConfig#getAdditionalHeaders() additional header}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AdditionalHeaderControlModel {

	private final RenderState _renderState;

	private final int _columnIndex;

	/**
	 * Creates an {@link AdditionalHeaderControlModel}.
	 * 
	 * @param renderState
	 *        Is not allowed to be null.
	 * @param columnIndex
	 *        Is not allowed to be null.
	 */
	public AdditionalHeaderControlModel(RenderState renderState, int columnIndex) {
		_renderState = requireNonNull(renderState);
		_columnIndex = requireNonNull(columnIndex);
	}

	/**
	 * The values of this column.
	 * <p>
	 * The values are ordered as they are displayed in the table.
	 * </p>
	 * <p>
	 * Contains exactly those rows that are displayed to the user, with one exception: If the table
	 * is split over several pages, contains the rows from all pages.
	 * </p>
	 * 
	 * @return Never null. A new, mutable and resizable {@link List}.
	 */
	public List<Object> getValues() {
		List<Object> values = list();
		TableViewModel tableViewModel = getTableViewModel();
		String columnName = getColumnName();
		for (Object rowObject : getDisplayedRows()) {
			Object rawCellValue = tableViewModel.getValueAt(rowObject, columnName);
			/* The mapping is necessary for example in the GridComponent: In the selected row, the
			 * raw cell value is the FormField. This mapping retrieves the actual value from the
			 * FormField. And there are further tables, which are configured in a way that this
			 * mapping is necessary. */
			values.add(mapToBusinessValue(tableViewModel, columnName, rawCellValue));
		}
		return values;
	}

	private Object mapToBusinessValue(TableViewModel tableViewModel, String columnName, Object rawCellValue) {
		return tableViewModel.getColumnDescription(columnName).getSortKeyProvider().apply(rawCellValue);
	}

	/**
	 * Short-cut for: {@link TableViewModel#getDisplayedRows()}
	 * 
	 * @return Never null.
	 */
	public List<?> getDisplayedRows() {
		return getTableViewModel().getDisplayedRows();
	}

	/**
	 * The name of this column.
	 * 
	 * @return Never null.
	 */
	public String getColumnName() {
		return getTableViewModel().getColumnName(getColumnIndex());
	}

	/**
	 * Short-cut to the {@link TableViewModel}.
	 * 
	 * @return Never null.
	 */
	public TableViewModel getTableViewModel() {
		return getRenderState().getModel();
	}

	/**
	 * Getter for the {@link RenderState}.
	 * 
	 * @return Never null.
	 */
	public RenderState getRenderState() {
		return _renderState;
	}

	/** The index of the column. */
	public int getColumnIndex() {
		return _columnIndex;
	}

}
