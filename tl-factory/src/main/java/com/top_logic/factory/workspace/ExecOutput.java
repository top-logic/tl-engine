/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

/**
 * Receiver of the output of a command run by
 * {@link WorkspaceProvider#exec(String, ExecCommand, ExecOutput)}.
 *
 * <p>
 * The output arrives in chunks as the command produces it, not split at line ends. A chunk never
 * ends in the middle of a character. The methods are called from the thread that streams the
 * output, one call at a time.
 * </p>
 */
public interface ExecOutput {

	/**
	 * An {@link ExecOutput} that drops all output.
	 */
	ExecOutput NONE = new ExecOutput() {
		// Drops everything.
	};

	/**
	 * Called with a chunk of the standard output of the command.
	 */
	default void stdout(String text) {
		// Ignored by default.
	}

	/**
	 * Called with a chunk of the error output of the command.
	 */
	default void stderr(String text) {
		// Ignored by default.
	}

}
