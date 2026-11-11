/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.discovery;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.top_logic.base.discovery.AgentDiscoveryContribution;
import com.top_logic.base.discovery.AgentDiscoveryService;
import com.top_logic.base.discovery.AgentDiscoveryServlet;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.base.discovery.catalog.AiCatalog;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.servlet.CacheControlFilter;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Test of {@link AgentDiscoveryService} and {@link AgentDiscoveryServlet}.
 */
@SuppressWarnings("javadoc")
public class TestAgentDiscoveryService extends BasicTestCase {

	private static final String CONTEXT_PATH = "/app";

	private static final String SCHEME = "https";

	private static final String HOST = "agents.example.com";

	private static final int PORT = 8443;

	private static final String APPLICATION_URL = SCHEME + "://" + HOST + ":" + PORT + CONTEXT_PATH;

	/**
	 * The variable the configured {@link AliasManager#HOST} alias is read from (environment
	 * variable or system property).
	 */
	private static final String HOST_VARIABLE = "tl_host";

	private static final String CONFIGURED_HOST = "https://public.example.com";

	private static final String SERVLET_NAME = "discovery";

	private static final String WEB_XML = """
		<web-app>
			<servlet>
				<servlet-name>%1$s</servlet-name>
				<servlet-class>%2$s</servlet-class>
			</servlet>
			<servlet-mapping>
				<servlet-name>%1$s</servlet-name>
				<url-pattern>%3$s</url-pattern>
			</servlet-mapping>
			<servlet-mapping>
				<servlet-name>%1$s</servlet-name>
				<url-pattern>%4$s</url-pattern>
			</servlet-mapping>
		</web-app>
		""".formatted(SERVLET_NAME, AgentDiscoveryServlet.class.getName(), AgentDiscoveryServlet.LLMS_TXT_PATH,
		AgentDiscoveryServlet.AI_CATALOG_PATH);

	/** Media type of the catalog entries of {@link TestContribution}. */
	static final String TEST_ENTRY_TYPE = "application/test-card+json";

	/** Identifier prefix of the catalog entries of {@link TestContribution}. */
	static final String TEST_IDENTIFIER_PREFIX = "urn:air:test:endpoints:";

	public void testLlmsTxt() throws Exception {
		WebResponse response = get(AgentDiscoveryServlet.LLMS_TXT_PATH);

		assertEquals(200, response.getResponseCode());
		assertEquals(AgentDiscoveryServlet.MARKDOWN_MEDIA_TYPE, response.getContentType());
		assertPublic(response);

		String text = response.getText();
		assertTrue(text, text.startsWith("# Test Application\n"));
		assertContains("\n> An application for testing the agent discovery.\n", text);
		assertContains(APPLICATION_URL + AgentDiscoveryServlet.AI_CATALOG_PATH, text);
		assertContains("\n## Test Endpoint\n", text);
		assertContains("- [Test Endpoint](" + APPLICATION_URL + "/test-endpoint): A test endpoint.", text);
		assertFalse(text, text.contains("Inactive Endpoint"));
	}

	public void testCatalog() throws Exception {
		WebResponse response = get(AgentDiscoveryServlet.AI_CATALOG_PATH);

		assertEquals(200, response.getResponseCode());
		assertEquals(AgentDiscoveryServlet.AI_CATALOG_MEDIA_TYPE, response.getContentType());
		assertPublic(response);

		String json = response.getText();
		assertContains("\"specVersion\":\"" + AgentDiscoveryService.AI_CATALOG_SPEC_VERSION + "\"", json);
		assertFalse(json, json.contains("inactive-endpoint"));

		AiCatalog catalog = AiCatalog.readAiCatalog(new JsonReader(new StringR(json)));
		assertEquals(AgentDiscoveryService.AI_CATALOG_SPEC_VERSION, catalog.getSpecVersion());
		assertEquals("Test Application", catalog.getHost().getDisplayName());
		assertEquals(APPLICATION_URL + AgentDiscoveryServlet.LLMS_TXT_PATH, catalog.getHost().getDocumentationUrl());

		List<CatalogEntry> entries = catalog.getEntries();
		assertEquals(1, entries.size());
		CatalogEntry entry = entries.get(0);
		assertEquals(TEST_IDENTIFIER_PREFIX + "test-endpoint", entry.getIdentifier());
		assertEquals(TEST_ENTRY_TYPE, entry.getType());
		assertEquals(APPLICATION_URL + "/test-endpoint", entry.getUrl());
		assertEquals("Test Endpoint", entry.getDisplayName());
		assertFalse(entry.hasData());
	}

	public void testApplicationUrl() throws Exception {
		assertEquals("https://host:8443/app", url("https", "host", 8443, "/app"));
		assertEquals("http://host:8080", url("http", "host", 8080, ""));
		assertEquals("http://host:8080/x", new DiscoveryContext("http://host:8080/").url("x"));
	}

