/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
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
