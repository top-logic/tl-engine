/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for {@link EventStreamReader}.
 */
public class TestEventStreamReader extends TestCase {

	/**
	 * Encodes a single event-stream frame with string-typed headers and a UTF-8 payload.
	 *
	 * <p>The two CRC fields are written as {@code 0} because the reader ignores them.</p>
	 */
	static byte[] frame(Map<String, String> stringHeaders, String payloadJson) throws IOException {
		// Encode headers into a byte array first so we know headers_length.
		ByteArrayOutputStream headersBuf = new ByteArrayOutputStream();
		DataOutputStream hdos = new DataOutputStream(headersBuf);
		for (Map.Entry<String, String> entry : stringHeaders.entrySet()) {
			byte[] nameBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
			byte[] valueBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
			hdos.writeByte(nameBytes.length);          // name_len (uint8)
			hdos.write(nameBytes);                     // name
			hdos.writeByte(7);                         // value_type = string
			hdos.writeShort(valueBytes.length);        // uint16 value length
			hdos.write(valueBytes);                    // value bytes
		}
		hdos.flush();
		byte[] headersBytes = headersBuf.toByteArray();

		byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);

		// total_length = 4 (total_len) + 4 (headers_len) + 4 (prelude_crc) + headers + payload + 4 (msg_crc)
		int totalLength = 16 + headersBytes.length + payloadBytes.length;

