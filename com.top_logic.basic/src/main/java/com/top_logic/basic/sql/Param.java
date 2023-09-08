/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Single parameter or result value in a {@link StatementScope}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Param {
	
	/**
	 * The index in the {@link PreparedStatement} or {@link ResultSet} of this parameter.
	 * 
	 * @see PreparedStatement#setObject(int, Object)
	 * @see ResultSet#getObject(int)
	 */
	int getIndex();
	
}