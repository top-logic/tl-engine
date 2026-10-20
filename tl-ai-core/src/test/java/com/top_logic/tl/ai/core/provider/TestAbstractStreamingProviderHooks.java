/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for extension hooks in {@link AbstractStreamingProvider}.
 */
public class TestAbstractStreamingProviderHooks extends TestCase {

	/**
	 * Minimal concrete subclass that exposes protected methods for testing.
	 */
	static class StubProvider extends AbstractStreamingProvider {

		StubProvider() {
			super(null, "https://example.com", defaultHttpClient());
		}

		@Override
		protected String endpointPath(ProviderRequest request) {
			return "/x";
		}

		@Override
		protected String buildRequestJson(ProviderRequest request) throws ProviderException {
			return "{}";
		}

		@Override
		protected void processStream(InputStream input, StreamHandler handler) {
			// no-op
		}

		@Override
		protected void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
			// no-op
		}

		/** Expose {@link #parseRetryAfter} for testing. */
		public Duration parseRetryAfterPublic(HttpHeaders headers) {
			return parseRetryAfter(headers);
		}

		/** Expose {@link #acceptHeader} for testing. */
		public String acceptHeaderPublic() {
			return acceptHeader();
		}
	}

	private final StubProvider _provider = new StubProvider();

	/** */
	public void testParseRetryAfterRatelimitResetFallback() {
		HttpHeaders headers = HttpHeaders.of(Map.of("ratelimit-reset", List.of("63599")), (a, b) -> true);
		Duration result = _provider.parseRetryAfterPublic(headers);
		assertEquals(Duration.ofSeconds(63599), result);
	}

	/** */
	public void testParseRetryAfterPrimaryWins() {
		HttpHeaders headers = HttpHeaders.of(
			Map.of("retry-after", List.of("5"), "ratelimit-reset", List.of("63599")),
			(a, b) -> true);
		Duration result = _provider.parseRetryAfterPublic(headers);
		assertEquals(Duration.ofSeconds(5), result);
	}

	/** */
	public void testDefaultAcceptHeader() {
		assertEquals("text/event-stream", _provider.acceptHeaderPublic());
	}

	/** */
	public void testParseRetryAfterBothAbsentReturnsNull() {
		HttpHeaders headers = HttpHeaders.of(Map.of(), (a, b) -> true);
		assertNull(_provider.parseRetryAfterPublic(headers));
	}

	/** */
	public void testParseRetryAfterHttpDateFallsBackToRatelimitReset() {
		HttpHeaders headers = HttpHeaders.of(
			Map.of("retry-after", List.of("Thu, 01 Jan 2026 00:00:00 GMT"),
				"ratelimit-reset", List.of("63599")),
			(a, b) -> true);
		Duration result = _provider.parseRetryAfterPublic(headers);
		assertEquals(Duration.ofSeconds(63599), result);
	}

	/** */
	public void testParseRetryAfterNonNumericRatelimitResetReturnsNull() {
		HttpHeaders headers = HttpHeaders.of(Map.of("ratelimit-reset", List.of("soon")), (a, b) -> true);
		assertNull(_provider.parseRetryAfterPublic(headers));
	}

}
