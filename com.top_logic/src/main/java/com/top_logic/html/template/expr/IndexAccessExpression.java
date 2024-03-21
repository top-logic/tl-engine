/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import java.util.List;
import java.util.Map;

import com.top_logic.html.template.TemplateExpression;
import com.top_logic.html.template.TemplateNode;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.util.error.TopLogicException;

/**
 * Access to a model variable to be either directly rendered or used in a
 * {@link TemplateExpression}.
 */
public class IndexAccessExpression extends TemplateNode implements TemplateExpression {

	private TemplateExpression _base;

	private TemplateExpression _index;

	/**
	 * Creates a {@link IndexAccessExpression}.
	 * 
	 * @param base
	 *        The expression delivering a map or value with properties.
	 * @param index
	 *        The expression delivering the index value to access the given base object with.
	 */
	public IndexAccessExpression(int line, int column, TemplateExpression base, TemplateExpression index) {
		super(line, column);
		_base = base;
		_index = index;
	}

	/**
	 * The expression delivering a value with properties.
	 */
	public TemplateExpression getBase() {
		return _base;
	}

	/**
	 * The expression computing the index to access.
	 */
	public TemplateExpression getIndex() {
		return _index;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		Object baseValue = getBase().eval(context, properties);
		Object indexValue = getIndex().eval(context, properties);
		if (baseValue == null) {
			return null;
		}
		if (indexValue == null) {
			return baseValue;
		}
		if (baseValue instanceof WithProperties) {
			String property = as(String.class, indexValue, this);
			try {
				return ((WithProperties) baseValue).getPropertyValue(property);
			} catch (NoSuchPropertyException ex) {
				throw WithProperties.reportError(properties, property);
			}
		}
		if (baseValue instanceof Map<?, ?>) {
			return ((Map<?, ?>) baseValue).get(indexValue);
		}
		if (baseValue instanceof List<?>) {
			return ((List<?>) baseValue).get(as(Number.class, indexValue, this).intValue());
		}

		throw new TopLogicException(
			I18NConstants.ERROR_VALUE_HAS_NO_PROPERTIES__VALUE_LOCATION.fill(baseValue, location(this)));
	}

	static <T> T as(Class<T> type, Object value, TemplateNode expr) {
		if (type.isInstance(value)) {
			return type.cast(value);
		}

		throw new TopLogicException(
			I18NConstants.ERROR_UNEXPECTED_TYPE__VALUE_TYPE_LOCATION.fill(value, type, location(expr)));
	}

	static Object location(TemplateNode expr) {
		return I18NConstants.LOCATION__LINE_COL.fill(expr.getLine(), expr.getColumn());
	}

	@Override
	public <R, A> R visit(Visitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
