/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.layout.form.boxes.tag.BoxContainerTag;
import com.top_logic.layout.form.boxes.tag.BoxTag;

/**
 * Base class for {@link BoxTag}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBoxTag extends AbstractBodyTag implements BoxTag {

	/**
	 * The {@link BoxContainerTag} ancestor of this tag.
	 * 
	 * @throws IllegalStateException
	 *         If this tag has no {@link BoxContainerTag} ancestor.
	 */
	protected BoxContainerTag getBoxContainerTag() throws IllegalStateException {
		Tag parent = TagSupport.findAncestorWithClass(this, BoxContainerTag.class);
		if (parent == null) {
			throw new IllegalStateException(
				"A " + getTagName() + " tag must be nested within a box container tag (" + VerticalLayoutTag.VERTICAL_LAYOUT_TAG + ", "
					+ HorizontalLayoutTag.HORIZONTAL_LAYOUT_TAG + ", or " + CellTag.CELL_TAG + ").");
		}
		return (BoxContainerTag) parent;
	}
}
