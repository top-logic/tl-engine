/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.model.FragmentBox;

/**
 * {@link BoxTag} creating the description part of a {@link DescriptionCellTag}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CellDescriptionTag extends AbstractBoxTag implements BoxContentTag {

	/**
	 * XML name of this tag.
	 */
	public static final String DESCRIPTION_TAG_NAME = "form:description";

	private FragmentBox _description;

	@Override
	protected String getTagName() {
		return DESCRIPTION_TAG_NAME;
	}

	@Override
	public void setColumns(int columns) {
		mkDescription().setInitialColumns(columns);
	}

	@Override
	public void setRows(int rows) {
		mkDescription().setInitialRows(rows);
	}

	@Override
	public void setCssClass(String cssClass) {
		mkDescription().setCssClass(cssClass);
	}

	@Override
	public void setStyle(String style) {
		mkDescription().setStyle(style);
	}

	@Override
	public void setWidth(String widthSpec) {
		mkDescription().setWidth(DisplayDimension.parseDimension(widthSpec));
	}

	private FragmentBox mkDescription() {
		if (_description == null) {
			_description = getDescriptionCellTag().initDescription();
		}
		return _description;
	}

	private DescriptionCellTag getDescriptionCellTag() {
		Tag parent = getParent();
		if (!(parent instanceof DescriptionCellTag)) {
			throw new IllegalStateException("A " + DESCRIPTION_TAG_NAME + " tag must be nested within a "
				+ DescriptionCellTag.DESCRIPTION_CELL_TAG + " tag.");
		}
		return (DescriptionCellTag) parent;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return JSPLayoutedControls.addControl(mkDescription(), childControl);
	}

	@Override
	public int doAfterBody() throws JspException {
		JSPLayoutedControls.setContentPattern(mkDescription(), getBodyContent().getString());

		return super.doAfterBody();
	}

	@Override
	protected void tearDown() {
		_description = null;

		super.tearDown();
	}

}
