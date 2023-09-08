/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

/**
 * Base interface for typed database result sets.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface QueryResult {

	/**
	 * Advance the result set pointer to the first/next result.
	 * 
	 * @return Whether there is a first/next result.
	 */
	public boolean next() throws SQLException;

	/**
	 * Closes the result set.
	 */
	public void close() throws SQLException;

}
