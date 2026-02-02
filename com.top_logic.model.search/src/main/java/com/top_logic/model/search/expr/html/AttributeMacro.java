/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.html;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.LabelRenderer;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link RenderExpression} creating an attribute of a {@link TagMacro}.
 * 
 * @see TagMacro#getAttributes()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeMacro extends RenderExpression {

	private String _name;

	private SearchExpression _value;

	private boolean _cssAttribute;

	/**
	 * Creates a {@link AttributeMacro}.
	 */
	public AttributeMacro(boolean cssAttribute, String name, SearchExpression value) {
		_cssAttribute = cssAttribute;
		_name = name;
		_value = value;
	}

	/**
	 * Whether separate values are separated by spaces automatically.
	 */
	public boolean isCssAttribute() {
		return _cssAttribute;
	}

	/**
	 * The attribute name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * The expression producing the attribute value.
	 */
	public SearchExpression getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(SearchExpression value) {
		_value = value;
	}

	@Override
	protected void write(DisplayContext context, TagWriter out, Args args, EvalContext definitions) throws IOException {
		if (_cssAttribute) {
			out.beginCssClasses(_name);
			writeAttribute(context, definitions, out);
			out.endCssClasses();
		} else {
			out.beginAttribute(_name);
			writeAttribute(context, definitions, out);
			out.endAttribute();
		}
	}

	private void writeAttribute(DisplayContext context, EvalContext definitions, TagWriter out)
			throws IOException {
		Renderer<Object> rendererBefore = definitions.setRenderer(LabelRenderer.INSTANCE);
		try {
			Object value = _value.eval(definitions);
			writeValue(context, out, definitions.getRenderer(), value);
		} finally {
			definitions.setRenderer(rendererBefore);
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitAttr(this, arg);
	}

}
