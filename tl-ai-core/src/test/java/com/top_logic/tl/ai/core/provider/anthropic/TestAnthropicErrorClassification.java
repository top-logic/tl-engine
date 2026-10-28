/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.ProviderException.Kind;

/**
 * Pins {@link AnthropicMessagesProvider#classifyError(String, int)} to its current behavior:
 * the {@code error.type} table is authoritative, then Anthropic's {@code 413} status
 * extra, then the shared status fallback.
 */
public class TestAnthropicErrorClassification extends TestCase {

	/** Every mapped error type is authoritative regardless of status. */
	public void testErrorTypeMap() {
		assertEquals(Kind.AUTHENTICATION, AnthropicMessagesProvider.classifyError("authentication_error", 200));
		assertEquals(Kind.AUTHENTICATION, AnthropicMessagesProvider.classifyError("permission_error", 200));
		assertEquals(Kind.BILLING, AnthropicMessagesProvider.classifyError("billing_error", 200));
		assertEquals(Kind.RATE_LIMITED, AnthropicMessagesProvider.classifyError("rate_limit_error", 200));
		assertEquals(Kind.OVERLOADED, AnthropicMessagesProvider.classifyError("overloaded_error", 200));
		assertEquals(Kind.OVERLOADED, AnthropicMessagesProvider.classifyError("api_error", 200));
		assertEquals(Kind.OVERLOADED, AnthropicMessagesProvider.classifyError("timeout_error", 200));
		assertEquals(Kind.CONTEXT_WINDOW_EXCEEDED,
			AnthropicMessagesProvider.classifyError("request_too_large", 200));
		assertEquals(Kind.INVALID_REQUEST, AnthropicMessagesProvider.classifyError("not_found_error", 200));
		assertEquals(Kind.INVALID_REQUEST, AnthropicMessagesProvider.classifyError("invalid_request_error", 200));
	}

	/** Unknown type falls back to status, including Anthropic's 413 extra. */
	public void testStatusFallback() {
		assertEquals(Kind.AUTHENTICATION, AnthropicMessagesProvider.classifyError("", 401));
		assertEquals(Kind.AUTHENTICATION, AnthropicMessagesProvider.classifyError("", 403));
		assertEquals(Kind.BILLING, AnthropicMessagesProvider.classifyError("", 402));
		assertEquals(Kind.CONTEXT_WINDOW_EXCEEDED, AnthropicMessagesProvider.classifyError("", 413));
		assertEquals(Kind.RATE_LIMITED, AnthropicMessagesProvider.classifyError("", 429));
		assertEquals(Kind.OVERLOADED, AnthropicMessagesProvider.classifyError("", 504));
		assertEquals(Kind.OVERLOADED, AnthropicMessagesProvider.classifyError("", 529));
		assertEquals(Kind.OVERLOADED, AnthropicMessagesProvider.classifyError("", 500));
		assertEquals(Kind.INVALID_REQUEST, AnthropicMessagesProvider.classifyError("", 400));
		assertEquals(Kind.INVALID_REQUEST, AnthropicMessagesProvider.classifyError("", 404));
	}
}
