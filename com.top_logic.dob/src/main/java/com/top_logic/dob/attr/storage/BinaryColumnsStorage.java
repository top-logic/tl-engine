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

		DBAttribute keyColumn = binaryAttribute.getKeyColumn();
		if (keyColumn != null) {
			String key = dbResult.getString(index(resultOffset, keyColumn));
			if (key != null) {
				String hash = dbResult.getString(index(resultOffset, binaryAttribute.getHashColumn()));
				long size = dbResult.getLong(index(resultOffset, binaryAttribute.getSizeColumn()));
				String contentType = dbResult.getString(index(resultOffset, binaryAttribute.getContentTypeColumn()));
				String name = dbResult.getString(index(resultOffset, binaryAttribute.getNameColumn()));
				return new BlobBinaryData(binaryAttribute.getStoreName(), key, hash, size, contentType, name);
			}
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

		DBAttribute keyColumn = binaryAttribute.getKeyColumn();
		DBAttribute hashColumn = binaryAttribute.getHashColumn();
		DBAttribute dataColumn = binaryAttribute.getDataColumn();

		if (value == null) {
			if (keyColumn != null) {
				storeObject(keyColumn, stmtArgs, stmtOffset, item, null);
				storeObject(hashColumn, stmtArgs, stmtOffset, item, null);
			}
			storeObject(binaryAttribute.getSizeColumn(), stmtArgs, stmtOffset, item, null);
			storeObject(binaryAttribute.getContentTypeColumn(), stmtArgs, stmtOffset, item, null);
			storeObject(binaryAttribute.getNameColumn(), stmtArgs, stmtOffset, item, null);
			if (dataColumn != null) {
				storeObject(dataColumn, stmtArgs, stmtOffset, item, null);
			}
			return;
		}

		boolean external = keyColumn != null && value instanceof BlobBinaryData;
		if (external) {
			BlobBinaryData blob = (BlobBinaryData) value;
			if (!Objects.equals(blob.getStoreName(), binaryAttribute.getStoreName())) {
				throw new SQLException("Blob '" + blob.getKey() + "' of attribute '" + attribute.getName()
					+ "' is not stored in the store of the attribute.");
			}
			storeObject(keyColumn, stmtArgs, stmtOffset, item, blob.getKey());
			storeObject(hashColumn, stmtArgs, stmtOffset, item, blob.getHash());
		} else {
			if (dataColumn == null) {
				throw new SQLException("Content '" + value.getName() + "' of attribute '" + attribute.getName()
					+ "' has not been stored in a blob store.");
			}
			if (keyColumn != null) {
				storeObject(keyColumn, stmtArgs, stmtOffset, item, null);
				storeObject(hashColumn, stmtArgs, stmtOffset, item, null);
			}
		}

		long size = value.getSize();
		if (size < 0) {
			throw new SQLException("Size of content '" + value.getName() + "' of attribute '" + attribute.getName()
				+ "' is unknown.");
		}
		storeObject(binaryAttribute.getSizeColumn(), stmtArgs, stmtOffset, item, Long.valueOf(size));
		storeObject(binaryAttribute.getContentTypeColumn(), stmtArgs, stmtOffset, item,
			truncate(binaryAttribute.getContentTypeColumn(), value.getContentType()));
		storeObject(binaryAttribute.getNameColumn(), stmtArgs, stmtOffset, item,
			truncate(binaryAttribute.getNameColumn(), storedName(value)));

		if (dataColumn != null) {
			storeObject(dataColumn, stmtArgs, stmtOffset, item, external ? null : value);
		}

		wrapInlineContent(pool, binaryAttribute, item, storage);
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
