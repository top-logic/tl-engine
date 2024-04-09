/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The FractionSelectControl extends the immutable rendering of the SelectControl by
 * prefixing the rendered string with a bar representing the index of the current selection
 * compared to the option list of the SelectField.
 * 
 * No bar is rendered if the field supports multiple selection or if the option list of the
 * field is empty.
 * 
 * Of course this control requires the option list to be ordered.
 * 
 * @author <a href="mailto:cwo@top-logic.com">cwo</a>
 */
public class FractionSelectControl extends SelectControl {

    private static final String STYLE_ATTR = "style";
    private static final String FILLED_COLOR_VALUE = "background-color:#8280ff;";

    private static final int DEFAULT_RESOLUTION = 10;

    private int resolution;

	/**
	 * Creates a {@link FractionSelectControl} with a resolution of 10 ticks.
	 */
    public FractionSelectControl(SelectField aModel) {
    	this(aModel, DEFAULT_RESOLUTION);
    }

    /**
	 * Creates a {@link FractionSelectControl} with a custom resolution. A higher resolution means
	 * more ticks, hence a longer bar is rendered.
	 * 
	 * @param aResolution
	 *        the number of max ticks to be used
	 */
    public FractionSelectControl(SelectField aModel, int aResolution) {
        super(aModel);
        this.resolution = aResolution;
    }

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = (SelectField) getFieldModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			if ((field.getSelection().size() == 1) && !field.isMultiple()) {

				int[] theFractionRule = getFractions(field);
				out.beginBeginTag(SPAN);
				out.writeAttribute(STYLE_ATTR, FILLED_COLOR_VALUE);
				out.endBeginTag();
				for (int i = 0; i < theFractionRule[0]; i++)
					out.writeText(HTMLConstants.NBSP);
				out.endTag(SPAN);

				for (int i = 0; i < theFractionRule[1]; i++)
					out.writeText(HTMLConstants.NBSP);
			}

			out.writeText(HTMLConstants.NBSP);
			out.writeText(HTMLConstants.NBSP);

			SelectFieldUtils.writeSelectionImmutable(context, out, field);
		}
		out.endTag(SPAN);
	}

    private int[] getFractions(SelectField aField) {

        int[] theResult = new int[2];

        int optionSize = aField.getOptionCount() - 1;
        int selectedIndex = aField.getOptions().indexOf(aField.getSingleSelection());

        if (optionSize == 0)
            return new int[] { 0, 0 };

        theResult[0] = 1 + Math
                .round(((float) resolution - 1) * selectedIndex / optionSize);
        theResult[1] = resolution - theResult[0];

        return theResult;
    }

}
