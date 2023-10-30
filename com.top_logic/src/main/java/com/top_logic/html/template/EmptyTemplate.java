/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * {@link HTMLTemplateFragment} that writes nothing.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class EmptyTemplate implements RawTemplateFragment, LiteralTemplate {

	/**
	 * Singleton {@link EmptyTemplate} instance.
	 */
	public static final EmptyTemplate INSTANCE = new EmptyTemplate();

	private EmptyTemplate() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		// Do nothing.
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
