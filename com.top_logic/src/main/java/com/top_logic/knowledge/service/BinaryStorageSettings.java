/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;

/**
 * Where the content of a binary value of a dynamic attribute is stored.
 *
 * <p>
 * Content smaller than the threshold is stored inline in the database, larger content is uploaded
 * to the blob store.
 * </p>
 *
 * @param storeName
 *        The name of the {@link BlobStore} in the {@link BlobStoreService} receiving large content,
 *        <code>null</code> for the default store of the service.
 * @param threshold
 *        The size in bytes from which on content is stored in the blob store.
 *
 * @see DynamicBinaryStoragePolicy
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record BinaryStorageSettings(String storeName, long threshold) {

	/**
	 * Creates settings with the same threshold and the given store.
	 *
	 * @param newStoreName
	 *        The store name of the result, <code>null</code> for the default store.
	 */
	public BinaryStorageSettings withStoreName(String newStoreName) {
		return new BinaryStorageSettings(newStoreName, threshold);
	}

	/**
	 * Creates settings with the same store and the given threshold.
	 *
	 * @param newThreshold
	 *        The threshold of the result.
	 */
	public BinaryStorageSettings withThreshold(long newThreshold) {
		return new BinaryStorageSettings(storeName, newThreshold);
	}

}
