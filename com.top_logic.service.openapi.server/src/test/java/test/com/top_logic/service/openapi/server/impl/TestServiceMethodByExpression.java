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
}
