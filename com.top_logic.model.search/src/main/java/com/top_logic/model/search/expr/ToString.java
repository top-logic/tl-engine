package com.top_logic.model.search.expr;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * {@link GenericMethod} converting a value, or concatenating a list of values to a single string.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToString extends SimpleGenericMethod {

	/** 
	 * Creates a {@link ToString}.
	 */
	protected ToString(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ToString(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Object base = arguments[0];
		if (arguments.length > 1 || base instanceof Collection<?>) {
			StringBuilder result = new StringBuilder();
			for (Object arg : arguments) {
				append(result, arg);
			}
			return result.toString();
		} else {
			return toString(base);
		}
	}

	private void append(StringBuilder result, Object arg) {
		if (arg instanceof Collection<?>) {
			for (Object element : (Collection<?>) arg) {
				append(result, element);
			}
		} else {
			result.append(toString(arg));
		}
	}

	/**
	 * Converts a value to a string / label.
	 */
	public static String toString(Object arg) {
		if (arg == null) {
			return "";
		}
		return MetaLabelProvider.INSTANCE.getLabel(arg);
	}

	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		for (Object argument : arguments) {
			if (!isPrimitive(argument)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isPrimitive(Object argument) {
		return (argument instanceof Number)
			|| (argument instanceof CharSequence)
			|| (argument instanceof Boolean)
			|| (argument instanceof Character)
			|| (argument == null);
	}

	/**
	 * {@link MethodBuilder} for {@link ToString}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ToString> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ToString build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			return new ToString(getName(), args);
		}

	}
}
