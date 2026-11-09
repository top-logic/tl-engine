/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

/**
 * Whether a capability or a tool may be used.
 */
/*
 * A type rather than the boolean it is computed from, so that no cell of the view can assemble its own
 * word for a decision: the verdict arrives as a value and ToolAccessLabels is the only place that turns
 * it into text. The boolean stays where the decision is made - see ToolAccessExplanation.
 */
public enum ToolAccessVerdict {

	/** May be used. */
	ALLOWED,

	/** May not be used. */
	DENIED;

	/**
	 * The verdict of a decision.
	 *
	 * @param allowed
	 *        Whether the decision allowed what was asked about.
	 * @return The matching verdict, never <code>null</code>.
	 */
	public static ToolAccessVerdict of(boolean allowed) {
		return allowed ? ALLOWED : DENIED;
	}

}
