/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import javax.servlet.jsp.JspException;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.model.ContentBox;

/**
 * Base class for {@link BoxContentTag}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBoxContentTag extends AbstractBoxTag implements BoxContentTag {

	private ContentBox _box;

	@Override
	public void setColumns(int columns) {
		mkBox().setInitialColumns(columns);
	}

	@Override
	public void setRows(int rows) {
		mkBox().setInitialRows(rows);
	}

	@Override
	public void setCssClass(String cssClass) {
		mkBox().setCssClass(cssClass);
	}

	@Override
	public void setStyle(String style) {
		mkBox().setStyle(style);
	}

	@Override
	public void setWidth(String widthSpec) {
		mkBox().setWidth(DisplayDimension.parseDimension(widthSpec));
	}

	@Override
	public int doStartTag() throws JspException {
		int result = super.doStartTag();

		getBoxContainerTag().addBox(mkBox());

		return result;
	}

	/**
	 * Lazily creates the content box.
	 * 
	 * @return The content box created by this tag.
	 */
	protected ContentBox mkBox() {
		if (_box == null) {
			_box = createBox();
		}
		return _box;
	}

	/**
	 * Hook to actually create the content box.
	 */
	protected abstract ContentBox createBox();

	@Override
	protected void tearDown() {
		super.tearDown();

		_box = null;
	}

}
