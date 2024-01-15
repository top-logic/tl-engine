/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.TagWriter.State;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DispatchingRenderer;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} that renders a {@link TemplateExpression template-computed value}.
 */
public class ExpressionTemplate implements RawTemplateFragment {

	private final TemplateExpression _expression;

	/**
	 * Creates a {@link ExpressionTemplate}.
	 * 
	 * @param expression
	 *        The expression whose value should be rendered.
	 */
	public ExpressionTemplate(TemplateExpression expression) {
		_expression = expression;
	}

	/**
	 * The expression to evaluate and render its result.
	 */
	public TemplateExpression getExpression() {
		return _expression;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		Object value;
		try {
			value = _expression.eval(context, properties);
		} catch (RuntimeException exception) {
			if (out.getState() != State.ELEMENT_CONTENT) {
				throw exception;
			}
			HTMLTemplateUtils.renderError(context, out, exception);
			return;
		}

		renderValue(context, out, value);
	}

	/**
	 * Renders a given plain value in the given context.
	 */
	public static void renderValue(DisplayContext context, TagWriter out, Object value)
			throws IOException {
		if (value == null) {
			return;
		} else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			// Do not use label provider for simple types, since primitive values such as numbers
			// must not be rendered in an internationalized way but for technical interpretation of
			// the browser.
			out.append(value.toString());
		} else if (value instanceof HTMLFragment) {
			((HTMLFragment) value).write(context, out);
		} else {
			switch (out.getState()) {
				case ELEMENT_CONTENT:
					DispatchingRenderer.INSTANCE.write(context, out, value);
					break;

				default:
					out.append(MetaLabelProvider.INSTANCE.getLabel(value));
					break;
			}
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
