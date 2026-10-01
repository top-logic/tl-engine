/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link MOAttribute} whose values reference blobs in a {@link BlobStore}.
 *
 * <p>
 * Each row stores the key of its blob in the {@link #getKeyColumn() key column}; the store is a
 * property of the attribute, not of the row.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlobReferenceAttribute extends MOAttribute {

	/**
	 * The name of the {@link BlobStore} in the {@link BlobStoreService} holding the content,
	 * <code>null</code> for the default store.
	 */
	String getStoreName();

	/**
	 * The column holding the blob key, <code>null</code> in rows without a blob.
	 */
	DBAttribute getKeyColumn();

	/**
	 * The column holding the SHA-256 hash of the blob content as hex string, <code>null</code> in
	 * rows without a blob.
	 */
	DBAttribute getHashColumn();

}
