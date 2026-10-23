/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tl.ai.tool.api.AICategory;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Service managing connections to external MCP servers.
 *
 * <p>
 * Configured via XML with named MCP server entries. Each server is connected at startup, and
 * its tools are discovered via the MCP {@code tools/list} call. Agents reference servers by name
 * via their {@code mcpServers} attribute; {@link com.top_logic.tl.ai.core.agent.AgentFactory}
 * pulls the server's tools into the agent's
 * {@link com.top_logic.tl.ai.core.tools.ToolCatalog}.
 * </p>
 *
 * @see McpTool
 * @see McpClientConnection
 */
@ServiceCategory.Of(AICategory.class)
public class McpServerRegistry extends ConfiguredManagedClass<McpServerRegistry.Config<?>> {

	/**
	 * Configuration for {@link McpServerRegistry}.
	 */
	public interface Config<I extends McpServerRegistry> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name for {@link #getMcpServers()}. */
		String MCP_SERVERS = "servers";

		/**
		 * Map of named MCP server configurations.
		 *
		 * @see McpServerConfig
		 */
		@Name(MCP_SERVERS)
		@Key(McpServerConfig.NAME_ATTRIBUTE)
		@EntryTag("server")
		Map<String, McpServerConfig> getMcpServers();
	}

	/**
	 * Configuration for a single MCP server.
	 */
	public interface McpServerConfig extends NamedConfigMandatory {

		/** Configuration property name for {@link #getTransport()}. */
		String TRANSPORT = "transport";

		/** Configuration property name for {@link #getToolTimeoutSeconds()}. */
		String TOOL_TIMEOUT_SECONDS = "tool-timeout-seconds";

		/**
		 * Transport configuration (stdio or HTTP).
		 *
		 * @see StdioMcpTransport
		 * @see HttpMcpTransport
		 */
		@Name(TRANSPORT)
		PolymorphicConfiguration<? extends McpTransportConfig> getTransport();

		/**
		 * Timeout in seconds for individual tool calls.
		 */
		@Name(TOOL_TIMEOUT_SECONDS)
		@IntDefault(60)
		int getToolTimeoutSeconds();
	}

	private final InstantiationContext _context;

	private final Map<String, McpClientConnection> _connections = new HashMap<>();

	/** Creates a {@link McpServerRegistry}. */
	public McpServerRegistry(InstantiationContext context, Config<?> config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		for (Map.Entry<String, McpServerConfig> entry : getConfig().getMcpServers().entrySet()) {
			String serverName = entry.getKey();
			McpServerConfig serverConfig = entry.getValue();

			try {
				McpTransportConfig transportConfig = _context.getInstance(serverConfig.getTransport());
				McpClientConnection connection =
					new McpClientConnection(serverName, transportConfig, serverConfig.getToolTimeoutSeconds());
				connection.connect();
				_connections.put(serverName, connection);
			} catch (Exception e) {
				Logger.error("Failed to connect to MCP server '" + serverName + "': "
					+ e.getMessage(), e, McpServerRegistry.class);
			}
		}
	}

