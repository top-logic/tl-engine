/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import com.top_logic.service.openapi.server.impl.ServiceMethodByExpression;
import com.top_logic.service.openapi.server.script.Response;

/**
 * Tests for {@link ServiceMethodByExpression#writeResponse(Object, jakarta.servlet.http.HttpServletResponse)}.
 */
public class TestServiceMethodByExpression {

	private final ServiceMethodByExpression _method =
		new ServiceMethodByExpression("/t", Collections.emptyList(), false, null);

	@Test
	public void rawResultIsJsonSerialized() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse("hello", resp);

		assertEquals(200, resp.getStatus());
		assertEquals("application/json", resp.getContentType());
		assertEquals("\"hello\"", resp.bodyString());
	}

	@Test
	public void wrappedPlainTextUsesStringValueOf() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(new Response(201, "hi", "text/plain; charset=utf-8"), resp);

		assertEquals(201, resp.getStatus());
		assertEquals("text/plain", resp.getContentType());
		assertEquals("utf-8", resp.getCharacterEncoding());
		assertEquals("hi", resp.bodyString());
	}

	@Test
	public void nullResultInWrapperSendsError() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(new Response(404, null, "text/plain"), resp);

		assertTrue(resp.errorSent());
		assertEquals(404, resp.errorStatus());
	}

	@Test
	public void rawBinaryDataUsesOwnContentType() throws Exception {
		byte[] payload = new byte[] { (byte) 0x89, 'P', 'N', 'G' };
		com.top_logic.basic.io.binary.BinaryData data =
			com.top_logic.basic.io.binary.BinaryDataFactory.createBinaryData(payload, "image/png");

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(data, resp);

		assertEquals(200, resp.getStatus());
		assertEquals("image/png", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	@Test
	public void wrappedBinaryDataStreamsRawBytes() throws Exception {
		byte[] payload = new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A };
		com.top_logic.basic.io.binary.BinaryData data =
			com.top_logic.basic.io.binary.BinaryDataFactory.createBinaryData(payload, "image/png");

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, data, "image/png"), resp);

		assertEquals(200, resp.getStatus());
		assertEquals("image/png", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	@Test
	public void wrappedByteArrayStreamsRawBytes() throws Exception {
		byte[] payload = "binary-bytes".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, payload, "application/octet-stream"), resp);

		assertEquals("application/octet-stream", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	@Test
	public void wrappedInputStreamStreamsRawBytes() throws Exception {
		byte[] payload = "stream-bytes".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
		java.io.InputStream in = new java.io.ByteArrayInputStream(payload);

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(new Response(200, in, "application/pdf"), resp);

		assertEquals("application/pdf", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}

	@Test
	public void rawByteArrayDefaultsToOctetStream() throws Exception {
		byte[] payload = new byte[] { 1, 2, 3, 4, 5 };

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		_method.writeResponse(payload, resp);

		assertEquals(200, resp.getStatus());
		assertEquals("application/octet-stream", resp.getContentType());
		assertArrayEquals(payload, resp.bodyBytes());
	}
}
