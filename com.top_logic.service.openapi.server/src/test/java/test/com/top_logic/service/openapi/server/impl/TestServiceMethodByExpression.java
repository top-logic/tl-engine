/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.service.openapi.server.impl.ServiceMethod;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.impl.ServiceMethodByExpression;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.HeaderParameter;
import com.top_logic.service.openapi.server.script.Response;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ServiceMethodByExpression}.
 */
@SuppressWarnings("javadoc")
public class TestServiceMethodByExpression extends TestCase {

	private final ServiceMethodByExpression _method =
		new ServiceMethodByExpression("/t", Collections.emptyList(), false, null);

	public void testRawStringIsTextPlain() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(success("hello"), resp);

		assertEquals(200, resp.getStatus());
		assertEquals("text/plain", resp.getContentType());
		assertEquals("hello", resp.bodyString());
	}

	public void testRawMapIsJsonSerialized() throws Exception {
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();

		_method.writeResponse(success(java.util.Map.of("greeting", "hello")), resp);

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
		_method.writeResponse(success(data), resp);

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
		_method.writeResponse(success(payload), resp);

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

	/**
	 * A header whose name is not a TL-Script variable name is accessed through its variable name,
	 * a parameter without variable name through its parameter name.
	 */
	public void testParameterVariableName() throws Exception {
		HeaderParameter.Config eventConfig = header("X-Gitea-Event");
		eventConfig.setVariableName("event");
		HeaderParameter.Config plainConfig = header("plain");

		List<ConcreteRequestParameter<?>> parameters = List.of(
			TypedConfigUtil.createInstance(eventConfig),
			TypedConfigUtil.createInstance(plainConfig));
		List<String> variables = parameters.stream()
			.flatMap(parameter -> parameter.getScriptParameterNames().stream())
			.collect(Collectors.toList());
		assertEquals(List.of("event", "plain"), variables);

		HttpServletRequest request = requestWithHeaders(Map.of("X-Gitea-Event", "push", "plain", "value"));
		Map<String, Object> arguments = new HashMap<>();
		for (ConcreteRequestParameter<?> parameter : parameters) {
			parameter.parse(arguments, request, Collections.emptyMap());
		}

		ServiceMethodBuilderByExpression.Config impl =
			TypedConfiguration.newConfigItem(ServiceMethodBuilderByExpression.Config.class);
		impl.setOperation(ExprFormat.INSTANCE.getValue(ServiceMethodBuilderByExpression.Config.OPERATION,
			"[$event, $plain]"));
		ServiceMethod method = ThreadContextManager.inSystemInteraction(TestServiceMethodByExpression.class,
			() -> TypedConfigUtil.createInstance(impl).build("/hook", variables));

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		method.handleRequest(null, arguments, resp);

		assertEquals(200, resp.getStatus());
		assertEquals("[\"push\",\"value\"]", resp.bodyString());
	}

	private static HeaderParameter.Config header(String name) {
		HeaderParameter.Config config = TypedConfiguration.newConfigItem(HeaderParameter.Config.class);
		config.setName(name);
		return config;
	}

	/**
	 * A {@link HttpServletRequest} that only answers single-valued header requests.
	 */
	private static HttpServletRequest requestWithHeaders(Map<String, String> headers) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				if ("getHeader".equals(method.getName())) {
					return headers.get(args[0]);
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	private static Response success(Object content) {
		return new Response(HttpServletResponse.SC_OK, content, null);
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestServiceMethodByExpression.class,
			ServiceTestSetup.createStarterFactoryForModules(
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}
}
