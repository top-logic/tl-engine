/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.list.DefaultListRenderer;
import com.top_logic.layout.list.ListControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * {@link DefaultListRenderer} to render a list of options in a selector pop up.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class OptionRenderer extends DefaultListRenderer {

	/**
	 * Create a non-configurable {@link OptionRenderer}.
	 */
	public OptionRenderer() {
		super(false);
	}

	/**
	 * Create a {@link OptionRenderer},that is configurable, or not.
	 */
	public OptionRenderer(boolean configurable) {
		super(configurable);
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, ListControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		LayoutControlRenderer.writeLayoutConstraintInformation(100, DisplayUnit.PERCENT, out);
		LayoutControlRenderer.writeLayoutInformationAttribute(Orientation.HORIZONTAL, 100, out);
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, ListControl control) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "selectbox");
		out.endBeginTag();
		{
			super.writeControlContents(context, out, control);
			out.beginScript();
			{
				out.append("BAL.disableSelection(document.getElementById('" + control.getContentID() + "'))");
			}
			out.endScript();
		}
		out.endTag(DIV);
	}
}