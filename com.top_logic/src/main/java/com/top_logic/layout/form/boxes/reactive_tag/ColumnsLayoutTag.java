/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.layout.form.tag.ControlTagUtil;

/**
 * {@link AbstractTag} creating a containers with a configurable number of columns for reactive
 * columns.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ColumnsLayoutTag extends AbstractBodyTag implements ControlBodyTag {

	private int _count = 1;

	private boolean _keep = false;

	private JSPLayoutedControls _jspControl;

	/**
	 * XML name of this tag.
	 */
	public static final String COLUMNS_LAYOUT_TAG = "form:columns";

	/**
	 * Maximum number of columns to use. Default is 1.
	 * 
	 * @param count
	 *        Number of columns.
	 */
	public void setCount(int count) {
		_count = count;
	}

	/**
	 * Whether the number of columns is kept instead of adjust to the viewport size. Default is
	 * <code>false</code>.
	 * 
	 * @param keep
	 *        If <code>true</code> the number is kept.
	 */
	public void setKeep(boolean keep) {
		_keep = keep;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return _jspControl.addControl(childControl);
	}

	@Override
	protected String getTagName() {
		return COLUMNS_LAYOUT_TAG;
	}

	@Override
	protected void setup() {
		super.setup();
		_jspControl = new JSPLayoutedControls();
	}

	@Override
	protected void tearDown() {
		_jspControl = null;
		super.tearDown();
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();
		if (getBodyContent() != null) {
			String contentPattern = getBodyContent().getString();
			_jspControl.setContentPattern(contentPattern);
		}

		return doAfterBody;
	}

	@Override
	public int doEndTag() throws JspException {
		ColumnsLayoutControl columnsLayout = new ColumnsLayoutControl(_jspControl);

		columnsLayout.setCount(_count);
		columnsLayout.setKeep(_keep);

		try {
			ControlTagUtil.writeControl(getOut(), this, pageContext, columnsLayout);
		} catch (IOException ex) {
			throw new JspException(ex);
		}
		return super.doEndTag();
	}
}
