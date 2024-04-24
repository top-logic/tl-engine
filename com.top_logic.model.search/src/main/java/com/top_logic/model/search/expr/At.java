/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.util.error.TopLogicException;

/**
 * Collection access expression.
 * 
 * @see SearchExpressionFactory#at(SearchExpression, SearchExpression)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class At extends SearchExpression {

	private SearchExpression _self;

	private SearchExpression _index;

	/**
	 * Creates an {@link At} expression.
	 */
	At(SearchExpression self, SearchExpression index) {
		_self = self;
		_index = index;
	}

	/**
	 * The collection/object being accessed.
	 */
	public SearchExpression getSelf() {
		return _self;
	}

	/**
	 * @see #getSelf()
	 */
	public void setSelf(SearchExpression self) {
		_self = self;
	}

	/**
	 * The expression creating the index value.
	 */
	public SearchExpression getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public void setIndex(SearchExpression index) {
		_index = index;
	}

	@Override
	protected Object internalEval(EvalContext definitions, Args args) {
		Object self = getSelf().eval(definitions);
		Object indexArg = getIndex().eval(definitions);

		if (self == null) {
			return null;
		} else if (self instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) self;
			return SearchExpression.normalizeValue(map.get(indexArg));
		} else if (self instanceof List<?>) {
			List<?> list = (List<?>) self;
			int index = asInt(indexArg);
			try {
				return SearchExpression.normalizeValue(list.get(index));
			} catch (IndexOutOfBoundsException ex) {
				throw new TopLogicException(
					I18NConstants.ERROR_INVALID_LIST_INDEX__LIST_INDEX_EXPR.fill(list, index, this));
			}
		} else if (self instanceof TLObject) {
			TLObject obj = (TLObject) self;
			if (indexArg instanceof TLStructuredTypePart) {
				return SearchExpression.normalizeValue(obj.tValue((TLStructuredTypePart) indexArg));
			} else {
				String propertyName = asString(indexArg);
				TLStructuredTypePart property = obj.tType().getPart(propertyName);
				if (property == null) {
					throw new TopLogicException(
						I18NConstants.ERROR_NO_SUCH_PROPERTY__OBJ_NAME_EXPR.fill(obj, propertyName, this));
				}
				return SearchExpression.normalizeValue(obj.tValueByName(propertyName));
			}
		} else if (self instanceof ConfigurationItem) {
			ConfigurationItem config = (ConfigurationItem) self;
			String propertyName = asString(indexArg);
			switch (propertyName) {
				case "$tag":
					TagName annotation = config.descriptor().getConfigurationInterface().getAnnotation(TagName.class);
					return annotation == null ? null : annotation.value();
				case "$intf":
					return config.descriptor().getConfigurationInterface().getName();
				case "$class":
					return (config instanceof PolymorphicConfiguration<?>)
						? ((PolymorphicConfiguration<?>) config).getImplementationClass().getName()
						: null;
				default:
					PropertyDescriptor property = config.descriptor().getProperty(propertyName);
					if (property == null) {
						throw new TopLogicException(
							I18NConstants.ERROR_NO_SUCH_PROPERTY__OBJ_NAME_EXPR.fill(
								"Config<" + config.descriptor().getConfigurationInterface().getName() + ">",
								propertyName, this));
					}
					return config.value(property);
			}
		} else {
			throw new TopLogicException(I18NConstants.ERROR_LIST_MAP_OR_OBJECT_REQUIRED__VALUE_EXPR.fill(self, this));
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitAt(this, arg);
	}
}
