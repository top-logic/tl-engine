/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.ToIntFunction;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link AttributeStorage} of an {@link AbstractBinaryAttribute}, storing a {@link BinaryData}
 * value in the columns of the attribute.
 *
 * <p>
 * The cache value is the application value. When a value is assigned, the attribute prepares it
 * for storage (see {@link AbstractBinaryAttribute#toStoredValue(BinaryData)}); content for a blob
 * store is uploaded at this time, so that writing the row only writes plain column values. A
 * {@link BlobBinaryData} value is stored as blob key, hash and metadata, all other values are
 * stored inline in the BLOB column of the attribute.
 * </p>
 *
 * <p>
 * Inline content read from the database is kept as {@link RowBinaryData}, which refetches the
 * content if its temporary copy vanishes. Content in a blob store is read as {@link BlobBinaryData}
 * without accessing any LOB column.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryColumnsStorage extends AbstractMOAttributeStorageImpl {

	/** Singleton {@link BinaryColumnsStorage} instance. */
	public static final BinaryColumnsStorage INSTANCE = new BinaryColumnsStorage();

	/**
	 * Creates a {@link BinaryColumnsStorage}.
	 */
	protected BinaryColumnsStorage() {
		// Singleton constructor.
	}

	private static AbstractBinaryAttribute binaryAttribute(MOAttribute attribute) {
		return (AbstractBinaryAttribute) attribute;
	}

	@Override
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue) {
		return cacheValue;
	}

	/**
	 * Prepares the given value for storage; uploads content to a blob store, if the attribute
	 * stores it there.
	 *
	 * @throws UncheckedIOException
	 *         If reading or uploading the content fails.
	 */
	@Override
	public Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue) {
		BinaryData data = BinaryData.cast(applicationValue);
		if (data == null) {
			return null;
		}
		try {
			return binaryAttribute(attribute).toStoredValue(data);
		} catch (IOException ex) {
			throw new UncheckedIOException(
				"Storing content '" + data.getName() + "' for attribute '" + attribute.getName() + "' failed.", ex);
		}
	}

	@Override
	public Object getApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage) {
		return getCacheValue(attribute, item, storage);
	}

	@Override
	public Object setApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		return setCacheValue(attribute, item, storage, fromApplicationToCacheValue(attribute, applicationValue));
	}

	@Override
	protected void defaultCheck(MOAttribute attribute, DataObject data, Object value)
			throws IncompatibleTypeException, DataObjectException {
		checkImmutable(attribute, data, value);
		if (value == null || value instanceof BinaryDataSource) {
			return;
		}
		StringBuilder incompatibleType = new StringBuilder();
		incompatibleType.append("Incompatible type '");
		incompatibleType.append(value.getClass().getName());
		incompatibleType.append("' for binary attribute '");
		appendAttribute(incompatibleType, attribute);
		incompatibleType.append("', expected '");
		incompatibleType.append(BinaryData.class.getName());
		incompatibleType.append("'.");
		throw new IncompatibleTypeException(incompatibleType.toString());
	}

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		AbstractBinaryAttribute binaryAttribute = binaryAttribute(attribute);

		BlobBinaryData blob = fetchReference(dbResult, binaryAttribute, column -> index(resultOffset, column));
		if (blob != null) {
			return blob;
		}

		DBAttribute dataColumn = binaryAttribute.getDataColumn();
		if (dataColumn != null) {
			String name = dbResult.getString(index(resultOffset, binaryAttribute.getNameColumn()));
			return DBBinaryData.fromBlobColumn(sqlDialect, dbResult, name,
				index(resultOffset, binaryAttribute.getContentTypeColumn()),
				index(resultOffset, binaryAttribute.getSizeColumn()),
				index(resultOffset, dataColumn));
		}

		return null;
	}

	/**
	 * Reads the reference to a blob from the columns of the given attribute.
	 *
	 * @param dbResult
	 *        The result positioned at the row to read.
	 * @param attribute
	 *        The attribute whose columns are read.
	 * @param columnIndex
	 *        The index of each column of the attribute in the given result.
	 * @return The reference to the blob of the row, <code>null</code> if the row has no blob
	 *         reference, e.g. because it stores its content inline or has no value.
	 */
	public static BlobBinaryData fetchReference(ResultSet dbResult, AbstractBinaryAttribute attribute,
			ToIntFunction<DBAttribute> columnIndex) throws SQLException {
		DBAttribute keyColumn = attribute.getKeyColumn();
		if (keyColumn == null) {
			return null;
		}
		String key = dbResult.getString(columnIndex.applyAsInt(keyColumn));
		if (key == null) {
			return null;
		}
		String hash = dbResult.getString(columnIndex.applyAsInt(attribute.getHashColumn()));
		DBAttribute storeColumn = attribute.getStoreColumn();
		String storeName = storeColumn != null ? dbResult.getString(columnIndex.applyAsInt(storeColumn))
			: attribute.getStoreName();
		long size = dbResult.getLong(columnIndex.applyAsInt(attribute.getSizeColumn()));
		String contentType = dbResult.getString(columnIndex.applyAsInt(attribute.getContentTypeColumn()));
		String name = dbResult.getString(columnIndex.applyAsInt(attribute.getNameColumn()));
		return new BlobBinaryData(storeName, key, hash, size, contentType, name);
	}

	private static int index(int resultOffset, DBAttribute column) {
		return resultOffset + column.getDBColumnIndex();
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		super.loadValue(pool, dbResult, resultOffset, attribute, item, storage, context);

		wrapInlineContent(pool, binaryAttribute(attribute), item, storage);
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		AbstractBinaryAttribute binaryAttribute = binaryAttribute(attribute);
		BinaryData value = (BinaryData) getCacheValue(attribute, item, storage);

		Object[] columnValues = columnValues(binaryAttribute, value);
		DBAttribute[] columns = binaryAttribute.getDbMapping();
		for (int n = 0; n < columns.length; n++) {
			storeObject(columns[n], stmtArgs, stmtOffset, item, columnValues[n]);
		}

		if (value != null) {
			wrapInlineContent(pool, binaryAttribute, item, storage);
		}
	}

	/**
	 * The values of the columns of the given attribute storing the given value.
	 *
	 * <p>
	 * A {@link BlobBinaryData} is stored as reference to its blob, if the attribute stores blob
	 * references, all other values are stored inline. The size of an inline value must be known.
	 * </p>
	 *
	 * @param attribute
	 *        The attribute to store the value in.
	 * @param value
	 *        The value to store, <code>null</code> for no value.
	 * @return The column values in the order of {@link AbstractBinaryAttribute#getDbMapping()}. The
	 *         value of the BLOB column is the given {@link BinaryData} itself.
	 * @throws SQLException
	 *         If the value cannot be stored in the columns of the attribute.
	 */
	public static Object[] columnValues(AbstractBinaryAttribute attribute, BinaryData value) throws SQLException {
		DBAttribute[] columns = attribute.getDbMapping();
		Object[] result = new Object[columns.length];
		if (value == null) {
			return result;
		}

		DBAttribute keyColumn = attribute.getKeyColumn();
		DBAttribute storeColumn = attribute.getStoreColumn();
		DBAttribute dataColumn = attribute.getDataColumn();

		boolean external = keyColumn != null && value instanceof BlobBinaryData;
		if (external) {
			BlobBinaryData blob = (BlobBinaryData) value;
			if (storeColumn != null) {
				set(result, columns, storeColumn, blob.getStoreName());
			} else if (!Objects.equals(blob.getStoreName(), attribute.getStoreName())) {
				throw new SQLException("Blob '" + blob.getKey() + "' of attribute '" + attribute.getName()
					+ "' is not stored in the store of the attribute.");
			}
			set(result, columns, keyColumn, blob.getKey());
			set(result, columns, attribute.getHashColumn(), blob.getHash());
		} else if (dataColumn == null) {
			throw new SQLException("Content '" + value.getName() + "' of attribute '" + attribute.getName()
				+ "' has not been stored in a blob store.");
		}

		long size = value.getSize();
		if (size < 0) {
			throw new SQLException("Size of content '" + value.getName() + "' of attribute '" + attribute.getName()
				+ "' is unknown.");
		}
		set(result, columns, attribute.getSizeColumn(), Long.valueOf(size));
		set(result, columns, attribute.getContentTypeColumn(),
			truncate(attribute.getContentTypeColumn(), value.getContentType()));
		set(result, columns, attribute.getNameColumn(), truncate(attribute.getNameColumn(), storedName(value)));

		if (dataColumn != null && !external) {
			set(result, columns, dataColumn, value);
		}
		return result;
	}

	private static void set(Object[] values, DBAttribute[] columns, DBAttribute column, Object value) {
		for (int n = 0; n < columns.length; n++) {
			if (columns[n] == column) {
				values[n] = value;
				return;
			}
		}
		throw new IllegalArgumentException("Column '" + column.getDBName() + "' is not a column of the attribute.");
	}

	private static String storedName(BinaryData value) {
		String name = value.getName();
		if (name == null || name.isEmpty() || BinaryData.NO_NAME.equals(name)) {
			return null;
		}
		return name;
	}

	private static String truncate(DBAttribute column, String value) {
		if (value == null) {
			return null;
		}
		int maxLength = column.getSQLSize();
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}

	private void wrapInlineContent(ConnectionPool pool, AbstractBinaryAttribute attribute, DataObject item,
			Object[] storage) {
		DBAttribute dataColumn = attribute.getDataColumn();
		if (dataColumn == null) {
			return;
		}
		BinaryData value = (BinaryData) getCacheValue(attribute, item, storage);
		if (value == null || (attribute.getKeyColumn() != null && value instanceof BlobBinaryData)) {
			return;
		}
		setSimpleCacheValue(attribute, storage,
			RowBinaryData.wrap(pool, item, storage, dataColumn, value, value.getName(), value.getContentType()));
	}

}
