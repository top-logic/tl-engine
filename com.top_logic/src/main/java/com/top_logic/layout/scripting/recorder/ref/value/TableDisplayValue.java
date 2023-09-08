/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The value of a table cell in an actually displayed table row accessed with row number (instead of
 * row object).
 * 
 * <p>
 * This form of access is only useful to reason about tables in a certain sort order.
 * </p>
 * 
 * @see RowTableValue
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface TableDisplayValue extends TableValue {

	/**
	 * {@link ValueRef} resolving to the row number in the {@link #getTable()}.
	 */
	ModelName getRowNumber();

	/**
	 * @see #getRowNumber()
	 */
	void setRowNumber(ModelName value);

}
