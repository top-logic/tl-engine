/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.Map;

import com.top_logic.tl.ai.core.provider.ProviderException.Kind;

/**
 * Shared classification of provider API errors into a {@link ProviderException.Kind}.
 *
 * <p>
 * Every provider first consults its own error-code/-type table and, when that yields nothing,
 * falls back to the HTTP status code. That status fallback is identical across providers and lives
 * here in {@link #classifyByStatus(int)}. {@link #classify(String, int, Map)} combines the two for
 * providers whose error body carries a machine-readable code.
 * </p>
 */
public final class ErrorClassifier {

	private ErrorClassifier() {
		// Static utility.
	}

	/**
	 * Classifies via the provider's code table first, then the shared HTTP-status fallback.
	 *
	 * @param errorCode
	 *        Provider-specific error code/type from the response body, or {@code null}.
	 * @param statusCode
	 *        HTTP status code.
	 * @param codeMap
	 *        Provider-specific mapping from error code/type to {@link Kind}, or {@code null}/empty
	 *        when the provider carries no such code (then only the status fallback applies).
	 */
	public static Kind classify(String errorCode, int statusCode, Map<String, Kind> codeMap) {
		if (codeMap != null && errorCode != null) {
			Kind byCode = codeMap.get(errorCode);
			if (byCode != null) {
				return byCode;
			}
		}
		return classifyByStatus(statusCode);
	}

	/**
	 * The HTTP-status fallback shared by all providers: {@code 401/403} → authentication,
	 * {@code 429} → rate limited, any {@code 5xx} → overloaded, everything else → invalid request.
	 */
	public static Kind classifyByStatus(int statusCode) {
		return switch (statusCode) {
			case 401, 403 -> Kind.AUTHENTICATION;
			case 429 -> Kind.RATE_LIMITED;
			default -> {
				if (statusCode >= 500) {
					yield Kind.OVERLOADED;
				}
				yield Kind.INVALID_REQUEST;
			}
		};
	}
}
