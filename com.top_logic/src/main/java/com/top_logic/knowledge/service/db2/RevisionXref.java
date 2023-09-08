/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.table;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;
import static java.util.Arrays.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * Access to the xref table.
 */
public class RevisionXref {

	/**
	 * Fires an insert into the {@link #REVISION_XREF_TYPE_NAME} table.
	 */
	public static class InsertXref implements AutoCloseable {
		
		private final Batch _batch;
		
		private final int _maxBatchSize;
	
		private int _numberBatches;
		
		/**
		 * Creates a {@link InsertXref}.
		 * 
		 * <p>
		 * This must be {@link InsertXref#close() closed} after finished.
		 * </p>
		 */
		public InsertXref(DBKnowledgeBase kb, Connection connection) throws SQLException {
			DBHelper sqlDialect = kb.dbHelper;
			CompiledStatement sql =
				createInsertStatement(sqlDialect, kb.lookupType(REVISION_XREF_TYPE_NAME));
			_batch = sql.createBatch(connection);
			_maxBatchSize = sqlDialect.getMaxBatchSize(3);
		}
	
		private CompiledStatement createInsertStatement(DBHelper sqlDialect, MOKnowledgeItem xRefType) {
			String tableAlias = NO_TABLE_ALIAS;
			SQLTable table = table(xRefType, tableAlias);
			DBAttribute revRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_REV_ATTRIBUTE);
			DBAttribute branchRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_BRANCH_ATTRIBUTE);
			DBAttribute typeRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_TYPE_ATTRIBUTE);
			List<String> columnNames = Arrays.asList(
				revRef.getDBName(),
				branchRef.getDBName(),
				typeRef.getDBName()
				);
			List<? extends SQLExpression> values = Arrays.asList(
				parameter(revRef, revRef.getDBName()),
				parameter(branchRef, branchRef.getDBName()),
				parameter(typeRef, typeRef.getDBName())
				);
			SQLInsert insert = insert(table, columnNames, values);
			List<Parameter> parameters = parameters(
				parameterDef(revRef, revRef.getDBName()),
				parameterDef(branchRef, branchRef.getDBName()),
				parameterDef(typeRef, typeRef.getDBName())
				);
			return query(parameters, insert).toSql(sqlDialect);
		}
	
		/**
		 * Sets the parameters of this statement.
		 * @param rev
		 *        The revision for which the cross reference link is created.
		 * @param type
		 *        The {@link MetaObject#getName()} of the touched object.
		 * @param branch
		 *        The branch in which the change happened.
		 */
		private void setParams(Batch batch, long rev, String type, long branch) throws SQLException {
			batch.addBatch(rev, branch, type);
		}
	
		/**
		 * Sets the parameters of this {@link InsertXref} and adds a batch to the statement.
		 * 
		 * <p>
		 * If maximal number of batches reached, the statement is executed.
		 * </p>
		 * 
		 * @param rev
		 *        The revision for which the cross reference link is created.
		 * @param type
		 *        The {@link MetaObject#getName()} of the touched object.
		 * @param branch
		 *        The branch in which the change happened.
		 * @see InsertXref#executeBatch()
		 */
		public void addBatch(long rev, String type, long branch) throws SQLException {
			setParams(_batch, rev, type, branch);
			_numberBatches++;
			if (_numberBatches >= _maxBatchSize) {
				executeBatch();
			}
		}
	
		/**
		 * Executes the batches formerly added via {@link #addBatch(long, String, long)}
		 * 
		 * @see InsertXref#addBatch(long, String, long)
		 */
		public void executeBatch() throws SQLException {
			if (_numberBatches > 0) {
				_batch.executeBatch();
				_numberBatches = 0;
			}
		}
	
		/**
		 * Closes this {@link InsertXref} and releases all resources.
		 */
		@Override
		public void close() throws SQLException {
			_batch.close();
		}
	}

	/**
	 * Query that reports objects touched in a range of revisions.
	 * 
	 * <p>
	 * This query is used during refetch in a cluster to find objects modified since the last local
	 * commit.
	 * </p>
	 * 
	 * @see XrefResult
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class RefetchXref {
		
		private final CompiledStatement _statement;
		
		final int revResultIdx;
		final int typeResultIdx;
		final int branchResultIdx;
	
		/**
		 * Creates a {@link RefetchXref}.
		 */
		public RefetchXref(DBKnowledgeBase kb) {
			String tableAlias = NO_TABLE_ALIAS;
			MOKnowledgeItemImpl xRefType = kb.lookupType(REVISION_XREF_TYPE_NAME);
			DBAttribute revRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_REV_ATTRIBUTE);
			DBAttribute typeRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_TYPE_ATTRIBUTE);
			DBAttribute branchRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_BRANCH_ATTRIBUTE);
			List<SQLColumnDefinition> columns = new java.util.ArrayList<>();
			columns.add(columnDef(column(tableAlias, revRef.getDBName()), revRef.getDBName()));
			revResultIdx = columns.size();
			columns.add(columnDef(column(tableAlias, typeRef.getDBName()), typeRef.getDBName()));
			typeResultIdx = columns.size();
			columns.add(columnDef(column(tableAlias, branchRef.getDBName()), branchRef.getDBName()));
			branchResultIdx = columns.size();
			SQLTableReference from = table(xRefType, tableAlias);
			SQLExpression where = and(
				ge(column(tableAlias, revRef.getDBName()), parameter(revRef, "firstRev")),
				le(column(tableAlias, revRef.getDBName()), parameter(revRef, "lastRev"))
				);
			List<SQLOrder> orderBy = orders(
				order(false, column(tableAlias, revRef.getDBName())),
				order(false, column(tableAlias, branchRef.getDBName())),
				order(false, column(tableAlias, typeRef.getDBName()))
				);
			SQLSelect select = select(columns, from, where, orderBy);
			List<Parameter> parameters = parameters(
				parameterDef(revRef, "firstRev"),
				parameterDef(revRef, "lastRev")
				);
			this._statement = SQLFactory.query(parameters, select).toSql(kb.dbHelper);
		}
	
		/**
		 * Construct a {@link XrefResult} for a refetch in the given revision
		 * range.
		 * 
		 * @param connection
		 *        The connection to read from.
		 * @param firstRev
		 *        The first revision (inclusive).
		 * @param lastRev
		 *        The last revision (inclusive).
		 * @return The {@link XrefResult} to read data from for executing a
		 *         refetch.
		 */
		public XrefResult query(Connection connection, long firstRev, long lastRev) throws SQLException {
			ResultSet result = _statement.executeQuery(connection, firstRev, lastRev);
			return new XrefResult(result);
		}
		
		/**
		 * Result of a {@link RefetchXref} query. 
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public class XrefResult extends ResultSetWrapper {
			/* package protected */XrefResult(ResultSet result) {
				super(result);
			}
			
			/**
			 * The revision for which information is retrieved.
			 */
			public long getRevision() throws SQLException {
				return resultSet.getLong(revResultIdx);
			}
			
			/**
			 * The type name for which information is retrieved.
			 */
			public String getType() throws SQLException {
				return resultSet.getString(typeResultIdx);
			}
			
			/**
			 * The branch in which information is retrieved.
			 */
			public long getBranch() throws SQLException {
				return resultSet.getLong(branchResultIdx);
			}
			
		}
	}

	/**
	 * Result of a {@link RevisionXref} query.
	 */
	public static class TypeResultImpl extends ResultSetWrapper implements TypeResult {
		
		private final int typeIdx;
	
		/**
		 * Creates a {@link TypeResultImpl}.
		 */
		TypeResultImpl(ResultSet resultSet, int typeIdx) {
			super(resultSet);
			this.typeIdx = typeIdx;
		}
	
		@Override
		public String getType() throws SQLException {
			return resultSet.getString(typeIdx);
		}
	}

	/**
	 * The type name of objects touched in the revision.
	 * 
	 * @see #XREF_REV_ATTRIBUTE
	 */
	public static final String XREF_TYPE_ATTRIBUTE = "type";

	/**
	 * The branch on which objects of a certain type were touched.
	 * 
	 * @see #XREF_TYPE_ATTRIBUTE
	 */
	public static final String XREF_BRANCH_ATTRIBUTE = "branch";

	/**
	 * The commit number of the revision this <code>xrev</code> entry is about.
	 */
	public static final String XREF_REV_ATTRIBUTE = "rev";

	/**
	 * The type name of the <code>xref</code> type.
	 */
	public static final String REVISION_XREF_TYPE_NAME = "RevisionXref";

	static long _treshold = 400; // randomly chosen

	/**
	 * Creates a new {@link TypeResult} from the given parameters
	 * 
	 * @param kb
	 *        The database to receive type names from
	 * @param readConnection
	 *        The database connection to use.
	 * @param firstRev
	 *        The first revision (inclusive) of interest.
	 * @param lastRev
	 *        The last revision (inclusive) of interest. Must be &gt;= <code>firstRev</code>
	 * @param typeNameFilter
	 *        Optional set of not <code>null</code> type names to which the result is
	 *        restricted. <code>null</code> for no additional filter.
	 * @param branchIds
	 *        Optional set of not <code>null</code> branch ids to which the result is
	 *        restricted. <code>null</code> for no additional filter.
	 * 
	 * @return The {@link TypeResult result} of this query.
	 */
	public static TypeResult createTypeResult(DBKnowledgeBase kb, Connection readConnection, long firstRev,
			long lastRev, Set<String> typeNameFilter, Set<Long> branchIds) throws SQLException {
		if (lastRev - firstRev > _treshold) {
			return createAllTypesResult(kb, typeNameFilter);
		} else {
			MOKnowledgeItemImpl xRefType = kb.lookupType(REVISION_XREF_TYPE_NAME);
			DBAttribute typeRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_TYPE_ATTRIBUTE);
			DBAttribute branchRef =
				(MOAttributeImpl) xRefType.getAttributeOrNull(XREF_BRANCH_ATTRIBUTE);
			DBAttribute revRef = (MOAttributeImpl) xRefType.getAttributeOrNull(XREF_REV_ATTRIBUTE);
			String typeColumn = typeRef.getDBName();
			String branchColumn = branchRef.getDBName();
			String xrefTable = xRefType.getDBName();
	
			String firstRevParam = "firstRev";
			String lastRevParam = "lastRev";
	
			String x = "x";
			String y = "y";
			String yType = "type1";
	
			SQLQuery<SQLSelect> query = SQLFactory.query(
				asList(
					parameterDef(revRef, firstRevParam),
					parameterDef(revRef, lastRevParam)),
	
				select(
					false,
					asList(columnDef(notNullColumn(y, yType), XREF_TYPE_ATTRIBUTE)),
	
					subQuery(
						select(
							true,
							asList(columnDef(notNullColumn(x, typeColumn), yType)),
							table(xrefTable, x),
							and(
								ge(column(x, revRef, true), parameter(revRef, firstRevParam)),
								le(column(x, revRef, true), parameter(revRef, lastRevParam)),
								typeNameFilter != null
									? inSet(notNullColumn(x, typeColumn), typeNameFilter, typeRef.getSQLType())
									: SQLBoolean.TRUE,
								branchIds != null
									? inSet(notNullColumn(x, branchColumn), branchIds, branchRef.getSQLType())
									: SQLBoolean.TRUE),
							Collections.<SQLOrder> emptyList()),
						y),
	
					SQLBoolean.TRUE,
					asList(
						order(false,
							CollationHint.BINARY,
							/* Currently, oder of types is important as sorting of events
							 * depends on it. See Ticket #2918. */
						notNullColumn(y, yType)))
				));
	
			CompiledStatement statement = query.toSql(kb.dbHelper);
	
			ResultSet resultSet = statement.executeQuery(readConnection, firstRev, lastRev);
			return new RevisionXref.TypeResultImpl(resultSet, 1);
		}
	}

	/**
	 * Returns a {@link TypeResult} that ignores the {@link #REVISION_XREF_TYPE_NAME} table and
	 * returns all types available in system.
	 */
	public static TypeResult createAllTypesResult(DBKnowledgeBase kb, Set<String> typeNameFilter) {
		final MORepository moRepository = kb.getMORepository();
		MOKnowledgeItemImpl itemType = kb.lookupType(BasicTypes.ITEM_TYPE_NAME);
		final Collection<? extends MetaObject> allTypes = moRepository.getMetaObjects();
		List<String> relevantTypes = new java.util.ArrayList<>(allTypes.size());
		for (MetaObject currentType : allTypes) {
			if (MetaObjectUtils.isAbstract(currentType)) {
				// ignore abstract and system types
				continue;
			}
			if (currentType instanceof MOKnowledgeItem) {
				// ignore system types
				final MOKnowledgeItem moKnowledgeItem = (MOKnowledgeItem) currentType;
				if (moKnowledgeItem.isSystem()) {
					continue;
				}
			}
			if (!currentType.isSubtypeOf(itemType)) {
				/* Ignore non item types. They may not have REV_MAX, REV_MIN, or BRANCH
				 * column. */
				continue;
			}
	
			String typeName = currentType.getName();
	
			// remove all not matched by typeName filter
			if (typeNameFilter != null && !typeNameFilter.contains(typeName)) {
				continue;
			}
			relevantTypes.add(typeName);
			
		}
		
		/* Must be sorted by {@link String#compareTo(String)} as {@link EventKey} use this
		 * sort order */
		Collections.sort(relevantTypes);
		
		return new IteratorTypeResult(relevantTypes.iterator());
	}

}
