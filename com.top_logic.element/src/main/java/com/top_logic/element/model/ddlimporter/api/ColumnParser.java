/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.ddlimporter.api;

import com.top_logic.model.TLObject;

/**
 * Parser callback that converts a value read form a database column to be stored in a
 * {@link TLObject} instance.
 * 
 * @see ColumnFormat
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ColumnParser {

	/**
	 * Converts a value read form a JDBC connection to a value stored in a corresponding property of
	 * a {@link TLObject}.
	 *
	 * @param columnValue
	 *        The value read from a database.
	 * @return The converted value to store in a model.
	 */
	Object getApplicationValue(Object columnValue);

}
