/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.diff;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.db2.CollationHintComputation;
import com.top_logic.knowledge.service.db2.ItemResult;
import com.top_logic.knowledge.service.db2.QueryResult;

/**
 * Query rows where contents has changed or a new row was created from (
 * {@link #getSourceBranch()}, {@link #getSourceRev()}) to (
 * {@link #getDestBranch()}, {@link #getDestRev()}).
 * 
 * <p>
 * Note: Deleted rows (which exist in the {@link #getSourceRev()}, but not in
 * {@link #getDestRev()}) are not reported.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDiffUpdateQuery extends AbstractDiffQuery {

	private CompiledStatement compiledStatement;


	/**
	 * Creates an {@link AbstractDiffUpdateQuery}.
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
	protected AbstractDiffUpdateQuery(DBHelper sqlDialect, MOClass type, long sourceBranch, long sourceRev, long destBranch, long destRev) {
		super(sqlDialect, type, sourceBranch, sourceRev, destBranch, destRev);
	}

	protected void init() {
		this.compiledStatement = createStatement();
	}

	private CompiledStatement createStatement() {
		String sourceAlias = "s";
		String destAlias = "d";
		boolean multipleBranches = multipleBranches();
		
		List<SQLColumnDefinition> columns = new ArrayList<>();
		List<MOAttribute> attributes = type.getAttributes();
		for (MOAttribute attribute : attributes) {
			for (DBAttribute dbAttribute : attribute.getDbMapping()) {
				columns.add(columnDef(column(destAlias, dbAttribute.getDBName()), "d_" + dbAttribute.getDBName()));
				
			}
		}
		for (MOAttribute attribute : attributes) {
			for (DBAttribute dbAttribute : attribute.getDbMapping()) {
				columns.add(columnDef(column(sourceAlias, dbAttribute.getDBName()), "s_" + dbAttribute.getDBName()));
			}
		}
		
		SQLExpression matchingRow = SQLBoolean.TRUE; 
		if (multipleBranches) {
			SQLExpression branchEq = eq(notNullColumn(sourceAlias, getBranchColumnName()), literalLong(sourceBranch));
			matchingRow = and(matchingRow, branchEq);
		}
		for (String idColumnName : getIdColumnNames()) {
			matchingRow =
				and(matchingRow,
					eq(notNullColumn(sourceAlias, idColumnName), notNullColumn(destAlias, idColumnName)));
		}
		matchingRow =
			and(matchingRow, le(notNullColumn(sourceAlias, getRevMinColumnName()), literalLong(sourceRev)));
		matchingRow =
			and(matchingRow, le(literalLong(sourceRev), notNullColumn(sourceAlias, getRevMaxColumnName())));

		SQLExpression where = SQLBoolean.TRUE;
		if (multipleBranches) {
			SQLExpression branchEq = eq(notNullColumn(destAlias, getBranchColumnName()), literalLong(destBranch));
			where = and(where, branchEq);
		}
		where = and(where, le(notNullColumn(destAlias, getRevMinColumnName()), literalLong(destRev)));
		where = and(where, le(literalLong(destRev), notNullColumn(destAlias, getRevMaxColumnName())));
		// must restrict on destAlias since if it is a creation everything in
		// the sourceAlias is null
		where = and(where, restrictResult(destAlias));
		
		SQLExpression hasDifference = isNull(column(sourceAlias, getRevMaxColumnName())); // Creation
		for (int n = 0, cnt = attributes.size(); n < cnt; n++) {
			MOAttribute attribute = attributes.get(n);
			if (attribute.isSystem()) {
				continue;
			}
			for (DBAttribute dbAttr : attribute.getDbMapping()) {
				boolean mandatoryAttribute = dbAttr.isSQLNotNull();
				/* can use not null also in right part of join because empty result is found because
				 * of null check of branch */
				boolean notNull = mandatoryAttribute ? NOT_NULL : !NOT_NULL;
				// Equal data in source and destination revision.
				SQLExpression differentColumnData = SQLBoolean.FALSE;
				if (!mandatoryAttribute) {
					// Both values not defined.
					differentColumnData =
						or(
							differentColumnData,
							and(
								isNull(column(sourceAlias, dbAttr.getDBName())),
								not(isNull(column(destAlias, dbAttr.getDBName())))));

					differentColumnData =
						or(
							differentColumnData,
							and(
								not(isNull(column(sourceAlias, dbAttr.getDBName()))),
								isNull(column(destAlias, dbAttr.getDBName()))));
				}

				// Large objects are not comparable within the DB.
				boolean comparable =
					sqlDialect.isComparable(
						dbAttr.getSQLType(),
						dbAttr.getSQLSize(), dbAttr.getSQLPrecision(),
						dbAttr.isSQLNotNull(), dbAttr.isBinary());
				if (comparable) {
					differentColumnData =
						or(
							differentColumnData,
							not(eq(
								column(sourceAlias, dbAttr.getDBName(), notNull),
								column(destAlias, dbAttr.getDBName(), notNull))));
				} else {
					// Select all, where both values (new and old) are non-null.
					// Those might have a difference, which is computed on the
					// server. This could be optimized by introducing an MD5/SHA1
					// hash for large objects.
					differentColumnData =
						or(
							differentColumnData,
							and(
								not(isNull(column(sourceAlias, dbAttr.getDBName(), notNull))),
								not(isNull(column(destAlias, dbAttr.getDBName(), notNull)))));
				}

				hasDifference =
					or(hasDifference, differentColumnData);
			}
			
		}
		where = and(where, hasDifference);
		
		List<SQLOrder> orderBy = new ArrayList<>();
		if (multipleBranches) {
			orderBy.add(order(false, column(destAlias, getBranchColumnName())));
		}
		Map<String, DBAttribute> attrByDBName = newDBAttributeMapping();
		for (String idColumnName : getIdColumnNames()) {
			DBAttribute idAttribute = attrByDBName.get(idColumnName);
			CollationHint collationHint = CollationHintComputation.INSTANCE.getCollationHint(idAttribute);
			orderBy.add(order(false, collationHint, column(destAlias, idColumnName)));
		}
		
		SQLTableReference sourceTable = table((DBTableMetaObject) type, sourceAlias);
		SQLTableReference destTable = table((DBTableMetaObject) type, destAlias);
		
		SQLSelect select = select(false, columns, join(false, destTable, sourceTable, matchingRow), where, orderBy);
		
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

	/**
	 * Name of the database column containing the branch attribute.
	 * 
	 * <p>
	 * Must only be called if {@link #getType()} (resp. its {@link MOStructure#getDBMapping()
	 * database representation}) supports multiple branches.
	 * </p>
	 * 
	 * @see DBTableMetaObject#multipleBranches()
	 */
	protected abstract String getBranchColumnName();


	/**
	 * Retrieves all rows from the given connection matching this {@link AbstractDiffUpdateQuery}.
	 */
	public abstract DiffUpdateResult query(Connection connection) throws SQLException;

	protected final ResultSet executeStatement(Connection context) throws SQLException {
		return compiledStatement.executeQuery(context);
	}

	/**
	 * Result of a {@link AbstractDiffUpdateQuery}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public abstract static class DiffUpdateResult<T extends ItemResult> implements QueryResult {

		private T oldValues;
		private T newValues;

		protected DiffUpdateResult(DBHelper sqlDialect, MOClass type, ResultSet commonResultSet)
				throws SQLException {
			oldValues = createOldValues(sqlDialect, type, commonResultSet, ((DBTableMetaObject) type).getDBColumnCount() + 1);
			newValues = createNewValues(sqlDialect, type, commonResultSet, 1);
		}			

		/**
		 * Returns an {@link ItemResult} having the old values.
		 * 
		 * <p>
		 * <b>Do neither call {@link QueryResult#next()} nor
		 * {@link QueryResult#close()}. Call {@link DiffUpdateResult#next()} and
		 * {@link DiffUpdateResult#close()}, instead.</b>
		 * </p>
		 */
		public T getOldValues() {
			return oldValues;
		}
		
		protected abstract T createOldValues(DBHelper sqlDialect, MOClass type, ResultSet wrappedResult, int offset);

		/**
		 * Returns an {@link ItemResult} having the new values.
		 * 
		 * <p>
		 * <b>Do neither call {@link QueryResult#next()} nor
		 * {@link QueryResult#close()}. Call {@link DiffUpdateResult#next()} and
		 * {@link DiffUpdateResult#close()}, instead.</b>
		 * </p>
		 */
		public T getNewValues() {
			return newValues;
		}
		
		protected abstract T createNewValues(DBHelper sqlDialect, MOClass type, ResultSet wrappedResult, int offset);
		
		@Override
		public void close() throws SQLException {
			getOldValues().close();
		}
		
		@Override
		public boolean next() throws SQLException {
			return getOldValues().next();
		}

		/**
		 * Whether the current row represents an object creation (all
		 * {@link #getOldValue(MOAttribute, ObjectContext) old values} are <code>null</code>.
		 */
		public abstract boolean isCreation() throws SQLException;
		
		/**
		 * Retrieve the generic value of the given attribute after the change.
		 */
		public final Object getNewValue(MOAttribute attribute, ObjectContext context) throws SQLException {
			return getNewValues().getValue(attribute, context);
		}
		
		/**
		 * Retrieve the generic value of the given attribute before the change.
		 */
		public final Object getOldValue(MOAttribute attribute, ObjectContext context) throws SQLException {
			return getOldValues().getValue(attribute, context);
		}
	}
}
