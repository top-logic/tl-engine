/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;


import com.top_logic.ai.mcp.server.authentication.McpAuthentication;
import com.top_logic.ai.mcp.server.authentication.McpAuthenticationFilter;
import com.top_logic.ai.mcp.server.authentication.McpSessionPrincipals;
import com.top_logic.ai.mcp.server.completions.ModuleNameCompletion;
import com.top_logic.ai.mcp.server.dynamic.ConfigurableResourceTemplate;
import com.top_logic.ai.mcp.server.dynamic.DynamicResource;
import com.top_logic.ai.mcp.server.resources.ConfigSchemaResource;
import com.top_logic.ai.mcp.server.resources.EnumerationClassifiersResource;
import com.top_logic.ai.mcp.server.resources.InstanceResource;
import com.top_logic.ai.mcp.server.resources.ModuleTypesResource;
import com.top_logic.ai.mcp.server.resources.TypeInstancesResource;
import com.top_logic.ai.mcp.server.resources.TypePartsResource;
import com.top_logic.ai.mcp.server.resources.TypeUsagesResource;
import com.top_logic.ai.mcp.server.tools.McpToolAdapter;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentications;
import com.top_logic.service.openapi.server.authentication.conf.ServerSecret;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

/**
 * TopLogic service that provides an MCP (Model Context Protocol) server for exposing
 * application-specific APIs to AI models via HTTP transport.
 *
 * <p>
 * This service initializes an MCP server during application startup using the MCP protocol
 * over HTTP and ensures proper cleanup during shutdown. The server can be configured through
 * the TopLogic application configuration.
 * </p>
 *
 * <p>
 * The service exposes HTTP endpoints (registered in web-fragment.xml under /mcp/*) that
 * implement the MCP protocol for bidirectional communication between AI clients and the
 * TopLogic application. {@link McpAuthenticationFilter MCP authentication filter} guards that
 * path: it authenticates every request and opens the {@code TLContext} of the account it
 * resolved, which the protocol handlers then run in.
 * </p>
 *
 * <p>
 * <b>This service is switched off in the configuration this module ships</b>, and an application
 * that switches it on has to say who may reach it: {@code /mcp/*} serves no request it cannot
 * attribute to an account, and it accepts only the {@link Config#getAuthentications()
 * authentications} the application configures. Configure none and every request is refused. See
 * {@code doc/security/mcp-endpoint.md} and issue #261. While the service is off,
 * {@code MCPTransportServlet} answers the whole {@code /mcp/*} path with 503.
 * </p>
 *
 * <p>
 * The service uses {@link ServletContextService} to obtain the application's context path
 * at startup, ensuring that endpoint URLs sent to clients include the correct context path
 * regardless of the deployment configuration.
 * </p>
 *
 * @author Bernhard Haumacher
 */
@ServiceDependencies({
	ServletContextService.Module.class,
	ToolRegistryService.Module.class
})
public class MCPServerService extends ConfiguredManagedClass<MCPServerService.Config<?>> {

	/** Path for the MCP endpoint (must match web.xml servlet mapping /mcp). */
	private static final String MCP_ENDPOINT = "/mcp";

