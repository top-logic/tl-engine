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

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;

/**
 * {@link DBAttributeStorageImpl} to store binary data attributes.
 * 
 * <p>
 * When a {@link Types#BLOB} attribute is read from the database, it is stored in the temporary file
 * system. If this content is removed (e.g. by a task), an error would occur. This
 * {@link AttributeStorage} ensures that the content is refetched from the database in this case.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BinaryAttributeStorage extends DBAttributeStorageImpl {

	private static class BinaryDataProxy extends AbstractBinaryData {

		private final BinaryContent _source;

		private volatile long _size = -1;

		public BinaryDataProxy(BinaryContent source) {
			_source = source;
		}

		@Override
		public long getSize() {
			if (_size == -1) {
				try {
					try (InputStream stream = getStream()) {
						_size = StreamUtilities.size(stream);
					}
				} catch (IOException ex) {
					// Size can not be determined.
				}
			}
			return _size;
		}

		@Override
		public String getContentType() {
			return BinaryData.CONTENT_TYPE_OCTET_STREAM;
		}

		@Override
		public String getName() {
			return BinaryData.NO_NAME;
		}

		@Override
		public InputStream getStream() throws IOException {
			return _source.getStream();
		}

	}

	private static class RowDBBinaryData extends DBBinaryData {

		private final MOAttribute _blobAttribute;

		private final Object[] _queryArguments;

		private final DBTableMetaObject _refetchTable;

		public RowDBBinaryData(BinaryData original, ConnectionPool aPool, DBTableMetaObject refetchTable,
				MOAttribute blobAttribute, Object[] queryArguments) {
			super(original, aPool);
			_blobAttribute = blobAttribute;
			_refetchTable = refetchTable;
			_queryArguments = queryArguments;
		}

		@Override
		public String getName() {
			return BinaryData.NO_NAME;
		}

		@Override
		protected BinaryData refetch(PooledConnection connection) throws SQLException {
			DBHelper sqlDialect = connection.getSQLDialect();
			CompiledStatement statement = createStatement(sqlDialect);
			try (ResultSet result = statement.executeQuery(connection, _queryArguments)) {
				if (result.next()) {
					return sqlDialect.getBlobValue(result, 1);
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

			List<SQLColumnDefinition> columns = columns(columnDef(column(tableAlias, dbAttribute(_blobAttribute))));

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

		private DBAttribute dbAttr(DBTableMetaObject table, String attrName) {
			return dbAttribute(table.getAttribute(attrName));
		}

		private DBAttribute dbAttribute(MOAttribute moAttr) {
			return moAttr.getDbMapping()[0];
		}

	}

	/** Singleton {@link BinaryAttributeStorage} instance. */
	public static final BinaryAttributeStorage INSTANCE = new BinaryAttributeStorage();

	/**
	 * Creates a new {@link BinaryAttributeStorage}.
	 */
	protected BinaryAttributeStorage() {
		// singleton instance
	}

	@Override
	protected Object fromCacheToDBValue(MOAttribute attribute, Object cacheValue) {
		return cacheValue;
	}

	@Override
	protected Object fromDBToCacheValue(MOAttribute attribute, Object dbValue) {
		return dbValue;
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		super.loadValue(pool, dbResult, resultOffset, attribute, item, storage, context);

		wrapCacheValue(pool, attribute, item, storage);
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		super.storeValue(pool, stmtArgs, stmtOffset, attribute, item, storage, currentCommitNumber);

		wrapCacheValue(pool, attribute, item, storage);
	}

	private void wrapCacheValue(ConnectionPool pool, MOAttribute attribute, DataObject item, Object[] storage) {
		BinaryContent cacheValue = (BinaryContent) getCacheValue(attribute, item, storage);
		if (cacheValue == null) {
			return;
		}
		if (cacheValue instanceof RowDBBinaryData) {
			// already wrapped
			return;
		}
		/* Use concrete table of item to create refetch algorithm. The attribute could be defined on
		 * some super class, such that the owner of the attribute points to a differen table. */
		DBTableMetaObject table = ((MOStructure) item.tTable()).getDBMapping();

		Object[] queryArgs = createQueryArguments(table, item, storage);

		BinaryContent proxy;
		if (cacheValue instanceof BinaryData) {
			proxy = new RowDBBinaryData((BinaryData) cacheValue, pool, table, attribute, queryArgs);
		} else {
			proxy = new RowDBBinaryData(new BinaryDataProxy(cacheValue), pool, table, attribute, queryArgs);
		}
		setSimpleCacheValue(attribute, storage, proxy);
	}

	/**
	 * Creates the query arguments to fetch the binary data from the database.
	 * 
	 * @implSpec The arguments must be adequate for the {@link CompiledStatement} in
	 *           {@link RowDBBinaryData#createStatement(DBHelper)}.
	 */
	private Object[] createQueryArguments(DBTableMetaObject table, DataObject item, Object[] storage) {
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
