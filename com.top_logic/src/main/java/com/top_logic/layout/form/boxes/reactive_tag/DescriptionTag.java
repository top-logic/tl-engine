/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.boxes.tag.BoxTag;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.tag.ControlBodyTag;

/**
 * {@link BoxTag} creating the description part of a {@link DescriptionCellTag} for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DescriptionTag extends AbstractBoxTag implements ControlBodyTag {

	private JSPLayoutedControls _contents;

	private String _cssClass;

	private String _style;

	/**
	 * XML name of this tag.
	 */
	public static final String DESCRIPTION_TAG_NAME = "form:description";

	/**
	 * Is ignored.
	 * 
	 * @param columns
	 *        Number of columns.
	 */
	public void setColumns(int columns) {
		// ignore
	}

	/**
	 * Is ignored.
	 * 
	 * @param rows
	 *        Number of rows.
	 */
	public void setRows(int rows) {
		// ignore
	}

	/**
	 * Sets the CSS class(es) to annotate to this cell.
	 * 
	 * @param cssClass
	 *        The CSS class(es).
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * Sets the CSS style to annotate to this cell.
	 * 
	 * @param style
	 *        The CSS style.
	 */
	public void setStyle(String style) {
		_style = style;
	}

	private boolean getColon() {
		if (getDescriptionCellParent() != null) {
			return getDescriptionCellParent().getColon();
		} else {
			return true;
		}
	}

	private DescriptionCellTag getDescriptionCellParent() {
		Tag parent = getParent();

		if (parent instanceof DescriptionCellTag) {
			return (DescriptionCellTag) parent;
		} else {
			return null;
		}
	}

	@Override
	protected String getTagName() {
		return DESCRIPTION_TAG_NAME;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		if (childControl instanceof LabelControl) {
			((LabelControl) childControl).setColon(getColon());
		}
		return _contents.addControl(childControl);
	}

	@Override
	protected void setup() {
		super.setup();
		_contents = new JSPLayoutedControls();
	}

	@Override
	protected void tearDown() {
		_contents = null;
		_style = null;
		_cssClass = null;
		super.tearDown();
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();

		String content = getBodyContent().getString();
		_contents.setContentPattern(content);
		setDescriptionToParent(_contents);

		return doAfterBody;
	}

	private void setDescriptionToParent(JSPLayoutedControls description) {
		DescriptionCellTag parent = getDescriptionCellParent();
		if (parent != null) {
			parent.setDescription(description);
			parent.setFirstColumnStyle(_style);
			parent.setFirstColumnCssClass(_cssClass);
		}
	}
}
