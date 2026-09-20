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

	/**
	 * How many of the schemas {@link #toolSchemas(Predicate)} returns are fixed for this agent's
	 * lifetime, counted from the front.
	 *
	 * <p>
	 * The prompt-cache boundary of the tool array (Gitea #346). Everything up to this index is sent
	 * byte-identical on every request of the conversation; everything behind it is a loaded skill's
	 * tools and can appear, disappear or move. A provider that places explicit cache breakpoints
	 * puts one here, so that loading a skill costs the tokens behind the boundary rather than all of
	 * them. A registry that cannot grow answers with its whole size.
	 * </p>
	 *
	 * @param filter
	 *        The same filter {@link #toolSchemas(Predicate)} is called with; the boundary is an
	 *        index into <em>that</em> list, so it has to be counted the same way.
	 */
	default int stableToolCount(Predicate<Tool> filter) {
		return toolSchemas(filter).size();
	}

	/**
	 * The skills whose tools this registry currently advertises, in the order they were loaded.
	 *
	 * <p>
	 * Only a {@link DynamicToolCatalog} has any, and only for the run that loaded them.
	 * </p>
	 */
	default List<String> loadedSkills() {
		return List.of();
	}

	/**
	 * Tells the registry that the conversation it serves was compacted.
	 *
	 * <p>
	 * The tool array does not change - a capability may not disappear for a reason the model cannot
	 * see. What can have disappeared is text: a loaded skill's instructions live in the conversation
	 * and a summarizing compaction may have replaced them. A registry that handed out such text
	 * takes note here, so that asking for it again works.
	 * </p>
	 */
	default void historyCompacted() {
		// Nothing to forget in a registry that cannot grow.
	}
}
