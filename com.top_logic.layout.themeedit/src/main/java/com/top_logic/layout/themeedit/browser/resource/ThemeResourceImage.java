/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.resource;

import com.top_logic.layout.basic.ThemeImage;

/**
 * A {@link ThemeImage} with a tool-tip.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ThemeResourceImage {
	ThemeImage _image;
	String _tooltip;

	/**
	 * Create a new {@link ThemeResourceImage}.
	 * 
	 * @param image
	 *        {@link ThemeImage}
	 * @param tooltip
	 *        Tool-tip as plain text.
	 */
	public ThemeResourceImage(ThemeImage image, String tooltip) {
		_image = image;
		_tooltip = tooltip;
	}

	/**
	 * Returns {@link ThemeImage} of a {@link ThemeResource}.
	 */
	public ThemeImage getImage() {
		return _image;
	}

	/**
	 * Returns the tool-tip of the {@link ThemeImage} of a {@link ThemeResource}.
	 * 
	 * @return Tool-tip as plain text.
	 */
	public String getTooltip() {
		return _tooltip;
	}
}