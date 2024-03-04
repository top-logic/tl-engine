/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.tag.BoxTag;
import com.top_logic.layout.form.tag.AbstractTag;

/**
 * {@link BoxTag} creating a vertical layout of {@link Box}es for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class VerticalLayoutTag extends AbstractTag {

	/**
	 * XML name of this tag.
	 */
	public static final String VERTICAL_LAYOUT_TAG = "form:vertical";

	/**
	 * Return the XML name of this tag.
	 * 
	 * @return The XML name.
	 */
	protected String getTagName() {
		return VERTICAL_LAYOUT_TAG;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}
}