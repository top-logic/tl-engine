/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} for applying a {@link Format} to a value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormatExpr extends SimpleGenericMethod {

	/**
	 * Creates a {@link FormatExpr}.
	 */
	protected FormatExpr(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new FormatExpr(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Format format = (Format) arguments[0];
		if (arguments.length == 2) {
			Object first = arguments[1];
			if (first instanceof Collection<?>) {
				return format.format(((Collection<?>) first).toArray());
			} else {
				if (format instanceof MessageFormat) {
					return format.format(new Object[] { first });
				} else {
					return format.format(first);
				}
			}
		} else {
			return format.format(Arrays.copyOfRange(arguments, 1, arguments.length));
		}
	}

	/**
	 * {@link MethodBuilder} for {@link FormatExpr}s.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<FormatExpr> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public FormatExpr build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new FormatExpr(getName(), args);
		}

	}

}
