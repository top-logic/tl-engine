/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.boxes.tag.AbstractBoxTag;
import com.top_logic.layout.form.boxes.tag.BoxContentTag;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.layout.form.tag.ControlTagUtil;

/**
 * {@link AbstractTag} creating an arbitrary Cell for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class CellTag extends AbstractBoxTag implements BoxContentTag {

	private JSPLayoutedControls _contents;

	private String _cssClass;

	private String _style;

	private String _width;

	private boolean _wholeLine = false;

	/**
	 * XML name of this tag.
	 */
	public static final String CELL_TAG = "form:cell";

	/**
	 * Sets the number of columns requested by this cell.
	 * 
	 * @param columns
	 *        Number of columns.
	 */
	@Override
	public void setColumns(int columns) {
		// ignore.
	}

	/**
	 * Sets the number of rows requested by this cell.
	 * 
	 * @param rows
	 *        Number of rows.
	 */
	@Override
	public void setRows(int rows) {
		// ignore.
	}

	/**
	 * Sets the CSS class(es) to annotate to the cell.
	 * 
	 * @param cssClass
	 *        The CSS class(es).
	 */
	@Override
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * Sets the CSS style to annotate to the cells
	 * 
	 * @param style
	 *        The CSS style.
	 */
	@Override
	public void setStyle(String style) {
		_style = style;
	}

	/**
	 * Sets the CSS width of the cell.
	 * 
	 * @param width
	 *        The CSS width of the cell.
	 */
	@Override
	public void setWidth(String width) {
		_width = width;
	}

	/**
	 * Sets whether the cell is rendered over the whole line.
	 * 
	 * @param wholeLine
	 *        Whether the cell is rendered over the whole line.
	 */
	public void setWholeLine(boolean wholeLine) {
		_wholeLine = wholeLine;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return _contents.addControl(childControl);
	}

	@Override
	protected String getTagName() {
		return CELL_TAG;
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
		CellControl cellControl = new CellControl(_contents);

		cellControl.setCssClass(_cssClass);
		cellControl.setStyle(_style);
		cellControl.setWidth(_width);
		cellControl.setWholeLine(_wholeLine);

		try {
			ControlTagUtil.writeControl(getOut(), this, pageContext, cellControl);
		} catch (IOException ex) {
			throw new JspException(ex);
		}

		return super.doEndTag();
	}
}
