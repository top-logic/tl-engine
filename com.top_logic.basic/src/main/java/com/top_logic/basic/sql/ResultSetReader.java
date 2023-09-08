/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.ResultSet;


/**
 * {@link ResultSetAdapter} that reads arbitrary single values from a
 * {@link ResultSet}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResultSetReader<T> extends ResultSetAdapter {

	/**
	 * Read information from the installed {@link ResultSet}.
	 * 
	 * <p>
	 * A call to this method does not advance the {@link ResultSet} pointer to
	 * the next result.
	 * </p>
	 * 
	 * @return The value read from the {@link ResultSet}
	 * 
	 * @see #setResultSet(ResultSet)
	 */
	T read();

}