	@Override
	protected void shutDown() {
		for (Map.Entry<String, McpClientConnection> entry : _connections.entrySet()) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				Logger.warn("Error closing MCP server '" + entry.getKey() + "': "
					+ e.getMessage(), McpServerRegistry.class);
			}
		}
		_connections.clear();

		super.shutDown();
	}

	/**
	 * Returns the names of all MCP servers that connected successfully.
	 */
	public Set<String> getServerNames() {
		return Collections.unmodifiableSet(_connections.keySet());
	}

	/**
	 * Returns the names of all MCP servers declared in configuration, whether connected or not.
	 */
	public Set<String> getConfiguredServerNames() {
		return Collections.unmodifiableSet(getConfig().getMcpServers().keySet());
	}

	/**
	 * Whether the named server currently has an open connection.
	 */
	public boolean isConnected(String serverName) {
		McpClientConnection connection = _connections.get(serverName);
		return connection != null && connection.isConnected();
	}

	/**
	 * Returns all tools from the given MCP server, wrapped as {@link McpTool} instances.
	 *
	 * @param serverName
	 *        The server name as configured.
	 * @return List of tools, or {@code null} if the server is not found or not connected.
	 */
	public List<Tool> getTools(String serverName) {
		McpClientConnection connection = _connections.get(serverName);
		if (connection == null || !connection.isConnected()) {
			return null;
		}

		List<McpSchema.Tool> mcpTools = connection.tools();
		List<Tool> tools = new ArrayList<>(mcpTools.size());

		for (McpSchema.Tool mcpTool : mcpTools) {
			tools.add(createTool(serverName, mcpTool, this));
		}

		return tools;
	}

	/**
	 * Wraps one MCP tool definition as an {@link McpTool}.
	 *
	 * <p>
	 * Nothing the server says about the tool beyond its name, description and input schema is read.
	 * The {@code annotations} of the definition - {@code readOnlyHint} and friends - are a server's
	 * claims about itself, and every tool reached over MCP is {@link ToolMode#EXTERNAL} whatever they
	 * say; see {@link McpTool#mode()}.
	 * </p>
	 *
	 * @param serverName
	 *        The server name as configured.
	 * @param mcpTool
	 *        The tool definition as the server declared it in its {@code tools/list} response.
	 * @param registry
	 *        The registry the tool delegates its calls to, or <code>null</code> for testing.
	 */
	static McpTool createTool(String serverName, McpSchema.Tool mcpTool, McpServerRegistry registry) {
		String inputSchemaJson = serializeInputSchema(mcpTool.inputSchema());
		String description = mcpTool.description() != null ? mcpTool.description() : "";

		return new McpTool(serverName, mcpTool.name(), description, inputSchemaJson, registry);
	}

	/**
	 * Calls a tool on the given MCP server.
	 *
	 * @param serverName
	 *        The server name.
	 * @param toolName
	 *        The original MCP tool name.
	 * @param arguments
	 *        Tool arguments.
	 * @return The call result.
	 * @throws IllegalArgumentException
	 *         if the server is not found or not connected
	 */
	public McpSchema.CallToolResult callTool(String serverName, String toolName,
			Map<String, Object> arguments) {
		McpClientConnection connection = _connections.get(serverName);
		if (connection == null || !connection.isConnected()) {
			throw new IllegalArgumentException(
				"MCP server '" + serverName + "' is not connected. Available: " + _connections.keySet());
		}
		return connection.callTool(toolName, arguments);
	}

	/**
	 * Serializes an MCP {@link io.modelcontextprotocol.spec.McpSchema.JsonSchema} to a JSON string.
	 */
	static String serializeInputSchema(McpSchema.JsonSchema schema) {
		if (schema == null) {
			return "{\"type\":\"object\",\"properties\":{}}";
		}

		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("type").value(schema.type() != null ? schema.type() : "object");

				json.name("properties");
				Map<String, Object> properties = schema.properties();
				if (properties != null) {
					writeJsonValue(json, properties);
				} else {
					json.beginObject();
					json.endObject();
				}

				List<String> required = schema.required();
				if (required != null && !required.isEmpty()) {
					json.name("required");
					json.beginArray();
					for (String name : required) {
						json.value(name);
					}
					json.endArray();
				}

				if (Boolean.FALSE.equals(schema.additionalProperties())) {
					json.name("additionalProperties").value(false);
				}

				json.endObject();
			}
			return buffer.toString();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to serialize MCP input schema", ex);
		}
	}

	private static void writeJsonValue(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof String s) {
			json.value(s);
		} else if (value instanceof Number n) {
			json.value(n);
		} else if (value instanceof Boolean b) {
			json.value(b.booleanValue());
		} else if (value instanceof Map<?, ?> map) {
			json.beginObject();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				json.name(entry.getKey().toString());
				writeJsonValue(json, entry.getValue());
			}
			json.endObject();
		} else if (value instanceof List<?> list) {
			json.beginArray();
			for (Object element : list) {
				writeJsonValue(json, element);
			}
			json.endArray();
		} else {
			json.value(value.toString());
		}
	}

	/**
	 * Returns the singleton instance.
	 */
	public static McpServerRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link McpServerRegistry}.
	 */
	public static final class Module extends TypedRuntimeModule<McpServerRegistry> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<McpServerRegistry> getImplementation() {
			return McpServerRegistry.class;
		}
	}
}
