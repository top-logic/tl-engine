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
 * {@link GenericMethod} accessing the start position of a {@link Regex} {@link Match}.
 * 
 * @see Match.Group#getStart()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RegexStart extends SimpleGenericMethod {

	/**
	 * Creates a {@link RegexStart}.
	 */
	protected RegexStart(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new RegexStart(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.INTEGER_TYPE);
	}

	@Override
	public Object eval(Object[] arguments) {
		Object match = arguments[0];
		if (match == null) {
			return null;
		}

		int groupId = asInt(arguments[1]);
		if (match instanceof Collection<?>) {
			return ((Collection<?>) match).stream().map(m -> getStart(m, groupId)).collect(Collectors.toList());
		} else {
			return getStart(match, groupId);
		}
	}

	private Object getStart(Object self, int groupId) {
		Match match = (Match) self;
		return toNumber(match.group(groupId).getStart());
	}

	/**
	 * {@link MethodBuilder} creating {@link RegexStart}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RegexStart> {

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
		public RegexStart build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new RegexStart(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
