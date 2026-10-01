/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Base class for configured {@link BlobStore} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBlobStore<C extends BlobStore.Config<?>> extends AbstractConfiguredInstance<C>
		implements BlobStore {

	/**
	 * Creates a {@link AbstractBlobStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractBlobStore(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public String getName() {
		return getConfig().getName();
	}

	/**
	 * Creates a new random key: the canonical lower case string form of a random (version 4)
	 * {@link UUID}.
	 */
	protected String newKey() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Checks the range arguments of {@link #get(String, long, long)}.
	 */
	protected static void checkRange(long offset, long length) {
		if (offset < 0) {
			throw new IllegalArgumentException("Negative offset: " + offset);
		}
		if (length < 0) {
			throw new IllegalArgumentException("Negative length: " + length);
		}
	}

	/**
	 * Checks that the number of bytes stored matches the size announced by the caller of
	 * {@link #put(InputStream, long, String)}.
	 *
	 * @param size
	 *        The announced size, or <code>-1</code> if unknown.
	 * @param written
	 *        The number of bytes actually read from the content.
	 */
	protected static void checkSize(long size, long written) throws IOException {
		if (size >= 0 && size != written) {
			throw new IOException(
				"Content size mismatch: announced " + size + " bytes, received " + written + " bytes.");
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + getName() + ")";
	}

}
