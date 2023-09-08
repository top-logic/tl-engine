/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.sql.DBType;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Factory for {@link KnowledgeBase}-specific {@link SQLExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBSQLFactory {

	/**
	 * {@link SQLExpression} that represents the current revision in the query.
	 */
	public static SQLExpression currentRevision() {
		/* In a current reference the revision context in which the query is executed is the current
		 * revision. */
		return SQLFactory.parameter(DBType.LONG, SQLBuilder.REQUESTED_REVISION_PARAM);
	}

	/**
	 * {@link SQLExpression} that represents history context in which the query was executed.
	 */
	public static SQLExpression requestedHistoryContext() {
		return SQLFactory.parameter(DBType.LONG, SQLBuilder.HISTORY_CONTEXT_PARAM);
	}

}