		ByteArrayOutputStream frameBuf = new ByteArrayOutputStream(totalLength);
		DataOutputStream fdos = new DataOutputStream(frameBuf);
		fdos.writeInt(totalLength);             // offset 0: total_length
		fdos.writeInt(headersBytes.length);     // offset 4: headers_length
		fdos.writeInt(0);                       // offset 8: prelude_crc32 (ignored)
		fdos.write(headersBytes);               // offset 12: headers
		fdos.write(payloadBytes);               // payload
		fdos.writeInt(0);                       // message_crc32 (ignored)
		fdos.flush();
		return frameBuf.toByteArray();
	}

	/** Converts a hex string (spaces allowed) to a byte array. */
	static byte[] hexToBytes(String hex) {
		String clean = hex.replaceAll("\\s+", "");
		int len = clean.length();
		byte[] result = new byte[len / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
		}
		return result;
	}

	/** */
	public void testRoundTripTwoFrames() throws Exception {
		Map<String, String> headers1 = new LinkedHashMap<>();
		headers1.put(":event-type", "messageStart");
		headers1.put(":message-type", "event");
		String payload1 = "{\"role\":\"assistant\"}";

		Map<String, String> headers2 = new LinkedHashMap<>();
		headers2.put(":event-type", "contentBlockDelta");
		headers2.put(":message-type", "event");
		String payload2 = "{\"delta\":{\"type\":\"text_delta\",\"text\":\"Hello\"}}";

		ByteArrayOutputStream concat = new ByteArrayOutputStream();
		concat.write(frame(headers1, payload1));
		concat.write(frame(headers2, payload2));

		try (EventStreamReader reader = new EventStreamReader(new ByteArrayInputStream(concat.toByteArray()))) {
			EventStreamReader.Message msg1 = reader.next();
			assertNotNull(msg1);
			assertEquals("messageStart", msg1.headers().get(":event-type"));
			assertEquals("event", msg1.headers().get(":message-type"));
			assertEquals(payload1, msg1.payloadString());

			EventStreamReader.Message msg2 = reader.next();
			assertNotNull(msg2);
			assertEquals("contentBlockDelta", msg2.headers().get(":event-type"));
			assertEquals(payload2, msg2.payloadString());

			assertNull(reader.next());
		}
	}

	/** */
	public void testDecodesRealCapturedFrame() throws Exception {
		// Live-captured first frame from AWS Bedrock converse-stream, 171 (0xAB) bytes.
		String hex =
			"000000ab 00000052 e7b04136 0b3a6576" +
			"656e742d 74797065 07000c6d 65737361" +
			"67655374 6172740d 3a636f6e 74656e74" +
			"2d747970 65070010 6170706c 69636174" +
			"696f6e2f 6a736f6e 0d3a6d65 73736167" +
			"652d7479 70650700 05657665 6e747b22" +
			"70223a22 61626364 65666768 696a6b6c" +
			"6d6e6f70 71727374 75767778 797a4142" +
			"43444546 4748494a 4b4c4d4e 4f505152" +
			"5354222c 22726f6c 65223a22 61737369" +
			"7374616e 74227d7a a9c97c";

		byte[] frameBytes = hexToBytes(hex);
		assertEquals(0xab, frameBytes.length);

		try (EventStreamReader reader = new EventStreamReader(new ByteArrayInputStream(frameBytes))) {
			EventStreamReader.Message msg = reader.next();
			assertNotNull(msg);

			Map<String, String> headers = msg.headers();
			assertEquals("messageStart", headers.get(":event-type"));
			assertEquals("application/json", headers.get(":content-type"));
			assertEquals("event", headers.get(":message-type"));

			String expectedPayload = "{\"p\":\"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRST\",\"role\":\"assistant\"}";
			assertEquals(expectedPayload, msg.payloadString());

			// Clean end of stream.
			assertNull(reader.next());
		}
	}

	/** */
	public void testEmptyStreamReturnsNull() throws Exception {
		try (EventStreamReader reader = new EventStreamReader(new ByteArrayInputStream(new byte[0]))) {
			assertNull(reader.next());
		}
	}

	/** */
	public void testFrameWithNoHeaders() throws Exception {
		byte[] frameBytes = frame(new LinkedHashMap<>(), "{}");
		try (EventStreamReader reader = new EventStreamReader(new ByteArrayInputStream(frameBytes))) {
			EventStreamReader.Message msg = reader.next();
			assertNotNull(msg);
			assertTrue(msg.headers().isEmpty());
			assertEquals("{}", msg.payloadString());
		}
	}

	/**
	 * Verifies that a non-string header (type 5, long, 8 value bytes) placed before a string
	 * header is correctly skipped, leaving the parse position aligned so the following string
	 * header is still decoded accurately.
	 */
	public void testSkipNonStringHeaderKeepsAlignment() throws Exception {
		// Build a header block that contains:
		//   1. a type-5 (long) header named "x-req-id" with value 0x0102030405060708
		//   2. a type-7 (string) header ":event-type" = "messageStart"
		ByteArrayOutputStream headersBuf = new ByteArrayOutputStream();
		DataOutputStream hdos = new DataOutputStream(headersBuf);

		// Header 1: non-string long header.
		byte[] longName = "x-req-id".getBytes(StandardCharsets.UTF_8);
		hdos.writeByte(longName.length);   // name_len
		hdos.write(longName);              // name
		hdos.writeByte(5);                 // value_type = long
		hdos.writeLong(0x0102030405060708L); // 8-byte long value

		// Header 2: string header that must be read correctly after the skip.
		byte[] strName = ":event-type".getBytes(StandardCharsets.UTF_8);
		byte[] strValue = "messageStart".getBytes(StandardCharsets.UTF_8);
		hdos.writeByte(strName.length);    // name_len
		hdos.write(strName);              // name
		hdos.writeByte(7);                 // value_type = string
		hdos.writeShort(strValue.length); // uint16 value length
		hdos.write(strValue);             // value bytes
		hdos.flush();

		byte[] headersBytes = headersBuf.toByteArray();
		byte[] payloadBytes = "{}".getBytes(StandardCharsets.UTF_8);
		int totalLength = 16 + headersBytes.length + payloadBytes.length;

		ByteArrayOutputStream frameBuf = new ByteArrayOutputStream();
		DataOutputStream fdos = new DataOutputStream(frameBuf);
		fdos.writeInt(totalLength);
		fdos.writeInt(headersBytes.length);
		fdos.writeInt(0); // prelude_crc ignored
		fdos.write(headersBytes);
		fdos.write(payloadBytes);
		fdos.writeInt(0); // message_crc ignored
		fdos.flush();

		try (EventStreamReader reader = new EventStreamReader(
				new ByteArrayInputStream(frameBuf.toByteArray()))) {
			EventStreamReader.Message msg = reader.next();
			assertNotNull(msg);
			// The long header must not appear (non-string types are omitted).
			assertFalse(msg.headers().containsKey("x-req-id"));
			// The string header after the skipped one must be decoded correctly.
			assertEquals("messageStart", msg.headers().get(":event-type"));
			assertNull(reader.next());
		}
	}

	/** */
	public void testPartialFrameThrowsIOException() throws Exception {
		// Write only the prelude (first 12 bytes) without the rest.
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(buf);
		dos.writeInt(100); // total_length claims 100 bytes
		dos.writeInt(20);  // headers_length
		dos.writeInt(0);   // prelude_crc
		// No headers or payload — stream ends abruptly.
		dos.flush();

		try (EventStreamReader reader = new EventStreamReader(new ByteArrayInputStream(buf.toByteArray()))) {
			try {
				reader.next();
				fail("Expected IOException for truncated frame");
			} catch (IOException expected) {
				// EOFException or similar — correct.
			}
		}
	}
}
