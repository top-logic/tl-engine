/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.setParameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.Conversion;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLDelete;
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
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.SQLH;

/**
 * {@link DBAccess} implementation for objects that are not under version
 * control.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class DirectDBAccess extends DefaultDBAccess {
	
	private static final Conversion LONG_ID_CONVERSION = new Conversion() {

		@Override
		public Object convert(Object argument, Map<String, Integer> argumentIndexByName, Object[] arguments) {
			return TLID.STORAGE_VALUE_MAPPING.map((TLID) argument);
		}
	};

	private final CompiledStatement _insertStatement;

	private final CompiledStatement _updateStatement;

	private final CompiledStatement _deleteStatement;

	private final CompiledStatement _fetchItemStatement;

	private final CompiledStatement _fetchAllStatement;

	/*package protected*/ DirectDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		super(sqlDialect, table);

		this._insertStatement = createInsertStatement();
		this._updateStatement = createUpdateStatement();
		this._deleteStatement = createDeleteStatement();
		this._fetchItemStatement = createFetchItemStatement();
		this._fetchAllStatement = createFetchAllStatement();
	}

	private CompiledStatement createInsertStatement() {
		return SQLH.createInsert(sqlDialect, type);
	}

	private CompiledStatement createUpdateStatement() {
		String tableAlias = NO_TABLE_ALIAS;
		List<Parameter> parameters = new ArrayList<>();
		
		SQLUpdate update =
			SQLFactory.update(table(type, tableAlias), getKeyColumns(tableAlias, false), getDataColumnNames(),
				getDataColumnParameters(false));
		addDataColumnParameters(parameters);
		addKeyColumnParameters(parameters);
		
		return query(parameters, update).toSql(sqlDialect);
	}
	
	private List<SQLExpression> getDataColumnParameters(boolean fetch) {
		List<SQLExpression> columnNames = new ArrayList<>();
		for (MOAttribute attribute : dataAttributes) {
			for (DBAttribute dbAttr : attribute.getDbMapping()) {
				columnNames.add(param(dbAttr, fetch));
			}
		}
		return columnNames;
	}

	private SQLParameter param(DBAttribute dbAttr, boolean fetch) {
		return parameter(dbAttr.getSQLType(), conversion(dbAttr, fetch), parameterName(dbAttr));
	}

	protected Conversion conversion(DBAttribute dbAttr, boolean fetch) {
		Conversion conversion;
		if (fetch && dbAttr == id()) {
			conversion = LONG_ID_CONVERSION;
		} else {
			conversion = Conversion.IDENTITY;
		}
		return conversion;
	}

	private SQLSetParameter idSetParam(DBAttribute idAttr, boolean fetch) {
		return setParameter(conversion(idAttr, fetch), parameterName(idAttr), idAttr.getSQLType());
	}

	private Parameter idSetParamDef(DBAttribute idAttr) {
		return setParameterDef(parameterName(idAttr), idAttr);
	}

	private List<String> getDataColumnNames() {
		List<String> columnNames = new ArrayList<>();
		for (MOAttribute attribute : dataAttributes) {
			for (DBAttribute dbAttr : attribute.getDbMapping()) {
				columnNames.add(dbAttr.getDBName());
			}
		}
		return columnNames;
	}

	private CompiledStatement createDeleteStatement() {
		String tableAlias = NO_TABLE_ALIAS; // No table alias allows in delete
		List<Parameter> parameters = new ArrayList<>();
		SQLDelete delete = SQLFactory.delete(table(type, tableAlias), getKeyColumns(tableAlias, false));
		addKeyColumnParameters(parameters);
		return query(parameters, delete).toSql(sqlDialect);
	}
	
	protected final SQLExpression getKeyColumns(String tableAlias, boolean fetch) {
		return eqCheck(tableAlias, keyAttributes, fetch);
	}

	private SQLExpression eqCheck(String tableAlias, MOAttribute[] attributes, boolean fetch) {
		SQLExpression result = and();
		for (MOAttribute attr : attributes) {
			for (DBAttribute dbAttribute : attr.getDbMapping()) {
				SQLExpression column = column(tableAlias, dbAttribute.getDBName());
				SQLExpression parameter = param(dbAttribute, fetch);
				SQLExpression eq = eqSQL(column, parameter);
				result = and(result, eq);
			}
		}
		return result;
	}

	protected void addKeyColumnParameters(List<Parameter> out) {
		addParameters(out, keyAttributes);
	}

	protected void addDataColumnParameters(List<Parameter> out) {
		addParameters(out, dataAttributes);
	}

	private void addParameters(List<Parameter> out, MOAttribute[] attributes) {
		for (MOAttribute attr : attributes) {
			for (DBAttribute dbAttribute : attr.getDbMapping()) {
				out.add(paramDef(dbAttribute));
			}
		}
	}

	private Parameter paramDef(DBAttribute dbAttribute) {
		return parameterDef(dbAttribute, parameterName(dbAttribute));
	}

	private String parameterName(DBAttribute dbAttribute) {
		return dbAttribute.getDBName();
	}

	protected CompiledStatement createFetchItemStatement() {
		String tableAlias = NO_TABLE_ALIAS;
		DBAttribute idAttribute = id();
		SQLSelect select =
			select(columnDefs(tableAlias, type), table(type, tableAlias),
				eqSQL(column(tableAlias, idAttribute.getDBName()), param(idAttribute, true)));
		List<Parameter> parameters = parameters(paramDef(idAttribute));
		return query(parameters, select).toSql(sqlDialect);
	}

	protected CompiledStatement createFetchAllStatement() {
		String tableAlias = NO_TABLE_ALIAS;
		DBAttribute idAttribute = id();
		List<SQLColumnDefinition> columns = columnDefs(tableAlias, type);
		SQLTable table = table(type, tableAlias);
		SQLExpression where = inSet(column(tableAlias, idAttribute.getDBName()), idSetParam(idAttribute, true));
		List<SQLOrder> order = orders(order(false, column(tableAlias, idAttribute, NOT_NULL)));
		SQLSelect select = select(columns, table, where, order);
		List<Parameter> parameters = parameters(idSetParamDef(idAttribute));
		return query(parameters, select).toSql(sqlDialect);
	}

	@Override
	protected ResultSet fetchData(PooledConnection db, long branchContext, TLID id, long dataRevision)
			throws SQLException {
		return _fetchItemStatement.executeQuery(db, id);
	}

	@Override
	protected ResultSet fetchDataOrdered(PooledConnection db, long branchContext, Collection<?> ids, long dataRevision)
			throws SQLException {
		assert ids.size() <= db.getSQLDialect().getMaxSetSize();
		return _fetchAllStatement.executeQuery(db, ids);
	}

	@Override
	protected final CompiledStatement getInsertStatement() {
		return _insertStatement;
	}
	
	@Override
	protected final CompiledStatement getDeleteStatement() {
		return _deleteStatement;
	}

	@Override
	protected final CompiledStatement getUpdateStatement() {
		return _updateStatement;
	}

	@Override
	protected void internalBranch(PooledConnection db, long branchId, long createRev, long baseBranchId,
			long baseRevision, SQLExpression filterExpr) throws SQLException {
		//  TODO BHU: Implement me!
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	protected DBAttribute id() {
		return attribute(getIdAttributeName());
	}

	protected abstract String getIdAttributeName();

}
