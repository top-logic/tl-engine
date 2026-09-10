/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

/**
 * Algorithm answering the color a value is displayed with.
 *
 * <p>
 * Not every value has a color: a value the algorithm knows no color for is displayed in the
 * default way.
 * </p>
 *
 * @see AnnotationValueColorProvider#INSTANCE
 */
public interface ValueColorProvider {

	/**
	 * The color the given value is displayed with.
	 *
	 * @param value
	 *        The value to display. May be <code>null</code>.
	 * @return The color of the given value, or <code>null</code> if it has none.
	 */
	ValueColor colorOf(Object value);

}
