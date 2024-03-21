/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import java.util.Map;

import com.top_logic.html.template.TemplateExpression;
import com.top_logic.html.template.TemplateNode;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Access to a model variable to be either directly rendered or used in a
 * {@link TemplateExpression}.
 */
public class PropertyAccessExpression extends TemplateNode implements TemplateExpression {

	private TemplateExpression _base;
	private final String _name;

	/**
	 * Creates a {@link PropertyAccessExpression}.
	 * 
	 * @param base
	 *        The expression delivering a value with properties.
	 * @param name
	 *        The name of the model property to access.
	 */
	public PropertyAccessExpression(int line, int column, TemplateExpression base, String name) {
		super(line, column);
		_base = base;
		_name = name;
	}

	/**
	 * The expression delivering a value with properties.
	 */
	public TemplateExpression getBase() {
		return _base;
	}

	/**
	 * The name of the accessed variable.
	 */
	public String getName() {
		return _name;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		Object baseValue = getBase().eval(context, properties);
		if (baseValue == null) {
			return null;
		}
		if (baseValue instanceof WithProperties) {
			try {
				return ((WithProperties) baseValue).getPropertyValue(_name);
			} catch (NoSuchPropertyException ex) {
				throw WithProperties.reportError(properties, _name);
			}
		}
		if (baseValue instanceof Map<?, ?>) {
			return ((Map<?, ?>) baseValue).get(_name);
		}

		throw new IllegalArgumentException("Value '" + baseValue + "' has no properties.");
	}

	@Override
	public <R, A> R visit(Visitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
