/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.parameter;
import static com.top_logic.basic.db.sql.SQLFactory.parameterDef;
import static com.top_logic.basic.db.sql.SQLFactory.table;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.setParameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLColumnReference;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLParameter;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLSetParameter;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.db.sql.SQLUpdate;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.SQLH;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.util.TLContext;


/**
 * {@link DBAccess} implementation for objects under version control.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class VersionedDBAccess extends DefaultDBAccess {

	private final DBAttribute _branchAttribute;

	private final DBAttribute _idAttribute;

	private final DBAttribute _revMaxAttribute;

	private final DBAttribute _revMinAttribute;

	private final CompiledStatement _insertStatement;

	private final CompiledStatement _outdateStatement;

	private final CompiledStatement _fetchHistoricStatement;

	private final CompiledStatement _fetchCurrentStatement;

	private final CompiledStatement _fetchAllHistoricStatement;

	private final CompiledStatement _fetchAllCurrentStatement;

	/**
	 * Creates a {@link VersionedDBAccess}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect to use for accessing data.
	 * @param table
	 *        See {@link #getType()}
	 */
	public VersionedDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		this(sqlDialect, table, BasicTypes.BRANCH_ATTRIBUTE_NAME, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME,
			BasicTypes.REV_MAX_ATTRIBUTE_NAME, BasicTypes.REV_MIN_ATTRIBUTE_NAME);
	}

	/**
	 * Creates a {@link VersionedDBAccess}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect to use for accessing data.
	 * @param table
	 *        See {@link #getType()}
	 * @param branchAttribute
	 *        Name of the attribute storing the branch.
	 * @param idAttribute
	 *        Name of the attribute storing the identifier.
	 * @param revMaxAttribute
	 *        Name of the attribute storing the maximal revision the corresponding line is valid.
	 * @param revMinAttribute
	 *        Name of the attribute storing the minimal revision the corresponding line is valid.
	 */
	public VersionedDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table, String branchAttribute,
			String idAttribute, String revMaxAttribute, String revMinAttribute) {
		super(sqlDialect, table);
		if (type.multipleBranches()) {
			_branchAttribute = attribute(branchAttribute);
		} else {
			_branchAttribute = null;
		}
		_idAttribute = attribute(idAttribute);
		_revMaxAttribute = attribute(revMaxAttribute);
		_revMinAttribute = attribute(revMinAttribute);
		
		_insertStatement = SQLH.createInsert(sqlDialect, table);
		_outdateStatement = createOutdateStatement();
		_fetchHistoricStatement = createFetchItemStatement(true);
		_fetchCurrentStatement = createFetchItemStatement(false);
		_fetchAllHistoricStatement = createFetchAllStatement(true);
		_fetchAllCurrentStatement = createFetchAllStatement(false);
	}

	private CompiledStatement createFetchItemStatement(boolean historicData) {
		String tableAlias = NO_TABLE_ALIAS;
		DBAttribute idAttribute = id();
		SQLExpression idCheck = eqSQL(column(tableAlias, idAttribute.getDBName()), param(idAttribute));
		Parameter idParamDef = paramDef(idAttribute);
		List<SQLOrder> orders = noOrder();
		return createFetchStatement(tableAlias, idCheck, idParamDef, historicData, orders);
	}

	private CompiledStatement createFetchAllStatement(boolean historicData) {
		String tableAlias = NO_TABLE_ALIAS;
		DBAttribute idAttribute = id();
		SQLExpression idCheck = inSet(column(tableAlias, idAttribute.getDBName()), setParam(idAttribute));
		Parameter idParamDef = setParamDef(idAttribute);
		List<SQLOrder> orders = orders(order(false, column(tableAlias, idAttribute.getDBName())));
		return createFetchStatement(tableAlias, idCheck, idParamDef, historicData, orders);
	}

	private CompiledStatement createFetchStatement(String tableAlias, SQLExpression idCheck, Parameter idParam,
			boolean historicData, List<SQLOrder> orders) {
		String branchParamName = "branchParameter";
		DBAttribute revMinAttribute = _revMinAttribute;
		DBAttribute revMaxAttribute = _revMaxAttribute;
		SQLExpression branchCheck;
		DBType branchDBType = IdentifierTypes.BRANCH_REFERENCE_MO_TYPE.getDefaultSQLType();
		if (type.multipleBranches()) {
			DBAttribute branchAttribute = _branchAttribute;
			SQLColumnReference branchColumn = column(tableAlias, branchAttribute.getDBName());
			SQLParameter branchParam = parameter(branchDBType, branchParamName);
			branchCheck = eqSQL(branchColumn, branchParam);
		} else {
			branchCheck = literalTrueLogical();
		}
		SQLExpression revCheck;
		if (historicData) {
			revCheck = and(
				le(column(tableAlias, revMinAttribute.getDBName()), param(revMinAttribute)),
				ge(column(tableAlias, revMaxAttribute.getDBName()), param(revMaxAttribute))
				);
		} else {
			revCheck = eqSQL(column(tableAlias, revMaxAttribute.getDBName()), param(revMaxAttribute));
		}

		SQLExpression where = and(
			branchCheck, revCheck, idCheck
		);
		SQLSelect select =
			select(
				columnDefs(tableAlias, type),
				table(type, tableAlias),
				where,
				orders);
		List<Parameter> parameters;
		if (historicData) {
			parameters = parameters(
				parameterDef(branchDBType, branchParamName),
				idParam,
				paramDef(revMinAttribute),
				paramDef(revMaxAttribute)
				);
		} else {
			parameters = parameters(
				parameterDef(branchDBType, branchParamName),
				idParam,
				paramDef(revMaxAttribute)
				);
		}
		return query(parameters, select).toSql(sqlDialect);
	}

	private CompiledStatement createOutdateStatement() {
		String branchParamName = "branchParam";
		String tableAlias = NO_TABLE_ALIAS;
		DBAttribute idAttr = id();
		DBAttribute revMaxAttr = _revMaxAttribute;
		DBAttribute revMinAttr = _revMinAttribute;
		DBType branchDBType = IdentifierTypes.BRANCH_REFERENCE_MO_TYPE.getDefaultSQLType();

		SQLExpression branchEquality;
		if (type.multipleBranches()) {
			DBAttribute branchAttr = _branchAttribute;
			SQLColumnReference column = column(NO_TABLE_ALIAS, branchAttr, true);
			SQLParameter branchParam = parameter(branchDBType, branchParamName);
			branchEquality = eq(column, branchParam);
		} else {
			branchEquality = literalTrueLogical();
		}
		SQLExpression idEquality =
			eq(column(NO_TABLE_ALIAS, idAttr, true), param(idAttr));
		// For finding the row in the primary key index
		SQLExpression maxRevMax =
			eq(column(NO_TABLE_ALIAS, revMaxAttr, true),
				literal(revMaxAttr.getSQLType(), Revision.CURRENT_REV_WRAPPER));
		// To ensure to detect concurrent updates.
		SQLExpression revMinEquality =
			eq(column(NO_TABLE_ALIAS, revMinAttr, true), param(revMinAttr));
		SQLExpression where = and(branchEquality, idEquality, maxRevMax, revMinEquality);
		SQLUpdate update =
			SQLFactory.update(table(type, tableAlias), where, Arrays.asList(revMaxAttr.getDBName()),
				Arrays.<SQLExpression> asList(param(revMaxAttr)));
		List<Parameter> parameters = Arrays.asList(
			paramDef(revMaxAttr),
			parameterDef(branchDBType, branchParamName),
			paramDef(idAttr),
			paramDef(revMinAttr)
			);
		return query(parameters, update).toSql(sqlDialect);
	}

	private CompiledStatement createOutdateInSetStatement(Collection<?> ids) {
		String branchParamName = "branchParam";
		String tableAlias = NO_TABLE_ALIAS;
		DBAttribute idAttr = id();
		DBAttribute revMaxAttr = _revMaxAttribute;
		DBAttribute revMinAttr = _revMinAttribute;
		DBType branchDBType = IdentifierTypes.BRANCH_REFERENCE_MO_TYPE.getDefaultSQLType();

		SQLExpression branchEquality;
		if (type.multipleBranches()) {
			DBAttribute branchAttr = _branchAttribute;
			SQLColumnReference column = column(NO_TABLE_ALIAS, branchAttr, true);
			SQLParameter branchParam = parameter(branchDBType, branchParamName);
			branchEquality = eq(column, branchParam);
		} else {
			branchEquality = literalTrueLogical();
		}
		SQLExpression idEquality =
			inSet(column(NO_TABLE_ALIAS, idAttr, true), ids, DBType.ID);
		SQLExpression maxRevMax =
			eq(column(NO_TABLE_ALIAS, revMaxAttr, true),
				literal(revMaxAttr.getSQLType(), Revision.CURRENT_REV_WRAPPER));
		SQLExpression where = and(branchEquality, idEquality, maxRevMax);
		SQLUpdate update =
			SQLFactory.update(table(type, tableAlias), where, Arrays.asList(revMaxAttr.getDBName()),
				Arrays.<SQLExpression> asList(param(revMaxAttr)));
		List<Parameter> parameters = Arrays.asList(
			paramDef(revMaxAttr),
			parameterDef(branchDBType, branchParamName),
			paramDef(revMinAttr));
		return query(parameters, update).toSql(sqlDialect);
	}

	private SQLParameter param(DBAttribute attr) {
		return parameter(attr, attr.getDBName());
	}

	private SQLSetParameter setParam(DBAttribute attr) {
		return setParameter(attr.getDBName(), attr.getSQLType());
	}

	private Parameter paramDef(DBAttribute attr) {
		return parameterDef(attr, attr.getDBName());
	}

	private Parameter setParamDef(DBAttribute attr) {
		return setParameterDef(attr.getDBName(), attr);
	}

	int outdateItem(Connection connection, Long lastRev, KnowledgeItemInternal object) throws SQLException {
		DBObjectKey key = object.tId();
		Object objectName = key.getObjectName();
		long branch = key.getBranchContext();
		return _outdateStatement.executeUpdate(connection, lastRev, branch, objectName, object.getLastUpdate());
	}

	void outdateItem(Batch batch, Long lastRev, KnowledgeItemInternal object) throws SQLException {
		DBObjectKey key = object.tId();
		batch.addBatch(lastRev, key.getBranchContext(), key.getObjectName(), object.getLastUpdate());
	}
	
	@Override
	protected ResultSet fetchData(PooledConnection db, long branchContext, TLID id, long dataRevision)
			throws SQLException {
		if (dataRevision != Revision.CURRENT_REV) {
			return _fetchHistoricStatement.executeQuery(db, branchContext, id, dataRevision, dataRevision);
		} else {
			return _fetchCurrentStatement.executeQuery(db, branchContext, id, dataRevision);
		}
	}

	@Override
	protected ResultSet fetchDataOrdered(PooledConnection db, long branchContext, Collection<?> ids, long dataRevision)
			throws SQLException {
		assert ids.size() <= db.getSQLDialect().getMaxSetSize();
		if (dataRevision != Revision.CURRENT_REV) {
			return _fetchAllHistoricStatement.executeQuery(db, branchContext, ids, dataRevision, dataRevision);
		} else {
			return _fetchAllCurrentStatement.executeQuery(db, branchContext, ids, dataRevision);
		}
	}

	@Override
	protected void internalBranch(PooledConnection db, long branchId, long createRev, long baseBranchId,
			long baseRevision, SQLExpression filterExpr) throws SQLException {
		String tableAlias = NO_TABLE_ALIAS;
		SQLTable table = table(type.getDBName(), tableAlias);
		String sourceTableAlias = NO_TABLE_ALIAS;
		DBAttribute branchAttribute = _branchAttribute;
		DBAttribute revMinAttribute = _revMinAttribute;
		DBAttribute revMaxAttribute = _revMaxAttribute;
		SQLExpression branchCheck =
			eqSQL(column(sourceTableAlias, branchAttribute.getDBName()), param(branchAttribute));
		SQLExpression revCheck = and(
			le(column(sourceTableAlias, revMinAttribute.getDBName()), param(revMinAttribute)),
			ge(column(sourceTableAlias, revMaxAttribute.getDBName()), param(revMaxAttribute))
			);

		SQLExpression where = and(
			branchCheck, revCheck, filterExpr
			);
		List<String> insertCols = new ArrayList<>();
		List<SQLColumnDefinition> selectCols = new ArrayList<>();
		insertCols.add(branchAttribute.getDBName());
		selectCols.add(columnDef(literalLong(branchId), sourceTableAlias));
		insertCols.add(revMinAttribute.getDBName());
		selectCols.add(columnDef(literalLong(createRev), sourceTableAlias));
		insertCols.add(revMaxAttribute.getDBName());
		selectCols.add(columnDef(literalLong(Revision.CURRENT_REV), sourceTableAlias));
		for (DBAttribute dbAttr : type.getDBAttributes()) {
			String columnName = dbAttr.getDBName();
			if (columnName.equals(branchAttribute.getDBName()) ||
				columnName.equals(revMinAttribute.getDBName()) ||
				columnName.equals(revMaxAttribute.getDBName())) {
				continue;
			}
			insertCols.add(columnName);
			selectCols.add(columnDef(column(sourceTableAlias, columnName), columnName));
		}

		SQLSelect select =
			select(
				selectCols,
				table(type, sourceTableAlias),
				where,
				noOrder());
		List<Parameter> parameters;
		parameters = parameters(
			paramDef(branchAttribute),
			paramDef(revMinAttribute),
			paramDef(revMaxAttribute)
			);

		CompiledStatement query =
			query(parameters, SQLFactory.insert(table, insertCols, select)).toSql(sqlDialect);
		query.executeUpdate(db, baseBranchId, baseRevision, baseRevision);
	}

	private void outdateAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects)
			throws SQLException {
		assert objects.size() >= 2 : "Using an in-query for less than 2 elements is inappropriate.";
		Long lastRev = Long.valueOf(commitNumber - 1);

		DBObjectKey[] tmp = new DBObjectKey[objects.size()];
		for (int n = 0; n < tmp.length; n++) {
			DBKnowledgeItem object = objects.get(n);

			if (object.wasConcurrentlyChanged(lastRev)) {
				throw createLostUpdateException(object);
			}
			tmp[n] = object.tId();
		}
		int maxSetSize = db.getSQLDialect().getMaxSetSize();
		List<TLID> ids = new ArrayList<>();

		long branch = tmp[0].getBranchContext();
		ids.add(tmp[0].getObjectName());
		for (int n = 1; n < tmp.length; n++) {
			DBObjectKey o = tmp[n];
			if (o.getBranchContext() != branch) {
				outdateItems(db, branch, lastRev, ids);
				branch = o.getBranchContext();
			}
			ids.add(o.getObjectName());
			if (ids.size() >= maxSetSize) {
				outdateItems(db, branch, lastRev, ids);
			}
		}
		if (!ids.isEmpty()) {
			outdateItems(db, branch, lastRev, ids);
		}

	}

	private void outdateItems(PooledConnection db, long branch, Long newRevMax, List<TLID> ids) throws SQLException {
		CompiledStatement outdateInStatement = createOutdateInSetStatement(ids);
		outdateInStatement.executeUpdate(db, newRevMax, branch, ids);
		ids.clear();
	}
	
	private void outdate(Connection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		int updateCnt = outdateItem(db, commitNumber - 1, object);
		if (updateCnt != 1) {
			if (updateCnt == 0) {
				throw createLostUpdateException(object);
			} else {
				StringBuilder msg = new StringBuilder();
				msg.append("More than one row affected: rows=");
				msg.append(updateCnt);
				msg.append(", type=");
				msg.append(object.tTable().getName());
				throw new AssertionError(msg.toString());
			}
		}
	}

	@Override
	public void updateAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects) throws SQLException {
		switch (objects.size()) {
			case 0: {
				// nothing to update
				break;
			}
			case 1: {
				// no need to create batch for only one element.
				update(db, commitNumber, objects.get(0));
				break;
			}
			default: {
				// Optimized batched access.
				outdateAll(db, commitNumber, objects);
				storeAll(db, commitNumber, objects);
			}
		}
	}

	@Override
	public void update(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		// Note: A new row must be inserted, no matter whether row attributes
		// have changed or not. Otherwise, lost updates cannot reliably be
		// detected in case of only changes of flexible and/or row attributes.
		// An alternative would be to introduce an explicit (potentially
		// flexible) attribute that stores the last update revision of the
		// object (no matter where the update occurred, row attributes, flexible
		// attributes, associations).
		//
		//		// Row data and flex data share the same modification marker. Prevent
		//		// inserting duplicate lines, if only flex data has changed.
		//		if (object.hasKOAttributeModifications()) {
		//			outdate(db, dbHelper, commitNumber, object);
		//			insert(db, dbHelper, commitNumber, object);
		//		} else {
		//			PreparedStatement stmt = this.checkUpdate.prepare(db);
		//			int rows = this.checkUpdate.query(stmt, 
		//				object.getBranchContext().longValue(), (String) object.getObjectName(), object.getLastUpdate().longValue());
		//			if (rows != 1) {
		//				if (rows == 0) {
		//					throw new SQLException("Object could not be updated (preventing lost update): type=" + object.getMetaObject().getName());
		//				} else {
		//					throw new AssertionError("More than one row found: rows=" + rows + ", type=" + object.getMetaObject().getName());
		//				}
		//			}
		//		}
		outdate(db, commitNumber, object);
		store(db, commitNumber, object);
	}
	
	@Override
	public void insertAll(PooledConnection db, long commitNumber, List<? extends AbstractDBKnowledgeItem> objects) throws SQLException {
		storeAll(db, commitNumber, objects);
	}

	private void storeAll(PooledConnection db, long commitNumber, List<? extends AbstractDBKnowledgeItem> objects)
			throws SQLException {
		for (int n = 0, cnt = objects.size(); n < cnt; n++) {
			AbstractDBKnowledgeItem object = objects.get(n);
			
			// Prevent NullPointerException, if object was concurrently deleted and
			// deletion was fetched before commit.
			checkAlive(object);
			
			// Make sure that the new revision is used as minimum validity in the new row.
			((DBKnowledgeItem) object).setLastUpdateLocal(commitNumber);
		}

		super.insertAll(db, commitNumber, objects);
	}

	private static void checkAlive(KnowledgeItem object) throws SQLException {
		if (! object.isAlive()) {
			throw new SQLException("Concurrent delete, commit aborted.");
		}
	}
	
	@Override
	public void insert(PooledConnection db, long commitNumber, AbstractDBKnowledgeItem object) throws SQLException {
		DBKnowledgeItem item = (DBKnowledgeItem) object;
		store(db, commitNumber, item);
	}

	private void store(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		// Prevent NullPointerException, if object was concurrently deleted and
		// deletion was fetched before commit.
		checkAlive(object);

		// Make sure that the new revision is used as minimum validity in the new row.
		object.setLastUpdateLocal(commitNumber);
		
		super.insert(db, commitNumber, object);
	}
	
	@Override
	public void delete(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		outdate(db, commitNumber, object);
	}
	
	@Override
	public void deleteAll(PooledConnection db, long commitNumber, List<? extends DBKnowledgeItem> objects) throws SQLException {
		switch (objects.size()) {
			case 0: {
				// nothing to delete
				break;
			}
			case 1: {
				// no need to create batch for only one element.
				delete(db, commitNumber, objects.get(0));
				break;
			}
			default: {
				// Optimized batched access.
				outdateAll(db, commitNumber, objects);
			}
		}
	}
	
	@Override
	protected CompiledStatement getInsertStatement() {
		return _insertStatement;
	}

	@Override
	protected CompiledStatement getUpdateStatement() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected CompiledStatement getDeleteStatement() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates an expression to access the column {@link BasicTypes#BRANCH_ATTRIBUTE_NAME} of this
	 * type.
	 * 
	 * @see DBAccess#createBranchExpr(TableSymbol)
	 */
	@Override
	public SQLExpression createBranchExpr(TableSymbol table) {
		assert type == table.getType();
		if (type.getDBMapping().multipleBranches()) {
			return createColumn(_branchAttribute, table);
		} else {
			return SQLFactory.literalLong(TLContext.TRUNK_ID);
		}
	}

	@Override
	protected DBAttribute id() {
		return _idAttribute;
	}

	/**
	 * Creates an expression to access the column {@link BasicTypes#REV_MAX_ATTRIBUTE_NAME} of this
	 * type.
	 * 
	 * @see DBAccess#createRevMaxExpr(TableSymbol)
	 */
	@Override
	public SQLExpression createRevMaxExpr(TableSymbol table) {
		return createColumn(_revMaxAttribute, table);
	}

	/**
	 * Creates an expression to access the column {@link BasicTypes#REV_MIN_ATTRIBUTE_NAME} of this
	 * type.
	 * 
	 * @see DBAccess#createRevMinExpr(TableSymbol)
	 */
	@Override
	public SQLExpression createRevMinExpr(TableSymbol table) {
		return createColumn(_revMinAttribute, table);
	}

}