	/**
	 * Configuration interface for {@link MCPServerService}.
	 */
	@DisplayOrder({
		Config.SERVER_NAME,
		Config.SERVER_VERSION,
		Config.KEEP_ALIVE_INTERVAL,
		ServerAuthentications.AUTHENTICATIONS,
		Config.SECRETS,
		Config.DYNAMIC_RESOURCES,
	})
	@DisplayInherited(DisplayStrategy.PREPEND)
	public interface Config<I extends MCPServerService>
			extends ConfiguredManagedClass.Config<I>, ServerAuthentications {

		/**
		 * Configuration property name for server name.
		 *
		 * @see #getServerName()
		 */
		String SERVER_NAME = "server-name";

		/**
		 * Configuration property name for server version.
		 *
		 * @see #getServerVersion()
		 */
		String SERVER_VERSION = "server-version";

		/**
		 * Configuration property name for keep-alive interval in seconds.
		 *
		 * @see #getKeepAliveInterval()
		 */
		String KEEP_ALIVE_INTERVAL = "keep-alive-interval";

		/**
		 * Configuration property name for dynamic resources.
		 *
		 * @see #getDynamicResources()
		 */
		String DYNAMIC_RESOURCES = "dynamic-resources";

		/**
		 * Configuration property name for the secrets callers authenticate with.
		 *
		 * @see #getSecrets()
		 */
		String SECRETS = "secrets";

		/**
		 * The name of the MCP server.
		 */
		@Name(SERVER_NAME)
		@StringDefault("TopLogic MCP Server")
		String getServerName();

		/**
		 * The version of the MCP server.
		 */
		@Name(SERVER_VERSION)
		@StringDefault("1.0.0")
		String getServerVersion();

		/**
		 * Keep-alive interval in seconds for SSE connections.
		 *
		 * <p>Default: 30 seconds</p>
		 */
		@Name(KEEP_ALIVE_INTERVAL)
		@LongDefault(30)
		long getKeepAliveInterval();

		/**
		 * List of configurable dynamic resources.
		 *
		 * <p>
		 * Each resource is configured using the polymorphic configuration pattern, allowing
		 * different implementations to provide MCP resources in different ways:
		 * </p>
		 * <ul>
		 * <li>{@link ConfigurableResourceTemplate} - Uses TL-Script expressions to compute content</li>
		 * <li>Future implementations can add other resource types (static, database-driven, etc.)</li>
		 * </ul>
		 */
		@Name(DYNAMIC_RESOURCES)
		List<PolymorphicConfiguration<? extends DynamicResource>> getDynamicResources();

		/**
		 * How a caller of the MCP endpoint proves who it is.
		 *
		 * <p>
		 * Configure at least one, or the endpoint refuses every request: it serves nothing it cannot
		 * attribute to an account. HTTP basic authenticates against the account's own password
		 * database, an API key maps a key to a named account, and a bearer token is validated
		 * against the authorization server the deployment runs. A caller may present any one of the
		 * configured schemes.
		 * </p>
		 *
		 * <p>
		 * Prefer an API key for a service account over the password of a real person: it is
		 * revocable on its own, and it keeps a login out of a client configuration file.
		 * </p>
		 */
		@Override
		@DisplayMinimized
		Map<String, ServerAuthentication.Config<?>> getAuthentications();

		/**
		 * The secrets the configured authentications check a caller against.
		 *
		 * <p>
		 * Each secret names the domain of the authentication it belongs to. A secret must name the
		 * account the request then acts as: this endpoint never processes a request in system
		 * context, so a secret without an account is refused at request time.
		 * </p>
		 */
		@Name(SECRETS)
		@DisplayMinimized
		List<ServerSecret> getSecrets();
	}

	private static volatile MCPServerService _instance;

	private McpSyncServer _mcpServer;

	private HttpServletStreamableServerTransportProvider _transportProvider;

	private Authenticator _authenticator;

	private final McpSessionPrincipals _sessionPrincipals = new McpSessionPrincipals();

	/**
	 * Creates a new {@link MCPServerService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	public MCPServerService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Returns the singleton instance of the MCP server service.
	 *
	 * @return The service instance, or {@code null} if not yet started.
	 */
	public static MCPServerService getInstance() {
		return _instance;
	}

	/**
	 * Returns the HTTP transport provider for delegating servlet requests.
	 *
	 * @return The transport provider, or {@code null} if the service is not started.
	 */
	public HttpServletStreamableServerTransportProvider getTransportProvider() {
		return _transportProvider;
	}

	/**
	 * How a request to {@code /mcp/*} is authenticated.
	 *
	 * <p>
	 * Built once at start-up from {@link Config#getAuthentications()} and
	 * {@link Config#getSecrets()}. Refuses everything when the application configured no
	 * authentication - see {@link McpAuthentication}.
	 * </p>
	 *
	 * @return The authenticator, never <code>null</code> while the service is started.
	 */
	public Authenticator getAuthenticator() {
		return _authenticator;
	}

	/**
	 * Which account opened which MCP session.
	 *
	 * @see McpSessionPrincipals
	 */
	public McpSessionPrincipals getSessionPrincipals() {
		return _sessionPrincipals;
	}

