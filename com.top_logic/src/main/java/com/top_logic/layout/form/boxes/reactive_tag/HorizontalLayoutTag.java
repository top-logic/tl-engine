/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.form.tag.AbstractTag;

/**
 * {@link AbstractTag} creating a horizontal collection of Cells for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class HorizontalLayoutTag extends AbstractTag {

	/**
	 * XML name of this tag.
	 */
	public static final String HORIZONTAL_LAYOUT_TAG = "form:horizontal";

	@Override
	protected int startElement() throws JspException, IOException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}
}