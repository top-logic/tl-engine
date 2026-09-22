/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.UIElement;

/**
 * A {@link UIElement} whose content is headed by a title.
 *
 * <p>
 * The title names what the element displays, so that a surrounding element can speak about that
 * content - a dashboard tile, for instance, names the action opening it after the title of what
 * the tile shows.
 * </p>
 */
public interface TitledElement {

	/**
	 * The title heading the content, {@code null} for content displayed without one.
	 */
	ResKey getTitle();

}
