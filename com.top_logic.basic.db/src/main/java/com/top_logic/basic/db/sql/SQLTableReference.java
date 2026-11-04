/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Part of an SQL statement that is used as table, e.g. a {@link SQLTable} or a {@link SQLJoin}.
 * 
 * <pre>
 * {@link SQLTableReference}:
 *     {@link SQLTable}
 *   | {@link SQLJoin}
 * </pre>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SQLTableReference extends SQLPart {
	
	/**
	 * The first table referenced by this part.
	 */
	public abstract SQLTable getInitialTable();

}
