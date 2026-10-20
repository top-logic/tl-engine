/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;

/**
 * {@link NameRef} subtype providing available MCP server names as options.
 *
 * @see TopLogicAgentDefinitions.AgentConfig#getMcpServers()
 */
public interface McpServerRef extends NameRef {

	@Override
	@Options(fun = AllMcpServerNameOptions.class)
	String getName();

	/**
	 * Provides all configured MCP server names for selection in the service editor.
	 */
	class AllMcpServerNameOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!McpServerRegistry.Module.INSTANCE.isActive()) {
				return Collections.emptyList();
			}
			return new ArrayList<>(McpServerRegistry.getInstance().getServerNames());
		}
	}
}