	/**
	 * A configured {@link AliasManager#HOST} wins over the host of the request, for a reverse proxy
	 * that does not forward the address the client used.
	 */
	public void testConfiguredHost() throws Exception {
		String previous = System.getProperty(HOST_VARIABLE);
		System.setProperty(HOST_VARIABLE, CONFIGURED_HOST);
		AliasManager.getInstance().reload();
		try {
			assertEquals(CONFIGURED_HOST + "/app", url("http", "internal", 8080, "/app"));

			String json = get(AgentDiscoveryServlet.AI_CATALOG_PATH).getText();
			AiCatalog catalog = AiCatalog.readAiCatalog(new JsonReader(new StringR(json)));
			assertEquals(CONFIGURED_HOST + CONTEXT_PATH + "/test-endpoint", catalog.getEntries().get(0).getUrl());
		} finally {
			if (previous == null) {
				System.clearProperty(HOST_VARIABLE);
			} else {
				System.setProperty(HOST_VARIABLE, previous);
			}
			AliasManager.getInstance().reload();
		}
	}

	private static String url(String scheme, String host, int port, String contextPath) throws Exception {
		HttpServletRequest request = invocation(AgentDiscoveryServlet.LLMS_TXT_PATH).getRequest();
		return DiscoveryContext.fromRequest(new ForwardedRequest(request, scheme, host, port, contextPath))
			.applicationUrl();
	}

	private static void assertPublic(WebResponse response) {
		assertEquals(AgentDiscoveryServlet.ANY_ORIGIN,
			response.getHeaderField(AgentDiscoveryServlet.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER));
		assertEquals(AgentDiscoveryServlet.CACHE_CONTROL,
			response.getHeaderField(CacheControlFilter.CACHE_CONTROL_HEADER));
	}

	/**
	 * Requests the given context-relative path as a client reaching the application at
	 * {@link #APPLICATION_URL}.
	 */
	private static WebResponse get(String path) throws Exception {
		InvocationContext invocation = invocation(path);
		HttpServletRequest request = invocation.getRequest();
		invocation.getServlet().service(
			new ForwardedRequest(request, SCHEME, HOST, PORT, request.getContextPath()),
			invocation.getResponse());
		return invocation.getServletResponse();
	}

	private static InvocationContext invocation(String path) throws Exception {
		ServletRunner runner =
			new ServletRunner(new ByteArrayInputStream(WEB_XML.getBytes(StandardCharsets.UTF_8)), CONTEXT_PATH);
		ServletUnitClient client = runner.newClient();
		return client.newInvocation(new GetMethodWebRequest("http://localhost" + CONTEXT_PATH + path));
	}

	/**
	 * A request as the servlet container reports it behind a proxy that forwards scheme, host and
	 * port.
	 */
	private static class ForwardedRequest extends HttpServletRequestWrapper {

		private final String _scheme;

		private final String _serverName;

		private final int _serverPort;

		private final String _contextPath;

		ForwardedRequest(HttpServletRequest request, String scheme, String serverName, int serverPort,
				String contextPath) {
			super(request);
			_scheme = scheme;
			_serverName = serverName;
			_serverPort = serverPort;
			_contextPath = contextPath;
		}

		@Override
		public String getScheme() {
			return _scheme;
		}

		@Override
		public String getServerName() {
			return _serverName;
		}

		@Override
		public int getServerPort() {
			return _serverPort;
		}

		@Override
		public String getContextPath() {
			return _contextPath;
		}

	}

	/**
	 * {@link AgentDiscoveryContribution} describing one test endpoint.
	 */
	public static class TestContribution implements AgentDiscoveryContribution {

		/**
		 * Configuration of a {@link TestContribution}.
		 */
		public interface Config<I extends TestContribution> extends PolymorphicConfiguration<I> {

			/** The name of the endpoint. */
			@Name("title")
			String getTitle();

			/** The context-relative path of the endpoint. */
			@Name("path")
			String getPath();

			/** Whether the endpoint is available. */
			@Name("active")
			boolean isActive();

		}

		private final Config<?> _config;

		/**
		 * Creates a {@link TestContribution} from configuration.
		 */
		public TestContribution(InstantiationContext context, Config<?> config) {
			_config = config;
		}

		@Override
		public boolean isActive() {
			return _config.isActive();
		}

		@Override
		public List<LlmsTxtSection> llmsTxtSections(DiscoveryContext context) {
			String title = _config.getTitle();
			return List.of(new LlmsTxtSection(title,
				"- [" + title + "](" + context.url(_config.getPath()) + "): A test endpoint.\n"));
		}

		@Override
		public List<CatalogEntry> catalogEntries(DiscoveryContext context) {
			return List.of(CatalogEntry.create()
				.setIdentifier(TEST_IDENTIFIER_PREFIX + _config.getPath().substring(1))
				.setType(TEST_ENTRY_TYPE)
				.setUrl(context.url(_config.getPath()))
				.setDisplayName(_config.getTitle()));
		}

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestAgentDiscoveryService}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestAgentDiscoveryService.class);
		test = ServiceTestSetup.createSetup(test, AgentDiscoveryService.Module.INSTANCE);
		test = new CustomPropertiesSetup(test,
			CustomPropertiesDecorator.createFileName(TestAgentDiscoveryService.class), true);
		return TLTestSetup.createTLTestSetup(test);
	}

}
