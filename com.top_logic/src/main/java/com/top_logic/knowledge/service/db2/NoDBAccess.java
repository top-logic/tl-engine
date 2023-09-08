/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;

/**
 * Dummy for {@link MOKnowledgeItem} that does not represent {@link KnowledgeItem}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class NoDBAccess implements DBAccess, DBAccessFactory {

	/** Singleton {@link NoDBAccess} instance. */
	public static final NoDBAccess INSTANCE = new NoDBAccess();

	private NoDBAccess() {
		// singleton instance
	}

	@Override
	public DBAccess createDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl type, MORepository repository) {
		if (type.isSubtypeOf(BasicTypes.ITEM_TYPE_NAME)) {
			throw new IllegalArgumentException("Can not create 'no-db-access' for type '" + type + "'.");
		}
		return this;
	}

	@Override
	public void insert(PooledConnection db, long commitNumber, AbstractDBKnowledgeItem object) throws SQLException {
		throw fail(object.tTable());
	}

	private IllegalStateException fail(MetaObject type) {
		return new IllegalStateException("This DBAccess does not represent a type for which object instances exist: "
			+ type);
	}

	private IllegalStateException fail() {
		return new IllegalStateException("This DBAccess does not represent a type for which object instances exist.");
	}

	@Override
	public void insertAll(PooledConnection db, long commitNumber, List<? extends AbstractDBKnowledgeItem> objects)
			throws SQLException {
		throw fail();
	}

	@Override
	public void update(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		throw fail(object.tTable());
	}

	@Override
	public void updateAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects)
			throws SQLException {
		throw fail();
	}

	@Override
	public void delete(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		throw fail(object.tTable());
	}

	@Override
	public void deleteAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects)
			throws SQLException {
		throw fail();
	}

	@Override
	public KnowledgeItemInternal fetch(DBKnowledgeBase knowledgeBase, PooledConnection db, long branchContext, TLID id,
			long itemRevision, long dataRevision) throws SQLException {
		throw fail();
	}

	@Override
	public Object[] fetch(PooledConnection db, AbstractDBKnowledgeItem sourceItem, long dataRevision)
			throws SQLException {
		throw fail(sourceItem.tTable());
	}

	@Override
	public void fetchAll(DBKnowledgeBase kb, PooledConnection db, long dataRevision, Object[] keys, int keyCnt)
			throws SQLException {
		throw fail();
	}

	@Override
	public void branch(PooledConnection db, long branchId, long createRev, long baseBranchId, long baseRevision,
			SQLExpression filterExpr) throws SQLException {
		throw fail();
	}

	@Override
	public SQLExpression createRevMinExpr(TableSymbol table) {
		throw fail(table.getType());
	}

	@Override
	public SQLExpression createTypeExpr(TableSymbol table) {
		throw fail(table.getType());
	}

	@Override
	public SQLExpression createBranchExpr(TableSymbol table) {
		throw fail(table.getType());
	}

	@Override
	public SQLExpression createIdentifierExpr(TableSymbol table) {
		throw fail(table.getType());
	}

	@Override
	public SQLExpression createRevMaxExpr(TableSymbol table) {
		throw fail(table.getType());
	}

}
