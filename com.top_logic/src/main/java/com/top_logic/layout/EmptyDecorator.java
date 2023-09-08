/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * {@link ContentDecorator} that creates no decoration at all.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EmptyDecorator implements ContentDecorator {
	
	/**
	 * Singleton {@link EmptyDecorator} instance.
	 */
	public static final EmptyDecorator INSTANCE = new EmptyDecorator();

	private EmptyDecorator() {
		// Singleton constructor.
	}
	
	@Override
	public void endDecoration(DisplayContext context, TagWriter out, Object value) throws IOException {
		// does nothing
	}

	@Override
	public void startDecoration(DisplayContext context, TagWriter out, Object value) throws IOException {
		// does nothing
	}
}