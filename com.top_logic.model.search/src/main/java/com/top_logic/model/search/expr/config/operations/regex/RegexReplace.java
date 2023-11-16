/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.regex;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * {@link GenericMethod} Replacing all matches of a {@link Regex} pattern in a target string.
 * 
 * <p>
 * The result is a string with all matches of the matched pattern replaced by a given replacement
 * string. The replacement string can also be dynamically computed by passing a replacement function
 * as second argument.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RegexReplace extends GenericMethod {

	/**
	 * Creates a {@link RegexReplace}.
	 */
	protected RegexReplace(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new RegexReplace(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[1];
		if (input == null) {
			return null;
		}
		Pattern pattern = (Pattern) arguments[0];
		String text = asString(input);
		Object replacement = arguments[2];

		Matcher matcher = pattern.matcher(text);
		StringBuffer buffer = new StringBuffer();
		if (replacement instanceof SearchExpression) {
			SearchExpression replacementExpr = (SearchExpression) replacement;
			while (matcher.find()) {
				Object replacementResult = replacementExpr.eval(definitions, Match.from(text, matcher));
				String replacementString = asString(replacementResult);
				matcher.appendReplacement(buffer, replacementString);
			}
		} else {
			String replacementString = asString(replacement);
			while (matcher.find()) {
				matcher.appendReplacement(buffer, replacementString);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/**
	 * {@link MethodBuilder} creating {@link RegexReplace}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RegexReplace> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("pattern")
			.optional("text")
			.optional("replacement")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public RegexReplace build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			checkThreeArgs(expr, args);
			return new RegexReplace(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
