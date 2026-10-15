/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * An element placed on a slide, e.g. a shape, a text box, a table or an image.
 *
 * <p>
 * Each kind of element has a type name in the data representation, under which its reader is
 * registered in {@link DescriptionReader}.
 * </p>
 */
public interface ElementSpec {

	/**
	 * The position and size of the element on the slide.
	 */
	Bounds bounds();

	/**
	 * The color role of the element, <code>null</code> for none.
	 *
	 * <p>
	 * Within the element, the color role
	 * {@link com.top_logic.presentation.data.PresentationData#ROLE_ACCENT} stands for this role, so
	 * that a style, e.g. of a card, can paint parts of the element in the color of the element's
	 * role.
	 * </p>
	 */
	default String role() {
		return null;
	}

}
