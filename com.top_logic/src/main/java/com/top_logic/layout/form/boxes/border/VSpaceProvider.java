/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.border;

import com.top_logic.layout.form.boxes.model.BoxFactory;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.ContentBox;

/**
 * {@link BoxFactory} creating vertical space boxes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VSpaceProvider implements BoxFactory {

	private static final String BOX_V_SPACE_CSS_CLASS = "boxVSpace";
	/**
	 * Singleton {@link VSpaceProvider} instance.
	 */
	public static final VSpaceProvider INSTANCE = new VSpaceProvider();

	private VSpaceProvider() {
		// Singleton constructor.
	}

	@Override
	public ContentBox newBox() {
		return Boxes.cssTextBox(BOX_V_SPACE_CSS_CLASS, "");
	}

}
