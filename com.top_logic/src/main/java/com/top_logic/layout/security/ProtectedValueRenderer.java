/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * {@link Renderer} for {@link ProtectedValue}s that hides the value, in case of insufficient
 * rights.
 * 
 * @see ProtectedValueCellRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProtectedValueRenderer implements Renderer<Object> {

	public static final String BLOCKED_VALUE_CSS_CLASS = "blocked";

	private final Renderer<Object> valueRenderer;
	private final BoundCommandGroup requiredRight;
	
	public ProtectedValueRenderer(Renderer<Object> valueRenderer) {
		this(valueRenderer, SimpleBoundCommandGroup.READ);
	}
	
	public ProtectedValueRenderer(Renderer<Object> valueRenderer, BoundCommandGroup requiredRight) {
		this.valueRenderer = valueRenderer;
		this.requiredRight = requiredRight;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value)
			throws IOException 
	{
		if (value instanceof ProtectedValue) {
			ProtectedValue protectedValue = (ProtectedValue) value;
			if (protectedValue.hasAccessRight(requiredRight)) {
				valueRenderer.write(context, out, protectedValue.getValue());
			} else {
				writeBlockedContent(out, context);
			}
		} else {
			valueRenderer.write(context, out, value);
		}
	}

	/**
	 * Writes the HTML content for a {@link ProtectedValue} which the user must not see.
	 */
	public static void writeBlockedContent(TagWriter out, DisplayContext context) {
		out.beginBeginTag(HTMLConstants.SPAN);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, BLOCKED_VALUE_CSS_CLASS);
		out.endBeginTag();
		out.writeText(getBlockedText(context.getResources()));
		out.endTag(HTMLConstants.SPAN);
	}

	/**
	 * Returns the text to display when a user has not the right to see the value of the represented
	 * {@link ProtectedValue}.
	 */
	public static String getBlockedText(Resources resources) {
		return resources.getString(I18NConstants.BLOCKED_VALUE_TEXT);
	}

}
