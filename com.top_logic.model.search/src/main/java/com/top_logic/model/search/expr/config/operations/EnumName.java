/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} determining the name of an {@link Enum} constant.
 */
public class EnumName extends GenericMethod {

	/**
	 * Creates a {@link EnumName}.
	 */
	protected EnumName(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new EnumName(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object value = arguments[0];
		if (value == null) {
			return null;
		}
		if (!value.getClass().isEnum()) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_AN_ENUM__VALUE__EXPR.fill(value, getArguments()[0]));

		}
		return ConfigUtil.getEnumExternalName((Enum<?>) value);
	}

	/**
	 * {@link MethodBuilder} creating {@link EnumName}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<EnumName> {
		/** Description of parameters for a {@link EnumName}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("enum")
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
		public EnumName build(Expr expr, SearchExpression[] args) {
			return new EnumName(getConfig().getName(), args);
		}

	}
}
