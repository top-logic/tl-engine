/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.top_logic.basic.Logger;

/**
 * Debug helper that dumps a provider's raw HTTP response stream with byte-arrival timing.
 *
 * <p>Wrapping the response body just below the parser (see {@link AbstractStreamingProvider#stream})
 * reveals, independently of the provider's wire format, exactly when bytes arrive from the server:
 * the time to the first body byte and how long each read blocks. This distinguishes a slow gateway
 * (large time-to-first-byte) from a slow token dribble (large per-read block times) from slow local
 * parsing (fast reads, slow overall).</p>
 *
 * <p>Disabled by default. Enable via the {@code tl.ai.provider.dumpStream} system property:</p>
 * <ul>
 *   <li>{@code -Dtl.ai.provider.dumpStream=true} — dump every provider stream.</li>
 *   <li>{@code -Dtl.ai.provider.dumpStream=x.ai,nexus,bedrock} — comma-separated substrings; a
 *       stream is dumped when any substring occurs in its label (provider class, model, host).</li>
 * </ul>
 *
 * <p>Output goes to {@link Logger} at INFO level, one line per read, prefixed with
 * {@code [stream-dump <label> +<elapsed>ms ...]}.</p>
 */
public final class StreamDump {

	/** Raw value of the {@code tl.ai.provider.dumpStream} system property; empty when unset. */
	private static final String SPEC = System.getProperty("tl.ai.provider.dumpStream", "");

	/** Single-byte reads are only logged when they blocked at least this long (frame boundaries). */
	private static final long GAP_LOG_MS = 25;

	/** Logged response text is truncated to this many characters per read. */
	private static final int MAX_TEXT = 4000;

	/**
	 * Whether stream dumping is enabled for the given label.
	 *
	 * @param label
	 *        A human-readable stream identifier (e.g. {@code "ChatCompletionsProvider grok-4 @api.x.ai"}).
	 * @return {@code true} if this stream should be dumped.
	 */
	public static boolean enabled(String label) {
		if (SPEC.isEmpty()) {
			return false;
		}
		if (SPEC.equalsIgnoreCase("true")) {
			return true;
		}
		String lower = label.toLowerCase();
		for (String part : SPEC.split(",")) {
			String token = part.trim().toLowerCase();
			if (!token.isEmpty() && lower.contains(token)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Wraps the given stream so that every read is logged with arrival timing.
	 *
	 * @param in
	 *        The raw response body stream.
	 * @param label
	 *        A human-readable stream identifier used as the log prefix.
	 * @param startNanos
	 *        The {@link System#nanoTime()} captured just before the HTTP request was sent; all
	 *        elapsed times are relative to it.
	 * @return a wrapping stream that logs as a side effect of reading.
	 */
	public static InputStream wrap(InputStream in, String label, long startNanos) {
		return new DumpStream(in, label, startNanos);
	}

	private StreamDump() {
		// Utility class — no instances.
	}

	private static long elapsedMs(long startNanos) {
		return (System.nanoTime() - startNanos) / 1_000_000L;
	}

	private static String sanitize(String text) {
		String escaped = text.replace("\r", "\\r").replace("\n", "\\n");
		if (escaped.length() <= MAX_TEXT) {
			return escaped;
		}
		return escaped.substring(0, MAX_TEXT) + "…(+" + (escaped.length() - MAX_TEXT) + " chars)";
	}

	/**
	 * {@link FilterInputStream} that logs each read with elapsed and block timing.
	 */
	private static final class DumpStream extends FilterInputStream {

		private final String _label;

		private final long _startNanos;

		private boolean _firstByteSeen = false;

		DumpStream(InputStream in, String label, long startNanos) {
			super(in);
			_label = label;
			_startNanos = startNanos;
		}

		private void noteFirstByte() {
			if (!_firstByteSeen) {
				_firstByteSeen = true;
				Logger.info("[stream-dump " + _label + "] first body byte +"
					+ elapsedMs(_startNanos) + "ms", StreamDump.class);
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			long before = System.nanoTime();
			int n = in.read(b, off, len);
			if (n > 0) {
				noteFirstByte();
				long blockedMs = (System.nanoTime() - before) / 1_000_000L;
				Logger.info("[stream-dump " + _label + " +" + elapsedMs(_startNanos)
					+ "ms blocked " + blockedMs + "ms] " + n + "B: "
					+ sanitize(new String(b, off, n, StandardCharsets.UTF_8)), StreamDump.class);
			}
			return n;
		}

		@Override
		public int read() throws IOException {
			long before = System.nanoTime();
			int value = in.read();
			if (value >= 0) {
				noteFirstByte();
				long blockedMs = (System.nanoTime() - before) / 1_000_000L;
				if (blockedMs >= GAP_LOG_MS) {
					Logger.info("[stream-dump " + _label + " +" + elapsedMs(_startNanos)
						+ "ms] blocked " + blockedMs + "ms waiting for next byte", StreamDump.class);
				}
			}
			return value;
		}
	}
}
