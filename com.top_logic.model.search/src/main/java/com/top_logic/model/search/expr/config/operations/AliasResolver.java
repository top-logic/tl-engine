/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * Generic method resolving an alias using the {@link AliasManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AliasResolver extends GenericMethod {

	/**
	 * Creates a new {@link AliasResolver}.
	 */
	protected AliasResolver(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new AliasResolver(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		String alias = asString(arguments[0]);
		if (alias == null) {
			return null;
		}
		return AliasManager.getInstance().replace(alias);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link AliasResolver}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<AliasResolver> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public AliasResolver build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new AliasResolver(getConfig().getName(), self, args);
		}

		@Override
		public boolean hasSelf() {
			return false;
		}
	}

}

