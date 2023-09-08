/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.table.TableData;

/**
 * {@link TableAspect} that resolves to the number of the displayed row of an row object in a
 * {@link TableData}. All filters and sorting are respected.
 * 
 * @since 5.7.5
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Deprecated
public interface RowOfObject extends TableAspect {

	/**
	 * The row of the cell that this assertion is about.
	 */
	ModelName getRowObject();

	/**
	 * @see #getRowObject()
	 */
	void setRowObject(ModelName rowObject);
}

