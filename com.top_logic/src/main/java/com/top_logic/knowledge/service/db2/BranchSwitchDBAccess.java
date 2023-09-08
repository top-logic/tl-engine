/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.Conversion;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;

/**
 * {@link DBAccess} to encapsulate access to branch switch table
 * 
 */
@FrameworkInternal
public final class BranchSwitchDBAccess extends SystemTypeDBAccess {

	/**
	 * {@link DBAccessFactory} for {@link BranchSwitchDBAccess} implementations.
	 */
	@FrameworkInternal
	public static class Factory implements DBAccessFactory {

		/**
		 * Singleton {@link BranchSwitchDBAccess.Factory} instance.
		 */
		public static final BranchSwitchDBAccess.Factory INSTANCE = new BranchSwitchDBAccess.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public DBAccess createDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl type, MORepository repository) {
			return new BranchSwitchDBAccess(sqlDialect, type);
		}

	}

	BranchSwitchDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		super(sqlDialect, table);
	}

	@Override
	public SQLExpression createRevMinExpr(TableSymbol table) {
		//TODO: RevMin is actually the revision in which the corresponding branch was created
		return SQLFactory.literalLong(Revision.INITIAL.getCommitNumber());
	}

	@Override
	protected String getIdAttributeName() {
		throw new UnsupportedOperationException(type + " has no identifier");
	}

	@Override
	protected CompiledStatement createFetchItemStatement() {
		return null;
	}

	@Override
	protected CompiledStatement createFetchAllStatement() {
		return null;
	}

	@Override
	protected Conversion conversion(DBAttribute dbAttr, boolean fetch) {
		return Conversion.IDENTITY;
	}

}