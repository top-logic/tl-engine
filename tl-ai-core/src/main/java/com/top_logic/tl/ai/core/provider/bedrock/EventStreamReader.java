/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Decodes frames from the AWS binary {@code application/vnd.amazon.eventstream} framing used by
 * the AWS Bedrock {@code converse-stream} endpoint.
 *
 * <p>Frame layout (all fields big-endian):</p>
 * <pre>
 * offset  0: uint32 total_length   – whole frame including both CRCs
 * offset  4: uint32 headers_length
 * offset  8: uint32 prelude_crc32  – ignored in v1
 * offset 12: headers[headers_length]
 *            each header: uint8 name_len, name[name_len], uint8 value_type, value…
 * payload[payload_length]          – payload_length = total_length − 16 − headers_length
 * uint32 message_crc32             – ignored in v1
 * </pre>
 *
 * <p>Only string-typed headers (value type {@code 7}) are returned in the {@link Message} headers
 * map. Headers with other value types are skipped while keeping the parse position aligned.</p>
 *
 * <p>CRC fields are read and discarded; they are not verified.</p>
 */
public final class EventStreamReader implements Closeable {

	/** Number of bytes occupied by the framing overhead (total_len + headers_len + prelude_crc + msg_crc). */
	private static final int FRAME_OVERHEAD = 16;

	private final DataInputStream _in;

	/**
	 * Creates an {@link EventStreamReader} that reads from the given {@link InputStream}.
	 */
	public EventStreamReader(InputStream in) {
		_in = new DataInputStream(in);
	}

	/**
	 * Returns the next message from the stream, or {@code null} at a clean end of stream.
	 *
	 * <p>A clean end of stream is detected when EOF is reached exactly at a frame boundary
	 * (i.e. before any byte of a new frame has been read). A partial frame triggers an
	 * {@link IOException}.</p>
	 *
	 * @throws IOException if the stream is truncated mid-frame or another I/O error occurs
	 */
	public Message next() throws IOException {
		// Try to read the first uint32 (total_length). EOF here = clean end of stream.
		int totalLength;
		try {
			totalLength = _in.readInt();
		} catch (EOFException eof) {
			return null;
		}

		// From here on, any EOF is a framing error.
		int headersLength = _in.readInt();
		_in.readInt(); // prelude_crc32 — ignored

		if (headersLength < 0) {
			throw new IOException(
				"Corrupt event-stream frame: negative headers_length=" + headersLength);
		}

		// Parse headers.
		byte[] headersBytes = new byte[headersLength];
		_in.readFully(headersBytes);
		Map<String, String> headers = parseHeaders(headersBytes);

		// Parse payload.
		int payloadLength = totalLength - FRAME_OVERHEAD - headersLength;
		if (payloadLength < 0) {
			throw new IOException(
				"Corrupt event-stream frame: negative payload_length=" + payloadLength
				+ " (total_length=" + totalLength + ", headers_length=" + headersLength + ")");
		}
		byte[] payload = new byte[payloadLength];
		_in.readFully(payload);

		_in.readInt(); // message_crc32 — ignored

		return new Message(headers, payload);
	}

	@Override
	public void close() throws IOException {
		_in.close();
	}

	// -------------------------------------------------------------------------
	// Header parsing
	// -------------------------------------------------------------------------

	/**
	 * Parses the header block bytes into a map of string-typed header values.
	 *
	 * <p>Non-string headers are skipped (their value bytes are consumed to maintain alignment).</p>
	 */
	private static Map<String, String> parseHeaders(byte[] headersBytes) throws IOException {
		DataInputStream dis = new DataInputStream(
			new ByteArrayInputStream(headersBytes));
		Map<String, String> result = new LinkedHashMap<>();

		while (dis.available() > 0) {
			// Read header name.
			int nameLen = dis.readUnsignedByte();
			byte[] nameBytes = new byte[nameLen];
			dis.readFully(nameBytes);
			String name = new String(nameBytes, StandardCharsets.UTF_8);

			// Read value type.
			int valueType = dis.readUnsignedByte();

			switch (valueType) {
				case 0: // true  — 0 value bytes
				case 1: // false — 0 value bytes
					// Nothing to read; skip.
					break;
				case 2: // byte — 1 byte
					dis.skipBytes(1);
					break;
				case 3: // short — 2 bytes
					dis.skipBytes(2);
					break;
				case 4: // integer — 4 bytes
					dis.skipBytes(4);
					break;
				case 5: // long — 8 bytes
					dis.skipBytes(8);
					break;
				case 6: { // byte_array — uint16 length + bytes
					int len = dis.readUnsignedShort();
					dis.skipBytes(len);
					break;
				}
				case 7: { // string — uint16 length + UTF-8 bytes  (only type used by Bedrock)
					int len = dis.readUnsignedShort();
					byte[] valueBytes = new byte[len];
					dis.readFully(valueBytes);
					result.put(name, new String(valueBytes, StandardCharsets.UTF_8));
					break;
				}
				case 8: // timestamp — 8 bytes
					dis.skipBytes(8);
					break;
				case 9: // uuid — 16 bytes
					dis.skipBytes(16);
					break;
				default:
					// Unknown type — cannot determine length; stop parsing headers.
					throw new IOException("Unknown event-stream header value type: " + valueType);
			}
		}

		return result;
	}

	// =========================================================================
	// Message record
	// =========================================================================

	/**
	 * A single decoded event-stream message.
	 */
	public static final class Message {

		private final Map<String, String> _headers;

		private final byte[] _payload;

		Message(Map<String, String> headers, byte[] payload) {
			_headers = headers;
			_payload = payload;
		}

		/**
		 * The string-typed headers of this message, keyed by header name.
		 *
		 * <p>Headers with non-string value types are omitted.</p>
		 *
		 * <p>The returned map is the live backing instance; callers must not mutate it.</p>
		 */
		public Map<String, String> headers() {
			return _headers;
		}

		/**
		 * The raw payload bytes of this message.
		 *
		 * <p>The returned array is the live backing instance; callers must not mutate it.</p>
		 */
		public byte[] payload() {
			return _payload;
		}

		/**
		 * The payload decoded as a UTF-8 string.
		 */
		public String payloadString() {
			return new String(_payload, StandardCharsets.UTF_8);
		}
	}
}
