/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.fragments.Fragments.Attribute;

/**
 * A {@link HTMLFragment} that allows inspection of its top-level tag.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Tag extends HTMLFragment {

	/**
	 * The tag name to be rendered.
	 * 
	 * @see TagWriter#beginTag(String)
	 */
	String getTagName();

	/**
	 * Whether this tag should be rendered empty.
	 * 
	 * <p>
	 * An empty tag must not have any {@link #getContent()}.
	 * </p>
	 */
	boolean isEmptyTag();

	/**
	 * All attributes of this tag.
	 * 
	 * @see TagWriter#writeAttribute(String, CharSequence)
	 */
	Attribute[] getAttributes();

	/**
	 * The content of this tag.
	 * 
	 * <p>
	 * Must be empty, if {@link #isEmptyTag()} returns <code>true</code>.
	 * </p>
	 */
	HTMLFragment[] getContent();

}
