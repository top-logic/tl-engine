/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;

/**
 * The built-in tool groups and their privilege classification.
 *
 * <p>
 * A tool group is the unit an administrator grants: with more than thirty tools in a typical
 * registry, per-tool administration does not scale. Every tool belongs to exactly one primary
 * group, declared on the tool itself ({@code @ToolMethod(group = ...)}) so that a newly added tool
 * cannot end up silently ungrouped - an ungrouped tool is denied to every role.
 * </p>
 *
 * <p>
 * The group names are open strings, not an enum: applications and MCP servers contribute tools that
 * need their own groups. The constants here name the groups the framework itself ships.
 * </p>
 *
 * <p>
 * These constants live in the tool contract module rather than next to the policy that evaluates
 * them, so that a tool defined outside {@code tl-ai-core} can declare its group without depending on
 * the engine that enforces it - which is the whole point of this module.
 * </p>
 *
 * @see ToolMethod#group()
 * @see Tool#group()
 */
public final class ToolGroups {

	/** Read-only inspection of the application model, layouts and configuration schema. */
	public static final String MODEL_READ = "model.read";

	/** Read-only access to business data (instance browsing, search, semantic search). */
	public static final String DATA_READ = "data.read";

	/** Modification of business data. */
	public static final String DATA_WRITE = "data.write";

	/** TL-Script execution and its reference tools. Privileged: expresses any data operation. */
	public static final String SCRIPT = "script";

	/**
	 * Modification of the application model, annotations and layouts. Privileged: changes the shape
	 * the security policy is written against.
	 */
	public static final String MODEL_STRUCTURE = "model.structure";

	/**
	 * Creation and rewiring of agents, skills and script tools. Privileged: an agent that may edit
	 * agents can grant itself any tool.
	 */
	public static final String AGENT_AUTHORING = "agent.authoring";

	/** Read-only inspection of agents, skills and the tool registry. */
	public static final String AGENT_META = "agent.meta";

	/** Conversation primitives: asking the user, loading skills, delegating to sub-agents. */
	public static final String INTERACTION = "interaction";

	/** User-scoped workspace tools: tasks, published pages. */
	public static final String WORKSPACE = "workspace";

	/**
	 * Tools that send data outside the application: web fetch/search, issue trackers, remote MCP
	 * servers.
	 *
	 * <p>
	 * Egress is a group rather than a {@code ToolMode} because the mode describes what state a tool
	 * changes <em>locally</em> and says nothing about where data goes. {@code webFetch} is a READ
	 * tool and the best exfiltration primitive in the catalog.
	 * </p>
	 */
	public static final String EGRESS = "egress";

	/**
	 * Groups that a wildcard grant never reaches and that an administrator must name explicitly.
	 *
	 * <p>
	 * Each of these is a general-purpose escalation primitive, so granting one is a deliberate
	 * decision that is itself audited.
	 * </p>
	 */
	public static final Set<String> PRIVILEGED = Set.of(SCRIPT, MODEL_STRUCTURE, AGENT_AUTHORING);

	/**
	 * Every group the framework itself ships.
	 *
	 * <p>
	 * Not the set of all legal group names - a group is any string a tool declares or a policy defines
	 * - but the set an administrator can rely on being present in every application.
	 * </p>
	 */
	public static final Set<String> ALL = Set.of(MODEL_READ, DATA_READ, DATA_WRITE, SCRIPT,
		MODEL_STRUCTURE, AGENT_AUTHORING, AGENT_META, INTERACTION, WORKSPACE, EGRESS);

	/**
	 * The resource carrying the display name and the explanation of every {@link #ALL framework
	 * group}.
	 *
	 * @see #labelKey(String)
	 */
	private static final Map<String, ResKey> LABELS = Map.of(
		MODEL_READ, I18NConstants.GROUP_MODEL_READ,
		DATA_READ, I18NConstants.GROUP_DATA_READ,
		DATA_WRITE, I18NConstants.GROUP_DATA_WRITE,
		SCRIPT, I18NConstants.GROUP_SCRIPT,
		MODEL_STRUCTURE, I18NConstants.GROUP_MODEL_STRUCTURE,
		AGENT_AUTHORING, I18NConstants.GROUP_AGENT_AUTHORING,
		AGENT_META, I18NConstants.GROUP_AGENT_META,
		INTERACTION, I18NConstants.GROUP_INTERACTION,
		WORKSPACE, I18NConstants.GROUP_WORKSPACE,
		EGRESS, I18NConstants.GROUP_EGRESS);

	private ToolGroups() {
		// Constant holder.
	}

	/**
	 * Whether the named group is privileged.
	 *
	 * @param group
	 *        The group name, may be <code>null</code>.
	 * @return Whether a wildcard grant must not reach this group.
	 */
	public static boolean isPrivileged(String group) {
		return group != null && PRIVILEGED.contains(group);
	}

	/**
	 * The resource describing the named group: its display name, and its explanation under
	 * {@link ResKey#tooltip()}.
	 *
	 * <p>
	 * Only the framework's own groups have one. A group an application, an MCP server or a
	 * {@code <tool-groups>} entry contributes is an arbitrary string with nobody to write a sentence
	 * about it, and is shown under its technical name.
	 * </p>
	 *
	 * @param group
	 *        The group name, may be <code>null</code>.
	 * @return The resource, or <code>null</code> for a group this class does not ship.
	 */
	public static ResKey labelKey(String group) {
		return group == null ? null : LABELS.get(group);
	}
}
