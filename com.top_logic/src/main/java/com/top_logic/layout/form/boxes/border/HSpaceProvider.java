/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.border;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.model.BoxFactory;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.FragmentBox;

/**
 * {@link BoxFactory} for horizontal separators.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HSpaceProvider implements BoxFactory {

	private static final String BOX_H_SPACE_CSS_CLASS = "boxHSpace";

	/**
	 * Singleton {@link HSpaceProvider} instance.
	 */
	public static final HSpaceProvider INSTANCE = new HSpaceProvider();

	private HSpaceProvider() {
		// Singleton constructor.
	}

	@Override
	public ContentBox newBox() {
		DisplayDimension width = ThemeFactory.getTheme().getValue(Icons.BOX_HSPACE_SIZE);

		// Render a non-breakable space to prevent the cell from collapsing.
		FragmentBox result = Boxes.cssTextBox(BOX_H_SPACE_CSS_CLASS, "");
		result.setWidth(width);
		return result;
	}
	
}
