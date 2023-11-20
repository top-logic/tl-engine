/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Part of a list from a start index (inclusive) to a stop index (exclusive).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubList extends GenericMethod {

	/**
	 * Creates a {@link SubList}.
	 */
	protected SubList(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new SubList(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		List<?> list = asList(arguments[0]);
		int beginIndex = index(list, arguments[1]);
		int endIndex = arguments.length < 3 ? list.size() : index(list, arguments[2]);

		// Special case for using the semantics "negative indices count from the end of the list".
		// (Negative) zero means the end of the list, if the range would be invalid otherwise.
		if (endIndex == 0 && endIndex < beginIndex) {
			endIndex = list.size();
		}
		return list.subList(beginIndex, endIndex);
	}

	private int index(List<?> list, Object index) {
		int result = asInt(index);
		if (result < 0) {
			result = Math.max(0, list.size() + result);
		}
		if (result > list.size()) {
			result = list.size();
		}
		return result;
	}

	/**
	 * {@link MethodBuilder} creating {@link SubList}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<SubList> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public SubList build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkArgs(expr, args, 2, 3);
			return new SubList("subList", args);
		}

	}
}
