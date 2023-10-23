/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.RenderErrorUtil;
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

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		try {
			properties.renderProperty(context, out, _name);
		} catch (RuntimeException exception) {
			switch (out.getState()) {
				case ELEMENT_CONTENT:
					ResKey resKey = I18NConstants.ERROR_RENDERING_TEMPLATE_FAILED;
					String message = exception.getMessage();
					RenderErrorUtil.produceErrorOutput(context, out, resKey, message, exception, VariableTemplate.class);
					break;

				default:
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
