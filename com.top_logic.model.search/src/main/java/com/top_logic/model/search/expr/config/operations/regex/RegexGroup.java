/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.regex;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SimpleGenericMethod;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} accessing a certain group of a {@link Regex} match.
 * 
 * <p>
 * The result is the part of the input text matched by the group with a given group index.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RegexGroup extends SimpleGenericMethod {

	/**
	 * Creates a {@link RegexGroup}.
	 */
	protected RegexGroup(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new RegexGroup(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Object match = arguments[0];
		if (match == null) {
			return null;
		}

		int groupId = asInt(arguments[1]);
		if (match instanceof Collection<?>) {
			return ((Collection<?>) match).stream().map(m -> getGroup(m, groupId)).collect(Collectors.toList());
		} else {
			return getGroup(match, groupId);
		}
	}

	private Object getGroup(Object base, int groupId) {
		Match match = (Match) base;
		return match.value(groupId);
	}

	/**
	 * {@link MethodBuilder} creating {@link RegexGroup}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RegexGroup> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("match")
			.optional("index", 0)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public RegexGroup build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new RegexGroup(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
