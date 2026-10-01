/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;
import static com.top_logic.knowledge.service.db2.AbstractFlexDataManager.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.storage.BinaryColumnsStorage;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Statements reading and writing the table of binary values of dynamic attributes.
 *
 * <p>
 * The table has the key columns of the flex data table (branch, type, identifier, attribute and
 * revision range) and stores the value in the columns of its {@link AbstractFlexDataManager#CONTENT}
 * attribute: content inline in the table, or a reference to a blob in a blob store together with
 * the name of the store.
 * </p>
 *
 * @see AbstractFlexDataManager#createFlexBinaryDataType(String, String, boolean)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class FlexBinaryAccess {

	/**
	 * Consumer of binary values read from the table.
	 */
	public interface ValueConsumer {

		/**
		 * Receives a value.
		 *
		 * @param id
		 *        The identifier of the object the value belongs to.
		 * @param attribute
		 *        The name of the attribute.
		 * @param value
		 *        The value.
		 * @param revMin
		 *        The revision in which the value was set.
		 */
		void accept(TLID id, String attribute, BinaryData value, long revMin) throws SQLException;

	}

	private static final String HISTORY_CONTEXT = "historyContext";

	private static final String TABLE_ALIAS = "x";

	private final DBHelper _sqlDialect;

	private final ConnectionPool _pool;

	private final MOKnowledgeItemImpl _type;

	private final AbstractBinaryAttribute _content;

	private final DBAttribute[] _contentColumns;

	private final boolean _multipleBranches;

	private final CompiledStatement _insert;

	private final int _insertBatchSize;

	private final CompiledStatement _historic;

	private final CompiledStatement _bulk;

	private final CompiledStatement _refetch;

	/**
	 * Creates a {@link FlexBinaryAccess}.
	 *
	 * @param pool
	 *        The pool to read inline content from, when its copy in the temporary file system
	 *        vanishes.
	 * @param sqlDialect
	 *        The SQL dialect of the pool.
	 * @param type
	 *        The type of the table.
	 * @param fetchSize
	 *        The fetch size for bulk loads, <code>0</code> for the default of the driver.
	 */
	public FlexBinaryAccess(ConnectionPool pool, DBHelper sqlDialect, MOKnowledgeItemImpl type, int fetchSize) {
		_pool = pool;
		_sqlDialect = sqlDialect;
		_type = type;
		_content = (AbstractBinaryAttribute) type.getAttributeOrNull(CONTENT);
		_contentColumns = _content.getDbMapping();
		_multipleBranches = type.multipleBranches();

		List<Parameter> insertParameters = new ArrayList<>();
		_insert = createInsert(insertParameters);
		_insertBatchSize = sqlDialect.getMaxBatchSize(insertParameters.size());
		_historic = createHistoric();
		_bulk = createBulk(fetchSize);
		_refetch = createRefetch();
	}

	/**
	 * The type of the table.
	 */
	public MOKnowledgeItemImpl getType() {
		return _type;
	}

	/**
	 * The attribute storing the values.
	 */
	public AbstractBinaryAttribute getContentAttribute() {
		return _content;
	}

	private CompiledStatement createInsert(List<Parameter> parameters) {
		List<String> columnNames = new ArrayList<>();
		List<SQLExpression> values = new ArrayList<>();
		if (_multipleBranches) {
			columnNames.add(BRANCH_DBNAME);
			values.add(parameter(DBType.LONG, BRANCH_DBNAME));
			parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
		}
		Collections.addAll(columnNames,
			TYPE_DBNAME,
			IDENTIFIER_DBNAME,
			BasicTypes.REV_MAX_DB_NAME,
			ATTRIBUTE_DBNAME,
			BasicTypes.REV_MIN_DB_NAME);
		Collections.addAll(values,
			parameter(DBType.STRING, TYPE_DBNAME),
			parameter(DBType.ID, IDENTIFIER_DBNAME),
			literal(DBType.LONG, CURRENT_REV),
			parameter(DBType.STRING, ATTRIBUTE_DBNAME),
			parameter(DBType.LONG, BasicTypes.REV_MIN_DB_NAME));
		Collections.addAll(parameters,
			parameterDef(DBType.STRING, TYPE_DBNAME),
			parameterDef(DBType.ID, IDENTIFIER_DBNAME),
			parameterDef(DBType.STRING, ATTRIBUTE_DBNAME),
			parameterDef(DBType.LONG, BasicTypes.REV_MIN_DB_NAME));
		for (DBAttribute column : _contentColumns) {
			columnNames.add(column.getDBName());
			values.add(parameter(column.getSQLType(), column.getDBName()));
			parameters.add(parameterDef(column.getSQLType(), column.getDBName()));
		}
		SQLInsert insert = insert(table(_type, NO_TABLE_ALIAS), columnNames, values);
		return query(parameters, insert).toSql(_sqlDialect);
	}

	private void addContentColumns(List<SQLColumnDefinition> columns) {
		for (DBAttribute column : _contentColumns) {
			columns.add(columnDef(column(TABLE_ALIAS, column.getDBName()), column.getDBName()));
		}
	}

	private SQLExpression keyCondition(SQLExpression identifierCondition, boolean withAttribute) {
		SQLExpression where;
		if (_multipleBranches) {
			where = eq(column(TABLE_ALIAS, BRANCH_DBNAME, NOT_NULL), parameter(DBType.LONG, BRANCH_DBNAME));
		} else {
			where = literalTrueLogical();
		}
		where = and(where,
			eq(column(TABLE_ALIAS, TYPE_DBNAME, NOT_NULL), parameter(DBType.STRING, TYPE_DBNAME)),
			identifierCondition);
		if (withAttribute) {
			where = and(where,
				eq(column(TABLE_ALIAS, ATTRIBUTE_DBNAME, NOT_NULL), parameter(DBType.STRING, ATTRIBUTE_DBNAME)));
		}
		return and(where,
			ge(column(TABLE_ALIAS, BasicTypes.REV_MAX_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT)),
			le(column(TABLE_ALIAS, BasicTypes.REV_MIN_DB_NAME, NOT_NULL), parameter(DBType.LONG, HISTORY_CONTEXT)));
	}

	private List<Parameter> keyParameters(Parameter identifierParameter, boolean withAttribute) {
		List<Parameter> parameters = new ArrayList<>();
		if (_multipleBranches) {
			parameters.add(parameterDef(DBType.LONG, BRANCH_DBNAME));
		}
		parameters.add(parameterDef(DBType.STRING, TYPE_DBNAME));
		parameters.add(identifierParameter);
		if (withAttribute) {
			parameters.add(parameterDef(DBType.STRING, ATTRIBUTE_DBNAME));
		}
		parameters.add(parameterDef(DBType.LONG, HISTORY_CONTEXT));
		return parameters;
	}

	/**
	 * Result columns: attribute, revMin, content columns.
	 */
	private CompiledStatement createHistoric() {
		List<SQLColumnDefinition> columns = new ArrayList<>();
		columns.add(columnDef(column(TABLE_ALIAS, ATTRIBUTE_DBNAME), ATTRIBUTE_DBNAME));
		columns.add(columnDef(column(TABLE_ALIAS, BasicTypes.REV_MIN_DB_NAME), BasicTypes.REV_MIN_DB_NAME));
		addContentColumns(columns);
		SQLSelect select = select(columns, table(_type, TABLE_ALIAS),
			keyCondition(
				eq(column(TABLE_ALIAS, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME)),
				false));
		select.setNoBlockHint(true);
		return query(keyParameters(parameterDef(DBType.ID, IDENTIFIER_DBNAME), false), select).toSql(_sqlDialect);
	}

	/**
	 * Result columns: identifier, attribute, revMin, content columns.
	 */
	private CompiledStatement createBulk(int fetchSize) {
		List<SQLColumnDefinition> columns = new ArrayList<>();
		columns.add(columnDef(column(TABLE_ALIAS, IDENTIFIER_DBNAME), IDENTIFIER_DBNAME));
		columns.add(columnDef(column(TABLE_ALIAS, ATTRIBUTE_DBNAME), ATTRIBUTE_DBNAME));
		columns.add(columnDef(column(TABLE_ALIAS, BasicTypes.REV_MIN_DB_NAME), BasicTypes.REV_MIN_DB_NAME));
		addContentColumns(columns);
		SQLSelect select = select(columns, table(_type, TABLE_ALIAS),
			keyCondition(
				inSet(column(TABLE_ALIAS, IDENTIFIER_DBNAME, NOT_NULL), setParameter(IDENTIFIER_DBNAME, DBType.ID)),
				false));
		select.setNoBlockHint(true);
		CompiledStatement statement =
			query(keyParameters(setParameterDef(IDENTIFIER_DBNAME, DBType.ID), false), select).toSql(_sqlDialect);
		statement.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if (fetchSize > 0) {
			statement.setFetchSize(fetchSize);
		}
		return statement;
	}

	/**
	 * Result columns: content type, size, data.
	 */
	private CompiledStatement createRefetch() {
		List<SQLColumnDefinition> columns = new ArrayList<>();
		columns.add(columnDef(column(TABLE_ALIAS, _content.getContentTypeColumn().getDBName()), null));
		columns.add(columnDef(column(TABLE_ALIAS, _content.getSizeColumn().getDBName()), null));
		columns.add(columnDef(column(TABLE_ALIAS, _content.getDataColumn().getDBName()), null));
		SQLSelect select = select(columns, table(_type, TABLE_ALIAS),
			keyCondition(
				eq(column(TABLE_ALIAS, IDENTIFIER_DBNAME, NOT_NULL), parameter(DBType.ID, IDENTIFIER_DBNAME)),
				true));
		select.setNoBlockHint(true);
		return query(keyParameters(parameterDef(DBType.ID, IDENTIFIER_DBNAME), true), select).toSql(_sqlDialect);
	}

	/**
	 * The statement inserting a current value.
	 *
	 * @see #insertArguments(long, long, String, TLID, String, BinaryData)
	 */
	public CompiledStatement insertStatement() {
		return _insert;
	}

	/**
	 * The maximum number of batches of the {@link #insertStatement()}.
	 */
	public int insertBatchSize() {
		return _insertBatchSize;
	}

	/**
	 * The arguments of the {@link #insertStatement()} for inserting the given value as current
	 * value of an attribute.
	 *
	 * @param branch
	 *        The branch of the object.
	 * @param commitNumber
	 *        The revision in which the value is set.
	 * @param type
	 *        The name of the type of the object.
	 * @param id
	 *        The identifier of the object.
	 * @param attribute
	 *        The name of the attribute.
	 * @param value
	 *        The value. Content not referencing a blob is stored inline.
	 */
	public Object[] insertArguments(long branch, long commitNumber, String type, TLID id, String attribute,
			BinaryData value) throws SQLException {
		List<Object> result = new ArrayList<>();
		if (_multipleBranches) {
			result.add(Long.valueOf(branch));
		}
		result.add(type);
		result.add(id);
		result.add(attribute);
		result.add(Long.valueOf(commitNumber));
		BinaryData storedValue;
		try {
			storedValue = BlobUpload.withKnownSize(value);
		} catch (IOException ex) {
			throw new SQLException("Reading content of attribute '" + attribute + "' failed.", ex);
		}
		Collections.addAll(result, BinaryColumnsStorage.columnValues(_content, storedValue));
		return result.toArray();
	}

	/**
	 * Reads the binary values of an object valid in the given revision.
	 *
	 * @param connection
	 *        The connection to read from.
	 * @param branch
	 *        The branch of the object.
	 * @param type
	 *        The name of the type of the object.
	 * @param id
	 *        The identifier of the object.
	 * @param dataRevision
	 *        The revision of the values to read.
	 * @param consumer
	 *        Receives the values.
	 */
	public void loadValues(PooledConnection connection, long branch, String type, TLID id, long dataRevision,
			ValueConsumer consumer) throws SQLException {
		Object[] args = _multipleBranches ? new Object[] { branch, type, id, dataRevision }
			: new Object[] { type, id, dataRevision };
		try (ResultSet result = _historic.executeQuery(connection, args)) {
			while (result.next()) {
				String attribute = result.getString(1);
				long revMin = result.getLong(2);
				BinaryData value = readValue(result, 3, branch, type, id, attribute, dataRevision);
				consumer.accept(id, attribute, value, revMin);
			}
		}
	}

	/**
	 * Reads the binary values of all given objects valid in the given revision.
	 *
	 * @param connection
	 *        The connection to read from.
	 * @param branch
	 *        The branch of the objects.
	 * @param type
	 *        The name of the type of the objects.
	 * @param ids
	 *        The identifiers of the objects.
	 * @param dataRevision
	 *        The revision of the values to read.
	 * @param consumer
	 *        Receives the values.
	 */
	public void loadAllValues(PooledConnection connection, long branch, String type, Collection<TLID> ids,
			long dataRevision, ValueConsumer consumer) throws SQLException {
		Object[] args = _multipleBranches ? new Object[] { branch, type, ids, dataRevision }
			: new Object[] { type, ids, dataRevision };
		try (ResultSet result = _bulk.executeQuery(connection, args)) {
			while (result.next()) {
				TLID id = IdentifierUtil.getId(result, 1);
				String attribute = result.getString(2);
				long revMin = result.getLong(3);
				BinaryData value = readValue(result, 4, branch, type, id, attribute, dataRevision);
				consumer.accept(id, attribute, value, revMin);
			}
		}
	}

	private BinaryData readValue(ResultSet result, int firstContentColumn, long branch, String type, TLID id,
			String attribute, long dataRevision) throws SQLException {
		BlobBinaryData blob = BinaryColumnsStorage.fetchReference(result, _content,
			column -> firstContentColumn + contentColumnIndex(column));
		if (blob != null) {
			return blob;
		}

		long size = result.getLong(firstContentColumn + contentColumnIndex(_content.getSizeColumn()));
		String contentType =
			result.getString(firstContentColumn + contentColumnIndex(_content.getContentTypeColumn()));
		String name =
			DBBinaryData.noEmptyName(
				result.getString(firstContentColumn + contentColumnIndex(_content.getNameColumn())));
		if (size < BinaryDataFactory.MAX_MEMORY_SIZE) {
			// The content column is read last, since reading other columns may close its stream.
			try (InputStream content =
				_sqlDialect.getBinaryStream(result, firstContentColumn + contentColumnIndex(_content.getDataColumn()))) {
				if (content == null) {
					throw new SQLException("No content of attribute '" + attribute + "' of '" + id + "'.");
				}
				return BinaryDataFactory.createMemoryBinaryData(content, size, contentType, name);
			} catch (IOException ex) {
				throw new SQLException("Reading content of attribute '" + attribute + "' failed.", ex);
			}
		}
		Object[] args = _multipleBranches ? new Object[] { branch, type, id, attribute, dataRevision }
			: new Object[] { type, id, attribute, dataRevision };
		return new InlineContent(size, args, name, contentType);
	}

	private int contentColumnIndex(DBAttribute column) {
		for (int n = 0; n < _contentColumns.length; n++) {
			if (_contentColumns[n] == column) {
				return n;
			}
		}
		throw new IllegalArgumentException("Not a content column: " + column.getDBName());
	}

	/**
	 * Inline content read from the table on first access.
	 */
	private final class InlineContent extends DBBinaryData {

		private final Object[] _args;

		private final String _name;

		private final String _contentType;

		InlineContent(long size, Object[] args, String name, String contentType) {
			super(size, _pool);
			_args = args;
			_name = name;
			_contentType = contentType;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getContentType() {
			return _contentType == null ? super.getContentType() : _contentType;
		}

		@Override
		protected BinaryData refetch(PooledConnection connection) throws SQLException {
			try (ResultSet result = _refetch.executeQuery(connection, _args)) {
				if (result.next()) {
					BinaryData content = fromBlobColumn(connection.getSQLDialect(), result, _name, 1, 2, 3);
					if (content != null) {
						return content;
					}
				}
				throw new SQLException("No binary data found.");
			}
		}

	}

}
