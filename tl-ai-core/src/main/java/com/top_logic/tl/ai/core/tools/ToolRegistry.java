/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.List;
import java.util.function.Predicate;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Read-side contract for tool lookup, schema production, and execution.
 *
 * <p>Both {@link ToolCatalog} (static, frozen) and {@link DynamicToolCatalog}
 * (layered, growable) implement this interface so {@link com.top_logic.tl.ai.core.agent.AgentLoop}
 * works against either.</p>
 */
public interface ToolRegistry extends ToolExecutor {

	/**
	 * Looks up a tool by name.
	 *
	 * @return the tool, or {@code null} if not registered
	 */
	Tool getTool(String name);

	/**
	 * Returns schemas of all registered tools matching the given filter.
	 */
	List<ToolSchema> toolSchemas(Predicate<Tool> filter);
}
