/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Reference to a placeholder of a slide layout.
 */
public sealed interface PlaceholderKey {

	/**
	 * A placeholder given by its index.
	 *
	 * @param idx
	 *        The index of the placeholder in its layout, as reported by the template analysis.
	 */
	record Index(long idx) implements PlaceholderKey {
		// Pure data.
	}

	/**
	 * A placeholder given by its name.
	 *
	 * @param name
	 *        The name of the placeholder shape or the placeholder type (e.g. <code>body</code>).
	 */
	record Name(String name) implements PlaceholderKey {
		// Pure data.
	}

}
