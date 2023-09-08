/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Tag} rendering a custom complex title of a {@link GroupCellTag}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CellTitleTag extends AbstractBoxTag implements ControlBodyTag {

	private static final String TITLE_TAG = "form:title";

	private boolean _raw;

	private JSPLayoutedControls _contents = new JSPLayoutedControls();

	/**
	 * Prevent automatically wraping the title with a {@link HTMLConstants#SPAN}.
	 * 
	 * @param raw
	 *        Whether the title content should be used as-is. Giving this option, the JSP is
	 *        responsible for rendering a {@link HTMLConstants#SPAN} with the CSS class
	 *        {@link GroupCellTag#TITLE_TEXT_CSS_CLASS} around the title text.
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
		String content = getBodyContent().getString();
		_contents.setContentPattern(content);
		getGroupCellParent().setTitle(wrapTitle(_contents));

		return super.doAfterBody();
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
	protected void tearDown() {
		_raw = false;
		_contents = null;

		super.tearDown();
	}

	@Override
	protected String getTagName() {
		return TITLE_TAG;
	}

}
