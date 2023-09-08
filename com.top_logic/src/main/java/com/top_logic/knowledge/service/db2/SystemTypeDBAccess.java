/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.util.TLContext;

/**
 * {@link DirectDBAccess} for internal types which are unchanged and live on trunk
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class SystemTypeDBAccess extends DirectDBAccess {

	SystemTypeDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		super(sqlDialect, table);
	}

	@Override
	public SQLExpression createRevMaxExpr(TableSymbol table) {
		// system types won't be changed
		return SQLFactory.literalLong(Revision.CURRENT_REV);
	}

	@Override
	public SQLExpression createBranchExpr(TableSymbol table) {
		// system types only exists on trunk
		return SQLFactory.literalLong(TLContext.TRUNK_ID);
	}

}

