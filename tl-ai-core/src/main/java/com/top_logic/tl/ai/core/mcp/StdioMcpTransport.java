/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpClientTransport;

/**
 * {@link McpTransportConfig} creating an MCP stdio transport (subprocess).
 *
 * <p>
 * The MCP server is started as a child process. Communication happens via stdin/stdout using
 * JSON-RPC. Environment variables can be used to pass API keys or other secrets.
 * </p>
 */
@InApp
public class StdioMcpTransport implements McpTransportConfig {

	/**
	 * Configuration for {@link StdioMcpTransport}.
	 */
	public interface Config extends PolymorphicConfiguration<StdioMcpTransport> {

		/** Configuration property name for {@link #getCommand()}. */
		String COMMAND = "command";

		/** Configuration property name for {@link #getArgs()}. */
		String ARGS = "args";

		/** Configuration property name for {@link #getEnv()}. */
		String ENV = "env";

		/**
		 * The command to execute (e.g., {@code "npx"} or {@code "python"}).
		 */
		@Name(COMMAND)
		String getCommand();

		/**
		 * Command-line arguments passed to the command.
		 *
		 * <p>
		 * In XML config, provide as comma-separated values:
		 * {@code args="-y,@modelcontextprotocol/server-github"}
		 * </p>
		 */
		@Name(ARGS)
		@Format(CommaSeparatedStrings.class)
		List<String> getArgs();

		/**
		 * Environment variables for the subprocess.
		 *
		 * <p>
		 * Use this to pass API keys without putting them in config:
		 * {@code <entry name="GITHUB_TOKEN" value="${env:GITHUB_TOKEN}"/>}
		 * </p>
		 */
		@Name(ENV)
		@Key(NamedValueConfig.NAME_ATTRIBUTE)
		@EntryTag("entry")
		Map<String, NamedValueConfig> getEnv();

	}

	private final String _command;

	private final List<String> _args;

	private final Map<String, String> _env;

	/**
	 * Creates a new {@link StdioMcpTransport} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public StdioMcpTransport(InstantiationContext context, Config config) {
		_command = config.getCommand();
		_args = config.getArgs();

		Map<String, NamedValueConfig> envConfig = config.getEnv();
		if (envConfig != null && !envConfig.isEmpty()) {
			Map<String, String> envMap = new HashMap<>();
			for (NamedValueConfig entry : envConfig.values()) {
				envMap.put(entry.getName(), entry.getValue());
			}
			_env = envMap;
		} else {
			_env = null;
		}
	}

	@Override
	public McpClientTransport createTransport() {
		ServerParameters.Builder builder = ServerParameters.builder(_command);

		if (_args != null && !_args.isEmpty()) {
			builder.args(_args);
		}

		if (_env != null) {
			builder.env(_env);
		}

		return new StdioClientTransport(builder.build(), McpJsonDefaults.getMapper());
	}

}