	@Override
	protected void startUp() {
		super.startUp();

		try {
			// Register singleton instance
			_instance = this;

			Config<?> config = getConfig();

			_authenticator = McpAuthentication.build(config.getAuthentications(), config.getSecrets());

			// Create HTTP transport provider
			// Note: A statically registered servlet (MCPTransportServlet) delegates requests
			// to this transport provider, so no dynamic servlet registration is needed.
			_transportProvider = HttpServletStreamableServerTransportProvider.builder()
				.jsonMapper(McpJsonDefaults.getMapper())
				.mcpEndpoint(MCP_ENDPOINT)
				.keepAliveInterval(Duration.ofSeconds(config.getKeepAliveInterval()))
				.build();

			// Initialize the transport provider
			jakarta.servlet.ServletContext servletContext = ServletContextService.getInstance().getServletContext();
			try {
				_transportProvider.init(new ServletConfigAdapter(servletContext));
			} catch (ServletException ex) {
				throw new RuntimeException("Failed to initialize MCP transport provider: " + ex.getMessage(), ex);
			}

			// Create MCP server builder with sync API.
			//
			// immediateExecution(true) keeps every protocol handler on the servlet request thread
			// instead of offloading it to Schedulers.boundedElastic(). That is what lets
			// McpAuthenticationFilter establish the caller's TLContext for the whole request: the
			// transport blocks the request thread while the handler runs, so the handler sees the
			// context the filter opened. Without it the handlers would run on a pooled thread with
			// no interaction and no account, and every read would have to be refused.
			var builder = McpServer.sync(_transportProvider)
				.immediateExecution(true)
				.serverInfo(config.getServerName(), config.getServerVersion());

			// Configure the server with application-specific capabilities BEFORE building
			configureServer(builder);

			// Build the server
			_mcpServer = builder.build();

			reportEndpointExposure();

		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to start MCP server: " + ex.getMessage(), ex);
		}
	}

	/**
	 * States in the boot log what enabling this endpoint published.
	 *
	 * <p>
	 * A statement at every boot rather than a line in a document, because it is a property of the
	 * running deployment: what {@code /mcp/*} serves, and to whom, follows from the authentications
	 * this application configured, and reaching this method means an administrator switched the
	 * module on - it is {@code false} in this module's own configuration.
	 * </p>
	 */
	private void reportEndpointExposure() {
		int schemes = getConfig().getAuthentications().size();
		if (schemes == 0) {
			Logger.warn("MCP server started on " + MCP_ENDPOINT + "/* with NO authentication"
				+ " configured, so it refuses every request with 503. The endpoint serves nothing it"
				+ " cannot attribute to an account. Configure <authentications> and <secrets> on"
				+ " MCPServerService to make it usable - see doc/security/mcp-endpoint.md.",
				MCPServerService.class);
			return;
		}

		Logger.info("MCP server started on " + MCP_ENDPOINT + "/* with "
			+ getConfig().getAuthentications().keySet() + " as accepted authentication(s). Every"
			+ " request is authenticated and acts as the account it authenticated: resource reads are"
			+ " filtered by that account's model access rights, and tool calls are authorized against"
			+ " its roles, bounded by the <background-allowlist> because an MCP caller has no human to"
			+ " answer an approval prompt. tools/list still discloses every tool name, description and"
			+ " input schema to any authenticated caller. See doc/security/mcp-endpoint.md.",
			MCPServerService.class);
	}

