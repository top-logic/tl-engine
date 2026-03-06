/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;

/**
 * Strategy for producing the confirmation message shown before a {@link ViewCommand} is executed.
 */
public interface ViewCommandConfirmation {

	/**
	 * Configuration for {@link ViewCommandConfirmation}.
	 */
	interface Config extends PolymorphicConfiguration<ViewCommandConfirmation> {
		// Marker interface.
	}

	/**
	 * Produces the confirmation message for the given command execution.
	 *
	 * @param commandLabel
	 *        The resolved label of the command (may be {@code null} if the command has no label).
	 * @param input
	 *        The current input value (may be {@code null}).
	 * @return The confirmation message to display, or {@code null} to skip confirmation.
	 */
	ResKey getConfirmation(ResKey commandLabel, Object input);
}
