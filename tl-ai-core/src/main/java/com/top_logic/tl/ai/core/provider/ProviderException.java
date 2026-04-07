/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.time.Duration;

/**
 * Exception thrown by {@link LLMProvider} on API or network errors.
 *
 * <p>The {@link #kind()} classifies the error for the agent loop to decide
 * whether to retry, compact, or report.</p>
 */
public class ProviderException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Classification of the error.
	 */
	public enum Kind {

		/** 429 — rate limited. Check {@link ProviderException#retryAfter()}. */
		RATE_LIMITED,

		/** 529 or 5xx — provider overloaded or temporarily unavailable. */
		OVERLOADED,

		/** Request too large for the model's context window. */
		CONTEXT_WINDOW_EXCEEDED,

		/** 401/403 — invalid or expired API key. */
		AUTHENTICATION,

		/** 400 — malformed request. */
		INVALID_REQUEST,

		/** Unclassified error. */
		UNKNOWN
	}

	private final Kind _kind;

	private final int _statusCode;

	private final Duration _retryAfter;

	/** */
	public ProviderException(Kind kind, int statusCode, String message) {
		this(kind, statusCode, message, null, null);
	}

	/** */
	public ProviderException(Kind kind, int statusCode, String message, Throwable cause) {
		this(kind, statusCode, message, null, cause);
	}

	/** */
	public ProviderException(Kind kind, int statusCode, String message, Duration retryAfter, Throwable cause) {
		super(message, cause);
		_kind = kind;
		_statusCode = statusCode;
		_retryAfter = retryAfter;
	}

	/** */
	public Kind kind() {
		return _kind;
	}

	/** */
	public int statusCode() {
		return _statusCode;
	}

	/**
	 * Suggested wait time before retrying, or {@code null} if unknown.
	 */
	public Duration retryAfter() {
		return _retryAfter;
	}
}
