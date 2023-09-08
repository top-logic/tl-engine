/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.html;

import java.io.IOException;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.TagWriter.State;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.LabelRenderer;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.util.error.TopLogicException;

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

	private boolean _dynamicSafety;

	/**
	 * Creates a {@link AttributeMacro}.
	 */
	public AttributeMacro(boolean cssAttribute, boolean dynamicSafety, String name, SearchExpression value) {
		_cssAttribute = cssAttribute;
		_dynamicSafety = dynamicSafety;
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
	 * Whether the attribute is written with buffering to dynamically check its contents for safety.
	 * 
	 * @see SafeHTML#checkAttribute(String, String, String)
	 */
	public boolean useDynamicSafety() {
		return _dynamicSafety;
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
		if (_dynamicSafety) {
			TagWriter attrOut = new TagWriter();
			attrOut.setState(_cssAttribute ? State.CLASS_ATTRIBUTE : State.ATTRIBUTE);

			TagWriter outBefore = definitions.setOut(attrOut);
			try {
				writeAttribute(context, definitions, attrOut);
			} finally {
				definitions.setOut(outBefore);
			}

			String bufferedValue = attrOut.toString();
			try {
				SafeHTML.getInstance().checkAttributeValue(_name, bufferedValue);
			} catch (I18NException ex) {
				throw wrap(ex, _name, bufferedValue);
			}
			if (_cssAttribute) {
				out.beginCssClasses(_name);
				out.append(bufferedValue.trim());
				out.endCssClasses();
			} else {
				out.writeAttribute(_name, bufferedValue);
			}
		} else {
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
	}

	private RuntimeException wrap(I18NException ex, String name, String value) {
		throw new TopLogicException(I18NConstants.ERROR_UNSAFE_HTML__ATTRIBUTE__VALUE.fill(name, value), ex);
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
