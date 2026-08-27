/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Set;

import com.top_logic.tl.ai.core.security.RoleToolAccess;

/**
 * One row of the Werkzeug-Katalog: a registered tool joined with what the policy says about it.
 *
 * <p>
 * A snapshot, built when the table is filled and never updated: the registry can grow while the
 * application runs - a script tool created by an agent, a reconnected MCP server - and a row that
 * silently re-decided itself would make the table disagree with the sentence next to it. Reloading
 * the view rebuilds every row.
 * </p>
 *
 * @param qualifiedName
 *        The name an administrator writes in the policy, {@code {factory}.{tool}}. Never
 *        <code>null</code>; the row's identity.
 * @param description
 *        The tool's own description - the sentence the language model is given - or the empty string
 *        for a tool that describes itself nowhere.
 * @param group
 *        The capability group the policy classifies the tool into, or <code>null</code> for a tool
 *        that belongs to none and is therefore denied to everyone.
 * @param groupSource
 *        Where {@link #group()} comes from. Never <code>null</code>.
 * @param reachingRoles
 *        The roles the policy configures that reach this tool, in a stable order. Empty when only the
 *        baseline reaches it, and empty when nothing does - {@link #baselineReaches()} tells the two
 *        apart. Administrators are deliberately absent; one footnote says so once.
 * @param baselineReaches
 *        Whether an account holding no role at all reaches the tool.
 */
public record ToolCatalogEntry(String qualifiedName, String description, String group,
		RoleToolAccess.GroupSource groupSource, Set<String> reachingRoles, boolean baselineReaches) {

	/** Whether the tool belongs to no capability group, which is the state worth a warning. */
	public boolean isUnclassified() {
		return group() == null;
	}

	/** Whether no role and no baseline reaches the tool. */
	public boolean isUnreachable() {
		return !baselineReaches() && reachingRoles().isEmpty();
	}

	/** Who reaches the tool, as the one of three states the catalog reports. */
	public ToolReachability reachability() {
		if (baselineReaches()) {
			return ToolReachability.EVERYONE;
		}
		return reachingRoles().isEmpty() ? ToolReachability.NOBODY : ToolReachability.NAMED_ROLES;
	}

	/**
	 * The first sentence of {@link #description()}, for a table cell.
	 *
	 * <p>
	 * A tool description is written for a language model: several sentences of API mechanics, in
	 * English. The first sentence is the one that says what the tool is; the rest is how to call it and
	 * belongs in the tooltip.
	 * </p>
	 */
	public String descriptionSentence() {
		String text = description().strip();
		if (text.isEmpty()) {
			return "";
		}
		int end = text.indexOf(". ");
		if (end < 0) {
			// A single sentence, whether or not it ends in a full stop.
			return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
		}
		return text.substring(0, end);
	}

}
