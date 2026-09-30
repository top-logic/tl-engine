/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} determining an {@link Enum} constant by its name.
 */
public class ResolveEnum extends GenericMethod {

	/**
	 * Creates a {@link ResolveEnum}.
	 */
	protected ResolveEnum(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ResolveEnum(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String type = asString(arguments[0]);
		String name = asString(arguments[1]);

		try {
			@SuppressWarnings("rawtypes")
			Class<? extends Enum> enumType = ConfigUtil.getClassForNameMandatory(Enum.class, "type", type);
			return ConfigUtil.getEnum(enumType, name);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link ResolveEnum}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ResolveEnum> {
		/** Description of parameters for a {@link ResolveEnum}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("type")
			.mandatory("name")
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
		public ResolveEnum build(Expr expr, SearchExpression[] args) {
			return new ResolveEnum(getConfig().getName(), args);
		}

	}
}
