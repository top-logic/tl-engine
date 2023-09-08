/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.tag.ControlBodyTag;

/**
 * {@link Tag} rendering a custom complex title of a {@link GroupCellTag} for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class CellTitleTag extends AbstractBoxTag implements ControlBodyTag {

	private static final String TITLE_TAG = "form:cellTitle";

	private boolean _raw;

	private JSPLayoutedControls _contents = new JSPLayoutedControls();

	/**
	 * Sets whether the title content should be used as-is. Giving this option, the JSP is
	 * responsible for rendering a span with the CSS class GroupCellTag#TITLE_TEXT_CSS_CLASS around
	 * the title text.
	 * 
	 * @param raw
	 *        Whether the title content should be used as-is.
	 */
	public void setRaw(boolean raw) {
		_raw = raw;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return _contents.addControl(childControl);
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();
		String content = getBodyContent().getString();
		_contents.setContentPattern(content);
		getGroupCellParent().setTitle(wrapTitle(_contents));

		return doAfterBody;
	}

	private HTMLFragment wrapTitle(HTMLFragment title) {
		if (_raw) {
			return title;
		}
		return GroupCellTag.wrapTitle(title);
	}

	private GroupCellTag getGroupCellParent() {
		Tag parent = getParent();
		if (!(parent instanceof GroupCellTag)) {
			throw new IllegalStateException("A '" + getTagName() + "' must only be nested within a '"
				+ GroupCellTag.GROUP_CELL_TAG + "' tag.");
		}
		return (GroupCellTag) parent;
	}

	@Override
	protected String getTagName() {
		return TITLE_TAG;
	}
}
