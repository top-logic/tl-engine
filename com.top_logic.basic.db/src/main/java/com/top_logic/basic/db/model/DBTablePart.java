/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link DBSchemaPart} that is part of a {@link DBTable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface DBTablePart extends DBNamedPart {

	/**
	 * @see #getTable()
	 */
	String TABLE = "table";

	@Override
	@DerivedRef({ TABLE, DBTable.SCHEMA })
	public DBSchema getSchema();

	/**
	 * The owner {@link DBTable} of this part.
	 */
	@Container
	@Name(TABLE)
	DBTable getTable();

}
