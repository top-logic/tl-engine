/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;

/**
 * A tooltip as {@link DynamicText}.
 */
public final class TooltipValue implements DynamicText {
	private final ResKey _tooltip;

	/** 
	 * Creates a {@link TooltipValue}.
	 */
	public TooltipValue(ResKey tooltip) {
		_tooltip = tooltip;
	}

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		String tooltipHtml = context.getResources().getString(_tooltip);
		try {
			SafeHTML.getInstance().check(tooltipHtml);
		} catch (I18NException ex) {
			tooltipHtml = TagUtil.encodeXMLAttribute(tooltipHtml);
		}
		out.append(tooltipHtml);
	}
}