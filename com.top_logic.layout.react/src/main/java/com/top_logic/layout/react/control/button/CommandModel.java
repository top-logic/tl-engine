/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Model for a command button providing label, executability, and execution.
 *
 * <p>
 * The {@link ReactButtonControl} uses this interface to read button state (label, disabled) and
 * listen for changes. The button registers a state change listener at construction and removes it
 * during cleanup.
 * </p>
 *
 * <p>
 * Lifecycle management ({@code attach}/{@code detach}) is NOT part of this interface because a
 * single model may be shared between multiple buttons. This happens when a child element contributes
 * an implicit command to a panel's {@code CommandScope}: the child creates a button for the command,
 * and the panel adds a second toolbar button for the same model. If the toolbar button is later
 * removed (because the command leaves the scope), it must not detach the model that the child's
 * button still uses. The model creator is responsible for lifecycle via
 * {@code ReactControl.addCleanupAction()}.
 * </p>
 */
public interface CommandModel {

	/**
	 * The resolved button label text.
	 */
	String getLabel();

	/**
	 * Whether the command is currently executable.
	 */
	boolean isExecutable();

	/**
	 * Executes the command.
	 *
	 * @param context
	 *        The view display context.
	 * @return The result of the command execution.
	 */
	HandlerResult executeCommand(ReactContext context);

	/**
	 * Registers a listener that is notified when any visible state (label, executability) changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addStateChangeListener(Runnable listener);

	/**
	 * Removes a previously registered state change listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeStateChangeListener(Runnable listener);
}
