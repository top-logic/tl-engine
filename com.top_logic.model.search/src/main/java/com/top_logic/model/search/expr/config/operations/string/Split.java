/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Splits a string around separators into a list of strings.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Split extends GenericMethod {

	/**
	 * Creates a {@link Split}.
	 */
	protected Split(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Split(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String input = asString(arguments[0]);
		Object separator = arguments[1];
		boolean trim = asBoolean(arguments[2]);

		if (separator == null) {
			return input;
		}

		Pattern pattern;
		if (separator instanceof Pattern) {
			pattern = (Pattern) separator;
		} else {
			String patternSrc = asString(separator);
			patternSrc = Pattern.quote(patternSrc);
			pattern = Pattern.compile(patternSrc);
		}

		Stream<String> result = pattern.splitAsStream(input);
		if (trim) {
			result = result.map(String::strip);
		}
		return result.collect(Collectors.toList());
	}

	/**
	 * {@link MethodBuilder} creating {@link Split}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Split> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("separator", ",")
			.optional("trim", true)
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
		public Split build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new Split(getConfig().getName(), args);
		}

	}

}
