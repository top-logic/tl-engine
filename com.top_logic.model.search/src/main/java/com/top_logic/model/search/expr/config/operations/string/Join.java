/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Joins a list into a string separating entries with a given separator.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Join extends GenericMethod {

	/**
	 * Creates a {@link Join}.
	 */
	protected Join(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Join(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> input = asCollection(arguments[0]);
		String separator = asString(arguments[1]);
		String prefix = asString(arguments[2]);
		String suffix = asString(arguments[3]);

		StringBuilder result = new StringBuilder();

		result.append(prefix);
		boolean first = true;
		appendAll(result, input, first, separator);
		result.append(suffix);

		return result.toString();
	}

	private boolean appendAll(StringBuilder result, Collection<?> input, boolean first, String separator) {
		for (Object element : input) {
			if (element instanceof Collection<?> collection) {
				// Flatten.
				first = appendAll(result, collection, first, separator);
			} else {
				if (first) {
					first = false;
				} else {
					result.append(separator);
				}
				result.append(ToString.toString(element));
			}
		}
		return first;
	}

	/**
	 * {@link MethodBuilder} creating {@link Join}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Join> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("separator", ", ")
			.optional("prefix", "")
			.optional("suffix", "")
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
		public Join build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Join(getConfig().getName(), args);
		}

	}

}
