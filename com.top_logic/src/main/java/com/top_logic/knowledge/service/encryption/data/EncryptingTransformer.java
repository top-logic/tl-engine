/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.encryption.SymmetricEncryption;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.TransformingFlexDataManager.DataTransformation;

/**
 * {@link DataTransformation} that encrypts all {@link BinaryData} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EncryptingTransformer implements DataTransformation {

	final SymmetricEncryption _encryption;

	/**
	 * Creates a {@link EncryptingTransformer}.
	 */
	public EncryptingTransformer() {
		SymmetricEncryption encryption;
		try {
			encryption = EncryptionService.getInstance().getEncryption();
		} catch (IllegalStateException ex) {
			// Do not break the DB setup.
			encryption = null;
		}
		_encryption = encryption;
	}

	@Override
	public FlexData encode(String typeName, FlexData values) {
		return ((CryptingValues) values).getImpl();
	}

	@Override
	public FlexData decode(FlexData values, boolean mutable) {
		return new CryptingValues(values, mutable);
	}

	private class CryptingValues implements FlexData {
		
		private final FlexData _impl;
		
		private final boolean _mutable;
		
		public CryptingValues(FlexData impl, boolean mutable) {
			_impl = impl;
			_mutable = mutable;
		}
		
		public FlexData getImpl() {
			return _impl;
		}

		@Override
		public long lastModified(String attributeName) {
			return _impl.lastModified(attributeName);
		}

		@Override
		public Collection<String> getAttributes() {
			return _impl.getAttributes();
		}
		
		@Override
		public boolean hasAttribute(String attributeName) {
			return _impl.hasAttribute(attributeName);
		}

		@Override
		public Object getAttributeValue(String attributeName) {
			Object storageValue = _impl.getAttributeValue(attributeName);
			
			Object value;
			if (storageValue instanceof BinaryDataSource) {
				value = new DecryptedData(BinaryData.cast(storageValue));
			} else {
				value = storageValue;
			}

			return value;
		}
		
		@Override
		public Object setAttributeValue(String attributeName, Object value) throws DataObjectException {
			if (!_mutable) {
				throw new IllegalStateException("Immutable object.");
			}

			Object storageValue;
			if (value instanceof BinaryDataSource) {
				storageValue = new EncryptedData(BinaryData.cast(value));
			} else {
				storageValue = value;
			}

			return _impl.setAttributeValue(attributeName, storageValue);
		}

	}

	private class EncryptedData extends AbstractBinaryData {
	
		private final BinaryData _dataImpl;

		/**
		 * Creates a {@link EncryptedData}.
		 * 
		 * @param impl
		 *        The wrapped {@link BinaryData}.
		 */
		public EncryptedData(BinaryData impl) {
			_dataImpl = impl;
		}
	
		@Override
		public InputStream getStream() throws IOException {
			boolean success = false;
			InputStream in = _dataImpl.getStream();
			try {
				InputStream result = _encryption.encrypt(in);
				success = true;
				return result;
			} finally {
				if (!success) {
					in.close();
				}
			}
		}

		@Override
		public long getSize() {
			long plainTextSize = _dataImpl.getSize();
			return _encryption.getCipherTextSize(plainTextSize);
		}

		@Override
		public String getName() {
			return _dataImpl.getName();
		}

		@Override
		public String getContentType() {
			return _dataImpl.getContentType();
		}

	}
	private class DecryptedData extends AbstractBinaryData {

		private final BinaryData _dataImpl;

		/**
		 * Creates a {@link EncryptedData}.
		 * 
		 * @param impl
		 *        The wrapped {@link BinaryData}.
		 */
		public DecryptedData(BinaryData impl) {
			_dataImpl = impl;
		}

		@Override
		public InputStream getStream() throws IOException {
			boolean success = false;
			InputStream in = _dataImpl.getStream();
			try {
				InputStream result = _encryption.decrypt(in);
				success = true;
				return result;
			} finally {
				if (!success) {
					in.close();
				}
			}
		}

		@Override
		public long getSize() {
			long chipherTextSize = _dataImpl.getSize();
			return _encryption.getPlainTextSize(chipherTextSize);
		}

		@Override
		public String getName() {
			return _dataImpl.getName();
		}

		@Override
		public String getContentType() {
			return _dataImpl.getContentType();
		}

	}
}
