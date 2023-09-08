/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;

/**
 * Strategy for persisting {@link AbstractDBKnowledgeItem}s of some {@link MOKnowledgeItem type} to
 * a relational database.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public interface DBAccess {

	/**
	 * Stores the given new object.
	 * @param commitNumber The number of the commit in which the given objects are updated. 
	 */
	void insert(PooledConnection db, long commitNumber, AbstractDBKnowledgeItem object) throws SQLException;
	
	/**
	 * Stores the objects in the given list.
	 * @param commitNumber The number of the commit in which the given objects are updated. 
	 */
	void insertAll(PooledConnection db, long commitNumber, List<? extends AbstractDBKnowledgeItem> objects) throws SQLException;

	/**
	 * Stores the modifications done to the given existing object.
	 * @param commitNumber The number of the commit in which the given object is updated. 
	 */
	void update(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException;
	
	/**
	 * Stores the modifications done to the existing objects in the given list.
	 * @param commitNumber The number of the commit in which the given objects are updated. 
	 */
	void updateAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects) throws SQLException;

	/**
	 * Removes the given object from persistent storage.
	 * @param commitNumber The number of the commit in which the given object is deleted. 
	 */
	void delete(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException;
	
	/**
	 * Removes the objects in the given list from persistent storage.
	 * @param commitNumber The number of the commit in which the given objects are deleted. 
	 */
	void deleteAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects) throws SQLException;

	/**
	 * Searches the object that with the given id in the given branch and revision context.
	 * 
	 * <p>
	 * It may be that a current object is requested but the data are old. That is (almost ever) the
	 * case, when the caller is not up to date. (Revision of the database is larger that that of the
	 * caller.)
	 * </p>
	 * 
	 * @param itemRevision
	 *        History context to assign to the fetched item.
	 * @param dataRevision
	 *        The history context to load the data from. With a stable (non-current) data revision
	 *        and a current item revision, it is possible to treat a concrete revision as the
	 *        current revision of a node. The item revision must be greater or equal to the given
	 *        data revision.
	 */
	KnowledgeItemInternal fetch(DBKnowledgeBase knowledgeBase, PooledConnection db, long branchContext,
			TLID id, long itemRevision, long dataRevision) throws SQLException;

	/**
	 * Creates the storage for the the given item in the given revision, or <code>null</code> when
	 * the item does not exists in the given revision.
	 * 
	 * @param db
	 *        Read connection to database.
	 * @param sourceItem
	 *        Item to fetch data for.
	 * @param dataRevision
	 *        The revision of the requested data.
	 * @return The data of the item in the given revision, or <code>null</code> when the item does
	 *         not exists at that revision.
	 */
	Object[] fetch(PooledConnection db, AbstractDBKnowledgeItem sourceItem, long dataRevision) throws SQLException;

	/**
	 * Fetch the objects for the given keys in the given data revision from a single branch.
	 * 
	 * @param kb
	 *        The calling {@link DBKnowledgeBase}.
	 * @param db
	 *        The database connection to use.
	 * @param dataRevision
	 *        The history context to load the data from. With a stable (non-current) data revision
	 *        and a current history context of the given identifiers, it is possible to treat a
	 *        concrete revision as the current revision of a cluster node. The history context of
	 *        the given identifiers must be greater or equal to the given data revision.
	 * @param keys
	 *        Array of {@link DBObjectKey}s when called. The keys are replaced with the resolved
	 *        items when the method returns. All given keys must be from the same branch and the
	 *        same history context.
	 * @param keyCnt
	 *        Number of valid entries in the keys array. Must be at least 1 and at most
	 *        {@link DBHelper#getMaxSetSize()} of the connection.
	 */
	void fetchAll(DBKnowledgeBase kb, PooledConnection db, long dataRevision, Object[] keys, int keyCnt) throws SQLException;

	/**
	 * Branches the objects of the {@link MOKnowledgeItem type}, this strategy manages.
	 */
	void branch(PooledConnection db, long branchId, long createRev, long baseBranchId, long baseRevision, SQLExpression filterExpr) throws SQLException;

	/**
	 * Creates an expression to access the lower bound of a lifetime range of an object of the
	 * represented type.
	 * 
	 * @param table
	 *        alias of the database table to access
	 */
	SQLExpression createRevMinExpr(TableSymbol table);

	/**
	 * Creates an expression to access the type of an object of the represented type.
	 * 
	 * @param table
	 *        alias of the database table to access
	 */
	SQLExpression createTypeExpr(TableSymbol table);

	/**
	 * Creates an expression to access the branch of an object of the represented type.
	 * 
	 * @param table
	 *        alias of the database table to access
	 */
	SQLExpression createBranchExpr(TableSymbol table);

	/**
	 * Creates an expression to access the upper bound of a lifetime range of an object of the
	 * represented type.
	 * 
	 * @param table
	 *        alias of the database table to access
	 */
	SQLExpression createIdentifierExpr(TableSymbol table);

	/**
	 * Creates an expression to access the identifier of an object of the represented type
	 * 
	 * @param table
	 *        alias of the database table to access
	 */
	SQLExpression createRevMaxExpr(TableSymbol table);
	
}
