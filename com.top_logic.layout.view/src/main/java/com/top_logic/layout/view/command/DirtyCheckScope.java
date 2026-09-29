/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

/**
 * Scope of the dirty check performed before executing a {@link ViewCommand}.
 */
public enum DirtyCheckScope {

	/**
	 * Check only the element that owns this command for unsaved changes.
	 */
	SELF,

	/**
	 * Check the entire view for unsaved changes.
	 */
	VIEW,

	/**
	 * Skip dirty checking entirely.
	 */
	NONE;
}
