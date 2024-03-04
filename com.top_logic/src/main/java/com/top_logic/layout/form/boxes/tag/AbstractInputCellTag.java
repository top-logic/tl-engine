/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.RequestUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.boxes.model.FragmentBox;

/**
 * Base class for {@link BoxTag}s creating lable/value cells.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractInputCellTag extends DescriptionCellTag {

	private boolean _customError = false;

	private FragmentBox _capture;

	/**
	 * Whether the error control is rendered explicitly instead of implicitly after the input control.
	 */
	public void setCustomError(boolean customError) {
		_customError = customError;
	}

	/**
	 * The {@link Tag} to render the label.
	 */
	protected abstract Tag setupLabelTag();

	/**
	 * The {@link Tag} to render the input element.
	 */
	protected abstract Tag setupInputTag();

	/**
	 * The {@link Tag} to use for rendering the error control.
	 */
	protected abstract Tag setupErrorTag();
	
	@Override
	public String addControl(HTMLFragment childControl) {
		if (_capture == null) {
			return super.addControl(childControl);
		} else {
			return JSPLayoutedControls.addControl(_capture, childControl);
		}
	}

	@Override
	public int doEndTag() throws JspException {
		if (!hasDescription()) {
			FragmentBox labelBox = initDescription();

			TagWriter buffer = new TagWriter();

			// Hack to capture generated controls by the label tag into the label content box.
			_capture = labelBox;
			try {
				render(buffer, setupLabelTag());
			} finally {
				_capture = null;
			}

			JSPLayoutedControls.setContentPattern(labelBox, buffer.toString());
		}

		if (getBodyContent() == null) {
			TagWriter buffer = new TagWriter();
			render(buffer, setupInputTag());
			render(buffer, setupErrorTag());

			setContentPattern(buffer.toString());
		} else {
			String jspContent = getBodyContent().getString();
			if (_customError) {
				setContentPattern(jspContent);
			} else {
				TagWriter buffer = new TagWriter();
				render(buffer, setupErrorTag());

				setContentPattern(jspContent + buffer.toString());
			}
		}

		return super.doEndTag();
	}

	private void render(TagWriter out, Tag tag) throws JspException {
		TagWriter before = getOut();
		RequestUtil.installTagWriter(pageContext, out);
		try {
			tag.doStartTag();
			tag.doEndTag();
		} finally {
			RequestUtil.installTagWriter(pageContext, before);
		}
	}

	/**
	 * Utility for starting a tag live-cycle.
	 * 
	 * @param tag The {@link Tag} to initialize.
	 */
	protected final void setupTag(Tag tag) {
		tag.setPageContext(pageContext);
		tag.setParent(this);
	}

	@Override
	protected void tearDown() {
		super.tearDown();

		_customError = false;
		_capture = null;
	}

}
