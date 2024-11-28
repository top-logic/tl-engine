/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.util.css.CssUtil;

/**
 * Control creating an arbitrary Cell for reactive forms.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class CellControl extends ConstantControl<HTMLFragment> {

	private String _cssClass;

	private String _style;

	private String _width;

	private boolean _wholeLine = false;

	/**
	 * Creates a {@link CellControl}. *
	 * 
	 * @param model
	 *        The content of this Cell.
	 */
	public CellControl(HTMLFragment model) {
		super(model);
	}

	/**
	 * Sets the CSS class(es) to annotate to the cell.
	 * 
	 * @param cssClass
	 *        The CSS class(es).
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * Sets the CSS style to annotate to the cells
	 * 
	 * @param style
	 *        The CSS style.
	 */
	public void setStyle(String style) {
		_style = style;
	}

	/**
	 * Sets the CSS width of the cell.
	 * 
	 * @param width
	 *        The CSS width of the cell.
	 */
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

	private void writeCombinedStyle(TagWriter out) throws IOException {
		String width = getWidth();

		String style1 = (_style != null) ? _style : "";
		String style2 = (width != null) ? ("min-width: " + width + "; width: " + width) : "";

		CssUtil.writeCombinedStyle(out, style1, style2);
	}

	private String getWidth() {
		if (_width != null) {
			if (_width.endsWith("%")) {
				return "calc(" + _width + " - " + Icons.RF_HORIZONTAL_GAP_COLUMNS.get() + ")";
			} else {
				return _width;
			}
		}
		return null;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		writeCombinedStyle(out);
		out.endBeginTag();
		getModel().write(context, out);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, _cssClass);
		HTMLUtil.appendCSSClass(out, ReactiveFormCSS.RF_INPUT_CELL);
		if (_wholeLine) {
			HTMLUtil.appendCSSClass(out, ReactiveFormCSS.RF_LINE);
		}
		if (_width != null) {
			HTMLUtil.appendCSSClass(out, ReactiveFormCSS.CELL_SMALL_CSS);
		}
	}
}
