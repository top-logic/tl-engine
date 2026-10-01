/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.IOException;

/**
 * Signals that a {@link BlobStore} holds no blob with a requested key.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoSuchBlobException extends IOException {

	private final String _storeName;

	private final String _key;

	/**
	 * Creates a {@link NoSuchBlobException}.
	 *
	 * @param storeName
	 *        The name of the {@link BlobStore} that was asked for the key.
	 * @param key
	 *        The key that was not found.
	 */
	public NoSuchBlobException(String storeName, String key) {
		this(storeName, key, null);
	}

	/**
	 * Creates a {@link NoSuchBlobException}.
	 *
	 * @param storeName
	 *        The name of the {@link BlobStore} that was asked for the key.
	 * @param key
	 *        The key that was not found.
	 * @param cause
	 *        The problem reported by the underlying storage, or <code>null</code>.
	 */
	public NoSuchBlobException(String storeName, String key, Throwable cause) {
		super("No blob with key '" + key + "' in blob store '" + storeName + "'.", cause);
		_storeName = storeName;
		_key = key;
	}

	/**
	 * The name of the {@link BlobStore} that was asked for the key.
	 */
	public String getStoreName() {
		return _storeName;
	}

	/**
	 * The key that was not found.
	 */
	public String getKey() {
		return _key;
	}

}
