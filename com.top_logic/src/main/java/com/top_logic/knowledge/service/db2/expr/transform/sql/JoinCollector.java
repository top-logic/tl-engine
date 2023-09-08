/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLTableReference;

/**
 * Hook interface for splitting {@link SQLBuilder} and {@link ExpressionEvaluator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface JoinCollector {

	void addLeftJoin(SQLTableReference setJoin, SQLExpression condition);

}
