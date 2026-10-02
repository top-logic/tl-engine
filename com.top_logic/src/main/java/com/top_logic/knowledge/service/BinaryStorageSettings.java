/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.dob.attr.BinaryAttributeKind;

/**
 * Where the content of a binary value of a dynamic attribute is stored.
 *
 * <p>
 * The kind decides between storing the content inline in the database and uploading it to the
 * blob store: {@link BinaryAttributeKind#INLINE} keeps all content inline,
 * {@link BinaryAttributeKind#REF} uploads all content, {@link BinaryAttributeKind#HYBRID} keeps
 * content smaller than the threshold inline and uploads larger content.
 * </p>
 *
 * @param kind
 *        The rule deciding between inline and external content.
 * @param storeName
 *        The name of the {@link BlobStore} in the {@link BlobStoreService} receiving external
 *        content, <code>null</code> for the default store of the service. Ignored for
 *        {@link BinaryAttributeKind#INLINE}.
 * @param threshold
 *        The size in bytes from which on content is stored in the blob store. Only relevant for
 *        {@link BinaryAttributeKind#HYBRID}.
 *
 * @see DynamicBinaryStoragePolicy
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record BinaryStorageSettings(BinaryAttributeKind kind, String storeName, long threshold) {

	/**
	 * Creates settings with the same store and threshold and the given kind.
	 *
	 * @param newKind
	 *        The kind of the result.
	 */
	public BinaryStorageSettings withKind(BinaryAttributeKind newKind) {
		return new BinaryStorageSettings(newKind, storeName, threshold);
	}

	/**
	 * Creates settings with the same kind and threshold and the given store.
	 *
	 * @param newStoreName
	 *        The store name of the result, <code>null</code> for the default store.
	 */
	public BinaryStorageSettings withStoreName(String newStoreName) {
		return new BinaryStorageSettings(kind, newStoreName, threshold);
	}

	/**
	 * Creates settings with the same kind and store and the given threshold.
	 *
	 * @param newThreshold
	 *        The threshold of the result.
	 */
	public BinaryStorageSettings withThreshold(long newThreshold) {
		return new BinaryStorageSettings(kind, storeName, newThreshold);
	}

	/**
	 * Prepares the given content for being stored according to these settings.
	 *
	 * @param data
	 *        The data to store, or <code>null</code>.
	 * @return The data to store inline, or the reference to the uploaded content.
	 *
	 * @see BinaryAttributeKind#toStoredValue(String, long, BinaryData)
	 */
	public BinaryData toStoredValue(BinaryData data) throws IOException {
		return kind.toStoredValue(storeName, threshold, data);
	}

}
