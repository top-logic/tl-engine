/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

/**
 * Texts could not be embedded: the endpoint is down, refused the request, or the model failed.
 *
 * <p>
 * A search falls back to words when the query cannot be embedded, and says so. The index worker
 * retries the batch later rather than writing chunks without vectors.
 * </p>
 */
public class EmbeddingException extends Exception {

	/**
	 * Creates an {@link EmbeddingException}.
	 */
	public EmbeddingException(String message) {
		super(message);
	}

	/**
	 * Creates an {@link EmbeddingException} with a cause.
	 */
	public EmbeddingException(String message, Throwable cause) {
		super(message, cause);
	}

}
