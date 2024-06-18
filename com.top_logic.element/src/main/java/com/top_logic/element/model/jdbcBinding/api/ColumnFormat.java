/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding.api;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLProperty;

/**
 * Callback that formats an application value to be stored in a DB column.
 * 
 * @see ColumnParser
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ColumnFormat {

	/**
	 * Converts the given value retrieved from a {@link TLProperty} to be sent to a JDBC connection.
	 *
	 * @param applicationValue
	 *        A property value of a {@link TLObject}.
	 * @return The corresponding value to be exported to a database.
	 */
	Object getColumnValue(Object applicationValue);

}
