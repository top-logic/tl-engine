/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Creates a {@link StructType} to be instantiated with {@link NewStruct}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructTypeDef extends SimpleGenericMethod {

	/**
	 * Creates a {@link StructTypeDef}.
	 */
	protected StructTypeDef(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new StructTypeDef(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public Object eval(Object[] arguments) {
		return new StructType(this, arguments);
	}

	/**
	 * {@link MethodBuilder} creating {@link StructTypeDef}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<StructTypeDef> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public StructTypeDef build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkNoTarget(expr, self);
			return new StructTypeDef(getConfig().getName(), self, args);
		}

	}

}
