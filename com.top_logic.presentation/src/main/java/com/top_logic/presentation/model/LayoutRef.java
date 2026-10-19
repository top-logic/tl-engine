/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Reference to the slide layout of a template a slide is based on.
 */
public sealed interface LayoutRef {

	/**
	 * A layout given by its position in the layouts of the template.
	 *
	 * @param index
	 *        The 0-based index in the <code>layouts</code> of the template analysis.
	 */
	record Index(int index) implements LayoutRef {
		// Pure data.
	}

	/**
	 * A layout given by its name.
	 *
	 * @param name
	 *        The name of the layout in the template.
	 */
	record Name(String name) implements LayoutRef {
		// Pure data.
	}

	/**
	 * A layout given by its purpose.
	 *
	 * @param role
	 *        The purpose of the layout, mapped to a layout of the template when the presentation is
	 *        created.
	 */
	record Role(LayoutRole role) implements LayoutRef {
		// Pure data.
	}

}
