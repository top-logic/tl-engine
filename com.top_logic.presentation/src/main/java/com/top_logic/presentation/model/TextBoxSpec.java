/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * A text without fill and outline.
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param text
 *        The text. Its inner margins are zero unless given otherwise, so that the text starts at
 *        the position of the box.
 */
public record TextBoxSpec(Bounds bounds, TextSpec text) implements ElementSpec {
	// Pure data.
}
