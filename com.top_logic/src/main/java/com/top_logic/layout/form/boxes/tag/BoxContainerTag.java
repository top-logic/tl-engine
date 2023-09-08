/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.boxes.control.BoxControl;
import com.top_logic.layout.form.boxes.layout.BoxLayout;
import com.top_logic.layout.form.boxes.model.Box;

/**
 * {@link BoxTag} that my have other {@link BoxTag}s as content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BoxContainerTag extends BoxTag {

	/**
	 * Whether this {@link Tag} is part of a {@link BoxTag} hierarchy.
	 * 
	 * @return <code>true</code>, if this {@link Tag} renders output by itself.
	 */
	boolean isRoot();

	/**
	 * The top-level {@link Tag} of the {@link BoxTag} hierarchy.
	 */
	BoxContainerTag getRoot();

	/**
	 * The layout algorithm of this {@link Box}.
	 */
	BoxLayout getLayout();

	/**
	 * Adds the given {@link Box} to the container box created by this {@link BoxTag}.
	 */
	void addBox(Box content);

	/**
	 * The rendering control, if this is the root tag.
	 * 
	 * @see #isRoot()
	 * 
	 * @return The rendering control, <code>null</code>, if this is not the root tag.
	 */
	BoxControl getRenderingControl();

}
