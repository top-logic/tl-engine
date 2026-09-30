/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} that renders a model value.
 */
public class VariableTemplate implements RawTemplateFragment {

	private final String _name;

	/**
	 * Creates a {@link VariableTemplate}.
	 *
	 * @param name
	 *        The name of the model property to render.
	 */
	public VariableTemplate(String name) {
		_name = name;
	}

	/**
	 * Name of the variable accessed.
	 */
	public String getName() {
		return _name;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		int depth = out.getDepth();
		try {
			properties.renderProperty(context, out, _name);
		} catch (Throwable exception) {
			switch (out.getState()) {
				case ELEMENT_CONTENT:
					// Only close in element content, then the caller cannot observe the change.
					out.endAll(depth);

					HTMLTemplateUtils.renderError(context, out, exception);
					break;

				default:
					// This cannot be handled, because the caller will fail afterwards because the
					// writer would have changed state, which is not expected by a caller.
					throw exception;
			}
			return;
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
