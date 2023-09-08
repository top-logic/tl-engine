/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.NullDefault;

/**
 * Definition of a database schema that is independent of a concret database
 * instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DBSchema extends DBNamedPart {

	/**
	 * The name of the root element of a {@link DBSchema} serialization.
	 */
	String SCHEMA_ELEMENT = "schema";

	/**
	 * See {@link #getTables()}.
	 */
	String TABLES = "tables";

	@Override
	@NullDefault
	public String getName();

	@Override
	@DerivedRef({})
	public DBSchema getSchema();

	/**
	 * All tables in this schema.
	 * 
	 * @see DBTable#getSchema()
	 */
	@Name(TABLES)
	@Key(DBTable.NAME)
	Collection<DBTable> getTables();

	/**
	 * Find the table in this schema with the given name.
	 * 
	 * @param name
	 *        The table name to find the {@link DBTable} for.
	 * 
	 * @return The table with the given name, or <code>null</code>, if this
	 *         schema does not contain a table with the given name.
	 */
	@Indexed(collection = TABLES)
	DBTable getTable(String name);
	
}