	/**
	 * Configures the MCP server builder with application-specific tools, resources, and prompts.
	 *
	 * <p>
	 * Override or extend this method to register custom MCP capabilities for your application.
	 * </p>
	 *
	 * <p>
	 * Default implementation registers resources for TopLogic model exploration:
	 * </p>
	 * <ul>
	 * <li>{@link InstanceResource} - Retrieves detailed information about a specific instance by its TID</li>
	 * <li>{@link TypeInstancesResource} - Retrieves all instances of a specific type</li>
	 * <li>{@link ModuleTypesResource} - Lists types within a specific module (dynamic resource template)</li>
	 * <li>{@link TypePartsResource} - Lists parts within a specific type (dynamic resource template)</li>
	 * <li>{@link EnumerationClassifiersResource} - Lists classifiers within an enumeration (dynamic resource template)</li>
	 * <li>{@link TypeUsagesResource} - Finds usages of a specific type (dynamic resource template)</li>
	 * <li>{@link ModuleNameCompletion} - Provides completions for module name parameters</li>
	 * </ul>
	 *
	 * <p>
	 * All tools are provided by the {@link McpToolAdapter} which adapts tools from the
	 * {@link ToolRegistryService} to MCP tool specifications.
	 * </p>
	 *
	 * <p>
	 * Additionally registers any configured resource templates from the service configuration.
	 * </p>
	 *
	 * @param builder
	 *        The MCP server builder to configure.
	 */
	protected void configureServer(McpServer.SyncSpecification<?> builder) {
		builder.capabilities(ServerCapabilities.builder().resources(true, true).tools(true).build());

		// Register resource template for retrieving instance details by TID
		builder.resourceTemplates(InstanceResource.createSpecification());

		// Register resource template for retrieving all instances of a specific type
		builder.resourceTemplates(TypeInstancesResource.createSpecification());

		// Register resource template for listing types in a specific module
		builder.resourceTemplates(ModuleTypesResource.createSpecification());

		// Register resource template for listing parts in a specific type
		builder.resourceTemplates(TypePartsResource.createSpecification());

		// Register resource template for listing classifiers in an enumeration
		builder.resourceTemplates(EnumerationClassifiersResource.createSpecification());

		// Register resource template for finding usages of a specific type
		builder.resourceTemplates(TypeUsagesResource.createSpecification());

		// Register resource template for configuration interface JSON schemas
		builder.resourceTemplates(ConfigSchemaResource.createSpecification());

		// Register completion handler for module name parameter
		builder.completions(ModuleNameCompletion.createSpecification());

		// Register configured dynamic resources
		InstantiationContext context = new com.top_logic.basic.config.DefaultInstantiationContext(getClass());
		for (PolymorphicConfiguration<? extends DynamicResource> resourceConfig : getConfig().getDynamicResources()) {
			DynamicResource resource = context.getInstance(resourceConfig);
			builder.resourceTemplates(resource.createSpecification());
		}

		// Register tools from ToolRegistryService (if available)
		for (io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification adaptedTool :
				McpToolAdapter.adaptAllTools()) {
			builder.tools(adaptedTool);
		}
	}

	@Override
	protected void shutDown() {
		try {
			// Unregister singleton instance
			_instance = null;
			_authenticator = null;

			// The transport is destroyed below, so every MCP session it held is gone; keeping their
			// owners would leave a restarted service deciding about sessions that no longer exist.
			_sessionPrincipals.clear();

			// Clean up MCP server resources
			if (_mcpServer != null) {
				try {
					_mcpServer.close();
				} catch (Exception ex) {
					// Log but don't fail shutdown
					System.err.println("Error closing MCP server: " + ex.getMessage());
				}
				_mcpServer = null;
			}

			// Clean up transport provider
			if (_transportProvider != null) {
				try {
					_transportProvider.destroy();
				} catch (Exception ex) {
					// Log but don't fail shutdown
					System.err.println("Error destroying MCP transport provider: " + ex.getMessage());
				}
				_transportProvider = null;
			}
		} finally {
			super.shutDown();
		}
	}

	/**
	 * Simple ServletConfig adapter for initializing the MCP transport servlet.
	 */
	private static class ServletConfigAdapter implements ServletConfig {

		private ServletContext _servletContext;

		/**
		 * Creates a {@link ServletConfigAdapter}.
		 */
		public ServletConfigAdapter(ServletContext servletContext) {
			_servletContext = servletContext;
		}

		@Override
		public String getServletName() {
			return "MCPServerTransport";
		}

		@Override
		public jakarta.servlet.ServletContext getServletContext() {
			return _servletContext;
		}

		@Override
		public String getInitParameter(String name) {
			return null;
		}

		@Override
		public java.util.Enumeration<String> getInitParameterNames() {
			return java.util.Collections.emptyEnumeration();
		}
	}

	/**
	 * Module for {@link MCPServerService}.
	 */
	public static final class Module extends TypedRuntimeModule<MCPServerService> {

		/**
		 * Singleton {@link MCPServerService.Module} instance.
		 */
		public static final MCPServerService.Module INSTANCE = new MCPServerService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<MCPServerService> getImplementation() {
			return MCPServerService.class;
		}
	}
}
