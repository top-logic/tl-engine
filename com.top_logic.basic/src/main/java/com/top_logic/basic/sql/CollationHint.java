/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;


/**
 * Specification of a collation for a collation sensitive operation.
 * 
 * @see DBHelper#appendCollatedExpression(Appendable, String, CollationHint)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum CollationHint {
	/**
	 * Default collation (column default, table default, connection default, or
	 * server default).
	 */
	NONE,
	
	/**
	 * Binary collation consistent with {@link String#compareTo(String)}
	 */
	BINARY,
	
	/**
	 * Natural language sort order.
	 */
	NATURAL
	
}