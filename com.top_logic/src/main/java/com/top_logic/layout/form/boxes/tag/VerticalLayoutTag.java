/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.layout.form.boxes.layout.VerticalLayout;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;

/**
 * {@link BoxTag} creating a vertical layout of {@link Box}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VerticalLayoutTag extends AbstractBoxStructureTag {

	/**
	 * XML name of this tag.
	 */
	public static final String VERTICAL_LAYOUT_TAG = "form:vertical";

	@Override
	protected DefaultCollectionBox createCollectionBox() {
		return new DefaultCollectionBox(VerticalLayout.INSTANCE);
	}

	@Override
	protected String getTagName() {
		return VERTICAL_LAYOUT_TAG;
	}

}
