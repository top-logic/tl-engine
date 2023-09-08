/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.Header;

/**
 * {@link LabelProvider} for column names of a {@link TableControl}.
 * 
 * <p>
 * Workaround for the quirks API that the renderer of a control defines the displayed column names.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ColumnLabelProvider implements LabelProvider {

	private TableData _table;

	/**
	 * Creates a {@link ColumnLabelProvider}.
	 */
	public ColumnLabelProvider(TableData table) {
		_table = table;
	}

	@Override
	public String getLabel(Object value) {
		String columnName = (String) value;

		Header header = _table.getTableModel().getHeader();
		Column column = header.getColumn(columnName);
		if (column == null) {
			return null;
		}

		return column.getLabel(_table);
	}

	/**
	 * {@link LabelProvider} for column names of the given table.
	 */
	public static LabelProvider newInstance(TableData table) {
		return new ColumnLabelProvider(table);
	}

}