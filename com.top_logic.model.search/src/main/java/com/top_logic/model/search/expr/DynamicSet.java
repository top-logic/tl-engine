/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;

/**
 * {@link GenericMethod} updating a property of a {@link TLObject} where the
 * {@link TLStructuredTypePart part} to set the value to is dynamically given by a
 * {@link SearchExpression}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DynamicSet extends GenericMethod {

	/**
	 * Creates a new {@link DynamicSet}.
	 */
	public DynamicSet(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new DynamicSet(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject obj = asTLObjectNonNull(arguments[0]);
		TLStructuredTypePart part = asTypePart(getArguments()[1], arguments[1]);
		Object value = arguments[2];
		obj.tUpdate(part, value);
		return null;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link DynamicSet}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<DynamicSet> {

		/**
		 * Configuration for the {@link Builder}.
		 */
		public interface Config extends AbstractSimpleMethodBuilder.Config<Builder> {

			@StringDefault("dynamicSet")
			@Override
			String getName();

		}

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public DynamicSet build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkThreeArgs(expr, args);
			return new DynamicSet(getConfig().getName(), self, args);
		}

	}

}
