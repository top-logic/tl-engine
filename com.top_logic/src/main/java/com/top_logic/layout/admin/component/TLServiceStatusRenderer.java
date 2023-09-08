/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImageRenderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Renders an icon for the service status.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLServiceStatusRenderer implements Renderer<Boolean> {

	/**
	 * Singleton {@link ThemeImageRenderer} instance.
	 */
	public static final TLServiceStatusRenderer INSTANCE = new TLServiceStatusRenderer();

	@Override
	public void write(DisplayContext context, TagWriter out, Boolean value) throws IOException {
		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, HTMLConstants.TEXT_ALIGN_CENTER);
		out.endBeginTag();

		ThemeUtil.writeThemeImage(context, out, createStatusIcon(value), null);

		out.endTag(HTMLConstants.DIV);
	}

	private ThemeImage createStatusIcon(Boolean value) {
		if (value) {
			return Icons.TL_SERVICE_RUNNING;
		} else {
			return Icons.TL_SERVICE_NOT_RUNNING;
		}
	}

}
