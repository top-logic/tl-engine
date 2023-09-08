/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

/**
 * Represents a database result which delivers type names.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface TypeResult extends QueryResult {

	/**
	 * The name of a type touched in a range of revisions.
	 * 
	 * <p>
	 * Before calling this method, the method {@link #next()} must have returned
	 * <code>true</code>. If not, the result is undefined.
	 * </p>
	 * 
	 * <p>
	 * It is ensured that all types returned by this method before the last call of
	 * {@link #next()} is {@link String#compareTo(String) less} than the returned type.
	 * </p>
	 */
	String getType() throws SQLException;
}