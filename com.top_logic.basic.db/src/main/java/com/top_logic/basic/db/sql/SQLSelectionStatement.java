/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLStatement} base class for {@link SQLSelect} and {@link SQLUnion}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SQLSelectionStatement extends SQLStatement {

	/**
	 * The left-most accessed table in a {@link SQLSelect}.
	 */
	SQLTable getInitialTable();

}
