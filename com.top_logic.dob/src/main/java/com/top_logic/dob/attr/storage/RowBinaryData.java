/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.parameter;
import static com.top_logic.basic.db.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;

/**
 * {@link DBBinaryData} for the content of a BLOB column of a single table row.
 *
 * <p>
 * The content read from the database is kept in the temporary file system. If this copy vanishes
 * (e.g. removed by a cleanup task), the content is fetched again from the row it was read from,
 * identified by branch, identifier and revision.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RowBinaryData extends DBBinaryData {

	private final DBAttribute _dataColumn;

	private final Object[] _queryArguments;

	private final DBTableMetaObject _refetchTable;

	private final String _name;

	private final String _contentType;

	/**
	 * Creates a {@link RowBinaryData}.
	 *
	 * @param original
	 *        The content as read or written.
	 * @param pool
	 *        The pool to read from when fetching the content again.
	 * @param refetchTable
	 *        The table of the row.
	 * @param dataColumn
	 *        The BLOB column holding the content.
	 * @param queryArguments
	 *        The values identifying the row, see
	 *        {@link #createQueryArguments(DBTableMetaObject, DataObject, Object[])}.
	 * @param name
	 *        The name of the data, <code>null</code> for {@link BinaryData#NO_NAME}.
	 * @param contentType
	 *        The content type of the data, <code>null</code> for the content type of the
	 *        original or fetched content.
	 */
	public RowBinaryData(BinaryData original, ConnectionPool pool, DBTableMetaObject refetchTable,
			DBAttribute dataColumn, Object[] queryArguments, String name, String contentType) {
		super(original, pool);
		_dataColumn = dataColumn;
		_refetchTable = refetchTable;
		_queryArguments = queryArguments;
		_name = name;
		_contentType = contentType;
	}

	/**
	 * Creates a {@link RowBinaryData} that fetches its content from the database on first access.
	 *
	 * @param size
	 *        The size of the content.
	 * @see #RowBinaryData(BinaryData, ConnectionPool, DBTableMetaObject, DBAttribute, Object[],
	 *      String, String)
	 */
	public RowBinaryData(long size, ConnectionPool pool, DBTableMetaObject refetchTable,
			DBAttribute dataColumn, Object[] queryArguments, String name, String contentType) {
		super(size, pool);
		_dataColumn = dataColumn;
		_refetchTable = refetchTable;
		_queryArguments = queryArguments;
		_name = name;
		_contentType = contentType;
	}

	/**
	 * Wraps the given content of a BLOB column of the given item into a {@link RowBinaryData}.
	 *
	 * @param pool
	 *        The pool to read from when fetching the content again.
	 * @param item
	 *        The item whose row holds the content.
	 * @param storage
	 *        The values of the item, providing branch, identifier and revision of the row.
	 * @param dataColumn
	 *        The BLOB column holding the content.
	 * @param content
	 *        The content to wrap.
	 * @param name
	 *        See {@link #RowBinaryData(BinaryData, ConnectionPool, DBTableMetaObject, DBAttribute, Object[], String, String)}.
	 * @param contentType
	 *        See {@link #RowBinaryData(BinaryData, ConnectionPool, DBTableMetaObject, DBAttribute, Object[], String, String)}.
	 * @return The given content, if it is already a {@link RowBinaryData} for the row of the item,
	 *         a new {@link RowBinaryData} otherwise.
	 */
	public static RowBinaryData wrap(ConnectionPool pool, DataObject item, Object[] storage,
			DBAttribute dataColumn, BinaryData content, String name, String contentType) {
		/* Use concrete table of item to create refetch algorithm. The attribute could be defined on
		 * some super class, such that the owner of the attribute points to a different table. */
		DBTableMetaObject table = ((MOStructure) item.tTable()).getDBMapping();
		Object[] queryArgs = createQueryArguments(table, item, storage);

		if (content instanceof RowBinaryData row) {
			if (row.isFor(table, dataColumn, queryArgs)) {
				return row;
			}
			BinaryData loaded = row.getBinaryDataIfPresent();
			if (loaded == null) {
				return new RowBinaryData(row.getSize(), pool, table, dataColumn, queryArgs, name, contentType);
			}
			return new RowBinaryData(loaded, pool, table, dataColumn, queryArgs, name, contentType);
		}
		return new RowBinaryData(content, pool, table, dataColumn, queryArgs, name, contentType);
	}

	private boolean isFor(DBTableMetaObject table, DBAttribute dataColumn, Object[] queryArgs) {
		return _refetchTable == table && _dataColumn == dataColumn && Arrays.equals(_queryArguments, queryArgs);
	}

	@Override
	public String getName() {
		return _name == null ? BinaryData.NO_NAME : _name;
	}

	@Override
	public String getContentType() {
		return _contentType == null ? super.getContentType() : _contentType;
	}

	@Override
	protected BinaryData refetch(PooledConnection connection) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		CompiledStatement statement = createStatement(sqlDialect);
		try (ResultSet result = statement.executeQuery(connection, _queryArguments)) {
			if (result.next()) {
				BinaryData content = sqlDialect.getBlobValue(result, 1);
				if (content == null) {
					throw new SQLException("No binary data found.");
				}
				return content;
			}
			throw new SQLException("No binary data found.");
		}
	}

	/**
	 * Creates the statement to access data for the {@link DBType#BLOB blob} type.
	 *
	 * @implSpec The created statement must be usable with {@link #_queryArguments}.
	 */
	private CompiledStatement createStatement(DBHelper db) {
		DBTableMetaObject table = _refetchTable;
		String tableAlias = "t";

		Parameter[] params;
		int nextParamIndex = 0;

		List<SQLColumnDefinition> columns = columns(columnDef(column(tableAlias, _dataColumn)));

		SQLTableReference from = table(table, tableAlias);

		SQLExpression where = literalTrueLogical();
		if (table.multipleBranches()) {
			DBAttribute branchAttribute = dbAttr(table, BasicTypes.BRANCH_ATTRIBUTE_NAME);
			params = new Parameter[3];
			DBType branchParamType = branchAttribute.getSQLType();
			String branchParamName = "branch";
			where = and(where,
				eq(column(tableAlias, branchAttribute), parameter(branchParamType, branchParamName)));
			params[nextParamIndex++] = parameterDef(branchParamType, branchParamName);
		} else {
			params = new Parameter[2];
		}

		DBAttribute idAttribute = dbAttr(table, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		DBType idParamType = idAttribute.getSQLType();
		String idParamName = "id";
		where = and(where,
			eq(column(tableAlias, idAttribute), parameter(idParamType, idParamName)));
		params[nextParamIndex++] = parameterDef(idParamType, idParamName);

		DBAttribute revMinAttribute = dbAttr(table, BasicTypes.REV_MIN_ATTRIBUTE_NAME);
		DBType revParamType = revMinAttribute.getSQLType();
		String revParamName = "revMin";
		where = and(where,
			eq(column(tableAlias, revMinAttribute), parameter(revParamType, revParamName)));
		params[nextParamIndex++] = parameterDef(revParamType, revParamName);

		return query(parameters(params), select(columns, from, where)).toSql(db);
	}

	private static DBAttribute dbAttr(DBTableMetaObject table, String attrName) {
		return table.getAttribute(attrName).getDbMapping()[0];
	}

	/**
	 * Creates the query arguments to fetch the binary data from the database.
	 *
	 * @implSpec The arguments must be adequate for the {@link CompiledStatement} in
	 *           {@link #createStatement(DBHelper)}.
	 */
	private static Object[] createQueryArguments(DBTableMetaObject table, DataObject item, Object[] storage) {
		Object[] queryArgs;
		int nextArgsIndex = 0;

		if (table.multipleBranches()) {
			queryArgs = new Object[3];
			MOAttribute branchMOAttr = table.getAttribute(BasicTypes.BRANCH_ATTRIBUTE_NAME);
			queryArgs[nextArgsIndex++] = branchMOAttr.getStorage().getCacheValue(branchMOAttr, item, storage);
		} else {
			queryArgs = new Object[2];
		}

		MOAttribute idAttribute = table.getAttribute(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		queryArgs[nextArgsIndex++] = idAttribute.getStorage().getCacheValue(idAttribute, item, storage);

		MOAttribute revMinAttribute = table.getAttribute(BasicTypes.REV_MIN_ATTRIBUTE_NAME);
		queryArgs[nextArgsIndex++] = revMinAttribute.getStorage().getCacheValue(revMinAttribute, item, storage);
		return queryArgs;
	}

}
