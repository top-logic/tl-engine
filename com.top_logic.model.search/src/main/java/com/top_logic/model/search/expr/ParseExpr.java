/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.Format;
import java.text.ParseException;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} parsing a given string value accoring to a given {@link Format}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseExpr extends SimpleGenericMethod {

	/**
	 * Creates a {@link ParseExpr}.
	 */
	protected ParseExpr(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ParseExpr(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Format format = (Format) arguments[0];
		String arg = asString(arguments[1]);
		try {
			return normalizeValue(format.parseObject(arg));
		} catch (ParseException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_FORMAT__STR_EXPR_MSG.fill(arg, this, ex.getMessage()));
		}
	}

	/**
	 * {@link MethodBuilder} for {@link ParseExpr}s.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ParseExpr> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ParseExpr build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new ParseExpr(getName(), self, args);
		}

	}

}
