/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.basic.db.sql.SQLColumnReference;
import com.top_logic.basic.db.sql.SQLInlineTransformation;
import com.top_logic.basic.db.sql.SQLPart;

/**
 * {@link SQLInlineTransformation} that changes the {@link SQLColumnReference#getTableAlias()} to the
 * visit argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetTableAlias extends SQLInlineTransformation<String> {

	/**
	 * Singleton {@link SetTableAlias} instance.
	 */
	public static final SetTableAlias INSTANCE = new SetTableAlias();

	private SetTableAlias() {
		// Singleton constructor.
	}

	@Override
	public SQLPart visitSQLColumnReference(SQLColumnReference sql, String arg) {
		sql.setTableAlias(arg);
		return super.visitSQLColumnReference(sql, arg);
	}

}
