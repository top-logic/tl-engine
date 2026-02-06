/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.common.document;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.yaml.snakeyaml.Yaml;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.document.IParameterObject;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.OperationObject;
import com.top_logic.service.openapi.common.document.ParameterLocation;
import com.top_logic.service.openapi.common.document.ParameterObject;
import com.top_logic.service.openapi.common.document.PathItemObject;
import com.top_logic.service.openapi.common.document.ReferencingParameterObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;

/**
 * Test case for reading OpenAPI documents as typed configuration using the JSON binding.
 *
 * <p>
 * Tests that {@link OpenapiDocument} and related configuration interfaces can be correctly read
 * from JSON/YAML OpenAPI specification files using {@link JsonConfigurationReader}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestOpenapiDocumentJsonBinding extends BasicTestCase {

	/**
	 * Tests reading the TestServerClient.import.json OpenAPI document.
	 */
	public void testReadServerClientJson() throws Exception {
		OpenapiDocument doc = readOpenAPIDocument("TestServerClient.import.json");

		// Verify basic document structure
		assertEquals("3.0.3", doc.getOpenapi());
		assertEquals("TL-Demo OpenAPI Server", doc.getInfo().getTitle());
		assertEquals("1.0", doc.getInfo().getVersion());

		// Verify servers
		assertEquals(1, doc.getServers().size());
		assertEquals("http://localhost:8080/demo/api", doc.getServers().get(0).getUrl());

		// Verify paths
		Map<String, PathItemObject> paths = doc.getPaths();
		assertEquals(4, paths.size());
		assertTrue(paths.containsKey("/structure/demo/get/{target}"));
		assertTrue(paths.containsKey("/structure/demo/create/{parent}"));
		assertTrue(paths.containsKey("/structure/demo/delete/{target}"));
		assertTrue(paths.containsKey("/structure/demo/getAs"));

		// Verify GET operation on /structure/demo/get/{target}
		PathItemObject getPath = paths.get("/structure/demo/get/{target}");
		assertNotNull(getPath.getGet());
		OperationObject getOp = getPath.getGet();
		assertEquals("Returns attribute values of elements", getOp.getSummary());
		assertEquals("getAttributeValues", getOp.getOperationId());

		// Verify parameter reference
		List<IParameterObject> pathParams = getPath.getParameters();
		assertEquals(1, pathParams.size());
		assertTrue(pathParams.get(0) instanceof ReferencingParameterObject);
		ReferencingParameterObject paramRef = (ReferencingParameterObject) pathParams.get(0);
		assertEquals("#/components/parameters/targetParam", paramRef.getReference());

		// Verify PUT operation on /structure/demo/create/{parent}
		PathItemObject createPath = paths.get("/structure/demo/create/{parent}");
		assertNotNull(createPath.getPut());
		OperationObject putOp = createPath.getPut();
		assertEquals("Creates a new element", putOp.getSummary());
		assertEquals("createNewElement", putOp.getOperationId());

		// Verify cookie parameter
		List<IParameterObject> putParams = putOp.getParameters();
		assertEquals(1, putParams.size());
		assertTrue(putParams.get(0) instanceof ParameterObject);
		ParameterObject cookieParam = (ParameterObject) putParams.get(0);
		assertEquals("elementType", cookieParam.getName());
		assertEquals(ParameterLocation.COOKIE, cookieParam.getIn());
		assertTrue(cookieParam.isRequired());

		// Verify security schemes in components
		Map<String, SecuritySchemeObject> securitySchemes =
			doc.getComponents().getSecuritySchemes();
		assertEquals(4, securitySchemes.size());

		// API Key scheme
		SecuritySchemeObject getDataScheme = securitySchemes.get("GetData");
		assertNotNull(getDataScheme);
		assertEquals(SecuritySchemeType.API_KEY, getDataScheme.getType());
		assertEquals("authParameter", getDataScheme.getName());
		assertEquals(APIKeyPosition.COOKIE, getDataScheme.getIn());

		// HTTP Basic scheme
		SecuritySchemeObject putDataScheme = securitySchemes.get("PutData");
		assertNotNull(putDataScheme);
		assertEquals(SecuritySchemeType.HTTP, putDataScheme.getType());
		assertEquals("Basic", putDataScheme.getScheme());

		// OpenID Connect scheme
		SecuritySchemeObject deleteDataScheme = securitySchemes.get("DeleteData");
		assertNotNull(deleteDataScheme);
		assertEquals(SecuritySchemeType.OPEN_ID_CONNECT, deleteDataScheme.getType());
		assertNotNull(deleteDataScheme.getOpenIdConnectUrl());

		// Verify global parameters
		assertEquals(1, doc.getComponents().getParameters().size());
		assertNotNull(doc.getComponents().getParameters().get("targetParam"));
	}

	/**
	 * Tests reading the TestMultiPartBody.import.yaml OpenAPI document (converted from YAML).
	 */
	public void testReadMultiPartBodyYaml() throws Exception {
		OpenapiDocument doc = readOpenAPIDocumentFromYaml("TestMultiPartBody.import.yaml");

		// Verify basic document structure
		assertEquals("3.0.3", doc.getOpenapi());
		assertEquals("TL-Demo OpenAPI Server", doc.getInfo().getTitle());
		assertEquals("1.0", doc.getInfo().getVersion());

		// Verify servers
		assertEquals(1, doc.getServers().size());
		assertEquals("http://localhost:8080/demo/api", doc.getServers().get(0).getUrl());

		// Verify paths
		Map<String, PathItemObject> paths = doc.getPaths();
		assertEquals(2, paths.size());
		assertTrue(paths.containsKey("/createA/{parent}"));
		assertTrue(paths.containsKey("/setBinaryValue/{target}"));

		// Verify PUT operation on /createA/{parent}
		PathItemObject createPath = paths.get("/createA/{parent}");
		assertNotNull(createPath.getPut());
		OperationObject putOp = createPath.getPut();
		assertEquals("Creates a new A", putOp.getSummary());

		// Verify request body with form-urlencoded content
		assertNotNull(putOp.getRequestBody());
		assertTrue(putOp.getRequestBody().isRequired());
		assertNotNull(putOp.getRequestBody().getContent().get("application/x-www-form-urlencoded"));

		// Verify path parameter
		List<IParameterObject> pathParams = createPath.getParameters();
		assertEquals(1, pathParams.size());
		assertTrue(pathParams.get(0) instanceof ParameterObject);
		ParameterObject parentParam = (ParameterObject) pathParams.get(0);
		assertEquals("parent", parentParam.getName());
		assertEquals(ParameterLocation.PATH, parentParam.getIn());
		assertTrue(parentParam.isRequired());

		// Verify PUT operation on /setBinaryValue/{target} with multipart/form-data
		PathItemObject binaryPath = paths.get("/setBinaryValue/{target}");
		assertNotNull(binaryPath.getPut());
		OperationObject binaryOp = binaryPath.getPut();
		assertNotNull(binaryOp.getRequestBody());
		assertNotNull(binaryOp.getRequestBody().getContent().get("multipart/form-data"));
	}

	/**
	 * Reads an OpenAPI document from a JSON resource file.
	 */
	private OpenapiDocument readOpenAPIDocument(String resourceName) throws Exception {
		try (InputStream in = getClass().getResourceAsStream(resourceName)) {
			assertNotNull("Resource not found: " + resourceName, in);
			String json = new String(StreamUtilities.readStreamContents(in), "UTF-8");
			return parseOpenAPIJson(json);
		}
	}

	/**
	 * Reads an OpenAPI document from a YAML resource file (converts to JSON first).
	 */
	private OpenapiDocument readOpenAPIDocumentFromYaml(String resourceName) throws Exception {
		try (InputStream in = getClass().getResourceAsStream(resourceName)) {
			assertNotNull("Resource not found: " + resourceName, in);

			// Convert YAML to JSON using the same approach as ImportOpenAPIConfiguration
			Object yamlData = new Yaml().load(in);
			ByteArrayOutputStream jsonOut = new ByteArrayOutputStream();
			new ObjectMapper().writeValue(jsonOut, yamlData);
			String json = jsonOut.toString("UTF-8");

			return parseOpenAPIJson(json);
		}
	}

	/**
	 * Parses an OpenAPI document from a JSON string.
	 */
	private OpenapiDocument parseOpenAPIJson(String json) throws ConfigurationException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(OpenapiDocument.class);

		BufferingI18NLog log = new BufferingI18NLog();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log.asLog());

		JsonConfigurationReader reader = new JsonConfigurationReader(context, descriptor);
		reader.treatUnexpectedEntriesAsWarn(true);

		CharacterContent content = CharacterContents.newContent(json, "test-openapi.json");
		reader.setSource(content);

		OpenapiDocument doc = (OpenapiDocument) reader.read();

		// Log any warnings for debugging
		if (log.hasEntries()) {
			log.getEntries().forEach(entry ->
				System.out.println("[" + entry.getLevel() + "] " + entry.getMessage()));
		}

		return doc;
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestOpenapiDocumentJsonBinding.class));
	}

}
