/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.db.sql.SQLColumnReference;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;

/**
 * Base class for {@link DBAccess} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AbstractDBAccess implements DBAccess {

	/**
	 * Default off set to store the argument for an {@link DBAttribute} in an array for the
	 * {@link PreparedStatement}.
	 */
	protected static final int STATEMENT_ARGS_OFFSET = 0;

	/** This {@link DBAccess} encapsulates the database access for that type. */
	protected final MOKnowledgeItemImpl type;
	
	/** Primary key columns of {@link #type}. */
	protected final MOAttribute[] keyAttributes;

	/** The number of columns in the primary key */
	protected final int numberKeyColumns;

	/** Non-primary-key columns of {@link #type}. */
	protected final MOAttribute[] dataAttributes;

	/** The number of columns in the primary key */
	protected final int numberDataColumns;

	/**
	 * The DB access layer used.
	 */
	protected final DBHelper sqlDialect;

	/**
	 * Creates an {@link AbstractDBAccess}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect to use for accessing data.
	 * @param table
	 *        See {@link #getType()}.
	 */
	public AbstractDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		this.sqlDialect = sqlDialect;

		this.type = table;
		
		MOIndex primaryKey = type.getPrimaryKey();
		if (primaryKey != null) {
			this.keyAttributes = primaryKey.getAttributes().toArray(MOAttribute.NO_MO_ATTRIBUTES);
			this.numberKeyColumns = primaryKey.getKeyAttributes().size();

			ArrayList<MOAttribute> dataColumnsBuffer = new ArrayList<>();
			int numberData = 0;
			List<MOAttribute> attributes = table.getAttributes();
			for (int n = 0, cnt = attributes.size(); n < cnt; n++) {
				MOAttribute attr = attributes.get(n);
				if (ArrayUtil.contains(keyAttributes, attr)) {
					continue;
				}
				numberData += attr.getDbMapping().length;
				dataColumnsBuffer.add(attr);
			}

			this.dataAttributes = dataColumnsBuffer.toArray(MOAttribute.NO_MO_ATTRIBUTES);
			this.numberDataColumns = numberData;
		} else {
			this.keyAttributes = MOAttribute.NO_MO_ATTRIBUTES;
			this.numberKeyColumns = 0;
			this.dataAttributes = type.getAttributes().toArray(MOAttribute.NO_MO_ATTRIBUTES);
			this.numberDataColumns = type.getDBColumnCount();
		}
		
	}
	
	/**
	 * The type this {@link DBAccess} operates on.
	 */
	public MOKnowledgeItemImpl getType() {
		return type;
	}

	@Override
	public void deleteAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects) throws SQLException {
		for (int n = 0, cnt = objects.size(); n < cnt; n++) {
			delete(db, commitNumber, objects.get(n));
		}
	}

	@Override
	public void insertAll(PooledConnection db, long commitNumber, List<? extends AbstractDBKnowledgeItem> objects) throws SQLException {
		for (int n = 0, cnt = objects.size(); n < cnt; n++) {
			insert(db, commitNumber, objects.get(n));
		}
	}

	@Override
	public void updateAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects) throws SQLException {
		for (int n = 0, cnt = objects.size(); n < cnt; n++) {
			update(db, commitNumber, objects.get(n));
		}
	}
	
	/**
	 * @see DBKnowledgeItem#getLocalValues()
	 */
	protected static Object[] getLocalData(AbstractDBKnowledgeItem object) {
		return object.getLocalValues();
	}

	/**
	 * @see DBKnowledgeItem#getGlobalValues(long)
	 */
	protected static Object[] getGlobalData(AbstractDBKnowledgeItem object, long revision) {
		return object.getGlobalValues(revision);
	}

	/**
	 * Stores the value of all given attributes.
	 * 
	 * @see AbstractDBAccess#setValue(ConnectionPool, Object[], MOAttribute, int, KnowledgeItem,
	 *      Object[], long)
	 */
	protected final void setAllValues(PooledConnection db, Object[] statementArgs,
			KnowledgeItem item, List<? extends MOAttribute> attributes, Object[] storage, long commitNumber) throws SQLException {
		ConnectionPool pool = db.getPool();
		for (MOAttribute attribute : attributes) {
			setValue(pool, statementArgs, attribute, STATEMENT_ARGS_OFFSET, item, storage, commitNumber);
		}
	}
	
	/**
	 * Stores the value of the {@link #keyAttributes key attributes} of the represented
	 * {@link #type}.
	 * 
	 * @see AbstractDBAccess#setValue(ConnectionPool, Object[], MOAttribute, int, KnowledgeItem,
	 *      Object[], long)
	 */
	protected final void setKeyValues(PooledConnection db, Object[] statementArgs, KnowledgeItem item, Object[] storage,
			int dbOffset, long commitNumber) throws SQLException {
		ConnectionPool pool = db.getPool();
		for (MOAttribute keyColumn : keyAttributes) {
			setValue(pool, statementArgs, keyColumn, dbOffset, item, storage, commitNumber);
		}
	}

	/**
	 * Stores the value of the {@link #dataAttributes data attributes} of the represented
	 * {@link #type}.
	 * 
	 * @see AbstractDBAccess#setValue(ConnectionPool, Object[], MOAttribute, int, KnowledgeItem,
	 *      Object[], long)
	 */
	protected final void setDataValues(PooledConnection db, Object[] statementArgs, KnowledgeItem item, Object[] storage,
			int dbOffset, long commitNumber) throws SQLException {
		ConnectionPool pool = db.getPool();
		for (MOAttribute dataColumn : dataAttributes) {
			setValue(pool, statementArgs, dataColumn, dbOffset, item, storage, commitNumber);
		}
	}

	/**
	 * Stores the value of the given attribute.
	 * 
	 * @see AttributeStorage#storeValue(ConnectionPool, Object[], int, MOAttribute, DataObject,
	 *      Object[], long)
	 */
	protected final void setValue(ConnectionPool pool, Object[] statementArgs, MOAttribute attr, int dbOffset,
			KnowledgeItem item, Object[] storage, long commitNumber) throws SQLException {
		attr.getStorage().storeValue(pool, statementArgs, dbOffset, attr, item, storage, commitNumber);
	}

	/**
	 * Creates a literal string with the name of the represented type.
	 * 
	 * @see DBAccess#createTypeExpr(TableSymbol)
	 */
	@Override
	public SQLExpression createTypeExpr(TableSymbol table) {
		return literalString(getType().getName());
	}

	/** Create the {@link SQLException} for "lost update" situations. */
	protected SQLException createLostUpdateException(KnowledgeItem object) {
		return new SQLException("Object could not be updated, as it has changes that are based on an outdated version"
			+ " (preventing lost update): type=" + object.tTable().getName());
	}

	/**
	 * creates an expression to access the given column
	 * 
	 * @param dbAttribute
	 *        the column to access
	 * @param table
	 *        Representation of the table to access
	 */
	protected static SQLColumnReference createColumn(DBAttribute dbAttribute, TableSymbol table) {
		boolean notNull = !table.isPotentiallyNull() && dbAttribute.isSQLNotNull();
		return column(table.getTableAlias(), dbAttribute, notNull);
	}

}

