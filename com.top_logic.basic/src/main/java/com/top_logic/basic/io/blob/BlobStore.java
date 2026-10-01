/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.stream.Stream;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Storage for immutable binary content.
 *
 * <p>
 * Each stored content (blob) gets a random key chosen by the store. A key is never reused and a
 * blob is never overwritten; content only changes by storing a new blob and deleting the old one.
 * The store keeps no metadata besides size and modification time; content type, name and hash of
 * the content are kept by the caller.
 * </p>
 *
 * <p>
 * Named stores are configured in the {@link BlobStoreService}, which closes them when it is shut
 * down.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlobStore extends AutoCloseable {

	/**
	 * Configuration of a {@link BlobStore}.
	 */
	interface Config<I extends BlobStore> extends PolymorphicConfiguration<I>, NamedConfigMandatory {

		/**
		 * The name under which the store is registered in the {@link BlobStoreService}.
		 */
		@Override
		String getName();

	}

	/**
	 * The name of this store in the {@link BlobStoreService}.
	 */
	String getName();

	/**
	 * Stores the given content under a new key.
	 *
	 * <p>
	 * The content is completely stored when this method returns. If storing fails, no blob is
	 * visible under any key.
	 * </p>
	 *
	 * @param content
	 *        The content to store. The stream is read to its end but not closed.
	 * @param size
	 *        The number of bytes of the content, or <code>-1</code> if unknown. If the size is
	 *        given and the content has a different size, storing fails.
	 * @param contentType
	 *        The MIME type of the content, or <code>null</code>. A store may pass it on to the
	 *        underlying storage; it is not part of the information read back from the store.
	 * @return The new key of the stored content, never used before.
	 */
	String put(InputStream content, long size, String contentType) throws IOException;

	/**
	 * Reads the complete content of the blob with the given key.
	 *
	 * @param key
	 *        A key returned by {@link #put(InputStream, long, String)}.
	 * @return The content. The caller must close the stream.
	 * @throws NoSuchBlobException
	 *         If there is no blob with the given key.
	 */
	InputStream get(String key) throws IOException;

	/**
	 * Reads a range of the content of the blob with the given key.
	 *
	 * @param key
	 *        A key returned by {@link #put(InputStream, long, String)}.
	 * @param offset
	 *        The position of the first byte to read. Must not be negative. An offset at or beyond
	 *        the end of the content delivers an empty stream.
	 * @param length
	 *        The maximum number of bytes to read. Must not be negative. A range exceeding the end
	 *        of the content delivers the bytes up to the end.
	 * @return The requested range of the content. The caller must close the stream.
	 * @throws NoSuchBlobException
	 *         If there is no blob with the given key.
	 */
	InputStream get(String key, long offset, long length) throws IOException;

	/**
	 * Deletes the blob with the given key.
	 *
	 * <p>
	 * Deleting a key that does not exist (any more) is not an error.
	 * </p>
	 *
	 * @param key
	 *        A key returned by {@link #put(InputStream, long, String)}.
	 */
	void delete(String key) throws IOException;

	/**
	 * Lists all blobs of this store in lexicographic order of their keys.
	 *
	 * <p>
	 * The listing is delivered lazily; blobs stored or deleted while the stream is consumed may or
	 * may not be reported.
	 * </p>
	 *
	 * @return The {@link BlobInfo} of all stored blobs. The caller must close the stream.
	 */
	Stream<BlobInfo> list() throws IOException;

	/**
	 * Removes temporary artifacts of this store (e.g. leftovers of failed or interrupted uploads)
	 * that were created before the given time.
	 *
	 * <p>
	 * Stored blobs are not affected.
	 * </p>
	 *
	 * @param olderThan
	 *        Only artifacts older than this time are removed, so that uploads in progress are not
	 *        disturbed.
	 */
	default void cleanup(Instant olderThan) throws IOException {
		// No temporary artifacts by default.
	}

	/**
	 * Releases the resources held by this store, e.g. the client of a remote storage.
	 *
	 * <p>
	 * Called by the {@link BlobStoreService} when it is shut down. The store must not be used
	 * afterwards.
	 * </p>
	 */
	@Override
	default void close() throws IOException {
		// No resources by default.
	}

}
