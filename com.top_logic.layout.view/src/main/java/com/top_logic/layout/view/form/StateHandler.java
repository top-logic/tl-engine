/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

/**
 * Abstraction for a component that holds state that can be dirty (modified but not yet persisted).
 *
 * <p>
 * Provides the operations needed by the dirty-check confirmation dialog: querying dirty/error
 * status, saving, and discarding changes.
 * </p>
 *
 * @see com.top_logic.layout.view.channel.DirtyChannel
 * @see com.top_logic.layout.view.channel.ChannelVetoException
 */
public interface StateHandler {

	/**
	 * Whether this handler has unsaved changes.
	 */
	boolean isDirty();

	/**
	 * Whether this handler currently has validation errors that would prevent saving.
	 */
	boolean hasErrors();

	/**
	 * Persists the current changes.
	 *
	 * <p>
	 * Must only be called when {@link #hasErrors()} returns {@code false}.
	 * </p>
	 */
	void executeSave();

	/**
	 * Discards all unsaved changes, reverting to the last persisted state.
	 */
	void executeDiscard();

	/**
	 * A human-readable description of this handler for display in the confirmation dialog.
	 *
	 * <p>
	 * Typically the form title or the name of the edited object.
	 * </p>
	 */
	String getDescription();
}
