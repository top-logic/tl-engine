/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.layout;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.io.character.StringContent;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.ParameterObject;
import com.top_logic.service.openapi.common.layout.ImportOpenAPIConfiguration;
import com.top_logic.service.openapi.common.layout.MultiPartBodyTransferType;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.Information;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.layout.ImportOpenAPIServer;
import com.top_logic.service.openapi.server.layout.OpenAPIExporter;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter.ParameterConfiguration;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter.ParameterConfiguration.ValidVariableName;
import com.top_logic.service.openapi.server.parameter.HeaderParameter;
import com.top_logic.service.openapi.server.parameter.MultiPartBodyParameter;
import com.top_logic.service.openapi.server.parameter.ParameterFormat;
import com.top_logic.service.openapi.server.parameter.QueryParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * Test for the transport of the {@link ParameterConfiguration#getVariableName() variable name} of
 * request parameters through an <i>OpenAPI</i> document.
 *
 * @see ParameterObject#X_TL_VARIABLE_NAME
 */
public class TestOpenAPIVariableNameRoundTrip extends BasicTestCase {

	private static final String PATH = "/hook";

	/**
	 * Explicit variable names survive an export and a subsequent import, a parameter without
	 * variable name does not get one.
	 */
	public void testRoundTrip() throws ConfigurationException {
		OpenApiServer.Config<?> server = server();
		OperationByMethod operation = operation(server);
		operation.getParameters().add(parameter(HeaderParameter.Config.class, "X-Gitea-Event", "event"));
		operation.getParameters().add(parameter(QueryParameter.Config.class, "q", null));

		MultiPartBodyParameter.Config body = TypedConfiguration.newConfigItem(MultiPartBodyParameter.Config.class);
		body.setName("requestBody");
		body.setTransferType(MultiPartBodyTransferType.FORM_DATA);
		MultiPartBodyParameter.BodyPart part = TypedConfiguration.newConfigItem(MultiPartBodyParameter.BodyPart.class);
		part.setName("file-name");
		part.setVariableName("fileName");
		part.setFormat(ParameterFormat.STRING);
		body.getParts().put(part.getName(), part);
		operation.getParameters().add(body);

		OpenapiDocument document = parse(export(server));

		OperationByMethod imported = importedOperation(document);
		ConcreteRequestParameter.Config<?> event = parameter(imported, "X-Gitea-Event");
		assertEquals("event", event.getVariableName());
		assertEquals("event", event.effectiveVariableName());

		ConcreteRequestParameter.Config<?> q = parameter(imported, "q");
		assertNull(q.getVariableName());
		assertEquals("q", q.effectiveVariableName());

		MultiPartBodyParameter.Config importedBody =
			(MultiPartBodyParameter.Config) parameter(imported, "requestBody");
		MultiPartBodyParameter.BodyPart importedPart = importedBody.getParts().get("file-name");
		assertNotNull(importedPart);
		assertEquals("fileName", importedPart.getVariableName());
		assertEquals("fileName", importedPart.effectiveVariableName());
	}

	/**
	 * An imported document without variable names gets variable names for parameter names that
	 * are no TL-Script variable names.
	 */
	public void testImportForeignDocument() throws ConfigurationException {
		String json = "{"
			+ "\"openapi\": \"3.0.3\","
			+ "\"info\": {\"title\": \"Foreign\", \"version\": \"1.0\"},"
			+ "\"paths\": {\"" + PATH + "\": {\"post\": {"
			+ "  \"parameters\": ["
			+ "    {\"name\": \"X-Request-Id\", \"in\": \"header\", \"schema\": {\"type\": \"string\"}},"
			+ "    {\"name\": \"q\", \"in\": \"query\", \"schema\": {\"type\": \"string\"}}"
			+ "  ],"
			+ "  \"requestBody\": {\"content\": {\"multipart/form-data\": {\"schema\": {"
			+ "    \"type\": \"object\","
			+ "    \"properties\": {"
			+ "      \"upload-file\": {\"type\": \"string\"},"
			+ "      \"comment\": {\"type\": \"string\"}"
			+ "    }"
			+ "  }}}},"
			+ "  \"responses\": {\"200\": {\"description\": \"OK\"}}"
			+ "}}}"
			+ "}";

		OperationByMethod imported = importedOperation(parse(json));

		ConcreteRequestParameter.Config<?> requestId = parameter(imported, "X-Request-Id");
		assertEquals("X_Request_Id", requestId.getVariableName());

		ConcreteRequestParameter.Config<?> q = parameter(imported, "q");
		assertNull(q.getVariableName());

		MultiPartBodyParameter.Config body = (MultiPartBodyParameter.Config) parameter(imported, "requestBody");
		assertEquals("upload_file", body.getParts().get("upload-file").getVariableName());
		assertNull(body.getParts().get("comment").getVariableName());
	}

	/**
	 * Test for {@link ValidVariableName#toScriptIdentifier(String)}.
	 */
	public void testToScriptIdentifier() {
		assertEquals("X_Gitea_Event", ValidVariableName.toScriptIdentifier("X-Gitea-Event"));
		assertEquals("event", ValidVariableName.toScriptIdentifier("event"));
		assertEquals("_1st_value", ValidVariableName.toScriptIdentifier("1st.value"));
		assertEquals("a_b", ValidVariableName.toScriptIdentifier("aäb"));
		assertEquals("_", ValidVariableName.toScriptIdentifier(""));
		assertTrue(ValidVariableName.isScriptIdentifier(ValidVariableName.toScriptIdentifier("9-x y")));
	}

	private static OpenApiServer.Config<?> server() {
		OpenApiServer.Config<?> server = TypedConfiguration.newConfigItem(OpenApiServer.Config.class);
		Information information = TypedConfiguration.newConfigItem(Information.class);
		information.setTitle("Hooks");
		information.setVersion("1.0");
		server.setInformation(information);
		return server;
	}

	private static OperationByMethod operation(OpenApiServer.Config<?> server) {
		PathItem pathItem = TypedConfiguration.newConfigItem(PathItem.class);
		pathItem.setPath(PATH);
		server.getPaths().add(pathItem);

		OperationByMethod operation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		operation.setMethod(HttpMethod.POST);
		pathItem.getOperations().put(HttpMethod.POST, operation);
		return operation;
	}

	private static <C extends ConcreteRequestParameter.Config<?>> C parameter(Class<C> type, String name,
			String variableName) {
		C config = TypedConfiguration.newConfigItem(type);
		config.setName(name);
		config.setVariableName(variableName);
		config.setFormat(ParameterFormat.STRING);
		return config;
	}

	private static String export(OpenApiServer.Config<?> server) {
		OpenAPIExporter exporter = new OpenAPIExporter(server);
		exporter.createDocument("http://localhost/api");
		ByteArrayStream out = new ByteArrayStream();
		try {
			exporter.deliverTo(out);
		} catch (java.io.IOException ex) {
			throw new AssertionError(ex);
		}
		return new String(out.toByteArray(), StandardCharsets.UTF_8);
	}

	private static OpenapiDocument parse(String json) throws ConfigurationException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(OpenapiDocument.class);
		JsonConfigurationReader reader =
			new JsonConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptor);
		reader.treatUnexpectedEntriesAsWarn(true);
		reader.setSource(new StringContent(json));
		return (OpenapiDocument) reader.read();
	}

	private static OperationByMethod importedOperation(OpenapiDocument document) {
		ImportOpenAPIServer importer = new ImportOpenAPIServer(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			TypedConfiguration.newConfigItem(ImportOpenAPIConfiguration.Config.class));
		OpenApiServer.Config<?> server = TypedConfiguration.newConfigItem(OpenApiServer.Config.class);
		List<ResKey> warnings = new ArrayList<>();
		importer.importDocument(document, server, warnings);
		assertEquals(List.of(), warnings);

		PathItem pathItem = server.getPaths().stream()
			.filter(item -> PATH.equals(item.getPath()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Path not imported: " + PATH));
		OperationByMethod operation = pathItem.getOperations().get(HttpMethod.POST);
		assertNotNull(operation);
		return operation;
	}

	private static ConcreteRequestParameter.Config<?> parameter(OperationByMethod operation, String name) {
		for (RequestParameter.Config<?> parameter : operation.getParameters()) {
			if (name.equals(parameter.getName())) {
				return (ConcreteRequestParameter.Config<?>) parameter;
			}
		}
		throw new AssertionError("Parameter not imported: " + name);
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestOpenAPIVariableNameRoundTrip.class));
	}
}
