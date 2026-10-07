/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry.impl.response;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.response.I18NConstants;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseChecker;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for {@link ResponseChecker}.
 */
public class TestResponseChecker extends BasicTestCase {

	private static final String METHOD_NAME = "testMethod";

	private static final Object DELEGATE_RESULT = new Object();

	private MethodDefinition _method;

	private Call _call;

	private int _delegateCalls;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_method = TypedConfiguration.newConfigItem(MethodDefinition.class);
		_method.setName(METHOD_NAME);
		_call = Call.newInstance(new Object[] { "self", "arg" });
		_delegateCalls = 0;
	}

	/**
	 * A "204 No Content" response without a body is a success.
	 */
	public void testNoContentWithoutEntity() throws Exception {
		assertSame(DELEGATE_RESULT, check(new BasicClassicHttpResponse(204)));
		assertEquals(1, _delegateCalls);
	}

	/**
	 * A "201 Created" response with a body is a success.
	 */
	public void testCreatedWithEntity() throws Exception {
		BasicClassicHttpResponse response = new BasicClassicHttpResponse(201);
		response.setEntity(new StringEntity("{}", ContentType.APPLICATION_JSON));
		assertSame(DELEGATE_RESULT, check(response));
		assertEquals(1, _delegateCalls);
	}

	/**
	 * A "200 OK" response is a success.
	 */
	public void testOk() throws Exception {
		BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
		response.setEntity(new StringEntity("ok", ContentType.TEXT_PLAIN));
		assertSame(DELEGATE_RESULT, check(response));
		assertEquals(1, _delegateCalls);
	}

	/**
	 * A redirect without a body that reaches the check is an error without details.
	 */
	public void testRedirectWithoutEntity() throws Exception {
		assertErrorContent(new BasicClassicHttpResponse(302), I18NConstants.ERROR_CALL_FAILED_NO_DETAILS);
	}

	/**
	 * A server error with a textual body reports the body.
	 */
	public void testServerErrorWithTextBody() throws Exception {
		BasicClassicHttpResponse response = new BasicClassicHttpResponse(500);
		response.setEntity(new StringEntity("boom", ContentType.TEXT_PLAIN));
		assertErrorContent(response, "boom");
	}

	/**
	 * A client error with a non-textual body is an error without details.
	 */
	public void testClientErrorWithBinaryBody() throws Exception {
		BasicClassicHttpResponse response = new BasicClassicHttpResponse(404);
		response.setEntity(new ByteArrayEntity(new byte[] { 1, 2, 3 }, ContentType.APPLICATION_OCTET_STREAM));
		assertErrorContent(response, I18NConstants.ERROR_CALL_FAILED_NO_DETAILS);
	}

	private void assertErrorContent(ClassicHttpResponse response, Object expectedContent) throws Exception {
		try {
			check(response);
			fail("Expected failure for status code " + response.getCode());
		} catch (TopLogicException ex) {
			ResKey errorKey = ex.getErrorKey();
			assertEquals(I18NConstants.ERROR_CALL_FAILED__FUN_ARGS_REASON_CODE_CONTENT.getKey(), errorKey.plain().getKey());
			List<Object> arguments = Arrays.asList(errorKey.arguments());
			assertEquals(METHOD_NAME, arguments.get(0));
			assertEquals(response.getCode(), arguments.get(3));
			assertEquals(expectedContent, arguments.get(4));
		}
		assertEquals(0, _delegateCalls);
	}

	private Object check(ClassicHttpResponse response) throws Exception {
		ResponseHandler delegate = (method, call, r) -> {
			_delegateCalls++;
			return DELEGATE_RESULT;
		};
		return new ResponseChecker(delegate).handle(_method, _call, response);
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestResponseChecker.class));
	}
}
