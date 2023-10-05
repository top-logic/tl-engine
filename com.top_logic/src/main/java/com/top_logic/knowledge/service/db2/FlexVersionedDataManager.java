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
import static com.top_logic.basic.db.sql.SQLFactory.setParameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.db.sql.SQLUpdate;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.AddAttributeStatement.AddAttributeBatchCollector;
import com.top_logic.knowledge.service.db2.MutableFlexData.ChangeType;

/**
 * {@link FlexDataManager} implementation that keeps all historic states of all of its
 * {@link NamedValues}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class FlexVersionedDataManager extends AbstractFlexDataManager {

	/**
	 * Service class to create statements for updating an attribute value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class UpdateAttributeStatement {

		/**
		 * Collector for batches of {@link UpdateAttributeStatement#updateCurrentValueStatement()} and
		 * {@link UpdateAttributeStatement#addOldValueStatement()}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface UpdateAttributeBatchCollector {

			/**
			 * Adds a new batch for {@link UpdateAttributeStatement#addOldValueStatement()}.
			 * 
			 * @param arguments
			 *        Arguments for the batch.
			 * @throws SQLException
			 *         when creating batch failed.
			 */
			void addInsertOldAttributeValueBatch(Object... arguments) throws SQLException;

			/**
			 * Adds a new batch for {@link UpdateAttributeStatement#updateCurrentValueStatement()}.
			 * 
			 * @param arguments
			 *        Arguments for the batch.
			 * @throws SQLException
			 *         when creating batch failed.
			 */
			void addUpdateCurrentAttributeValueBatch(Object... arguments) throws SQLException;

		}

		private int UPDATE_CURRENT_BATCH_SIZE;

		private int ADD_OLD_BATCH_SIZE;

		private final CompiledStatement _updateCurrentStatement;

		private final CompiledStatement _addOldOldStatement;

		private final MOKnowledgeItemImpl _dataType;

		public UpdateAttributeStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			_dataType = dataType;
			_updateCurrentStatement = createUpdateCurrentStmt(sqlDialect);
			_addOldOldStatement = createAddOldStmt(sqlDialect);
		}

		private CompiledStatement createAddOldStmt(DBHelper sqlDialect) {
			String tableAlias = NO_TABLE_ALIAS;
			SQLTable table = table(_dataType, tableAlias);
			boolean multipleBranches = _dataType.multipleBranches();

			List<String> columnNames = new ArrayList<>();
			if (multipleBranches) {
				columnNames.add(BRANCH_DBNAME);
			}
			Collections.addAll(columnNames,
				TYPE_DBNAME,
				IDENTIFIER_DBNAME,
				BasicTypes.REV_MAX_DB_NAME,
				ATTRIBUTE_DBNAME,
				BasicTypes.REV_MIN_DB_NAME,
				DATA_TYPE_DBNAME,
				LONG_DATA_DBNAME,
				DOUBLE_DATA_DBNAME,
				VARCHAR_DATA_DBNAME,
				CLOB_DATA_DBNAME,
				BLOB_DATA_DBNAME);
			List<SQLExpression> values = new ArrayList<>();
			if (multipleBranches) {
				values.add(parameter(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(values,
				parameter(DBType.STRING, TYPE_DBNAME),
				parameter(DBType.ID, IDENTIFIER_DBNAME),
				parameter(DBType.LONG, BasicTypes.REV_MAX_DB_NAME),
				parameter(DBType.STRING, ATTRIBUTE_DBNAME),
				parameter(DBType.LONG, BasicTypes.REV_MIN_DB_NAME),
				parameter(DBType.BYTE, DATA_TYPE_DBNAME),
				parameter(DBType.LONG, LONG_DATA_DBNAME),
				parameter(DBType.DOUBLE, DOUBLE_DATA_DBNAME),
				parameter(DBType.STRING, VARCHAR_DATA_DBNAME),
				parameter(DBType.CLOB, CLOB_DATA_DBNAME),
				parameter(DBType.BLOB, BLOB_DATA_DBNAME));
			SQLInsert insert = insert(table, columnNames, values);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			parameters.add(parameterDef(DBType.STRING, TYPE_DBNAME));
			parameters.add(parameterDef(DBType.ID, IDENTIFIER_DBNAME));
			parameters.add(parameterDef(DBType.LONG, BasicTypes.REV_MAX_DB_NAME));
			parameters.add(parameterDef(DBType.STRING, ATTRIBUTE_DBNAME));
			parameters.add(parameterDef(DBType.LONG, BasicTypes.REV_MIN_DB_NAME));
			parameters.add(parameterDef(DBType.BYTE, DATA_TYPE_DBNAME));
			parameters.add(parameterDef(DBType.LONG, LONG_DATA_DBNAME));
			parameters.add(parameterDef(DBType.DOUBLE, DOUBLE_DATA_DBNAME));
			parameters.add(parameterDef(DBType.STRING, VARCHAR_DATA_DBNAME));
			parameters.add(parameterDef(DBType.CLOB, CLOB_DATA_DBNAME));
			parameters.add(parameterDef(DBType.BLOB, BLOB_DATA_DBNAME));
			ADD_OLD_BATCH_SIZE = sqlDialect.getMaxBatchSize(parameters.size());
			CompiledStatement sql = query(parameters, insert).toSql(sqlDialect);
			return sql;
		}

		private CompiledStatement createUpdateCurrentStmt(DBHelper sqlDialect) {
			String tableAlias = NO_TABLE_ALIAS;
			SQLTable table = table(_dataType, tableAlias);
			boolean multipleBranches = _dataType.multipleBranches();
			SQLExpression where;
			if (multipleBranches) {
				where = eq(column(tableAlias, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
			} else {
				where = SQLFactory.literalTrueLogical();
			}
			where = and(
				where,
				eq(column(tableAlias, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
				eq(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME)),
				eq(column(tableAlias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), literalLong(CURRENT_REV)),
				eq(column(tableAlias, ATTRIBUTE_DBNAME, NOT_NULL), parameter(DBType.STRING, ATTRIBUTE_DBNAME)));
			List<String> columns = new ArrayList<>();
			Collections.addAll(columns,
				BasicTypes.REV_MIN_DB_NAME,
				DATA_TYPE_DBNAME,
				LONG_DATA_DBNAME,
				DOUBLE_DATA_DBNAME,
				VARCHAR_DATA_DBNAME,
				CLOB_DATA_DBNAME,
				BLOB_DATA_DBNAME);
			List<SQLExpression> values = new ArrayList<>();
			Collections.addAll(values,
				parameter(DBType.LONG, BasicTypes.REV_MIN_DB_NAME),
				parameter(DBType.BYTE, DATA_TYPE_DBNAME),
				parameter(DBType.LONG, LONG_DATA_DBNAME),
				parameter(DBType.DOUBLE, DOUBLE_DATA_DBNAME),
				parameter(DBType.STRING, VARCHAR_DATA_DBNAME),
				parameter(DBType.CLOB, CLOB_DATA_DBNAME),
				parameter(DBType.BLOB, BLOB_DATA_DBNAME));

			SQLUpdate updates = update(table, where, columns, values);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(parameters,
				parameterDef(DBType.STRING, TYPE_DBNAME),
				parameterDef(DBType.ID, IDENTIFIER_DBNAME),
				parameterDef(DBType.STRING, ATTRIBUTE_DBNAME),
				parameterDef(DBType.LONG, BasicTypes.REV_MIN_DB_NAME),
				parameterDef(DBType.BYTE, DATA_TYPE_DBNAME),
				parameterDef(DBType.LONG, LONG_DATA_DBNAME),
				parameterDef(DBType.DOUBLE, DOUBLE_DATA_DBNAME),
				parameterDef(DBType.STRING, VARCHAR_DATA_DBNAME),
				parameterDef(DBType.CLOB, CLOB_DATA_DBNAME),
				parameterDef(DBType.BLOB, BLOB_DATA_DBNAME));

			UPDATE_CURRENT_BATCH_SIZE = sqlDialect.getMaxBatchSize(parameters.size());
			CompiledStatement sql = query(parameters, updates).toSql(sqlDialect);
			return sql;
		}

		public CompiledStatement updateCurrentValueStatement() {
			return _updateCurrentStatement;
		}

		public int updateCurrentValueMaxBatchSize() {
			return UPDATE_CURRENT_BATCH_SIZE;
		}

		public CompiledStatement addOldValueStatement() {
			return _addOldOldStatement;
		}

		public int addOldValueMaxBatchSize() {
			return ADD_OLD_BATCH_SIZE;
		}

		public void updateAttribute(UpdateAttributeBatchCollector collector, long branch, long commitNumber,
				String type, TLID id, String attributeName, Object newValue, Long lastChangedRevision, Object oldValue)
				throws SQLException {
			collectAddOldBatch(collector, branch, commitNumber, type, id, attributeName, lastChangedRevision, oldValue);
			collectUpdateCurrentBatch(collector, branch, commitNumber, type, id, attributeName, newValue);
		}

		private void collectAddOldBatch(UpdateAttributeBatchCollector collector, long branch, long commitNumber,
				String type, TLID id, String attributeName, Long lastChangedRevision, Object oldValue)
				throws SQLException {
			Object[] args;
			int argsIndex = 0;
			if (_dataType.multipleBranches()) {
				args = new Object[12];
				args[argsIndex++] = branch;
			} else {
				args = new Object[11];
			}
			args[argsIndex++] = type;
			args[argsIndex++] = id;
			args[argsIndex++] = commitNumber - 1;
			args[argsIndex++] = attributeName;
			args[argsIndex++] = lastChangedRevision;
			setData(commitNumber, args,
				oldValue,
				argsIndex,
				argsIndex + 1,
				argsIndex + 2,
				argsIndex + 3,
				argsIndex + 4,
				argsIndex + 5);
			collector.addInsertOldAttributeValueBatch(args);
		}

		private void collectUpdateCurrentBatch(UpdateAttributeBatchCollector collector, long branch, long commitNumber,
				String type, TLID id, String attributeName, Object newValue) throws SQLException {
			Object[] args;
			int argsIndex = 0;
			if (_dataType.multipleBranches()) {
				args = new Object[11];
				args[argsIndex++] = branch;
			} else {
				args = new Object[10];
			}
			args[argsIndex++] = type;
			args[argsIndex++] = id;
			args[argsIndex++] = attributeName;
			args[argsIndex++] = commitNumber;
			setData(commitNumber, args,
				newValue,
				argsIndex,
				argsIndex + 1,
				argsIndex + 2,
				argsIndex + 3,
				argsIndex + 4,
				argsIndex + 5);
			collector.addUpdateCurrentAttributeValueBatch(args);
		}

	}

	private static class DeleteAttributeStatement {

		/**
		 * Collector for batches of {@link DeleteAttributeStatement#statement()}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface DeleteAttributeBatchCollector {

			/**
			 * Adds a new batch for {@link DeleteAttributeStatement#statement()}.
			 * 
			 * @param arguments
			 *        Arguments for the batch.
			 * @throws SQLException
			 *         when creating batch failed.
			 */
			void addDeleteAttributeBatch(Object... arguments) throws SQLException;

		}

		private final int MAX_BATCH_SIZE;

		private final CompiledStatement statement;

		private final MOKnowledgeItemImpl _dataType;

		public DeleteAttributeStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			_dataType = dataType;
			String tableAlias = NO_TABLE_ALIAS;
			SQLTable table = table(dataType, tableAlias);
			boolean multipleBranches = _dataType.multipleBranches();
			SQLExpression where;
			if (multipleBranches) {
				where = eq(column(tableAlias, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
			} else {
				where = SQLFactory.literalTrueLogical();
			}
			where = and(
				where,
				eq(column(tableAlias, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
				eq(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME)),
				eq(column(tableAlias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), literalLong(CURRENT_REV)),
				eq(column(tableAlias, ATTRIBUTE_DBNAME, NOT_NULL), parameter(DBType.STRING, ATTRIBUTE_DBNAME))
				);
				List<String> columns = Collections.singletonList(BasicTypes.REV_MAX_DB_NAME);
			List<SQLExpression> values =
					Collections.<SQLExpression> singletonList(parameter(DBType.LONG, BasicTypes.REV_MAX_DB_NAME));
			SQLUpdate updates = update(table, where, columns, values);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(parameters,
				parameterDef(DBType.STRING, TYPE_DBNAME),
				parameterDef(DBType.ID, IDENTIFIER_DBNAME),
				parameterDef(DBType.LONG, BasicTypes.REV_MAX_DB_NAME),
				parameterDef(DBType.STRING, ATTRIBUTE_DBNAME)
				);
			MAX_BATCH_SIZE = sqlDialect.getMaxBatchSize(parameters.size());
			this.statement = query(parameters, updates).toSql(sqlDialect);
		}

		public CompiledStatement statement() {
			return statement;
		}

		/**
		 * Determines the maximal possible numbers of batches for {@link #statement()}.
		 */
		public int maxBatchSize() {
			return MAX_BATCH_SIZE;
		}

		public void deleteAttribute(DeleteAttributeBatchCollector collector, long branch, long commitNumber,
				String type, TLID id, String attributeName) throws SQLException {
			if (_dataType.multipleBranches()) {
				collector.addDeleteAttributeBatch(branch, type, id, commitNumber - 1, attributeName);
			} else {
				collector.addDeleteAttributeBatch(type, id, commitNumber - 1, attributeName);
			}
		}

    }
    
	private static abstract class DeleteObjectStatement {

		private final CompiledStatement _statement;

		private final MOKnowledgeItemImpl _dataType;

		public DeleteObjectStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			_dataType = dataType;
			_statement = createStatement(sqlDialect, dataType);
		}

		private CompiledStatement createStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			String tableAlias = NO_TABLE_ALIAS;
			SQLTable table = table(dataType, tableAlias);
			boolean multipleBranches = dataType().multipleBranches();
			SQLExpression where;
			if (multipleBranches) {
				where = eq(column(tableAlias, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
			} else {
				where = SQLFactory.literalTrueLogical();
			}
			where = and(
				where,
				eq(column(tableAlias, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
				identifierMatch(tableAlias),
				eq(column(tableAlias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), literalLong(CURRENT_REV))
				);
			List<String> columns = Collections.singletonList(BasicTypes.REV_MAX_DB_NAME);
			List<SQLExpression> values =
				Collections.<SQLExpression> singletonList(parameter(DBType.LONG, BasicTypes.REV_MAX_DB_NAME));
			SQLUpdate updates = update(table, where, columns, values);
			List<Parameter> parameters = new ArrayList<>();
			if (multipleBranches) {
				parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
			}
			Collections.addAll(parameters,
				parameterDef(DBType.STRING, TYPE_DBNAME),
				identifierParameterDefinition(),
				parameterDef(DBType.LONG, BasicTypes.REV_MAX_DB_NAME));
			SQLQuery<SQLUpdate> query = query(parameters, updates);
			CompiledStatement sql = query.toSql(sqlDialect);
			return sql;
		}

		/**
		 * Parameter that is later filled with the identifier (resp. identifiers).
		 */
		protected abstract Parameter identifierParameterDefinition();

		/**
		 * {@link SQLException} that matches the rows with the correct identifier values.
		 */
		protected abstract SQLExpression identifierMatch(String tableAlias);

		public CompiledStatement statement() {
			return _statement;
		}

		/**
		 * This method returns the dataType.
		 * 
		 * @return Returns the dataType.
		 */
		public MOKnowledgeItemImpl dataType() {
			return _dataType;
		}

	}

	private static class DeleteSingleObjectStatement extends DeleteObjectStatement {

		public DeleteSingleObjectStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			super(sqlDialect, dataType);
		}

		@Override
		protected SQLExpression identifierMatch(String tableAlias) {
			return eq(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME));
		}

		@Override
		protected Parameter identifierParameterDefinition() {
			return parameterDef(DBType.ID, IDENTIFIER_DBNAME);
		}

		public int deleteObject(Connection connection, long branch, long commitNumber, TLID id, String type)
				throws SQLException {
			int result;
			if (dataType().multipleBranches()) {
				result = statement().executeUpdate(connection, branch, type, id, commitNumber - 1);
			} else {
				result = statement().executeUpdate(connection, type, id, commitNumber - 1);
			}
			return result;
		}

	}

	private static class DeleteMultipleObjectsStatement extends DeleteObjectStatement {

		public DeleteMultipleObjectsStatement(DBHelper sqlDialect, MOKnowledgeItemImpl dataType) {
			super(sqlDialect, dataType);
		}

		@Override
		protected SQLExpression identifierMatch(String tableAlias) {
			return inSet(column(tableAlias, IDENTIFIER_DBNAME, NOT_NULL), setParameter(IDENTIFIER_DBNAME, DBType.ID));
		}

		@Override
		protected Parameter identifierParameterDefinition() {
			return setParameterDef(IDENTIFIER_DBNAME, DBType.ID);
		}

		public int deleteObjects(Connection connection, long branch, long commitNumber, Collection<TLID> ids,
				String type) throws SQLException {
			int result;
			if (dataType().multipleBranches()) {
				result = statement().executeUpdate(connection, branch, type, ids, commitNumber - 1);
			} else {
				result = statement().executeUpdate(connection, type, ids, commitNumber - 1);
			}
			return result;
		}

	}

	private static class BatchCollector
			implements AddAttributeBatchCollector,
			DeleteAttributeStatement.DeleteAttributeBatchCollector,
			UpdateAttributeStatement.UpdateAttributeBatchCollector,
			AutoCloseable {

		private final Batch _addAttributeBatch;

		private final int _maxNumberAddAttributeBatches;

		private int _numberAddAttributeBatches;

		private final Batch _deleteAttributeBatch;

		private final int _maxNumberDeleteAttributeBatches;

		private int _numberDeleteAttributeBatches;

		private final Batch _addOldAttributeValueBatch;

		private final int _maxNumberOldAttributeValueBatches;

		private int _numberOldAttributeValueBatches;

		private final Batch _updateCurrentAttributeBatch;

		private final int _maxNumberUpdateCurrentAttributeBatches;

		private int _numberUpdateCurrentAttributeBatches;

		public BatchCollector(Batch addAttributeBatch, int maxNumberAddAttributeBatches, Batch deleteAttributeBatch,
				int maxNumberDeleteAttributeBatches, Batch addOldAttributeValueBatch,
				int maxNumberOldAttributeValueBatches, Batch updateCurrentAttributeBatch,
				int maxNumberUpdateCurrentAttributeBatches) {
			_addAttributeBatch = addAttributeBatch;
			_maxNumberAddAttributeBatches = maxNumberAddAttributeBatches;
			_deleteAttributeBatch = deleteAttributeBatch;
			_maxNumberDeleteAttributeBatches = maxNumberDeleteAttributeBatches;
			_addOldAttributeValueBatch = addOldAttributeValueBatch;
			_maxNumberOldAttributeValueBatches = maxNumberOldAttributeValueBatches;
			_updateCurrentAttributeBatch = updateCurrentAttributeBatch;
			_maxNumberUpdateCurrentAttributeBatches = maxNumberUpdateCurrentAttributeBatches;
		}

		@Override
		public void addDeleteAttributeBatch(Object... arguments) throws SQLException {
			_deleteAttributeBatch.addBatch(arguments);
			_numberDeleteAttributeBatches++;
			if (_numberDeleteAttributeBatches >= _maxNumberDeleteAttributeBatches) {
				_deleteAttributeBatch.executeBatch();
				_numberDeleteAttributeBatches = 0;
			}
		}

		@Override
		public void addAddAttributeBatch(Object... arguments) throws SQLException {
			_addAttributeBatch.addBatch(arguments);
			_numberAddAttributeBatches++;
			if (_numberAddAttributeBatches >= _maxNumberAddAttributeBatches) {
				/* It is necessary first to perform all delete operation before add operations: An
				 * update of an attribute is realized by outdating ("delete") the old row and insert
				 * ("add") a new row. When the outdate is not happened, a primary key constraint
				 * violation occurs, because with the new row, two different rows with
				 * "rev_max=current" exist. */
				if (_numberDeleteAttributeBatches > 0) {
					_deleteAttributeBatch.executeBatch();
					_numberDeleteAttributeBatches = 0;
				}
				_addAttributeBatch.executeBatch();
				_numberAddAttributeBatches = 0;
			}
		}

		@Override
		public void addInsertOldAttributeValueBatch(Object... arguments) throws SQLException {
			_addOldAttributeValueBatch.addBatch(arguments);
			_numberOldAttributeValueBatches++;
			if (_numberOldAttributeValueBatches >= _maxNumberOldAttributeValueBatches) {
				_addOldAttributeValueBatch.executeBatch();
				_numberOldAttributeValueBatches = 0;
			}
		}

		@Override
		public void addUpdateCurrentAttributeValueBatch(Object... arguments) throws SQLException {
			_updateCurrentAttributeBatch.addBatch(arguments);
			_numberUpdateCurrentAttributeBatches++;
			if (_numberUpdateCurrentAttributeBatches >= _maxNumberUpdateCurrentAttributeBatches) {
				_updateCurrentAttributeBatch.executeBatch();
				_numberUpdateCurrentAttributeBatches = 0;
			}
		}

		@Override
		public void close() throws SQLException {
			if (_numberDeleteAttributeBatches > 0) {
				_deleteAttributeBatch.executeBatch();
			}
			if (_numberAddAttributeBatches > 0) {
				_addAttributeBatch.executeBatch();
			}
			if (_numberOldAttributeValueBatches > 0) {
				_addOldAttributeValueBatch.executeBatch();
			}
			if (_numberUpdateCurrentAttributeBatches > 0) {
				_updateCurrentAttributeBatch.executeBatch();
			}
		}

	}

	private final DeleteAttributeStatement deleteAttributeStatement;

	private final AddAttributeStatement addAttributeStatement;

	private final UpdateAttributeStatement _updateAttributeStatement;

	private final DeleteSingleObjectStatement _deleteSingleObjectStatement;

	private final DeleteMultipleObjectsStatement _deleteMultipleObjectsStatement;

	public FlexVersionedDataManager(ConnectionPool connectionPool, MOKnowledgeItemImpl dataType) {
		super(connectionPool, dataType);

		this.deleteAttributeStatement = new DeleteAttributeStatement(sqlDialect, dataType);
		this.addAttributeStatement = new AddAttributeStatement(sqlDialect, dataType);
		_updateAttributeStatement = new UpdateAttributeStatement(sqlDialect, dataType);
		_deleteSingleObjectStatement = new DeleteSingleObjectStatement(sqlDialect, dataType);
		_deleteMultipleObjectsStatement = new DeleteMultipleObjectsStatement(sqlDialect, dataType);
	}
	
	@Override
	public void updateAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		int firstDynamicValueIndex = -1;
		for (int i = 0; i < items.size(); i++) {
			FlexData dynamicValues = items.get(i).getLocalDynamicValues(context);
			if (dynamicValues != null) {
				firstDynamicValueIndex = i;
				break;
			}
		}
		if (firstDynamicValueIndex == -1) {
			// no dynamic values changed.
			return;
		}

		try (
				Batch addBatch = addAttributeStatement.statement().createBatch(context.getConnection());
				Batch deleteBatch = deleteAttributeStatement.statement().createBatch(context.getConnection());
				Batch addOldValueBatch =
					_updateAttributeStatement.addOldValueStatement().createBatch(context.getConnection());
				Batch updateCurrentValueBatch =
					_updateAttributeStatement.updateCurrentValueStatement().createBatch(context.getConnection());
				BatchCollector collector = new BatchCollector(addBatch, addAttributeStatement.maxBatchSize(),
					deleteBatch, deleteAttributeStatement.maxBatchSize(), addOldValueBatch,
					_updateAttributeStatement.addOldValueMaxBatchSize(), updateCurrentValueBatch,
						_updateAttributeStatement.updateCurrentValueMaxBatchSize());) {
			for (int i = firstDynamicValueIndex; i < items.size(); i++) {
				DBKnowledgeItem item = items.get(i);
				FlexData dynamicValues = item.getLocalDynamicValues(context);
				if (dynamicValues != null) {
					internalStore(item.tId(), dynamicValues, context, collector);
				}
			}

		}
	}
	
	@Override
	protected void internalStore(ObjectKey key, FlexData data, CommitContext context) throws SQLException {
		try (
				Batch addBatch = addAttributeStatement.statement().createBatch(context.getConnection());
				Batch deleteBatch = deleteAttributeStatement.statement().createBatch(context.getConnection());
				Batch addOldValueBatch =
					_updateAttributeStatement.addOldValueStatement().createBatch(context.getConnection());
				Batch updateCurrentValueBatch =
					_updateAttributeStatement.updateCurrentValueStatement().createBatch(context.getConnection());
				BatchCollector collector = new BatchCollector(addBatch, addAttributeStatement.maxBatchSize(),
					deleteBatch, deleteAttributeStatement.maxBatchSize(), addOldValueBatch,
					_updateAttributeStatement.addOldValueMaxBatchSize(), updateCurrentValueBatch,
					_updateAttributeStatement.updateCurrentValueMaxBatchSize());) {
			internalStore(key, data, context, collector);
		}
	}

	private void internalStore(ObjectKey key, FlexData data, CommitContext context, BatchCollector collector)
			throws SQLException {
		long branch = key.getBranchContext();
		long commitNumber = context.getCommitNumber();
		String theType = key.getObjectType().getName();
		TLID id = key.getObjectName();

		MutableFlexData anObject = (MutableFlexData) data;
		for (Entry<String, ChangeType> change : anObject.getChanges()) {

			ChangeType changeType = change.getValue();
			String changedAttribute = change.getKey();

			try {
				switch (changeType) {
					case ADD: {
						Object attributeValue = anObject.getAttributeValue(changedAttribute);
						if (attributeValue == NextCommitNumberFuture.INSTANCE) {
							anObject.setAttributeValue(changedAttribute, commitNumber);
							attributeValue = commitNumber;
						}
						addAttributeStatement.addAttribute(collector, branch, commitNumber, theType, id,
							changedAttribute, attributeValue);
						break;
					}
					case UPDATE: {
						Object newAttributeValue = anObject.getAttributeValue(changedAttribute);
						Object oldAttributeValue = anObject.oldValue(changedAttribute);
						Long lastModified = anObject.lastModified(changedAttribute);
						_updateAttributeStatement.updateAttribute(collector, branch, commitNumber, theType, id,
							changedAttribute, newAttributeValue, lastModified, oldAttributeValue);

						if (newAttributeValue == NextCommitNumberFuture.INSTANCE) {
							anObject.setAttributeValue(changedAttribute, commitNumber);
							newAttributeValue = commitNumber;
						}
						break;

					}
					case DELETE: {
						deleteAttributeStatement.deleteAttribute(collector, branch, commitNumber, theType, id,
							changedAttribute);
						break;
					}
					case UNCHANGED:
						throw new UnreachableAssertion(
							"Changes contain only changed attributes: '" + key + "." + changedAttribute + "'.");
				}
			} catch (SQLException ex) {
				throw SQLH.enhanceMessage(ex, "Failed to store dynamic attribute for '" + key + "." + changedAttribute
						+ "' (change type '" + changeType + "').");
			}

		}
		anObject.commitLocally(commitNumber);
	}

	@Override
	public void deleteAll(List<DBKnowledgeItem> items, CommitContext context) throws SQLException {
		int size = items.size();
		switch (size) {
			case 0:
				break;
			case 1: {
				internalDelete(items.get(0).tId(), context);
				break;
			}
			default: {
				long commitNumber = context.getCommitNumber();
				int maxSetSize = context.getConnection().getSQLDialect().getMaxSetSize();
				List<TLID> ids = new ArrayList<>(size);

				DBObjectKey key = items.get(0).tId();
				long branch = key.getBranchContext();
				String type = key.getObjectType().getName();
				ids.add(key.getObjectName());
				for (int i = 1; i < size; i++) {
					DBObjectKey nextKey = items.get(i).tId();
					long nextBranch = nextKey.getBranchContext();
					String nextType = nextKey.getObjectType().getName();
					if (nextBranch != branch || !nextType.equals(type) || ids.size() >= maxSetSize) {
						if (ids.size() == 1) {
							deleteSingleObject(branch, ids.get(0), type, context);
						} else {
							_deleteMultipleObjectsStatement.deleteObjects(context.getConnection(), branch, commitNumber,
								ids, type);
						}
						ids.clear();

						branch = nextBranch;
						type = nextType;
					}
					ids.add(nextKey.getObjectName());
				}

				if (ids.size() == 1) {
					deleteSingleObject(branch, ids.get(0), type, context);
				} else {
					_deleteMultipleObjectsStatement.deleteObjects(context.getConnection(), branch, commitNumber, ids,
						type);
				}
			}
		}
	}

	@Override
	protected boolean internalDelete(ObjectKey key, CommitContext context) throws SQLException {
		long branch = key.getBranchContext();
		TLID id = key.getObjectName();
		String type = key.getObjectType().getName();

		return deleteSingleObject(branch, id, type, context);
	}

	private boolean deleteSingleObject(long branch, TLID id, String type, CommitContext context) throws SQLException {
		long commitNumber = context.getCommitNumber();
		return _deleteSingleObjectStatement.deleteObject(context.getConnection(), branch, commitNumber, id, type) > 0;
	}

}
