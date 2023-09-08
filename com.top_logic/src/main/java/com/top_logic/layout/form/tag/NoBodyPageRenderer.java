/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * {@link PageRenderer} that renders the page without a body and it's container tags.
 *
 * @author <a href="mailto:Jens.Schäfer@top-logic.com">Jens Schäfer</a>
 */
public class NoBodyPageRenderer extends PageRenderer {

	/**
	 * Singleton instance of {@link NoBodyPageRenderer}.
	 */
	public static final NoBodyPageRenderer INSTANCE = new NoBodyPageRenderer();

	@Override
	public void beginBody(TagWriter out, String containerId, boolean resetScollPosition) throws IOException {
		// No body container wanted
	}

	@Override
	public void writeBodyContent(DisplayContext context, TagWriter out, PageControl pageControl) throws IOException {
		// No body wanted
	}

	@Override
	public void endBody(TagWriter out) throws IOException {
		// No body container wanted
	}

}
