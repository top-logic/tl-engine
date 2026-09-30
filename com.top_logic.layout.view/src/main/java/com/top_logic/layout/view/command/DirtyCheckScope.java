/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

/**
 * The forms a {@link ViewCommand} asks about before it runs.
 *
 * <p>
 * When a form in the checked scope holds unsaved changes, the command does not run right away.
 * Instead, the user is asked whether to save the changes, discard them, or cancel. After saving or
 * discarding, the command runs; after cancelling, it does not.
 * </p>
 */
public enum DirtyCheckScope {

	/**
	 * Asks about the unsaved changes in the scope the command is displayed in: the tab, the sidebar
	 * item, the frame of a tile stack or the dialog holding the command, including all scopes nested
	 * within it.
	 */
	SELF,

	/**
	 * Asks about the unsaved changes anywhere in the browser window the command is displayed in.
	 */
	VIEW,

	/**
	 * Runs the command without asking, whatever changes are left unsaved.
	 */
	NONE;

	/**
	 * The configuration value of {@link #VIEW}.
	 */
	public static final String VIEW_NAME = "VIEW";

	/**
	 * The configuration value of {@link #NONE}.
	 */
	public static final String NONE_NAME = "NONE";

}
