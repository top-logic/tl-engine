/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.link;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * Common super class for {@link LinkRenderer} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractLinkRenderer implements LinkRenderer {

	/**
	 * Writes the tooltip attributes as specified by {@link Link#getTooltip()} and
	 * {@link Link#getTooltipCaption()} of the given {@link Link}.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	protected <B> void writeTooltipAttributes(DisplayContext context, TagWriter out, Link button) throws IOException {
		ResKey tooltip = button.getTooltip();
		ResKey label = button.getLabel();

		if (tooltip == null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, label);
		} else {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip,
				button.getTooltipCaption());
		}
	}

	/**
	 * Writes the CSS classes attribute on the top-level element.
	 * 
	 * @see #writeCssClassesContent(DisplayContext, Appendable, Link)
	 */
	protected final <B> void writeCssClasses(DisplayContext context, TagWriter out, Link button)
			throws IOException {
		out.beginCssClasses();
		{
			writeCssClassesContent(context, out, button);
		}
		out.endCssClasses();
	}

	/**
	 * Writes the CSS classes of the top-level element as specified by {@link Link#writeCssClassesContent(Appendable)}.
	 * 
	 * <p>
	 * The method may be overridden by implementing classes to add renderer-specific classes.
	 * </p>
	 */
	protected <B> void writeCssClassesContent(DisplayContext context, Appendable out, Link button) throws IOException {
		button.writeCssClassesContent(out);
	}

}
