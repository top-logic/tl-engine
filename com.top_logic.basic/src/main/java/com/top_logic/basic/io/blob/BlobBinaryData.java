/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.HashingInputStream;
import com.top_logic.basic.io.VerifyingInputStream;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DirectDownload;

/**
 * {@link BinaryData} whose content is stored as blob in a {@link BlobStore}.
 *
 * <p>
 * The value is immutable and only references the content: it consists of the name of the store,
 * the key of the blob, the {@link HashingInputStream#SHA_256} hash and size of the content, and
 * the content type and name of the data. The content is read lazily from the store; the store is
 * resolved by its name in the {@link BlobStoreService} when the content is accessed.
 * </p>
 *
 * <p>
 * A complete read through {@link #getStream()} checks the content against the stored hash and size
 * and fails with an {@link IOException} on a mismatch. A range read through
 * {@link #getStream(long, long)} is not checked.
 * </p>
 *
 * <p>
 * A {@link DirectDownload} is offered if the store issues download URLs, see
 * {@link BlobStore#createDownloadUrl(String, long, String, String)}.
 * </p>
 *
 * @see BlobUpload
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BlobBinaryData extends AbstractBinaryData implements DirectDownload {

	private final String _storeName;

	private final String _key;

	private final String _hash;

	private final long _size;

	private final String _contentType;

	private final String _name;

	/**
	 * Creates a {@link BlobBinaryData}.
	 *
	 * @param storeName
	 *        See {@link #getStoreName()}.
	 * @param key
	 *        See {@link #getKey()}.
	 * @param hash
	 *        See {@link #getHash()}.
	 * @param size
	 *        See {@link #getSize()}.
	 * @param contentType
	 *        See {@link #getContentType()}. <code>null</code> means
	 *        {@link BinaryData#CONTENT_TYPE_OCTET_STREAM}.
	 * @param name
	 *        See {@link #getName()}. <code>null</code> or empty means {@link BinaryData#NO_NAME}.
	 */
	public BlobBinaryData(String storeName, String key, String hash, long size, String contentType,
			String name) {
		_storeName = StringServices.isEmpty(storeName) ? null : storeName;
		_key = Objects.requireNonNull(key, "Blob key must not be null.");
		_hash = Objects.requireNonNull(hash, "Blob hash must not be null.");
		_size = size;
		_contentType = nonNullContentType(contentType);
		_name = StringServices.isEmpty(name) ? BinaryData.NO_NAME : name;
	}

	/**
	 * The name of the {@link BlobStore} in the {@link BlobStoreService}, <code>null</code> for
	 * the default store.
	 */
	public String getStoreName() {
		return _storeName;
	}

	/**
	 * The key of the blob in its store.
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * The {@link HashingInputStream#SHA_256} hash of the content as lower case hex string.
	 */
	public String getHash() {
		return _hash;
	}

	@Override
	public long getSize() {
		return _size;
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

	@Override
	public String getName() {
		return _name;
	}

	/**
	 * The store holding the content.
	 *
	 * @throws IllegalArgumentException
	 *         If no store with the name {@link #getStoreName()} is configured.
	 */
	public BlobStore getStore() {
		return BlobStoreService.getInstance().getStore(_storeName);
	}

	@Override
	public InputStream getStream() throws IOException {
		return new VerifyingInputStream(getStore().get(_key), _hash, _size, describe());
	}

	/**
	 * Reads a range of the content without checking its integrity.
	 *
	 * @see BlobStore#get(String, long, long)
	 */
	public InputStream getStream(long offset, long length) throws IOException {
		return getStore().get(_key, offset, length);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The URL is created by the store of the content. It is <code>null</code> if the store does
	 * not issue download URLs for this content.
	 * </p>
	 *
	 * @see BlobStore#createDownloadUrl(String, long, String, String)
	 */
	@Override
	public URI getDirectDownloadUrl() {
		String fileName = BinaryData.NO_NAME.equals(_name) ? null : _name;
		return getStore().createDownloadUrl(_key, _size, _contentType, fileName);
	}

	private String describe() {
		return "blob '" + _key + "' in store '" + (_storeName == null ? "<default>" : _storeName) + "'";
	}

	/**
	 * Compares the content with the given object.
	 *
	 * <p>
	 * Two {@link BlobBinaryData} are compared by their hashes without reading their content. All
	 * other cases compare the content.
	 * </p>
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other instanceof BlobBinaryData otherBlob) {
			return _size == otherBlob._size && _hash.equalsIgnoreCase(otherBlob._hash);
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
