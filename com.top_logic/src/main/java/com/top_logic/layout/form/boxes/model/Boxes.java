/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.basic.fragments.Fragments;

/**
 * Factory for {@link Box} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Boxes {

	/**
	 * Creates a {@link FragmentBox} with the given plain text content.
	 * 
	 * @param text
	 *        The textual contents.
	 * @return The new {@link Box}.
	 */
	public static FragmentBox textBox(String text) {
		return contentBox(Fragments.text(text));
	}

	/**
	 * Creates a {@link FragmentBox} with styled contents.
	 * 
	 * @param cssClass
	 *        The CSS class to add to the box.
	 * @param text
	 *        The textual contents.
	 * @return The new {@link Box}.
	 */
	public static FragmentBox cssTextBox(String cssClass, String text) {
		FragmentBox result = textBox(text);
		result.setCssClass(cssClass);
		return result;
	}

	/**
	 * Creates an initially empty {@link FragmentBox}.
	 */
	public static FragmentBox contentBox() {
		return contentBox(Fragments.empty());
	}

	/**
	 * Creates a {@link FragmentBox} with the given contents.
	 * 
	 * @param content
	 *        See {@link FragmentBox#getContentRenderer()}.
	 * @return The new {@link FragmentBox}.
	 */
	public static FragmentBox contentBox(HTMLFragment content) {
		return new FragmentBox(content);
	}

}
