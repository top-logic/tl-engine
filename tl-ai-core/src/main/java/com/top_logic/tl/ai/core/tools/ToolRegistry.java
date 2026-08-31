/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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

	/**
	 * Whether the named tool would arrive by loading one of the skills this registry's agent offers.
	 *
	 * <p>
	 * Only a {@link DynamicToolCatalog} knows this, and only because {@code AgentFactory} tells it.
	 * It is what separates "load the skill that has it" from "this agent does not carry it at all",
	 * two absences that {@link #getTool(String)} answers identically.
	 * </p>
	 *
	 * @param name
	 *        The tool name asked for.
	 * @return Whether some skill of this agent declares it.
	 */
	default boolean isReachableBySkill(String name) {
		return false;
	}
}
