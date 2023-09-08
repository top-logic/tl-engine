/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.basic.fragments.Fragments;

/**
 * A {@link Box} with content specified by a XML frament string.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FragmentBox extends AbstractContentBox {

	private HTMLFragment _content;

	FragmentBox(HTMLFragment content) {
		_content = content;
	}

	/**
	 * Sets the XML fragment content to be rendered.
	 */
	public void setRenderedContent(String value) {
		setContentRenderer(Fragments.htmlSource(value));
	}

	/**
	 * Sets the text content to be rendered.
	 */
	public void setTextContent(String value) {
		setContentRenderer(Fragments.text(value));
	}

	@Override
	public HTMLFragment getContentRenderer() {
		return _content;
	}

	/**
	 * @see #getContentRenderer()
	 */
	public void setContentRenderer(HTMLFragment content) {
		_content = content;
		notifyLayoutChange();
	}

}
