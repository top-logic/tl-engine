/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.renderers.PreformattedRenderer;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link Renderer} displaying {@link SearchExpression}s in HTML.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLScriptRenderer extends PreformattedRenderer<SearchExpression> {

	/**
	 * Singleton {@link TLScriptRenderer} instance.
	 */
	public static final TLScriptRenderer INSTANCE = new TLScriptRenderer();

	private TLScriptRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeContent(DisplayContext context, TagWriter out, SearchExpression value) throws IOException {
		out.writeText(value.toString());
	}

}
