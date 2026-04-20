/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import static org.junit.Assert.*;

import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.service.openapi.server.impl.ServiceMethodByExpression;
import com.top_logic.service.openapi.server.script.Response;

/**
 * Tests for {@link ServiceMethodByExpression#writeResponse(Object, jakarta.servlet.http.HttpServletResponse)}.
 */
@SuppressWarnings("javadoc")
public class TestServiceMethodByExpression extends TestCase {

	private final ServiceMethodByExpression _method =
		new ServiceMethodByExpression("/t", Collections.emptyList(), false, null);

	public void testRawStringIsTextPlain() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse("hello", resp);

		assertEquals(200, resp.getStatus());
		assertEquals("text/plain", resp.getContentType());
		assertEquals("hello", resp.bodyString());
	}

	public void testRawMapIsJsonSerialized() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(java.util.Map.of("greeting", "hello"), resp);

		assertEquals(200, resp.getStatus());
		assertEquals("application/json", resp.getContentType());
		assertEquals("{\"greeting\":\"hello\"}", resp.bodyString());
	}

	public void testWrappedBinaryDataWithoutContentTypeUsesBinaryDefault() throws Exception {
		byte[] payload = new byte[] { 1, 2, 3 };
		com.top_logic.basic.io.binary.BinaryData data =
			com.top_logic.basic.io.binary.BinaryDataFactory.createBinaryData(payload, "image/jpeg");

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, data, null), resp);

		assertEquals("image/jpeg", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	public void testWrappedPlainTextUsesStringValueOf() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(new Response(201, "hi", "text/plain; charset=utf-8"), resp);

		assertEquals(201, resp.getStatus());
		assertEquals("text/plain", resp.getContentType());
		assertEquals("utf-8", resp.getCharacterEncoding());
		assertEquals("hi", resp.bodyString());
	}

	public void testNullResultInWrapperSendsError() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(new Response(404, null, "text/plain"), resp);

		assertTrue(resp.errorSent());
		assertEquals(404, resp.errorStatus());
	}

	public void testRawBinaryDataUsesOwnContentType() throws Exception {
		byte[] payload = new byte[] { (byte) 0x89, 'P', 'N', 'G' };
		com.top_logic.basic.io.binary.BinaryData data =
			com.top_logic.basic.io.binary.BinaryDataFactory.createBinaryData(payload, "image/png");

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(data, resp);

		assertEquals(200, resp.getStatus());
		assertEquals("image/png", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	public void testWrappedBinaryDataStreamsRawBytes() throws Exception {
		byte[] payload = new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
		com.top_logic.basic.io.binary.BinaryData data =
			com.top_logic.basic.io.binary.BinaryDataFactory.createBinaryData(payload, "image/png");

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, data, "image/png"), resp);

		assertEquals(200, resp.getStatus());
		assertEquals("image/png", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	public void testWrappedByteArrayStreamsRawBytes() throws Exception {
		byte[] payload = "binary-bytes".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, payload, "application/octet-stream"), resp);

		assertEquals("application/octet-stream", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	public void testWrappedInputStreamStreamsRawBytes() throws Exception {
		byte[] payload = "stream-bytes".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
		java.io.InputStream in = new java.io.ByteArrayInputStream(payload);

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, in, "application/pdf"), resp);

		assertEquals("application/pdf", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	public void testRawByteArrayDefaultsToOctetStream() throws Exception {
		byte[] payload = new byte[] { 1, 2, 3, 4, 5 };

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(payload, resp);

		assertEquals(200, resp.getStatus());
		assertEquals("application/octet-stream", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	public void testWrappedBinaryHonorsExplicitContentType() throws Exception {
		byte[] payload = "Hello,World\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, payload, "text/csv; charset=utf-8"), resp);

		assertEquals("text/csv; charset=utf-8", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}
}
