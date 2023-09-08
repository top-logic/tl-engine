/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import javax.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.model.DescriptionBox;
import com.top_logic.layout.form.boxes.model.FragmentBox;

/**
 * Tag creating a description/content cell.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DescriptionCellTag extends AbstractBoxTag implements BoxContentTag {

	/**
	 * XML name of this tag.
	 */
	public static final String DESCRIPTION_CELL_TAG = "form:descriptionCell";

	private DescriptionBox _descriptionBox;

	@Override
	protected String getTagName() {
		return DESCRIPTION_CELL_TAG;
	}

	@Override
	protected void setup() {
		super.setup();

		getBoxContainerTag().addBox(mkDescriptionBox());
	}

	@Override
	public void setCssClass(String cssClass) {
		mkDescriptionBox().setCssClass(cssClass);
	}

	@Override
	public void setStyle(String style) {
		mkDescriptionBox().setStyle(style);
	}

	@Override
	public void setWidth(String widthSpec) {
		mkDescriptionBox().getContent().setWidth(DisplayDimension.parseDimension(widthSpec));
	}

	@Override
	public void setColumns(int columns) {
		mkDescriptionBox().setInitialColumns(columns);
	}

	@Override
	public void setRows(int rows) {
		mkDescriptionBox().setInitialRows(rows);
	}

	private DescriptionBox mkDescriptionBox() {
		if (_descriptionBox == null) {
			_descriptionBox = new DescriptionBox(JSPLayoutedControls.createJSPContentBox());
			_descriptionBox.setCssClass(DescriptionBox.DEFAULT_CONTENT_CSS_CLASS);
		}
		return _descriptionBox;
	}

	@Override
	public int doAfterBody() throws JspException {
		setContentPattern(getBodyContent().getString());
		return super.doAfterBody();
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return JSPLayoutedControls.addControl(getContentBox(), childControl);
	}

	/**
	 * Sets the given JSP content pattern to the content part.
	 * 
	 * @see JSPLayoutedControls#setContentPattern(String)
	 */
	protected void setContentPattern(String contentPattern) {
		JSPLayoutedControls.setContentPattern(getContentBox(), contentPattern);
	}

	private FragmentBox getContentBox() {
		return (FragmentBox) mkDescriptionBox().getContent();
	}

	@Override
	protected void tearDown() {
		_descriptionBox = null;
	}

	/**
	 * Initializes the description part of this tag's {@link DescriptionBox}.
	 * 
	 * @return The newly created description part.
	 */
	public FragmentBox initDescription() {
		FragmentBox result = JSPLayoutedControls.createJSPContentBox();
		result.setWidth(DisplayDimension.ZERO_PERCENT);
		result.setCssClass(DescriptionBox.DEFAULT_DESCRIPTION_CSS_CLASS);
		_descriptionBox.setDescription(result);
		return result;
	}

	/**
	 * Whether the description part has already been initialized.
	 * 
	 * @see #initDescription()
	 */
	public boolean hasDescription() {
		return _descriptionBox.hasDescription();
	}

}
