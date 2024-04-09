/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The ColouredSelectControl extends the immutable rendering of the SelectControl by
 * prefixing the rendered string with a colored box. The color is taken from a continuous
 * spectrum from RED to GREEN and reflects the index of the current selection compared to
 * the current options of the field. 
 * 
 * No colored box is rendered if the field supports
 * multiple selections. For an option list of size 1 the rendered color is GREEN.
 * 
 * Of course this control requires the option list to be ordered.
 * 
 * @author <a href="mailto:cwo@top-logic.com">cwo</a>
 */
public class ColoredSelectControl extends SelectControl {

    private static final String STYLE_ATTR = "style";
    private static final String COLOR_VALUE = "background-color:#___;";

    /**
	 * Creates a {@link ColoredSelectControl}.
	 */
    public ColoredSelectControl(SelectField aModel) {
    	super(aModel);
    }
    
	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = (SelectField) getFieldModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if ((field.getSelection().size() == 1) && !field.isMultiple()) {
				int optionSize = field.getOptionCount() - 1;
				int selectedIndex = field.getOptions().indexOf(field.getSingleSelection());

				out.beginBeginTag(SPAN);
				out.writeAttribute(STYLE_ATTR, COLOR_VALUE.replaceFirst("___", getBackgroundColor(selectedIndex, optionSize)));
				out.endBeginTag();
				out.writeText(HTMLConstants.NBSP);
				out.writeText(HTMLConstants.NBSP);
				out.writeText(HTMLConstants.NBSP);
				out.endTag(SPAN);

				out.writeText(HTMLConstants.NBSP);
				out.writeText(HTMLConstants.NBSP);
			}

			SelectFieldUtils.writeSelectionImmutable(context, out, field);
		}
		out.endTag(SPAN);
	}

    private static String getBackgroundColor(int anIndex, int aTotal) {

        float hue = 0.333f;
        if (aTotal > 0)
            hue = anIndex / ((float) aTotal * 3);

        Color theColor = Color.getHSBColor(hue, 0.5f, 1.0f);
        float[] theRGBComponents = theColor.getRGBColorComponents(null);

        int red = Math.round(theRGBComponents[0] * 255);
        int green = Math.round(theRGBComponents[1] * 255);
        int blue = Math.round(theRGBComponents[2] * 255);

        return Integer.toHexString(red) + Integer.toHexString(green)
                + Integer.toHexString(blue);

    }
}
