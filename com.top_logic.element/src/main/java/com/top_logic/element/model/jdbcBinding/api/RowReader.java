/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
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
