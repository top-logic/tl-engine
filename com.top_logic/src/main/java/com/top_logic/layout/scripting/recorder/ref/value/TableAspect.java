/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.table.TableData;

/**
 * Abstract {@link ValueRef} that accesses some aspect of a {@link TableData} model.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface TableAspect extends ValueRef {

	/**
	 * The table this assertion is about.
	 */
	ModelName getTable();

	/**
	 * @see #getTable()
	 */
	void setTable(ModelName table);

}
