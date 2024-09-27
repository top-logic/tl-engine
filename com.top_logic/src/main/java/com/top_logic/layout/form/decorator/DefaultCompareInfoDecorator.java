/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Default {@link DetailDecorator} rendering the image of the {@link CompareInfo} with a tooltip.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultCompareInfoDecorator implements DetailDecorator {

	/** Default instance of {@link DefaultCompareInfoDecorator}. */
	public static DefaultCompareInfoDecorator INSTANCE = new DefaultCompareInfoDecorator();

	/**
	 * Creates a new {@link DefaultCompareInfoDecorator}.
	 */
	protected DefaultCompareInfoDecorator() {
		// No properties to set here.
	}

	@Override
	public void start(DisplayContext context, TagWriter out, CompareInfo info, DetailDecorator.Context mode)
			throws IOException {
		out.beginBeginTag(HTMLConstants.SPAN);
		out.beginCssClasses();
		out.append(info.getChangedCSSClass());
		switch (mode) {
			case DEFAULT:
				out.append("form");
				break;
			case TABLE:
				out.append("table");
				break;
		}
		out.endCssClasses();
		out.endBeginTag();

		writeImage(context, out, info);
	}

	private void writeImage(DisplayContext context, TagWriter out, CompareInfo info) throws IOException {
		ThemeImage changedIcon = info.getChangedIcon();
		if (changedIcon == null) {
			return;
		}

		changedIcon.writeWithTooltip(context, out, getTooltip(context, info));
	}

	@Override
	public void end(DisplayContext context, TagWriter out, CompareInfo info, DetailDecorator.Context mode)
			throws IOException {
		out.endTag(SPAN);
	}

	/**
	 * Return the translated tool tip describing the change.
	 * 
	 * @param context
	 *        Context we are currently rendering in.
	 * @param info
	 *        The info object containing all needed information.
	 * @return The requested too tip.
	 */
	protected ResKey getTooltip(DisplayContext context, CompareInfo info) {
		return info.getTooltip();
	}

}
