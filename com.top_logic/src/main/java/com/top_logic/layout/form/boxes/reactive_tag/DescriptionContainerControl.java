/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.util.css.CssUtil;

/**
 * Control to write a container tag for description/content cells.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class DescriptionContainerControl extends ConstantControl<HTMLFragment> {

	private String _cssClass;

	private String _style;

	private String _width;

	/**
	 * Creates a DescriptionContainer.
	 * 
	 * @param model
	 *        The content.
	 */
	protected DescriptionContainerControl(HTMLFragment model) {
		super(model);
	}

	/**
	 * The CSS style to annotate to occupied descriptionContainer.
	 * 
	 * @param style
	 *        The CSS style.
	 */
	public void setStyle(String style) {
		_style = style;
	}

	/**
	 * The CSS class(es) to annotate to occupied descriptionContainer.
	 * 
	 * @param cssClass
	 *        The CSS class(es).
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * The CSS width of the column of description container, only relevant if used in a column
	 * layout.
	 * 
	 * @param width
	 *        The CSS width.
	 */
	public void setWidth(String width) {
		_width = width;
	}


	private void writeStyle(TagWriter out) {
		if (_style != null && _width != null) {
			String styles = CssUtil.joinStyles(_style, "width: " + _width);
			out.writeAttribute(STYLE_ATTR, styles);
		} else {
			if (_style != null) {
				out.writeAttribute(STYLE_ATTR, _style);
			}

			if (_width != null) {
				out.writeAttribute(STYLE_ATTR, "width: " + _width);
			}
		}
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		writeStyle(out);
		out.endBeginTag();

		getModel().write(context, out);

		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append("rf_descriptionContainer");
		if (_cssClass != null) {
			out.append(_cssClass);
		}
	}
}