/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Converts a given string to lower case.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToUpperCase extends SimpleGenericMethod {

	/**
	 * Creates a {@link ToUpperCase}.
	 */
	protected ToUpperCase(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ToUpperCase(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		String str = asString(self);
		return str.toUpperCase();
	}

	/**
	 * {@link MethodBuilder} creating {@link ToUpperCase}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToUpperCase> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ToUpperCase build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, self, args);
			return new ToUpperCase(getConfig().getName(), self, args);
		}
	}

}
