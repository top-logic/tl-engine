/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.ProviderException.Kind;

/**
 * Tests for {@link ErrorClassifier}, pinning the shared HTTP-status fallback and the code-map path
 * as used by the OpenAI and chat-completions providers.
 */
public class TestErrorClassifier extends TestCase {

	/** The status fallback shared by all providers. */
	public void testStatusFallback() {
		assertEquals(Kind.AUTHENTICATION, ErrorClassifier.classifyByStatus(401));
		assertEquals(Kind.AUTHENTICATION, ErrorClassifier.classifyByStatus(403));
		assertEquals(Kind.RATE_LIMITED, ErrorClassifier.classifyByStatus(429));
		assertEquals(Kind.OVERLOADED, ErrorClassifier.classifyByStatus(500));
		assertEquals(Kind.OVERLOADED, ErrorClassifier.classifyByStatus(503));
		assertEquals(Kind.OVERLOADED, ErrorClassifier.classifyByStatus(529));
		// Not part of the shared fallback: these are provider-specific in Anthropic only.
		assertEquals(Kind.INVALID_REQUEST, ErrorClassifier.classifyByStatus(402));
		assertEquals(Kind.INVALID_REQUEST, ErrorClassifier.classifyByStatus(413));
		assertEquals(Kind.INVALID_REQUEST, ErrorClassifier.classifyByStatus(400));
		assertEquals(Kind.INVALID_REQUEST, ErrorClassifier.classifyByStatus(404));
	}

	/** A null or empty code map degrades to the status fallback (chat-completions provider). */
	public void testNoCodeMap() {
		assertEquals(Kind.AUTHENTICATION, ErrorClassifier.classify(null, 401, null));
		assertEquals(Kind.RATE_LIMITED, ErrorClassifier.classify("whatever", 429, Map.of()));
		assertEquals(Kind.OVERLOADED, ErrorClassifier.classify(null, 500, null));
		assertEquals(Kind.INVALID_REQUEST, ErrorClassifier.classify(null, 402, null));
	}

	/** The OpenAI code map wins over the status; unknown codes fall back to status. */
	public void testOpenAiCodeMap() {
		Map<String, Kind> codeMap = Map.of(
			"context_length_exceeded", Kind.CONTEXT_WINDOW_EXCEEDED,
			"rate_limit_exceeded", Kind.RATE_LIMITED,
			"authentication_error", Kind.AUTHENTICATION);

		// Code is authoritative, regardless of status.
		assertEquals(Kind.CONTEXT_WINDOW_EXCEEDED,
			ErrorClassifier.classify("context_length_exceeded", 200, codeMap));
		assertEquals(Kind.RATE_LIMITED, ErrorClassifier.classify("rate_limit_exceeded", 400, codeMap));
		assertEquals(Kind.AUTHENTICATION, ErrorClassifier.classify("authentication_error", 500, codeMap));

		// Unknown code → status fallback.
		assertEquals(Kind.AUTHENTICATION, ErrorClassifier.classify("mystery", 401, codeMap));
		assertEquals(Kind.OVERLOADED, ErrorClassifier.classify("mystery", 503, codeMap));
		assertEquals(Kind.INVALID_REQUEST, ErrorClassifier.classify("mystery", 400, codeMap));
	}
}
