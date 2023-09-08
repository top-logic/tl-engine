/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.layout.table.TableData;

/**
 * {@link TableAspect} that resolves to the number of actually displayed rows in a {@link TableData}
 * (after filters have been applied).
 * 
 * @see AllTableRows
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface DisplayedTableRows extends TableAspect {

	// Pure marker interface.

}
