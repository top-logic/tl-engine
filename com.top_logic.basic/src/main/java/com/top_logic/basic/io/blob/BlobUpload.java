/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import com.top_logic.basic.io.HashingInputStream;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Stores {@link BinaryData} in a {@link BlobStore} and decides between storing content in a blob
 * store and keeping it inline.
 *
 * <p>
 * An upload reads the content exactly once: it is streamed to the store while its
 * {@link HashingInputStream#SHA_256} hash and size are computed. The result is a
 * {@link BlobBinaryData} referencing the stored blob.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BlobUpload {

	/**
	 * The largest threshold for which {@link #uploadAboveThreshold(String, long, BinaryData)}
	 * accepts content of unknown size.
	 *
	 * <p>
	 * To decide about content of unknown size, up to the threshold number of bytes are buffered in
	 * an array, whose size is limited by the virtual machine.
	 * </p>
	 */
	public static final int MAX_BUFFERED_THRESHOLD = Integer.MAX_VALUE - 8;

	private BlobUpload() {
		// Utility class.
	}

	/**
	 * Stores the content of the given data in a blob store.
	 *
	 * @param storeName
	 *        The name of the store in the {@link BlobStoreService}, <code>null</code> for the
	 *        default store.
	 * @param data
	 *        The data to store.
	 * @return The reference to the stored content with the content type and name of the given
	 *         data.
	 */
	public static BlobBinaryData upload(String storeName, BinaryData data) throws IOException {
		long size = data.getSize();
		try (InputStream content = data.getStream()) {
			return upload(storeName, content, size < 0 ? -1 : size, data.getContentType(), data.getName());
		}
	}

	/**
	 * Stores the given content in a blob store.
	 *
	 * @param storeName
	 *        The name of the store in the {@link BlobStoreService}, <code>null</code> for the
	 *        default store.
	 * @param content
	 *        The content to store. The stream is read to its end but not closed.
	 * @param size
	 *        The number of bytes of the content, <code>-1</code> if unknown.
	 * @param contentType
	 *        The content type of the data.
	 * @param name
	 *        The name of the data.
	 * @return The reference to the stored content.
	 */
	public static BlobBinaryData upload(String storeName, InputStream content, long size, String contentType,
			String name) throws IOException {
		BlobStore store = BlobStoreService.getInstance().getStore(storeName);
		HashingInputStream hashing = HashingInputStream.sha256(content);
		String key = store.put(hashing, size, contentType);
		return new BlobBinaryData(storeName, key, hashing.getHash(), hashing.getCount(), contentType, name);
	}

	/**
	 * Ensures that the size of the given data is known.
	 *
	 * @param data
	 *        The data, or <code>null</code>.
	 * @return The given data, if its size is known, a copy of the given data in memory or in the
	 *         temporary file system otherwise.
	 */
	public static BinaryData withKnownSize(BinaryData data) throws IOException {
		if (data == null || data.getSize() >= 0) {
			return data;
		}
		try (InputStream content = data.getStream()) {
			return BinaryDataFactory.createBinaryData(content, -1, data.getContentType(), data.getName());
		}
	}

	/**
	 * Prepares the given data for being stored inline in the database.
	 *
	 * <p>
	 * Content of a {@link BlobBinaryData} is copied, so that it no longer references the blob.
	 * Content of unknown size is copied to determine its size.
	 * </p>
	 *
	 * @param data
	 *        The data, or <code>null</code>.
	 * @return A {@link BinaryData} with known size that is not a {@link BlobBinaryData}, or
	 *         <code>null</code> for <code>null</code>.
	 */
	public static BinaryData inline(BinaryData data) throws IOException {
		if (data instanceof BlobBinaryData) {
			try (InputStream content = data.getStream()) {
				return BinaryDataFactory.createBinaryData(content, data.getSize(), data.getContentType(),
					data.getName());
			}
		}
		return withKnownSize(data);
	}

	/**
	 * Stores the content of the given data in a blob store, if it has at least the given size.
	 *
	 * <p>
	 * If the size of the data is unknown, at most <code>threshold</code> bytes are read into
	 * memory to decide. If the content ends before, it is kept in memory; otherwise the buffered
	 * prefix and the rest of the content are uploaded in one pass.
	 * </p>
	 *
	 * @param storeName
	 *        The name of the store in the {@link BlobStoreService}, <code>null</code> for the
	 *        default store.
	 * @param threshold
	 *        The minimum size in bytes of content to store in the blob store. With a threshold of
	 *        <code>0</code>, all content is stored in the blob store. For content of unknown size,
	 *        the threshold must not be larger than {@link #MAX_BUFFERED_THRESHOLD}.
	 * @param data
	 *        The data to store, or <code>null</code>.
	 * @return A {@link BlobBinaryData} referencing the uploaded content, or a {@link BinaryData}
	 *         with known size smaller than the threshold that is not a {@link BlobBinaryData}.
	 */
	public static BinaryData uploadAboveThreshold(String storeName, long threshold, BinaryData data)
			throws IOException {
		if (data == null) {
			return null;
		}
		long size = data.getSize();
		if (size >= 0) {
			if (size < threshold) {
				return inline(data);
			}
			return upload(storeName, data);
		}

		if (threshold > MAX_BUFFERED_THRESHOLD) {
			throw new IllegalArgumentException("Threshold too large for buffering content of unknown size: "
				+ threshold + " > " + MAX_BUFFERED_THRESHOLD);
		}
		try (InputStream content = data.getStream()) {
			byte[] prefix = content.readNBytes((int) threshold);
			if (prefix.length < threshold) {
				return BinaryDataFactory.createBinaryData(prefix, data.getContentType(), data.getName());
			}
			InputStream all = new SequenceInputStream(new ByteArrayInputStream(prefix), content);
			return upload(storeName, all, -1, data.getContentType(), data.getName());
		}
	}

}
