/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.ResultSet;

/**
 * Common super interface for dynamic adapters to a JDBC {@link ResultSet}.
 * 
 * @see #setResultSet(ResultSet) Installing the {@link ResultSet} is required
 *      before using access method defined by sub interfaces.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResultSetAdapter {

	/**
	 * Installs the given {@link ResultSet} into this adapter.
	 * 
	 * <p>
	 * This adapter becomes usable only after installing a {@link ResultSet}.
	 * </p>
	 * 
	 * @param resultSet
	 *        The {@link ResultSet} this adapter should take its data from.
	 */
	void setResultSet(ResultSet resultSet);

}
