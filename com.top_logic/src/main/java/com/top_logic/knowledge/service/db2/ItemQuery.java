/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.ResultSetProxy;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Query for whole rows of a type (all row attributes of a knowledge item).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemQuery {

	final DBHelper sqlDialect;
	
	private final MOClass type;
	
	private final CompiledStatement _statement;

	/**
	 * Creates an {@link ItemQuery}.
	 * 
	 * @param sqlDialect
	 *        See {@link #getSqlDialect()}
	 * @param type
	 *        See {@link #getType()}
	 */
	public ItemQuery(DBHelper sqlDialect, MOClass type, String tableAlias, SQLExpression filter, List<SQLOrder> order) {
		this.sqlDialect = sqlDialect;
		this.type = type;
		
		List<SQLColumnDefinition> columns = allColumnRefs(type, tableAlias);
		SQLTableReference table = table(type.getDBMapping(), tableAlias);

		_statement =
			SQLFactory.query(select(columns, table, filter, order)).toSql(sqlDialect);
	}
	
	/**
	 * The SQL dialect, in which this query is constructed.
	 */
	public DBHelper getSqlDialect() {
		return sqlDialect;
	}
	
	/**
	 * The concrete type from which items are read.
	 */
	public MOClass getType() {
		return type;
	}

	/**
	 * Retrieves all rows from the given connection matching this {@link ItemQuery}.
	 */
	public ItemResult query(Connection connection) throws SQLException {
		ResultSet result = _statement.executeQuery(connection);
		return DirectItemResult.createDirectItemResult(sqlDialect, result);
	}

	/**
	 * Result of a {@link ItemQuery}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DirectItemResult extends ResultSetProxy implements ItemResult {

		private final DBHelper _sqlDialect;

		private final int _offset;

		private final ResultSet _resultSet;

		private DirectItemResult(DBHelper sqlDialect, ResultSet resultSet, int offset) {
			this._sqlDialect = sqlDialect;
			_resultSet = resultSet;
			this._offset = offset;
		}

		@Override
		protected ResultSet impl() {
			return _resultSet;
		}

		public static DirectItemResult createDirectItemResult(DBHelper sqlDialect, ResultSet resultSet) {
			return createDirectItemResult(sqlDialect, resultSet, DBAttribute.DEFAULT_DB_OFFSET);
		}
		
		public static DirectItemResult createDirectItemResult(DBHelper sqlDialect, ResultSet resultSet, int offset) {
			return new DirectItemResult(sqlDialect, resultSet, offset);
		}
		
		@Override
		public Object getValue(MOAttribute attribute, ObjectContext context) throws SQLException {
			return attribute.getStorage().fetchValue(_sqlDialect, this, _offset, attribute, context);
		}

		@Override
		public String getStringValue(DBAttribute attribute) throws SQLException {
			return getString(attribute.getDBColumnIndex() + _offset);
		}
		
		@Override
		public String getStringValue(MOReference attribute, ReferencePart part) throws SQLException {
			return getString(attribute.getColumn(part).getDBColumnIndex() + _offset);
		}

		@Override
		public String getClobStringValue(DBAttribute attribute) throws SQLException {
			return getClobStringValue(_sqlDialect, this, attribute.getDBColumnIndex() + _offset);
		}

		/**
		 * Returns the clob value taken from the given {@link ResultSet}.
		 * 
		 * @param resultSet
		 *        The {@link ResultSet} to get data from
		 * @param columnIndex
		 *        the index in the {@link ResultSet} to take data from
		 */
		public static String getClobStringValue(DBHelper sqlDialect, ResultSet resultSet, int columnIndex) throws SQLException {
			return sqlDialect.getClobValue(resultSet, columnIndex);
		}
		
        @Override
		public BinaryData getBlobValue(DBAttribute nameAttr, DBAttribute contentTypeAttr, DBAttribute sizeAttr,
				DBAttribute blobAttr) throws SQLException {
			int sizeIndex = sizeAttr.getDBColumnIndex() + _offset;
			int contentTypeIndex = contentTypeAttr.getDBColumnIndex() + _offset;
			int blobIndex = blobAttr.getDBColumnIndex() + _offset;

			return DBBinaryData.fromBlobColumn(_sqlDialect, this, getClobStringValue(nameAttr), contentTypeIndex,
				sizeIndex, blobIndex);
        }

		@Override
		public int getIntValue(DBAttribute attribute) throws SQLException {
			return getInt(attribute.getDBColumnIndex() + _offset);
		}
		
		@Override
		public long getLongValue(DBAttribute attribute) throws SQLException {
			return getLong(attribute.getDBColumnIndex() + _offset);
		}

		@Override
		public float getFloatValue(DBAttribute attribute) throws SQLException {
			return getFloat(attribute.getDBColumnIndex() + _offset);
		}
		
		@Override
		public double getDoubleValue(DBAttribute attribute) throws SQLException {
			return getDouble(attribute.getDBColumnIndex() + _offset);
		}
		
		@Override
		public byte getByteValue(DBAttribute attribute) throws SQLException {
			return getByte(attribute.getDBColumnIndex() + _offset);
		}
		
		@Override
		public TLID getIDValue(DBAttribute attribute) throws SQLException {
			return IdentifierUtil.getId(this, attribute.getDBColumnIndex() + _offset);
		}
	}

}
