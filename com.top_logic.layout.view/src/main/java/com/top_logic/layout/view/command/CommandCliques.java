/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.control.button.ButtonTone;

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

	/** Clique for the commands adding a hidden tile to a dashboard again. */
	public static final String DASHBOARD_TILES = "dashboardTiles";

	/**
	 * The tone the given clique gives a command placed in it.
	 *
	 * <p>
	 * A command in {@link #DELETE} is destructive, even when none of its actions is: its button is
	 * drawn destructive (see {@link ViewCommandModel#getTone()}), and so is the affirmative answer of
	 * a confirmation in its chain (see {@link Continuation#tone()}).
	 * </p>
	 *
	 * @param clique
	 *        The {@link ViewCommand.Config#getClique() clique} of a command, {@code null} for none.
	 * @return {@link ButtonTone#DANGER} for {@link #DELETE}, {@link ButtonTone#DEFAULT} otherwise.
	 */
	public static ButtonTone tone(String clique) {
		return DELETE.equals(clique) ? ButtonTone.DANGER : ButtonTone.DEFAULT;
	}

	private CommandCliques() {
		// No instantiation.
	}
}
