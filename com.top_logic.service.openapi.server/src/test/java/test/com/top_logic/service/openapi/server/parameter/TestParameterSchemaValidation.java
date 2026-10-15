/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.parameter;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.json.JSON;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.service.openapi.common.layout.MultiPartBodyTransferType;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.InvalidValueException;
import com.top_logic.service.openapi.server.parameter.MultiPartBodyParameter;
import com.top_logic.service.openapi.server.parameter.ParameterFormat;
import com.top_logic.service.openapi.server.parameter.QueryParameter;
import com.top_logic.service.openapi.server.parameter.RequestBodyParameter;
import com.top_logic.service.openapi.server.schema.I18NConstants;
import com.top_logic.service.openapi.server.schema.JsonSchemaCompiler;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for the validation of request parameter values against the
 * {@link ConcreteRequestParameter.ParameterConfiguration#getSchema() schema} of the parameter.
 */
public class TestParameterSchemaValidation extends BasicTestCase {

	private static final String RELEASE_SCHEMA = """
		{
			"type": "object",
			"required": ["name", "release"],
			"additionalProperties": false,
			"properties": {
				"name": {"type": "string"},
				"state": {"type": "string", "enum": ["open", "closed"]},
				"comment": {"type": "string", "nullable": true},
				"release": {"$ref": "#/components/schemas/Release"}
			}
		}
		""";

	private static final String RELEASE = """
		{
			"type": "object",
			"required": ["version"],
			"properties": {
				"version": {"type": "string"},
				"author": {"$ref": "#/components/schemas/Person"}
			}
		}
		""";

	private static final String PERSON = """
		{
			"type": "object",
			"required": ["login"],
			"properties": {
				"login": {"type": "string"}
			}
		}
		""";

	/**
	 * A value matching the schema is passed to the implementation unchanged.
	 */
	public void testValidBody() throws Exception {
		String body = "{\"name\": \"tl\", \"state\": \"open\", \"comment\": \"c\", "
			+ "\"release\": {\"version\": \"8.0\", \"author\": {\"login\": \"bhu\"}}}";
		Object value = parseBody(body(RELEASE_SCHEMA), body);
		assertEquals(JSON.fromString(body), value);
	}

	/**
	 * A required property must be given.
	 */
	public void testMissingRequiredProperty() throws Exception {
		assertViolation(body(RELEASE_SCHEMA), "{\"release\": {\"version\": \"8.0\"}}",
			"Value of parameter 'body' does not match its schema: $: required property 'name' not found");
	}

	/**
	 * A property must have the declared type.
	 */
	public void testWrongType() throws Exception {
		assertViolation(body(RELEASE_SCHEMA), "{\"name\": 42, \"release\": {\"version\": \"8.0\"}}",
			"Value of parameter 'body' does not match its schema: $.name: integer found, string expected");
	}

	/**
	 * An enumeration property must have one of the declared values.
	 */
	public void testEnumViolation() throws Exception {
		assertViolation(body(RELEASE_SCHEMA),
			"{\"name\": \"tl\", \"state\": \"done\", \"release\": {\"version\": \"8.0\"}}",
			"$.state: does not have a value in the enumeration [\"open\", \"closed\"]");
	}

	/**
	 * Undeclared properties are rejected with <code>additionalProperties: false</code>.
	 */
	public void testAdditionalProperty() throws Exception {
		assertViolation(body(RELEASE_SCHEMA),
			"{\"name\": \"tl\", \"extra\": 1, \"release\": {\"version\": \"8.0\"}}",
			"$: property 'extra' is not defined in the schema and the schema does not allow additional properties");
	}

	/**
	 * A <code>nullable</code> property accepts <code>null</code>, other properties do not.
	 */
	public void testNullable() throws Exception {
		parseBody(body(RELEASE_SCHEMA), "{\"name\": \"tl\", \"comment\": null, \"release\": {\"version\": \"8.0\"}}");

		assertViolation(body(RELEASE_SCHEMA), "{\"name\": null, \"release\": {\"version\": \"8.0\"}}",
			"$.name: null found, string expected");
	}

	/**
	 * A violation inside a referenced global schema is reported with its location.
	 */
	public void testViolationInReferencedSchema() throws Exception {
		assertViolation(body(RELEASE_SCHEMA), "{\"name\": \"tl\", \"release\": {\"version\": 8}}",
			"Value of parameter 'body' does not match its schema: $.release.version: integer found, string expected");
	}

	/**
	 * Global schemas can reference each other.
	 */
	public void testViolationInTransitivelyReferencedSchema() throws Exception {
		assertViolation(body(RELEASE_SCHEMA),
			"{\"name\": \"tl\", \"release\": {\"version\": \"8.0\", \"author\": {}}}",
			"$.release.author: required property 'login' not found");
	}

	/**
	 * All violations of a value are reported.
	 */
	public void testMultipleViolations() throws Exception {
		InvalidValueException ex = violation(body(RELEASE_SCHEMA), "{\"name\": 1, \"release\": {}}");
		assertContains("$.name: integer found, string expected", ex.getMessage());
		assertContains("$.release: required property 'version' not found", ex.getMessage());
	}

	/**
	 * An empty schema defines no schema, every value is accepted.
	 */
	public void testEmptySchema() throws Exception {
		assertEquals(JSON.fromString("{\"any\": 1}"), parseBody(body(""), "{\"any\": 1}"));
		assertEquals(JSON.fromString("{\"any\": 1}"), parseBody(body(null), "{\"any\": 1}"));
	}

	/**
	 * A parameter that is not given is not validated.
	 */
	public void testMissingValue() throws Exception {
		Map<String, Object> arguments = parse(body(RELEASE_SCHEMA), request("", Collections.emptyMap()));
		assertFalse(arguments.containsKey("body"));
	}

	/**
	 * Object values of a query parameter are validated, too.
	 */
	public void testQueryParameter() throws Exception {
		QueryParameter.Config config = TypedConfiguration.newConfigItem(QueryParameter.Config.class);
		config.setName("filter");
		config.setFormat(ParameterFormat.OBJECT);
		config.setSchema("{\"type\": \"object\", \"properties\": {\"limit\": {\"type\": \"integer\"}}}");

		Map<String, Object> arguments = parse(config, request("", Map.of("filter", "{\"limit\": 5}")));
		assertEquals(JSON.fromString("{\"limit\": 5}"), arguments.get("filter"));

		try {
			parse(config, request("", Map.of("filter", "{\"limit\": \"5\"}")));
			fail("Schema violation expected.");
		} catch (InvalidValueException ex) {
			assertEquals("Value of parameter 'filter' does not match its schema: $.limit: string found, integer expected",
				ex.getMessage());
		}
	}

	/**
	 * Object values of a multipart body part are validated against the schema of the part.
	 */
	public void testMultiPartPart() throws Exception {
		MultiPartBodyParameter.Config config = TypedConfiguration.newConfigItem(MultiPartBodyParameter.Config.class);
		config.setTransferType(MultiPartBodyTransferType.URL_ENCODED);
		MultiPartBodyParameter.BodyPart part = TypedConfiguration.newConfigItem(MultiPartBodyParameter.BodyPart.class);
		part.setName("meta");
		part.setFormat(ParameterFormat.OBJECT);
		part.setSchema("{\"$ref\": \"#/components/schemas/Person\"}");
		config.getParts().put(part.getName(), part);

		Map<String, Object> arguments = parse(config, request("meta=%7B%22login%22%3A%22bhu%22%7D", Map.of()));
		assertEquals(JSON.fromString("{\"login\": \"bhu\"}"), arguments.get("meta"));

		try {
			parse(config, request("meta=%7B%7D", Map.of()));
			fail("Schema violation expected.");
		} catch (InvalidValueException ex) {
			assertEquals("Value of parameter 'meta' does not match its schema: $: required property 'login' not found",
				ex.getMessage());
		}
	}

	/**
	 * A schema that is no valid JSON is rejected when compiling.
	 */
	public void testSchemaNotJson() {
		assertCompileError(body("{\"type\": "), I18NConstants.ERROR_SCHEMA_NOT_JSON__PROBLEM.getKey());
	}

	/**
	 * A reference to an undefined global schema is rejected when compiling.
	 */
	public void testUnresolvableReference() {
		assertCompileError(body("{\"$ref\": \"#/components/schemas/Undefined\"}"),
			I18NConstants.ERROR_SCHEMA_NOT_COMPILABLE__PROBLEM.getKey());
	}

	/**
	 * A global schema that is no valid JSON is rejected when compiling.
	 */
	public void testGlobalSchemaNotJson() {
		Map<String, SchemaObject> schemas = new LinkedHashMap<>(globalSchemas());
		schemas.put("Broken", schema("Broken", "{"));
		ConcreteRequestParameter<?> parameter = TypedConfigUtil.createInstance(body(RELEASE_SCHEMA));
		try {
			parameter.compileSchemas(new JsonSchemaCompiler(schemas));
			fail("Compile error expected.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_GLOBAL_SCHEMA_NOT_JSON__NAME__PROBLEM.getKey(),
				ex.getErrorKey().plain().getKey());
		}
	}

	/**
	 * An invalid schema of a multipart body part is reported with the part name.
	 */
	public void testInvalidPartSchema() {
		MultiPartBodyParameter.Config config = TypedConfiguration.newConfigItem(MultiPartBodyParameter.Config.class);
		MultiPartBodyParameter.BodyPart part = TypedConfiguration.newConfigItem(MultiPartBodyParameter.BodyPart.class);
		part.setName("meta");
		part.setFormat(ParameterFormat.OBJECT);
		part.setSchema("[");
		config.getParts().put(part.getName(), part);

		ConcreteRequestParameter<?> parameter = TypedConfigUtil.createInstance(config);
		try {
			parameter.compileSchemas(new JsonSchemaCompiler(globalSchemas()));
			fail("Compile error expected.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.service.openapi.server.parameter.I18NConstants.ERROR_INVALID_PART_SCHEMA__PART
				.getKey(), ex.getErrorKey().plain().getKey());
			assertEquals(I18NConstants.ERROR_SCHEMA_NOT_JSON__PROBLEM.getKey(),
				((TopLogicException) ex.getCause()).getErrorKey().plain().getKey());
		}
	}

	private void assertCompileError(ConcreteRequestParameter.Config<?> config, String expectedKey) {
		ConcreteRequestParameter<?> parameter = TypedConfigUtil.createInstance(config);
		try {
			parameter.compileSchemas(new JsonSchemaCompiler(globalSchemas()));
			fail("Compile error expected.");
		} catch (TopLogicException ex) {
			assertEquals(expectedKey, ex.getErrorKey().plain().getKey());
		}
	}

	private void assertViolation(ConcreteRequestParameter.Config<?> config, String body, String expectedMessagePart)
			throws Exception {
		assertContains(expectedMessagePart, violation(config, body).getMessage());
	}

	private InvalidValueException violation(ConcreteRequestParameter.Config<?> config, String body)
			throws Exception {
		try {
			parseBody(config, body);
			fail("Schema violation expected for: " + body);
			throw new AssertionError();
		} catch (InvalidValueException ex) {
			return ex;
		}
	}

	private static Object parseBody(ConcreteRequestParameter.Config<?> config, String body)
			throws InvalidValueException {
		return parse(config, request(body, Collections.emptyMap())).get(config.getName());
	}

	private static Map<String, Object> parse(ConcreteRequestParameter.Config<?> config, HttpServletRequest request)
			throws InvalidValueException {
		ConcreteRequestParameter<?> parameter = TypedConfigUtil.createInstance(config);
		parameter.compileSchemas(new JsonSchemaCompiler(globalSchemas()));
		Map<String, Object> arguments = new HashMap<>();
		parameter.parse(arguments, request, Collections.emptyMap());
		return arguments;
	}

	private static RequestBodyParameter.Config body(String schema) {
		RequestBodyParameter.Config config = TypedConfiguration.newConfigItem(RequestBodyParameter.Config.class);
		config.setName("body");
		config.setFormat(ParameterFormat.OBJECT);
		config.setSchema(schema);
		return config;
	}

	private static Map<String, SchemaObject> globalSchemas() {
		Map<String, SchemaObject> result = new LinkedHashMap<>();
		result.put("Release", schema("Release", RELEASE));
		result.put("Person", schema("Person", PERSON));
		return result;
	}

	private static SchemaObject schema(String name, String json) {
		SchemaObject result = TypedConfiguration.newConfigItem(SchemaObject.class);
		result.setName(name);
		result.setSchema(json);
		return result;
	}

	/**
	 * A {@link HttpServletRequest} with the given body and single-valued query parameters.
	 */
	private static HttpServletRequest request(String body, Map<String, String> parameters) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "getReader":
						return new BufferedReader(new StringReader(body));
					case "getCharacterEncoding":
						return "utf-8";
					case "getParameter":
						return parameters.get(args[0]);
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestParameterSchemaValidation.class));
	}
}
