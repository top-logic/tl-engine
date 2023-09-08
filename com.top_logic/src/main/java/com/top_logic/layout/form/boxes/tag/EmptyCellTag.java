/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.boxes.border.HSpaceProvider;
import com.top_logic.layout.form.boxes.border.VSpaceProvider;
import com.top_logic.layout.form.boxes.model.ContentBox;

/**
 * {@link BoxTag} creating a (visible) empty cell with the dimension of a single text row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptyCellTag extends AbstractBoxContentTag {

	/**
	 * XML name of this tag.
	 */
	public static final String EMPTY_CELL_TAG = "form:emptyCell";

	@Override
	protected String getTagName() {
		return EMPTY_CELL_TAG;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		throw new UnsupportedOperationException("Empty cells may not contain contents.");
	}

	@Override
	protected ContentBox createBox() {
		BoxContainerTag containerTag = getBoxContainerTag();
		boolean horizontal = containerTag.getLayout().isHorizontal();

		if (horizontal) {
			return HSpaceProvider.INSTANCE.newBox();
		} else {
			return VSpaceProvider.INSTANCE.newBox();
		}
	}

}
