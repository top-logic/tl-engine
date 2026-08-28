/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.tl.ai.core.security.ToolAccessExplanation;

/**
 * One row of the Effektive-Rechte table.
 *
 * <p>
 * Two levels, because that is how a policy is written and read: an administrator grants a capability
 * group, and the tools are what the group turns out to contain. The unclassified tools are the
 * exception that proves the rule - they have no group to sit under, so they are rows of their own.
 * </p>
 */
public sealed interface EffectiveAccessRow {

	/** The decision this row reports. */
	ToolAccessExplanation explanation();

	/**
	 * A capability group, with the decision for the group as a whole.
	 *
	 * @param group
	 *        The technical group name.
	 * @param explanation
	 *        The decision for the group.
	 * @param toolCount
	 *        How many registered tools the group holds. Zero for a group the policy knows and no tool
	 *        belongs to - which is worth seeing, because a grant of it reaches nothing.
	 * @param scriptCommitNote
	 *        The extra sentence the {@code script} group needs, or <code>null</code> for every other
	 *        group. A capability that is consumed by a tool <em>argument</em> does not fit the row
	 *        model and has to be said in words.
	 */
	record Capability(String group, ToolAccessExplanation explanation, int toolCount,
			String scriptCommitNote) implements EffectiveAccessRow {
		// Intentionally empty.
	}

	/**
	 * One tool, under its capability group or on its own if it has none.
	 *
	 * @param qualifiedName
	 *        The name the policy knows the tool by.
	 * @param explanation
	 *        The decision for this tool, which can differ from its group's: a per-tool grant or a
	 *        per-tool deny reaches exactly one row.
	 */
	record ToolRow(String qualifiedName, ToolAccessExplanation explanation) implements EffectiveAccessRow {
		// Intentionally empty.
	}

}
