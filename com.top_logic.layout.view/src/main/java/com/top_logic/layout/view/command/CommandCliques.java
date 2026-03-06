/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

/**
 * Well-known clique names for grouping related {@link ViewCommand}s in the UI.
 *
 * <p>
 * Commands with the same clique are rendered together (e.g. in the same toolbar group or context
 * menu section).
 * </p>
 */
public final class CommandCliques {

	/** Clique for commands that create new objects. */
	public static final String CREATE = "create";

	/** Clique for commands that edit existing objects. */
	public static final String EDIT = "edit";

	/** Clique for commands that delete objects. */
	public static final String DELETE = "delete";

	/** Clique for commands that commit or save changes. */
	public static final String COMMIT = "commit";

	/** Clique for navigation commands. */
	public static final String NAVIGATE = "navigate";

	/** Clique for commands that open or display objects. */
	public static final String VIEW = "view";

	/** Clique for export commands. */
	public static final String EXPORT = "export";

	/** Clique for additional commands in an overflow menu. */
	public static final String MORE = "more";

	private CommandCliques() {
		// No instantiation.
	}
}
