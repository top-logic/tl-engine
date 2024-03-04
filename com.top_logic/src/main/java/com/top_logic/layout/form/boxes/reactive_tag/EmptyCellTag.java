/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.form.tag.AbstractTag;

/**
 * {@link AbstractTag} creating a (visible) empty cell with the dimension of a single text row for
 * reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class EmptyCellTag extends AbstractTag {

	/**
	 * XML name of this tag.
	 */
	public static final String EMPTY_CELL_TAG = "form:emptyCell";

	@Override
	protected int startElement() throws JspException, IOException {
		getOut().beginTag(DIV);
		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		getOut().endTag(DIV);
		return EVAL_PAGE;
	}
}