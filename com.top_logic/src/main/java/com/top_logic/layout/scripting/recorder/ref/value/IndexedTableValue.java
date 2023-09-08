/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.table.TableModel;

/**
 * {@link TableValue} in a general {@link TableModel}.
 * 
 * @deprecated Use {@link RowTableValue} instead, as its row-object based row identification is much
 *             more stable then this row-index based approach.
 * 
 * @see RowTableValue
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface IndexedTableValue extends TableValue {

	/**
	 * The index of the accessed row.
	 */
	int getRowIndex();

	/**
	 * @see #getRowIndex()
	 */
	void setRowIndex(int value);

}
