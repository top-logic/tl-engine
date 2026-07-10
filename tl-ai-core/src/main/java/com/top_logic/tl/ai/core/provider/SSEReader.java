/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Reads Server-Sent Events from an {@link InputStream}.
 *
 * <p>Each call to {@link #next()} returns the next SSE event (event type + data),
 * or {@code null} when the stream ends.</p>
 */
public class SSEReader implements Closeable {

	/**
	 * A single SSE event.
	 *
	 * @param event the event type (from the {@code event:} field)
	 * @param data  the event data (from the {@code data:} field)
	 */
	public record Event(String event, String data) { /**/ }

	private final BufferedReader _reader;

	/** Creates an {@link SSEReader}. */
	public SSEReader(InputStream input) {
		_reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
	}

	/**
	 * Read the next SSE event from the stream.
	 *
	 * @return the next event, or {@code null} if the stream has ended
	 */
	public Event next() throws IOException {
		String eventType = null;
		StringBuilder data = null;
		String line;

		while ((line = _reader.readLine()) != null) {
			if (line.isEmpty()) {
				if (eventType != null || data != null) {
					return new Event(
						eventType != null ? eventType : "",
						data != null ? data.toString() : "");
				}
				continue;
			}

			if (line.startsWith("event:")) {
				eventType = line.substring(6).trim();
			} else if (line.startsWith("data:")) {
				String value = line.substring(5).trim();
				if (data == null) {
					data = new StringBuilder(value);
				} else {
					data.append('\n').append(value);
				}
			}
		}

		if (eventType != null || data != null) {
			return new Event(
				eventType != null ? eventType : "",
				data != null ? data.toString() : "");
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		_reader.close();
	}
}
