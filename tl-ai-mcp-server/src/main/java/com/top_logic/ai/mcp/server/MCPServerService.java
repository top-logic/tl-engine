/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.time.Duration;
import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import com.top_logic.ai.mcp.server.tools.LangChain4jToolAdapter;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
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

import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
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
 * TopLogic application.
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
	ToolRegistry.Module.class
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
		Config.DYNAMIC_RESOURCES,
	})
	@DisplayInherited(DisplayStrategy.PREPEND)
	public interface Config<I extends MCPServerService> extends ConfiguredManagedClass.Config<I> {

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
	}

	private static volatile MCPServerService _instance;

	private McpSyncServer _mcpServer;

	private HttpServletStreamableServerTransportProvider _transportProvider;

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

	@Override
	protected void startUp() {
		super.startUp();

		try {
			// Register singleton instance
			_instance = this;

			Config<?> config = getConfig();

			// Create HTTP transport provider
			// Note: A statically registered servlet (MCPTransportServlet) delegates requests
			// to this transport provider, so no dynamic servlet registration is needed.
			_transportProvider = HttpServletStreamableServerTransportProvider.builder()
				.jsonMapper(new JacksonMcpJsonMapper(new ObjectMapper()))
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

			// Create MCP server builder with sync API
			var builder = McpServer.sync(_transportProvider)
				.serverInfo(config.getServerName(), config.getServerVersion())
				.jsonSchemaValidator(new NoOpJsonSchemaValidator());

			// Configure the server with application-specific capabilities BEFORE building
			configureServer(builder);

			// Build the server
			_mcpServer = builder.build();

		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to start MCP server: " + ex.getMessage(), ex);
		}
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
	 * All tools are provided by the {@link LangChain4jToolAdapter} which adapts tools from the
	 * {@link ToolRegistry} to MCP tool specifications.
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

		// Register langchain4j tools from ToolRegistry (if available)
		for (io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification adaptedTool :
				LangChain4jToolAdapter.adaptAllTools()) {
			builder.tools(adaptedTool);
		}
	}

	@Override
	protected void shutDown() {
		try {
			// Unregister singleton instance
			_instance = null;

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
	 * Returns the MCP server instance.
	 *
	 * @return The MCP server, or {@code null} if not started.
	 */
	public McpSyncServer getMCPServer() {
		return _mcpServer;
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
