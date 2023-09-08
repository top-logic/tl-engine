/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link BoxTag} crating a vertical or horizontal visible separator depending on the orientation of
 * the container.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SeparatorTag extends AbstractBoxTag {

	private static final String VR_CSS_CLASS = "frmVr";

	/**
	 * CSS class for a horizontal ruler.
	 */
	public static final String HR_CSS_CLASS = "frmHr";

	/**
	 * XML name of this tag.
	 */
	public static final String SEPARATOR_TAG = "form:separator";

	@Override
	protected String getTagName() {
		return SEPARATOR_TAG;
	}

	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		BoxContainerTag containerTag = getBoxContainerTag();
		boolean horizontal = containerTag.getLayout().isHorizontal();
		Box separator = horizontal ? createVerticalBox() : createHorizontalBox();
		containerTag.addBox(separator);
		
		return SKIP_BODY;
	}

	private Box createVerticalBox() {
		final Theme theme = ThemeFactory.getTheme();
		final DisplayDimension width = theme.getValue(Icons.VR_BACKGROUND_WIDTH);
		ContentBox result = Boxes.contentBox(new HTMLFragment() {
			@Override
			public void write(DisplayContext displayContext, TagWriter out) throws IOException {
				// Render an invisible image with the specified width to ensure that the column
				// reserves the requested space.
				XMLTag tag = Icons.NULL.toIcon();
				tag.beginBeginTag(displayContext, out);
				{
					out.beginAttribute(HTMLConstants.WIDTH_ATTR);
					out.writeFloat(width.getValue());
					out.write(width.getUnit().toString());
					out.endAttribute();
					out.writeAttribute(HTMLConstants.HEIGHT_ATTR, "1");
				}
				tag.endEmptyTag(displayContext, out);
			}
		});
		result.setCssClass(VR_CSS_CLASS);
		result.setWidth(width);
		return result;
	}

	private Box createHorizontalBox() {
		ContentBox result = Boxes.contentBox();
		result.setCssClass(HR_CSS_CLASS);
		return result;
	}

}
