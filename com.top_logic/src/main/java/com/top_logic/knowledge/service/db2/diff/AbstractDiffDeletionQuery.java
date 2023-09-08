/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.db2.CollationHintComputation;
import com.top_logic.knowledge.service.db2.ResultSetWrapper;
import com.top_logic.util.TLContext;

/**
 * Query rows that was deleted from ({@link #getSourceBranch()},
 * {@link #getSourceRev()}) to ({@link #getDestBranch()}, {@link #getDestRev()}
 * ).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDiffDeletionQuery extends AbstractDiffQuery {

	private CompiledStatement compiledStatement;


	/**
	 * Creates an {@link AbstractDiffDeletionQuery}.
	 * 
	 * @param sqlDialect
	 *        See {@link #getSqlDialect()}
	 * @param type
	 *        See {@link #getType()}
	 * @param sourceBranch
	 *        See {@link #getSourceBranch()}
	 * @param sourceRev
	 *        See {@link #getSourceRev()}
	 * @param destBranch
	 *        See {@link #getDestBranch()}
	 * @param destRev
	 *        See {@link #getDestRev()}
	 */
	protected AbstractDiffDeletionQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev, long destBranch, long destRev) {
		super(sqlDialect, type, sourceBranch, sourceRev, destBranch, destRev);
	}

	protected void init() {
		this.compiledStatement = createStatement();
	}

	private CompiledStatement createStatement() {
		String sourceAlias = "s";
		String destAlias = "d";
		boolean multipleBranches = multipleBranches();
		
		List<SQLColumnDefinition> columns = allColumnRefs(type, sourceAlias);

		SQLExpression matchingRow = SQLBoolean.TRUE; 
		if (multipleBranches) {
			SQLExpression branchEQ = eq(notNullColumn(destAlias, getBranchColumnName()), literalLong(destBranch));
			matchingRow = and(matchingRow, branchEQ);
		}
		for (String idColumnName : getIdColumnNames()) {
			matchingRow =
				and(matchingRow,
					eq(notNullColumn(destAlias, idColumnName), notNullColumn(sourceAlias, idColumnName)));
		}
		matchingRow = and(matchingRow, le(notNullColumn(destAlias, getRevMinColumnName()), literalLong(destRev)));
		matchingRow = and(matchingRow, ge(notNullColumn(destAlias, getRevMaxColumnName()), literalLong(destRev)));

		SQLExpression where = SQLBoolean.TRUE;
		if (multipleBranches) {
			SQLExpression branchEq = eq(notNullColumn(sourceAlias, getBranchColumnName()), literalLong(sourceBranch));
			where = and(where, branchEq);
		}
		where = and(where, le(notNullColumn(sourceAlias, getRevMinColumnName()), literalLong(sourceRev)));
		where = and(where, ge(notNullColumn(sourceAlias, getRevMaxColumnName()), literalLong(sourceRev)));
		where = and(where, isNull(column(destAlias, getRevMaxColumnName())));
		where = and(where, restrictResult(sourceAlias));
		
		List<SQLOrder> orderBy = new ArrayList<>();
		if (multipleBranches) {
			orderBy.add(order(false, column(sourceAlias, getBranchColumnName())));
		}

		Map<String, DBAttribute> attrByDBName = newDBAttributeMapping();
		for (String idColumnName : getIdColumnNames()) {
			DBAttribute idAttribute = attrByDBName.get(idColumnName);
			CollationHint collationHint = CollationHintComputation.INSTANCE.getCollationHint(idAttribute);
			orderBy.add(order(false, collationHint, column(sourceAlias, idColumnName)));
		}
		
		SQLTableReference sourceTable = table((DBTableMetaObject) type, sourceAlias);
		SQLTableReference destTable = table((DBTableMetaObject) type, destAlias);
		
		SQLSelect select = select(false, columns, join(false, sourceTable, destTable, matchingRow), where, orderBy);
		
		Map<String, Integer> noArguments = Collections.emptyMap();
		return SQLQuery.toSql(sqlDialect, select, noArguments);
	}

	/**
	 * Expression will be used to restrict the result of the query, i.e. the
	 * returned condition will be used in 'where' clause of the select
	 * 
	 * @param tableAlias
	 *        the alias of the table to define {@link SQLExpression}
	 */
	protected SQLExpression restrictResult(String tableAlias) {
		return SQLBoolean.TRUE;
	}

	protected abstract String[] getIdColumnNames();
	
	protected abstract String getRevMaxColumnName();

	protected abstract String getRevMinColumnName();

	protected abstract String getBranchColumnName();


	/**
	 * Retrieves all rows from the given connection matching this {@link AbstractDiffDeletionQuery}.
	 */
	public abstract DiffDeletionResult query(Connection connection) throws SQLException;
		
	protected final ResultSet executeStatement(Connection context) throws SQLException {
		return compiledStatement.executeQuery(context);
	}

	/**
	 * Result of a {@link AbstractDiffDeletionQuery}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public abstract static class DiffDeletionResult extends ResultSetWrapper {
		
		private static final int NO_BRANCH_COLUMN = -1;

		final int _identifierIndex;

		final int _branchIndex;

		/** SQL dialect that was used to create the {@link ResultSetWrapper#resultSet}. */
		protected final DBHelper _sqlDialect;

		/* package protected */DiffDeletionResult(DBHelper sqlDialect, ResultSet resultSet, MOClass type,
				String branchAttr, String idAttr) {
			super(resultSet);
			_sqlDialect = sqlDialect;
			if (type.getDBMapping().multipleBranches()) {
				DBAttribute branchAttribute = (MOAttributeImpl) type.getAttributeOrNull(branchAttr);
				_branchIndex = DBAttribute.DEFAULT_DB_OFFSET + branchAttribute.getDBColumnIndex();
			} else {
				_branchIndex = NO_BRANCH_COLUMN;
			}
			DBAttribute idAttribute = (MOAttributeImpl) type.getAttributeOrNull(idAttr);
			_identifierIndex = DBAttribute.DEFAULT_DB_OFFSET + idAttribute.getDBColumnIndex();

		}
		
		/**
		 * Retrieve branch ID of the deleted object.
		 */
		public final long getBranchId() throws SQLException {
			if (_branchIndex != NO_BRANCH_COLUMN) {
				return this.resultSet.getLong(_branchIndex);
			} else {
				return TLContext.TRUNK_ID;
			}
		}
		
		/**
		 * Retrieve identifier of the deleted object.
		 */
		public final TLID getObjectName() throws SQLException {
			return IdentifierUtil.getId(this.resultSet, _identifierIndex);
		}
		
		/**
		 * Retrieve type of the deleted object.
		 */
		public abstract String getTypeName() throws SQLException;

	}

}
