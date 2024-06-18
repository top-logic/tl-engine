/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding.api;

import com.top_logic.model.TLObject;

/**
 * Algorithm for importing values from a database row to a {@link TLObject}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RowReader {

	/**
	 * Reads the given row and updates the given {@link TLObject} with imported values.
	 *
	 * @param model
	 *        The object to update.
	 * @param row
	 *        The row to read.
	 */
	void read(TLObject model, ImportRow row);

}
