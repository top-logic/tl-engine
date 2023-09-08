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
	protected RegexGroup(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new RegexGroup(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		if (self == null) {
			return null;
		}

		int groupId = arguments.length > 0 ? asInt(arguments[0]) : 0;
		if (self instanceof Collection<?>) {
			return ((Collection<?>) self).stream().map(m -> getGroup(m, groupId)).collect(Collectors.toList());
		} else {
			return getGroup(self, groupId);
		}
	}

	private Object getGroup(Object self, int groupId) {
		Match match = (Match) self;
		return match.value(groupId);
	}

	/**
	 * {@link MethodBuilder} creating {@link RegexGroup}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RegexGroup> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public RegexGroup build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException {
			checkMaxArgs(expr, args, 1);
			return new RegexGroup(getConfig().getName(), self, args);
		}
	}
}
