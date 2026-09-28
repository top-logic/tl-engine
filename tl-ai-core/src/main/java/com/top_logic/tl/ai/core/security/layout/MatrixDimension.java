/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;

/**
 * What the columns of one matrix of the policy editor are.
 */
/*
 * Issue #404. One enum for the four sub-tabs rather than RoleAccessPolicy.Dimension plus a flag,
 * because the capability matrix is not a fourth run section: it has no section default, no row that
 * is locked, and two rows - the basic permissions and the limit of unattended runs - that no role
 * owns. What the four share is the grid of roles against names, and that is what this names.
 */
public enum MatrixDimension {

	/** Which agents a role may run. */
	AGENTS(Dimension.AGENT),

	/** Which language models a role may choose. */
	MODELS(Dimension.MODEL),

	/** Which MCP servers a role may attach. */
	MCP_SERVERS(Dimension.MCP_SERVER),

	/** Which capability groups a role may use. */
	CAPABILITIES(null);

	private final Dimension _runDimension;

	private MatrixDimension(Dimension runDimension) {
		_runDimension = runDimension;
	}

	/**
	 * The run section this matrix edits, or <code>null</code> for {@link #CAPABILITIES}.
	 */
	public Dimension runDimension() {
		return _runDimension;
	}

	/** Whether this matrix edits one of the three run sections, which lock a role they do not name. */
	public boolean isRunSection() {
		return _runDimension != null;
	}

	/** The matrix of the given run section. */
	public static MatrixDimension of(Dimension dimension) {
		switch (dimension) {
			case MODEL:
				return MODELS;
			case MCP_SERVER:
				return MCP_SERVERS;
			default:
				return AGENTS;
		}
	}

}
