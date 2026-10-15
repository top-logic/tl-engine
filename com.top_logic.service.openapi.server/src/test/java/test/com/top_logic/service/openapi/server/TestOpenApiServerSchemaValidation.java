/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.service.openapi.server.impl.CapturingHttpServletResponse;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.service.openapi.server.I18NConstants;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.parameter.ParameterFormat;
import com.top_logic.service.openapi.server.parameter.ParameterReference;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
import com.top_logic.service.openapi.server.parameter.RequestBodyParameter;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test for the validation of request values against parameter schemas by the
 * {@link OpenApiServer}.
 */
public class TestOpenApiServerSchemaValidation extends BasicTestCase {

	private static final String PATH = "/release";

	private static final String SCHEMA = """
		{
			"type": "object",
			"required": ["name"],
			"properties": {
				"name": {"type": "string"},
				"release": {"$ref": "#/components/schemas/Release"}
			}
		}
		""";

	private static final String RELEASE = """
		{"type": "object", "properties": {"version": {"type": "string"}}}
		""";

	/**
	 * A valid body reaches the implementation unchanged.
	 */
	public void testValidRequest() throws Exception {
		TestedServer server = start(serverConfig(SCHEMA));

		String body = "{\"name\": \"tl\", \"release\": {\"version\": \"8.0\"}}";
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		server.handleRequest(request(body), resp);

		assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
		assertEquals(JSON.fromString(body), JSON.fromString(resp.bodyString()));
	}

	/**
	 * A body violating the schema is answered with status 400, the implementation is not called.
	 */
	public void testInvalidRequest() throws Exception {
		TestedServer server = start(serverConfig(SCHEMA));

		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		server.handleRequest(request("{\"release\": {\"version\": 8}}"), resp);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
		String message = resp.bodyString();
		assertTrue(message,
			message.startsWith(
				"Invalid API request to '" + PATH + "': Value of parameter 'body' does not match its schema: "));
		assertContains("$: required property 'name' not found", message);
		assertContains("$.release.version: integer found, string expected", message);
	}

	/**
	 * A global parameter is validated against its schema.
	 */
	public void testGlobalParameter() throws Exception {
		OpenApiServer.Config<?> config = serverConfig(null);
		OperationByMethod operation = config.getPaths().get(0).getOperations().get(HttpMethod.POST);
		operation.getParameters().clear();

		ReferencedParameter global = TypedConfiguration.newConfigItem(ReferencedParameter.class);
		global.setReferenceName("releaseBody");
		global.setParameterDefinition(body(SCHEMA));
		config.getGlobalParameters().put(global.getReferenceName(), global);

		ParameterReference.Config reference = TypedConfiguration.newConfigItem(ParameterReference.Config.class);
		reference.setName("releaseBody");
		operation.getParameters().add(reference);

		TestedServer server = start(config);
		CapturingHttpServletResponse resp = new CapturingHttpServletResponse();
		server.handleRequest(request("{\"name\": 1}"), resp);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
		assertContains("$.name: integer found, string expected", resp.bodyString());
	}

	/**
	 * A schema that cannot be compiled prevents the server from starting.
	 */
	public void testUncompilableSchema() throws Exception {
		try {
			start(serverConfig("{\"$ref\": \"#/components/schemas/Undefined\"}"));
			fail("Server must not start with an invalid schema.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_PARAMETER_SCHEMA__PATH__METHOD__PARAMETER.getKey(),
				ex.getErrorKey().plain().getKey());
			assertEquals(
				com.top_logic.service.openapi.server.schema.I18NConstants.ERROR_SCHEMA_NOT_COMPILABLE__PROBLEM.getKey(),
				((TopLogicException) ex.getCause()).getErrorKey().plain().getKey());
		}
	}

	private static TestedServer start(OpenApiServer.Config<?> config) {
		TestedServer server = new TestedServer(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		server.start();
		return server;
	}

	private static OpenApiServer.Config<?> serverConfig(String schema) throws Exception {
		OpenApiServer.Config<?> config = TypedConfiguration.newConfigItem(OpenApiServer.Config.class);
		config.getInformation().setTitle("test");
		config.getInformation().setVersion("1.0");

		SchemaObject release = TypedConfiguration.newConfigItem(SchemaObject.class);
		release.setName("Release");
		release.setSchema(RELEASE);
		config.getGlobalSchemas().put(release.getName(), release);

		PathItem pathItem = TypedConfiguration.newConfigItem(PathItem.class);
		pathItem.setPath(PATH);
		config.getPaths().add(pathItem);

		OperationByMethod operation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		operation.setMethod(HttpMethod.POST);
		pathItem.getOperations().put(HttpMethod.POST, operation);
		operation.getParameters().add(body(schema));

		ServiceMethodBuilderByExpression.Config impl =
			TypedConfiguration.newConfigItem(ServiceMethodBuilderByExpression.Config.class);
		impl.setOperation(ExprFormat.INSTANCE.getValue(ServiceMethodBuilderByExpression.Config.OPERATION, "$body"));
		operation.setImplementation(impl);
		return config;
	}

	private static RequestBodyParameter.Config body(String schema) {
		RequestBodyParameter.Config body = TypedConfiguration.newConfigItem(RequestBodyParameter.Config.class);
		body.setName("body");
		body.setFormat(ParameterFormat.OBJECT);
		body.setSchema(schema);
		return body;
	}

	/**
	 * A POST request to {@link #PATH} with the given body.
	 */
	private static HttpServletRequest request(String body) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "getPathInfo":
						return PATH;
					case "getMethod":
						return HttpMethod.POST.name();
					case "getReader":
						return new BufferedReader(new StringReader(body));
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	/**
	 * {@link OpenApiServer} that can be started without the module system.
	 */
	private static class TestedServer extends OpenApiServer {

		public TestedServer(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		public void start() {
			startUp();
		}

	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestOpenApiServerSchemaValidation.class,
			ServiceTestSetup.createStarterFactoryForModules(
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}
}
