/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * The class {@link InvisibleView} is an dummy implementation for a view which is always invisible
 * and writes nothing.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InvisibleView implements View {

	/** Sole instance of {@link InvisibleView}. */
	public static final InvisibleView INSTANCE = new InvisibleView();

	private InvisibleView() {
		// singleton instance
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		// always invisible
	}

	@Override
	public boolean isVisible() {
		return false;
	}

}

