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
 * {@link GenericMethod} accessing the end position of a {@link Regex} {@link Match}.
 * 
 * @see Match.Group#getEnd()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RegexEnd extends SimpleGenericMethod {

	/**
	 * Creates a {@link RegexEnd}.
	 */
	protected RegexEnd(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new RegexEnd(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.INTEGER_TYPE);
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		Object match = arguments[0];
		if (match == null) {
			return null;
		}

		int groupId = asInt(arguments[1]);
		if (match instanceof Collection<?>) {
			return ((Collection<?>) match).stream().map(m -> getEnd(m, groupId)).collect(Collectors.toList());
		} else {
			return getEnd(match, groupId);
		}
	}

	private Object getEnd(Object base, int groupId) {
		Match match = (Match) base;
		return toNumber(match.group(groupId).getEnd());
	}

	/**
	 * {@link MethodBuilder} creating {@link RegexEnd}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RegexEnd> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("match")
			.optional("groupId", 0)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public RegexEnd build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			return new RegexEnd(getConfig().getName(), self, args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
