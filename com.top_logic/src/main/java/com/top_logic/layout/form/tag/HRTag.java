/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.boxes.tag.SeparatorTag;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * Create a horizontal line with a label within.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class HRTag extends ResourceTag {

	private static final String CONTAINER_CSS_CLASS = "fhrContainer";

	private static final String TABLE_CSS_CLASS = "fhrTable";

	private static final String LEFT_CSS_CLASS = "fhrLeft";

	private static final String MIDDLE_CSS_CLASS = "fhrMiddle";

	private static final String RIGHT_CSS_CLASS = "fhrRight";

	private boolean info;

    @Override
	protected int startFormMember() throws IOException {
		if (hasResource()) {
			String theTooltip = this.getTooltip();

			TagWriter out = getOut();
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, CONTAINER_CSS_CLASS);
			DisplayContext context = getDisplayContext();
			if (!StringServices.isEmpty(theTooltip)) {
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, theTooltip, null);
			}
			out.endBeginTag();

			{
				out.beginBeginTag(TABLE);
				out.writeAttribute(CLASS_ATTR, TABLE_CSS_CLASS);
				out.endBeginTag();
				{
					out.beginTag(TR);
					{
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, LEFT_CSS_CLASS);
						out.endBeginTag();
						{
							out.writeText(NBSP);
						}
						out.endTag(TD);

						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, MIDDLE_CSS_CLASS);
						out.endBeginTag();
						{
							// Write internationalized text.
							this.writeText(this.getLabel());

				            if (this.hasInfo()) {
								this.writeContent(NBSP);
								getInfoImage().write(context, out);
				            }
						}
						out.endTag(TD);

						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, RIGHT_CSS_CLASS);
						out.endBeginTag();
						{
							out.writeText(NBSP);
						}
						out.endTag(TD);
					}
					out.endTag(TR);
				}
				out.endTag(TABLE);
			}

			out.endTag(DIV);
        }
        else {
			TagWriter out = getOut();
			out.beginBeginTag(HR);
			out.writeAttribute(CLASS_ATTR, SeparatorTag.HR_CSS_CLASS);
			out.endEmptyTag();
        }

        return EVAL_BODY_INCLUDE;
    }
    
    @Override
	protected int endFormMember() throws IOException {
        return super.endFormMember();
    }

    /**
	 * Whether to display an info icon.
	 * 
	 * @see #getInfoImage()
	 */
	public boolean hasInfo() {
		return this.info || hasExplicitImage();
	}

	private boolean hasExplicitImage() {
		return getIcon() != null;
	}

	private ThemeImage getInfoImage() {
		ThemeImage result = getIcon();
		if (result == null) {
			result = com.top_logic.util.error.Icons.INFO;
		}
		return result;
	}

	/**
	 * @see #hasInfo()
	 */
	public void setInfo(boolean showInfo) {
		this.info = showInfo;
	}
}
