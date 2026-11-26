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
	 * The tone a command takes from its configuration.
	 *
	 * <p>
	 * The {@link ViewCommand.Config#getTone() configured tone} always applies, whatever the clique
	 * of the command and whatever its actions do. Without one, a command in {@link #DELETE} is
	 * destructive, even when none of its actions is. In both cases the tone is the tone of the
	 * command's button (see {@link ViewCommandModel#getTone()}) and of the affirmative answer of
	 * every confirmation in its chain (see {@link Continuation#tone()}).
	 * </p>
	 *
	 * @param config
	 *        The configuration of a command.
	 * @return The configured tone, {@link ButtonTone#DANGER} for a command in {@link #DELETE}
	 *         without one, {@code null} for a command whose tone follows what its actions do (see
	 *         {@link ViewCommand#getTone()}).
	 */
	public static ButtonTone tone(ViewCommand.Config config) {
		ButtonTone configured = config.getTone();
		if (configured != null) {
			return configured;
		}
		return DELETE.equals(config.getClique()) ? ButtonTone.DANGER : null;
	}

	private CommandCliques() {
		// No instantiation.
	}
}
