/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.form.boxes.tag.BoxTag;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.model.form.ReactiveFormCSS;

/**
 * {@link BoxTag} crating a vertical or horizontal visible separator depending on the orientation of
 * the container for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class SeparatorTag extends AbstractTag {

	private boolean _visible = true;

	/**
	 * XML name of this tag.
	 */
	public static final String SEPARATOR_TAG = "form:separator";

	/**
	 * Sets whether the separator should be displayed or is rendered just as space.
	 * 
	 * @param visible
	 *        Visibility of the separator.
	 */
	public void setVisible(boolean visible) {
		_visible = visible;
	}

	private String getCssClass() {
		if (_visible) {
			return "rf_hr " + ReactiveFormCSS.RF_INPUT_CELL + " " + ReactiveFormCSS.RF_LINE + " visible";
		} else {
			return "rf_hr " + ReactiveFormCSS.RF_INPUT_CELL + " " + ReactiveFormCSS.RF_LINE;
		}
	}

	@Override
	protected int startElement() throws JspException, IOException {

		getOut().beginBeginTag(DIV);
		getOut().writeAttribute(CLASS_ATTR, getCssClass());
		getOut().endBeginTag();
		getOut().endTag(DIV);

		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}
}