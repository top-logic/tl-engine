/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.properties;


/**
 * Database schema constants for {@link DBProperties}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBPropertiesSchema {

	/**
	 * The table name, where the properties are stored.
	 */
    String TABLE_NAME = "TL_PROPERTIES";

    /**
     * The value column.
     */
    String PROP_VALUE_COLUMN_NAME = "propValue";

    /**
     * The key column.
     */
	String PROP_KEY_COLUMN_NAME = "propKey";

	/**
	 * The cluster node column. 
	 */
	String NODE_COLUMN_NAME = "node";

}
