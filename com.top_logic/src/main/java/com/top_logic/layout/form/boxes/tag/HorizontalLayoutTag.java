/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.layout.form.boxes.layout.HorizontalLayout;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;

/**
 * {@link BoxTag} crating a horizontal collection of {@link Box}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HorizontalLayoutTag extends AbstractBoxStructureTag {

	/**
	 * XML name of this tag.
	 */
	public static final String HORIZONTAL_LAYOUT_TAG = "form:horizontal";

	@Override
	protected DefaultCollectionBox createCollectionBox() {
		return new DefaultCollectionBox(HorizontalLayout.INSTANCE);
	}

	@Override
	protected String getTagName() {
		return HORIZONTAL_LAYOUT_TAG;
	}

}
