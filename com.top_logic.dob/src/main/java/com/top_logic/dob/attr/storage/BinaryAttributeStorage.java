/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;

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
		BinaryData content =
			cacheValue instanceof BinaryData data ? data : new BinaryDataProxy(cacheValue);
		setSimpleCacheValue(attribute, storage,
			RowBinaryData.wrap(pool, item, storage, dbAttribute(attribute), content, null, null));
	}

}
