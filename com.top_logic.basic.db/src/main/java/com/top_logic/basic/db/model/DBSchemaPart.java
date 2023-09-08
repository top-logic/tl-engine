/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.sql.DBHelper;

/**
 * Common interface for all parts of the {@link DBSchema} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface DBSchemaPart extends ConfigPart {

	/**
	 * @see #getSchema()
	 */
	String SCHEMA = "schema";

	/**
	 * The schema of this table.
	 * 
	 * @see DBSchema#getTables()
	 */
	@Name(SCHEMA)
	@Abstract
	DBSchema getSchema();
	
	/**
	 * Visits this part with the given {@link DBSchemaVisitor}.
	 */
	<R, A> R visit(DBSchemaVisitor<R,A> v, A arg);
	
	/**
	 * Produces SQL in the given dialect.
	 */
	default String toSQL(DBHelper sqlDialect) {
		return DBSchemaUtils.toSQL(sqlDialect, this);
	}

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();
}
