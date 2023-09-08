/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * {@link TableValue} in an {@link EditableRowTableModel}.
 * 
 * @see IndexedTableValue
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface RowTableValue extends TableValue {

	/**
	 * The row of the cell that this assertion is about.
	 */
	ModelName getRowObject();

	/**
	 * @see #getRowObject()
	 */
	void setRowObject(ModelName rowObject);

}
