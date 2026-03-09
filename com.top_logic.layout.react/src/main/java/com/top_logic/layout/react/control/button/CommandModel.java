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
 * single model may be shared between multiple buttons (e.g. a toolbar button and an inline button
 * for the same command). The model creator is responsible for lifecycle.
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
