/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.boxes.tag.BoxContentTag;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.tag.ControlTagUtil;
import com.top_logic.layout.form.tag.FormTag;

/**
 * Tag creating a container for description/content cells for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DescriptionContainerTag extends AbstractBodyTag implements BoxContentTag {

	private JSPLayoutedControls _contents;

	private String _cssClass;

	private String _style;

	private String _width;

	private String _firstColumnWidth;

	/**
	 * XML name of this tag.
	 */
	public static final String DESCRIPTION_CONTAINER_TAG = "form:descriptionContainer";

	@Override
	public void setColumns(int columns) {
		// ignore
	}

	@Override
	public void setRows(int rows) {
		// ignore
	}

	@Override
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	@Override
	public void setStyle(String style) {
		_style = style;
	}

	@Override
	public void setWidth(String widthSpec) {
		_width = widthSpec;
	}

	/**
	 * Sets the CSS width of the first column.
	 * 
	 * @param firstColumnWidth
	 *        CSS width.
	 */
	public void setFirstColumnWidth(String firstColumnWidth) {
		_firstColumnWidth = firstColumnWidth;
	}

	/**
	 * Returns the CSS width of the first column.
	 * 
	 * @return CSS width.
	 */
	public String getFirstColumnWidth() {
		if (_firstColumnWidth != null) {
			return _firstColumnWidth;
		} else {
			GroupCellTag groupCellParent = getGroupCellParent();

			if (groupCellParent != null) {
				return groupCellParent.getFirstColumnWidth();
			} else {
				return null;
			}
		}
	}

	private GroupCellTag getGroupCellParent() {
		Tag ancestor = getParent();

		while (ancestor != null) {
			if (ancestor instanceof GroupCellTag) {
				return (GroupCellTag) ancestor;
			}

			if (ancestor instanceof FormTag) {
				// Other FormTag that is not a GroupCellTag, stop search since the current tag must
				// be
				// rendered in textual context mode.
				return null;
			}

			ancestor = ancestor.getParent();
		}

		// No container found.
		return null;
	}

	@Override
	protected void setup() {
		super.setup();
		_contents = new JSPLayoutedControls();
	}

	@Override
	protected void tearDown() {
		_contents = null;
		super.tearDown();
	}

	@Override
	protected String getTagName() {
		return DESCRIPTION_CONTAINER_TAG;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return _contents.addControl(childControl);
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();
		if (getBodyContent() != null) {

			String contentPattern = getBodyContent().getString();
			_contents.setContentPattern(contentPattern);
		}

		return doAfterBody;
	}

	@Override
	public int doEndTag() throws JspException {
		DescriptionContainerControl descriptionContainerControl = new DescriptionContainerControl(_contents);

		descriptionContainerControl.setStyle(_style);
		descriptionContainerControl.setCssClass(_cssClass);
		descriptionContainerControl.setWidth(_width);

		try {
			ControlTagUtil.writeControl(getOut(), this, pageContext, descriptionContainerControl);
		} catch (IOException ex) {
			throw new JspException(ex);
		}

		return super.doEndTag();
	}
}
