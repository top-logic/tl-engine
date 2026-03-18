/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.persistency.attribute.expr.ExprStorageMapping;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} determining an {@link Enum} constant by its name.
 */
public class ParseScript extends GenericMethod {

	/**
	 * Creates a {@link ParseScript}.
	 */
	protected ParseScript(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ParseScript(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType("tl.model.search:Expr");
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String script = asString(arguments[0]);
		return ExprStorageMapping.INSTANCE.getBusinessObject(script);
	}

	/**
	 * {@link MethodBuilder} creating {@link ParseScript}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ParseScript> {
		/** Description of parameters for a {@link ParseScript}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("script")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ParseScript build(Expr expr, SearchExpression[] args) {
			return new ParseScript(getConfig().getName(), args);
		}

	}
}
