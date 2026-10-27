/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.util.Objects;

/**
 * The end of the console output of a build, as {@link CIProvider#console(String, int, int) read}
 * from its {@link CIProvider}.
 *
 * @param text
 *        The output, plain text, starting with a complete line.
 * @param truncated
 *        Whether the output has more than {@link #text()}: earlier lines were dropped to keep
 *        within the requested size.
 */
public record CIConsole(String text, boolean truncated) {

	/**
	 * Creates a {@link CIConsole}.
	 */
	public CIConsole {
		Objects.requireNonNull(text, "text");
	}

}
