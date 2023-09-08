/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.code;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.renderers.PreformattedRenderer;

/**
 * Renderer for preview tooltips of code editor fields displayed in a table cell.
 */
public class CodeEditorTooltipRenderer extends PreformattedRenderer<String> {

	/**
	 * Singleton {@link CodeEditorTooltipRenderer} instance.
	 */
	public static final CodeEditorTooltipRenderer INSTANCE = new CodeEditorTooltipRenderer();

	private CodeEditorTooltipRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeContent(DisplayContext context, TagWriter out, String value) throws IOException {
		out.writeText(value);
	}

}
