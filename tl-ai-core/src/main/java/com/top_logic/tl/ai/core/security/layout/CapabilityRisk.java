/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.Set;

import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * What makes a capability group worth looking at twice.
 */
/*
 * Three markers, because three different things about a group make an administrator stop: it can express
 * every other capability, it can send data out of the application, or every account holds it whether the
 * policy says so or not. NONE rather than a null risk, so that a cell renders a value and never an
 * accidental "null".
 */
public enum CapabilityRisk {

	/** Its tools can express any other capability, so granting it grants more than it names. */
	PRIVILEGED,

	/** Its tools send data out of the application. */
	EGRESS,

	/** Every account holds it, configured or not. */
	IMPLICIT,

	/** Nothing beyond what the group itself says. */
	NONE;

	/**
	 * The groups whose documentation in {@link ToolGroups} says "grant with care", plus the one that can
	 * grant every other.
	 *
	 * @see ToolGroups#SCRIPT
	 * @see ToolGroups#MODEL_STRUCTURE
	 * @see ToolGroups#AGENT_AUTHORING
	 */
	private static final Set<String> PRIVILEGED_GROUPS = Set.of(
		ToolGroups.SCRIPT, ToolGroups.MODEL_STRUCTURE, ToolGroups.AGENT_AUTHORING);

	/**
	 * The marker of the named capability group.
	 *
	 * @param group
	 *        The technical group name, may be <code>null</code>.
	 * @return The marker, never <code>null</code>; {@link #NONE} for a group that carries none and for
	 *         no group at all.
	 */
	public static CapabilityRisk of(String group) {
		if (group == null) {
			return NONE;
		}
		if (RoleToolAccess.IMPLICIT_BASELINE.contains(group)) {
			return IMPLICIT;
		}
		if (ToolGroups.EGRESS.equals(group)) {
			return EGRESS;
		}
		if (PRIVILEGED_GROUPS.contains(group)) {
			return PRIVILEGED;
		}
		return NONE;
	}

}
