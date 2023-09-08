/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.border;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.layout.HorizontalLayout;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;
import com.top_logic.layout.form.boxes.model.FragmentBox;

/**
 * {@link Box} rendering two columns creating a vertical bevel border between them.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class VerticalBevelBorder extends DefaultCollectionBox {

	private static final String LEFT_CSS_CLASS = "frmVrLeft";

	private static final String RIGHT_CSS_CLASS = "frmVrRight";

	/**
	 * Creates a {@link VerticalBevelBorder}.
	 */
	public VerticalBevelBorder() {
		super(HorizontalLayout.INSTANCE);

		FragmentBox left = Boxes.cssTextBox(LEFT_CSS_CLASS, "");
		left.setWidth(DisplayDimension.ZERO_PERCENT);
		FragmentBox right = Boxes.cssTextBox(RIGHT_CSS_CLASS, "");
		right.setWidth(DisplayDimension.ZERO_PERCENT);

		addContent(left);
		addContent(right);
	}

}