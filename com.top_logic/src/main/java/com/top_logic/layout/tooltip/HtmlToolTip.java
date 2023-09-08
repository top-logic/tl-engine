/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tooltip;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.util.Resources;

/**
 * A tool-tip displaying pre-rendered HTML.
 */
public final class HtmlToolTip implements ToolTip {

	private final String _captionHtml;

	private final String _tooltipHtml;

	/**
	 * Creates a {@link HtmlToolTip}.
	 */
	public HtmlToolTip(String tooltipHtml) {
		this(tooltipHtml, null);
	}

	/**
	 * Creates a {@link HtmlToolTip}.
	 */
	public HtmlToolTip(String tooltipHtml, String captionHtml) {
		_tooltipHtml = tooltipHtml;
		_captionHtml = captionHtml;
	}

	@Override
	public boolean hasCaption() {
		return !StringServices.isEmpty(_captionHtml);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void writeCaption(DisplayContext context, TagWriter out) throws IOException {
		out.writeContent(ensureSafeHTMLTooltip(_captionHtml));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void writeContent(DisplayContext context, TagWriter out) throws IOException {
		out.writeContent(ensureSafeHTMLTooltip(_tooltipHtml));
	}

	/**
	 * Converts the given tooltip HTML to a safe variant.
	 */
	public static String ensureSafeHTMLTooltip(String tooltip) {
		try {
			SafeHTML.getInstance().check(tooltip);
			return tooltip;
		} catch (I18NException ex) {
			return TagUtil.encodeXMLAttribute(Resources.getInstance().getString(ex.getErrorKey()) + " " + tooltip);
		}
	}

}