/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLTableReference}:
 *     {@link SQLTable}
 *   | {@link SQLJoin}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SQLTableReference extends SQLPart {
	
	public abstract SQLTable getInitialTable();

}
